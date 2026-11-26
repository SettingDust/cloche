# Context

[Work](work.md) · [Plan](plan.md) · [Waves](waves.md)

## Authority 评估

沿用此前已采用的 authority 评估；下列 update 是此前完成的对齐。本次完整读取当前 Cloche Spec 及独立验收报告，不重新声称读取两方全部 authority 或执行动态验收。

| family | owner / 处理 | 路径与依据 |
| --- | --- | --- |
| PRD | Cloche reuse | [产品需求](../../prd/mixin-preapplication.md)：编译预览、四消费者、精确来源、runtime/发布隔离均未改变。 |
| PRD | Codev reuse | [产品需求](../../../../minecraft-codev/docs/prd/codev-mixin-rework.md)：Codev 执行精确清理，调用方组合和消费，原输入保护及失败传播已有覆盖。 |
| ADR | Codev reuse | [mixin-operation-isolation](../../../../minecraft-codev/docs/adr/mixin-operation-isolation.md)：沿用现有 operation、Task/Transform、独立完成和注册分工，不增隔离或事务架构。 |
| ADR | Cloche skip | 本轮是既有统一入口的数据输入修正及采用 Codev 接口，无新独立架构决策，不新建 ADR。 |
| Spec | Cloche update 已落实；本 Plan reuse 当前版本 | [owning Spec](../../spec/mixin-preapplication.md)：完整原位置判 S 一次后委托完整 action、通用 Info、原运行来源、私有开发编译来源及 V1–V7 已明确。 |
| Spec | Codev 英/中 update 已落实；本 Plan reuse 当前版本 | [English](../../../../minecraft-codev/docs/spec/mixin-preapplication.md) / [中文](../../../../minecraft-codev/docs/spec/mixin-preapplication.zh-CN.md)：有序多文件 `removalsFile`、首次 File 去重、指纹、调用迁移及完整生命周期语义一致。 |

没有发现需要本准备新增或修改 authority 的冲突；这些判断不证明实现通过。

## 当前完成事实与证据

独立计划复核无阻断通过，用户随后确认披露范围，授权生产实施及同范围图/完成记录维护。Router 已读取、关联并采用 A/B/C/V 原始 Results，判定 G-API、G-CALLERS、G-PLATFORM、G-ACCEPT 满足；本记录不重新采用或自行释放 gate。

- A completed：Codev 的 removalsFile 为有序 ConfigurableFileCollection，单记录 caller 已迁移；完整安全生命周期及真实顺序/内容 Transform 指纹有适用证据。
- B completed：[MixinPreapplication.kt](../../../src/main/kotlin/earth/terrarium/cloche/target/compilation/MixinPreapplication.kt) 在原位置只判 S 一次，命中单次委托完整 Codev action，游戏直接使用同一 action；旧 StripCompilationMixins 和 recordOrder 已删除。13 个不同测试方法有适用成功证据，八组模型 1067 比较通过，原失败保留。
- C completed：[TargetCompilation.kt](../../../src/main/kotlin/earth/terrarium/cloche/target/compilation/TargetCompilation.kt) 及平台构造使用有序 named map、primary Provider、runtime 原料 map 和有值 extra Provider，删除 Fabric 强转及 null 注册信号；原 compilation/target/main AW、runtime 和 sources 来源保留。11 个不同聚焦方法有适用通过结果。
- V completed：[独立验收报告](../../../build/tmp/v-accept-20261105/REPORT.md)核查合并后对象，V1–V7 均 supported、无阻断或必需缺证。15 个聚焦方法全部通过，30 个 fixture、199 项复核、当前模型 1067 比较通过。恢复时 265 项连续性核对后仅补 side/sourceNamespace/context 原生输入失效缺口，没有因重启重复整批测试。原 generation 1 为 403 insufficient balance / child_exited_without_result，未提交 Handler Result；generation 2 是同一 V 的唯一可采用终态。

验收报告是项目内最小证据入口，内含原始 XML、日志、模型、C 作者证据、Codev 组件证据与来源核对导航；作者检查不等同 V。本次只核对文档及必要引用，不重跑报告中的测试。私有 init 在输入快照前按模块 identity 去旧加新，Kotlin/KSP/Java/Test、metadata、JavaCompiler、TestKit 与 Tooling 同源的实际证据由 V 报告说明，不能仅凭 fixture metadata 或文件名推断。

## 七条追加评论的处理

这些维护在原 A/B/C/V 图之外，不增加 item、依赖或 gate；V 已核查其合并后的适用性。

| 评论 | 已采用的处理及边界 |
| --- | --- |
| 1 | 删除两项 fixture 属性及 installation/子 JVM 覆盖，普通 Java/Kotlin on/off/direct/file 与 include 真实路线通过。首次环境迁移仍 partial，独立 helper 曾被 generated TestKit 布局阻断；诊断及 Spec 修订后，沿默认 Runner 同类空间 CurrentGradleInstallation/beacon 定位安装，修复 helper/JDK junction 比较。真实 syncToolingOn、namespace 故障阻断及撤故障恢复通过。见[Tooling 恢复报告](../../../build/tmp/tooling-recovery-20261105-a/REPORT.md)。 |
| 2 | Linkage 三处项目依赖声明提取局部 helper；保留 scope/test/WITHOUT_DATA/完整惰性 capability，编译及实际 identity 路线通过。 |
| 3 | 保留 TargetHandler 角色属性对齐/raw 选择，避免 classifier 误筛和回接环。 |
| 4 | 旧 extra 由有效游戏集合承接；只恢复 off 的 runtime extra→primary 顺序，on clean client→common 和 SourceSet 顺序不变。[真实锁定 stub 正反例报告](../../../build/tmp/stub-order-restoration/REPORT.md)支持该限定结论，不保证任意重复类兼容。 |
| 5、7 | C 的数据输入迁移已删除私有 Fabric 强转和 finalCommonJar=null 注册信号。 |
| 6 | compilation 显式 AW 已迁入统一注册，来源与实际 producer 回归通过，不恢复重复旧行。 |

## 环境、发布与未验证边界

当前限定成功适用于 Windows、Gradle 9.5.1、Microsoft OpenJDK 17.0.19 和指定冻结本地新 API（JAR 文件名仍含 0.1.1）。实际来源见 V 报告，不另建永久 hash ledger。测试环境合同见 [Spec](../../spec/mixin-preapplication.md#测试运行环境来源)：普通 Runner 采用默认 TestKit/worker 来源，helper 由 worker java.home 启动，沿 Runner 同类空间 locator/beacon 定位，helper Connector 核对并绑定实际安装；generated TestKit 父目录不是安装根，也不固定 lib/plugins 布局。Gradle internal 定位机制升级维护及 getter ABI 改变须重编译的风险保留。

正式依赖接入、最低 9.4 完整动态矩阵、其他安装布局、最终 IDE UI/模块/输出根/符号、原生 data/test archive/index、条件性 IDE 下游合库及远程 build-cache 未验证。workspace / UP-TO-DATE 复用不等于 FROM-CACHE。限定结论是“本地未发布修正版下的开发功能验收完成”，不能扩展为全产品全绿或正式制品可用。

另行 Git 请求已将 Codev Mixins/Fabric/Forge 0.1.2 与 18 文件签名提交 951143cdbb24c357d9bd21e9e4a7eac49e0f0954 并推送 SettingDust/minecraft-codev 的 g1/mixin-rework；未合 main、正式 Maven 发布或升级 Cloche。该独立任务的 77 测试为 74 通过、2 Forge 依赖超时、1 跳过；9.4 发布准备被 Javadoc zh/CRLF vs en/LF immutable guard 拒绝。JVM Multiplatform 已另行合并推送发布；这些事实均非本 tuple gate，也不改变本轮冻结来源结论。

## 历史效果与不可重放边界

[旧 Context](../mixin-compilation-transformations/context.md)保留原采用事实：旧交付不重开；CT2 原 Result 仍 partial，已产生 P/N、AW 和部分输出，对照曾改写 observation。不得重放旧 11 tracked 恢复/129 文件删除，不扫描追补未知 2 文件/9057 bytes；未知不等于无效果，后续涉及这些效果的安全判断仍需授权和原始证据。旧 partial/blocked/failed 不追溯改为 completed。本次未重验旧报告、执行清理或修改原始证据。
