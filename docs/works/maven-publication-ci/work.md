# Maven publication CI work

Prepare and, after separate implementation-scope confirmation, deliver Cloche's same-repository Maven publication CI. The selected build must publish its actual Maven publications to the root of `maven`, preserving history and enforcing release equality, native snapshot updates, current-`main` provenance, and safe Git updates.

Authority: [Maven publication CI Spec](../../spec/maven-publication-ci.md) and [accepted publishing ADR](../../adr/git-backed-maven-publishing.md).

Selected tuple: this `work.md`, [processed Context](context.md), [implementation Plan](plan.md), and its sole [executable Waves](waves.md). These are derived delivery documents, not authority.

Current stage: the earlier author self-check and adopted independent delivery-plan review are historical facts; the Deno/TypeScript plan was reviewed ready and the user subsequently confirmed local implementation and verification scope, as recorded in [Context](context.md). MP1–MP3 remain unstarted and G1–G3 unsatisfied. This maintenance only aligns the consumer environment method; its author self-check is not implementation evidence or independent review of the changed boundary.

Completion signal for the implementation: all three gates defined in the selected Waves satisfy the Plan success conditions, with focused local publication/consumption, version-content, Git-update, and workflow evidence. Hosted GitHub Actions execution and real remote publication are explicitly outside this delivery's necessary checks and must remain unverified in its report.

Existing module-feature work, other repositories, the retained central Maven repository, and prior branch deletion are outside this Work.
