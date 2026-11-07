# Target / Compilation 解耦 Waves

[Work](work.md) · [匹配 Plan](plan.md) · [Context](context.md)

这是本 tuple 唯一执行 item graph。本次授权覆盖尚未实施初始图的 Joined/Split 迁移、兼容及证据责任修订；作者已自检。已采用旧独立评审的原证据责任阻断已由前次修订解决，旧 review 仅对未变部分适用，本轮 Joined/Split 迁移 delta 及必要连接已独立静态复核，无 blocking 或需先纠正的 nonblocking finding；结合旧 review 未变范围，Router 已判断计划 ready。以下实现/测试/独立验收均**未执行，待具体 scope 确认**。所有 gates **未通过/未判定**。本文不授予执行、未来无限图维护、调度或发布权限。

## 分组结论

原料存在可由旧家族直接消费的句柄边界，因此 Fabric 与 joined 分别完成/失败而不回滚对方。依赖查询可由旧转换消费；SourceSet 职责分离可由旧 sync 消费；Common 与 sync 可分别接新 preparation 并保留旧出口；这些是独立可保留/重试的终态，分别成项。

组合项继续检验了 AW、preview、sources 的独立边界：现旧总函数能调用已收窄的机制函数并保留其它原正文，因此每部分都能由旧调用者消费，并在下一机制失败时保留，不必重复注册已完成部分。初稿将三者仅视为组合 helper 的分组据此细化为三个 item；验收是机制不再导航整个 target/Info/compilation 且原调用正常，而非函数数量。预应用机制内部 S/records/Strip/C 状态仍因同一 raw→clean 合同和环路边界保持一起。最终组合项才切断候选的旧总入口并集中消费准备。Common 与 sync 也有独立旧消费者及迁移边界，分别保留。

每项一 Handler、一终态 Result；表中的验证是证明其谓词，不是第二个“独立验收”结果。最终验收归不同于实现作者的独立 owner。无人可把实现与该 verdict 同项完成，也不建立无实际 consumer 的证据/ledger item。

本轮按实际源码重检形状迁移切口：共享 Info 目前是 Map/String，Fabric 与 joined 共用构造，旧转换及 client secondary/fixture 仍读取旧出口。因此 I-FABRIC 的形状和兼容接缝必须让未迁 joined/旧调用继续编译/消费；I-JOINED 再接 Joined，I-AW/PREVIEW 分别把 named→AW、AW→tasks/Strip 改为保形，I-SOURCES 只收独立 primary 已选结果，I-COMPOSE 关闭候选内部 Map 接缝。纯数据定义、兼容与针对性字节/旧值核查只服务其迁移谓词，不能独立宣称有业务消费价值或拆出第二 Result。后项失败可保前项原句柄/旧调用中间态，仅恢复剩余边界；不新增重复注册链。Common/sync 只知有效有序游戏/preparation，不学 GameShards。以上仍支持 11 个单终态 items、2 Waves/gates，没有新增依赖或独立收尾结果。

作者项若改变前项已验证的接口、连接、输入或验证环境，须在**自身终态前**沿实际 diff 判断受影响证据：仍适用而沿用、已被本项运行覆盖、或须定向补验，并实际完成后一类必要检查。结论及覆盖/方法/限制随本项 Result 交付，不回写前项终态、不另建账本。只补具体失效或缺证；未变且仍匹配的行为/输入/环境可重用。补验仅用于证明本项迁移与原消费者等价，越出本项写范围的缺口应报告而不暗中修复，也不能交给前置 gate 后的 I-ACCEPT 生产证据。

## Items

所有 IDs 稳定；prerequisites 同时表达结果等待或明确冲突串行。正文顺序不产生其它依赖。写 owner 仅限其 outcome 的源码/针对性测试，不能修改 authority、产品配置/依赖、其它工作成果或自行推进图状态。

### I-FABRIC — Fabric 原料与原位家族装配边界

- Wave：W-STRUCTURE；prerequisites：无；owner：该边界实现者。
- 唯一终态：Fabric 三形态与已有/迟到 secondary 通过 Split named、独立惰性 primary 和家族 side 事实装配，下层无需 target/main 导航，原库/metadata/listener 行为等价；共享 Info 的未迁 joined 与旧消费者仍可编译/消费同一结果。
- 写范围：FabricTargetImpl、FabricCompilationInfo/client secondary、共享 TargetCompilationInfo 的必要纯数据形状/单向兼容接缝及原料函数；必要测试/fixture。只为保旧调用可改旧装配接缝，不迁 joined 平台、不重写下游机制。
- 证据：Plan G2/G4 的 split/included/unsplit/主/secondary、CLIENT+COMMON、任务/config 身份/producer、构造返回前库/metadata、立即/迟到及用户 action 顺序。新 Split 输入不提前求值 primary/文件，不重注册。G3 定向编译/旧调用验证共享 Info 改后未迁 joined 及旧总函数仍消费成功；对照实际受影响构造/getter JVM owner/descriptor，旧 Map/String 实值不变。原 source/runtime AW 来源分别回归。
- 过渡：保旧构造/getters，仅在接缝校验 named 键恰为 joined 或 common+client、旧角色精确转换，未知/不匹配失败；新内部输入以形状承接。不得靠泛型擦除把旧 String getter 值换 enum，或删除入口留给 I-JOINED 修编译。旧转换尚未迁完时可读兼容出口，注册仍只有原链。
- 冲突：与后续项共享 MixinCompilationTests/fixture 与编译 metadata；完成后方可开始 I-JOINED，不并行写/构建。

### I-JOINED — Forge/NeoForge 原料与原位家族装配边界

- Wave：W-STRUCTURE；prerequisites：I-FABRIC（共享测试/构建写冲突串行，不声称 joined 语义依赖 Fabric）。owner：joined 边界实现者。
- 唯一终态：Forge/NeoForge 用显式原料和 Joined named、独立 JOINED primary、CLIENT 家族事实在原 init/construct 配库/patches/mappings/secondary extra；旧 consumers/输出规则继续工作，不伪造 Split。
- 写范围：ForgeLikeTargetImpl、ForgeCompilationInfo/必要原料函数及测试；只兼容接续 I-FABRIC 的共享 Info 接缝，不破坏已迁 Fabric/旧出口。
- 证据：Joined 单片 CLIENT、主/secondary/迟到、后配 loaderVersion 原惰性文件名、R 对齐、条件 implementation extra 与 legacy direct extra、Forge/NeoForge 原参数/身份与真实代表消费。两平台与 Fabric/旧调用同时编译，受影响 JVM owner/descriptor 和旧 joined 字符串/Map 投影保原协议、无二次注册；纯数据类型不替代 producer 或实际文件证据。
- 冲突：共享 Info/fixture/metadata 在最新 I-FABRIC 状态上处理，I-VIEWS 等待本项消除测试/构建冲突。

### I-VIEWS — 原配置上的窄角色/完整视图与 buckets 接口

- Wave：W-STRUCTURE；prerequisites：I-JOINED（共享测试/metadata 串行）。owner：依赖边界实现者。
- 唯一终态：选择/继承只需其原配置和明确输入，旧转换、remap 与发布仍可消费同语义的惰性视图，没有角色/身份或 producer 变化。
- 写范围：TargetCompilation 的 role/unmapped 派生、TargetHandler 的 addDependencies/配置函数、必要小函数与 tests/fixture。四配置保留，不重写选择算法。
- 证据：Plan G3 的原 C/R 引用/完整成员、主辅助交集 `(id,variant)`、buildDependencies、原 classifier/project classes/resources/NO-SOURCE、用户追加与 scopes；四配置创建时点及继承/attributes/native view，R 不用 C 的并集替代。读取完整 raw C/R 不触发 clean 闭环。
- 冲突：TargetCompilation/TargetHandler/fixture/metadata 被下游继续使用；I-SOURCESET 等待本项，I-COMPOSE 另需本项的实际窄接口结果。

### I-SOURCESET — 目录、sync 与归档职责可独立调用

- Wave：W-STRUCTURE；prerequisites：I-VIEWS（TargetHandler/fixture/metadata 写冲突）。owner：SourceSet 边界实现者。
- 唯一终态：现调用顺序不变的目录、sync 注册、classifier 边界分离，旧消费协议仍工作，改变目录/classifier 不再穿透 sync。
- 写范围：CompilationInternal.configureSourceSet 的分离函数及原 TargetHandler 接线、针对性测试。sync 内部换新 consumption 属于 I-SYNC，不在本项。
- 证据：原 SourceSet 引用、Java/resources/Kotlin（含插件迟到）目录、非 test feature 与 Jar classifier、classes/resources、SourceSet/sync→buckets→公共操作/编译/Linkage 顺序；开关和 sources/javadoc 选项原生效时点不变，代表变化审查。
- 冲突：CompilationInternal/TargetHandler、共用 tests/metadata 与 I-COMPOSE/I-SYNC 串行。

### I-AW — AW 机制显式输入

- Wave：W-STRUCTURE；prerequisites：I-FABRIC、I-JOINED、I-VIEWS（原料/家族及 unmapped 输入），I-SOURCESET（共享源码/fixture/metadata 串行）。owner：AW 边界实现者。
- 唯一终态：AW 以显式 named 形状、独立 primary、原 C/wideners/output/remap helpers 返回保形 awGames 和原任务句柄，旧总函数仍能消费，机制不导航整个 target/Info/compilation。
- 写范围：TargetCompilation 现 AW 注册正文、必要窄函数与当前旧调用接缝及 tests/fixture；原 setupFiles→旧总函数暂保唯一调用，原 runtime 和 preview AW 位置/次数不变。
- 证据：Joined 用原 primary；Split 用原 primary/other 注册，primary Provider 惰性将两输出映射回 common/client。解析前切换 primary 时核对输入/输出/producer 角色随之变、任务数/名字/目录规则不变，不匹配求值失败；禁止按 common/client 重命名 AW。各片自身 preview AW、runtime target AW/独立 client main 原 AW、namespace 条件 helper、AW/file producer 首产与关闭保留分别验证。未迁 preview/runtime/sources 仍消费同结果，临时旧边界适配不新增注册；受影响可见 JVM 入口保原 owner/descriptor。
- 冲突：共享 TargetCompilation/fixture/metadata；I-PREVIEW 在本结果上串行，失败不重做平台注册或独立 SourceSet 边界。

### I-PREVIEW — 预应用与清理机制显式输入

- Wave：W-STRUCTURE；prerequisites：I-AW（明确两片 AW 与原 producer）；owner：预应用边界实现者。
- 唯一终态：旧总函数用显式 C/角色、awGames、side、enabled 等调用单一 preview，后者以同形 Mixin TaskProvider 配对 output/独立 records/strippedGames，集中 S/Strip/C state，不导航整个 target/Info/compilation。
- 写范围：MixinPreapplication 与旧总函数实参/必要兼容接缝、tests/fixture；不改变 Common/sync 或 FinalJarTasks 布局。
- 证据：named→awGames→tasks→output/records→strippedGames 按角色保形、同任务配对；SERVER 仍静态 Split 注册，仅 common 进入有效游戏/operations/records；CLIENT 查找/编译 client→common、operations/records common→client，禁止 values/reversed 插序协议。真实 cold file/Maven、独立记录位置/内容、native producer/项目身份、后配开关/side、空 namespace 与失败阻断；C state/clean transform 保同一边界、无 raw/clean 环。旧 sources/编译仍消费同输出，受影响旧入口/签名单向投影同组结果，不再注册；形状不代表阶段已生产。
- 冲突：与 I-SOURCES/I-COMPOSE 共享 setup/fixture/metadata，显式串行。

### I-SOURCES — sources 注册显式输入

- Wave：W-STRUCTURE；prerequisites：I-PREVIEW（preview 输出与原消费准备句柄），I-AW（关闭 AW 输出）；owner：sources 边界实现者。
- 唯一终态：反编译只收已选 input/有效 libraries/preparation 与原输出，旧总函数用最终开关及独立惰性 primary 选择；sources 机制无需 GameShards/side/records/target 知识。
- 写范围：旧总函数内 Decompile 抽取、装配选择及 tests/fixture；不提前迁整体 setupFiles 或新建通用 pipeline。
- 证据：真实 on/off source-only 首产/失败阻断，Split 的未分割 CLIENT+COMMON 与独立 client CLIENT、SERVER common、Joined 分别观察；primary 合法后配改变 sources input/另一有效片 libraries/输出规则但不重注册，不匹配失败；自身 AW 与关闭原 producer、原选项时点不变。仅有效游戏作 libraries，不将 SERVER 未选 client 静态句柄泄漏；形状句柄不能替代实际成员/字节。输出名/libraries 改变不改 AW/preview 签名。
- 冲突：setup/fixture/metadata 与 I-COMPOSE 串行；此阶段使用当前明确 producer 输入，统一 consumption 在下一边界建立后仅改装配实参。

### I-COMPOSE — 唯一组合与关联消费准备

- Wave：W-STRUCTURE；prerequisites：I-FABRIC、I-JOINED、I-VIEWS（显式原料/家族/选择输入），I-SOURCESET（分离接线），I-AW、I-PREVIEW、I-SOURCES（已收窄的机制）。owner：组合边界实现者。
- 唯一终态：setupFiles 在原构造点直接组合窄 AW、preview、相关有效选择、consumption/preparation、sources；内部 named/AW/tasks/Strip 保形，Java/Kotlin/sources 接同一准备，旧出口投影同结果，runtime/发布保原输入。
- 写范围：TargetCompilation、MixinPreapplication、必要纯数据/窄函数、TargetHandler 编译接线与 tests/fixture；保 FinalJarTasks/旧实际 JVM owner/descriptor，必要访问口只读同结果。移除候选内部逐阶段 Map 接缝，保必要外部兼容入口，不复制注册。
- 证据：Plan G1–G3/G6/G8 及适用 V1/V3–V6；真实 cold file/Maven native producer、Java/Kotlin/sources 实际文件、on/off 与解析前双向 enabled/side/primary 后配；覆盖 SERVER Split 静态与单片有效、CLIENT 两种次序、独立 records、CLIENT+COMMON、Joined、client:data 自身 preview/main runtime AW。对照 primary/other 任务数/名字/目录/输出与惰性角色，阶段 producer/字节由实际运行证明。核对旧 String 返回原标签，Map 合法/非法/未知角色及形状不匹配失败、Info 旧调用、FinalJarTasks/生成接口/protected setupFiles/getters、实际 JVM owner/descriptor，完整 operations 兼容出口保原协议且一次注册；不只看 Property/dry-run 或改 fixture 期望。
- 有效 preparation：完整有效非项目 C 原生 files/Transform 依赖 + 有效有序 games + 条件 operations；off 直接空必要 operation 且不求值本轮 operations Provider，不安装私有 state，仍保原 AW/remap/外部 producer。sources 首产失败或需配置期读文件则本谓词不成立，不借后验掩盖。
- 前证据接续：I-AW/I-PREVIEW/I-SOURCES 在旧总函数中间态产证；本项更换实参/相关选择/preparation 后，终态前逐一核对 named/awGames/tasks/strippedGames 配对、primary/other 与有效有序游戏、独立 records/state、sources input/libraries/producer、Java/Kotlin 准备和旧出口。取得适用于直接组合的首产、后配与失败阻断证据，并说明本项哪次运行覆盖；未变机制内部清理/保护可沿用，旧调用成功不能证明新连接。平台时序、角色、目录及旧兼容证据只在实际输入/连接/环境仍匹配时沿用，具体失效由本项补，不因共文件一律重跑。
- 冲突：与 Common/sync 共享消费合同、fixture/metadata；I-COMMON 等待此实际结果，失败/未完成不允许下游按假接口继续。

### I-COMMON — Common stub 统一消费投影

- Wave：W-STRUCTURE；prerequisites：I-COMPOSE（已建立的消费协议）；owner：Common 消费边界实现者。
- 唯一终态：Common 按原 selector 取得参与 compilation 后，只从同一消费结果取得 artifact/component、已选有序游戏及完整 preparation，不解释 GameShards/primary/side/records 或重建开关/任务协议，输出与 fallback 保原语义。
- 写范围：CommonTargetCreator 的 stub 接线与必要 tests/fixture；只因实际相接需要才改其它 Common 连接，不机械改 Linkage/run。
- 证据：两目标的共有注入 API、NO-SOURCE/project/variant 身份、无虚构游戏 component；关闭 extra→primary、混合启用参与者、额外片与原项目 producer，操作集合不扩到同 target 另一侧，真实 stub 输出。承接 Plan **全部 G5** 作者观察：原 Linkage compile/runtime/include/constraints/AW/Mixins/outgoing、data WITHOUT_DATA/资源与 test output 追加、Common static source linkage；保留成组关系，不因本次只改 stub 而略过相接回归。
- G5 选择方法：在现 stub/identity fixture 上补有界配置观察，以 run 为 Common 参与者策略的对照，不启动游戏。独立 client 已存在且 target data/test 已有、client 对应 secondary 缺失时，观察 Common client:data/test 取 client、run clientData/clientTest 取 target secondary；随后在解析前配置 client secondary，再读取原参与者 Provider 与 run 的 SourceSet Provider，核对均改取它。查询前后检查未自行创建缺失具体成员；另用隔离的缺输入构建确认 run 所需 secondary 最终仍缺时在原 afterEvaluate 检查报出对应原因，非任意非零退出。保留已有/迟到两路径；不改 run selector、不修旧 Common 环或 run TODO。上述选择/失败观察共同证明本项“原 selector → 同一参与者消费投影”的等价性，不形成独立 run 迁移结果。
- 前证据接续：本项改参与者投影/preparation 后，自己补足 stub 首产/关闭/故障及 G5 选择证据；I-COMPOSE 的 Java/Kotlin/sources 证据在共享协议、输入与环境未变时沿用，若本项确改该共享边界则在本项终态前补受影响消费者检查。G5 及本项适用性结论随本项 Result 供 I-SYNC 和 gate 消费。
- 冲突：Common fixture/全模型与共用 tests/metadata；I-SYNC 等待本项以避免同时改写同一完整消费模型验证输入。

### I-SYNC — sync 接入完整消费准备并保持 runtime 独立

- Wave：W-STRUCTURE；prerequisites：I-COMPOSE、I-SOURCESET（新协议与分离入口），I-COMMON（共享全模型 fixture/metadata 串行）。owner：sync 消费边界实现者。
- 唯一终态：sync 在原执行位置消费已选有序 compile/games 与 preparation 并物化实际文件，不解释 GameShards/primary/side/records；runtime/可选 sources 独立保留，真实 Tooling 同请求看见正确模型。
- 写范围：CompilationInternal 的 sync 函数及 tests/fixture/helper 的必要观察；不得修改真实模型语义或把安装覆盖塞进 fixture。
- 证据：Plan G7 的独立 sync 与真实 Tooling、完整节点/关系/index/scope/variant/raw→clean 对照、关闭完整 producer、compile/runtime 隔离、sources 开关；`model(...).forTasks(sync)` 内准备先 builder，实际故障阻断及撤故障恢复。不得用手工先跑 task 再另发模型请求代替。
- 前证据接续：I-SOURCESET 分离后保留的旧 sync 在此换接消费协议；本项终态前以自身 sync/Tooling 运行覆盖新 compile/games/runtime/可选 sources 连接及准备/参数读取时序。目录/迟到 Kotlin/classifier 原证据在注册位置、SourceSet 身份与输入未变时沿用；若触及 wrapper 调用顺序、注册位置或共有输入，补对应目录/classifier/身份/时序观察。I-COMPOSE 的编译/sources 和 I-COMMON 的 stub/G5 证据在该共享边界及测试来源未变时沿用；若本项的协议、fixture/helper 或 metadata 改动影响其输入/执行条件，则由本项补对应检查，不能把 sync 或模型成功扩展成这些消费者成功。
- gate 输入汇合：作为最后消费接线者，本项 Result 汇合已有作者证据对 G-STRUCTURE 所需覆盖及当前候选的适用性，明确沿用、被本项覆盖及实际补验的部分；G5 的观察原产者为 I-COMMON。这里只检查/消费已有结论并判断本项改动影响、做自己的必要受影响验证，不追加第二个全局实现验收/独立研究终态，不接管无关旧缺证或重跑全部 G1–G8/V1–V7。所需作者证据缺失、失效未补或关联不清时报告具体 gap，本项不能以“待 I-ACCEPT 补”满足终态。
- 冲突：同源 metadata/测试私有写在本项结束并留明确实际输入状态后才由独立 I-ACCEPT 使用。

### I-ACCEPT — 独立整体交付接受结论

- Wave：W-ACCEPT；prerequisites：I-SYNC（最终候选及证据；其余实现由 G-STRUCTURE 统一等待）；owner：未参与实现的独立验收者。
- 唯一终态：对指定候选和指定本地环境，独立判断 Plan 全部成功条件是否满足，提供可采纳的有限接受结论；不修实现、不推进图状态、不宣称发布就绪。
- 效果范围：生产代码只读；允许本次独立测试/同源编译产生必要私有 build、metadata、报告及 fixture 运行输出，不改产品依赖/config。验收不并行占用任何实现构建输出。
- 证据：读取实际实现/连接与适用原始作者证据，核实 G1–G8 / 受影响 V1–V7 的 coverage；复用仍匹配的检查，按独立接受所需运行尚未覆盖或不再适用的 cold/后配/Transform/各消费者/故障恢复/运行发布隔离检查，逐项指出实际观察及限制；代表变化审查证明跨层知识实质减少。至少新候选的真实运行证据存在，历史 XML 不能替代。此项不承担 G-STRUCTURE 前必须由作者取得的缺证/失效补验；若独立核实发现该缺口，报告它对既有证据及接受条件的影响，不能用本项补测或 verdict 倒填前 gate。
- 失败处理：必要条件有缺口不能给接受 verdict；报告具体受影响谓词、已完成/未知输出与安全重试边界。修复需要另有 lawful outcome/图处理，验收者不自行转为实现或重写已提交 Result。

## Waves 与 gates

| Wave | 增量 outcome | Members（各出现一次） | 前置 Wave gates | Release gate |
| --- | --- | --- | --- | --- |
| W-STRUCTURE | 十个可保留的迁移终态已组成最终候选，并有作者定向验证 | I-FABRIC, I-JOINED, I-VIEWS, I-SOURCESET, I-AW, I-PREVIEW, I-SOURCES, I-COMPOSE, I-COMMON, I-SYNC | 无 | G-STRUCTURE |
| W-ACCEPT | 独立核实最终候选的全局组织与受影响行为等价，并限定结论 | I-ACCEPT | G-STRUCTURE | G-ACCEPT |

**G-STRUCTURE（当前未判定）**：所有十个 item 的原始完整 Results 可关联且均为 completed，并有对应实际 diff/编译或运行观察证明其独立谓词；验证引用适用于最终候选，后续修改使前证据失效的受影响检查已由改变该边界的作者在其终态前补足。消费 I-COMPOSE 的机制到直接组合证据、I-COMMON 的 stub/全部 G5（含不同回退、迟到 secondary、不自动创建及缺 run 输入原失败）证据，以及 I-SYNC 对自身接线的证据与最终适用性汇合；其它作者证据仍须按实际改动判定适用，不能靠汇合摘要替代必要原始证据。核对唯一组合、Joined/Split 跨阶段/任务配对、独立 primary/两种次序、primary/other 任务身份与路径、家族/构造时序、目录隔离、完整消费准备、Common/sync 不扩散形状知识、旧 Map/String/JVM 兼容和无 runtime 泄漏的实际连接与定向证据；形状或 completed 标签不替代 producer/字节/消费。gate 只判断已经获得的这些作者证据，不隐含测试执行者；缺失或失效未补即不释放，不能等待 I-ACCEPT 为此前 gate 产证。成功只开放 W-ACCEPT 的前置 gate 条件，不等于总体接受或额外授权。

**G-ACCEPT（当前未判定）**：I-ACCEPT 完整 Result 为 completed 且独立有据的接受结论覆盖 Plan 表中所有适用义务；真实首次物化/后配/失败恢复与完整模型证据充分、代表变化面成立、无未解决阻断。必要局部未验证不靠“有限风险”豁免；Plan 明确排除的完整 ABI/最低版本/缓存/最终 IDE/正式发布仍在结论中保留。它建立 Plan 的整体 outcome；不授权 CI/发布/提交或自动续接其它工作。

全图的可行联合拓扑：I-FABRIC → I-JOINED → I-VIEWS → I-SOURCESET → I-AW → I-PREVIEW → I-SOURCES → I-COMPOSE → I-COMMON → I-SYNC → G-STRUCTURE → I-ACCEPT → G-ACCEPT。额外 item 前置仅为上述链上的向前引用，G-STRUCTURE 依其全部 members，G-ACCEPT 依 I-ACCEPT；任何 gate 都不被其自身 member 作为前置。两 Wave 仅在独立接受边界切开，没有额外全局 barrier。共享只读 authority/源码不构成冲突，串行因明确共用源码/fixture/metadata/编译输出副作用，不能以私有 TestKit Home 推断编译端也可并行。

## 执行前及维护边界

每一未来 item 仍需 Router 对当前 tuple/authority/确认 scope/真实 prerequisites/gates/写冲突及工具条件做当次/JIT 检查。C4 尚无；原始 Result 的首次采纳必须完整同源读取并可靠关联，文档摘要不替代该义务。partial/failed/blocked、缺失或不可采纳 Result 均不释放等待。

本次初始图写权限不自动授予未来 C5 维护；material 变化保留受影响边界复核与 C6。终态 Result 不可恢复重写；后继恢复身份必须只处理剩余谓词、保已完成/可能发生/未知副作用，不 replay 完成平台或消费迁移。没有历史 tuple 继承、重命名或 gate 复用。

持久记录只保后继 scope/方法/覆盖/限制与已核可读仓库相对引用；实际文件产生后才引用。不新建 ledger、外部 Result 依赖或历史 build/tmp 永久基线。各 owner 负责本次 invocation 的安全结束及效果报告，无独立清理/收尾授权。

本轮作者自检逐项核对可保留旧消费者终态、充分证据和所有权：共享 Info 接缝与类型只服务平台迁移；AW/preview/sources 在旧总调用下各有独立消费；组合迁移的相关选择/preparation 及必要兼容共同构成唯一新装配；Common 的 run 对照证明参与者策略，sync 汇合只消费证据并验证自身改动，独立接受另行给 verdict。未发现要求增拆合的独立边界，仍为 11 items/2 Waves/2 gates。共享源码/fixture/metadata/编译输出冲突继续串行。

联合检查 item 前置、所属 Wave 前置 gates 及 gate 等待全部 members：上述拓扑覆盖全部等待，无 gate 等待自身 member 的反向释放；membership 恰好一次，引用/匹配互链及 Plan G1–G8/适用 V1–V7 保证对应。本轮已采用的独立静态复核范围是共享 Info 可编译中间态、保形与惰性投影/两种次序、原任务身份、旧 Map/String/JVM 协议及其机制→组合证据接续，并连同既有 Common/sync 适用性责任核对；这些 delta 的新复核无 blocking 或需先纠正的 nonblocking finding，Router 结合旧 review 未变范围已判断计划 ready。本次准备记录不证明动态行为或实际 ABI，不满足 C4，未执行/放行任何 item/gate。
