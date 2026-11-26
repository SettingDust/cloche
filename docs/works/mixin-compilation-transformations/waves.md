# Mixin compilation 转换 Waves

匹配 [Plan](plan.md) 与 [Work](work.md)，处理后事实见 [Context](context.md)。本文件是唯一执行图；权威行为来自 [Spec](../../spec/mixin-preapplication.md)。这是现有结构调整交付的恢复维护，不迁移或重开旧 Work 图。

Router 已完整读取并可靠关联 CT1 `implement-mixin-compilation-slices` generation 1 completed 与 CT2 `independent-mixin-slice-acceptance` generation 1 partial；G1 已判断满足且当时仅释放原 CT2，G2 未满足。Router 也已完整读取并可靠关联 CT3 `fix-mixin-classifier-selection` generation 1 completed、CT4 `accept-mixin-slices-after-classifier` generation 1 completed，判断 G3/G4 满足；G3 当时仅释放 CT4，G4 与 G1/G3 建立当前 Plan 的限定本地交付完成。以下保留原 items/predicates 与真实历史。一个 item 对应一个 Handler 和一个完整 Result；CT3、CT4 各用新的 Handler/Result，不能续跑或重写任何已终止 Result。缺失/不可采用的 Result、partial/failed/blocked 或缺少必要证据均不满足 gate。本次文档维护和作者自检不完成任何实现/验收 item，不释放 gate。

## 终态划分依据

CT1 原迁移中的统一注册、两片返回值、独立 N 顺序清理及四消费者改接共同建立一次有效视图替换；单个 helper/字段/N 参数不提供独立消费终态，故当时合为 CT1。该结构替换和聚焦检查已 completed，不因后续缺陷重跑或否定历史 predicate。

原 CT2 的完整 V1–V7 独立结论有单独证据终态，但当时的 classifier 反例使其 partial；原 predicate 不降低、不删除，历史效果和缺口见报告。恢复分组积极比较了两种独立结果：**CT3** 的精确角色修复及真实回归能被验收者消费，在完整验收仍有离线/其他缺口时也能独立保留；**CT4** 的修后完整限定验收必须由不同作者建立，能单独失败且不撤销已证实修复。因此分成两个新 items，不能将生产修复混进验收，亦不能只重命名 CT2 或等它再变 completed。

CT3 内的定位、最小生产改动、classifier 正反消费者及既有分片 sources/Strip 回归共同证明一个修复 predicate，无单独被消费的研究/fixture 交付；CT4 内的私有离线依赖准备、缺口补测、适用证据复用共同建立一个验收结论，无另一个产品交付。未发现需要再拆分的独立终态；实际缺乏必要原生关联、离线闭包或权限时，报告具体 gap，不藉未来 research item 预先承诺成功。持续确认范围、C6 和受影响边界独立复核仍由 Router 判断。

## Items

### CT1 — 统一有效编译视图的实现替换

- **已采用状态：** completed；结构替换及聚焦作者检查满足其 predicate，G1 已由 Router 判断满足。关联已采用的 CT1 原 Handler/Result，记录不替代原件；不重新执行或认定为修后验收。
- **前置 items：** 无。**所属 Wave：** W1。
- **唯一完成 predicate：** 现有 compilation 链已整体替换成 Plan 的统一注册与分片/独立记录转换，必要调用方和验收器随之迁移，且聚焦真实消费及错误/关闭检查证明这项替换可工作。
- **范围/预期效果：** `setupFiles → registerCompilationTransformations` 拥有 AW/Mixin/顺序 Strip/Decompile；Info 仅 Provider；SERVER/joined 一片、Fabric CLIENT 本视图双 CLIENT；编译 client→common、清理 common→client、原选片 sources+另一 clean library；原 C 条件 state/raw 上游/非 S 旁路/项目关系及 runtime/发布原链保留。各入口有真实必要生产关系；关闭不调度。适配旧单 Task/单 game/Join 的消费者和正式验收假设。
- **写所有权：** Plan 所定位的 compilation/Mixin、Fabric 输入归属、TargetHandler、common stub/sync 等必要生产调用方及旧 Join 删除；`MixinCompilationTests.kt` 与 `integration-test/mixin-compilation` 中必要 fixture/observer/比较器迁移；私有作者验证输出。保留独立 test/capability/remap-helper 修复。不得改 PRD/Spec/ADR、Codev、正式依赖、环境或旧 Work tuple。
- **所需证据：** 实际生产 diff 与调用链/接口职责说明；Kotlin/测试编译及聚焦真实 Java/Kotlin、原选片 sources、stub/sync 的清理输入观察；至少一组真实双 CLIENT 对 common/client API 的消费、逐片 P/N、S+两 P 顺序 core Strip 和 client 优先；逐片记录参数/producer 来源及必要任务先于读取；关闭和后配置、核心失败无旧结果回退的聚焦回归。完整覆盖边界按 Plan V1–V7 表留给 CT2，不以作者通过宣告完整验收。
- **冲突：** 所有 root 编译、metadata 生成、正式 fixture 与私有 Home/报告写入串行；不能与 CT2 或其他同路径写者并行。发现需扩大产品/Codev/环境边界的缺口则报告，不自行改 authority。

### CT2 — 新转换链的独立限定本地功能验收

- **已采用状态：** partial；关联已终止的 CT2 原 Handler/Result。[原报告](../../../build/reports/mixin-compilation-transformations/ct2/REPORT.md)保留 classifier high 失败、子证据及未覆盖条件。原 predicate 未满足，G2 未释放，禁止续跑/改写为 completed；恢复结果属于 CT3/CT4。
- **前置 items：** CT1。**所属 Wave：** W2；并等待 G1。
- **唯一完成 predicate：** 对 CT1 的当前实现形成充分、可复现的限定本地 V1–V7 功能结论，Plan 规定的改变对象均有实际行为证据且无必需未覆盖条件。
- **范围/预期效果：** 由不同于 CT1 作者的执行者消费实际 diff、当前实现与原始证据，使用 Plan 的方法定向重验四消费者、原 C/模型、分片/side/选片/独立记录、关闭/隔离、首产/错误恢复和必要失效。允许复用仍适用的方法/未变证据，必须说明适用性，不继承旧通过数。
- **写所有权：** 必要验收 fixture/比较器小修及新私有验收报告/输出；不修改生产代码、authority、正式依赖、Codev、环境或旧 tuple。生产缺陷由结果报告具体失败、已知/可能效果和受影响完成条件；不在此 item 混入生产修复或替旧 Result 改状态。
- **所需证据：** 按 Plan 表逐项定位的 V1–V7 实际命令/来源/环境、文件内容与任务顺序、完整 Gradle/Tooling 模型及差异/SourceSet index、正负消费者、真实 producer 和两步清理故障/恢复、复跑/新进程/失效结论；覆盖私有新 Home 缺 P/N/clean/workspace 的四类首产，以及独立 file/Maven C/view/SourceSet，不以预跑 operations 或同场游戏任务掩盖。记录可能留存旧输出和失败后实际效果；每份 N 不混用，实际清理只为 S+各 P。模型比较逻辑须含遗漏片/raw 回流/身份或 scope 丢失的反例自检。必要条件无证不能满足 predicate。
- **结论限制：** 仅 Windows/PATH Gradle 9.5.1/既有 Microsoft JDK 17/指定本地未发布 Mixins；正式来源、最低版本动态矩阵、最终 IDEA 导入/根/符号未验证。保留实际重复类/插件、原生 data/test index 与 scope 风险，不宣称通用兼容。
- **冲突：** 在 CT1 结果采用及 G1 判断后，针对最新一致状态串行执行；不与根构建/fixture/同 Home 写者并行。测试准备错误可在本验收边界内修正后重查，不能清共享缓存、停共享进程或重放历史删除。

### CT3 — 同 component 不同 classifier 的精确角色修复

- **执行关联：** 已采用 completed，独立的新修复 Handler/Result；[CT3 报告](../../../build/reports/mixin-compilation-transformations/ct3/REPORT.md)记录精确角色修复与作者聚焦证据。G3 已由 Router 判断满足且当时仅释放 CT4；不复用或改写 CT1/CT2 终态。
- **前置 items：** CT1。**所属 Wave：** W3；并等待 G1。CT2 partial 是必须读取的缺陷/效果证据，不是需要 completed 的前置。
- **唯一完成 predicate：** 在既有 C 及角色边界恢复准确 artifact/variant/classifier/完整位置的 S 识别，保留同 component 主 mod 与普通 extra 各自身份、compile/runtime 及项目关系，并由保留 classifier 输入的真实正反消费者和聚焦分片 sources/Strip 回归证明修复成立。
- **范围/预期效果：** 修复 `MixinPreapplication.roleFiles` 的 component 扩张根因；沿既有 resolvable roles、C 的非项目 raw view 与正常 remap/include 最终属性建立实际 artifact 关联，必要时最小调整相连 helper；不重建 C/依赖图、不以 component/basename/GAV/Set 顺序或角色优先级冒充精确选择。保持 raw 上游、带 producer 的 ABSOLUTE selectedSources、原 input 只选一次、非 S 直接原文件、两片/独立 N 与两个顺序、原有 opt-out、四消费者接线及 runtime/发布隔离。Plan 恢复方法给出已读根因及验证边界，不默认改 Codev 或设计全局角色框架。
- **写所有权：** 上述角色选择及直接必要调用/helper，`MixinCompilationTests.kt`、`integration-test/mixin-compilation` 中证明这一修复所需的最小 fixture/observer/断言，以及新的私有作者输出。保留其他未提交改动；不写 authority、Codev、正式依赖/版本、共享环境、旧 tuple、原 CT1/CT2 报告/日志，不混入独立 test/capability/remap-helper 修复或全部剩余验收。
- **所需证据：** 实际最小 diff 及 role artifact→C 原 input 的关联依据；保留 `modCompileOnly probe:ref:1` 与普通 `implementation probe:ref:1:extra`，两片实际 Mixin sources 纳入主 mod、排除 extra；真实 Java/Kotlin 正向消费主 mod 注入 API，普通 extra 实际消费成功且仍为非 S 原 artifact，runtimeOnly 负消费者无预览 API、compileOnly 不入 runtime。对照原失败日志、无 extra 对照、off/双向后配置；不能删 extra 或关闭开关当作修复。聚焦验证逐片 P/N、S+P 顺序 Strip、原选片 sources/另一 clean library、client 优先、非 S 原文件、原 C/项目/NO-SOURCE 角色及 source/game 必要失败无旧结果回退。按 Plan 说明旧证据适用与新证据，完整 V1–V7 仍由 CT4 判定。
- **执行前/失败边界：** 读当前代码、CT2 完整报告及相关原始日志，查已产生 P/N/部分输出与 observation 已被对照改写的事实；用新私有目录，不覆盖原证据或重放已完成删除。JIT 核对工具、metadata/local Mixins 与必要依赖；找不到精确原生关联或需扩大 API/环境时报告具体断点，不提交未经证实的替代设计。
- **冲突：** root 编译/metadata、正式 fixture、私有 Home/报告写串行；与 CT4 或其他同路径写者不并行，独立验收只能消费最新已采用修复状态。

### CT4 — 修后剩余 V1–V7 的独立限定验收接续

- **执行关联：** 已采用 completed，独立的新验收 Handler/Result；执行者不同于 CT1、CT3 作者。[CT4 报告](../../../build/reports/mixin-compilation-transformations/ct4/REPORT.md)逐项记录全部必要 V1–V7/classifier 复核的新跑与适用复用，无剩余必需缺证或阻断失败。G4 已由 Router 判断满足；旧 CT2 partial 及 G2 未满足不改写。
- **前置 items：** CT1、CT3。**所属 Wave：** W4；并等待 G3（其中已要求 G1）。无 CT2 completed 或 G2 前置。
- **唯一完成 predicate：** 对 CT1 结构与 CT3 修复后的当前实现形成原 CT2 所要求的充分、可复现、无必需缺证或阻断失败的限定本地 V1–V7 独立功能结论，并独立补核保留同 component classifier 的精确角色反例；其保证不低于原 CT2。
- **范围/预期效果：** 读取实际修复 diff/当前实现、CT3 完整结果、CT2 原报告/必要原始证据，按 Plan 恢复适用性表选择新跑、复用与必查对象，补齐独立 file/Maven C/view/SourceSet 和四出口首产、模型、joined/legacy/角色、mapping/refmap/context、失败恢复及必要失效。按实际输入/来源/实现影响/环境/消费者/新鲜性重核受影响旧证据；不因 170 项或四格成功缩小保证，不重做已完成 CT1 或未变引擎穷举。
- **写所有权：** 必要验收 fixture/observer/比较器小修、新私有 fixture/Home/依赖准备副本和独立报告/输出；不改生产代码、authority、正式依赖、Codev、共享环境、原报告/日志或旧 tuple。生产缺陷只报告定位、效果、受影响条件；不藏生产修复，也不自动改任一旧 Result。
- **所需证据：** 原 CT2 全部所需证据及结论限制逐项保留；Plan V1–V7 与恢复表中的 classifier 正反消费者、两片 sources/顺序 Strip、原 C/artifact/variant/classifier/项目与 SourceSet/index/闭包/scope、清理实物及真实四 builders 模型分别可核验。按报告实际覆盖复用 170 项/比较器反例与 Fabric sources 四格，记录适用理由；S 改变影响的生产/清理/消费及模型必须重查。新增四出口与直接模型缺 P/N/clean/workspace 首产、独立 file/Maven C/view/SourceSet 无同场 game 调度、joined/legacy/关闭隔离、真实 tuple/context/direct、source/game 首/后步与分片 producer 故障及撤故障恢复、新进程/必要失效。四出口各有实际失败传播与恢复，不生成全部故障×出口乘积。留旧输出时仍须失败，不能用离线缺依赖或配置错误代替生产故障证据。
- **离线准备/新鲜状态：** 按 Plan/Context 区分 FullCommon annotations 与 Sources build-tools 的实际缺项。只读现有同版本缓存，副本写入已许可私有输出，保留坐标/metadata/传递闭包，依赖准备不得运行 operations 或复制 transforms/P/N/clean/build。在私有 Home 验证离线可解析并于首产前再次检查冷状态及实际 binary/metadata 来源；闭包仍缺则报告准确 gap，不能换版本/改正式依赖/共享设置或关闭校验。准备和故障的已知/可能输出均据实保留，不宣称零效果。
- **结论限制：** Windows/PATH Gradle 9.5.1/既有 Microsoft JDK 17/指定本地未发布 Mixins；正式来源、最低版本动态矩阵、最终 IDEA 导入/根/符号未验证，原生 index/scope、重复类与插件限制继续携带。
- **冲突：** 等 CT3 的完整结果采用及 G3 判断后，针对最新一致状态串行验证；root/metadata/fixture/同 Home 不并发。可在验收边界内修正普通准备错误后重查，不清共享缓存、停共享进程或重放历史清理。发现新生产缺陷则保留有用不完整证据，不自行改图、修生产或释放 gate。

## Waves 与 gates

| Wave | 增量结果 | items（各恰一次） | 前置 Wave gates | release predicate |
| --- | --- | --- | --- | --- |
| W1 | 已完成结构替换与聚焦作者证据 | CT1 | 无 | **G1（原判定范围保留）：** CT1 的完整 completed Result 可采用，实现/fixture diff 在限定边界内，统一调用/分片/N/四出口迁移与所需聚焦证据充分，未混入独立修复或假定完整验收。Router 已判断满足，当时仅为原 CT2 提供可验对象，不是修后功能通过。 |
| W2 | 原独立验收的历史 partial | CT2 | G1 | **G2（未满足，保留历史）：** 原 CT2 的完整 completed Result 可采用、全部必要 V1–V7 有适用证据且无阻断失败/缺证，声明携带来源及限制。实际 CT2 partial 不满足；没有当前下游等待 G2，不能由 CT3/CT4 自动关闭或称删除即通过。其原完成保证由 G4 明确承接。 |
| W3 | 精确 artifact 角色修复与作者回归 | CT3 | G1 | **G3（Router 已判断满足）：** 新 CT3 完整 completed Result 可采用，修复边界及全部聚焦证据充分，保留 classifier 的真实正反消费者通过，主 mod/普通 extra 角色与原 C/项目关系不退化，分片 sources/Strip 回归成立，无影响该 predicate 的缺口。只释放明确依赖它的 CT4，不建立完整 V1–V7 成功。 |
| W4 | 修后完整限定验收 | CT4 | G3 | **G4（Router 已判断满足）：** 新 CT4 完整 completed Result 可采用，独立性成立，原 CT2 全部必要 V1–V7 保证及 classifier 修复复核有当前适用证据，无必需未覆盖项/阻断失败；离线准备不能顶替首产、旧证据复用范围可核验，声明保留来源及未验证限制。与 G1、G3 一起建立当前 Plan 完成信号，无自动实施/发布下游。 |

Router 独立判断真实资格与 gate，不因表格、作者自检、completed 字样或测试总数释放。W3 引用原 G1 只消费已建立的结构替换前提，不扩大原释放判断；CT3/CT4 的结果采用及 G3/G4 判断已由 Router 完成。未来 planned 调用须以当前可读的同目录 Work/Context/Plan/Waves 各恰一次正式 ref 明确选择匹配 tuple，并核对当前内容/身份、JIT、仍确认范围、前置及冲突。本次选定 tuple 仅约束完成记录维护；实现/验收完成来自 Router 已采用的 CT3/CT4，不来自本次自检。

## 历史恢复图 delta 与当前作者检查

- **身份/predicate：** CT1 与 CT2 的原 predicate 原文保留，状态分别为 adopted completed/partial；新增独立 CT3 修复、CT4 验收接续，各自新 Handler/Result。修复和独立验收有不同可保留/失败边界，不是一次混合 outcome 或 CT2 状态翻转。
- **证据/风险：** 原 V1–V7 保证全部保留，新增 classifier 必查与修复影响适用性判断；170 项模型、比较器反例、sources 四格只按实际条件复用。FullCommon/Sources 的离线原始缺项分别定位，依赖副本准备与产品冷状态分离，不能把私有缓存准备冒充首产或任意改环境。
- **依赖/冲突：** CT3 需 CT1/G1 及 CT2 失败证据可读，无 CT2 成功等待；CT4 需 CT1、CT3/G3，不需 G2。新结果不得回写旧 Result；生产与验收写分属不同作者，root/metadata/fixture/Home 共写串行。未改变的旧所有权/隔离限制不扩张。
- **gates/完成信号：** G1 保持历史判断范围；G2 保持未满足且无当前下游，G4 承接全部原保证，当前 Plan 需 G1+G3+G4。没有删除必要门槛宣称通过，也没有以新结果倒填 G2。变化含修复边界及 gate/依赖连接，当时不能仅凭身份更名复用旧评审；这组变化及必要原连接已完成作者自检与 `review-mixin-classifier-recovery` generation 1 独立复核，随后才执行 CT3/CT4，无准备度阻断。本次仅同步完成记录，没有新的实质 graph delta。

组合等待包含 item 前置、Wave 前置 gate 以及 gate 等待所属 item：`CT1 → G1 → CT2 → G2`（历史）与 `CT1 → G1 → CT3 → G3 → CT4 → G4`（当前）；另有 CT1→CT2、CT1→CT3、CT1→CT4、CT3→CT4 直接前置。W1 无 gate 前置，W2/W3 等 G1，W4 等 G3；每 gate 仅等本 Wave 的唯一 item 及证据，无自身释放等待、反向边或 CT2 partial 的永久成功等待。四 items/四非空 Waves 各恰一次，所有 item/gate 引用可解析。

已与 Plan 的范围/完成条件和 V1–V7、Work 当前描述及 Context 证据/效果/限制对照；同目录唯一互链保持，未引入第二执行图或外部 Result locator/永久账本。保留原失败产生 P/N/部分输出、对照改写 observation、旧不可重放清理与未知差额；未知差额不改成无效果。本次仅作者语义自检与记录同步，未执行修复/试验/验收或独立评审，未自行完成 item/gate。CT4 的模型 743/902/352 检查有重叠、不加总；六角色 source-only 路线及 CT3 producer→Decompile 故障传播仅按报告适用复用，其余关键行为由多次定向运行建立。UP-TO-DATE/workspace 不称 FROM-CACHE。最终结论限 Windows/PATH Gradle 9.5.1/Microsoft JDK 17.0.19/指定本地未发布 Mixins；正式来源、最低 9.4 动态矩阵、最终 IDE 导入/模块根/符号未验证，原生 data/test index miss、scope grouping、任意重复类/插件限制不变。
