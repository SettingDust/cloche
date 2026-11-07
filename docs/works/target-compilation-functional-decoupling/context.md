# 处理后的交付 Context

[Work](work.md) · [Plan](plan.md) · [Waves](waves.md)

这里只保留会影响后续选择、验证或安全重试的事实。源码观察来自准备时工作树；未运行代码，不把历史成功推广到候选。术语见[领域 Context](../../../CONTEXT.md)。

## 已接受模型与现实现

函数式方案及 `GameShards<T>` 的 `Joined(value)` / `Split(common, client)` 已接受；具体类型/函数名仍可调整。named、awGames、strippedGames 和内部 Mixin TaskProvider 保形，游戏只在消费边界按用途惰性投影。SERVER 保 Split 静态注册而仅 common 有效；CLIENT 查找/编译 client→common，operations/records common→client。primary 与 side 独立，未分割 main 为 CLIENT+COMMON；形状/primary 不匹配失败。AW 保原 primary/other 任务身份、名字、目录及随 Provider 变化的角色映射，runtime 辅助 common 仍独立。句柄形状不能证明阶段字节、producer 或执行。

本次定点读取 [TargetCompilation](../../../src/main/kotlin/earth/terrarium/cloche/target/compilation/TargetCompilation.kt)、[MixinPreapplication](../../../src/main/kotlin/earth/terrarium/cloche/target/compilation/MixinPreapplication.kt)、[Fabric Info](../../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricCompilationImpl.kt)、[joined Info](../../../src/main/kotlin/earth/terrarium/cloche/target/forge/ForgeCompilationImpl.kt) 及 [client secondary](../../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricClientSecondarySourceSets.kt)：共享 `TargetCompilationInfo` 仍暴露 named Map 与 `primaryMinecraftPart: Provider<String>`，Fabric 构造 common/client Map，joined 构造 joined Map；旧转换/preview 分别依赖键、mapValues 和 values 次序，client secondary 也按键读取。故不能先删除共享旧构造/getters 再等未迁移平台补齐，也不能把擦除后同 descriptor 的 String 值换成 enum。

迁移先增加纯数据形状及共享 Info 单向边界，保未迁移 joined 与旧转换继续消费；再迁平台和各机制，最终候选新链不逐阶段往返 Map。旧 named Map 只容许恰为 joined 或 common+client；未知标签、空/缺/多/混合键及 primary 不匹配必须失败。旧 String/getter 与必要 JVM 入口保原值协议/owner/descriptor，投影同一结果，不再注册。具体分阶段责任见 Plan/Waves；不单立类型定义或兼容研究项。

## 当前连接与迁移依据

| 当前正文 | 对后续的约束 / 结论 |
| --- | --- |
| [TargetCompilation](../../../src/main/kotlin/earth/terrarium/cloche/target/compilation/TargetCompilation.kt) 的 role/unmapped、setupFiles、FinalJarTasks、remap/include、init | 全量转换入口的生产调用在 setupFiles；保留 protected 属性和五字段 data class/getters。完整 unmapped 分别从 C/R 派生 raw+REMAPPED=false，角色按主/辅助图 `(id, variant)` 交集且 builtBy 原 files；不能用角色并集代完整 C/R。新旧出口可共用构造所得结果，无须第二次注册。 |
| [MixinPreapplication](../../../src/main/kotlin/earth/terrarium/cloche/target/compilation/MixinPreapplication.kt) | 当前 receiver 读取 target/Info/角色；S 选择、records、Strip、C.withDependencies 和 afterEvaluate 有紧密联系。已有操作条件依赖，但多个消费者重读开关；新 preparation 要集中完整有效 C files、游戏和条件 operation。 |
| [CompilationInternal](../../../src/main/kotlin/earth/terrarium/cloche/target/compilation/CompilationInternal.kt) 与 [TargetHandler](../../../src/main/kotlin/earth/terrarium/cloche/target/TargetHandler.kt) | configureSourceSet 把目录、sync、classifier 混在一处，已有可保顺序的切口；Handler 顺序为 feature→SourceSet/sync→buckets→metadata/copyMixins/AW/Jar→Java/Kotlin→outgoing/attributes。四辅助配置仍需要；view 不能在 buckets 创建前解析。 |
| [FabricTargetImpl](../../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricTargetImpl.kt)、[client secondary](../../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricClientSecondarySourceSets.kt)、[LazyConfigurableInternal](../../../src/main/kotlin/earth/terrarium/cloche/target/LazyConfigurableInternal.kt) | 原料句柄能与原 init/construct 装配分开；独立 client/includedClient 库对齐、继承、secondary listeners 和 metadata 在返回前完成。发布→listeners→action 与已存在立即路径不可互换。 |
| [ForgeLikeTargetImpl](../../../src/main/kotlin/earth/terrarium/cloche/target/forge/ForgeLikeTargetImpl.kt) | output 惰性 zip minecraftVersion/loaderVersion；implementation 的条件 clientExtra 与 legacy 的直接 generateClientExtra 有不同用途。库/patches/mappings 仍原 init，secondary client-extra 仍返回前。 |
| [CommonTargetCreator](../../../src/main/kotlin/earth/terrarium/cloche/target/common/CommonTargetCreator.kt)、[Fabric run](../../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricRunConfigurations.kt)、[Linkage](../../../src/main/kotlin/earth/terrarium/cloche/Linkage.kt) | stub 当前分别组装游戏/artifact 投影和 producer；可以先选择参与者再统一投影。Common client:data 缺 client secondary 时回退 client，run 回退 target.data。Linkage 成组维护 incoming/include/AW/Mixins/outgoing 与 test/data 例外，保留即可，不以全局名义强改。 |

当前源码仍有 `name.startsWith("client")` 推导 preapplicationSide；全局合同要求由装配显式提供家族事实。本交付需迁移这处输入，不能把旧名字推导保留成最终实现。primary 仍是独立 Provider，不能因移走该判断改变 runtime 或 sources 选片。

最新两 Spec 已实际采用有限形状、唯一 setupFiles 及单向 Map/String 边界，Mixin 的职责、图和 V3 同向。ADR 的模型决定已接受，但尾部仍有“后续 owner 承接”、提案 Draft/旧 HTML 的历史协调句；提案也留有旧同步状态说明。这些不覆盖当前两 Spec 正文，不构成未决架构选择。本次仅以 ADR 决定和两 Spec 当前合同规划，不扩写这些只读文档，也不凭 inline 的 HTML 同步事实宣称本次读过 HTML。

## 可用验证设施与来源边界

[MixinCompilationTests](../../../src/test/kotlin/earth/terrarium/cloche/MixinCompilationTests.kt) 有既有真实 Java/Kotlin、sources、独立 native view、身份、平台 AW、Common、sync/Tooling 入口（沿用前次阅读事实）；本次读取 [platform fixture](../../../integration-test/mixin-compilation/platform.gradle) 与 [sides fixture](../../../integration-test/mixin-compilation/sides.gradle) 的相关正文，确认其实际观察旧 primary 字符串/Map 键、sources input/libraries、runtime AW 与 producer；[identity fixture](../../../integration-test/mixin-compilation/identities.gradle) 保留 single/multi-target、先 client/后 client、项目角色/capability 方法基础。未来扩展形状、primary 切换/不匹配失败、静态/有效任务数量与两种次序时须保旧协议断言，不能只改期望。配置观察可检查注册身份及惰性映射，实际阶段、首产和失败另由真实消费证明；构造中间时序、joined loader 后配、完整 preparation 冷关闭及代表变化面仍需针对性观察。

已读 [构建任务](../../../build.gradle.kts)：toolchain 与专用 Test launcher 都是 17。已读 [Tooling helper](../../../integration-test/mixin-compilation/R4Tooling.java) 及 test 的 runSyncTooling/prepare：默认 Runner 类空间 locator/beacon、worker JDK 工具、Connector lib 安装根、BuildEnvironment、同请求 `model(...).forTasks(":clocheIdeSync")`、故障时 builder 不出现/撤故障恢复都有实际接线。prepare 核对 getter 与加载类来源，fixture 复制同一 metadata。版本相关 internal locator 不是任意 Gradle 支持保证。

Handoff 提供的只读 readiness 已核实本地 Gradle 9.5.1/JDK17.0.19、私有缓存及冻结 Codev JAR 可供开发验收；本次未再次验证其字节或运行。主 wrapper 9.8.2 与最低 9.4 不由它证明。后继每次开始前检查实际安装/JDK、唯一模块 identity/字节、编译/KSP/Java/Test/metadata 和运行 code source；缺失则报告具体条件，不下载、不静默换发布 JAR。

已读历史 invocation-local init 的替换方法：按模块 identity 识别旧 JAR，独立 saved collection 保其它输入/顺序/builtBy，在各实际输入及 metadata 中替换唯一条目。它硬编码历史输出目录，不能原样运行。后继必须新私有 invocation，读取当前输入后构造本次脚本/metadata/日志位置；不覆盖旧 runner/ready marker、旧日志或共享模块缓存。这里保留方法，不把 build/tmp 报告或历史 digest 当永久基线。

验收使用显式 `--no-configuration-cache` 的本地模式；不修改产品默认 configuration-cache 配置、不宣称缓存兼容。新私有 Home 可只复制已核实依赖缓存，不复制 Transform workspace 或预应用输出/records；需要新进程的检查必须保持这一隔离。

## 本次规划修订的处理事实

已采用的旧计划独立复核，其唯一证据责任阻断已由前次修订解决：改变连接的作者在自身终态前区分沿用/本项覆盖/需补并实际补足；组合承接机制中间态，Common 承担全部 G5，sync 验证自身物化/Tooling 并汇合最终适用性，gate 只判断已有作者证据，独立接受不倒填前 gate。此约束继续有效，旧 review 继续适用于未变部分。

本次修订同一初始四文档：补形状贯穿、共享 Info 保留未迁移消费者的可编译中间态、AW/preview/sources 的惰性保形配对、最终旧协议/签名/无二次注册证据；Common/sync 只消费已选有序游戏与 preparation，不学习 GameShards。按实际边界重检后仍为 11 items、2 Waves、2 gates，无新增等待或执行。本轮独立复核已覆盖 Joined/Split 迁移 delta 及必要连接：共享 Info 中间态、窄机制→setupFiles 切口、新模型职责/兼容观察、证据链/责任及现 fixture 方法均有依据，无 blocking 或需先纠正的 nonblocking finding。复核只读解析确认 11 items/2 Waves/2 gates 联合无环、成员恰一次、互链一致及 29 个唯一本地链接可读；Router 结合旧 review 未变范围已判断计划 ready。该静态结论不证明动态无环、实际 ABI 或 HTML 渲染，未运行构建/测试/实验/环境复验；首产、后配、Transform、四消费者及 Tooling/兼容仍需未来候选证据，不构成 C4；所有 item/gate 未执行/未判定。未重验动态环境或历史运行，证据责任/方法以 Plan/Waves 为准。

## 保留的限制

当前有用户未提交生产、测试、构建及文档修改，后继以当次工作树为输入，禁止 reset/覆盖恢复。历史 15 tests/0 failures 只说明旧设施曾执行；不继承旧 tuple/gates，不重新采用历史 Results。正式 Codev 发布接入、角色算法精度、完整外部 ABI、配置缓存/性能、最低版本全矩阵和最终 IDE 都未由本准备解决。

未来发生注册/listener 或测试故障时可能已有部分注册、P/N 或私有输出；报告已知/未知效果和本次输出位置，不能将未知写成未发生、清理历史目录后重试或恢复终态 Result。只在原 outcome 授权内修复明确可重试问题，出现 ADR 重访条件或需改变语义时停止受影响承诺。
