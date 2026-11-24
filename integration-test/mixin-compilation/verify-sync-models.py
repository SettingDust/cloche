"""R4M: complete getter/identity closure and physical compile/runtime consumption.
Usage: python verify-sync-models.py BASELINE OFF ON OUTPUT_DIRECTORY [EXTRA_FIXTURE ...]
Raw getter differences are retained. Semantic comparison accounts for native ordering,
file-collection grouping, and COMPILE = compile + runtime purpose, with exact files
independently checked before any clean->raw identity substitution.
"""
import copy
import hashlib
import json
from pathlib import Path
import re
import sys
import zipfile
from typing import Any


def load(root, name):
    return json.loads((root / name).read_text(encoding="utf-8"))


def objects(value):
    if isinstance(value, dict):
        yield value
        for item in value.values():
            yield from objects(item)
    elif isinstance(value, list):
        for item in value:
            yield from objects(item)


def expanded(value) -> Any:
    ids = {node["$id"]: node for node in objects(value) if "$id" in node}
    def visit(v, stack=()):
        if isinstance(v, list):
            return [visit(item, stack) for item in v]
        if not isinstance(v, dict):
            return v
        if "$ref" in v:
            v = ids[v["$ref"]]
        identity = v.get("$id")
        if identity is not None and identity in stack:
            return {"$cycle": v.get("$type")}
        return {k: visit(item, stack + ((identity,) if identity is not None else ())) for k, item in v.items() if k != "$id"}
    return visit(value)


def differences(a, b, path=""):
    if type(a) is not type(b):
        return [{"path": path, "baseline": a, "candidate": b}]
    if isinstance(a, dict):
        if a.keys() != b.keys():
            return [{"path": path + "/<keys>", "baseline": sorted(a), "candidate": sorted(b)}]
        return [d for key in a for d in differences(a[key], b[key], path + "/" + key)]
    if isinstance(a, list):
        if len(a) != len(b):
            return [{"path": path + "/<members>", "baseline": a, "candidate": b}]
        return [d for i, (x, y) in enumerate(zip(a, b)) for d in differences(x, y, path + "/" + str(i))]
    return [] if a == b else [{"path": path, "baseline": a, "candidate": b}]


def normalize(value, root, replacements=()):
    def text(s):
        s = s.replace("\\", "/")
        for original, substitute in replacements:
            s = s.replace(original.replace("\\", "/"), substitute.replace("\\", "/"))
        s = s.replace(str(root).replace("\\", "/"), "<ROOT>")
        return re.sub(r"(?<=/transforms/)[0-9a-f]{32}(?=/)", "<workspace>", s)
    if isinstance(value, str):
        return text(value)
    if isinstance(value, list):
        return [normalize(v, root, replacements) for v in value]
    if isinstance(value, dict):
        return {text(k): normalize(v, root, replacements) for k, v in value.items()}
    return value


def key(value):
    return json.dumps(value, sort_keys=True, separators=(",", ":"))


def unordered(value):
    """Native membership order is not an identity or consumption purpose."""
    if isinstance(value, list):
        return sorted((unordered(v) for v in value), key=key)
    if isinstance(value, dict):
        return {k: unordered(v) for k, v in value.items()}
    return value


PURPOSES = {"COMPILE": {"compile", "runtime"}, "PROVIDED": {"compile"}, "RUNTIME": {"runtime"}, "TEST": {"compile", "runtime"}}


def dependency_graph(roots):
    """Every getter contributes to node identity, except documented scope/order/edges.
    Preserve each root and closure edge with purpose. File collections are attributed
    to every exact member; only their path-derived group names are regenerated.
    """
    nodes, edges = {}, {}
    def visit(node, parent="<SourceSet>", inherited=None):
        uses = PURPOSES[node["getScope"]]
        if inherited is not None:
            uses = uses & inherited
        children = node.get("getDependencies", [])
        identity = {k: v for k, v in node.items() if k not in ("getScope", "getClasspathOrder", "getDependencies")}
        identities = [identity]
        if "getFiles" in identity:
            identities = []
            members = identity["getFiles"]
            for file in members:
                atom = copy.deepcopy(identity)
                atom["getFiles"] = [file]
                atom["getName"] = "[" + file + "]"
                atom["getId"]["getName"] = atom["getName"]
                atom["getId"]["getPresentableName"] = atom["getName"]
                identities.append(atom)
            if not members:
                identities.append(identity)
        for identity in identities:
            identity = unordered(identity)
            identifier = key(identity)
            nodes.setdefault(identifier, set()).update(uses)
            edges.setdefault((parent, identifier), set()).update(uses)
            for child in children:
                visit(child, identifier, uses)
    for node in roots:
        visit(node)
    return {"nodes": [{"identity": json.loads(k), "uses": sorted(v)} for k, v in sorted(nodes.items())],
            "edges": [{"from": a, "to": b, "uses": sorted(v)} for (a, b), v in sorted(edges.items())]}


def replace_compile_games(roots, old, new):
    """Replace only exact compile game members, retaining runtime and every other getter/edge."""
    for node in roots:
        children = node.get("getDependencies", [])
        replace_compile_games(children, old, new)
    for node in list(roots):
        if "getFiles" not in node or "compile" not in PURPOSES[node["getScope"]]:
            continue
        if not set(node["getFiles"]) & set(old):
            continue
        if "runtime" in PURPOSES[node["getScope"]]:
            runtime = copy.deepcopy(node)
            runtime["getScope"] = "RUNTIME"
            roots.append(runtime)
        node["getScope"] = "PROVIDED"
        node["getFiles"] = [p for p in node["getFiles"] if p not in old] + list(new)


def semantic_model(model):
    model = copy.deepcopy(model)
    for project in model.values():
        project["dependencies"]["getDependencies"] = {role: dependency_graph(nodes) for role, nodes in project["dependencies"]["getDependencies"].items()}
    return unordered(model)


def zip_entries(path):
    with zipfile.ZipFile(path) as archive:
        return {n: archive.read(n) for n in archive.namelist() if not n.endswith("/")}


def digest(path):
    return hashlib.sha256(b"".join(n.encode() + b for n, b in sorted(zip_entries(path).items()))).hexdigest()


def exact_strip(raw, clean, notifications):
    before, after = zip_entries(raw), zip_entries(clean)
    if before.keys() != after.keys():
        return False
    for name, data in before.items():
        if any(name in notice for notice in notifications):
            old, new = json.loads(data), json.loads(after[name])
            expected = copy.deepcopy(old)
            for notice in notifications:
                for section in ("mixins", "client", "server"):
                    if section in expected:
                        expected[section] = [v for v in expected[section] if v not in notice.get(name, [])]
            if expected != new:
                return False
        elif data != after[name]:
            return False
    return True


def dep_files(node):
    return ([node["getFile"]] if "getFile" in node else node.get("getFiles", []))


def compilation_rows(root):
    if (root / "sync-observation.json").exists():
        return load(root, "sync-observation.json")["compilations"]
    return {c["sourceSetName"]: c for c in load(root, "full-model-observation.json")["compilations"]}


def verify_fixture(root, model, checks, records):
    tree = expanded(model["models"])
    rows = compilation_rows(root)
    log = (root / "model.log").read_text(encoding="utf-8")
    checks["same_request_production_before_builders"] = min(log.find(x) for x in ["VERIFIED_SYNC" if (root / "sync-observation.json").exists() else "VERIFIED_FULL_MODEL_INPUTS"]) >= 0 and log.index("R4M_BUILDER") > log.index("VERIFIED_") and "R4M_STRICT_READ_COMPLETE" in log
    calls = model["builderCalls"]
    projects = list(tree)
    checks["all_projects_all_builders"] = len(calls) == len(projects) * 4 and all(len([c for c in calls if c["project"] == p]) == 4 for p in projects)
    root_roles = tree[":"]["dependencies"]["getDependencies"]
    for role, c in rows.items():
        nodes = list(objects(root_roles[role]))
        compile_files = {f for n in nodes if "getScope" in n and "compile" in PURPOSES[n["getScope"]] for f in dep_files(n)}
        runtime_files = {f for n in nodes if "getScope" in n and "runtime" in PURPOSES[n["getScope"]] for f in dep_files(n)}
        strict = model["nativeViews"]["::" + role]
        def check(name, condition):
            checks[role + ":" + name] = bool(condition)
        check("strict_C_view_SourceSet", set(strict["C"]) == {r["file"] for r in c["C"]} and set(strict["view"]) == {r["file"] for r in c["view"]} and set(strict["sourceSet"]) == set(c["sourceSet"]))
        check("all_external_compile_files_in_model", all(r["file"] in compile_files for r in c["view"]))
        check("ordered_effective_shards", all(p in compile_files and p in strict["sourceSet"] and Path(p).is_file() for p in c["games"]) and [p for p in strict["sourceSet"] if p in c["games"]] == c["games"])
        check("runtime_original_chain", not any("mixinStripped" in p for p in c["runtime"]) and c["runtimeGame"] in c["runtime"])
        check("C_view_SourceSet_producers", set(strict["Cdeps"]) == set(c["Cdeps"]) and set(strict["viewDeps"]) == set(c["viewDeps"]) and set(strict["sourceSetDeps"]) == set(c["sourceSetDeps"]))
        check("parameter_collection_producers", set(c["parameters"]["C"]) == set(c["Cdeps"]) and set(c["parameters"]["sourceSet"]) == set(c["sourceSetDeps"]))
        check("runtimeOnly_not_compile", not any(n.get("getGroup") == "probe" and n.get("getName") == "runtime" and "compile" in PURPOSES[n["getScope"]] for n in nodes if "getScope" in n))
        check("compileOnly_not_runtime", not any(n.get("getGroup") == "probe" and n.get("getName") == "ref" and n.get("getClassifier") is None and "runtime" in PURPOSES[n["getScope"]] for n in nodes if "getScope" in n))
        if any(r["component"] == "probe:ref:1" and "ref-1-extra" in r["id"] for r in c["C"]):
            extra = [n for n in nodes if n.get("getGroup") == "probe" and n.get("getName") == "ref" and n.get("getClassifier") == "extra" and "getScope" in n]
            check("ordinary_classifier_compile_and_runtime", bool(extra) and {purpose for n in extra for purpose in PURPOSES[n["getScope"]]} == {"compile", "runtime"})
        selected = c["cleanSources"]
        check("producer_in_graph", not selected or c["parameters"]["operation"] in (load(root,"sync-observation.json") if (root / "sync-observation.json").exists() else load(root,"full-model-observation.json"))["graph"])
        if selected:
            shards = c["shards"]
            notices = [load(Path(shard["notification"]).parent, Path(shard["notification"]).name) for shard in shards]
            check("independent_notifications", len({shard["notification"] for shard in shards}) == len(shards))
            check("parameter_provider", all(shard["operation"] in shard["producers"] and shard["operation"] in c["parameters"]["game"] for shard in shards))
            check("game_exact_sequential_strip", len(shards) == len(c["games"]) and all(exact_strip(shard["produced"], game, notices) for shard, game in zip(reversed(shards), c["games"])))
            check("old_game_not_compile", c["runtimeGame"] not in compile_files and not any(p in compile_files for p in c["runtimeExtras"]))
            for shard in shards:
                raw, produced = zip_entries(shard["raw"]), zip_entries(shard["produced"])
                check(shard["operation"] + ":shard_resources", raw.keys() == produced.keys() and all(produced[n] == data for n, data in raw.items() if not n.endswith(".class")))
            for r in selected:
                label = role + ":" + r["rawArtifact"]["id"]
                checks[label + ":exact_strip"] = exact_strip(r["raw"], r["clean"], notices)
                checks[label + ":identity"] = r["rawArtifact"]["component"] == r["cleanArtifact"]["component"] and r["rawArtifact"]["variant"] == r["cleanArtifact"]["variant"] and {k:v for k,v in r["rawArtifact"]["attributes"].items() if k != "earth.terrarium.cloche.mixinPreapplication"} == {k:v for k,v in r["cleanArtifact"]["attributes"].items() if k != "earth.terrarium.cloche.mixinPreapplication"} and r["rawArtifact"]["attributes"]["earth.terrarium.cloche.mixinPreapplication"] == "raw" and r["cleanArtifact"]["attributes"]["earth.terrarium.cloche.mixinPreapplication"] != "raw"
                checks[label + ":clean_compile_raw_not_compile"] = r["clean"] in compile_files and r["raw"] not in compile_files
                records.append({"role":role, **r, "rawDigest":digest(r["raw"]), "cleanDigest":digest(r["clean"])})
            clean_paths = {r["clean"] for r in selected}
            check("non_S_original", all(r["file"] in compile_files for r in c["view"] if r["file"] not in clean_paths))
        else:
            check("off_games_original", c["games"] == [c["runtimeGame"]] + c["runtimeExtras"])
    if (root / "sync-observation.json").exists():
        for role in rows:
            checks[role + ":multi_artifact_inheritance"] = len([r for r in rows[role]["C"] if r["component"] == "ordinary:ref:1"]) == (2 if role in ("main", "test") else 1)
        for project in (":populated", ":empty"):
            checks[project + ":native_project_and_closure"] = any(n.get("getProjectPath") == project and n.get("getConfigurationName") == "apiElements" for n in objects(root_roles["main"]))
        checks["empty_NO_SOURCE_not_created"] = not (root / "empty/build/classes/java/main").exists()
        checks["test_native_miss"] = all(r["artifact"] is None for r in model["lookups"] if r["sourceSet"] == "test")
        checks["data_native_miss"] = any(r["sourceSet"] == "data" and r["artifact"] is None for r in model["lookups"])
    else:
        common = load(root,"full-model-observation.json")["common"]
        checks["six_common_groups"] = len(common) == 6
        for c in common:
            nodes = list(objects(root_roles[c["sourceSet"]]))
            files = {f for n in nodes if "getScope" in n and "compile" in PURPOSES[n["getScope"]] for f in dep_files(n)}
            checks[c["sourceSet"] + ":stub_in_native_model"] = set(c["files"]).issubset(files) and all(Path(p).is_file() for p in c["files"])
            checks[c["sourceSet"] + ":stub_in_SourceSet"] = set(c["files"]).issubset(c["sourceSetFiles"]) and c["task"] in c["sourceSetDeps"]
            games = [zip_entries(p)["named/Game.class"] for p in c["files"] if "named/Game.class" in zip_entries(p)]
            checks[c["sourceSet"] + ":stub_common_injected_API"] = bool(games) and all((b"probe/Ref" in data) == rows["right"]["enabled"] for data in games)


def native_configurations(root, model, replacements) -> Any:
    result = copy.deepcopy(model["configurations"])
    state = "earth.terrarium.cloche.mixinPreapplication"
    pairs = [r for c in compilation_rows(root).values() for r in c["cleanSources"]]
    for config in result.values():
        for a in config["artifacts"] or []:
            if state in a["attributes"]:
                assert a["attributes"][state] in ("raw", config["attributes"].get(state))
                a["attributes"][state] = "raw"
            matching = [r for r in pairs if r["clean"] == a["file"] and r["rawArtifact"]["component"] == a["component"] and r["rawArtifact"]["variant"] == a["variant"] and r["rawArtifact"]["attributes"] == a["attributes"]]
            if matching:
                assert len({key(r["rawArtifact"]) for r in matching}) == 1
                a["id"] = matching[0]["rawArtifact"]["id"]
        config["attributes"].pop(state, None)
    return unordered(normalize(result, root, replacements))


def main():
    roots = [Path(arg).resolve() for arg in sys.argv[1:4]]
    out = Path(sys.argv[4]).resolve()
    out.mkdir(parents=True, exist_ok=True)
    extras = [Path(arg).resolve() for arg in sys.argv[5:]]
    models = [load(root,"model.json") for root in roots]
    checks, records = {}, []
    normalized = []
    substitutions = []
    for i,(root,m) in enumerate(zip(roots,models)):
        replacements = []
        if i == 2:
            for role,c in compilation_rows(root).items():
                replacements += [(r["clean"],r["raw"]) for r in c["cleanSources"]]
                replacements += list(zip(c["games"], [shard["raw"] for shard in reversed(c["shards"])]))
        substitutions.append(replacements)
        normalized.append(normalize(expanded(m["models"]),root,replacements))
    for i,name in [(1,"off"),(2,"on")]:
        raw_diff = differences(normalized[0],normalized[i])
        (out / ("full-getter-"+name+"-differences.json")).write_text(json.dumps(raw_diff,indent=2),encoding="utf-8")
        baseline = copy.deepcopy(normalized[0])
        if i == 2:
            for role, c in compilation_rows(roots[i]).items():
                off = compilation_rows(roots[1])[role]
                replace_compile_games(baseline[":"]["dependencies"]["getDependencies"][role],
                    [normalize(p, roots[1]) for p in off["games"]],
                    [normalize(shard["raw"], roots[i]) for shard in reversed(c["shards"])])
        semantic_diff = differences(semantic_model(baseline), semantic_model(normalized[i]))
        (out / ("purpose-"+name+"-differences.json")).write_text(json.dumps(semantic_diff,indent=2),encoding="utf-8")
        checks[name+":complete_model_identity_closure_purposes"] = not semantic_diff
        for section in ("index","lookups","sourceSetIdentities"):
            checks[name+":"+section] = unordered(normalize(models[0][section],roots[0])) == unordered(normalize(models[i][section],roots[i]))
        original_configs = native_configurations(roots[0], models[0], [])
        candidate_configs = native_configurations(roots[i], models[i], substitutions[i])
        for conf in original_configs:
            for field in original_configs[conf]:
                checks[name+":"+conf+":"+field] = original_configs[conf][field] == candidate_configs[conf][field]
    common_roots = [r for r in extras if (r / "full-model-observation.json").is_file()]
    if common_roots:
        assert len(common_roots) == 2
        common_models, common_semantics, common_configurations = [], [], []
        for root in common_roots:
            m = load(root, "model.json")
            replacements = []
            for c in compilation_rows(root).values():
                if c["enabled"]:
                    replacements += [(r["clean"], r["raw"]) for r in c["cleanSources"]]
                    replacements += list(zip(c["games"], [shard["raw"] for shard in reversed(c["shards"])]))
            complete = normalize(expanded(m["models"]), root, replacements)
            common_models.append(complete)
            common_configurations.append(native_configurations(root, m, replacements))
            common_semantics.append(semantic_model(complete))
        for index, root in enumerate(common_roots):
            other_root = common_roots[1-index]
            for role, c in compilation_rows(root).items():
                other = compilation_rows(other_root)[role]
                if not c["enabled"] and other["enabled"]:
                    replace_compile_games(common_models[index][":"]["dependencies"]["getDependencies"][role],
                        [normalize(p, root) for p in c["games"]],
                        [normalize(shard["raw"], other_root) for shard in reversed(other["shards"])])
        common_semantics = [semantic_model(model) for model in common_models]
        full_diff = differences(common_models[0], common_models[1])
        semantic_diff = differences(common_semantics[0], common_semantics[1])
        (out / "full-common-getter-differences.json").write_text(json.dumps(full_diff, indent=2), encoding="utf-8")
        (out / "full-common-purpose-differences.json").write_text(json.dumps(semantic_diff, indent=2), encoding="utf-8")
        checks["full_common:all_native_identity_closure_and_purposes"] = not semantic_diff
        checks["full_common:complete_configuration_artifacts_variants_classifiers_edges_producers"] = common_configurations[0] == common_configurations[1]
        for section in ("index", "lookups", "sourceSetIdentities"):
            checks["full_common:" + section] = unordered(normalize(load(common_roots[0], "model.json")[section], common_roots[0])) == unordered(normalize(load(common_roots[1], "model.json")[section], common_roots[1]))
    for root,m in zip(roots+extras,models+[load(r,"model.json") for r in extras]):
        local={}
        verify_fixture(root,m,local,records)
        checks.update({root.name+":"+k:v for k,v in local.items()})
        (out / (root.name+"-expanded.json")).write_text(json.dumps(expanded(m["models"]),indent=2),encoding="utf-8")
    (out / "clean-model-members.json").write_text(json.dumps(records,indent=2),encoding="utf-8")
    report={"roots":list(map(str,roots+extras)),"checks":checks,"failed":[k for k,v in checks.items() if not v],
        "interpretation":"Every native getter retained in raw/expanded evidence. Semantic identity uses all non-order/scope/edge getters; exact file-collection members attributed individually with path-derived group labels; Every root/closure edge and compile/runtime purpose retained. Every effective CLIENT shard retains its own input identity and exact sequential cleanup evidence; only baseline compile file membership is replaced with the proven shard origins. Exact clean->raw substitution is only for comparison; physical compile isolation and exact notification cleanup are separate assertions."}
    (out/"checks.json").write_text(json.dumps(report,indent=2),encoding="utf-8")
    print(json.dumps({"checks":len(checks),"failed":report["failed"]},indent=2))
    return bool(report["failed"])


if __name__ == "__main__":
    sys.exit(main())
