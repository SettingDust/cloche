# Maven publication CI implementation plan

This Plan derives from the [Maven publication CI Spec](../../spec/maven-publication-ci.md) and [accepted ADR](../../adr/git-backed-maven-publishing.md). Its entry is [Work](work.md), with the reused authority assessment and inspected source in [Context](context.md). Its exactly matching execution graph is [Waves](waves.md); only that document defines items, dependencies, and gates.

Status: the draft and original author self-check below are historical. Independent review adopted the Deno/TypeScript plan as ready, and subsequent user confirmation authorized local implementation and verification; see [Context](context.md). This consumer-method maintenance completes no implementation item or gate; its affected boundary still requires independent review.

## Outcome and exclusions

Deliver a Cloche-specific CI path that publishes every evaluated Maven publication through a dedicated Gradle file repository and updates only the same repository's `maven` branch, atomically at the Git-ref level. Preserve history and enforce the Spec's entry-point, complete-release equality, native snapshot, source-freshness, permission, and concurrency contracts.

Success conditions:

- **S1 — Complete native output:** all actual Maven publications, including markers and models, produce complete current output; plugin resolution reaches implementation and models from temporary Maven storage, with appropriate artifacts, POMs, module metadata, and checksums. No HTTP publishing or Marketplace task is selected.
- **S2 — Version and history correctness:** release equality compares all version-relative paths and raw bytes against an immutable baseline; incomplete/different releases fail the whole batch; equal releases are skipped without metadata-only updates. Snapshots retain historical payloads with Gradle-native metadata. Seeded leftovers cannot prove current generation.
- **S3 — Safe repository update:** absent/existing branch cases produce the Maven tree at branch root in one non-forced update; stale source skips, lookup/generation/conflict/permission failures prevent updates, competing writers lose no history, and ambiguous/post-update interruption reports do not assert an unproved outcome.
- **S4 — Correct CI entry and disclosure:** push/path/manual guards, selected SHA, JDK/task wiring, token/destination, and concurrency settings match the Spec. Required local evidence is recorded with hosted CI and real GitHub publication explicitly unverified.

Completion requires **G1, G2, and G3**, as defined in Waves. Required checking is local and focused; no full-project tests, loader/Minecraft matrix, real remote push, source commit/tag, or hosted workflow run is a necessary step. No shared publishing framework, manifest/receipt system, metadata generator, retention policy, version rewrite, or monotonicity guard is introduced. Existing feature plans and other repositories are excluded.

## Concrete ownership and execution strategy

Use three new, repository-local implementation carriers, plus one focused check carrier:

| Intended path (not yet created) | Responsibility |
| --- | --- |
| `.github/scripts/publish-maven.init.gradle` | Groovy Gradle init script: dedicated file repository, evaluated model/task selection, generation completeness, exact release comparison, native metadata validation, all-skipped restoration. No new Gradle plugin or build dependency. |
| `.github/scripts/publish_maven.ts` | Deno/TypeScript entry using Deno standard APIs: isolated baseline/candidate storage, Git source/destination checks, wrapper invocation, candidate commit and one guarded update. Grant only the Deno permissions required for temporary files, subprocesses, environment reads, and authenticated Git transport; production derives its destination from `GITHUB_REPOSITORY`, not a configurable publication service. |
| `.github/scripts/test_publish_maven.ts` | Focused Deno/TypeScript assertions for staging/policy/Git behavior and temporary consumer setup; real Gradle checks use the wrapper and the init script. Use Deno.test without an additional framework, with test-only permissions limited to the disposable fixtures and subprocesses it exercises. |
| `.github/workflows/maven-publication.yml` | GitHub event filters, manual guard, exact-SHA checkout, Java setup, scoped token and runner entry. |

No edits to root/models/IDE builds, properties, dependency versions, settings, or wrapper are intended. An init script keeps this CI destination separate from existing HTTP callers. If actual evaluated Gradle behavior contradicts this approach, report the concrete gap within the affected item rather than silently amending authority or repairing unrelated build behavior.

### Native publishing, completeness, and comparison

The Deno/TypeScript entry captures the remote `maven` tip (or confirms absence) and exports its tree into two temporary directories outside tracked source and `mavenLocal`: immutable baseline **B**, and writable candidate **C**, initially an exact copy of B. Absent branch means both start empty. Build outputs and temporary control data stay outside the candidate. Never seed from the user's Maven cache.

The init script applies only to projects with `maven-publish`, adds file repository `CiMaven`, and inspects all `MavenPublication` instances after project evaluation, including plugin-generated publications. Register aggregate `ciPublishMaven` depending only on `PublishToMavenRepository` tasks targeting that repository. Baseline source observations predict `:publishPluginMavenPublicationToCiMavenRepository`, `:publishClochePluginMarkerMavenPublicationToCiMavenRepository`, and `:cloche-models:publishMavenPublicationToCiMavenRepository`; discover rather than hard-code these. Empty models, unrepresented publications, duplicate output ownership, or incomplete current generation fail before Git update.

Wire preparation before every selected publication, with sequential task execution. Derive each version path and expected POM/artifact/classifier/extension set from the evaluated publication and the applicable enabled Gradle metadata-generation task. A marker can be POM-only; do not impose implementation attachments on it or models. Require successful execution of every selected destination task, existence of each current artifact source and generated POM/module output, and output bytes/checksums corresponding to those current sources. Do not infer success from retained paths or an aggregate task exit alone.

For every **release**, remove only its selected version directory from C before publishing; B stays untouched. Retain parent artifact metadata and other versions so Gradle updates the seeded history natively. Consequently every file under the new release version directory comes from this invocation, and an old JAR/POM/module/checksum cannot mask missing output. Validate expected current files and Gradle-generated checksums before comparing the complete generated version directory to B. Compare recursive relative-path sets and exact bytes, including extra files, checksums, and any in-version metadata; no exclusions, normalized XML, archive rewriting, or timestamp exceptions.

For every **snapshot**, keep its version directory seeded. Require execution of its native publishing task, validate the newly selected snapshot metadata against the evaluated artifacts and current artifact bytes, and validate referenced payload/checksums. Preserve all baseline timestamped files and checksums byte for byte, apart from mutable snapshot metadata/checksums; also preserve other versions and unrelated paths. A task or current artifact missing while old snapshot files remain must fail. Do not implement snapshot names or metadata generation independently of Gradle.

After all tasks and validation succeed, classify releases absent/equal/conflicting against B. Any conflict fails the whole run and requires a version bump; no subset reaches Git. Equal releases keep their baseline bytes. Restore artifact-level metadata/checksums from B for modules whose only current publications are skipped releases; this prevents metadata-only writes caused by their native publication. For an actively changed module, retain and validate Gradle's generated metadata against history. If all publications are skipped releases, restore C entirely to B and exit without commit/update. Snapshots count as native updates. This restoration uses baseline bytes, not a metadata generator.

The surgical release-directory reset is temporary generation hygiene, not deletion of published history: nothing is visible remotely until all comparisons pass. A second fresh-only publication repository or retained manifest is unnecessary.

### JDK and task wiring

Use Linux with Temurin **JDK 17** for the Gradle 9.4.0 wrapper and the root's JVM-17 toolchain; use the runner's pinned Deno runtime for the repository scripts; do not change IDE bytecode targets. The Deno entry invokes the equivalent of:

```sh
bash ./gradlew --no-daemon --no-configuration-cache --no-configure-on-demand --no-parallel \
  --init-script .github/scripts/publish-maven.init.gradle \
  -PciMavenBaseline=<absolute-temporary-B> -PciMavenOutput=<absolute-temporary-C> ciPublishMaven
```

The two path properties are internal local staging inputs; they never rewrite publication versions. Disabling those optimizations only for this command ensures whole-build late publication discovery and ordered preparation/validation. Inspect the evaluated selected task graph with `--dry-run`, then execute the real destination tasks; neither unqualified `publish` nor `publishToMavenLocal` is used. Gradle resolves external dependencies normally. Do not pass the GitHub credential to the Gradle subprocess.

### Git transaction and workflow

The runner uses the event-selected checkout SHA and checks that it is the allowed same-repository `refs/heads/main` source. It never switches to newer source or invents a version. Fetch/read the publication tree without touching the source working tree; create the candidate Git commit in disposable storage, parented to the captured `maven` tip or with no parent on first creation. Add only C's Maven tree at its root. Source branches and tags are read-only.

Immediately before the single explicit `HEAD:refs/heads/maven` push, query remote `main`: mismatch means stale-source skip, missing/failed lookup means failure. Recheck remote `maven` against the captured baseline and fail if it changed. Use a normal non-forced push, so an intervening advancing writer is also rejected by Git. No automatic retry: a later invocation rebuilds B/C from the current tip, reruns publication/comparison, and repeats freshness checking. The workflow additionally serializes the repository's publishers with one concurrency group and `cancel-in-progress: false`.

Use `GITHUB_TOKEN` with job-level `contents: write` for the same repository URL derived from GitHub context. The workflow invokes the repository entry with the pinned Deno runtime and explicit least-privilege permissions for environment reads, temporary file access, subprocesses, and the required Git/network transport; it must not grant broad read/write access to the repository or pass `GITHUB_TOKEN` into Gradle. Keep checkout credentials unpersisted; provide authenticated Git access only to the necessary Git subprocesses, without token-bearing URLs, logs, commits, or staged files. Denied writes fail without a credential/destination fallback. A local bare remote and rejecting hook model these failures; no production override CLI is needed.

On uncertain push results, read back the remote tip: expected commit confirms update; a definitive rejection with unchanged baseline confirms no update; a failed query or ambiguous competing state remains unconfirmed. Cancellation before the push leaves the remote unchanged. After a successful push, cancellation cannot undo it; never assert unchanged merely because a later step was cancelled. Local tests cover observable interruption points; abrupt hosted termination may leave confirmation unavailable and must be reported as such.

Workflow entry points are `push.branches: [main]`, `push.paths: ['**/gradle.properties']`, and `workflow_dispatch`. Reject manual branch/tag refs other than `refs/heads/main` before publication. Checkout `${{ github.sha }}` and pass that selected SHA to the runner. PRs, tags, unrelated paths, other branches, and `maven` updates have no automatic publication entry. Use checkout, Java-setup, and the pinned Deno runtime setup; the wrapper and Deno standard APIs suffice without a publishing action or additional package ecosystem.

## Verification arrangements and limits

The executable items own their checks; this is no extra test phase or second graph. G1 establishes S1/S2 using actual local Gradle publication and consumer resolution. G2 establishes S3 with the same native publisher against disposable bare Git. G3 establishes S4 and checks the combined wiring against the prior evidence. The [Spec's seven acceptance conditions](../../spec/maven-publication-ci.md#acceptance-conditions) are mapped explicitly in Waves.

Exercise equal payloads from captured actual local output and independently rebuild the same release to observe byte stability. If the rebuild differs, demonstrate batch conflict and report the exact difference; do not force reproducibility or weaken equality merely to obtain a no-op. Temporary local test copies may change versions for new-release/snapshot scenarios without modifying the source checkout. Reuse the user's existing Gradle user home, Gradle 9.4.0 distribution, JDK-17 toolchains, and proxy settings for an isolated temporary consumer project; do not create a replacement home or redownload the wrapper distribution for isolation. Constrain Cloche/marker/models resolution exclusively to the current candidate at a unique temporary file URL; normal external repositories remain available for external dependencies. Establish genuinely fresh consumption through actual candidate metadata/artifact source evidence and a focused warm-cache negative control: withhold the relevant candidate metadata/artifact, require resolution to fail despite same-GAV cached content, then restore it and require successful consumption. Use scoped refresh/revalidation as needed, without clearing user caches; a unique URL, refresh flag, cached artifact path, or success alone is insufficient proof. These observations belong to the same focused consumer check, not a duplicate publication suite or generic cache-isolation platform. If the reused home cannot provide this proof, report the exact gap rather than reverting to a new home or weakening acceptance. Keep evidence concise in these Work documents; do not commit generated Maven trees or test caches.

Local static workflow checks include a YAML syntax/schema check and explicit event/ref/path cases; a local YAML checker can be an ephemeral tool, not a project dependency. Local Git checks establish script behavior, not GitHub permissions, hosted path matching, branch policy, or a successful hosted run. Those remain explicit delivery limitations, not reasons to require an actual remote publication here.

## Risks and recovery

Exact native bytes may differ on rebuild; the prescribed failure/version-bump behavior is the compatibility contract. Native metadata retention, late marker discovery, model completeness, and JDK-17 execution remain empirical checks. Dependency availability and IDE project configuration may block the focused build; report the cause without expanding into unrelated module work.

A generation/conflict/validation failure discards temporary C and leaves the branch untouched. A rejected concurrent update requires a fresh invocation, not reuse of stale metadata. A successful Git update preserves prior commits and versions; this delivery does not plan destructive branch rollback or artifact pruning. Preserve the current local working tree and existing untracked artifacts throughout implementation.

## Author self-check

This is author evidence for independent review, not independent review or implementation acceptance.

| Check | Concrete reasoning/evidence |
| --- | --- |
| Authority | Context's PRD skip / ADR reuse / Spec reuse matches the current owners. Every S1–S4 commitment derives from the Spec; directory clearing, task wiring, Deno/TypeScript and Groovy carriers, and non-forced Git updates are implementation choices within it. No authority text was edited. |
| Success coverage | G1 requires S1/S2; G2 requires S3 using G1's publisher; G3 requires S4 with G1/G2 evidence. All seven Spec acceptance conditions have item-owned evidence in Waves, including marker/model conflicts, leftovers, retained snapshots, stale source, ambiguous update, and denied writes. |
| Single-result grouping | Native publication/comparison is a locally consumable capability even before Git wiring. The Git transaction consumes it and is testable before hosted routing. Workflow entry wiring consumes that runner. These three independent terminal boundaries are split; helper edits and their necessary regression checks stay with the capability they prove. No standalone testing, documentation, or per-file item is added. |
| Evidence sufficiency | Actual wrapper publish/consume and repeat/conflict checks are required, rather than file existence or static tasks alone. Bare-Git tests replace remote writes only for locally observable Git semantics; hosted limitations remain explicit. |
| Dependencies and conflicts | MP2 consumes MP1; MP3 consumes MP2. Native publisher and runner share the focused test carrier, so they serialize; workflow verification reads the settled runner. No claimed background overlap or dependency from display order. |
| Gates and cycles | Three non-empty Waves, one item each. Combined wait chain is MP1 → G1 → MP2 → G2 → MP3 → G3, with direct item prerequisites MP1 → MP2 → MP3. No forward reference, self-wait, cycle, or unstated global gate. Each gate requires the complete correlated item Result and stated evidence; none is released now. |
| Tuple and portable records | Four direct same-directory members; Plan links Work and exactly one Waves, which links the same Plan/Work. Existing-source links are repository-relative; future implementation paths are explicitly intended paths, not claimed readable evidence. |
| Scope and limits | The original preparation wrote only derived documents and asserted no Gradle/workflow/local publication/Git mutation. Its then-outstanding independent review and implementation confirmation subsequently occurred; see Context. Actual item evidence and gate judgments remain absent. |

Actual preparation verification (document/source checks only): an assert-based repository-script check using explicit UTF-8 reads passed for all four direct tuple files, the unique reciprocal Plan/Waves pair, all **29 relative link occurrences and their fragments**, three unique Waves/items/gates, exact item membership, resolved prerequisites, and the combined wait DAG `MP1 → G1 → MP2 → G2 → MP3 → G3`. It also checked the four S1–S4 labels and seven acceptance-coverage rows; the table above records the separate semantic assessment. SHA-256 comparison confirmed all **112 pre-existing tracked/untracked non-ignored files** were unchanged, with exactly the four intended additions and unchanged HEAD. Ignored build/cache contents were not exhaustively hashed. No build, publication, Git ref mutation, or workflow was executed.
