// Focused native-publisher regression check. Run serially; uses the existing Gradle home.
// deno test --allow-read=build/tmp,../.mp1-maven-tests --allow-write=build/tmp,../.mp1-maven-tests --allow-run=cmd.exe .github/scripts/test_publish_maven.ts
// On Linux, grant --allow-run=bash instead.
const root = new URL("../../", import.meta.url);
const path = (url: URL) => decodeURIComponent(url.pathname).replace(/^\/([A-Za-z]:)/, "$1");
const rootPath = path(root);
const stagingParent = new URL("../.mp1-maven-tests/", root);
const init = path(new URL(".github/scripts/publish-maven.init.gradle", root));
const decoder = new TextDecoder();
function assert(value: unknown, message: string): asserts value {
  if (!value) throw new Error(message);
}
async function write(base: URL, file: string, content: string) {
  const target = new URL(file, base);
  await Deno.mkdir(new URL("./", target), { recursive: true });
  await Deno.writeTextFile(target, content);
}
async function files(directory: URL, prefix = ""): Promise<Map<string, Uint8Array>> {
  const result = new Map<string, Uint8Array>();
  for await (const entry of Deno.readDir(new URL(prefix, directory))) {
    const name = prefix + entry.name;
    if (entry.isDirectory) {
      for (const [key, value] of await files(directory, name + "/")) result.set(key, value);
    } else {
      assert(entry.isFile, `not a regular file: ${name}`);
      result.set(name, await Deno.readFile(new URL(name, directory)));
    }
  }
  return result;
}
function same(a: Map<string, Uint8Array>, b: Map<string, Uint8Array>) {
  return a.size === b.size && [...a].every(([key, bytes]) => {
    const other = b.get(key);
    return other && bytes.length === other.length && bytes.every((v, i) => v === other[i]);
  });
}
async function restore(directory: URL, payload: Map<string, Uint8Array>) {
  await Deno.remove(directory, { recursive: true });
  await Deno.mkdir(directory, { recursive: true });
  for (const [name, bytes] of payload) {
    const target = new URL(name, directory);
    await Deno.mkdir(new URL("./", target), { recursive: true });
    await Deno.writeFile(target, bytes);
  }
}

async function fixture(label: string) {
  const scratch = new URL(`build/tmp/mp1-policy/${label}/`, root);
  await Deno.mkdir(scratch, { recursive: true });
  await Deno.mkdir(stagingParent, { recursive: true });
  const stagePath = await Deno.makeTempDir({ dir: path(stagingParent), prefix: ".mp1-policy-" });
  const stage = new URL((Deno.build.os === "windows" ? "file:///" : "file://") + stagePath.replaceAll("\\", "/") + "/");
  const baseline = new URL("B/", stage);
  const candidate = new URL("C/", stage);
  await Deno.mkdir(baseline);
  await Deno.mkdir(candidate);
  await write(scratch, "stage.txt", stagePath);
  await write(scratch, "settings.gradle", "rootProject.name = 'native-fixture'\ninclude 'models'\n");
  await write(scratch, "build.gradle", `
plugins { id 'java-gradle-plugin'; id 'maven-publish' }
group = 'fixture.cloche'
version = providers.gradleProperty('fixtureVersion').getOrElse('1.0')
java { withSourcesJar(); withJavadocJar() }
gradlePlugin { plugins { cloche { id = 'fixture.cloche'; implementationClass = 'fixture.FixturePlugin' } } }
dependencies { implementation project(':models') }
`);
  await write(scratch, "models/build.gradle", `
plugins { id 'java-library'; id 'maven-publish' }
group = rootProject.group
version = providers.gradleProperty('modelsVersion').getOrElse(rootProject.version.toString())
publishing { publications { maven(MavenPublication) { from components.java } } }
`);
  await write(scratch, "src/main/java/fixture/FixturePlugin.java", `package fixture;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
public class FixturePlugin implements Plugin<Project> {
    public void apply(Project project) { project.getExtensions().getExtraProperties().set("fixtureModel", Model.class.getName()); }
}
`);
  await write(scratch, "models/src/main/java/fixture/Model.java", "package fixture; public class Model {}\n");
  let commandIndex = 0;
  async function run(label: string, properties: string[] = [], injection = "", expectedError = "") {
    const before = await files(baseline);
    await restore(candidate, before);
    const injectionUrl = new URL("injection.gradle", scratch);
    await write(scratch, "injection.gradle", injection);
    const command = Deno.build.os === "windows" ? "cmd.exe" : "bash";
    const wrapper = Deno.build.os === "windows" ? ["/d", "/c", path(new URL("gradlew.bat", root))] : [path(new URL("gradlew", root))];
    const args = [...wrapper, "--no-daemon", "--no-configuration-cache", "--no-configure-on-demand", "--no-parallel", "--console=plain",
      "-p", path(scratch), "--init-script", init, "--init-script", path(injectionUrl),
      `-PciMavenBaseline=${path(baseline)}`, `-PciMavenOutput=${path(candidate)}`, ...properties, "ciPublishMaven"];
    const result = await new Deno.Command(command, { cwd: rootPath, args, stdout: "piped", stderr: "piped" }).output();
    const output = decoder.decode(result.stdout) + decoder.decode(result.stderr);
    const log = `${String(++commandIndex).padStart(2, "0")}-${label}.log`;
    await write(scratch, log, `${command} ${args.join(" ")}\nexit=${result.code}\n${output}`);
    console.log(`${label}: exit=${result.code} (${log})`);
    assert(same(before, await files(baseline)), `${label}: immutable baseline changed`);
    assert(expectedError ? result.code !== 0 && output.includes(expectedError) : result.code === 0, `${label}: unexpected result; see ${log}`);
    return output;
  }
  async function accept() { await restore(baseline, await files(candidate)); }
  await run("initial");
  await accept();
  const release = await files(baseline);
  return { run, accept, baseline, candidate, release, scratch, stagePath };
}

Deno.test("native release equality, batch conflicts and current generation", async () => {
  const { run, baseline, candidate, release } = await fixture("releases");
  const equal = await run("captured-equal");
  assert(equal.includes("all releases skipped; candidate unchanged"), "equal payload was not skipped");
  assert(same(release, await files(candidate)), "all-skipped tree changed");

  const implementation = "fixture/cloche/native-fixture/1.0/";
  const marker = "fixture/cloche/fixture.cloche.gradle.plugin/1.0/";
  const models = "fixture/cloche/models/1.0/";
  const mutations: [string, string, "add" | "remove" | "change"][] = [
    ["implementation-pom", implementation + "native-fixture-1.0.pom", "change"],
    ["implementation-classifier", implementation + "native-fixture-1.0-sources.jar", "remove"],
    ["implementation-extra", implementation + "maven-metadata.xml", "add"],
    ["marker-pom", marker + "fixture.cloche.gradle.plugin-1.0.pom", "remove"],
    ["marker-checksum", marker + "fixture.cloche.gradle.plugin-1.0.pom.sha256", "change"],
    ["marker-extra", marker + "extra", "add"],
    ["models-module", models + "models-1.0.module", "change"],
    ["models-jar", models + "models-1.0.jar", "remove"],
    ["models-extra", models + "extra", "add"],
  ];
  for (const [label, name, action] of mutations) {
    await restore(baseline, release);
    if (action === "remove") await Deno.remove(new URL(name, baseline));
    else await Deno.writeTextFile(new URL(name, baseline), "different baseline bytes");
    await run(label, [], "", "version bump required");
  }
  await restore(baseline, release);
  await run("mixed-new-equal", ["-PfixtureVersion=2.0", "-PmodelsVersion=1.0"]);
  const retainedPayload = new Map([...release].filter(([name]) => !/\/maven-metadata\.xml(\.|$)/.test(name)));
  assert(same(retainedPayload, new Map([...await files(candidate)].filter(([name]) => retainedPayload.has(name)))), "old release payload changed");
  await Deno.writeTextFile(new URL(models + "models-1.0.pom", baseline), "conflicting models");
  await run("mixed-new-conflict", ["-PfixtureVersion=2.0", "-PmodelsVersion=1.0"], "", "version bump required");
  await restore(baseline, release);

  for (const [label, task, failure] of [
    ["suppressed-pom", "generatePomFileForPluginMavenPublication", "current POM generation missing"],
    ["suppressed-module", "generateMetadataFileForPluginMavenPublication", "current module generation missing"],
    ["suppressed-destination", "publishPluginMavenPublicationToCiMavenRepository", "destination task did not publish current output"],
  ]) {
    await run(label, [], `gradle.projectsEvaluated { rootProject.tasks.named('${task}') { onlyIf { false } } }`, failure);
  }
  await run("missing-artifact", [], `gradle.projectsEvaluated {
    rootProject.tasks.named('sourcesJar') { outputs.upToDateWhen { false }; doLast { archiveFile.get().asFile.delete() } }
}`, "FileNotFoundException");
});

Deno.test("native snapshot and repository history", async () => {
  const { run, accept, baseline, candidate, scratch, stagePath } = await fixture("history");
  await write(baseline, "unrelated/keep.txt", "retained unrelated repository file\n");
  await run("snapshot-first", ["-PfixtureVersion=3.0-SNAPSHOT"]);
  await accept();
  const history = await files(baseline);
  await run("snapshot-again", ["-PfixtureVersion=3.0-SNAPSHOT"]);
  const next = await files(candidate);
  assert([...history].filter(([name]) => !/\/maven-metadata\.xml(\.|$)/.test(name)).every(([name, bytes]) => same(new Map([[name, bytes]]), new Map([[name, next.get(name)!]]))), "snapshot history changed");
  assert(next.size > history.size, "native snapshot did not add timestamped payload");
  await accept();
  await run("snapshot-suppressed", ["-PfixtureVersion=3.0-SNAPSHOT"], `gradle.projectsEvaluated {
    rootProject.tasks.named('publishPluginMavenPublicationToCiMavenRepository') { onlyIf { false } }
}`, "destination task did not publish current output");
  await run("new-release-with-history", ["-PfixtureVersion=4.0"]);
  console.log(`Native policy evidence: ${path(scratch)}; B/C: ${stagePath}`);
});
