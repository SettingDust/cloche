# Maven publication CI

Status: Ready for implementation planning under the accepted rules below. This document specifies behavior; it does not establish that a workflow exists or that publishing has succeeded.

## Authority and boundary

The [accepted Git-backed Maven publishing ADR](../adr/git-backed-maven-publishing.md) owns the repository, trigger, permission, and tagging decisions. No PRD change is required for this infrastructure change. The preparation baseline is the current local source, without remote synchronization.

This Spec owns Cloche's Maven publication CI only. It does not own JVM or Codev publishing, IntelliJ Marketplace publishing, the central `SettingDust/maven` repository, or existing feature documentation. It introduces no shared publisher, publication manifest, receipt system, source tagging, Plan, or Waves graph.

## Publication scope and source evidence

CI must cover every `MavenPublication` configured in the selected Cloche build, including publications created by applied plugins after project evaluation, and apply the publication, skip, and conflict rules below to each. The configured Gradle publication model is the source of truth; a fixed publication count, JAR glob, namespace copy, or list of only explicitly declared publications must not define completeness.

The inspected local configuration implies the following baseline. These are source-derived observations, not an executed Gradle inventory. `V` denotes the publication's evaluated version; the current root `gradle.properties` supplies `group=earth.terrarium` and `version=0.19.13`.

| Project | Publication | Expected coordinates | Basis and content |
| --- | --- | --- | --- |
| `:` | `pluginMaven` | `earth.terrarium:cloche:V` | Root `java-gradle-plugin` plus `maven-publish` create the Java component publication. Root `java` configuration adds sources and Javadoc JARs. |
| `:` | `clochePluginMarkerMaven` | `earth.terrarium.cloche:earth.terrarium.cloche.gradle.plugin:V` | Root plugin declaration named `cloche`, with ID `$group.cloche`, creates the marker POM referring to the implementation publication. A marker need not have a JAR. |
| `:cloche-models` | `maven` | `earth.terrarium:cloche-models:V` | `models/build.gradle.kts` explicitly publishes `components["java"]`; `settings.gradle.kts` renames the project from `models` to `cloche-models`. |

The root implementation depends on `projects.clocheModels`, so omitting models leaves the published plugin's project dependency unavailable from this repository. CI must preserve Gradle's generated dependency coordinates and marker-to-implementation relationship.

`settings.gradle.kts` also includes `:cloche-idea`, sourced from `idea/`. Its build configures IntelliJ `publishPlugin`, but declares neither `maven-publish` nor a Maven publication; Marketplace tasks are outside this scope. `integration-test/` and the source test fixtures are separate builds, not included publishing projects in the root settings. If the configured Maven model changes, CI must follow the actual model rather than assume that this table is exhaustive forever. An unexpected empty model or incomplete output must fail before branch mutation.

Evidence:

- [Root build](../../build.gradle.kts): plugin declaration, Java attachments, models dependency, and existing publishing repositories.
- [Settings](../../settings.gradle.kts), [project properties](../../gradle.properties), [models build](../../models/build.gradle.kts), and [IDE build](../../idea/build.gradle.kts).
- [Wrapper configuration](../../gradle/wrapper/gradle-wrapper.properties): Gradle 9.8.2.
- Gradle 9.8.2 [plugin development publishing integration](https://docs.gradle.org/9.8.2/userguide/java_gradle_plugin.html#maven_publish_plugin): `pluginMaven` and per-plugin marker publications are configured after evaluation.
- Gradle 9.8.2 [Maven publishing](https://docs.gradle.org/9.8.2/userguide/publishing_maven.html) and [publication setup](https://docs.gradle.org/9.8.2/userguide/publishing_setup.html): publication identities, repository-specific tasks, file repositories, and generated metadata.

## Entry points and permissions

- Automatic publication must run only on a push to `main` changing root `gradle.properties`. A matching path is sufficient; nested fixture properties are not release inputs.
- A `workflow_dispatch` entry point must exist and permit publication only from the same repository's `main` branch. A dispatch selecting any other branch or a tag must be rejected before publication.
- Pull requests, pushes to other branches, and tag events must not automatically publish. A push to `maven` must not trigger another publication run.
- Both entry points must use the source commit selected by their permitted event. A run must derive coordinates and versions from that build, without independently rewriting versions or switching to a newer source revision. Immediately before updating `maven`, it must query the current remote `main` HEAD and compare it with the selected source commit. A mismatch must skip publication without a branch update; inability to establish the current HEAD must fail without an update. The current HEAD may be rerun, subject to the version-content rules below.
- This freshness check does not impose increasing version numbers: the current `main` commit can still declare an older version. It also does not lock `main` against a subsequent push; it establishes equality at the pre-update check.
- The only remote write destination must be the `maven` branch of `github.repository`, using the same repository's `GITHUB_TOKEN` with `contents: write`. Missing write permission or branch-policy rejection must fail the run, without switching to another repository or credential.
- CI must not create, delete, or move source tags, or modify source branches. Legacy `maven-repo` deletion and the retained central repository require no actions here.

## Local publication and repository output

CI must use Gradle's existing `maven-publish` machinery and a dedicated file-backed Maven publishing repository. Its output directory must be separate from the source tree's tracked files and from the user's or runner's `mavenLocal` cache. Before publication, CI must initialize that directory from the existing `maven` branch's Maven tree, or as an empty Maven tree when the branch does not exist. It must preserve the baseline needed for comparison and use Gradle to publish into the seeded directory, so native metadata updates see retained history. Unrelated cache entries must never enter the branch. Retained files alone must not be treated as evidence that a current publication was successfully generated.

CI must select repository-specific publication tasks for that destination across all participating projects, including plugin-created markers. It must not invoke the unqualified aggregate `publish`, which includes existing HTTP destinations, or copy `~/.m2/repository`. The existing HTTP publishing configuration must remain available to its existing callers, but the new CI path must not invoke HTTP upload tasks or require `mavenUsername` / `mavenPassword` secrets. Dependency downloads over HTTP(S) are unaffected.

Gradle must generate the Maven paths, POMs, configured artifacts, applicable Gradle Module Metadata, repository metadata (`maven-metadata.xml`), and checksums. CI must retain generated files and their relationships, including a POM-only marker; it must not synthesize POMs, invent classifier artifacts, or require every publication to have the same file types. In particular, the root's sources/Javadoc configuration does not impose those classifiers on models or markers. A dedicated file repository is required because `publishToMavenLocal` does not produce the same checksum output as a regular Maven publishing repository.

The resulting repository must expose Maven group paths directly at the `maven` branch root, for example `earth/terrarium/cloche/V/` and `earth/terrarium/cloche/earth.terrarium.cloche.gradle.plugin/V/`. It must not add a `repository/`, project-name, or staging-directory wrapper. Existing versions and unrelated existing repository paths must survive publication. Gradle must update affected metadata and its checksums against the seeded history; CI must not overlay fresh-only repository metadata that drops retained versions or introduce a custom metadata generator.

## Version content and repeat publication

These rules apply to every evaluated publication, including implementation, marker, and models, before any remote update. A version ending in `-SNAPSHOT` follows the snapshot rule; other versions follow the release rule.

For a release, the complete version payload is the exact set of relative file paths and bytes under its Maven `group/artifact/version/` directory. It includes the POM, every configured artifact and classifier, applicable Gradle Module Metadata, checksums, and any other files inside that directory. Completeness must be established against the evaluated publication and files actually generated for the current build, independently of retained files. A POM-only marker is complete when its own generated file set is complete; it does not require a JAR.

- If the release version path is absent, CI must add the complete generated payload, subject to the whole-run checks before a remote update.
- If a complete existing release has exactly the same paths and bytes as the complete current payload, CI must skip that release. If every publication is skipped, the run must leave `maven` unchanged, including metadata.
- If an existing release has any added, missing, or differing payload file relative to the complete current payload, CI must fail the whole run without a remote update and require a version bump. An incomplete existing release is a conflict, not permission to fill or overwrite it. A conflict in models or a marker must also prevent publishing the rest of the set.

No release payload file may be silently excluded or normalized to make the comparison pass. In particular, changed archive timestamps, POM bytes, module metadata, or checksums remain differences even if an implementer considers them incidental. Being the current `main` HEAD permits a rerun; it does not waive this equality check or guarantee that a rebuild produces identical bytes.

Repository metadata outside a release's version directory, such as `group/artifact/maven-metadata.xml` and its checksum files, is mutable indexing data and is outside that release payload comparison. It may change through Gradle's native publishing when adding versions or updating snapshots, while continuing to describe retained history. Calling a file metadata does not exempt it if it is inside a release version directory. CI must not persist metadata changes solely to record an otherwise skipped release.

For `-SNAPSHOT` versions, CI must allow Gradle's native snapshot publication to add the current snapshot and update snapshot metadata and checksums. Snapshot version metadata is mutable; the release equality rule does not apply to it. Historical timestamped snapshot artifacts, their checksums, and other retained versions must remain available. CI must not add snapshot pruning, cleanup, or a retention limit. A current-source snapshot rerun may therefore produce another native snapshot update rather than a no-op.

The implementation plan owns the task wiring and comparison mechanics needed to demonstrate these contracts; no shared publisher, manifest, receipt, or custom metadata framework is required or authorized.

## Branch update and failure behavior

When no `maven` branch exists, the first successful run must create it with the Maven repository at its root. When it exists, a run must prepare a complete candidate from the current repository state and the completed local publications before making a remote update. Source files, build caches, credentials, and temporary staging files must not be added to the branch.

Generation, missing-publication, metadata validation, or permission failures must prevent a remote update and fail the run. A failed or cancelled run must not intentionally push a partial set of publications. Consumers must receive either the previous complete Git tree or the new complete Git tree through one branch update. An ambiguous push result must be reported as unconfirmed until checked; it must not be reported as confirmed failure or success without evidence.

Concurrent runs must not lose each other's versions or metadata. Updates must not force-push over a changed branch tip. Serializing writers or failing a stale update is sufficient; automatic retries are not required. If a run retries after an intervening `maven` update, it must rebuild and validate the candidate against that branch state, including the version-content checks, and repeat the source freshness check before updating. Cancellation or timeout before the update must leave the branch unchanged. Cancellation after a successful update cannot roll it back and must not lead to a claim that no update occurred.

## Decision closure and readiness

The accepted user confirmation closes the draft's three blockers: D1 uses exact complete-payload equality for releases and native retained-history snapshot updates; D2 restricts both entry points to `main` with a current-source check; D3 seeds the isolated output from `maven` (or an empty tree) before Gradle publication. No policy choice from the former options remains open. Task wiring and comparison implementation remain delivery details, subject to the verifiable contracts above. Documentation readiness does not establish implementation or live publication readiness.

## Acceptance conditions

The following are required future checks, not results of this documentation task. They need only a focused publication check and branch-update scenarios, not a loader or Minecraft-version matrix.

1. Inspect the evaluated publication model and the tasks selected for the dedicated file repository. Demonstrate coverage of every actual Maven publication, including root implementation, marker, and models, without invoking HTTP upload or Marketplace tasks.
2. Publish into temporary local storage and inspect the Maven tree: root implementation attachments, models artifacts, marker POM dependency, applicable module metadata, repository metadata, and generated checksums. Confirm coordinates match the selected build and no cache or source files leak into output. Resolve the marker to its implementation and models through that repository, using normal external repositories only for external dependencies.
3. Check that only a root `gradle.properties` push on `main` triggers automatic publication, not nested properties, unrelated paths, PRs, other branches, tags or `maven` pushes. Manual `main` is allowed; other refs are skipped. For either entry point, a stale SHA skips and a failed source lookup prevents an update. Reruns and older declared versions remain subject to the same freshness and version-content rules.
4. In a disposable local Git repository, exercise an absent `maven` branch and an existing tree with historical releases and snapshots. Publish through the seeded file repository; verify direct root layout, retained versions, historical snapshot files and checksums, native metadata entries and checksum validity, and no cleanup. Publish a snapshot again and verify updated native snapshot metadata with the earlier timestamped payload retained.
5. Exercise complete release equality as a skip and an all-skipped run as no branch update. For each of implementation, marker, and models, independently change, remove, or add a version-payload file and verify failure requiring a version bump without publishing any of the set. Include POM, artifact/classifier where configured, module metadata where applicable, checksum, and any in-version metadata differences. Demonstrate that seeded old files cannot mask missing current output. Adding a new version must permit Gradle's metadata and checksum changes outside existing release directories while preserving those releases byte for byte.
6. Verify one complete branch update, no source tag changes, a stale concurrent `maven` update failing without lost content, and generation failure/cancellation before update leaving the branch unchanged. Any retry after a competing update must revalidate the current branch baseline and source freshness; check reporting of an ambiguous push and cancellation after an update.
7. Check configured token scope and destination, and demonstrate that denied writes fail without a fallback. Actual same-repository GitHub publication remains unverified until an authorized implementation run proves it.

Documentation self-check is limited to source and authority alignment, publication coverage reasoning, links, internal consistency, and explicitly recorded gaps. No build, publication, Git mutation, or workflow execution is asserted by this Spec.
