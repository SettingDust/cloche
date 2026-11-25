# Common stub 显式参与平台：实施计划

依据：[架构规格 §6–7](../../spec/target-compilation-architecture.md)。只处理 `shared.stubFrom(forge)`；此次确认计划不代表代码已实施。未获实施授权前不动生产代码和测试，不提交、推送、发布。

## 从同一构建脚本验收

以真实 Kotlin DSL 多 target 工程作对照。平台 A（Fabric）正常共享 `shared` 的源码，Forge 不共享；两个平台使用可区分的 Minecraft 版本。A 的编译依赖有 `Widget.open()` 和 `preview()`；Forge 通过 `compileOnly` 声明同一组件的另一版本，只有 `open()`。断言 Forge 的 Widget 不在其 `implementation` 声明及继承闭包中，但在有效编译路径中；显式参与后，在实际 stub 任务输入的 Forge 分组中核对该组件及版本。

| 操作 | 应观察的结果 |
|---|---|
| 不调用新入口 | 从真实 stub JAR 读到两个方法；A 继续编译和打包 shared 独有类，而 Forge 不包含它。记录两个平台的源码输入及运行时依赖；核对 common 的版本集合仅有 A 的版本、唯一版本为 A 的版本，loader 推导反映 A 而非 Forge。 |
| 只加 `shared.stubFrom(forge)` | 在无旧 stub/必要 producer 输出时直接请求 stub，生成物保留 `open()`、移除 `preview()`。检查真实编译源输入、平台 JAR 与解析后的 runtime 依赖保持基线；common 的版本集合、唯一版本及 loader 属性保持原值，不能混入 Forge 版本或因多 loader 而丢失原属性。另一个未声明的 common 不受影响。 |
| 重复声明 Forge，同时显式声明已共享源码的 A | 实际 stub 任务输入中每个平台仍只有一组，API 与上一步一致；A 的原源码关系继续生效。只比较输出字节不能证明去重。 |
| 已配置的 client、data、test | 用可区分的 API 或映射到具体 compilation 的任务输入核对 §7：有对应 compilation 时选它，无时按原顺序回退；区分独立 client 缺 secondary 与无独立 client，不新建平台 compilation 或另一侧 operation。 |

## 实施顺序与改动边界

1. 一个执行者负责 DSL、内部接线、fixture 和定向运行。先记录仓库基线、Gradle/JDK 与 Codev 来源；复用现有 stub 测试设施，建立上表的默认行为对照。
2. 在公共 Common target API 增加 `stubFrom(platform)`。内部单独保存该 common 显式声明的平台；按平台身份与原源码参与者稳定去重，仅在计算该 common 的 stub 时使用并集。不要修改 `CommonTargetInternal.dependents` 的原有含义，或把新增关系送进 Linkage、版本/loader 推导、发布和运行时。
3. 在 `CommonTargetCreator.kt` 现有 main/client/data/test 选择点接入同一个惰性参与者来源。每个平台继续经同一个 `compilationConsumption` 提供分组 classpath、游戏角色和 preparation；保留 `GenerateStubApi` 任务及正常源码关系。不要改交集算法、展平文件或在配置期解析依赖。
4. 在同一候选上运行上述定向 Gradle 集成测试与受影响的原 common stub 测试；交回命令、环境、候选指纹、生成 JAR 的成员与对照结果。缺少必要依赖时报告具体未测路径，不以 dry-run 或旧结果替代本候选验收。
5. 未参与实现的审查者从 DSL 调用及实际 stub/编译/打包入口核对候选和对照证据，再向用户交付。实现或测试不符合已确认规格，先修同一候选问题并复核；需改变行为或验法则先回到用户确认。此次不扩展其他功能，不做发布或最终 IDE 验收。
