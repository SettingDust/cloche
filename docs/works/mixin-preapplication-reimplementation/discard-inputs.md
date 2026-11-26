# 已执行授权弃置的历史路径输入

这是新 [Work](work.md) / [Plan](plan.md) / [Waves](waves.md) 的派生历史输入，**不可再次执行**。R0 已按下列 11 tracked 恢复和 129 删除路径执行，完整 Result 为 partial；已采用的核验及未知限制见 [Context](context.md)。固定恢复对象是 `5613cd2b4ce781aa71aa3e8b4af231b2b3f13d42`。路径均相对仓库根，仅用于识别已完成效果、保护对象和防止重放，不证明这些路径当前状态，也不授权覆盖后续编辑或删除同名新实现。

## tracked：11 个，已恢复工作树和索引到固定 HEAD

原 MinecraftTarget.kt 含 staged 修改，执行时已连同其余十项逐条恢复工作树及索引；这里不再提供恢复步骤。

- `build.gradle.kts`
- `src/main/kotlin/earth/terrarium/cloche/Linkage.kt`
- `src/main/kotlin/earth/terrarium/cloche/api/target/MinecraftTarget.kt`
- `src/main/kotlin/earth/terrarium/cloche/target/MinecraftTargetInternal.kt`
- `src/main/kotlin/earth/terrarium/cloche/target/TargetHandler.kt`
- `src/main/kotlin/earth/terrarium/cloche/target/common/CommonTargetCreator.kt`
- `src/main/kotlin/earth/terrarium/cloche/target/compilation/CompilationInternal.kt`
- `src/main/kotlin/earth/terrarium/cloche/target/compilation/CompilationVariantCreator.kt`
- `src/main/kotlin/earth/terrarium/cloche/target/compilation/TargetCompilation.kt`
- `src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricClientSecondarySourceSets.kt`
- `src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricTargetImpl.kt`

## 原 untracked 源码/测试/fixture：125 个，已逐文件删除

以下封闭名单记录已执行对象；目录名从未授权递归删除。后续在同一路径新建源码/fixture 不得再按本名单删除，既有旧内容也不作为重实现结构依据。

- `integration-test/mixin-preapplication/artifact-bridge/README.md`
- `integration-test/mixin-preapplication/artifact-bridge/build.gradle`
- `integration-test/mixin-preapplication/artifact-bridge/payload/unique.txt`
- `integration-test/mixin-preapplication/artifact-bridge/removals.json`
- `integration-test/mixin-preapplication/artifact-bridge/settings.gradle`
- `integration-test/mixin-preapplication/artifact-preregistration/README.md`
- `integration-test/mixin-preapplication/artifact-preregistration/build.gradle`
- `integration-test/mixin-preapplication/artifact-preregistration/mappings.tiny`
- `integration-test/mixin-preapplication/artifact-preregistration/payload/unique.txt`
- `integration-test/mixin-preapplication/artifact-preregistration/removals.json`
- `integration-test/mixin-preapplication/artifact-preregistration/settings.gradle`
- `integration-test/mixin-preapplication/artifact-selection/README.md`
- `integration-test/mixin-preapplication/artifact-selection/build.gradle`
- `integration-test/mixin-preapplication/artifact-selection/mixed-1.pom`
- `integration-test/mixin-preapplication/artifact-selection/payloads/library/role.txt`
- `integration-test/mixin-preapplication/artifact-selection/payloads/mod/role.txt`
- `integration-test/mixin-preapplication/artifact-selection/removals.json`
- `integration-test/mixin-preapplication/artifact-selection/settings.gradle`
- `integration-test/mixin-preapplication/compile/README.md`
- `integration-test/mixin-preapplication/compile/auxiliary.gradle`
- `integration-test/mixin-preapplication/compile/build.gradle`
- `integration-test/mixin-preapplication/compile/caller/auxiliary/Consumer.java`
- `integration-test/mixin-preapplication/compile/caller/disabled/Consumer.java`
- `integration-test/mixin-preapplication/compile/caller/empty/Consumer.java`
- `integration-test/mixin-preapplication/compile/caller/forbidden/ForbiddenConsumer.java`
- `integration-test/mixin-preapplication/compile/caller/forge/Consumer.java`
- `integration-test/mixin-preapplication/compile/caller/java/Consumer.java`
- `integration-test/mixin-preapplication/compile/caller/kotlin/KotlinConsumer.kt`
- `integration-test/mixin-preapplication/compile/caller/mapping/MappingConsumer.java`
- `integration-test/mixin-preapplication/compile/caller/own/OwnMixin.java`
- `integration-test/mixin-preapplication/compile/caller/plain/Consumer.java`
- `integration-test/mixin-preapplication/compile/caller/roles/ScopeConsumer.java`
- `integration-test/mixin-preapplication/compile/caller/server/Consumer.java`
- `integration-test/mixin-preapplication/compile/decompile.gradle`
- `integration-test/mixin-preapplication/compile/environments.gradle`
- `integration-test/mixin-preapplication/compile/game/client/game/Client.java`
- `integration-test/mixin-preapplication/compile/game/client/game/Duplicate.java`
- `integration-test/mixin-preapplication/compile/game/client/resources/client-only.txt`
- `integration-test/mixin-preapplication/compile/game/client/resources/duplicate.txt`
- `integration-test/mixin-preapplication/compile/game/common/game/Common.java`
- `integration-test/mixin-preapplication/compile/game/common/game/Duplicate.java`
- `integration-test/mixin-preapplication/compile/game/common/resources/bad-target.json`
- `integration-test/mixin-preapplication/compile/game/common/resources/common-only.txt`
- `integration-test/mixin-preapplication/compile/game/common/resources/duplicate.txt`
- `integration-test/mixin-preapplication/compile/game/common/resources/fixture.mixins.json`
- `integration-test/mixin-preapplication/compile/helper/helper/Parent.java`
- `integration-test/mixin-preapplication/compile/identity-mappings.tiny`
- `integration-test/mixin-preapplication/compile/linkage.gradle`
- `integration-test/mixin-preapplication/compile/mappings.tiny`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/ApiVariantMixin.java`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/ClientMixin.java`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/CommonMixin.java`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/DirectAccess.java`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/OptionalMixin.java`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/OrdinaryMixin.java`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/ProjectMixin.java`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/RefmapAccess.java`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/RuntimeMixin.java`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/RuntimeVariantMixin.java`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/SecondMixin.java`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/ServerMixin.java`
- `integration-test/mixin-preapplication/compile/mod/java/fixture/TransitiveMixin.java`
- `integration-test/mixin-preapplication/compile/mod/resources/bad-source.json`
- `integration-test/mixin-preapplication/compile/mod/resources/direct.mixins.json`
- `integration-test/mixin-preapplication/compile/mod/resources/fabric.mod.json`
- `integration-test/mixin-preapplication/compile/mod/resources/fixture.accesswidener`
- `integration-test/mixin-preapplication/compile/mod/resources/fixture.mixins.json`
- `integration-test/mixin-preapplication/compile/mod/resources/fixture.refmap.json`
- `integration-test/mixin-preapplication/compile/mod/resources/identity.txt`
- `integration-test/mixin-preapplication/compile/mod/resources/optional.mixins.json`
- `integration-test/mixin-preapplication/compile/mod/resources/refmap.mixins.json`
- `integration-test/mixin-preapplication/compile/mod/resources/unique.txt`
- `integration-test/mixin-preapplication/compile/neo-mod/META-INF/neoforge.mods.toml`
- `integration-test/mixin-preapplication/compile/nested/fabric.mod.json`
- `integration-test/mixin-preapplication/compile/nested/never-discover.mixins.json`
- `integration-test/mixin-preapplication/compile/own-resources/own.mixins.json`
- `integration-test/mixin-preapplication/compile/projectMod/build.gradle`
- `integration-test/mixin-preapplication/compile/publication.gradle`
- `integration-test/mixin-preapplication/compile/resource-staging.gradle`
- `integration-test/mixin-preapplication/compile/roles-resources/apivariant/apivariant.mixins.json`
- `integration-test/mixin-preapplication/compile/roles-resources/apivariant/fabric.mod.json`
- `integration-test/mixin-preapplication/compile/roles-resources/apivariant/identity.txt`
- `integration-test/mixin-preapplication/compile/roles-resources/ordinary/fabric.mod.json`
- `integration-test/mixin-preapplication/compile/roles-resources/ordinary/identity.txt`
- `integration-test/mixin-preapplication/compile/roles-resources/ordinary/ordinary.mixins.json`
- `integration-test/mixin-preapplication/compile/roles-resources/project/fabric.mod.json`
- `integration-test/mixin-preapplication/compile/roles-resources/project/identity.txt`
- `integration-test/mixin-preapplication/compile/roles-resources/project/project.mixins.json`
- `integration-test/mixin-preapplication/compile/roles-resources/runtime/fabric.mod.json`
- `integration-test/mixin-preapplication/compile/roles-resources/runtime/identity.txt`
- `integration-test/mixin-preapplication/compile/roles-resources/runtime/runtime.mixins.json`
- `integration-test/mixin-preapplication/compile/roles-resources/runtimevariant/fabric.mod.json`
- `integration-test/mixin-preapplication/compile/roles-resources/runtimevariant/identity.txt`
- `integration-test/mixin-preapplication/compile/roles-resources/runtimevariant/runtimevariant.mixins.json`
- `integration-test/mixin-preapplication/compile/roles-resources/second/fabric.mod.json`
- `integration-test/mixin-preapplication/compile/roles-resources/second/identity.txt`
- `integration-test/mixin-preapplication/compile/roles-resources/second/second.mixins.json`
- `integration-test/mixin-preapplication/compile/roles.gradle`
- `integration-test/mixin-preapplication/compile/settings.gradle`
- `integration-test/mixin-preapplication/compile/stub.gradle`
- `integration-test/mixin-preapplication/compile/stub/caller/client-plain/ClientCaller.java`
- `integration-test/mixin-preapplication/compile/stub/caller/client/ClientCaller.java`
- `integration-test/mixin-preapplication/compile/stub/caller/forbidden/ForbiddenCaller.java`
- `integration-test/mixin-preapplication/compile/stub/caller/main/SharedCaller.java`
- `integration-test/mixin-preapplication/compile/stub/caller/plain/SharedCaller.java`
- `integration-test/mixin-preapplication/compile/stub/mod/fixture/OnlyMixin.java`
- `integration-test/mixin-preapplication/compile/stub/mod/fixture/SharedMixin.java`
- `integration-test/mixin-preapplication/compile/stub/only/fabric.mod.json`
- `integration-test/mixin-preapplication/compile/stub/only/only.mixins.json`
- `integration-test/mixin-preapplication/compile/sync-ide-model.gradle`
- `integration-test/mixin-preapplication/compile/sync.gradle`
- `integration-test/mixin-preapplication/compile/test-lifecycle.gradle`
- `integration-test/mixin-preapplication/compile/transitive/fabric.mod.json`
- `integration-test/mixin-preapplication/compile/transitive/identity.txt`
- `integration-test/mixin-preapplication/compile/transitive/transitive.mixins.json`
- `integration-test/mixin-preapplication/exact-file-strip/README.md`
- `integration-test/mixin-preapplication/exact-file-strip/build.gradle`
- `integration-test/mixin-preapplication/exact-file-strip/probe.mixins.json`
- `integration-test/mixin-preapplication/exact-file-strip/removals.json`
- `integration-test/mixin-preapplication/exact-file-strip/settings.gradle`
- `src/main/kotlin/earth/terrarium/cloche/target/compilation/MixinPreapplication.kt`
- `src/main/kotlin/earth/terrarium/cloche/target/compilation/SelectMixinCompileArtifact.kt`
- `src/main/kotlin/earth/terrarium/cloche/target/compilation/TestCompilationVariants.kt`
- `src/main/kotlin/earth/terrarium/cloche/tasks/MergeMinecraftClient.kt`
- `src/test/kotlin/earth/terrarium/cloche/MixinPreapplicationTests.kt`

## 原 untracked 旧派生 Work 文档：4 个，已逐文件删除

用户明确授权“旧的文档也不需要，开始吧”；新增删除边界亦已独立复核无阻断，下列四项已执行删除。它们不再是本套文档的必要证据输入，不授权删除所在目录或原始日志/Results、build 证据。

- `docs/works/mixin-preapplication/context.md`
- `docs/works/mixin-preapplication/plan.md`
- `docs/works/mixin-preapplication/waves.md`
- `docs/works/mixin-preapplication/work.md`

## 保留集合

原八个 untracked 保护文件中的其余四个继续保留：

- `.intellijPlatform/localPlatformArtifacts/IC-243.22562.145/bundledPlugin-com.intellij.gradle-IC-243.22562.145.xml`
- `.intellijPlatform/localPlatformArtifacts/IC-243.22562.145/bundledPlugin-com.intellij.java-IC-243.22562.145.xml`
- `docs/prd/mixin-preapplication.md`
- `docs/spec/mixin-preapplication.md`

同时保留本目录全部新文档、所有忽略内容/缓存/历史证据（含原始日志/Results、build 证据和 IDEA 生成文件）和相邻 Codev。新出现文件不自动归入弃置范围。

## 剩余必要核验的用途

本清单的写效果已执行，不得重放恢复/删除，也不重新执行原前扫描或建立历史全树基线。原 R0 九个明确保护文件的前后字节核对和 Git 可见集合结论已采用；汇总净少 2 文件/9057 字节、前快照丢失、Codev 后比较未完成及不可归因事实完整保留在 Context，不能声称全树未变。

新的 R0V 只读检查上列 PRD/Spec/两个 XML、本目录 `work.md`、`context.md`、`plan.md`、`waves.md`、本文件共九个明确保护对象，以及 Context 证据表的四份报告当前存在可读。四份报告分别供开关、通知、Tooling 与 Codev 命名设计消费；其精确相对入口仅在现有 Context/Plan 维护，不扩为扫描目录或全部缓存。授权维护过的新 Work 字节不与 R0 前快照比较。原始证据、缓存、忽略文件及相邻 Codev 的范围外保护继续有效，不以全量历史字节相等作 G0 门槛。

R0V 的唯一 predicate、证据、冲突及 G0 连接仅在 Waves 定义。若具体必要输入缺失/不可读或发现实际安全 gap，返回受影响对象/消费者和限制，不恢复未知文件、不补造报告、不改权限或共享进程。未来实现须自己的当前调用/JIT 和 V1–V7 证据，本文不能代替；当前 R0V 与所有 gates 仍未完成。
