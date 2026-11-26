"""Focused regression checks for native scope/closure interpretation, no Gradle needed."""
import copy
import importlib.util
from pathlib import Path

spec = importlib.util.spec_from_file_location("verify", Path(__file__).with_name("verify-sync-models.py"))
assert spec and spec.loader
verify = importlib.util.module_from_spec(spec)
spec.loader.exec_module(verify)


def library(scope, file="/exact/compile.jar", classifier=None, dependencies=()):
    return {"$type":"Library", "getFile":file, "getClassifier":classifier,
            "getScope":scope, "getClasspathOrder":2, "getDependencies":list(dependencies)}


base = [library("COMPILE")]
split = [library("PROVIDED"), library("RUNTIME")]
assert verify.dependency_graph(base) == verify.dependency_graph(split)
assert verify.dependency_graph(base) != verify.dependency_graph([library("PROVIDED")])
assert verify.dependency_graph(base) != verify.dependency_graph([library("COMPILE", classifier="extra")])
assert verify.dependency_graph(base) != verify.dependency_graph([library("COMPILE", file="/other/compile.jar")])
closure = [library("COMPILE", dependencies=[library("COMPILE", file="/transitive.jar")])]
assert verify.dependency_graph(closure) != verify.dependency_graph(base + [library("COMPILE", file="/transitive.jar")])
assert verify.dependency_graph(closure) == verify.dependency_graph([
    library("PROVIDED", dependencies=[library("PROVIDED",file="/transitive.jar")]),
    library("RUNTIME", dependencies=[library("RUNTIME",file="/transitive.jar")])])
# A file-collection group may split, but every exact member and its uses must survive.
group = {"$type":"Files", "getFiles":["/a","/b"], "getScope":"COMPILE", "getClasspathOrder":1,
         "getName":"[/a, /b]", "getId":{"getName":"[/a, /b]", "getPresentableName":"[/a, /b]"}, "getDependencies":[]}
a = copy.deepcopy(group); a["getFiles"] = ["/a"]
b = copy.deepcopy(group); b["getFiles"] = ["/b"]
assert verify.dependency_graph([group]) == verify.dependency_graph([a,b])
b["getScope"] = "RUNTIME"
assert verify.dependency_graph([group]) != verify.dependency_graph([a,b])
empty = copy.deepcopy(group); empty["getFiles"] = []
assert verify.dependency_graph([empty]) != verify.dependency_graph([])
joined = copy.deepcopy(group); joined["getFiles"] = ["/joined"]; joined["getScope"] = "PROVIDED"
a["getScope"] = b["getScope"] = "RUNTIME"
assert verify.dependency_graph([group]) == verify.dependency_graph([joined,a,b], {"/joined":["/a","/b"]})
assert verify.dependency_graph([group]) != verify.dependency_graph([joined,a,b])
print("Verified split scope, exact paths/classifier, closure edges, empty nodes, file grouping and proven game composition; rejected purpose loss/expansion.")
