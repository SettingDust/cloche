# Processed context: Maven publication CI

Owner: [Work](work.md). Delivery strategy: [Plan](plan.md). Execution graph: [Waves](waves.md).

## Authority assessment reused for this scope

| Family | Assessment and owner | Evidence and reason |
| --- | --- | --- |
| PRD | Skip; infrastructure publishing only | The [Spec's authority boundary](../../spec/maven-publication-ci.md#authority-and-boundary) and [ADR context](../../adr/git-backed-maven-publishing.md#context) explicitly require no product requirement change. |
| ADR | Reuse; repository/trigger/token/tagging architecture | [Git-backed Maven publishing](../../adr/git-backed-maven-publishing.md) is accepted and delegates detailed conflict, freshness, and metadata contracts to the Spec. No different architecture is proposed. |
| Spec | Reuse; Cloche Maven CI behavior | [Maven publication CI](../../spec/maven-publication-ci.md) closes the three policy decisions and supplies seven acceptance conditions. The planned mechanics implement those rules without changing them. |

The supplied specification-settlement result reports completion and eight passing relative-link checks, with no Gradle execution. That historical result is adopted as supplied context, not a rerun or a repository original. Current preparation independently read the Spec, ADR, and source below. No external session/result locator is a persistent dependency.

## Current repository observations

Read-only baseline: local source at `d51199b4a398582ec0c71dbeea6de3523a4db0b2`, including existing untracked documentation. No remote synchronization was performed. Initial `git status --short` showed untracked `docs/`, `.intellijPlatform/`, and `src/test/resources/include-identity/build/`; those existing contents must be preserved.

- [Root build](../../../build.gradle.kts) applies `java-gradle-plugin` and `maven-publish`, declares plugin `cloche`, attaches sources/Javadoc, depends on `projects.clocheModels`, retains Maven-local and credentialed HTTP publishing destinations, and requests Kotlin JVM toolchain 17.
- [Settings](../../../settings.gradle.kts) includes `:cloche-models` from `models/` and `:cloche-idea` from `idea/`. [Models build](../../../models/build.gradle.kts) declares Maven publication `maven` from its Java component and the same existing publishing destinations.
- [IDE build](../../../idea/build.gradle.kts) has Marketplace `publishPlugin`, no declared Maven publishing plugin/publication. Do not invoke Marketplace work or remove this project from settings to make CI cheaper.
- [Properties](../../../gradle.properties) supply `group=earth.terrarium`, `version=0.19.13`, configuration cache, configuration on demand, and parallel execution. [Wrapper](../../../gradle/wrapper/gradle-wrapper.properties) selects Gradle 9.4.0.
- The inspected `.github/` tree contains issue templates and no existing workflow/scripts to reuse. The intended new implementation files are named in the Plan; they do not yet exist.

Expected source-derived model: root `pluginMaven`, root `clochePluginMarkerMaven`, and models `maven`. This is not an evaluated publication inventory. The future init script must discover the full model after evaluation; neither this list nor a fixed count defines coverage.

## Accepted delivery constraints

Only this repository is in scope. Branch root layout, `GITHUB_TOKEN`, automatic `main` properties-path pushes plus manual `main`, complete actual publications including markers, native metadata, history-seeded output, release path-and-byte equality, retained snapshot history, and pre-update current-`main` checking are settled authority. No version-monotonicity rule, custom metadata, shared publisher, manifest/receipt system, cleanup, source tagging, or central-repository changes are authorized.

The original invocation created the four initial Work documents and performed author self-check; its draft/awaiting-review wording describes that historical stage. The adopted independent review found the first Python-entry blocker closed by the Deno/TypeScript Plan/Waves and judged the plan ready. The user subsequently confirmed the three projects' Deno implementation and local verification scope and continued Maven publishing. No Cloche implementation item has started, and no gate is satisfied. This invocation is authorized only for local maintenance of this tuple, not implementation execution.

The current environment constraint is to reuse the existing Gradle user home, distribution, toolchains, and proxy rather than create a fresh home and download another wrapper distribution. MP1 still owes genuine fresh candidate-only marker → implementation → models consumption, excluding same-GAV cache reuse and external fallback. The [Plan](plan.md#verification-arrangements-and-limits) and [Waves](waves.md#w1--mp1--native-publication-and-version-policy-result) carry the bounded source/negative-control method. Supplied JVM experience with a unique file URL, observed sources, and warm-cache removal/restoration is a reference direction only, not Cloche execution evidence. P4/Cloche internal source adaptation remains deferred; this does not suspend the authorized Cloche Maven CI scope.

These are processed adopted facts from supplied coordination context, without an external Result locator or a promise of a repository original. The prior favorable review covers unchanged scope; this changed consumer method and its necessary connections remain subject to independent review. No Gradle run, configuration/dependency change, publication, commit, push, or plan execution occurs in this maintenance.

## Evidence limits and remaining work

Actual publication coverage, generated byte stability, JDK/task compatibility, local consumer resolution, metadata retention, and Git-update behavior have not been executed. These are implementation checks owned by the Waves items. Rebuilding a release may differ in archive or generated metadata bytes; the accepted response is conflict/version bump, never comparison normalization.

The proposed dedicated CI path disables configuration cache, configuration on demand, and parallel execution for reliable whole-model inspection and ordered staging; normal caller settings remain available. Local dependency downloads may still need network access. Existing IDE configuration may also affect configuration cost; it does not authorize a broader build repair.

Hosted runner behavior, GitHub token permissions/branch policies, and actual same-repository publication cannot be established by local bare-Git checks. The implementation report must retain those limitations. Prior deletion of legacy `maven-repo` and retention of the central repository are supplied facts, not operations or checks of this Work.
