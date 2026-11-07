# 整体 target / compilation 函数式解耦 Plan

[Work](work.md) · [Context](context.md) · [唯一匹配 Waves](waves.md)

状态：同一未执行初始 Plan 的 Joined/Split 模型迁移与证据责任已修订、作者已自检。已采用旧独立评审的证据责任阻断已由前次修订解决；旧 review 仅对未变部分适用，本轮 Joined/Split 迁移 delta 及必要连接已独立静态复核，无 blocking 或需先纠正的 nonblocking finding；结合旧 review 未变范围，Router 已判断计划 ready。未获实施 scope 确认，未执行。本文拥有一个整体交付的 scope、策略、风险与验证；唯一执行 item graph 在 Waves，不另建 Tasks、总览实施图或第二套任务表。

## Authority assessment

| 家族 | 处理与 owner | 依据 |
| --- | --- | --- |
| PRD | reuse：[Mixin PRD](../../prd/mixin-preapplication.md) | 仅沿用默认/关闭、外部 compile mod、四消费者、环境与运行发布隔离；全局内部组织不新增产品能力，不创建全 Cloche PRD。 |
| ADR | reuse：[函数式组合 ADR](../../adr/target-compilation-functional-composition.md) | 同一已接受决定已选 Joined/Split 泛型纯数据、显式输入、原状态、setupFiles 原位与兼容；具体标识符/签名可调整，接受不等于执行确认。 |
| Spec | reuse：[全局 Spec](../../spec/target-compilation-architecture.md) 与 [Mixin owner](../../spec/mixin-preapplication.md) | 当前全局第 2/5/8 节已拥有有限形状、惰性阶段/任务投影与 Map/String 兼容，G1–G8 拥有跨层/身份/时间/兼容；Mixin 的职责、图、模型许可和 V1–V7 同向，无待实施时选择的 authority 冲突。 |

[Accepted 提案](../../proposals/target-compilation-architecture.md) 的模型、平台/阶段/消费/兼容接口和图仅作非权威设计解释；内部标识符/精确签名可变，Joined/Split 形状与投影语义已选。ADR 尾部及提案中的旧同步状态不覆盖两 Spec 当前正文；不改只读 authority/提案/HTML。领域 Context reuse：没有新领域含义；不改 Codev 引擎或发布 authority。本次阅读及复用 assessment 未发现本 scope 所需的权威缺失，不新增 ADR/Spec/PRD 或研究前置。

## Outcome 与范围

交付保留原行为的全局组织：平台注册只接显式原料，家族仍原位配对；角色/完整视图各守边界；SourceSet 目录、sync、classifier 可独立变化；唯一 setupFiles 直接组合 AW、preview、相关消费输入与 sources；编译/Common/sync 复用准备协议。内部游戏及 Mixin task 角色以泛型纯数据 `Joined(value)` / `Split(common, client)` 保形贯穿阶段，不靠字符串键或插序传协议；类型本身不保证阶段或 producer 已执行。target/main 导航止于家族或出口装配，下层不靠整个 target/Info/compilation 找数据。状态仍在原 DSL/Property/Configuration/SourceSet/Task。

必须保留：Fabric split/included/unsplit、Forge/NeoForge joined，main/data/test 与 client secondary；C/R、SourceSet 原实例/名字/作用域/属性/variant/classifier/capability、classes/resources 和用户追加；feature/test 例外；主辅助多图角色算法与四辅助配置；库/metadata/patches/mappings 和原时序；Linkage/Common 源码关系；Common/run 各自 fallback；runtime/legacy/归档/发布的原输入。

排除：角色算法新修复或删除四配置；公开 DSL/ABI 破坏；修改 sources/javadoc 普通布尔选项生效时点；Forge clientTest、Common 环/孤立语义旧缺陷；新增职责对象、总 context、registry、freeze、通用 pipeline、汇总 Task 或 scheduler；引擎矩阵重做、正式 Codev 升版/发布、最低 Gradle 全矩阵、最终 IDE 实验、性能优化、CI、Git stage/commit/push。普通实现中的必要测试/fixture 与私有同源编译安排属于未来 scope，当前只写这四份文档。

## 具体组织与迁移策略

以下为设计约束，不宣称候选已实现。代码定位及现有连接见 Context，详细调用示意复用提案而不复制全部签名。

| 边界 / owner | 输入、输出与副作用 | 迁移方式与禁止传播的知识 |
| --- | --- | --- |
| Fabric / joined 原料函数 | Project、原名字/目录/namespace/版本与 mapping Providers → 原任务/库/游戏句柄；原位置注册一次 | 在原 Info 构造位置分别输入 Split / Joined named，secondary 复用原句柄；primary 独立 Provider。先加共享兼容边界让未迁移平台/旧转换能继续消费，不先删 Map/String 构造/getters。注册不接 R 或执行 runtime 对齐；joined loaderVersion 与输出文件名保持惰性。 |
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

兼容保留 FinalJarTasks 的包/类、五字段顺序/类型、data class 生成接口及 library/sources getters，setupFiles 保持 protected 原类型；新内部数据与旧出口投影同一结果。共享 Info 保旧构造入口及可见 getters 的实际 JVM owner/descriptor（含适用生成入口），新内部角色可用 enum，但 `primaryMinecraftPart: Provider<String>` 继续返回 common/client/joined 字符串，不能靠泛型擦除偷换值。必要 named Map 入口完整键集合须恰为 `{joined}` 或 `{common, client}`；空/缺/多/混合键、未知标签和形状/primary 不匹配必须失败。旧 getter 仅反向投影原标签/槽位，新链不得逐阶段 Map 往返。

被迁移的 internal JVM 入口默认保留原 owner/descriptor；最终仅单向读取同一已组合结果（必要时内部无行为访问口），不执行注册；候选调用不经旧入口，避免构造循环。源码定位含 `TargetCompilationKt.registerCompilationTransformations`、`MixinPreapplicationKt.registerMixinPreapplication`、Info 构造/getters；实际字节 owner/descriptor 要在未来同源编译对照中确认，不以文件名或 Kotlin internal 代替。每个改变可见边界的作者在自身终态前对照改前/后字节与旧调用，最终组合核对整体旧出口/一次注册；完整外部 ABI 不在本轮承诺。

### 分阶段价值及依赖

平台原料边界可由原家族/运行直接消费；Fabric 与 joined 任一成功可保留而另一失败只修剩余平台。共享 Info 必须先提供可同时承接旧 Map/String 输入与新形状的单向边界，保留未迁移 joined/旧调用可编译、可消费的中间态；例如保旧构造并校验转换为形状、新输入以形状构造而旧 getters 仅投影，不预先删除旧入口。实际构造签名及默认参数生成入口按改前字节核对，不能添加仅靠泛型实参区分的重载。旧总函数尚未迁完时可继续读兼容出口；已迁机制以形状为主，兼容只在当前迁移接缝转换，不在各阶段往返。这是平台终态可保留的必要部分，不另交一个类型定义/研究 Result。原配置查询/buckets 可由旧转换和 remap 消费，目录/classifier 分离可由旧 sync 使用，故各有独立终态。

组合迁移前，旧总函数仍能单向调用窄 AW、preview 和 sources 函数并保留其余正文，所以每机制有可被旧调用者消费、可保留/恢复的终态。AW 交付 named→awGames 与 primary/other 惰性映射；preview 交付 AW→同形 tasks→output/独立 records→strippedGames 与有效选择；sources 仅消费独立 primary 已选的 input/libraries/preparation。旧签名需要的临时边界适配随该项完成，不能第二次注册；最终组合关闭这些内部 Map 接缝，只留必要外部兼容入口。最终组合项移除候选对总函数的调用，集中相关选择、完整 preparation 和旧出口投影。Common/sync 分别接有效有序游戏与 preparation，不承担形状解释；它们各有独立旧消费者与可保留迁移终态。必要 helper、旧调用迁移与回归只建立对应谓词，不按函数数拆项。

不增加单独“兼容调查”“future research”“测试框架”“实现 review”或“收尾”item：保签名路线已有可行证据；fixture 的新增断言归其所证明实现；最终验收同时审查实际组织和行为证据，独立静态 review 不另设同义 gate；每次 invocation 的输出/进程责任归执行者，关闭记录用最终有限结论即可。真实任务等待/写冲突/结果消费与图分组见 Waves。

## 验证安排与 authority coverage

这里定义充分证据，全部是未来执行义务。采用同输入、同二进制与环境的当前行为对照；原缺口与候选回归分开，不能将原 bug 冻结成标准。参数化配置观察可覆盖全家族，不把每形态都扩为真实引擎全矩阵。受改动保证逐项覆盖，无消费者的引擎穷举不加入。

| Authority / 目标 | 方法及可观察判据 | 承接 gate |
| --- | --- | --- |
| G1，全局解耦 / V3 静态 | 沿平台→家族→唯一 setupFiles→窄机制→各消费/出口读取实际实现；核对一次注册、原状态、无隐藏导航/总参数转发；核对 Joined/Split 纯数据贯穿 named/AW/Strip/tasks、同任务 output/records 配对且无逐阶段 Map 往返；执行下述代表变化审查。类型存在不能代替阶段/producer 证据 | G-STRUCTURE，G-ACCEPT 独立核实 |
| G2/G4，原料、版本及时序 | 扩展 platform/sides/identities 配置观察：Fabric split/included/unsplit 与 joined、main/secondary；primary/other 注册名称/数目/目录、输入 producer 与角色惰性映射；解析前合法 primary/side/开关改变不重注册，形状/primary 不匹配求值失败。SERVER 保 Split 但仅 common 有效 games/ops/records；CLIENT 查找/编译 client→common 与 records common→client 分别断言；未分割 CLIENT+COMMON、独立 client:data 自身 preview AW/main runtime AW、Joined 单片。joined loaderVersion 后配仍改变输出。构造返回前库/metadata/继承、已存在立即/迟到 listeners、publish→listeners→action、重复 action/互斥失败点均观察；最终值相等不替代中间时序 | G-STRUCTURE，G-ACCEPT |
| G3，身份/兼容；V2/V5 | 原 C/R/SourceSet 引用、名称/attributes/完整成员、artifact/component/variant/classifier、scope、项目 classes/resources/NO-SOURCE、用户追加；完整 unmapped C/R 与四角色/native producer 分开。扩展 classifier/project-consumers/identities，不重选图或强造 JAR。对照各受影响实际 JVM owner/descriptor、Info 构造/旧调用、FinalJarTasks 生成接口/getters；核对旧 String 实值及合法/非法 Map、未知角色/不匹配失败，无二次注册；保留现 fixture 原协议断言。共享 Info 中间态须同时编译/消费已迁 Fabric、未迁 joined 和旧入口 | G-STRUCTURE，G-ACCEPT |
| G5，真实关系 | Linkage incoming/include/constraints/AW/Mixins/outgoing/data WITHOUT_DATA/test output、Common static linkage；独立 client 已存在但无 data/test，Common 回退 client 而 run 回退 target secondary，之后配置 secondary 再读 Provider；检查未自动创建缺失成员及缺 run 输入原失败。源码未改不意味着可省消费者回归 | G-STRUCTURE 的作者观察由 Common 消费迁移承担（具体 I-COMMON 方法见 Waves）；G-ACCEPT 独立核实 |
| G6 / V1/V3/V4 | 真 Java、Kotlin 编译调用两片注入 API，sources 实际所选 clean input/另一片 library 及产物成员，stub 至少两个 target 共有 API/混合关闭参与者；分别从新 Home、无 P/N/清理输出/Transform workspace 首次请求四消费者，记录实际图/参数读取先后/实际消费。file 与 Maven 路线均覆盖；保角色/游戏顺序与独立 records | G-ACCEPT；对应作者 item 先做定向回归 |
| G6/G8 / V5/V6/V7 | on/off 冷启动、解析前 true→false/false→true、side/primary 后配、targets 隔离；关闭保完整外部 producer、AW/remap，不安装本次 state（含 raw），不调度 Mixin/Strip，且必要 operation 直接为空、不求值本轮 operations Provider；兼容完整 operations getter 仍保原协议。独立 C/view/SourceSet 物化不靠同场游戏代调度。实际 remap/refmap/context 与无 remap 两路线；必要 producer/分片/source 与游戏清理失败阻断、撤故障恢复、所有原始输入保护。合法空/零记录、记录顺序/来源、AW/side/mapping/context/source 成员或路径变化失效与新进程恢复按 V7；复用适用 Codev 组件证据，不穷举引擎/close/move 全矩阵 | G-ACCEPT |
| G7 / V2/V5/V7 | sync 实际 files 物化与真实 Tooling 分开观察；原模型全节点/SourceSet index（含未命中）、build/project 关联、闭包、scope、variant 与清理文件对照，不过滤额外成员、不以 set 相等/计数证明；同请求 forTasks(sync) 准备标记先于 builder，故障时 builder 不消费，撤故障恢复。runtime/legacy/发布解析保持 R/AW/raw C，检查 clean/records 无回流 | G-ACCEPT |

代表变化审查必须指出当前实现中的具体输入/函数及会改动的位置，而非只数函数：

- sources 输出名或 Decompile library 改变仅涉及 sources 注册及组合处，不穿透 AW/preview 接口。
- sync 的物化方式改变不修改目录/classifier，Java/Kotlin 新消费者只取得 preparation、不读取 enabled/side/records。
- joined loaderVersion/输出规则改变不修改 Common/Mixin 消费；库对齐变更只在家族装配原时点。
- client:data 显式 AW 变化只进入自身 preview，runtime common 仍 main 原 AW；data Linkage 的成组变化仍需同时核对资源/回环，不能为了“解耦”拆散。

已有测试是方法基础，不是覆盖声明。实现者需补充上表相对现 fixture 缺少的时序、lazy loader、C/R 引用、完整 off preparation 等观察并运行本 item 最小回归。**G-STRUCTURE 所需作者证据必须在该 gate 前生产**：改变已验证接口、连接、输入或环境的作者在自身终态前判断证据适用性，沿用仍匹配的部分，说明本项运行已覆盖的部分，并实际补足具体失效/缺证检查。后续项不能只交局部成功、把前证据是否仍成立留给独立验收。适用性随实际来源、输入、消费者与执行条件判断，不要求每次 byte 变化全量重跑。

机制到直接组合时，AW/preview/sources 未变的内部机制证据可保留，但新实参配对、共享 preparation 的编译/sources 首产、后配及失败阻断须有直接组合阶段的适用证据。sync 换接时，可保留输入/注册位置未变的目录/classifier、编译/sources 与 stub/G5 证据；新 sync 物化及 Tooling 时序由 sync 作者验证，其实际改动若影响这些前边界，则由它在终态前补对应检查。最终适用性由最后的 sync 消费接线项随自身结果汇合已有作者证据；这是检验该次接线改动影响及提供 gate 输入，不是第二个全局实施验收或独立研究结果。具体责任及必要方法仅在 Waves 定义。

独立验收者另行审查实际实现和作者证据适用性，可运行独立接受尚需的检查，复用仍适用的相同运行；不得承担前 gate 缺失的作者工作或用后来的补测倒填 G-STRUCTURE。若发现前 gate 所需证据缺失/失效，报告其影响而不修实现或回写图。G-ACCEPT 仍须覆盖上表全部适用义务；V6/V7 某必须集成证据缺失时，必须取得或报告明确 gap，不能用未覆盖清单替代必要 gate。

### 可执行来源与现场检查

复用 Context 所述现有测试入口与私有同源编译方法；开发验收限定 Windows、现场实际可用 Gradle 9.5.1、JDK17、本地未发布 Codev 修正版，明确关闭 configuration cache。普通 TestKit 沿默认安装/worker 来源；helper 依实际 Runner 类空间/worker JDK 和 Connector 来源绑定，不添加 fixture 安装或 JVM 覆盖。最低 9.4、wrapper 9.8.2 未由此证明。

未来 invocation 开始前需 fresh 检查实际 Codev 模块 identity/唯一新 getter、字节及 code sources、Kotlin/KSP/Java/Test/metadata 同源与其它条目/builtBy 保留、测试 worker/helper/子构建环境。每次使用新私有目录、metadata、日志及 Home，不覆盖历史 ready 标记或结果；冷启动不能复制 Transform workspace/P/N。旧 init 只能读取方法后针对本次路径/当前输入改写，不原样重放。任何命令示意如 `mixinCompilationTest --tests '...MixinCompilationTests.<method>' --offline --no-configuration-cache` 都须配合该次私有编译输入安排；本准备未运行或验证这些命令。

当前可获得/观察证据的方法已存在；新 preparation 的首产、Transform 隔离和后配置是否成立是实现接受条件，不能现在宣称已证明。保原 TaskProvider 与消费者原位置补明确边是当前路线，无需先引入 scheduler。若实际只能靠配置期读文件、修改产品语义或新增调度器实现，则触发 ADR 重访，停止受影响 scope，不把它伪装成未来 research 箭头。

## 风险、取舍与完成判定

直接组合减少全参数转发和重复消费协议，但 setupFiles 仍负责配对/顺序，Gradle 不静态保证恰好一次。GameShards 只约束槽位；primary/other 与角色映射、静态 Split/有效 SERVER、两种次序若混淆仍会回归，须分别看任务身份、producer、参数和实际字节。Provider 来源、原生 buildDependencies、实际图、Transform 参数隔离和文件物化是不同证据；最大的新增风险仍是 sources 与共享 preparation 首产时机，必须有上述真实冷启动观察。

保签名适配比删除入口多一层兼容出口，代价可控且不多注册；未审计的任意外部 JVM 使用不视为不存在。角色算法保留而非修复，其精度边界不扩大。构造/监听部分失败不自动回滚；后继不得 replay 已完成 durable effects。没有理由机械改写 Linkage/run；保留它们降低时序与发布回归风险，但仍核对与新输入相接的路径。

Plan 完成需要 Waves `G-STRUCTURE` 与 `G-ACCEPT` 全部满足：实际全局职责收窄及受影响行为合同有适用证据、无未解决阻断；只编译通过或只得到 completed 标签不足。最终声明必须保留本地未发布来源、configuration cache/性能/完整 ABI/最低版本矩阵/最终 IDE 未验证。正式发布可用性不作为本地开发交付的前置，也不能被宣称完成。

## 作者自检（仅初始准备模型修订）

本轮完整读同目录四文档及当前全局 Spec，读取 ADR 的接受/模型/兼容/历史协调、Mixin Spec 的适用合同/职责/图/V1–V7，提案的模型/平台/阶段/消费/兼容接口及图，以及复用 PRD/领域 Context。定点读取当前 TargetCompilation/Info/旧总函数、MixinPreapplication、两平台 Info/client secondary 和 platform/sides fixture，查明共享旧构造/getters、按键和插序消费及原字符串断言。这些实际源码支持先保共享兼容中间态、再迁各机制、最后唯一直接组合；字节兼容和动态行为仍是未来实际检查，不声称本次编译证明。

按 owning 方法逐项重检：平台各能由原家族/运行消费并保留，形状定义/Info 接缝只是实现该谓词的必要 helper；views/SourceSet 可由旧转换/sync 消费；AW/preview/sources 可由旧总函数分别消费，后项失败不重做前项注册。最终组合集中相关选择/preparation/旧出口，不能拆出独立“兼容完成”或重复注册结果；Common/run 策略对照只证明 Common 参与者等价，sync 汇合只检验自身影响并消费前证据，独立接受不给前 gate 产证。故保持 11 个单谓词 items、2 Waves/gates，无新增等待边、第二图或 Tasks。沿用原身份/时序/角色/关系/运行发布与全部 G1–G8、适用 V1–V7；模型增量只强化相应迁移和证据义务。

检查 Plan scope/策略/风险/验证与 Waves 谓词/gates 闭合：新增保形/primary/原任务/旧 Map/String/JVM 证据由改变该边界的作者在自身终态前生产，组合承接机制中间态，Common 仍负责全部 G5，sync 验自身物化/Tooling并汇合适用性，G-STRUCTURE 只判断已有作者证据。按所有 item prerequisite + 所属 Wave 前置 gate + gate 等待所有 members 合并核对无环，各 item membership 恰一次；共源码/fixture/metadata/编译输出冲突已有串行，Plan 不复制图。匹配互链、全部仓库相对引用的实际可读性均检查；没有身份迁移或可继承实现/gate 完成状态。现有 fixture、同源编译/字节对照及私有 invocation 方法使所需未来证据可观察，未发现需新架构选择或先做未经授权实验才能承诺的具体 gap。

旧独立评审的证据责任阻断已由前次修订解决，未变部分按可靠已采用结论复用。本轮新增实质边界包括共享 Info 保未迁消费者的中间态、跨阶段/任务保形与惰性角色、有效顺序/原任务身份、旧 Map/String/JVM 协议及机制→直接组合证据接续；这些与相关 Common/sync 衔接已由本轮独立静态复核覆盖，无 blocking 或需先纠正的 nonblocking finding；结合旧 review 未变范围，Router 已判断计划 ready。该结论不证明动态无环、实际 ABI 或 HTML 渲染；首产、后配、Transform、四消费者及 Tooling/兼容仍需未来候选证据。作者自检及独立 readiness 均不满足 C4，不执行/放行 item/gate；material 变化仍保 C6。未运行构建、项目测试、新试验、环境或历史日志复验；Windows/Gradle 9.5.1/JDK17/冻结未发布 Codev/关闭 configuration cache 与其它排除不变。
