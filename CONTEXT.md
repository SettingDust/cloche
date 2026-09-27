# Mod Multi-Target Orchestration Context

This bounded context describes the language Mod authors use to declare target variants, shared sources, compilation views, and delivery intent in Cloche. It does not own Minecraft game artifact naming, loader packaging formats, or JVM multiplatform implementation semantics.

## Language

**target**: A named build target within a Cloche project. A concrete target selects a Minecraft version and a Fabric, Forge, or NeoForge loader; a common target represents a shared source scope. A target name may be empty in single-target mode. [Target declarations](src/main/kotlin/earth/terrarium/cloche/ClocheExtension.kt#L66)

**concrete target**: A target that describes a loader-specific Minecraft variant and can provide the inputs for a development or distribution view. It is distinct from a common target. [Target configuration](src/main/kotlin/earth/terrarium/cloche/ClocheExtension.kt#L160)

**common target**: A target whose purpose is shared sources for concrete targets. It is not an independently runnable game target, and its applicable version set comes from its concrete dependents. [Common target](src/main/kotlin/earth/terrarium/cloche/target/common/CommonTargetCreator.kt#L77) [Run boundary](src/main/kotlin/earth/terrarium/cloche/target/common/CommonTargetCreator.kt#L131)

**compilation**: A purpose-specific source and dependency view belonging to a target. `main`, `client`, `data`, and `test` are compilation names, not loaders or individual task executions. [Compilation](src/main/kotlin/earth/terrarium/cloche/api/target/compilation/Compilation.kt#L16) [Usage](README.md#L79)

**common**: Shared source relationships among Cloche targets. It is not Minecraft Codev's game common/client artifact split or JVM Multiplatform's common API intersection. [Common relationships](src/main/kotlin/earth/terrarium/cloche/ClocheExtension.kt#L218)

**target attributes**: The target identity values used for dependency matching, including loader and game-version characteristics. They are not a Mod ID, namespace, or capability. [Published attributes](src/main/kotlin/earth/terrarium/cloche/api/attributes/Attributes.kt#L1)

**capability**: A consumer-requestable identity exposed by a published view. It is not a namespace and does not state that an artifact has been remapped. [Published views](src/main/kotlin/earth/terrarium/cloche/target/compilation/CompilationVariantCreator.kt#L13)

**variant**: A consumable view of a compilation selected through dependency attributes and capabilities. It is not a loader distribution format. [Published views](src/main/kotlin/earth/terrarium/cloche/target/compilation/CompilationVariantCreator.kt#L13)

**include**: An explicit request to embed selected dependencies in a Mod JAR. It is distinct from regular compile/runtime dependencies and from Gradle settings `include`; the selected resolution is non-transitive. [Include selection](src/main/kotlin/earth/terrarium/cloche/target/compilation/TargetCompilation.kt#L274)

**Mod identity**: The Mod ID and version identifying the Mod, separate from the Minecraft version and loader version. A concrete target requires a Mod ID. [Configuration checks](src/main/kotlin/earth/terrarium/cloche/ClocheExtension.kt#L160)

**distribution namespace**: The namespace selected when a non-test compilation is prepared for delivery. It is distinct from target attributes and from a mapping namespace owned by Minecraft Codev. [Distribution selection](src/main/kotlin/earth/terrarium/cloche/target/compilation/TargetCompilation.kt#L303)

**unified Mod metadata**: Cloche's Mod declaration before translation to loader-specific metadata. It does not make Fabric JSON and Forge/NeoForge TOML interchangeable. [Fabric metadata](src/main/kotlin/earth/terrarium/cloche/tasks/GenerateFabricModJson.kt#L119) [Forge metadata](src/main/kotlin/earth/terrarium/cloche/target/forge/ForgeCompilationImpl.kt#L82)

**Mixin configuration**: A declared resource or metadata reference for Mixins. Its presence does not mean that Mixins have already been applied. [Mixin resources](src/main/kotlin/earth/terrarium/cloche/target/TargetHandler.kt#L241)

## Unresolved

- Repository ownership and user contracts for shared-target cycles, isolated common targets, and common views with no concrete dependents are not established by the inspected source.
- The source does not establish that every embedded dependency has a recoverable embedding coordinate or that all dependency categories are represented in loader metadata.
- Cloche's published dependency versions and the neighboring local checkouts are not proven to be the same integration artifact.
