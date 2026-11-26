# Git-backed Maven publishing

Status: Accepted; implementation pending.

## Context

Cloche needs CI publishing of its Gradle plugin and Maven publications to a Maven repository owned by the same GitHub repository. The accepted baseline is the current local working tree; preparing this change must not synchronize remote source code. This is an infrastructure decision and does not change product requirements.

The user accepted the publishing boundaries below. This ADR records that acceptance; it does not establish that CI has been implemented or that a publication has succeeded.

## Decision

- Publish to the `maven` branch of `github.repository`, with Maven repository paths at the branch root. Each participating project owns its repository's publication branch; Cloche publishes to its own repository.
- Automatically trigger publishing only for pushes to `main` that change the root `gradle.properties`. Nested fixture changes do not select a release. Also provide `workflow_dispatch` for manual publishing. Pull requests, other branches, and tags must not automatically publish.
- Do not create or move source tags as part of publishing.
- Use the same repository's `GITHUB_TOKEN` with `contents: write` to update the publication branch.
- Generate the project's actual Maven publications, including Gradle plugin marker publications, into a dedicated local output directory. Do not copy the entire `mavenLocal` repository.
- Preserve existing HTTP publishing capability, but do not invoke it from the new CI publishing path.

## Rationale

Keeping publication artifacts in the producing repository aligns ownership and GitHub permissions without requiring a separate publishing repository. A Maven layout at the branch root provides a direct repository layout. Restricting automatic publication to version-properties changes on `main` makes the automatic trigger explicit, while manual dispatch supports intentional runs. Dedicated publication output limits uploaded content to this project's publications and includes the plugin markers needed for plugin resolution.

The older external Deno shared publisher was not accepted and is not introduced by this decision. This decision does not introduce a shared publishing framework, manifests, or publication receipts.

## Consequences and boundaries

The `maven` branch becomes the repository's Maven artifact store, and publishing CI needs permission to write it. Source tagging remains independent of publishing. Existing HTTP publishing remains available outside this CI path.

According to the supplied acceptance context, the three projects' legacy `maven-repo` branches have already been deleted and their absence checked with `ls-remote`; the central `SettingDust/maven` repository is retained. Those are reported prior actions, not verification performed for this ADR. Replacing Codev's obsolete publishing workflow belongs to Codev's implementation, not to this Cloche document.

The [Maven publication CI Spec](../spec/maven-publication-ci.md) now owns the accepted conflict, source-freshness, and metadata rules. Both automatic and manual publication are restricted to `main`; immediately before updating `maven`, a run must verify that its selected source commit is still the current remote `main` HEAD, skipping an outdated run. A current-source rerun is allowed: complete identical release payloads are skipped, differing release payloads fail and require a version bump, and snapshots use Gradle's native updates while retaining historical snapshots without cleanup. The dedicated output is seeded from the existing `maven` tree, or initialized empty when that branch is absent, before Gradle maintains metadata and checksums. These rules do not require monotonically increasing version numbers. Existing Mixin, include, and JVM module documentation and plans have different ownership and are unaffected.

## Revisit criteria

Reconsider this decision if repository or permission policy prevents writing the publication branch, if artifact storage or distribution needs outgrow a Git branch, or if the publishing Spec reveals a correctness requirement incompatible with this ownership or trigger model.
