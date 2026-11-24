"""Small counterexamples for the CT1 model/strip comparison, runnable without Gradle."""
import copy
import importlib.util
import json
from pathlib import Path
import tempfile
import zipfile

spec = importlib.util.spec_from_file_location("models", Path(__file__).with_name("verify-sync-models.py"))
assert spec is not None and spec.loader is not None
models = importlib.util.module_from_spec(spec)
spec.loader.exec_module(models)

with tempfile.TemporaryDirectory() as directory:
    root = Path(directory)
    def jar(name, members, resource=b"original"):
        path = root / name
        with zipfile.ZipFile(path, "w") as z:
            z.writestr("mixins.json", json.dumps({"mixins": members, "client": ["Keep"], "other": 7}))
            z.writestr("duplicate.txt", resource)
        return path
    raw = jar("raw.jar", ["Common", "Client", "Keep"])
    clean = jar("clean.jar", ["Keep"])
    notices = [{"mixins.json": ["Common"]}, {"mixins.json": ["Client", "Common"]}]
    assert models.exact_strip(raw, clean, notices)
    assert not models.exact_strip(raw, clean, notices[:1])
    assert not models.exact_strip(raw, jar("missed.jar", ["Client", "Keep"]), notices)
    assert not models.exact_strip(raw, jar("resource.jar", ["Keep"], b"changed"), notices)
    assert not models.exact_strip(raw, jar("overdelete.jar", []), notices)

node = {"getFiles": ["raw-common", "ordinary"], "getScope": "COMPILE", "getName": "old",
        "getId": {"getName": "old", "getPresentableName": "old"}, "getDependencies": []}
roots = [copy.deepcopy(node)]
models.replace_compile_games(roots, ["raw-common"], ["clean-client", "clean-common"])
graph = models.dependency_graph(roots)
uses = {n["identity"]["getFiles"][0]: n["uses"] for n in graph["nodes"]}
assert uses == {"raw-common": ["runtime"], "ordinary": ["compile", "runtime"],
                "clean-client": ["compile"], "clean-common": ["compile"]}
bad = copy.deepcopy(roots)
bad[0]["getFiles"].remove("clean-client")
assert models.dependency_graph(bad) != graph
bad = copy.deepcopy(roots)
bad[0]["getFiles"].append("raw-common")
assert models.dependency_graph(bad) != graph
bad = copy.deepcopy(roots)
bad[0]["getScope"] = "RUNTIME"
assert models.dependency_graph(bad) != graph
bad = copy.deepcopy(roots)
bad[0]["getId"]["classifier"] = "extra"
assert models.dependency_graph(bad) != graph
bad = copy.deepcopy(roots)
bad[0]["getDependencies"].append(copy.deepcopy(node))
assert models.dependency_graph(bad) != graph
print("VERIFIED_CT1_COMPARISON_COUNTEREXAMPLES")
