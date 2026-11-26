# Target / Compilation 解耦 Waves

[Work](work.md) · [匹配 Plan](plan.md) · [Context](context.md)

这是本 tuple 唯一执行图。原 12 项与 G-STRUCTURE/G-ACCEPT 保持旧候选的已采用 bounded acceptance；历史 blocked/partial/failed 与未知效果不改写。指定内部清理已经执行并留下 terminal partial；本次保留其历史，将未完成边界接到新恢复身份，再安排保持覆盖的正常测试集中及最终独立修正审查，三个结果分别可观察、各一 Handler/Result。本次只维护文档并作作者自检，不执行它们或释放 gate。

## 原迁移分组依据（历史）

原料存在可由旧家族直接消费的句柄边界，因此 Fabric 与 joined 分别完成/失败而不回滚对方。依赖查询可由旧转换消费；SourceSet 职责分离可由旧 sync 消费；Common 与 sync 可分别接新 preparation 并保留旧出口；这些是独立可保留/重试的终态，分别成项。

组合项继续检验了 AW、preview、sources 的独立边界：现旧总函数能调用已收窄的机制函数并保留其它原正文，因此每部分都能由旧调用者消费，并在下一机制失败时保留，不必重复注册已完成部分。初稿将三者仅视为组合 helper 的分组据此细化为三个 item；验收是机制不再导航整个 target/Info/compilation 且原调用正常，而非函数数量。预应用机制内部 S/records/Strip/C 状态仍因同一 raw→clean 合同和环路边界保持一起。最终组合项才切断候选的旧总入口并集中消费准备。Common 与 sync 也有独立旧消费者及迁移边界，分别保留。

每项一 Handler、一终态 Result；表中的验证是证明其谓词，不是第二个“独立验收”结果。最终验收归不同于实现作者的独立 owner。无人可把实现与该 verdict 同项完成，也不建立无实际 consumer 的证据/ledger item。

初始准备时按实际源码重检的形状迁移切口为：共享 Info 目前是 Map/String，Fabric 与 joined 共用构造，旧转换及 client secondary/fixture 仍读取旧出口。因此 I-FABRIC 的形状和兼容接缝必须让未迁 joined/旧调用继续编译/消费；I-JOINED 再接 Joined，I-AW/PREVIEW 分别把 named→AW、AW→tasks/Strip 改为保形，I-SOURCES 只收独立 primary 已选结果，I-COMPOSE 关闭候选内部 Map 接缝。纯数据定义、兼容与针对性字节/旧值核查只服务其迁移谓词，不能独立宣称有业务消费价值或拆出第二 Result。后项失败可保前项原句柄/旧调用中间态，仅恢复剩余边界；不新增重复注册链。Common/sync 只知有效有序游戏/preparation，不学 GameShards。这支持原 11 个单终态 items，后经 I-V67-HARNESS 恢复形成原 12 items、2 Waves/gates。上述旧接缝与保签名要求只解释历史迁移，不再约束本次已取消兼容的入口。

作者项若改变前项已验证的接口、连接、输入或验证环境，须在**自身终态前**沿实际 diff 判断受影响证据：仍适用而沿用、已被本项运行覆盖、或须定向补验，并实际完成后一类必要检查。结论及覆盖/方法/限制随本项 Result 交付，不回写前项终态、不另建账本。只补具体失效或缺证；未变且仍匹配的行为/输入/环境可重用。补验仅用于证明本项迁移与原消费者等价，越出本项写范围的缺口应报告而不暗中修复，也不能交给前置 gate 后的 I-ACCEPT 生产证据。

## 原 12 项（历史谓词及终态保留，不重开）

以下原 item 正文保留用于完成事实、证据和旧失败的关联，不作为重新执行或恢复已取消 Map/String/synthetic/preview 兼容的命令；当前义务以最新 authority、Plan 及下方增量 items 为准。所有 IDs、prerequisites 与旧两 Wave/gate 关系保持，正文顺序不产生其它依赖；旧恢复项的仅验收权限不限制已确认新清理的独立写范围。

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
- 冲突：同源 metadata/测试私有写须结束并留明确实际输入状态，才由 I-V67-HARNESS 使用；独立接受再等待恢复和 G-STRUCTURE。原接线完成及后续汇合事实见 Context，缺口由新恢复项承担，不恢复本项旧终态。

### I-V67-HARNESS — 最终候选 V6/V7 作者证据恢复

- Wave：W-STRUCTURE；prerequisites：I-SYNC（最终候选、自身观察及已完成的适用性汇合；其前置链涵盖其余九项）。owner：此有界作者恢复的执行者，不兼任独立接受。
- 已采用结果：I-V67-HARNESS Result 为 `completed`；Router 已采用其最终候选 V6/V7 作者证据及适用性结论。该结果不单独释放 gate，证据范围和限制见下方 gate 记录及 Plan。
- 唯一终态：对指定最终候选及获准本地环境，V6/V7 剩余必要作者证据已闭合，并关联仍适用的组件/阶段观察与新增实际验证，使 G-STRUCTURE 能判断证据充分性。harness 编写、定向运行及适用性汇合只是证明该谓词的手段，共享输入、权限和恢复边界，不单立工具交付或独立接受 verdict。
- 写范围：仅 [现有测试](../../../src/test/kotlin/earth/terrarium/cloche/MixinCompilationTests.kt)、[fixture 目录](../../../integration-test/mixin-compilation/) 内必要验收输入/断言/观察 helper，以及本次新私有 build/metadata/Home/报告。执行前先读实际相关正文，保留既有断言。不改生产实现/行为、authority、正式依赖、wrapper、共享缓存、旧脚本/结果，不推进图/gate，不发布或提交。
- 方法：先 fresh 检查候选与已采用证据的实际来源、内容、输入、环境和消费者；沿 Plan 的私有同源编译/默认 Runner 与 worker 来源方法核实冻结 Codev、Kotlin/KSP/Java/Test/metadata 和 code source。保留未变十项的具体沿用范围，只补变化/不确定或缺证部分，不把所有旧测试强制重跑。
- V6 证据：复用真实 remap/refmap、已 remap 无 refmap selector 共用默认 tuple 和 direct/no tuple 的编译消费入口。必要新增 refmap-only/chosen 有效显式 context，通过现有 Task API 证明实际预期 API；missing context/namespace 必须以对应原因阻断消费，撤错恢复。不能给混合 default/no-refmap 输入强加 context，不能靠属性字符串或 prepareFinalAcceptanceInputs 成功替代引擎/消费证据。
- V7 连续证据：新私有 fixture 内同一受控 workspace 按 baseline→单变量变化→恢复观察 source 内容/配置/成员/完整位置、有效 AW 内容、side、mapping/context 的 producer outcome、P/N 与最终实际消费。mapping 文件内容变化要用有效内容和输出/member 观察，历史 sourceNamespace 参数变化只支持其自身失效。正常复跑及不同实际进程中检查必要 producer、remap/include 与顺序清理的恢复；新 Home 不证明新进程，选择器 workspace 复用不称 FROM-CACHE。冷首产检查另用无旧 workspace/P/N 的私有 Home，不预跑 operation。
- V7 关联与保护：逐项关联零记录失败、合法空记录副本、缺配置/解析/I/O、独立 records 内容/顺序/同 File 去重、首后步清理、原输入及后份记录保护、撤故障恢复；组件生命周期穷举仍归 Codev。以实际最终输入清单、记录来源/顺序、当前委托路径与冻结实现核对组件证据的适用性，缺必要集成观察时补最小快照/故障断言。预检失败保留旧输出不能被解释为消费者成功回退；不得消费旧/临时结果掩盖失败。
- 完成证据：提供所测候选/本地环境、可重现入口和实际原始观察，明确“沿用/本次覆盖/仍缺”以及 harness 改动对既有四消费者证据的影响；必要条件仍缺即本谓词未满足。四消费者的既有首产/失败传播和真实 Tooling 时序按匹配范围沿用或定向补足；保留 standalone sources=true 为 common-only 的限制，不据此发明全矩阵。G-STRUCTURE 需要本项证据，I-ACCEPT 仍独立核实，二者不互相倒填。
- 冲突与安全：与 I-SYNC、其它实现/构建及独立接受共享测试、fixture、metadata 和编译输出，必须串行；私有 TestKit Home 不证明编译端可并行。开始前查明相关旧输出/进程的可能效果，仅在新私有根执行，禁止原样重放写入旧目录的状态脚本。生产缺陷或需改语义时停止受影响工作并报告原因/已完成或未知效果，不暗修生产、不恢复任何旧终态 Result。

### I-ACCEPT — 独立整体交付接受结论

- Wave：W-ACCEPT；prerequisites：I-SYNC、I-V67-HARNESS（最终候选及恢复作者证据；其余实现由 G-STRUCTURE 统一等待）；owner：未参与实现的独立验收者。
- 已采用结果：新的独立 I-ACCEPT Result 为 `completed`；Router 已采用其 bounded acceptance，并判定 `G-STRUCTURE` 与 `G-ACCEPT` 满足。原 I-ACCEPT blocked Result 保持历史，不由本项恢复、覆盖或改写。
- 唯一终态：对指定候选和指定本地环境，独立判断 Plan 全部成功条件是否满足，提供可采纳的有限接受结论；不修实现、不推进图状态、不宣称发布就绪。
- 效果范围：生产代码只读；允许本次独立测试/同源编译产生必要私有 build、metadata、报告及 fixture 运行输出，不改产品依赖/config。验收不并行占用任何实现构建输出。
- 证据：读取实际实现/连接与适用原始作者证据，核实 G1–G8 / 受影响 V1–V7 的 coverage；复用仍匹配的检查，按独立接受所需运行尚未覆盖或不再适用的 cold/后配/Transform/各消费者/故障恢复/运行发布隔离检查，逐项指出实际观察及限制；代表变化审查证明跨层知识实质减少。至少新候选的真实运行证据存在，历史 XML 不能替代。此项不承担 G-STRUCTURE 前必须由作者取得的缺证/失效补验；若独立核实发现该缺口，报告它对既有证据及接受条件的影响，不能用本项补测或 verdict 倒填前 gate。
- 历史及身份：原 I-ACCEPT Result 已为 blocked，不得恢复或改写。这里保留独立接受的语义边界与新增等待条件；任何后续接受执行须由 Router 依法关联新的执行身份，不能据本图重开旧 Handler，也不能自动视旧 blocked 已完成。
- 失败处理：必要条件有缺口不能给接受 verdict；报告具体受影响谓词、已完成/未知输出与安全重试边界。修复需要另有 lawful outcome/图处理，验收者不自行转为实现或重写已提交 Result。

## 当前增量与清理历史

已确认取消指定内部兼容，不再研究或重选。清理恢复是原未完成边界的安全结算/补齐；正常测试集中有可单独保留的 driver/fixture 及覆盖保持结果；最终独立审查消费实际最终候选和两者作者证据，有独立 verdict，故分为三项。各项必要 helper/fixture/定向检查只证明本项谓词，不额外创建工具、盘点、耗时诊断或账本结果。

### I-COMPAT-CLEANUP — 原清理 terminal partial（历史，不恢复）

原归属 W-CLEANUP，原前置 I-ACCEPT/历史 G-ACCEPT，原 release gate G-CLEANUP；`cleanup-internal-compatibility/g1` 已有 terminal partial。指定 accessor/Map/String/Info/preview 与专用 Legacy caller 已删除，真实 typed 调用及混合行为断言已迁；生产/测试 Kotlin、typed 单测和 11 个用例报告通过，consumers 批 7/8 通过，sides 的 Provider 包装层身份观察已修而补验未结算。最后 native 运行与现存成功日志不等于最终作者证据闭合；XML/退出状态/快照/metadata/ABI 适用性及可能相关写入仍待结算。详见 [Context 的恢复现场](context.md) 与[原私有目录](../../../build/tmp/compat-cleanup-20261108-a/)。

该旧 item/Result 不 resume、不追改 completed、不重放完成效果；G-CLEANUP 保持未释放历史。W-CLEANUP 与该 member/gate 关系仅在此保留，不属于下表当前必要等待集。新 I-COMPAT-RECOVERY 承接同一必要清理谓词和剩余证据，成功仅供新 G-CLEANUP-RECOVERY 判断，不自动关闭旧 item/gate。原 12 项与原两 gate 的历史不动。

### I-COMPAT-RECOVERY — 未完成清理的安全结算与必要证据闭合

- Wave：W-CLEANUP-RECOVERY；prerequisites：I-ACCEPT（历史有界基线）；owner：恢复作者，不兼任最终独立审查者。旧 partial 是必须消费的恢复事实，不是要求变为 completed 的前置 item。
- 唯一终态：在已发生删除/typed 迁移的当前候选上，已有清理效果与原始证据被安全结算，只补真实缺失或失效部分，使指定内部入口已清除、真实调用与有效断言迁移、公开/明确保留出口及原行为保证均有充分适用作者证据；不以单条成功日志或编译成功代替。
- 写范围：继续原已确认清理的未完成部分，只在实际缺口确有需要时修 TargetHandler、GameShards/Info、TargetCompilation/MixinPreapplication、Fabric/Joined/secondary typed 接缝及现有 GameShardsTests/MixinCompilationTests/相关 fixture；必要新私有编译/metadata/运行输出。不是再次删除全部入口或重写清单。不得改 authority、正式依赖/锁/wrapper/Codev、提案/HTML、旧 Results/输出，不提交/推送/发布或自行回写图/gate。
- 先检查现场：fresh 读取当前 diff/相关正文、原 native 启动/日志/结果与候选/输入快照、metadata/ABI 观察，核对与本候选/输出有关的写入是否结束及实际效果。原 PID 消失不证明无相关写入；不要求全机器无进程。native 的成功行只供定位，须关联完整 XML、失败/通过方法、退出状态（取得不到则明示，判断其余证据是否充分）与所测候选/来源，不能伪造退出码或盲重放批次。原目录只读，不运行原样旧状态脚本/覆盖结果；汇总脚本先读实际输入/写入与安全性，再仅在获准的新位置适配使用。关联或安全性不明即停止受影响采用/重试并报告。
- 已有证据先结算：保留通过 11 项、consumers 7 过 1 失败、fixture 修正和 native 可能完成的区别；按源码/输入/来源/环境/消费者适用性沿用，只补 sides 修正验证、未完批次/出口/证据汇合的真实缺口。保 CLIENT libraries 底层对象身份及其它情况为空，不修生产 libraries 或拿默认发布 Codev 两处 `.from` 问题换依赖；不得重跑已完成构建来替代读取现场。
- 来源与行为证据：沿 Plan 私有同源方法核对唯一 Mixins identity、冻结 SHA256、新 `ConfigurableFileCollection getRemovalsFile`、实际 code sources、编译/Test/metadata 及其它条目/顺序/builtBy。实际生产/测试编译、typed 形状/独立 primary/不匹配失败、角色/native producer/平台装配懒值及已有/迟到/后配、FinalJarTasks 五字段/生成接口/getters/protected setupFiles 与相接消费者按实际影响结算或补足；NO-SOURCE 不称有源 Java 编译。保 joined/common/client 字符串实值、records/顺序/输入保护、四消费者与 runtime/raw/clean 隔离。未变行为可适用沿用，不强制全历史矩阵/V6/V7 或完整 ABI 重跑。
- Result：给出恢复前已知/未知效果、现存运行结算、必要新增运行与安全边界、实际候选/来源/输入/环境/可读原始输出，以及沿用/本项覆盖/仍缺和保留断言。必要证据缺失/失败就不满足本项，不能留给测试集中或最终审查补作者缺证。出现需改产品/authority/Codev 或无法安全继续的缺口则停止受影响边界，保留效果事实。
- 冲突：与测试集中共享 MixinCompilationTests/fixture/metadata/编译输出及被审候选，必须全部写入/运行结束后串行；私有 TestKit Home 不证明编译端可并行。共享只读 authority 无冲突。

### I-TEST-CONSOLIDATION — 保持覆盖的正常组集中

- Wave：W-TEST-CONSOLIDATION；prerequisites：I-COMPAT-RECOVERY（已结算清理候选及有效断言基线）；owner：测试集中作者，不兼任最终独立审查者。具体实施 scope 已经独立计划复核、Router 完整披露及用户确认，本图只记录该事实，不授予新权限。
- 唯一终态：现有 driver/fixture 在相容正常条件下共享 prepare/ProjectDir/Home，实际减少重复准备与内层调用，同时有效断言和必要特殊入口保证未丢、无状态污染，并提供可核对的实际调用次数及耗时方法。
- 写范围：仅现有 MixinCompilationTests、integration-test/mixin-compilation 内必要 root/:populated/:empty 配置/probes/helper 与私有运行安排；生产、DSL、正式依赖/锁/wrapper/Codev/authority 不改。不新建通用 mode 框架、永久矩阵或身份/hash/耗时账本，不 stage/commit/push。
- 正常组：复用现有模板，以具名 target/probe、fixture 局部 verifyMatrix 或普通多任务调用聚合相容的装配、真实 Java/Kotlin 编译、sources 内容、classifier 和普通 sync。每组只 prepare 一次并共享 ProjectDir/Home，避免仅合并 JUnit 却仍复制准备/逐 case build；可用最少几个相容组，不承诺一次覆盖全部。保完整身份/成员/variant/classifier/scope、原生 producer/项目/NO-SOURCE、API/内容/路径/字符串与原混合 typed 观察，不硬编码结果或弱化断言。
- 排除与状态：raw-only 不与 AW/preapply/compile 同调用；source-only、Common preparation、Joined legacy、独立 C/view/SourceSet 等 absence 保持原观察范围，不能由其它同场任务代生产。负编译/配置失败、插件早晚/unnamed 和互斥 mode 分次并验证真实原因；四消费者冷入口各新实例/私有新 Home、无旧 Transform/P/N/清理输出、不预跑聚合/operation；连续变化/故障恢复同 ProjectDir/Home/旧输出内单变量改变再恢复。普通 Home 可共享，不能以每步新 Home 取代恢复，也不能把私有 invocation 误读为每个正常断言独立 Home。
- 真实 Tooling：保既有 helper 同请求 forTasks(sync) 准备→builders→strict views、完整模型/index/闭包/scope/清理文件、真实故障阻断和撤故障恢复；普通 sync 不替代。Driver/fixture/helper/metadata 的实际变化决定哪些冷/absence/Tooling/恢复证据失效需要定向补，未变适用证据可沿用，不重跑全历史矩阵。
- 作者证据：在本项 Result 直接对应原有效断言到新 probe/方法/运行或适用沿用，列出保留的独立观察与特殊入口补证、组内输出及无污染依据。记录实际 prepare、内层 GradleRunner build/buildAndFail、Tooling 请求次数、任务参数/输入/原始输出及外层运行；prepare、每次 Gradle/Tooling 和外层 wall time 的起止方法/冷暖缓存/环境明确。合并前按同一有效断言范围从清理完成的 driver/fixture 核对调用结构（静态推导与已有实测区分），再对照候选实际次数，不强制重跑旧全套取得基准；无旧逐方法耗时基准不伪造提速百分比。没有实际重复调用收敛或覆盖/隔离缺证均不满足本项。作者在自身终态前补足受实际变化影响的保证，不交给独立审查。
- 来源及冲突：保持 Plan 的同源编译/metadata/Runner/worker/helper 来源与本地开发限制，沿最新恢复候选验证。与恢复/最终审查共享候选/测试/fixture/编译/metadata 副作用，串行；运行结束、完整作者证据与最终候选确定后才具备最终审查输入。生产缺陷超出本项写范围，须报告，不顺手修复或改语义。

### I-CORRECTION-REVIEW — 最终候选的独立只读修正审查

- Wave：W-CORRECTION-REVIEW；prerequisites：I-COMPAT-RECOVERY、I-TEST-CONSOLIDATION（实际最终候选和两项完整作者证据）；owner：未参与这两项实现的独立审查者。
- 唯一终态：对清理及测试集中后的整体变更提供有证据支持、可采纳的独立修正审查结论，明确与当前 authority 的一致性、真实 typed 调用及保留行为/覆盖是否充分。review-task completed 仅说明审查完成，否定结论或必要缺证不表示接受。
- 效果范围：生产/测试/fixture/四文档/authority 只读；独立必要观察只用新私有输出。不修代码/图、删断言、换依赖、回写旧 Result 或提交/发布，不自动重跑所有历史检查。
- 必要证据：读取实际最终整体 diff、相接实现/callers/保留接口/tests/fixtures 和当前 ADR/Spec，核对两项作者原始输出与实际候选/来源/输入/消费者适用性。检查取消入口是否遗留、typed/形状/primary/唯一 setupFiles、角色/producer/装配时序/四消费者/records/输入保护/隔离；特别核对合并前后有效断言对应、重复 prepare/调用收敛、冷/absence/连续恢复的状态寿命及真实 Tooling 仍独立成立。不得以正常聚合成功代替特殊保证或重新调查旧内部兼容必要性。
- 缺口与结论：定位 finding、影响与代码/原始观察/未验范围；只按实际影响补必要独立观察，不用本项运行倒填 G-CLEANUP-RECOVERY/G-TEST-CONSOLIDATION 作者义务。作者缺证即报告，审查不能转为实现；已取消旧内部 ABI 不作为必须恢复条件，公开/明确保留出口仍受保护。
- 冲突：仅在两项作者写入/运行结束、其 gates 满足且候选稳定后审查；不得并发修改被审候选或共用输出。漂移时限定结论并报告，不自选新候选。与所有历史验收一样，不证明正式发布/完整 ABI/最低版本/最终 IDE。

## Waves 与 gates

下表是当前必要等待集；原 W-CLEANUP/I-COMPAT-CLEANUP/G-CLEANUP 仅保留上文历史，不能因旧 partial 永久阻断新恢复，也不能把它改称 completed。当前 items 每个归属一次，每项一个 Handler/Result。

| Wave | 增量 outcome | Members（各出现一次） | 前置 Wave gates | Release gate |
| --- | --- | --- | --- | --- |
| W-STRUCTURE | 历史十项迁移及 V6/V7 作者恢复；Router 已判定满足，限于旧候选 | I-FABRIC, I-JOINED, I-VIEWS, I-SOURCESET, I-AW, I-PREVIEW, I-SOURCES, I-COMPOSE, I-COMMON, I-SYNC, I-V67-HARNESS | 无 | G-STRUCTURE |
| W-ACCEPT | 历史独立 bounded acceptance；Router 已判定满足 | I-ACCEPT | G-STRUCTURE | G-ACCEPT |
| W-CLEANUP-RECOVERY | 清理剩余证据与效果安全结算/闭合，尚未执行 | I-COMPAT-RECOVERY | G-ACCEPT | G-CLEANUP-RECOVERY |
| W-TEST-CONSOLIDATION | 正常组集中、覆盖保持与次数/耗时证据，scope 已确认、尚未执行 | I-TEST-CONSOLIDATION | G-CLEANUP-RECOVERY | G-TEST-CONSOLIDATION |
| W-CORRECTION-REVIEW | 最终候选独立修正审查结论，尚未执行 | I-CORRECTION-REVIEW | G-TEST-CONSOLIDATION | G-CORRECTION-REVIEW |

**G-STRUCTURE（历史，Router 已判定满足，限于旧候选）**：原十项实现及 I-V67-HARNESS 的 completed 结果/作者适用性证据已采用，I-SYNC 八测试/汇合与 I-COMPOSE/I-COMMON 按匹配范围沿用。唯一组合、纯数据跨阶段配对、primary/次序/任务身份、家族时序、目录、完整消费/Common/sync、当时旧兼容与运行隔离得到指定本地范围支持；不倒填 I-ACCEPT 或抹除旧失败/未知。证据入口：[V67 报告](../../../build/tmp/v67-recovery-20261108-d/REPORT.md)、[observations](../../../build/tmp/v67-recovery-20261108-d/checked-observations.json)、[I-SYNC checks](../../../build/tmp/i-sync-evidence-recovery-b89893880337/model-checks/checks.json)。方法/限制见 Plan 与 Context，不证明新候选。

**G-ACCEPT（历史，Router 已判定满足，限于旧候选）**：新的独立 I-ACCEPT completed Result 已采用；当时 G1–G8/适用 V1–V7 的 bounded acceptance 保持，不扩成正式来源、完整 ABI/最低版本/平台矩阵、configuration/远程缓存、性能或最终 IDE。证据入口：[model comparison](../../../build/tmp/i-accept-review-85b722068e/model-comparison/checks.json)、[review evidence](../../../build/tmp/i-accept-review-85b722068e/review-evidence.json)、[finish check](../../../build/tmp/i-accept-review-85b722068e/finish-check.json)。不授权新工作/发布，不关闭新 gates。

**G-CLEANUP-RECOVERY（未判定）**：W-CLEANUP-RECOVERY 唯一 member 有可采纳 completed Result；Router 完整读取/关联并核实旧效果安全结算、实际删除/typed caller/有效断言迁移与公开/保留行为的必要聚焦作者证据充分，来源/候选/输入/环境匹配，未解决清理缺口为零。此 gate 承接当前必要清理谓词，由 W-TEST-CONSOLIDATION 等待，最终审查也消费其证据；不要求旧 partial completed、不释放旧 G-CLEANUP 或代表最终审查接受。

**G-TEST-CONSOLIDATION（未判定）**：G-CLEANUP-RECOVERY 满足，W-TEST-CONSOLIDATION 唯一 member 有可采纳 completed Result；Router 完整读取/关联，正常组的原有效断言对应、实际 prepare/Gradle/Tooling 次数及耗时方法、必要受影响冷/absence/连续恢复/Tooling 证据充分，无覆盖丢失/状态污染或未解决作者缺证。只是 JUnit 方法合并、无真实调用收敛或删覆盖换时间不满足；不承诺未知提速比例，也不代替独立审查。

**G-CORRECTION-REVIEW（未判定）**：前两新 gate 满足，W-CORRECTION-REVIEW 唯一 member 有可采纳 completed Result；Router 完整读取并关联同一最终候选，结论有充分证据支持当前本地增量接受，无未解决 blocking finding/必要缺证。审查 completed 但否定/证据不足不满足；不倒填作者证据、不放行未列出工作或发布。任一当前必要 Result 为 partial/failed/blocked、不可采纳或关联不明，都不能满足对应 gate。

联合可行拓扑：原 I-FABRIC → I-JOINED → I-VIEWS → I-SOURCESET → I-AW → I-PREVIEW → I-SOURCES → I-COMPOSE → I-COMMON → I-SYNC → I-V67-HARNESS → G-STRUCTURE → I-ACCEPT → G-ACCEPT → I-COMPAT-RECOVERY → G-CLEANUP-RECOVERY → I-TEST-CONSOLIDATION → G-TEST-CONSOLIDATION → I-CORRECTION-REVIEW → G-CORRECTION-REVIEW。此线含所有历史额外前置的合法先后；每项同时等待显式 item prerequisites 与所属 Wave 前置 gates，每 gate 等待所在 Wave 全 members。当前必要等待集 15 items/5 Waves/5 gates，另保留 1 个旧 partial item/Wave/gate 的历史关系；联合无环、无自等 gate、无指向旧 partial 的新必需边。

## 执行前及作者自检边界

四文档作者准备/自检已完成；后续独立 review-delivery-plan completed、无阻断结论已由 Router 采用，用户经完整 scope 披露已确认测试集中实施与定向验证。首次因作者回执截断而 blocked 的复核保留历史，后续新复核完整消费作者事实后通过。原清理未变 scope 确认及 C5 同 tuple 图维护授权持续有效，本次仅同步记录，不新增授权。三个增量均未开始/完成，新 gates 均未判定；实际执行仍需当前 authority/确认、tuple 当前字节/JIT、prerequisites/gates、冲突及工具条件，Router 独立判断；C4/C5/C6 不由图或审查替代。

此前作者自检：当时是一套派生安排终态；未来恢复、测试集中、最终独立审查具有分别可接受/失败/保留/恢复和证据消费边界，按语义分开。清理恢复保持原必要语义/来源/风险，但以新结果承接剩余结算和新 gate；测试集中增加 driver/fixture、覆盖对应、状态寿命、次数/计时证据，最终审查需同时消费两份最终作者证据，整体是实质 delta，不能宣称身份更名式 review 复用。旧评审仅支持未变部分。

已对照 Plan/authority 的保留保证、证据取得方法与失败处理 consumer，核对唯一 ID/成员归属、全部 item/Wave/gate 联合等待和真实冲突串行、互链/仓库引用可读。新运行证据未来取得而未伪称已有；旧私有目录/所列入口存在可读不等于 XML 已结算、进程已退出或脚本已运行。没有新建永久矩阵/报告/hash ledger 或外部 Result 依赖。

此前作者准备未实施恢复/测试集中、未运行构建/旧脚本、未作独立计划/代码审查或释放 gates；其要求的增量独立计划复核现已完成并被 Router 采用，测试 scope 也已确认。本次仅记录同步，不重新审查或验证实现。仍未完成的是现场结算、测试集中实际作者证据和最终候选独立修正审查，不由作者自检、计划复核或 scope 确认代替。
