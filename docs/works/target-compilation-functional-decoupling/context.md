# 处理后的交付 Context

[Work](work.md) · [Plan](plan.md) · [Waves](waves.md)

这里只保留后续选择、证据复用或安全恢复需要的事实。原迁移与旧 bounded acceptance 保持历史；当前清理已经发生且 terminal Result 为 partial，本次仅修订派生安排，未运行或重新验证历史代码/输出。术语见[领域 Context](../../../CONTEXT.md)。

## 原候选已采用事实与限制

十项实现的完整 Results 已采用，保留其完成事实；不建立逐项永久身份链，不以本文替代新 Result 首次完整读取和可靠关联。最新 I-SYNC 自身八测试无失败；后续适用性恢复已汇合九项沿用范围、最终 sync 覆盖及失效断言，重新检查七个模型为 721 checks/0 failed。I-V67-HARNESS 的 completed Result 已采用，补充最终候选 V6/V7 的连续 context/source/location/AW/side/mapping 变化、失败恢复、records 顺序清理、输入保护及不同 PID 观察。新的独立 I-ACCEPT Result 也已采用；Router 已在指定本地边界内判定 `G-STRUCTURE` 与 `G-ACCEPT` 满足。旧独立接受虽完成 1118 项模型检查仍返回 blocked，原因是当时最终作者适用性汇合缺失；该旧 verdict 不追改。

采用的证据入口为 [V67 报告](../../../build/tmp/v67-recovery-20261108-d/REPORT.md)、[V67 observations](../../../build/tmp/v67-recovery-20261108-d/checked-observations.json)、[V67 测试 XML](../../../build/tmp/v67-recovery-20261108-d/results/verified/mixinCompilationTest/TEST-earth.terrarium.cloche.MixinCompilationTests.xml)、[I-SYNC checks](../../../build/tmp/i-sync-evidence-recovery-b89893880337/model-checks/checks.json)、[独立接受 model comparison](../../../build/tmp/i-accept-review-85b722068e/model-comparison/checks.json)、[独立接受 review evidence](../../../build/tmp/i-accept-review-85b722068e/review-evidence.json) 与 [独立接受 finish check](../../../build/tmp/i-accept-review-85b722068e/finish-check.json)。这些是已验证存在的项目内证据入口，不替代原始 Result 的首次完整读取或永久归档。bounded acceptance 只覆盖当时指定 dirty 候选、Windows、Gradle 9.5.1、Microsoft JDK 17.0.19、offline、关闭 configuration cache 及本地未发布 Codev Mixins；不证明正式依赖发布、完整外部 ABI、最低 Gradle/其它平台与版本、远程缓存、性能、最终 IDE、完整引擎/平台笛卡尔矩阵或正式发布二进制。

保留旧 sync 恢复 blocked、V6/V7 闭合 partial、阶段红测/failed 及旧 I-ACCEPT blocked 事实；混合批次只沿用具体通过方法，不改称整批通过。I-V67-HARNESS 与新的 I-ACCEPT 是后续独立结果，不覆盖上述历史，也不把 bounded acceptance 扩成正式发布或完整矩阵。早期编译接线修复最终无净生产变更，不替代动态验收。已有生产迁移、构建输出、私有 fixture 和报告不得重放/覆盖；历史失败可能留下注册、P/N 或部分输出，未查明的效果仍未知，不能当作未发生或已清理。必要重试先检查受影响位置与进程，使用新私有输出；旧终态 Result 不恢复、不重写。

完整可行性调查支持有限复用 Codev 的零/空记录、内容/顺序指纹、输入保护及新进程组件观察，以及 Cloche remap/refmap/direct、四消费者故障恢复入口；该调查本身不证明最终候选闭合，闭合事实来自 I-V67-HARNESS 与独立 I-ACCEPT 的有界 Result。context 输入须为兼容的 refmap-only/chosen 语义，missing 失败后恢复，不能给混合 no-refmap 默认输入强加 context。历史 mapping 模式实际改变 sourceNamespace，不证明 mapping 文件内容变化。旧状态脚本硬编码历史目录并写回，禁止原样重跑。

具体恢复谓词/方法/冲突见 [Waves](waves.md) 的 I-V67-HARNESS。最终关联须区分组件语义、历史运行及当前集成观察；不能从新 Home 推断新进程，或将 workspace 复用写成 FROM-CACHE。零份记录失败时旧输出存在不等于消费回退成功。最新 standalone sources=true 仅 common-only，不推广为 Split CLIENT 该特定组合已新跑，也不要求全笛卡尔矩阵。

用户先前的仅验收 harness 与图维护授权已产出 I-V67-HARNESS 和新的 I-ACCEPT，G-STRUCTURE/G-ACCEPT 的历史判定保持原边界。随后最新确认已扩大本次清理的代码/测试迁移范围并授权 authority 同步和同 tuple 维护；不得沿用原“只许验收、不许生产修改”的旧恢复范围来阻止已确认的新清理，也不得把新权限推广到正式依赖、Codev、发布或其它语义。

## 最新确认与当前增量起点

Router 已可靠采用的兼容审查指出：没有证据表明人工 synthetic accessor 或旧 Info Map/String/preview adapters 属于产品公开合同；Legacy fixture 自身不能证明必要性。用户已获知删除影响并明确取消指定内部兼容支持。最新 [ADR](../../adr/target-compilation-functional-composition.md)、[全局 Spec 第 8 节/G3](../../spec/target-compilation-architecture.md)、[Mixin Spec 统一职责/V3](../../spec/mixin-preapplication.md) 已同步；本次读取当前正文，不重新调查兼容必要性，也不编辑 authority。

已采用的 address-review-compatibility/g1 内层 Result 仍为 failed：只删除 TargetCompilation.kt 的旧 getUnmappedClasspath(String) 及 accessor，另三处当时误因 Legacy 测试而保留。该已发生删除不得恢复或盲重放。默认 ./gradlew :test 在未替换的发布 Codev classpath 下两处 .from 未解析，未进入测试主体；不是新源码缺陷的证据，也没有产生新候选测试通过结论。旧失败可能有构建输出，沿原不覆盖/不重放边界处理，不将未知效果写成已清理。兼容必要性审查与 authority 修订的已采用 completed 只支持其各自范围，不证明清理已完成。

`cleanup-internal-compatibility/g1` 的完整 Result 已由 Router 可靠采用，内层仍为 partial，不得 resume 或追改。指定手写 accessor、GameShards Map/String、共享/平台旧 Info、preview adapter 及专用 Legacy caller 已删除；真实 typed 调用与混合行为断言已迁移，先前 getUnmappedClasspath(String) 删除保留。原 Result 报告生产/测试 Kotlin 与 typed 单测成功、另有 11 个通过用例；consumers 批 8 中 7 过 1 失败，`sides.gradle` 当时错比 libraries 的 Provider 包装层，已修观察方式，仍保 CLIENT 底层对象身份及其它情况为空的断言。不能把这次 fixture 修正说成生产 libraries 或两处 `.from` 的修复，也不能把混合批称整批通过。

恢复现场为[原私有目录](../../../build/tmp/compat-cleanup-20261108-a/)，其中[最后启动脚本](../../../build/tmp/compat-cleanup-20261108-a/run.ps1)、[native 日志](../../../build/tmp/compat-cleanup-20261108-a/native.log)、[结果目录](../../../build/tmp/compat-cleanup-20261108-a/results/)、[候选快照](../../../build/tmp/compat-cleanup-20261108-a/candidate-current-sha256.json)、[metadata](../../../build/tmp/compat-cleanup-20261108-a/metadata/)、[保留 ABI 观察](../../../build/tmp/compat-cleanup-20261108-a/final-jar-tasks.javap) 与[待检查汇总脚本](../../../build/tmp/compat-cleanup-20261108-a/verify-results.py) 均在本次静态检查中确认存在可读，未执行或核实其内容/结论。最后确认启动的是 `run.ps1 -Run native`，原 Result 时仍在输出；Router 后续只读见 native.log 的 `BUILD SUCCESSFUL in 7m54s`，但完整 XML、退出码、候选/metadata/ABI 适用性尚未汇合，不能当作清理通过。原记录 PID 302296 后来不在，git.exe 归属未证，不能推断所有相关写入都结束。verification.json 与最终报告没有完成证据，不把未来产物写成现存入口。

恢复先 fresh 检查与本候选/输出有关的写入和已发生效果，结算已有日志/XML及候选/输入关联，之后只补真正缺失或失效的检查；禁止盲重跑 native 或成功批次、覆盖旧输出、原样运行旧状态脚本。若无法确认运行终止/关联或来源适用性，阻断该受影响结算或重试，不扩大成全机器无进程证明。脚本存在不代表已执行或可原样安全使用；旧快照只用于本次恢复比对，不建立永久 hash ledger。

清理保留公开 DSL、FinalJarTasks 五字段/生成接口与明确保留 getters、protected setupFiles、原 Gradle 身份与 producer/构造监听时序、四消费者、records/输入保护、runtime/raw/clean 隔离。只取消指定旧内部 accessor/Map/String/Info/preview 接缝及其专用兼容 fixture；GamePart 的 joined/common/client 值仍用于真实任务名、路径和 operation/type key，legacyName 可改名但值不变，Forge legacy 等其它协议不受此删除授权影响。

当前安排的三个独立终态是：未完成清理的安全恢复、保持覆盖的正常测试集中、最终候选的独立只读修正审查。旧清理 partial 不参与新的必需成员等待，新恢复结果显式承接必要清理谓词和 gate；原 12 项及历史两 gate 不重开。最终审查等待清理恢复和测试集中作者证据，不能代作者补缺证。四文档作者准备/自检已完成，Router 已采用后续独立 review-delivery-plan completed、无阻断结论；用户经完整 scope 披露已明确确认测试集中实施及定向验证，原清理确认和 C5 同 tuple 图维护授权持续有效。首次因作者回执截断而 blocked 的复核保持历史，后续新独立复核完整消费作者事实后通过；不将文档准备或确认当作实施完成，三个增量均未开始/完成，新 gates 均未判定。

旧状态登记两代均 child_exit_1、无 Handler Result 且未写四文档；恢复请求返回 result_projection_failed，元数据仍为原 failed/g2、无新代次记录。这些启动失败不代表图内实现完成，本次不处理运行时。

## 正常测试集中采用依据与边界

Router 已完整读取并可靠关联 `compact-test-matrix/g1` 与 `assess-test-consolidation-boundary/g1` 的 completed Results；调查针对当前 dirty 树、HEAD `985c4a1`，本次复核 HEAD 一致，未重做全面源码盘点。PRD/ADR/两 Spec 均 reuse；生产架构、依赖与 DSL 不变。现有 `mixinCompilationTest` 外层入口及[根 fixture](../../../integration-test/mixin-compilation/) 的根 + `:populated` + `:empty` 模板可承载多个命名 target/probe；当前重复成本来自多次复制 ProjectDir/Home、prepare 及内层 GradleRunner/Tooling 调用，并非每个 loader/SourceSet 必须独立 build/Home。

拟在同一普通 fixture 内共享一次 prepare 与 ProjectDir/Home，用具名 probe、fixture 局部 `verifyMatrix` 或普通多任务调用聚合相容的正常装配、真实编译、sources 内容、classifier 和普通 sync 观察。不是只合并 JUnit 方法却保留所有重复 prepare/build，不创建通用 mode 框架。具体保持条件/排除与作者证据见 Plan；真实 Tooling 仍独立验证同请求 `forTasks(sync)` 先准备→builders→strict views，普通 sync 不能替代它。

raw-only 与 AW/preapply/compile 不同调用；source-only、Common preparation、Joined legacy 及独立 C/view/SourceSet 等 absence 检查保原观察范围。负编译/配置失败、插件早晚和 unnamed 等互斥模式分次；四消费者冷入口各自新实例/无可复用 Transform/P/N；连续故障恢复在同 ProjectDir/Home/旧输出中单变量改变。正常 Home 可共享，不将 invocation 私有目录要求扩成每个断言独立 Home。作者以实际变化判定哪些冷/absence/恢复证据须补，不能删覆盖提速，也不要求全历史矩阵重跑。

`diagnose-cleanup-test-duration/g1` backend failed `child_exit_1`，无可采用 Result；后续 inspect 同状态，旧 session 读取失败，未取得耗时细分，既不当完成也不当无效果证明。旧 `inventory-test-fixture-matrix/g1` 原 Result 被截断未采用，由上述独立 compact 调查按当前源码重建，不引用被截断报告背书。缺旧逐方法耗时基准，测试集中只报告实际 prepare/Gradle/Tooling 次数及耗时采集方法，不伪造提速比例。

原分阶段共享 Info Map/String 接缝只解释已完成迁移；当前清理已移除它，不恢复按名字前缀猜 side 的 fallback。Joined/Split、独立 primary、CLIENT 编译/查找 client→common 与 records common→client、SERVER Split 仅 common 有效、runtime/sources 各自选片等现行保证沿 authority 保留。

## 可用验证设施与来源边界

[MixinCompilationTests](../../../src/test/kotlin/earth/terrarium/cloche/MixinCompilationTests.kt) 与现有 [platform](../../../integration-test/mixin-compilation/platform.gradle)、[sides](../../../integration-test/mixin-compilation/sides.gradle)、[fabric assembly](../../../integration-test/mixin-compilation/fabric-assembly.gradle)、[joined assembly](../../../integration-test/mixin-compilation/joined-assembly.gradle)、[identity](../../../integration-test/mixin-compilation/identities.gradle) 是已采用调查及清理 Result 所指入口，本次未重新盘点。专用旧兼容断言已移除，混合的角色/producer/后配/路径/消费观察已迁 typed，但最终清理证据未闭合；聚合仍须保留这些有效断言和各自的状态寿命，配置观察不代替实际消费/首产/失败检查。

已有调查与历史运行支持：[构建任务](../../../build.gradle.kts) 的 toolchain/专用 Test launcher 为 17；[Tooling helper](../../../integration-test/mixin-compilation/R4Tooling.java) 与 test 的 runSyncTooling/prepare 具备 Runner 类空间 locator/beacon、worker JDK、Connector lib 安装根、BuildEnvironment、同请求先任务后 builder、故障阻断及撤故障恢复接线。prepare 核对 getter/加载来源，fixture 复用同一 metadata。这里采用方法而未在本次重读其正文或运行；internal locator 不保证任意 Gradle 支持。

Handoff 提供的只读 readiness 已核实本地 Gradle 9.5.1/JDK17.0.19、私有缓存及冻结 Codev JAR 可供开发验收；本次未再次验证其字节或运行。主 wrapper 9.8.2 与最低 9.4 不由它证明。后继每次开始前检查实际安装/JDK、唯一模块 identity/字节、编译/KSP/Java/Test/metadata 和运行 code source；缺失则报告具体条件，不下载、不静默换发布 JAR。

已采用历史 invocation-local init 方法按模块 identity 替换唯一旧 JAR，独立 saved collection 保其它输入/顺序/builtBy。旧脚本硬编码原输出，不能原样运行。新运行须用新私有 invocation 编译/metadata/日志位置；已有运行先只读结算，不覆盖其 runner/ready marker/日志/共享模块缓存。正常组在本次私有 invocation 内可共用 ProjectDir/Home；冷入口及连续恢复依断言所需状态寿命隔离或保留旧输出，不为每个断言复制新 Home。冻结来源检查仍适用，不把历史 digest/报告当永久基线。

验收仍显式 `--no-configuration-cache`，不改变产品默认配置或宣称缓存兼容。冷入口可只复制已核实依赖缓存，不能复制 Transform workspace、P/N 或清理输出；不同实际进程须直接观察，不能从新 Home 推定。连续恢复须保留同一 ProjectDir/Home 与旧输出作为被测状态，不能每步重置而丢失恢复保证。

## 初始规划修订与既有复核范围（历史）

已采用的旧计划独立复核，其唯一证据责任阻断已由前次修订解决：改变连接的作者在自身终态前区分沿用/本项覆盖/需补并实际补足；组合承接机制中间态，Common 承担全部 G5，sync 验证自身物化/Tooling 并汇合最终适用性，gate 只判断已有作者证据，独立接受不倒填前 gate。此约束继续有效，旧 review 继续适用于未变部分。

初始准备和原恢复最终形成 12 items、2 Waves/gates；旧独立计划复核支持当时迁移与作者证据责任，其历史结论不证明当前恢复/测试集中增量 ready。当前图保留这些历史归属及旧清理 partial/未释放 gate，另以恢复、正常测试集中、最终独立审查三个边界承接增量；实质谓词/证据/连接变化须由独立计划复核评估，不能套用身份更名式复用。

## 保留的限制

当前有用户未提交生产、测试、构建及文档修改，后继以当次工作树为输入，禁止 reset/覆盖恢复。历史 15 tests/0 failures 只说明旧设施曾执行；不继承旧 tuple/gates，不重新采用历史 Results。正式 Codev 发布接入、角色算法精度、完整外部 ABI、配置缓存/性能、最低版本全矩阵和最终 IDE 都未由本准备解决。

未来发生注册/listener 或测试故障时可能已有部分注册、P/N 或私有输出；报告已知/未知效果和本次输出位置，不能将未知写成未发生、清理历史目录后重试或恢复终态 Result。只在原 outcome 授权内修复明确可重试问题，出现 ADR 重访条件或需改变语义时停止受影响承诺。
