# 整体 target / compilation 函数式解耦 Plan

[Work](work.md) · [Context](context.md) · [唯一匹配 Waves](waves.md)

状态：原 12 项及 G-STRUCTURE/G-ACCEPT 保持旧候选 bounded acceptance。指定内部兼容清理已发生、原 terminal Result 为 partial，不能恢复或追改；四文档作者准备/自检已完成，后续独立计划复核 completed、无阻断结论已由 Router 采用，用户经完整 scope 披露已确认正常测试集中实施与定向验证。未完成清理恢复、正常测试集中和最终独立修正审查均未开始/完成，新 gates 均未判定。已发生效果/失败/未知及现场记录见 [Context](context.md)，唯一执行图见 Waves。

## Authority assessment

| 家族 | 处理与 owner | 依据 |
| --- | --- | --- |
| PRD | reuse：[Mixin PRD](../../prd/mixin-preapplication.md) | 仅沿用默认/关闭、外部 compile mod、四消费者、环境与运行发布隔离；全局内部组织不新增产品能力，不创建全 Cloche PRD。 |
| ADR | reuse：[函数式组合 ADR](../../adr/target-compilation-functional-composition.md) | 同一已接受决定已选 Joined/Split 泛型纯数据、显式输入、原状态、setupFiles 原位；最新同一决定已取消指定内部 accessor/Map/String/Info/preview 支持，明确保留公开出口，接受不等于执行确认。 |
| Spec | reuse：[全局 Spec](../../spec/target-compilation-architecture.md) 与 [Mixin owner](../../spec/mixin-preapplication.md) | 全局第 8 节/G3 已取消指定内部兼容，但保公开出口、身份/producer/时序；第 9 节及 Mixin V1–V7 保四消费者、冷首产/absence/失败恢复/真实 Tooling。Mixin 验证条款允许同 fixture 多条件；本次仅重组相容正常观察，按断言状态寿命保留隔离，无须修改 authority。 |

[Accepted 提案](../../proposals/target-compilation-architecture.md) 只作非权威历史解释。沿用作者准备时采用的测试集中 assessment 及 PRD、ADR、两 Spec 相关保证核对：三家族均 reuse；生产架构、领域、Codev 与发布边界不变。本次只同步 Router 已采用的复核/确认事实，不重新评估 authority；原清理确认和 C5 同 tuple 图维护授权持续有效，测试重组具体 scope 已经完整披露并获用户确认，不由本文新增权限。

## Outcome 与范围

交付保留原行为的全局组织：平台注册只接显式原料，家族仍原位配对；角色/完整视图各守边界；SourceSet 目录、sync、classifier 可独立变化；唯一 setupFiles 直接组合 AW、preview、相关消费输入与 sources；编译/Common/sync 复用准备协议。内部游戏及 Mixin task 角色以泛型纯数据 `Joined(value)` / `Split(common, client)` 保形贯穿阶段，不靠字符串键或插序传协议；类型本身不保证阶段或 producer 已执行。target/main 导航止于家族或出口装配，下层不靠整个 target/Info/compilation 找数据。状态仍在原 DSL/Property/Configuration/SourceSet/Task。

必须保留：Fabric split/included/unsplit、Forge/NeoForge joined，main/data/test 与 client secondary；C/R、SourceSet 原实例/名字/作用域/属性/variant/classifier/capability、classes/resources 和用户追加；feature/test 例外；主辅助多图角色算法与四辅助配置；库/metadata/patches/mappings 和原时序；Linkage/Common 源码关系；Common/run 各自 fallback；runtime/legacy/归档/发布的原输入。

排除：角色算法新修复或删除四配置；公开 DSL/明确保留 ABI 破坏；普通 sources/javadoc 布尔时点、Forge clientTest、Common 环/孤立语义旧缺陷；新增职责对象、总 context、registry、freeze、通用 pipeline、生产汇总 Task 或 scheduler；正式 Codev/依赖/锁/wrapper 变化、引擎矩阵重做、最低版本全矩阵、最终 IDE 实验、CI、Git stage/commit/push。测试集中仅改现有 test driver/fixture 及必要私有运行安排，不变更生产逻辑或承诺产品性能指标。当前只写四文档，不启动 Gradle/TestKit/引擎/旧脚本。

## 具体组织与迁移策略

以下为持续有效的组织约束，原迁移已完成，不按表重放。当前清理已移除指定适配并迁 typed 调用/观察，恢复负责结算及补齐其剩余必要证据；正常测试集中只改变验证组织，不改变各生产 owner、接线、注册时点或 DSL。

| 边界 / owner | 输入、输出与副作用 | 迁移方式与禁止传播的知识 |
| --- | --- | --- |
| Fabric / joined 原料函数 | Project、原名字/目录/namespace/版本与 mapping Providers → 原任务/库/游戏句柄；原位置注册一次 | 在原 Info 构造位置分别输入 Split / Joined named，secondary 复用原句柄；primary 独立 Provider。原分阶段共享 Map/String 接缝已完成其迁移用途；本增量删除指定旧构造/getters，真实调用直接传 typed 形状/primary/家族 side。注册不接 R 或执行 runtime 对齐；joined loaderVersion 与输出文件名保持惰性。 |
| 平台家族装配 | 具体 target/main、原料句柄与明确 client 家族事实 → Info/库/metadata/listeners | 保持原 init/construct；库一致性→localImplementation→secondary 库 listeners→main metadata 完成后 return，再 publish→listeners→action。Info 只供输入；不给下层任意导航权限。 |
| 选择与 buckets | 原 C/R、角色配置 Provider、collectors/isTest/SourceSet → 原惰性 views/继承 | 完整 unmapped C/R 分别 raw+REMAPPED=false；role 主辅助 `(id, variant)` 交集保 builtBy。四配置在 Handler 原位置创建，早建 view 不提前查不存在的 bucket。发布解析用 raw C 的非项目 view 加原项目 view。 |
| setupFiles 组合 | 展开同 compilation 输入、家族 side、primary、enabled → AW、preview、消费结果、sources，最后 FinalJarTasks | 先 preview AW，再本地 runtime AW，再 Mixin/Strip，再 sources；固定拓扑、惰性选择。组合不实现 role、Transform 或反编译内部；不得再转发全量总参数。 |
| AW / preview / sources | AW 以 named+独立 primary 返回保形 awGames；preview 以同形 Mixin tasks 投影 output/records/strippedGames，并集中 S/records/Strip/C state；sources 只收已选 input/libraries/preparation | primary/other 原任务不改名/重注册，由 primary 惰性映射回角色。SERVER 静态 Split、仅 common 有效；CLIENT 查找/编译 client→common，operations/records common→client。preview 两片用自身 AW；runtime common 用 target AW 或独立 client 家族的 main 原 AW。sources primary 独立，不从 side 推导；关闭保原 C 请求、AW/remap。 |
| 消费输入及编译 | 同一有效非项目 C view、已按用途惰性投影的有序游戏、stub 顺序、条件 operations → 无行为小结果/preparation | preparation 保原生 files/Transform producer + 有效 games + 条件 operation。off 直接取空必要 operation，不求值本轮 operations。Java/Kotlin 原位置只 dependsOn preparation；Common/sync 不学习 GameShards/primary/records。投影不枚举任务，不能通过 `.files`/records 发现 producer。 |
| Common stub | 在原 dependents Provider 选择已有参与 compilation，再投影同一消费数据 → 每参与者分组的 artifact/component + preparation | 关闭仍 extra→primary；游戏不伪造 component，不额外创建 secondary、不增加同 target 另一侧 operation。Common 源码 linkage 保持原 TargetApplicator 位置，与 stub 分开。 |
| SourceSet / sync / classifier | 原 SourceSet/目录，已选消费输入，独立 runtime/sources → 原目录、sync 实际文件物化、原 classifier | 可保顺序 wrapper；目录只管 Java/resources/Kotlin 与 plugins.withId，classifier 留非 test 的归档局部配置，sync 不知 classifier。sync 原执行位置物化有效 compile/games 和独立 runtime，可选 sources。 |
| Linkage / run / 发布 | 原关系双方或原 Provider 出口 | Linkage 成组操作保留，不拆成调用者手工排序的步骤；run/Common selector 分开。Jar→RemapJar/原 Jar→IncludeJar、Fabric client 注入/main include 选择与 Forge legacy 差异原样保留，不接 clean/records。 |

实际路径例如独立 Fabric client:data：平台传 named Split 和独立 CLIENT primary，runtime extra 另接 main 原 AW；自身两片 AW→同形 CLIENT tasks→各自独立 records→strippedGames。任务 output 与 records 始终从同一任务投影，records common→client，查找/编译 client→common；惰性选 clean client 供 sources、clean common 作 library，preparation 供四消费者。runtime 仍自身 client AW + main 原 common AW。未分割 main 为 Split+CLIENT+primary COMMON，sources 仍 common、runtime 不新增 common extra；分割 SERVER 仍 Split 静态注册、只消费 common；joined 为 Joined+CLIENT+JOINED。形状/primary 不匹配必须求值失败，不猜主片。关闭时 SourceSet primary→extra、stub extra→primary，各保原顺序；完整 operation 兼容出口不改成条件空列表。

正常 remap 的空 namespace 早退仍只跳过原 remap，不跳过 preview/Strip/sources。原 raw C/R 只供上游，clean 只下行；必要生产/解析/AW/Mixin/Strip/Decompile 失败沿真实 owner 阻断相应消费，无 raw fallback。注册/listener 抛错可能留部分效果，不引入事务或重试。

### 已确认内部兼容清理策略

保留 `FinalJarTasks` 包/类、五字段名称/顺序/类型、data class 生成接口、`libraryArtifact` / `sourcesArtifact`、明确保留的原 compile/runtime/operation getters 和 protected `setupFiles` 原类型；共用唯一生产链。取消的仅为全局 Spec 第 8 节列出的旧内部入口，不把 Kotlin internal 的 JVM 可调用性或 Legacy fixture 当作公开合同，也不声称没有外部旧调用者。

原清理 Result 已报告这些指定入口及专用 Legacy caller 的删除、真实 typed 迁移，`getUnmappedClasspath(String)` 删除也保留；不再计划从头删除或重放成功构建。恢复先读取相关现场/当前 diff/必要 caller 与原始输出，确认原 native 运行是否终止、XML/退出状态与所测候选/metadata/ABI 的关联及 `sides.gradle` 修正后证据。日志成功行仅是线索，不是作者终态；退出码缺失时明确缺口，只有其它完整可关联证据足以证明相应检查才可采用，不能编造退出码。若当前有相关写入或关联不明，停止受影响采用/重试；不得因单 PID 消失推定安全，也不要求全机器无进程。

将已通过方法按实际源码、输入、来源、环境及消费者适用性结算，只补真实缺失/失效部分。原目录仅供读取，旧脚本先审实际输入/输出及效果，不能原样重跑或覆盖；新增观察使用新私有输出。清理恢复完成谓词仍是原指定入口已清除、真实 typed 消费及保留保证闭合且作者证据充分；原 partial/失败/未知历史不改写，恢复结果不自动完成旧 item/gate。

测试按作用迁移：仅证明已取消入口的 Legacy Java/Kotlin caller 或旧非法 Map/String 测试可删除；同一 fixture 中的角色/native producer、平台构造/监听、解析前后配、typed 形状/primary 不匹配、顺序/路径、真实消费与输入保护断言保留并换成 typed 观察。先读所有受影响 caller 与断言，不能以删入口为由整批删测试或只改期望。移除接缝与这些必要迁移/聚焦检查共享同一清理成功谓词、写入与恢复边界，不按文件或函数拆项。

最终独立修正审查等待清理恢复及测试集中后的实际最终候选和完整作者证据，核对整体变更与相接生产/消费保证；不能由任一作者兼任，不能补作者缺证或修改代码。其单独 verdict 和 gates 只在 Waves 定义。

原平台→机制→组合→Common/sync 的独立迁移切口及当时保签名路线已完成，保留原 12 项语义/终态供历史关联，不再作为待执行步骤或当前兼容义务。取消内部支持是已确认的实质变更，不是仅换恢复身份；旧评审只可复用于未变范围。

### 保持覆盖的正常测试集中策略（scope 已确认，尚未实施）

已采用调查显示，重复成本在同一 `mixinCompilationTest` 外层入口中的多次 `prepare`、ProjectDir/Home 复制及内层 GradleRunner/Tooling 调用。[现有测试](../../../src/test/kotlin/earth/terrarium/cloche/MixinCompilationTests.kt) 与[根 fixture](../../../integration-test/mixin-compilation/) 已有根 + `:populated` + `:empty` 模板，可在同一普通配置下承载多个命名 target/probe。拟共享一次 prepare 和同一 ProjectDir/Home，用 fixture 局部具名 probes/`verifyMatrix` 或普通多任务调用，联合检查相容的装配、真实 Java/Kotlin 编译、sources 内容、classifier 与普通 sync；该验证 task 只属测试 fixture，不引入生产汇总 Task。一次调用不保证覆盖所有条件，不增加通用 mode/调度框架。

正常组以未解析前相容配置和各 probe 独立可观察输出为边界，保留完整成员/身份/variant/classifier/producer、注入 API、sources 所选内容和 library、原任务/路径/字符串、原生项目/NO-SOURCE 与 scope 断言。变更 driver 必须真实减少重复准备/内层调用，不能仅把多个 JUnit 方法换成一个、仍逐 case prepare/build。若分成几个相容组，每组明示输入和状态寿命；不强求单次全覆盖。仅减少重复开销，不改 production 或放宽通过条件。

| 必须保留的边界 | 组织和观察规则 |
| --- | --- |
| raw-only / 独立 C、继承 view、SourceSet / source-only / Common preparation / Joined legacy 等 absence | 按原观察范围单独调用；raw-only 不与 AW/preapply/compile 同调用，不能让同场游戏或编译顺带生产来掩盖缺边/不应出现的任务。source-only 不混入 compile，Common preparation 不由其它消费者代调度，Joined legacy 不引入 clean 消费；保留原不应调度或不应出现的具体断言。 |
| 四消费者冷入口、file/Maven 及关闭/后配中需证明首产的条件 | 各入口用新实例/私有新 Home，无旧 Transform workspace、P/N/清理输出，不预跑聚合或 operation；实际源码编译及其余三消费者分别证明实际消费。未变适用证据可复用；只补受本次 driver/fixture 变化影响者。 |
| 负编译/配置失败、互斥 mode、插件早晚/unnamed | 保留分次配置和原因断言，不让一次 build 中不兼容 DSL/失败路径覆盖其它条件；不把任意非零退出当预期失败。 |
| 连续单变量变化/故障恢复 | 同 ProjectDir/Home 及旧输出内 baseline→单变量改变/失败→恢复，保原输入保护、producer outcome、最终消费与无旧结果回退；不能每步新 Home 清掉恢复状态。 |
| 真实 Tooling | 仍由既有 helper 同请求 `forTasks(sync)` 准备→builders→strict views，核对完整身份/SourceSet index/闭包/scope/清理文件；保真实故障阻断及撤故障恢复。普通 sync 成功不能替代 Tooling。helper/metadata 若未变，旧适用证据按实际影响复用。 |

正常 Home 可在本次私有 invocation 中共享；冷/恢复的 Home 寿命按上述语义决定。生产所需同源 Codev、metadata、Runner/worker/helper 来源检查保持，不把私有 invocation 误读为每个正常断言独立 Home。作者直接在现有测试/fixture 与本项 Result 中给出“原有效断言→新 probe/方法/执行或适用沿用”的对应，不新建永久矩阵文档/账本；专用旧兼容断言已取消，真实混合断言不能借机删除。

作者记录实际外层运行、prepare 次数、内层 GradleRunner build/buildAndFail 与 Tooling 请求次数、每组任务参数/输入和可重现入口；计时区分 prepare、各次 Gradle/Tooling 与外层 wall time，说明计时起止、缓存/冷暖及环境。合并前可从清理完成时的 driver/fixture 按同一有效断言范围核对准备/调用结构，区分静态推导与已有实测，再与候选实际次数对照，不强制重跑旧全套取得基准。无旧逐方法耗时基准不宣称提速百分比，不能以少测缩短时间。组内正例共同通过及必要独立入口/恢复结果证明无状态污染，不以仅一次聚合成功推广所有冷/absence 保证。

## 验证安排与 authority coverage

下表保留原行为 coverage，gate 列中的 G-STRUCTURE/G-ACCEPT 为历史承接。当前受影响部分由恢复与测试集中各自作者结算/补足，分别供 G-CLEANUP-RECOVERY、G-TEST-CONSOLIDATION 判断，最终 G-CORRECTION-REVIEW 核实整体适用性；不要求重跑全部历史检查。原候选证据按来源/内容/输入/环境/消费者/新鲜度复用；恢复先结算已有成功/混合失败输出，测试集中按实际 driver/fixture 变化定向补证。参数化配置观察覆盖分类，不将所有形态扩成真实引擎全矩阵；不得删既有有效覆盖提速或将单个编译成功推广四消费者。

| Authority / 目标 | 方法及可观察判据 | 承接 gate |
| --- | --- | --- |
| G1，全局解耦 / V3 静态 | 沿平台→家族→唯一 setupFiles→窄机制→各消费/出口读取实际实现；核对一次注册、原状态、无隐藏导航/总参数转发；核对 Joined/Split 纯数据贯穿 named/AW/Strip/tasks、同任务 output/records 配对且无逐阶段 Map 往返；执行下述代表变化审查。类型存在不能代替阶段/producer 证据 | G-STRUCTURE，G-ACCEPT 独立核实 |
| G2/G4，原料、版本及时序 | 扩展 platform/sides/identities 配置观察：Fabric split/included/unsplit 与 joined、main/secondary；primary/other 注册名称/数目/目录、输入 producer 与角色惰性映射；解析前合法 primary/side/开关改变不重注册，形状/primary 不匹配求值失败。SERVER 保 Split 但仅 common 有效 games/ops/records；CLIENT 查找/编译 client→common 与 records common→client 分别断言；未分割 CLIENT+COMMON、独立 client:data 自身 preview AW/main runtime AW、Joined 单片。joined loaderVersion 后配仍改变输出。构造返回前库/metadata/继承、已存在立即/迟到 listeners、publish→listeners→action、重复 action/互斥失败点均观察；最终值相等不替代中间时序 | G-STRUCTURE，G-ACCEPT |
| G3，身份/公开兼容；V2/V5 | 原 C/R/SourceSet 身份、完整成员、artifact/component/variant/classifier、scope、项目 classes/resources/NO-SOURCE、用户追加与 native producer 保留；定点核对受影响公开/保留出口、FinalJarTasks 五字段/生成接口/getters/protected setupFiles。typed/形状失败及字符串实值保持，已取消旧 ABI 不恢复 | 历史 G-STRUCTURE/G-ACCEPT；当前必要清理由 G-CLEANUP-RECOVERY 承接，正常组覆盖由 G-TEST-CONSOLIDATION 承接，最终 G-CORRECTION-REVIEW 核对 |
| G5，真实关系 | Linkage incoming/include/constraints/AW/Mixins/outgoing/data WITHOUT_DATA/test output、Common static linkage；独立 client 已存在但无 data/test，Common 回退 client 而 run 回退 target secondary，之后配置 secondary 再读 Provider；检查未自动创建缺失成员及缺 run 输入原失败。源码未改不意味着可省消费者回归 | G-STRUCTURE 的作者观察由 Common 消费迁移承担（具体 I-COMMON 方法见 Waves）；G-ACCEPT 独立核实 |
| G6 / V1/V3/V4 | 真 Java、Kotlin 编译调用两片注入 API，sources 实际所选 clean input/另一片 library 及产物成员，stub 至少两个 target 共有 API/混合关闭参与者；分别从新 Home、无 P/N/清理输出/Transform workspace 首次请求四消费者，记录实际图/参数读取先后/实际消费。file 与 Maven 路线均覆盖；保角色/游戏顺序与独立 records | G-ACCEPT；对应作者 item 先做定向回归 |
| G6/G8 / V5/V6/V7 | on/off 冷启动、解析前 true→false/false→true、side/primary 后配、targets 隔离；关闭保完整外部 producer、AW/remap，不安装本次 state（含 raw），不调度 Mixin/Strip，且必要 operation 直接为空、不求值本轮 operations Provider；兼容完整 operations getter 仍保原协议。独立 C/view/SourceSet 物化不靠同场游戏代调度。实际 remap/refmap/context 与无 remap 两路线；必要 producer/分片/source 与游戏清理失败阻断、撤故障恢复、所有原始输入保护。合法空/零记录、记录顺序/来源、AW/side/mapping/context/source 成员或路径变化失效与新进程恢复按 V7；复用适用 Codev 组件证据，不穷举引擎/close/move 全矩阵 | G-ACCEPT |
| G7 / V2/V5/V7 | sync 实际 files 物化与真实 Tooling 分开观察；原模型全节点/SourceSet index（含未命中）、build/project 关联、闭包、scope、variant 与清理文件对照，不过滤额外成员、不以 set 相等/计数证明；同请求 forTasks(sync) 准备标记先于 builder，故障时 builder 不消费，撤故障恢复。runtime/legacy/发布解析保持 R/AW/raw C，检查 clean/records 无回流 | G-ACCEPT |

### 本增量的聚焦验证安排

| 受影响边界 | 必要证据与实际 consumer |
| --- | --- |
| 来源与编译 | 先结算既有成功编译的实际候选/冻结来源/唯一输入/getter/code source/metadata 关联，再按失效或缺证补最小同源运行。新运行沿私有编译/metadata/logs 方法，生产/测试 Kotlin、KSP、Java（确有源时）、Test 及 fixture 消费同源输入；其它条目/顺序/传递图/builtBy 不变。默认发布 Codev 的 `.from` 错误不作生产修复理由。 |
| typed 形状/真实调用 | 以现有 GameShardsTests 与受影响 MixinCompilationTests/fixture 为基础，证明 Joined/Split 值/槽位、独立惰性 primary、形状不匹配失败及真实平台 typed 构造/secondary 调用；旧非法字符串/Map 输入在入口删除后不再是测试义务。 |
| 角色/依赖与平台装配 | 若删除 accessor/旧 getter/构造或观察迁移涉及角色、完整 unmapped C/R、native view、Fabric/Joined 构造/后配，运行相应身份/producer/装配检查；保四辅助配置、原任务/路径/字符串实值、构造前副作用与已有/迟到 listeners。不能只用“旧 Legacy 测试可删”证明此边界无行为变化。 |
| 真实消费与保留出口 | 对实际受影响接线运行最小真实 Java/Kotlin、sources、stub、sync/Tooling 的消费或回归入口，核对有源编译/实际清理文件、同任务 output/records、顺序、关闭/后配、原生 producer、必要失败阻断及 runtime/raw/clean 隔离；四消费者中未变边界明确说明可复用何种适用证据，变化/缺证部分由清理作者在终态前补齐。定点核对公开/明确保留出口，不进行完整 ABI 审计。 |

实际 diff 决定检查组合，不强制全平台/版本矩阵或整套 V6/V7。恢复作者先结算当前输出，并报告“沿用/本次覆盖/仍缺”；测试集中作者承担其新 driver/fixture 的有效断言对应、无状态污染、实际准备/调用次数/计时及受影响特殊入口补证。最终独立审查只核对最终候选与这两份作者证据，不能代填；必要检查失败/缺失就不满足相应作者谓词。历史 gate/绿色 XML 或单行构建成功不单独证明新候选通过。

## 当前采用结果与 bounded acceptance（历史候选）

- `I-V67-HARNESS` 的 completed Result 已由 Router 采用。`REPORT.md`、`checked-observations.json` 和最终 XML 记录了 Windows、Gradle 9.5.1、Microsoft JDK 17.0.19、offline、关闭 configuration cache、本地未发布 Codev 下的两项测试 2 tests/0 failures/0 errors，以及 context/source location/AW/side/mapping 变化、失败恢复、records 顺序清理、输入保护和不同进程观察；Java 根任务的 NO-SOURCE 限制保留。
- 新的独立 `I-ACCEPT` Result 已由 Router 采用。其 bounded acceptance 覆盖当时指定 dirty 候选及本地开发环境内的 G1–G8、V1–V7 适用义务；Router 已判定历史 `G-STRUCTURE` 与 `G-ACCEPT` 满足。该判定不证明清理/测试集中后的候选，不把 I-ACCEPT 倒填为作者证据，不改写旧 blocked Result，也不扩大为正式发布或完整矩阵。
- 可复用证据入口：[V67 报告](../../../build/tmp/v67-recovery-20261108-d/REPORT.md)、[V67 observations](../../../build/tmp/v67-recovery-20261108-d/checked-observations.json)、[V67 测试 XML](../../../build/tmp/v67-recovery-20261108-d/results/verified/mixinCompilationTest/TEST-earth.terrarium.cloche.MixinCompilationTests.xml)、[I-SYNC checks](../../../build/tmp/i-sync-evidence-recovery-b89893880337/model-checks/checks.json)、[独立接受 model comparison](../../../build/tmp/i-accept-review-85b722068e/model-comparison/checks.json)、[独立接受 review evidence](../../../build/tmp/i-accept-review-85b722068e/review-evidence.json) 与 [finish check](../../../build/tmp/i-accept-review-85b722068e/finish-check.json)。这些仓库内引用仅作后续证据入口，不是项目外原始 Result 依赖。
- 未覆盖限制继续有效：正式 Codev/Cloche 依赖发布、完整外部 ABI、最低 Gradle/其它平台与版本、configuration cache、远程缓存、性能、最终 IDE 导入/模块根/PSI、完整引擎/平台笛卡尔矩阵及正式发布二进制；角色算法既有精度边界和 standalone `sources=true` 的 common-only 限制也保留。

代表变化审查必须指出当前实现中的具体输入/函数及会改动的位置，而非只数函数：

- sources 输出名或 Decompile library 改变仅涉及 sources 注册及组合处，不穿透 AW/preview 接口。
- sync 的物化方式改变不修改目录/classifier，Java/Kotlin 新消费者只取得 preparation、不读取 enabled/side/records。
- joined loaderVersion/输出规则改变不修改 Common/Mixin 消费；库对齐变更只在家族装配原时点。
- client:data 显式 AW 变化只进入自身 preview，runtime common 仍 main 原 AW；data Linkage 的成组变化仍需同时核对资源/回环，不能为了“解耦”拆散。

原作者责任同时适用于恢复与测试集中：改变已验证接口、连接、输入或环境的作者，在自身终态前判定沿用/本项覆盖/具体需补并实际补足。未变证据按真实适用性复用，不因共文件或 byte 改变全量重跑；正常组证明覆盖保持，冷/absence/恢复的特殊语义仍独立，不把作者工作交给最终审查。

原机制→组合、Common/sync 及 V6/V7 的证据接续已由历史 I-V67-HARNESS/独立 I-ACCEPT 闭合，仅支持当时候选。清理恢复与测试集中各自负责受其实际变化影响的实参配对、producer、首产/absence、物化/Tooling 时序与故障恢复，不追改旧 I-SYNC 等终态。

最终独立修正审查读取清理及测试集中后的整体 diff、相接实现和完整作者证据，判断保留出口/行为及覆盖是否受损；按影响独立观察，不重做“内部兼容是否必要”调查，不修代码/回写图。作者缺证则报告其具体影响，不以审查运行倒填 G-CLEANUP-RECOVERY 或 G-TEST-CONSOLIDATION；全部 V1–V7 保证不降低，也不变成强制全历史矩阵重跑。

### 可执行来源与现场检查

复用 Context 所述现有测试入口与私有同源编译方法；开发验收限定 Windows、现场实际可用 Gradle 9.5.1、JDK17、本地未发布 Codev 修正版，明确关闭 configuration cache。普通 TestKit 沿默认安装/worker 来源；helper 依实际 Runner 类空间/worker JDK 和 Connector 来源绑定，不添加 fixture 安装或 JVM 覆盖。最低 9.4、wrapper 9.8.2 未由此证明。

未来执行先 fresh 检查实际 Codev 模块 identity/唯一新 getter、字节及 code sources；本增量冻结 Mixins SHA256 为 `7AA32D6DD42A69A0AF12E1B651F1803ECFE4F84D4A441AC0C8E3FE8657493F35`，不是本次已复验或永久基线。核对 Kotlin/KSP/Java/Test/metadata 同源与其它输入/builtBy、Runner/worker/helper 来源。恢复先读已有现场，禁止原样重放旧 init/状态脚本或覆盖原 ready/logs；必要新运行用新私有 invocation 编译/metadata/日志。普通组可共享其 ProjectDir/Home；冷入口新实例/无旧 Transform/P/N，连续恢复同 ProjectDir/Home/旧输出。命令如 `mixinCompilationTest --tests '...MixinCompilationTests.<method>' --offline --no-configuration-cache` 必须配合当次同源安排；本准备没有运行这些命令。

私有同源编译和真实消费观察方法已有原运行支持；清理后 producer/首次物化/后配仍须按实际影响验证，不能仅凭准备材料宣称通过。保持原 TaskProvider、生产链与消费者原位置，不以清理为由新增 scheduler；若确实无法在原合同内满足必要消费，报告具体 ADR 重访边界，不暗改语义或把未知藏到未来研究。

## 风险、取舍与完成判定

原直接组合的持续风险仍在 setupFiles 配对/顺序、primary/other 映射、静态 Split 与有效 SERVER、两种次序、Provider/原生 producer/参数隔离/物化的区别。清理若影响这些边界，按聚焦安排补真实首产/后配/失败观察；未变且适用的历史证据可复用，不机械重做原迁移。

取消指定内部入口支持会使依赖它们的旧 JVM caller 不获兼容保证，已由用户明确接受；不据此破坏公开或明确保留出口，也不声称外部 caller 不存在。主要增量风险是把适配和真实行为测试一起删掉、typed 迁移改变懒值/producer/顺序或字符串实值、用错误依赖环境的失败掩盖实际缺证。保留角色算法及其精度边界；构造/监听部分失败不自动回滚，不能 replay 已完成 durable effects。

原 G-STRUCTURE/G-ACCEPT 保持历史接受；旧 I-COMPAT-CLEANUP 的 partial 与 G-CLEANUP 未释放事实保留，但不作为新 gate 永久不可完成的 member/前置。当前必要清理谓词由新恢复结果及 G-CLEANUP-RECOVERY 承接，随后 G-TEST-CONSOLIDATION 支持正常组覆盖/调用收敛，最终 G-CORRECTION-REVIEW 对同一最终候选作独立判断；均待实际证据和 Router 判定。测试重组 scope 已经 Router 完整披露并获用户确认，不是由图维护或旧确认自动授予。仍保本地未发布来源、configuration cache/完整 ABI/最低版本/最终 IDE 未验边界；没有产品性能量化或旧逐方法耗时基准。

## 授权的 V6/V7 恢复（历史已完成，不重放）

原有界恢复以验收 harness、受控私有输入、定向运行和适用性汇合闭合当时候选 V6/V7 作者证据；I-V67-HARNESS 与后续独立 I-ACCEPT 已采用。该恢复不修改生产、authority、正式依赖或 wrapper；不与本次已确认清理权限混淆。方法与真实 context/source/location/AW/side/mapping、records/输入保护/新进程观察见 Context 及可读 V67 报告，不重放历史脚本或要求新清理全部重跑。组件语义、当时集成观察和当前可复用范围须区分；保留 standalone sources=true 为 common-only、局部 JVM 兼容及正式来源未验限制，不扩成 Split CLIENT 或完整 ABI 的新证明。

## 作者自检与独立计划复核边界

原恢复前自检为历史范围：当时 12 items、2 Waves、2 gates，原恢复写范围、sync 汇合及前 gate 作者责任已由后续有界结果采用；旧 failed/blocked/partial 不追改。它不证明当前清理或新计划增量已独立复核。
以下作者准备/自检叙述属于当时阶段，不表示当前仍待复核或确认：后续新独立 review-delivery-plan 已 completed 且无阻断，Router 已采用；用户经完整 scope 披露已确认测试集中实施及定向验证。首次因作者回执截断而 blocked 的复核不改写，新复核完整消费作者事实后通过。本次仅同步上述已采用事实，不重新独立审查。

本次完整读取四份现有派生材料，加载完整 primary/四个 companion，核对 PRD/ADR/两 Spec 的相关保证；采用 Handoff 已可靠关联的 compact 调查、reuse assessment、清理 partial 及失败/采用限制。路线可行依据已足：现有根多项目模板与具名 probes 可聚合相容普通条件，特殊入口状态寿命明确；未来实际输出属于作者接受证据，未伪装新增调研阶段。动态现场只在执行时有意义，已有只读结算/关联及只补缺口的方法；本次未检查进程或结算 XML。

自检覆盖一个派生安排终态、三个未来独立结果的分组，以及 predicates/evidence、真实候选/测试输出冲突串行、item prerequisites + Wave 前置 gate + gate 全成员等待、唯一归属/ID/无环和仓库引用可读性。原 12 项与旧两 gate 不变；旧清理 partial/旧 gate 未释放留历史，明确新恢复结果承接必要清理条件，不依赖旧 partial 完成。测试集中与恢复可分别保留成果/失败恢复，独立 verdict 另有证据消费边界，不能混成一个 Result。

作者准备时确认：恢复沿用原清理语义/保留保证/来源/风险及未变持续确认，但新增终态身份与结算连接；测试集中增加 driver/fixture 写范围、断言对应/状态寿命及次数耗时证据，最终审查改为消费最终两项作者证据。整体不是身份更名式 review 复用；当时要求独立计划复核覆盖这些实质 delta 及与历史保证的必要连接，旧评审只支持未变部分。该后续复核及测试 scope 确认现已完成；仍缺恢复结算、测试集中实际作者证据及最终独立修正审查结论。

此前作者阶段只完成四文档准备与静态自检，未构建/测试、运行旧脚本、修改生产/fixture/authority 或执行独立审查。本次也只同步状态，不实施三个增量、不宣称 item 完成或 gate 释放；后续独立计划复核与用户确认是 Router 已采用的事实，不是本次自检产物。C4/C5/C6 与实际 gate 判断独立保留。