import {
  type Generate,
  generateCandidate,
  type Git,
  gitCommand,
  publishMaven,
  type PublishOptions,
} from "./publish_maven.ts";

const text = (bytes: Uint8Array) => new TextDecoder().decode(bytes);
const bytes = (value: string) => new TextEncoder().encode(value);
function assert(condition: unknown, message: string): asserts condition {
  if (!condition) throw new Error(message);
}
async function command(directory: string, args: string[]) {
  const result = await gitCommand(directory, args);
  assert(result.code === 0, `fixture Git ${args[0]} failed: ${result.code}`);
  return text(result.stdout).trim();
}
const project = decodeURIComponent(new URL("../../", import.meta.url).pathname)
  .replace(/^\/([A-Za-z]:)/, "$1").replace(/\/$/, "");
const evidence = `${project}/build/tmp/i-workflow`;
// Synthetic payloads test Git exposure; Gradle policy has its own native fixtures.
const payload = {
  "local/fixture/lib/1.0/lib-1.0.pom": bytes("<project>fixture</project>\r\n"),
  "local/fixture/lib/1.0/lib-1.0.jar": new Uint8Array([0, 255, 13, 10, 128]),
};
const history = {
  "other/history/0.1/history-0.1.pom": bytes("historical payload\r\n"),
};
type Tree = Record<string, Uint8Array>;
async function writeTree(directory: string, tree: Tree) {
  for (const [path, value] of Object.entries(tree)) {
    const file = `${directory}/${path}`;
    await Deno.mkdir(file.slice(0, file.lastIndexOf("/")), { recursive: true });
    await Deno.writeFile(file, value);
  }
}
async function readTree(directory: string): Promise<Tree> {
  const tree: Tree = {};
  const visit = async (relative: string) => {
    for await (const entry of Deno.readDir(`${directory}/${relative}`)) {
      const path = relative ? `${relative}/${entry.name}` : entry.name;
      if (entry.isDirectory) await visit(path);
      else tree[path] = await Deno.readFile(`${directory}/${path}`);
    }
  };
  await visit("");
  return tree;
}
function same(first: unknown, second: unknown, message: string) {
  const normalize = (value: unknown) =>
    JSON.stringify(
      value,
      value && typeof value === "object"
        ? Object.keys(value).sort()
        : undefined,
    );
  assert(normalize(first) === normalize(second), message);
}

async function fixture(name: string, actualSource = false) {
  const directory = await Deno.makeTempDir({
    dir: evidence,
    prefix: `${name}-`,
  });
  const remote = `${directory}/remote.git`;
  const source = actualSource ? project : `${directory}/source`;
  await Deno.mkdir(remote);
  await command(remote, ["init", "--bare", "."]);
  if (!actualSource) {
    await Deno.mkdir(source);
    await command(source, ["init", "-b", "main"]);
    await command(source, ["config", "user.name", "fixture"]);
    await command(source, ["config", "user.email", "fixture@localhost"]);
    await Deno.writeTextFile(
      `${source}/source.txt`,
      "source must not be published",
    );
    await command(source, ["add", "."]);
    await command(source, [
      "-c",
      "commit.gpgsign=false",
      "commit",
      "-m",
      "source",
    ]);
    await command(source, ["push", remote, "main"]);
  } else {
    await command(remote, ["fetch", "--no-tags", source, "HEAD"]);
    await command(remote, ["update-ref", "refs/heads/main", "FETCH_HEAD"]);
  }
  const sha = await command(source, ["rev-parse", "HEAD"]);
  await command(remote, ["update-ref", "refs/tags/fixture-source", sha]);
  const options: PublishOptions = {
    sourceDirectory: source,
    sourceRef: "refs/heads/main",
    sourceSha: sha,
    remote,
  };
  const refs = () =>
    command(remote, ["for-each-ref", "--format=%(refname) %(objectname)"]);
  const state = async () => {
    const all = await refs();
    const head = all.split("\n").find((line) =>
      line.startsWith("refs/heads/maven ")
    )?.split(" ")[1];
    return {
      refs: all,
      source: all.split("\n").filter((line) =>
        !line.startsWith("refs/heads/maven ")
      ).join("\n"),
      head: head ?? null,
      tree: head
        ? await command(remote, ["rev-parse", `${head}^{tree}`])
        : null,
    };
  };
  const seed = async (tree: Tree) => {
    const work = await Deno.makeTempDir({
      dir: directory,
      prefix: "destination-",
    });
    await command(work, ["init", "-b", "maven"]);
    await command(work, ["config", "user.name", "fixture"]);
    await command(work, ["config", "user.email", "fixture@localhost"]);
    const before = await state();
    if (before.head) {
      await command(work, ["fetch", "--no-tags", remote, "refs/heads/maven"]);
      await command(work, ["reset", "--hard", "FETCH_HEAD"]);
    }
    await writeTree(work, tree);
    await command(work, ["add", "--all", "--force", "."]);
    await command(work, [
      "-c",
      "commit.gpgsign=false",
      "commit",
      "-m",
      "destination",
    ]);
    await command(work, ["push", remote, "HEAD:refs/heads/maven"]);
  };
  const verifyTree = async (expected: Tree) => {
    const names =
      (await command(remote, ["ls-tree", "-r", "--name-only", "maven"])).split(
        "\n",
      ).sort();
    same(
      names,
      Object.keys(expected).sort(),
      "destination contains exactly the complete candidate at root",
    );
    for (const [path, value] of Object.entries(expected)) {
      const result = await gitCommand(remote, ["show", `maven:${path}`]);
      assert(
        result.code === 0 && result.stdout.length === value.length &&
          value.every((byte, index) => result.stdout[index] === byte),
        `exact bytes preserved: ${path}`,
      );
    }
  };
  const generate: Generate = async (baseline, candidate) => {
    const retained = await readTree(baseline);
    const seeded = await readTree(candidate);
    assert(Object.keys(retained).length === Object.keys(seeded).length && Object.entries(retained).every(([path, data]) => seeded[path]?.length === data.length && data.every((b, i) => seeded[path][i] === b)), "candidate seeded byte-for-byte");
    await writeTree(candidate, payload);
  };
  return {
    directory,
    remote,
    source,
    sha,
    options,
    refs,
    state,
    seed,
    verifyTree,
    generate,
  };
}

Deno.test("Maven entry exposes one complete eligible candidate", async (t) => {
  await Deno.mkdir(evidence, { recursive: true });
  const records: unknown[] = [];
  for (
    const scenario of [
      "absent",
      "seeded",
      "identical-current-rerun",
      "stale-rerun",
      "non-main",
      "advance-during-generation",
      "main-lookup-failure",
      "destination-lookup-failure",
      "generation-failure",
      "empty",
      "incomplete",
      "release-conflict-with-new-version",
      "interrupted",
      "destination-race",
      "denied",
      "uncertain-confirmed",
      "uncertain-unconfirmed",
      "uncertain-readback-failed",
    ]
  ) {
    await t.step(scenario, async () => {
      const f = await fixture(scenario);
      if (
        [
          "seeded",
          "identical-current-rerun",
          "destination-race",
          "uncertain-unconfirmed",
          "release-conflict-with-new-version",
        ].includes(scenario)
      ) {
        await f.seed(
          scenario === "identical-current-rerun"
            ? payload
            : scenario === "release-conflict-with-new-version"
            ? { ...history, ...payload }
            : history,
        );
      }
      if (scenario === "stale-rerun") {
        await command(f.source, [
          "-c",
          "commit.gpgsign=false",
          "commit",
          "--allow-empty",
          "-m",
          "new main",
        ]);
        await command(f.source, ["push", f.remote, "main"]);
        // The event checkout stays on its original immutable SHA.
        await command(f.source, ["checkout", "--detach", f.sha]);
      }
      if (scenario === "non-main") f.options.sourceRef = "refs/heads/feature";
      if (scenario === "denied") {
        await Deno.writeTextFile(
          `${f.remote}/hooks/pre-receive`,
          "#!/bin/sh\nexit 1\n",
          { mode: 0o755 },
        );
      }
      const before = await f.state();
      let expectedBeforePush = before;
      let generated = false;
      let pushes = 0;
      const calls: string[][] = [];
      const git: Git = async (directory, args) => {
        calls.push(args);
        if (args[0] === "ls-remote") {
          const ref = args.at(-1);
          if (
            (scenario === "main-lookup-failure" && ref === "refs/heads/main") ||
            (scenario === "uncertain-readback-failed" && pushes === 1 && ref === "refs/heads/maven") ||
            (scenario === "destination-lookup-failure" &&
              ref === "refs/heads/maven")
          ) {
            return { code: 128, stdout: bytes("") };
          }
        }
        if (args[0] === "push") {
          pushes++;
          assert(
            !args.some((arg) =>
              arg.startsWith("+") || arg.startsWith("--force")
            ),
            "push must be non-force",
          );
          assert(
            args.at(-1)?.endsWith(":refs/heads/maven"),
            "only Maven ref may be pushed",
          );
          if (scenario === "uncertain-unconfirmed") {
            return { code: 128, stdout: bytes("") };
          }
          const result = await gitCommand(directory, args);
          if (["uncertain-confirmed", "uncertain-readback-failed"].includes(scenario)) {
            assert(
              result.code === 0,
              "uncertain fixture must actually update remote before losing response",
            );
            return { code: 128, stdout: bytes("") };
          }
          return result;
        }
        return gitCommand(directory, args);
      };
      const generate: Generate = async (baseline, candidate) => {
        generated = true;
        if (scenario === "empty") return;
        await f.generate(baseline, candidate);
        if (scenario === "generation-failure") throw new Error("Gradle failed");
        if (scenario === "incomplete") {
          throw new Error("Missing Maven output: configured artifact");
        }
        if (scenario === "release-conflict-with-new-version") {
          await writeTree(candidate, {
            "local/fixture/new/2.0/new-2.0.pom": bytes("otherwise new version"),
          });
          throw new Error(
            "Release payload conflict for local.fixture:lib:1.0; use a version bump",
          );
        }
        if (scenario === "interrupted") {
          const child = new Deno.Command("deno", {
            args: [
              "eval",
              "Deno.stdout.writeSync(new Uint8Array([1])); setInterval(() => {}, 1000)",
            ],
            stdout: "piped",
            stderr: "null",
          }).spawn();
          const reader = child.stdout.getReader();
          await reader.read(); // Child is running; terminate only this owned fixture process.
          await reader.cancel();
          child.kill("SIGTERM");
          const status = await child.status;
          assert(!status.success, "interrupted generator must fail");
          throw new Error(`generation interrupted (exit ${status.code})`);
        }
        if (scenario === "advance-during-generation") {
          await command(f.source, [
            "-c",
            "commit.gpgsign=false",
            "commit",
            "--allow-empty",
            "-m",
            "advance",
          ]);
          await command(f.source, ["push", f.remote, "main"]);
          expectedBeforePush = await f.state();
        }
        if (scenario === "destination-race") {
          await f.seed({
            "other/concurrent/2.0/concurrent-2.0.pom": bytes(
              "concurrent writer",
            ),
          });
          expectedBeforePush = await f.state();
        }
      };
      let outcome: string;
      try {
        outcome = await publishMaven(f.options, generate, git);
      } catch (error) {
        outcome = `failed: ${error instanceof Error ? error.message : error}`;
      }
      const after = await f.state();
      const succeeds = [
        "absent",
        "seeded",
        "uncertain-confirmed",
      ].includes(scenario);
      if (succeeds) {
        assert(outcome.startsWith("published:"), outcome);
        assert(
          after.head !== before.head,
          "successful publication advances Maven head",
        );
        const parents = await command(f.remote, [
          "show",
          "-s",
          "--format=%P",
          "maven",
        ]);
        assert(
          parents === (before.head ?? ""),
          "candidate commit has exactly the fetched parent or orphan history",
        );
        await f.verifyTree(
          scenario === "seeded" ? { ...history, ...payload } : payload,
        );
      } else if (scenario === "uncertain-readback-failed") {
        assert(outcome.includes("unconfirmed") && outcome.includes("lookup failed (exit 128)"), outcome);
        assert(pushes === 1 && after.head !== before.head, "update succeeded before both responses were lost");
        await f.verifyTree(payload);
      } else if (scenario === "identical-current-rerun") {
        assert(outcome.startsWith("no-change:"), outcome);
        await f.verifyTree(payload);
        same(
          after,
          before,
          "current-source rerun must avoid empty commit for identical tree",
        );
      } else {
        const skip = ["stale-rerun", "non-main", "advance-during-generation"]
          .includes(scenario);
        assert(outcome.startsWith(skip ? "skipped:" : "failed:"), outcome);
        same(
          after,
          expectedBeforePush,
          "failure/skip cannot expose any candidate tree",
        );
      }
      assert(
        after.source === expectedBeforePush.source,
        "publisher must preserve source branches and tags",
      );
      assert(pushes <= 1, "no retries");
      if (scenario === "non-main") {
        assert(
          !generated && calls.length === 0,
          "ineligible event has no Git/generation effects",
        );
      }
      if (
        [
          "generation-failure",
          "empty",
          "incomplete",
          "release-conflict-with-new-version",
          "interrupted",
        ].includes(scenario)
      ) {
        assert(pushes === 0, "failed/interrupted generation cannot reach push");
      }
      if (pushes) {
        const pushAt = calls.findIndex((args) => args[0] === "push");
        assert(
          calls[pushAt - 1]?.at(-1) === "refs/heads/main",
          "source lookup is immediately before push",
        );
      }
      records.push({
        scenario,
        fixture: f.directory,
        before,
        checkpoint: expectedBeforePush,
        after,
        outcome,
        pushes,
      });
      await Deno.writeTextFile(
        `${evidence}/git-cases.json`,
        JSON.stringify(records, null, 2),
      );
    });
  }

  if (Deno.env.get("MAVEN_REAL_GRADLE") === "1") {
    await t.step(
      "real complete root Gradle producer plus production Deno driver",
      async () => {
        const f = await fixture("real-root", true);
        const before = await f.state();
        const sourceRefs = await command(project, [
          "for-each-ref",
          "--format=%(refname) %(objectname)",
        ]);
        let expected: Tree = {};
        const outcome = await publishMaven(
          f.options,
          async (baseline, candidate) => {
            await generateCandidate(project, baseline, candidate);
            expected = await readTree(candidate);
          },
        );
        assert(outcome.startsWith("published:"), outcome);
        await f.verifyTree(expected);
        const after = await f.state();
        assert(
          sourceRefs ===
            await command(project, [
              "for-each-ref",
              "--format=%(refname) %(objectname)",
            ]),
          "source repository refs/tags remain unchanged",
        );
        assert(
          before.source === after.source,
          "real producer/driver preserve fixture main and tags",
        );
        records.push({
          scenario: "real-root",
          fixture: f.directory,
          before,
          after,
          outcome,
        });
        await Deno.writeTextFile(
          `${evidence}/git-cases.json`,
          JSON.stringify(records, null, 2),
        );
      },
    );
  }
});
