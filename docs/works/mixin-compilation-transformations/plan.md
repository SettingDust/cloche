# Mixin compilation 转换实施 Plan

[Work](work.md) · [Context](context.md) · 唯一执行图：[Waves](waves.md)

## 范围与完成含义

落实 [Spec](../../spec/mixin-preapplication.md) 的 compilation 统一转换、Fabric 分片与独立记录合同，使编译、原选片 sources、common stub、compile sync/真实 Tooling 模型实际消费同一有效 clean 域，保留原 C、原生项目关系及运行/发布隔离。Authority 的 PRD reuse / ADR skip / Spec reuse 评估见 Context；本 Plan 不改 authority。

这是一个结构调整实施范围，无额外 overview 或第二执行图。CT1 completed 与原 G1 满足事实保留：G1 只建立结构替换及作者聚焦检查，当时仅释放原 CT2。CT2 已以 partial 终止，classifier 缺陷使原 G2 未满足；不续跑、覆盖或改写该 Result。当前完成信号为 **G1 + G3 + G4**：G3 建立同范围精确角色修复及作者回归，G4 以新的独立验收结果承接原 G2 的全部必要 V1–V7 保证并补核 classifier 反例。Router 已采用 CT3/CT4 completed 并判断 G3/G4 满足，G1+G3+G4 完成信号已成立，限定本地交付完成。原 CT2/G2 保持历史未满足，不作为当前路径必须等成功的前置。

用户已在具体范围及同范围维护披露后回复“可以”，持续 C4/C5 授权覆盖修复、回归与图维护；本次只更新四份派生文档和作者自检，不执行实现、试验或验收。恢复图作者自检及 `review-mixin-classifier-recovery` generation 1 独立受影响边界复核已在 CT3/CT4 前完成，无准备度阻断；本次忠实记录 Router 已采用的结果和 gate 判断，不代作新的判断。

保留：常规 JAR 原生属性适用范围、同 target 开关继承、正常 remap/include、原 compile 身份/variant/classifier、NO-SOURCE 和项目/SourceSet 角色、common 跨 target 交集及回退、真实失败阻断、原有 runtime/outgoing/publication。非目标：新 Codev API、Join/重复条目预过滤、通知汇总、任意归档/插件兼容、通用模型调度框架、独立 test/capability/remap-helper 修复、fixture 环境属性整理、正式来源发布接入、最低版本动态矩阵与最终 IDEA 导入。

## 实施策略与具体职责

### 统一入口与返回值

原调整前的 `TargetCompilation.setupFiles` 已调用 `registerCompilationTransformations` 注册 AW/Decompile，Mixin 在调用外、Fabric Info 反向注册辅助转换和 Join；CT1 已沿这条调用链完成统一入口和调用方迁移。以下保留已确认的职责与行为合同，作为修复/验收比较基准，不是当前代码仍待整体重做的描述。SERVER/CLIENT 或分片间不复制注册函数；旧 Join 已移除，辅助 common AW 的旧 runtime 用途由统一所有者维持，不另建 Mixin 或无消费者 Decompile。

以下为**设计示意，非当前签名/已实现 API**，允许使用现有参数与局部变量实现相同职责，不要求新 DTO：

```kotlin
// 原函数扩展：同一实现拥有本 compilation 的全部转换。
registerCompilationTransformations(
    target, featureName, sourceSet,
    /* Info 的 named common/client 或 joined Provider、原选片与原 extra；
       compilation 自己的显式 AW、有效 side、C/R Provider */
): FinalJarTasks

// 扩展现有返回值的职责；具体字段类型沿现有 Gradle Provider 结构。
FinalJarTasks.finalMinecraftFile      // 原选片 AW，runtime 协议可继续用旧别名
FinalJarTasks.runtimeExtraFiles       // 原 extra / 原次序
FinalJarTasks.compileMinecraftFiles   // 有序有效游戏 Provider，CLIENT: client → common
FinalJarTasks.sourcesArtifact         // 原选片 Decompile 输出
// 内部保留必要 operations TaskProvider 集合，供现有消费者条件补边。
```

Info 只提供 named 分片、库、归属、原选片等 Provider 输入；不接收 compilation 注册任务。`featureName ?: main` 作为内部 feature 身份，沿现有 Gradle 命名生成 task 名，路径由 target 外层、feature、分片隔离；不要再造 collapsed 命名协议。统一函数可以调用同文件/原模块的小型私有 helper，但不得把四类转换的职责重新外移到 Info。

Fabric SERVER 只对本 compilation 的 common AW 注册一次 Mixin。Fabric CLIENT 对本 compilation 的 common/client AW 用同一注册逻辑各注册一次，两者均为 CLIENT；joined Forge/NeoForge 一份 CLIENT。data/test 与 client data/test 以自身 C/S/AW/mapping/context 构造，不借用 main SERVER 预应用。当前 `finalCommonJar` 从 main 传入的关系可为旧 runtime 视图保留，但不能决定启用后当前 compilation 的 clean common 输入。另一片 raw AW 必须也应用当前 compilation 的外部及显式 AW。

### 状态、raw → clean 与生产失败归属

统一入口拥有每视图 operation key、分片 Task/P/N 和下游选择；JAR 默认 raw 仍由 project 现有入口提供。原 C 仅在最终 enabled 为 true 时请求 operation；false 不安装本次任何 state。沿现有 `withDependencies` 条件 hook 或等效公开 API 保留解析前双向后配置，不以 absent Provider 表示关闭。

```text
C/R 非项目 raw views + 本 compilation raw AW/libraries → 各片 Mixin
C 的 raw 外部 mod S                                  → 各片 mixinFiles
每片 T → Mixin → P、N（独立 Provider）
原 C 的最终 remap/include 状态 → 选择器：原 input 精确匹配 S 一次
    S → Strip(N_common) → 临时 → Strip(N_client) → 最终 cleanSources
    非 S → 原文件；无本 state 的 project classes/resources 仍原生选择
每片 P → 独立游戏清理请求 → 同序全部 N → cleanP
clean C + [cleanP_client, cleanP_common] → compile / stub / sync
原选片 cleanP + 另一 cleanP/library + clean C → Decompile → sources
原 AW/extra + 原 runtime 请求 → runtime；原 raw compile/AW → 发布 remap
```

SERVER/joined 的清理仅一步。编译游戏顺序 client→common，记录处理顺序 common→client，分别显式跟踪，不能从 Task 执行顺序推导。两片保留全部类/资源/metadata/Manifest，包括重复项。

保留 `selectedSources` FileCollection 的内容、成员、完整路径和生产来源；`@InputArtifact`/S 为 ABSOLUTE。选择器 `from` 匹配正常 remap/include 最终属性，`to` 只设本视图 state，原 C 不强加 JAR type，不重选图/重建依赖。各 P 用专用精确游戏请求，不再 remap/include；每份 N 单独由对应 Task 的 `appliedMixins` Provider 跟踪及按序使用，无通知文件并集或汇总任务。

Cloche action 在原 input 选中后，依次调用现有 `ExactMixinStripper.clean/rewrite`，后步只读前步暂存文件；仅登记最终结果。保留 Codev 包装层的输出与输入重叠/别名保护、并发输出保护、暂存、原子发布及异常清理；核心 `clean` 只写暂存，不替代这些包装责任。非 S 直接返回原 InputArtifact，不写清理副本。不能对同一 `TransformOutputs` 连续调用原 `StripMixins.transform`。本地已读 core/包装层与实验 action 可供实现比照；JSON 删除算法继续由 Codev 拥有。

raw 上游至少核对 remap helpers（保留 REMAPPED=false）、AW/辅助 AW、external/role/S、Mixin 解析 classpath及发布回读；都只覆盖非项目 artifact view，不能将 raw 施加到整份 C/项目消费。sync/stub 消费 helper 则继承 C 的有效状态。Decompile 读取 clean C/另一 clean 片，raw extra 不得从 clean 链回接 Mixin。编译只追加有效游戏，不沿继承再引入 raw AW；项目输出追加保持原关系。

任何分片 Mixin 异常由该真实 Task 抛出，必要 source/game 任一步 Strip/I/O 错误由 action 抛出，受影响消费者失败；保留 compilation/分片/记录/原 input 定位及原因，清理暂存，禁止半成品或旧路径回退。旧 clean/sources 文件留存本身不构成成功。只允许用户 opt-out 选择旧视图，不自动吞错。

### 四消费者及实际生产入口

| 入口/当前位置 | 有效输入与时序安排 | 必要观察合同 |
| --- | --- | --- |
| Java/Kotlin；TargetHandler 的任务注册 | 统一返回值的有序游戏加原 C；原 `dependOnPreapplication` 由统一所有者提供全部必要分片的最终开关条件依赖 | Java/Kotlin 有实际源码调用 common/client 注入 API；任务图各必要 producer 在参数读取/消费前完成，关闭不带入 |
| `compilation.sources` / Decompile；统一转换入口 | 原选片 cleanP，另一 cleanP 作 library，clean C 与必要原库；用有效输入的生产关系，不足时在注册处条件补全部 operations | 未分割 main sources 选 common，独立 client/includedClient 选 client；SERVER/joined 原单片；实际反编译内容、library 文件与时序，不要求单 sources 含两片 |
| common stub；CommonTargetCreator.convertClasspath/intersection | 把单 game+extra 改为同一有序游戏集合；clean 非项目 artifact view 仍携带 component；所有实际参与者/回退的必要 producers | 两 target 共有 API 保留、独有 API 不进入交集、一个 target 关闭仍参与；main/data/test/client/clientData/clientTest 的现有选择和实际使用者编译 |
| compile sync；CompilationInternal.configureSourceSet | inputs 与 `.files` 实际物化 clean 非项目 view + 有效游戏；原条件依赖涵盖全部必要分片，runtime sync 沿原链 | 不构建额外项目输出；记录物化文件、参数来源、集合依赖与任务图 |
| 直接真实 Tooling 模型；既有 R4Tooling/observer | `connection.model(Observation.class).forTasks(":clocheIdeSync").get()` 同请求先准备再 builders/严格读取；sources 请求沿原出口 | 新 Home 缺 P/N/clean 时，准备完成先于 builders；完整模型与 SourceSet index（含未命中）、原项目/源集/变体/闭包/scope；失败时不得产出成功模型 |
| 可独立物化 C / 继承 view / SourceSet；已有 verifyC/View/SourceSet | 沿实际支持的观察任务接同一条件生产关系，分别 file/Maven 冷首产，不借同场游戏消费调度 | 集合 `buildDependencies` 与 consumer 图分别记录；不要求 C 本身新增 operation、不新增任意第三方支持 |

关闭返回原 AW/extra 与原选片 sources，原 C 无私有 state；条件边与有效游戏选择均不得调度 Mixin/Strip。保留原 compile scope/variant，不能为发布回读改用 runtime 副本；Forge legacy writer 和 runtime 使用旧 AW。

## 证据可获得性与维护代价

已采用的双 CLIENT 报告、CT1 实施及 CT2 子证据支持单输入 Task、多份 N、内部顺序 Strip、另一 clean 片作 Decompile library 和条件补边这条既定路线；CT2 当时的 classifier 失败推翻了精确 S 选择的局部正确性；现已由 [CT3 修复及作者报告](../../../build/reports/mixin-compilation-transformations/ct3/REPORT.md)与 [CT4 独立 V1–V7 报告](../../../build/reports/mixin-compilation-transformations/ct4/REPORT.md)补齐修复和验收，不变更产品、Codev API 或重做 CT1。以下表格保留原验证义务和证据适用性判据，已完成情况以两份报告为准，不表示仍待执行。

代价是 Fabric CLIENT 每视图两个 producers、每个 S/P 两次清理及四消费者的必要依赖接线；不同 compilation 不能只因 side 相同而复用 P/N。共参 S/N 改变可使非 S 归档也重进 action，仍不写 Strip；没有性能量化保证。相对旧 Join，本方案撤去拼接/去重和合并记录的维护责任，仍需维护两个明确顺序及 sources 的选片/library 关系。没有新框架、全局通知协议或通用工具模型调度成本。

## 验证安排与成功条件

使用现有 `:mixinCompilationTest`、`MixinCompilationTests` 和 `integration-test/mixin-compilation`。根准备/编译/测试保持串行，例：在既有 Microsoft JDK 17 下以 PATH `gradle` 执行 `:mixinCompilationTest --tests earth.terrarium.cloche.MixinCompilationTests.<适用方法> --no-configuration-cache`；保留原 fixture installation/java/metadata 属性。方法名见下表；迁移后可按实际名称定向选择，不必把全部历史运行重新包装为一遍矩阵。本次未运行该命令。

fixture 私有 metadata 从规范 plugin-under-test metadata 精确替换唯一 Mixins JAR，保留其余条目/次序；嵌套 TestKit/Tooling 显式接收该文件。执行时核对真实加载 Mixin/Strip/ProduceMixinsKt/宿主异常类来源、实际 implementationClasspath、锁定 Sponge、Gradle/JVM/Home，禁止只凭文件名、组件测试或参数推断消费来源。

| 合同 | 定向重验与具体可用方法 | 充分结论边界 |
| --- | --- | --- |
| V1/V3 | `sourceCompilersConsumeRealPreapplicationAndOffRestoresRaw`、`sourcesConsumeRealCleanClientAndRestoreOldView`，结合原型的两个 CLIENT/独立通知与重复条目用例扩充正式 fixture | AW 先于 Mixin；common/client API 在真实 Java/Kotlin 可调用，原选片 sources 展示新增接口/成员且另一 clean 片实际作 library。Invoker 合成方法不要求反编译显示其方法体。两 P 各保留重复/唯一条目，client 优先，无 Join/预过滤或 raw AW 回流。 |
| V2 | `localCompilationIdentitiesRemainLazyAndNative`、`nativeProjectConsumersRetainCompilationRuntimeAndSources` 及 identities/final-non-s fixture | compileOnly/传递 mod 在 S；runtimeOnly、普通库、项目/本项目 Mixin 不在 S。完整 C 成员/原 artifact/variant/classifier、非 S 原文件、项目/SourceSet/producer 保持；同名/同组件另一 classifier 原 input 完整位置精确选择一次。NO-SOURCE 目录不存在仍原生解析，不造目录/JAR。 |
| V3/V4 | `configuredCompilationsInheritEnvironmentAndTargetSwitch`、`nestedAwAndSourcesUseUniqueDeclaredPaths`、`commonStubsIntersectEffectiveParticipantsAndFallbacks`/main 版本 | SERVER 一片；各 CLIENT 两片且都 CLIENT；joined 一片。data/test 自身 S/AW/tuple/context 与归属，任务/输出唯一；至少两 target 六组参与/回退、共有/独有 API 正负编译及一个关闭参与者。分类参数可复用方法，不把 14 角色都重做完整引擎。 |
| V2/V5/V7 模型 | `syncToolingBaseline/Off/On/Sources/FullCommonModels`、`syncFullCommonFallbacksAndRuntimePrepareBeforeModel`、`R4Tooling.java`、observe/full-model/sync 及 Python 比较器 | 同输入 no-state baseline/off/on 的 Gradle 成员/关联/依赖、sync 实物与完整真实模型分别比较；四 builders、SourceSet index 含 miss、继承/多产物/variant、闭包/scope与游戏集合均保留。允许原生排序/grouping/重叠；完整差异先保留再解释，不过滤额外成员，不固定 909 数。 |
| V5 | 现有 baseline/off、双向后配置、target 隔离与 runtime/remap/include 观察，final-legacy-real fixture 的真实 writer 方法 | 所有消费者关闭回旧链，C 不安装 state、不执行/依赖 Mixin/Strip。非空 include、项目 JAR/outgoing、发布 raw compile 变体与原 AW、runtime/Forge legacy 文件保持隔离，原始输入不变。仅 tiny 输入真实 writer 范围，不宣称完整 Forge 下载/补丁/启动。 |
| V6 | 现有 prepare 的 root-refmap remap 与无 refmap selector、direct/no tuple；final-acceptance context 方法 | 分片接线下真实 remap 后 selector 到达预期 owner/member并编译调用；无 remap 仍预应用/Strip；默认/有效显式 context、抛出的不存在 context/namespace 及恢复。不同 source namespace/context 不可共用时报告具体上游缺口，不猜测/改写 refmap。 |
| V7 首产/失败 | `isolatedNativeCompileViewsProduceNotificationBeforeCleanup`、sources/stub/sync/Tooling 的私有新 Home 方法，正式 fixture 的故障注入及原型独立 N 思路 | 编译、反编译、stub、sync（含直接模型）分别缺所有 P/N/clean/workspace；任务入口不预跑 operations，模型先执行规定准备。file/Maven 的 C/view/SourceSet 各自首产与真实任务图；任一分片 producer、source/game 首步/后步清理失败分别阻断受影响四消费者；保留旧输出不回退，撤故障实际恢复。 |
| V7 清理/缓存 | 顺序 action 聚焦测试与现有 final-acceptance、失效/workspace 方法定向迁移 | 清理恰为 S+各 P，全部 N 有序作用且只发布最终；空记录、重复删除、缺配置安全，坏记录/命中格式错/I/O 真失败。复跑/新进程，S 配置/内容/成员/有效位置、每份 N 内容/顺序、AW/side/tuple/context/实现改变的必要失效及视图隔离。 |

以上为已完成验收的方法与必要保证；已采用的 CT1/CT2 子结论按下节适用性判定复用，旧通过数不能代替当前输入的成功证据。CT1 的作者聚焦检查已完成，恢复作者检查先建立精确角色修复，独立接续验收保留完整受影响 V1–V7。共享 action 的首步/后步和 source/game 分支分别测真实故障，四类出口各有必要失败的传播及恢复证据，不要求故障与出口的全笛卡尔积。可复用未变化的引擎穷举与已证实的方法；不能因身份/文档字节变化重跑所有旧矩阵，也不能复用已改变的单片产品结论。

CT1 已迁移验收器；恢复时继续核对这些语义：`decompile.gradle` 不再要求“一个 sources/game 同含 Game/ClientGame”，`verify-sync-models.py` 不得回到 `unique_effective_game`、预 Join composition 和单 notice 假设；必须逐片 P→cleanP、逐记录顺序语义、完整游戏成员与 client→common、选片 sources/library 实证。对于同 component/variant 下的多产物，观察器也必须保留所选 artifact/classifier 的原生关联，不能把单产物 fixture 的唯一性断言推广为身份配对算法；执行时核对实际观察器的当前实现。保持比较器的完整 getter/模型身份/原始差异、scope 用途和物理 clean 隔离，不把新双片集合先折成虚构单 JAR 再宣告相等。对新比较逻辑保留小型反例自检，确保遗漏一片、raw 回流或项目/scope 丢失会失败。

选择器不得 `@CacheableTransform`，不要求 build cache hit；同位置/相同完整输入/实现/workspace 尚存的复用单列，不标成 FROM-CACHE。各 P/N producer、原 remap/include 和实际声明可缓存的游戏 action 按真实状态观察，不继承旧单 N 游戏 Strip 的缓存结论。新 Home 首产不能由暖 workspace 或预置 N 替代；不清共享缓存逼造命中。

恢复证据已在新的私有输出中记录实际命令/环境、任务和参数来源、文件/ZIP 内容、失败原因/已知效果/恢复及 V1–V7 覆盖与限制；[CT2 原报告](../../../build/reports/mixin-compilation-transformations/ct2/REPORT.md)及原日志只读保留，不覆盖失败输入的证据，不新增 hash/身份账本。任何必需条件缺证不得宣告完整本地开发验收；只报告受限结论和具体缺口。

## 恢复方法与证据适用性

原根因已由实际源码与 CT2 classifier 正反运行界定：`roleFiles` 将 role 的 component 成员扩张为 C 中该 component 的所有 artifacts，ordinary 因 extra 同属 component 而错误否决主 mod。CT3 已实施的最小方法是在既有 resolvable mod/nonMod role 和 C 一致解析关系内，读取实际 role 所选 artifact 及其原生关联，与 C 的非项目 raw artifacts 按 artifact/variant/classifier 和完整文件位置精确关联。role view 的属性必须与当前 C 的正常 remap/include 状态兼容并只在非项目上游请求 raw；不重选 C、构造逐 artifact detached 依赖、依靠同名文件或只调整 ordinary/mod 优先级。`selectedSources` 仍复用带来源的 raw FileCollection，保持 ABSOLUTE 及依赖/成员/内容跟踪，选择器对原 input 精确判定一次；传递 mod、普通库、runtimeOnly、项目/SourceSet 和关闭角色不变。CT3 在 role 原生选择前惰性继承 C attributes，以原生 artifact ID 与完整 variant 相等关系匹配并返回 C 的完整位置，保留两侧 producer；共用必要 remap helper，不使用 variant reselection 或扩展 Codev。

修复的充分作者证据必须保留 `modCompileOnly probe:ref:1` 与普通 `implementation probe:ref:1:extra` 同时存在：两片实际 Mixin sources 包含主 artifact、排除普通 extra，真实 Java/Kotlin 消费主 mod 注入 API；普通 extra 的实际消费者仍可编译且其文件/角色未被 Strip 或提升为 mod，runtimeOnly 负消费者不能获得预览 API、compileOnly 不泄入 runtime。另保留原无 extra 对照及 off/后配置对照，不能靠删 extra、关闭开关或只看文件名消除反例。聚焦检查还要观察两份 N、S+各 P 的有序 Strip、原选片 sources 与另一 clean library、非 S 原文件及原 C/项目关系；这些检查共同证明一次角色修复，不构成完整独立验收。

| 条件 | 可复用候选与适用边界 | 恢复时要求取得或重查的证据（实际完成见 CT4） |
| --- | --- | --- |
| V1/V2 | CT1 无重叠 fixture 的真实 Java/Kotlin与原生身份检查；CT2 重读的 170 项物理模型及比较器反例。需逐项核对修复影响、输入、实现、来源/环境和消费者，不能外推到 classifier。 | 保留 extra 的 classifier 正反真实消费者、准确 S/非 S、主 artifact/extra 的原关联与 scope；修后 C、view、SourceSet、sync 实物及完整 Tooling 身份/闭包/index/scope 重核，覆盖原生项目/NO-SOURCE、同名位置、传递 mod。 |
| V3 | CT2 Fabric 六角色 named/unnamed × on/off sources 首产及重复运行，按实际 observer/输入复用；可复用未变统一接线静态事实。 | 角色修复影响的双 CLIENT sources/Strip、选片/library须新证；补 joined Forge/NeoForge 单 CLIENT、data/test 自身 S/AW/tuple/context/side 与实际参数/任务唯一性，分类覆盖而非每角色全引擎矩阵。 |
| V4 | CT2 `commonStubsIntersectEffectiveParticipantsAndFallbacks` 及适用物理模型仅覆盖所测组合。 | 补两个 target 的 main/client/data/test 回退与关闭参与者、共有/独有 API 正负编译和 producer 关系；S 改变所影响的 stub 必须新查。 |
| V5 | CT2 四格 sources 的对应 off/runtime 观察及旧同输入模型可按范围复用。 | 补成功的 Sources/FullCommon 关闭模型及 baseline/on/off、target 隔离/双向后配置；真实 runtime/outgoing/publication、非空 include、raw compile 变体及原 AW、Forge legacy writer，限制仍为 tiny 输入真实 writer。 |
| V6 | classifier 原失败说明走过真实 mappings/refmap 路径，不能当作 refmap 成功验收。 | 新跑真实 remap/refmap 与无 refmap selector 的编译正向、无 remap/direct、默认/有效显式 context，以及不存在 context/namespace 的实际抛错/撤故障恢复；不改写 refmap 或声称支持不兼容 context。 |
| V7 首产/故障 | CT2 六角色 sources 四格只覆盖该 sources 路线；CT1 已采用失败/恢复仅在实现、故障位置、消费者和输入仍适用时复用。 | 新私有 Home 独立 file/Maven C、继承 view、SourceSet，无同场 game 调度；编译/sources/stub/sync 与直接模型各补实际缺 P/N/clean/workspace 首产及先生产后读取。任一分片 producer、source/game Strip 首/后步分别真实失败、临时/旧结果不回退、撤故障恢复；四出口各保留必要传播证据，非全部故障×出口乘积。 |
| V7 失效/清理 | 未变 Codev 引擎穷举与比较器反例可复用，不复跑全引擎矩阵；paths 的重复 UP_TO_DATE 不等于新进程/失效保证。 | 顺序 action 的空记录/重复删除/缺配置安全与坏记录/格式/I/O 失败；S 配置/内容/成员/完整位置、各 N 内容/顺序、AW/side/mapping/context/实现变化、视图隔离、新进程/workspace 条件及实际声明可缓存单元。选择器无 build cache，不把 workspace 复用记成 FROM-CACHE。 |

离线准备与冷首产分开。现有 `MixinCompilationTests.prepare` 准备输入/metadata，`R4Tooling` 使用私有 Home 并以 `--offline` 同请求先 `:clocheIdeSync` 后 builders。原 FullCommon annotations 与 Sources Kotlin build-tools 准备缺口已由 CT4 分别通过私有 offline resolveClosure 补齐；报告记录完整闭包、来源与冷状态。下述为实际准备沿用的隔离保证：只在已许可私有输出内，从同 Gradle 9.5.1 现有缓存取得静止副本，保留原 `modules-2` artifacts/metadata 与传递闭包；排除锁/进程文件及所有 transforms、P/N/clean/项目 build 输出，不改共享源。可用既有工具在该私有 Home 做仅依赖解析准备并保留实际来源，禁止调度 AW/Mixin/Strip 或消费者预产。执行前重新核对仍为指定 Gradle/JDK/local Mixins/Sponge/唯一 metadata 替换，所需坐标与闭包在私有 Home 离线可解析。

准备后、首产前再次检查各 P/N/clean、fixture build 及 Transform workspace 缺失；若准备意外产生产品输出，保留其效果，另建干净私有首产目录，不能删除旧证据后冒充从未运行。允许依赖缓存预置不等于允许 clean workspace 预置。文件存在/POM 可读不保证完整解析；具体缺坐标/metadata/工具或安全副本不可取得时保留 gap 和受影响 V 条件，不能通过正式依赖/版本/安装/共享设置变更或关闭全项校验宣告完成。此准备服务验收结果，没有独立产品交付边界。

## 风险与执行边界

- 双片参数 Provider 不会自动给 C 加 producer。统一所有者补真实必要任务边；直接模型先准备再解析。不得用 mustRunAfter、同场任务或配置阶段 `.files` 掩盖。
- raw/clean helper 用错可成 AW→选择器→Mixin→AW 环，或使 Decompile/模型偷读 raw。按上游与消费者职责逐个核对实际文件和图，失败时保留真实原因。
- 重复类的 classpath 顺序、原 sources 选片与模型原生 grouping 是三个不同观察；原型只排除了所测小型路线障碍，任意编译器插件/差异字节码未验证。新退化若需改产品或 Codev 合同，停止受影响承诺并报告。
- root、fixture、metadata 与私有报告的共享写必须串行；禁止同时构建根工程、覆盖 fixture 或对同 Home 并发故障/恢复。保留现有未提交实现及其他用户修改，仅按本范围迁移，不回退整个工作树。
- 本地 JAR/工具/规范 metadata 以及新目录状态需执行时 JIT 核查，缺失时报告所缺输入，不安装/换版本、改依赖或静默用发布 JAR。正式来源、最低版本和最终 IDE 未验证限制随每次完成声明保留；模型 scope 合库及 data/test 原生 index miss 不据此修复。

## 完成证据与作者语义自检

Router 已完整阅读并可靠关联 CT1/CT2/CT3/CT4 原 Result，采用 CT1、CT3、CT4 completed 和 CT2 partial，并判断 G1、G3、G4 满足；原 G2 未满足不变。准备图的作者自检及独立受影响边界复核已完成。本次仅同步采用事实，没有重新采用原 Result、执行构建/行为验收、独立评审或自行释放 gate。

CT3 报告记录保留主 mod+extra 的真实正反编译、精确原生角色及双片 sources/Strip 回归；CT4 报告逐项承接全部 V1–V7/classifier 保证。新跑覆盖六组 common、六个 file/Maven C/view/SourceSet 冷首产、同请求先准备后四 builders、真实失败恢复及必要失效；六角色 source-only 路线和 source producer→Decompile 传播仅按报告核对条件适用复用。模型 743/902/352 检查有重叠、不加总；旧 170 项不替代当前完整模型。UP-TO-DATE/workspace 不等于 FROM-CACHE；离线准备不算产品首产。详细新跑/复用/中间失败与最终重核从 CT4 报告导航，不复制日志或新增账本。

作者自检对照四份记录与 Router 事实及两份仓内报告：完成信号一致，CT2 partial/G2 未满足保留；原 item predicates、V1–V7、必要保证、scope、风险、所有权、依赖与冲突保持，唯一 Waves 和同目录互链一致，证据入口可读。历史失败的 P/N/AW/部分输出、无 extra 对照改写 observation、旧不可重放恢复/删除及未知差额仍保留于 Context；未知不称无效果。本次状态与措辞维护没有新的实质 graph delta，不新增实施或审批。

限定结论为 Windows/PATH Gradle 9.5.1/Microsoft JDK 17.0.19/指定未发布本地 Mixins。正式来源、最低 9.4 动态矩阵、最终 IDE 导入/模块根/符号未验证；data/test 原生 index miss、scope grouping、任意重复类/插件兼容边界保留。文档作者自检不证明代码重新验证或证据永久适用。
