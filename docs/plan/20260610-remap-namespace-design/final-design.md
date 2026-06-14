# Cloche Remap Namespace 最终可实现设计

## 1. 设计结论

Cloche 的 mod dependency remap 需要把「用户声明命名空间」「artifact 当前命名空间」「transform 输出命名空间」「最终 classpath 消费命名空间」拆开建模。

核心结论：

- 用户 DSL 只暴露 source namespace override：`modImplementation(notation)` 与 `modImplementation(notation, sourceNamespace = "...")`；不新增 `modImplementationIntermediary`、`modCompileOnlySrg` 这类分裂 API。
- 用户传入的 `sourceNamespace` 是 Cloche 声明元数据，不得写入 `ModuleDependency.attributes`。
- `RemapNamespaceAttribute.ATTRIBUTE` 从 dependency declaration attribute 迁移/废弃；它只能短期作为兼容入口被读取并转换为内部声明，不再作为推荐 public API，也不作为 transform identity。
- 内部可以继续使用 per-source namespace bucket，但 bucket 必须是 lazy、dedup、受限、不可文档承诺的实现细节。
- Artifact transform 只接受完整解析后的 artifact view，不做文件名/路径式 patchy filtering；replacement 以 selected component + variant + artifact identity 为主键。
- Compile/runtime classpath 统一消费显式 `consumerClasspathNamespace`。MVP 中 compile 默认为 `named`，但必须从 target/compilation contract 显式传入，不能散落使用全局常量。
- 增加诊断模型和 `clocheRemapReport` 任务，报告声明、bucket、resolution、transform、replacement 与最终 classpath normalize 结果。

## 2. 命名空间生命周期

| 名称 | 含义 | 典型值 | 谁设置 | 是否 public contract |
| --- | --- | --- | --- | --- |
| `declaredSourceNamespace` | 用户对某个 mod dependency 的源 namespace 声明 | `intermediary`, `srg`, `obf`, `none`, 自定义 mapping namespace | DSL/兼容适配层 | DSL 是 public；存储方式不是 |
| `sourceNamespace` | raw artifact 当前字节码/metadata 所在 namespace | 同上 | classifier/router | internal |
| `artifactNamespace` / `currentArtifactNamespace` | 某个 resolved artifact 在当前阶段实际 namespace | raw 阶段为 source；remapped 阶段为 output | artifact view/transform result | internal |
| `outputNamespace` | transform 输出 artifact namespace | MVP: compilation 的 `consumerClasspathNamespace` | transform registry | internal |
| `consumerClasspathNamespace` | compile/runtime classpath 允许消费的 namespace | MVP compile 默认 `named`，runtime 同 target/compilation 明确配置 | target/compilation contract | internal but explicit |
| `REMAPPED` stage | artifact 是否已经完成 Cloche remap/normalize | `false` raw, `true` remapped | transform attributes | internal |

禁止事项：不复用一个 `remapNamespace` attribute 同时表达声明、source identity、output 请求；不把用户 `sourceNamespace` 写入 `ModuleDependency.attributes`；不把 `target.modRemapNamespace` 当成 output namespace。

## 3. 用户 DSL

```kotlin
dependencies {
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.100.0+1.21")
    modImplementation("com.example:legacy-srg-mod:1.0", sourceNamespace = "srg")
}
```

同族 collector 均保持一致：`modApi`、`modCompileOnly`、`modRuntimeOnly`、`modLocalImplementation`、`modLocalRuntime`、`modCompileOnlyApi`。

默认规则：未传 `sourceNamespace` 时使用 target/compilation 的 default source namespace；`sourceNamespace = "none"` 表示不需要字节码 remap 但仍进入 mod pipeline；`sourceNamespace = "named"` 表示显式 no-op normalize 到 named。

迁移/废弃：`DependencyHandler.remapNamespace(...)`、`ModuleDependency.remapNamespace(...)`、`RemapNamespaceAttribute.ATTRIBUTE` 标记 deprecated，替换为 `modImplementation(notation, sourceNamespace = namespace)`。兼容期内 router 可读取旧 attribute，但读取后必须转换为 `declaredSourceNamespace` 并剥离；breaking 版本移除。

## 4. 内部结构

建议新增 internal remap 子域，先包裹现有逻辑，再逐步迁移：

```text
earth.terrarium.cloche.remap
  NamespaceModel.kt
  ModDependencyDeclaration.kt
  ModDependencyRouter.kt
  RemapAttributes.kt
  RemapTransformRegistry.kt
  ResolvedModArtifactIndex.kt
  ClasspathNormalizer.kt
  RemapDiagnostics.kt
```

关键数据对象：`NamespaceName`、`ConsumerClasspathNamespace`、`ModDependencyDeclaration`、`SourceBucketKey`、`ResolvedModArtifactKey`、`ResolvedModArtifact`、`ReplacementDecision`。

组件职责：

- `ModDependencyDeclaration` 捕获 DSL 调用和 source namespace override；不写 Gradle dependency attributes。
- `ModDependencyRouter` 根据 explicit/default source namespace 放入 per-source bucket；负责 lazy 创建、去重、限制可见性。
- `RemapTransformRegistry` 注册 `(sourceNamespace, consumerClasspathNamespace, target)` 的 raw->output transform；`none` 或 source==output 走 no-op。
- `ResolvedModArtifactIndex` 对每个 source bucket 做 artifact view resolution，收集 raw/remapped artifact key，生成 replacement map。
- `ClasspathNormalizer` 组装 compile/runtime classpath；只输出 consumer namespace artifact、project output、Minecraft artifacts、non-mod artifacts 与允许的 raw initial directories。
- `RemapDiagnostics` 收集声明、bucket、resolution、transform、replacement、最终 classpath。

## 5. 配置与 attribute contract

### Public API attribute

`RemapNamespaceAttribute.ATTRIBUTE` 状态为 deprecated。兼容期仅作为 declaration input compatibility；不得作为 transform-chain source identity；不得用于新 DSL；不得出现在 copied bucket dependency 的 `ModuleDependency.attributes` 中。

### Internal attributes

| Attribute | 类型 | 阶段 | 生产者 | 消费者 | 生命周期 |
| --- | --- | --- | --- | --- | --- |
| `REMAPPED_ATTRIBUTE` | Boolean | raw/remapped stage | bucket/transform/outgoing variant | artifact views/classpath | internal stable |
| `REMAP_SOURCE_NAMESPACE_ATTRIBUTE` | String | source identity | bucket/router/transform registry | transform/artifact view/report | internal stable |
| `REMAP_ARTIFACT_NAMESPACE_ATTRIBUTE` | String | current artifact namespace | transform output/raw view | classpath/report | new internal |
| `REMAP_CONSUMER_NAMESPACE_ATTRIBUTE` | String | requested classpath namespace | target/compilation resolvable attrs | transform selection/classpath | new internal |
| `ClocheTargetAttribute.ATTRIBUTE` | String | target containment | target setup | transform/artifact views | internal stable |

若一次改动风险过高，先引入 `artifactNamespace(...)`、`sourceNamespace(...)`、`consumerNamespace(...)` helper 包裹现有 attribute 写入，并禁止业务代码直接写 public attribute。

### Configuration topology

每个 target compilation + classpath kind：

```text
<sourceSet>Mod<Scope>Declaration       // dependencyScope，收集用户 mod* DSL
<sourceSet>ModClasspathAll             // resolvable，用于完整 graph 解析与 diagnostics
<sourceSet>ModSource_<namespace>        // dependencyScope，internal bucket，lazy 创建
<sourceSet>ModSource_<namespace>Files   // resolvable，source bucket artifact view
<sourceSet>NonModClasspath              // resolvable，普通依赖 graph
```

Bucket 约束：不可消费；名称不写入 public docs；仅按需创建；按 `(configuration, dependency identity, sourceNamespace)` 去重；resolution 必须完整解析 transitive graph。

## 6. 数据流

```text
用户 DSL
  -> ModDependencyDeclaration(notation, declaredSourceNamespace?)
  -> Router classify: explicit sourceNamespace ?: target.defaultModSourceNamespace
  -> Internal source bucket
  -> Full Gradle resolution graph per bucket
  -> Raw artifact index keyed by selected component+variant+artifact
  -> Transform request raw(sourceNamespace,currentNamespace,REMAPPED=false) -> output(consumerNamespace,REMAPPED=true)
  -> Remapped artifact index
  -> Replacement map rawKey -> remappedKey/file
  -> ClasspathNormalizer builds compile/runtime classpath in consumerClasspathNamespace
  -> Diagnostics report
```

原则：dependency graph 完整解析；replacement 不按 coordinates 或文件路径判断；classpath normalize 是集合代数；compile/runtime 独立计算但共享 artifact index 与 diagnostics model。

## 7. Replacement identity spec

主键：

```text
ResolvedModArtifactKey = selected component id + selected variant attribute fingerprint + artifact id/display name + artifact type/extension/classifier + source namespace + classpath kind when necessary
```

覆盖场景：external module 使用 selected `ModuleComponentIdentifier`；project/included build 走 project output/remapped outgoing variant；file dependency 退化到 normalized absolute file + timestamp/length 并报告低稳定性；platform/BOM 不进入 artifact replacement；multi-artifact module 每个 artifact 单独建 key；capability/variant substitution 以 selected 后结果为准；transitive mod dependency 通过完整 source bucket graph remap。

Fail 条件：source namespace 无 mapping path；应 remap 但 remapped artifact 缺失；同 raw key 匹配多个 remapped artifact；不同 source namespace 产生同一 consumer artifact 且无法证明内容相同。

## 8. Compile/runtime classpath normalization

新增 target/compilation contract：

```kotlin
internal interface RemapNamespaceConsumerContract {
    val defaultModSourceNamespace: Provider<NamespaceName>
    val consumerClasspathNamespace: Provider<NamespaceName>
}
```

MVP：compile `consumerClasspathNamespace = named`；runtime 默认同 compile，除非 target 明确提供 runtime-specific namespace；`named` 来自 contract provider，不在各函数中直接引用 `MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE`。

Compile/runtime 必须包含 source/project output、normalized Minecraft artifacts、remapped mod dependencies、non-mod dependencies 完整 graph、允许的 raw `none`/initial dirs/files；必须排除已有 remapped replacement 的 raw mod jar、source namespace 非 consumer namespace 且未 remap 的 raw jar、platform/BOM no-artifact node。

## 9. Diagnostics 与 `clocheRemapReport`

新增任务：`clocheRemapReport` 与 `cloche<target><compilation>RemapReport`。

输出：

```text
build/reports/cloche/remap/<target>/<compilation>/report.json
build/reports/cloche/remap/<target>/<compilation>/report.txt
```

Report 包含：target、compilation、consumerClasspathNamespace、declarations、buckets、components、transforms、replacements、compile/runtime classpath kept/replaced/dropped/failed、warnings、errors。

原则：默认不打印 credentials；完整 JSON 写文件；failure 必须包含 dependency display name、source namespace、consumer namespace、component id、artifact identity、建议 DSL 修复。

## 10. MVP 分阶段实施计划

1. **Phase 0 术语与 guardrail**：添加 namespace model/helper；集中 direct attribute 写入；新增测试禁止 DSL 写 `ModuleDependency.attributes`；`named` output 改由 contract provider 提供。
2. **Phase 1 DSL 与 declaration model**：为 `ClocheDependencyHandler` 增加 `modImplementation(notation, sourceNamespace = null)` 同族方法；内部收集 `ModDependencyDeclaration`；废弃/删除 helper。
3. **Phase 2 lazy/dedup bucket router**：抽 `ModDependencyRouter`；bucket lazy 创建与 dedup；尽量去掉 `afterEvaluate` routing，必要时仅兼容 adapter 使用。
4. **Phase 3 transform attributes containment**：引入 internal artifact/current namespace attribute helper；迁移 transform registry；明确 source identity、current artifact namespace、consumer output namespace、REMAPPED stage、target containment。
5. **Phase 4 replacement index 与 classpath normalizer**：建立 `ResolvedModArtifactIndex` 与 `ReplacementDecision`；compile/runtime 均基于 decisions 组装，不再路径 patch filtering。
6. **Phase 5 diagnostics/report**：添加 `clocheRemapReport` 与 per-target report task；失败输出 actionable diagnostics。

## 11. 测试/验证矩阵

| 维度 | 场景 | 期望 |
| --- | --- | --- |
| DSL | `modImplementation(notation)` | 使用 target default source namespace；dependency attributes 无 source namespace。 |
| DSL | `modImplementation(notation, sourceNamespace="intermediary")` | 进入 intermediary bucket；不写 ModuleDependency attributes。 |
| Migration | 旧 `remapNamespace` helper | 若兼容期保留：deprecation warning，router 可读且剥离；若 breaking：编译失败并有迁移说明。 |
| Fabric | default intermediary -> named | compile/runtime classpath 只含 named/remapped artifacts。 |
| Forge | default srg -> named | transform 使用 srg source，output named。 |
| Explicit override | Forge target dependency 指定 intermediary | 此 dependency 使用 intermediary source，其它 dependency 使用 Forge default。 |
| Transitive graph | mod A -> mod B transitive | A、B 都进入 source bucket resolution 并 remap。 |
| External module | dynamic/changing/capability | replacement key 使用 selected component，不用 requested coordinates。 |
| Project dependency | local project mod | 使用 project output/remapped outgoing variant，不当 external jar remap。 |
| Included build | included mod project | 按 project component 分支处理。 |
| File dependency | `files("libs/foo.jar")` | 可 remap或报告 unsupported；report 标明 file identity fallback。 |
| Platform/BOM | `platform(...)` in mod scope | 不进入 artifact replacement；只作为约束/诊断记录。 |
| Multi-artifact | classifier/dev jar | 按 artifact identity 单独替换，不串 artifact。 |
| Classpath | KotlinCompile libraries | 无 raw intermediary/srg Minecraft owner 泄漏。 |
| Runtime | run config classpath | 与 compile 同样 normalized。 |
| Diagnostics | `clocheRemapReport` | 输出 declarations/buckets/components/transforms/replacements/classpath。 |
| Failure | missing mapping namespace | 报 source/consumer/component/artifact 和修复建议。 |

建议测试：`RemapNamespaceDslTests`、`RemapReplacementIdentityTests`、`RemapClasspathNormalizationTests`、`ClocheRemapReportTests`，并扩展现有 `RemapNamespaceDependencyTests.kt`。

## 12. 风险与取舍

风险：`DependencyCollector` 自定义 metadata 能力有限；Gradle variant artifact fingerprint API 可能不足；一次性替换 sanitizer 风险高；file dependency identity 天然弱。

取舍：保持 DSL 简单，牺牲 Gradle 原生 attribute declaration 灵活性；保留 internal bucket 降低迁移成本；selected identity 复杂但覆盖 substitution/dynamic/capability/multi-artifact；显式 `consumerClasspathNamespace` 减少魔法常量并支持未来非 named consumer。

## 13. 非目标

不设计 `modImplementationIntermediary`；不把 namespace override 建模为 public dependency attribute；不承诺 bucket/internal attribute 名称为用户 API；MVP 不支持多个 consumer output namespace 同时进入同一 compilation classpath。
