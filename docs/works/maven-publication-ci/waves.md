# Maven publication CI Waves

Matched [Plan](plan.md), [Work](work.md), and [Context](context.md). This is the sole executable graph for the Plan's S1–S4 outcome. Authority: [Spec](../../spec/maven-publication-ci.md) and [ADR](../../adr/git-backed-maven-publishing.md).

Every item remains unstarted; every gate is unsatisfied. Earlier author self-check, adopted independent plan review (ready), and subsequent user implementation-scope confirmation are recorded in [Context](context.md). This maintenance changes only the MP1 consumer environment/evidence method and completes no item or gate. Each item has one Handler and exactly one Result. The Router must read and correlate the complete Result and judge its required evidence; `partial`, `failed`, `blocked`, absent Results, or missing evidence satisfy no item or gate. The changed boundary still requires independent review; author self-check does not supply it.

## Graph and grouping

| Wave | Items | Required predecessor gates | Incremental outcome | Release gate |
| --- | --- | --- | --- | --- |
| W1 | MP1 | None | Locally consumable native Maven publication with version/history policy | G1 |
| W2 | MP2 | G1 | Guarded Git transaction consuming that publisher | G2 |
| W3 | MP3 | G2 | CI entry wired to the verified local transaction | G3 |

Item prerequisites are MP2 → MP1 and MP3 → MP2 (an arrow here means "requires"). Each item belongs to exactly one Wave. Each gate waits for every item in its own Wave, never for its own release. Gates establish the Plan outcome jointly: G1 proves S1/S2, G2 proves S3, and G3 proves S4 with the prior evidence.

Grouping rationale: MP1's file-repository publisher can be invoked and consumed locally without Git; its native output and comparison predicate survive a later transaction failure. MP2's Git runner has independent local-remote acceptance and is consumed by workflow wiring. MP3 provides the independently observable GitHub entry/configuration boundary. Keep these boundaries separate. Model discovery, staging hygiene, comparison, and necessary native-publication checks jointly establish MP1; Git failure checks jointly establish MP2; workflow static checks jointly establish MP3. File count and test count do not create extra items.

Known conflicts: MP1 and MP2 both own `.github/scripts/test_publish_maven.ts` and must serialize against the latest file. MP2 consumes the init-script behavior; MP3 reads the settled runner and must not change its contract silently. All items read the same Spec/ADR, which alone is not a conflict. Do not overlap actual Gradle fixtures sharing build output or Gradle user-home state; isolate temporary copies or run checks serially. No unknown tool/capability assumption permits parallel work.

## W1 / MP1 — native publication and version-policy result

- **Prerequisite items:** none. **Containing-Wave prerequisite gates:** none.
- **Writes:** new `.github/scripts/publish-maven.init.gradle` and focused native-publication/check support in `.github/scripts/test_publish_maven.ts`. Read existing root/models/settings/IDE builds; do not change them. Test Maven trees and consumer projects stay in temporary storage; reuse the existing Gradle user home, distribution, toolchains, and proxy without clearing user caches. No Git source/ref changes.
- **Bounded effects:** implement the Plan's single dedicated destination and aggregate task, late evaluated-model discovery, history-seeded generation, complete current-output checks, full release comparison, equal-release metadata restoration, and native snapshot/history validation. No Git transaction or workflow changes.
- **Terminal predicate:** invoking the native publisher with immutable B and seeded C produces exactly the complete, Spec-compliant candidate or fails without modifying B; its Maven output is consumable locally. An all-skipped release batch leaves C identical to B.
- **Required evidence:**
  - Execute Gradle 9.4.0 on JDK 17 using the Plan's init-script flags. Report the evaluated publication identities/coordinates, all selected destination-task paths and outcomes, and inspect `--dry-run` coverage. Prove no HTTP destination, `publishToMavenLocal`, or Marketplace upload task is selected. Unexpected empty/unrepresented output fails.
  - Publish the actual Cloche model into temporary file storage. Inspect implementation JAR/sources/Javadoc, models' actual artifacts, marker POM dependency, applicable module metadata, native repository metadata, and checksum validity. Confirm direct Maven group layout and absence of source/cache/control files.
  - Resolve the plugin marker to implementation and models in an isolated temporary consumer project, reusing the user's existing Gradle user home, Gradle 9.4.0 distribution, JDK-17 toolchains, and proxy. Configure content/exclusive-content rules for candidate-only resolution of those coordinates at a unique temporary file URL; external dependencies may use normal repositories. Resolve/apply the plugin without a Minecraft or full-project test matrix, and record actual candidate metadata/artifact sources for the marker, implementation, and models. Within this same focused check, withhold relevant candidate metadata/artifact after warming the cache, require resolution failure despite same-GAV cached content, then restore and require success; use scoped refresh/revalidation as needed. Do not treat a unique URL, refresh flag, cached artifact path, or success alone as fresh-source proof. Do not clear user caches, create a replacement home/redownload its distribution, add a duplicate publication suite, or introduce a generic cache-isolation platform. Report a concrete proof gap if the reused environment cannot exclude same-GAV cache reuse or external fallback.
  - Preserve a baseline with historical releases, unrelated paths, and timestamped snapshots/checksums. Add a new release and verify old releases are byte-identical while Gradle-generated artifact metadata/checksums describe retained and new versions. Publish a snapshot twice through the seeded destination, validating current snapshot metadata and retaining previous timestamped artifacts/checksums without pruning.
  - Use captured real payloads for an identical-release case; assert exact path/byte skip and all-skipped full-tree equality, including metadata. Independently rebuild the same release and record equal bytes or exact conflict differences honestly. No reproducibility requirement is invented.
  - For implementation, marker, and models independently, add/remove/change an existing version payload file and assert whole-batch conflict requiring a version bump. Cover POM, main/classifier artifacts where configured, module metadata where applicable, checksums, and in-version metadata. Include an incomplete existing release, a mixed new/equal batch, and a mixed new/conflicting batch; no subset may become an accepted candidate after conflict.
  - Suppress/fail a current POM/artifact/module generation or destination task while seeding its previous output; require failure rather than a false complete publication. Test snapshot current-output validation with retained old payloads too. Verify B is untouched after every failure and no release files are excluded or normalized for comparison.
- **Conflicts:** MP2 shares the check carrier and consumes this behavior. No production or remote publication side effect is permitted by this item.

**G1 release predicate:** MP1 has a `completed` Result with all required local native-publication, consumer, conflict, no-op, and history evidence; the reported outcomes establish S1/S2 with no unexplained publication omission. A failed byte-identical rebuild is acceptable only when the policy correctly reports conflict and a captured equal-payload case separately proves skip. Missing actual publish/consume evidence does not release G1. G1 releases only W2.

## W2 / MP2 — safe Git publication transaction result

- **Prerequisite items:** MP1. **Containing-Wave prerequisite gates:** G1.
- **Writes:** new `.github/scripts/publish_maven.ts`; extend `.github/scripts/test_publish_maven.ts` against the settled init-script interface. The production entry uses only the explicitly granted Deno permissions for temporary files, environment reads, subprocesses, and authenticated Git transport; tests use disposable fixtures and narrower test permissions. Disposable bare remotes, local candidate commits, and temporary source fixtures are allowed only in future confirmed local verification; never commit/push the real source repository or contact a real write destination.
- **Bounded effects:** implement baseline acquisition, isolated B/C initialization, Deno subprocess invocation of the wrapper, all-skipped exit, same-repository production destination/token handling, candidate commit, source/baseline guards, one non-forced branch update, and truthful push reporting. No workflow yet; no automatic retry.
- **Terminal predicate:** the runner updates only a disposable remote's `maven` ref with one complete candidate when all contracts pass, or leaves it unchanged for each proved pre-update failure/skip; uncertain/post-update outcomes retain accurate confirmation status.
- **Required evidence:**
  - Exercise absent branch creation and existing-history update via a local bare remote using MP1's actual native publisher at least for the successful integrated path. Compare the resulting Git tree with the candidate, verify Maven paths at branch root, and prove source branches/tags and unrelated historical repository paths remain unchanged. Check excluded source/build/credential data never enters the commit.
  - For both push and manual-source selections, advance the local remote `main` after selection and require stale-source skip with unchanged `maven`. Fail source-HEAD lookup and require no update. Current HEAD rerun must pass freshness; an older declared version must reach MP1's version rules. Do not fetch/checkout newer source for an old run.
  - Confirm equal all-release output creates no commit/ref update. Generation error, missing current publication, release conflict in any publication, validation failure, and cancellation before push must leave the remote publication tip unchanged. Reuse the actual comparator, not a duplicate simplified policy.
  - Interpose a competing `maven` commit both before the baseline guard and between that guard and push. Require stale/precondition or non-fast-forward failure without losing the winning tree. A separate fresh invocation must reseed/revalidate from the winner and repeat source checking; there is no blind automatic retry.
  - Deny writes with a local rejecting hook; require failure with no fallback. Inspect production credential/destination construction and token redaction; actual GitHub token enforcement remains unverified.
  - Inject a transport/error result after an accepted local push and test read-back confirmation; inject failed read-back and require unconfirmed reporting. Simulate interruption after a successful update and assert it is not described as unchanged. Local failure injection may replace subprocess outcomes at the push boundary while ordinary success and concurrent rejection exercise real Git.
- **Conflicts:** consumes MP1 and shares its test file, hence serial. MP3 consumes the runner interface and its evidence. Remote-write semantics are tested only against disposable local Git.

**G2 release predicate:** MP2 has a `completed` Result with the stated local Git and integrated-native-publisher evidence establishing S3; no lost history, source-ref mutation, fallback write, or false outcome report remains. G1 evidence is still applicable to the runner's exact publisher interface. G2 releases only W3.

## W3 / MP3 — workflow entry and integrated wiring result

- **Prerequisite items:** MP2. **Containing-Wave prerequisite gates:** G2.
- **Writes:** new `.github/workflows/maven-publication.yml`; workflow-specific assertions in `.github/scripts/test_publish_maven.ts` only if necessary. No changes to existing build configuration or dependencies. Record concise actual verification and limitations in this Work's existing documents as part of the result, not as a separate execution graph or receipt.
- **Bounded effects:** connect GitHub event routing and allowed-ref rejection, selected-SHA checkout, Linux/JDK-17 setup, token scope, publication concurrency, and Deno/TypeScript entry. Preserve the settled native/Git contracts.
- **Terminal predicate:** the checked-in workflow statically matches the Spec's event/authentication/task contract and invokes the locally verified runner without widening publication scope or requiring real remote publication.
- **Required evidence:**
  - Parse/lint workflow syntax and relevant Actions fields with a focused local checker; record its name/version or command. Inspect root and nested `gradle.properties` main-push cases, unrelated-path push, PR, other-branch push, tag, and `maven` push. Include manual `main`, other branch, and tag; disallowed manual refs must reject before Gradle publication.
  - Verify checkout uses the permitted event's SHA; same-repository URL and `contents: write` use `GITHUB_TOKEN`; token is absent from Gradle inputs and persisted checkout credentials. Verify the pinned Deno runtime, explicit least-privilege Deno permissions, JDK 17, wrapper 9.4.0, `ciPublishMaven` init-script invocation with the required flags, no aggregate HTTP upload/Marketplace task, and one concurrency group with cancellation disabled.
  - Correlate G1/G2 checks with the actual workflow entry and run the focused local entry/argument checks once if wiring differs. Do not repeat the expensive publish suite without a changed contract, failure, or unresolved gap. Any behavioral fix to an earlier carrier requires its affected checks and preserves the item/gate evidence boundary.
  - Record what passed locally and what remains unverified: hosted event/path semantics, hosted Java/tool setup, actual GitHub token/branch-policy enforcement, hosted cancellation, and real publication. Do not describe static validation as an Actions run.
- **Conflicts:** reads settled MP1/MP2 interfaces; shares the check carrier if assertions are needed. Changes to those interfaces cannot be hidden as YAML-only wiring.

**G3 release predicate:** MP3 has a `completed` Result with workflow static/entry checks establishing S4; G1/G2 evidence remains applicable and all seven Spec acceptance conditions have the required local evidence or the explicitly allowed hosted limitation. G3 is the final integration gate and has no dependent Wave. Favorable plan review alone releases none of G1–G3.

## Spec acceptance coverage

| Spec acceptance condition | Item-owned evidence and gate |
| --- | --- |
| 1: evaluated model and destination tasks | MP1 native inventory/task graph, G1; MP3 verifies runner wiring, G3. |
| 2: actual tree and marker → implementation → models consumption | MP1 actual publication, content/checksum inspection and exclusive local-coordinate consumer, G1. |
| 3: events, selected source, stale/current source and older declared versions | MP3 event/ref cases, G3; MP2 both-entry freshness/lookup/rerun cases, G2; MP1 version rules, G1. |
| 4: absent/existing Git layout and retained release/snapshot/native metadata | MP1 native history checks, G1; MP2 local branch creation/integration, G2. |
| 5: complete equality, batch conflicts, leftovers, new-version metadata | MP1 actual payload fixtures, full directory reset/current-source checks and mutation cases, G1; MP2 no-update enforcement, G2. |
| 6: atomic update, tags, concurrency, cancellation, retry and ambiguous push | MP2 disposable Git transaction/fault cases, G2; MP3 concurrency configuration, G3. |
| 7: destination/token scope and denied writes | MP2 credential/destination inspection plus local hook denial, G2; MP3 workflow permission checks and hosted limitation, G3. |

## Author graph self-check

Three unique item IDs and three unique Wave IDs; membership is non-empty and exactly once. Every item and gate prerequisite resolves. Combined execution waits are MP1 → G1 → MP2 → G2 → MP3 → G3, with the equivalent direct item-prerequisite edges; no path returns to its origin. Each gate waits for its own completed item, not its own release. No display-order dependency or implicit barrier exists.

S1/S2 → MP1/G1, S3 → MP2/G2, S4 → MP3/G3 covers every Plan condition. The native-output, transaction, and workflow boundaries each have independently observable acceptance, while their necessary checks have no separate retained Result. Shared check-file ownership is explicitly serialized. No previous item completion/history was migrated or discarded. These checks establish graph consistency only; all actual item evidence and gate judgments are still absent.

Consumer-method maintenance author self-check: read the complete current tuple and owning Spec/ADR; neither authority requires a new Gradle user home, so no authority conflict was found. Checked all 34 current repository-relative link occurrences and their fragments. Compared S1–S4, MP1–MP3 terminal predicates, G1–G3 release predicates, Wave membership/predecessors, item prerequisites, and seven acceptance mappings with the pre-maintenance tuple: unchanged. Rechecked the combined acyclic wait chain and shared-check-file serialization. Release equality/restoration/no-op, native snapshot history, non-forced Git, JDK 17/Gradle 9.4.0, and init-script scope remain intact. The changed MP1 evidence method and matching Plan/Context/Work status connections are the independent-review boundary. This check executed no build, implementation test, publication, workflow, or Git write, and establishes no actual consumer freshness or item/gate completion.
