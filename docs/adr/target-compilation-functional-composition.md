# Target / Compilation 的函数式组合

状态：**Accepted（函数式架构方向与 Joined/Split 泛型纯数据模型）；实现与行为验收尚未由本决定建立。**

记录日期：2026-11-06。

决定身份：Cloche 整体 target / compilation 的内部组合与协议所有权。本文是独立架构决定，不替代 [Git-backed Maven publishing](git-backed-maven-publishing.md) 的发布决定。

## 上下文与接受范围

用户要求整体重新审视 target / compilation 的耦合，偏好函数式组合，不通过新增职责对象划分行为；范围包括平台供料、家族装配、依赖选择、转换、消费、Common、Linkage、run 和发布出口，不能仅以拆分 `registerCompilationTransformations` 交付。已有 Gradle DSL 与状态对象仍是必需的状态载体。

[整体设计提案](../proposals/target-compilation-architecture.md) 第 2–9 节及其 [架构说明图](../proposals/target-compilation-architecture.html) 区分了可收窄的知识传播与必须保留的领域联系：消费者不应重复掌握预应用开关、选片、records 与条件生产依赖；平台版本、家族归属、Linkage 成组写入以及后配置时序则需要保持联系。术语沿用 [领域 Context](../../CONTEXT.md)：target 聚合平台与版本，compilation 拥有独立源码与依赖视图；Common 共享源码不同于游戏 common 分片，feature 也不是 compilation。

用户最初在阅读说明图后表示“看着没什么问题，规划一下”，接受函数式组织与原位组合方向；本次在已明确展示 `Joined(value)` / `Split(common, client)`、`awGames` / `strippedGames`、named → AW → preview 流及泛型复用文件/任务拓扑的代价后，表示“可以了，开始生成文档吧”。本记录据此在同一决定身份下承接该模型选择，不再将内部形状整体列为未选择。具体内部标识符、字段和精确函数签名仍可调整，不冻结为公开 API；兼容承诺按下文已确认的内部边界收窄，不授权破坏公开 API/ABI。提案为 Accepted、non-authoritative，HTML 用于说明已接受的模型与来源；两者用于解释来源，本文不将其全文提升为已接受合同。

用户随后在获知旧内部二进制入口及专用兼容测试的删除范围和影响后，确认取消这些内部兼容承诺。该确认在同一决定身份下替代旧 Map/String 适配与内部 JVM 入口保留要求：不支持 `TargetHandler` 的旧 synthetic accessor（包括手写 `access$addDependencies$...`）、`GameShards` 的旧 Map/String 适配、`TargetCompilationInfo` 的旧 Map/String 构造与 getter，以及旧 `registerMixinPreapplication` preview adapter；不恢复已删除的 `getUnmappedClasspath(String)` 及其 accessor。专用 Legacy fixture 的观察不再构成这些入口的保留义务，也不要求为维持它们保留专用兼容测试。此收窄不取消公开 DSL、下文明确保留的出口或真实任务/输出/属性值协议；文档同步本身不证明代码或测试已清理。

此接受支持记录决定及必要规划准备，不是生产实施、发布或执行确认，也不是代码已实现、独立实现评审通过或行为验收完成的证明。

## 决定

### 以显式输入的函数组织行为，状态留在原对象

内部以选择、任务注册和接线函数组织可独立变化的协议。下层函数只接实际需要的领域值以及原 `Provider`、`TaskProvider`、`Configuration`、`FileCollection` 等 Gradle 句柄；需要多个输出时，可返回无行为的小型结果。结果不得变成服务容器、任意导航入口、注册表、闭包集合或另一套生命周期。

可变状态继续由原 DSL、`Property`、`SourceSet`、`Configuration` 和 Task 实例持有。平台家族装配可以使用具体 target 及 `target.main` 配对关系双方；底层选择、转换及消费机制不凭整个 target / compilation 查找其他成员。不另建巨型 context、职责服务或通用 pipeline / effect 框架。惰性 artifact view 仍可能触发 Gradle 解析，不把这种函数式组织称为无副作用的纯计算。

### 用泛型 Joined/Split 纯数据表达固定游戏拓扑

接受 `GameShards<T>` 所表达的内部模型：`Joined(value)` 为单片，`Split(common, client)` 为两片。相同形状贯穿 named 游戏、AW 后的 `awGames`、Mixin→Strip 后的 `strippedGames` 文件句柄及内部 Mixin 任务投影；泛型复用角色映射与任务→输出/records 配对，不赋予容器职责方法、可执行闭包、注册能力或状态管理。操作仍由显式输入的函数承担，不增加 manager、阶段 wrapper 或 phantom type。

固定拓扑与有效消费分开：Fabric SERVER 仍保留 Split 静态注册，仅 common 进入有效游戏、operations 和 records；单元素有效列表不改称 Joined。CLIENT 编译/游戏查找为 client→common，records 为 common→client，顺序由各自协议明确投影，不由容器遍历偶然决定。side 与 primary 独立，未分割 main 可以是 CLIENT + primary COMMON；独立 client:data 的 preview 使用自身 AW，runtime common 仍沿 main 原 AW。具体行为细则继续由 Mixin owner 拥有。

容器只表达句柄槽位与拓扑，不证明 AW/Strip 字节阶段、primary 匹配、producer 完整、文件已物化或任务已执行。primary/other 任务身份与原命名保留，角色选择随原 Provider 延迟改变，不重新注册；形状与 primary 不匹配必须失败。

### setupFiles 是唯一原位组合点

在 compilation 原构造位置的 `setupFiles` 直接组合 AW、Mixin preview、消费输入与 sources。该处负责把自身依赖、AW、家族事实与出口正确配对；角色算法、Mixin records/state 协议、Transform 和反编译细节分别留在相应函数边界内。

接受取消全量参数总入口作为内部主组织的方向。根据已采用的设计评估，完整装配目前只有 `setupFiles` 一个调用位置，再转发一次全套参数没有复用收益；不因此把流程复制到各平台。注册保持一次，先后仍为 preview AW → 原本需要的本地 runtime 辅助 AW → Mixin/Strip → sources，不把注册放进 Provider 求值闭包。

`FinalJarTasks` 在组合末端投影同一组结果，保留下文明确列出的布局、签名及 getter 出口。新消费输入与保留的兼容投影共用同一生产链，不维持第二份注册实现；不因旧内部入口在 JVM 可调用而要求保留适配层。本决定不授权删除或破坏公开 ABI。

### 隐藏重复消费协议，保留紧密的一致性边界

Mixin 注册、独立 records、Strip、确切 sources 选择及原编译配置 `C` 的消费 state 属于同一一致性边界。它们共同保证记录、清理与消费属于同一 compilation 和有效视图，不拆成可由消费者任意拼接的独立协议。精确清理及其完整安全生命周期仍由 Codev 拥有。

开关、有效游戏选片和必要 operation 的选择在组合处形成一次相关的消费输入。Java/Kotlin、sources/反编译、Common stub 和 sync 复用该选择与准备依赖，不再分别解释 enabled、side、records 或 producer 规则。准备依赖须覆盖完整有效非项目 compile 的原生文件/Transform producer、有效游戏 producer 及必要条件 operation；artifact/component 投影不能代替生产依赖，调度 producer 也不能代替实际消费文件。sync 的原 runtime 和可选 sources 输入继续独立接入。

平台原料注册不读取 runtime 配置 `R` 来耦合解析。库一致性、metadata、patches/mappings 及 secondary 关系保持各平台原登记位置；供料函数不接管这些时序。Linkage 继续成组维护 C/R、include、AW/Mixins、outgoing 及 data/test 例外，不拆散其共同成立的关系。

Common 先选择已有参与 compilation，再投影同一消费输入；Common 源码 linkage 与 stub classpath 仍是不同关系。run 与 Common 保留各自回退：独立 client 存在但缺 data 时，Common 的 client:data 回退该 client，run 的 clientData 回退 target.data。不得为减少函数数量合成一个模式驱动的通用 selector，也不自动创建缺失 secondary。

## 保留的行为与兼容边界

以下是本次组织调整必须保留的边界，详细 Mixin 合同仍由 [Mixin PRD](../prd/mixin-preapplication.md) 和 [Mixin Spec](../spec/mixin-preapplication.md) 拥有；本文不复制其环境、输入或验证数据表。

- **公开配置与身份。** 保留 target / compilation DSL、继承与 target 转发 main；顶层 AW 仍是 main AW 转发，不成为全家族默认 AW。保留原 C/R、SourceSet 实例及名称、scope、capability、variant/classifier、classes/resources、任务/输出命名、用户追加 classpath 和本节明确保留的公开/兼容出口签名。非 test 沿原规则复用真实 Java main 变体或注册 feature；多 target 的 main 未必是 Java main，test 没有 outgoing feature 或归档任务。
- **兼容投影。** 保留 `FinalJarTasks` 包/类身份及 data class 生成接口，五字段名称、顺序和类型不变：`accessWidenTask: TaskProvider<AccessWiden>`、`decompileTask: TaskProvider<Decompile>`、`runtimeExtraFiles: Provider<List<RegularFile>>`、`compileMinecraftFiles: Provider<List<RegularFile>>`、`preapplyMixinsTasks: Provider<List<TaskProvider<Mixin>>>`；保留 `libraryArtifact` / `sourcesArtifact`、原 getters 与 protected `setupFiles` 原类型。内部使用 `GameShards<T>` 与 typed `Part` 表达拓扑和角色，不保留上述旧 Info Map/String 构造、getter 或 preview 适配层；形状与 primary 不匹配仍须失败。用于既有任务名、目录、operation/type key 和属性协议的 `joined` / `common` / `client` 字符串值必须保留，内部 `legacyName` 字段名可改；保留这些值不要求保留旧 `Provider<String>` getter 或 Map 入口，新链不逐阶段往返 Map。
- **构造与监听。** 原构造返回前的库、metadata、secondary 接线副作用必须仍在返回前完成。保持 `construct → publish → listeners → action`、listener 登记顺序、`onConfigured` 对已存在对象立即执行及迟到对象发布时执行的路径；重复配置只执行新 action。feature、SourceSet/sync、buckets、公共编译接线及 Linkage 保持原相对位置，不改为“所有对象创建后”统一阶段。
- **依赖选择。** 普通 raw、有效消费 view、完整 unmapped C/R 与角色 view 各守原用途。完整 unmapped 从原 C/R 分别派生 `raw + REMAPPED=false`，不由 mod/ordinary 并集重建；四个辅助角色配置继续保留。既有多图 `(id, variant)` 交集算法不在本决定中替换，也不声称其角色精度已证明。保留原生 producer、原项目/SourceSet 关系，不重建 ProjectDependency、不强制 JAR、不重选原依赖图。
- **Mixin 产品行为。** 沿用 PRD 的默认启用、解析前后配置 opt-out、常规 JAR 原生属性链适用范围、有效环境、四消费者、确切清理和失败传播。每个 compilation 使用自己的输入与预应用结果；关闭恢复原请求与旧视图，不安装本私有 state（包括 raw），不由该消费链调度 Mixin/Strip，但保留正常外部 producer、AW 与 remap。Common 保持既有跨 target API 交集，不为同 target 增加另一侧 operation 或双侧交集。
  此处保留的是跨 target 求交的语义，不限定后续可声明的参与平台。未声明 `stubFrom` 时参与者不变；显式参与关系见[架构规格第 6 节](../spec/target-compilation-architecture.md)。
- **开发与运行/发布隔离。** raw 只供上游，clean 只向编译、sources、stub、sync，不回灌 Mixin。preview AW 与 runtime 辅助 AW 来源保持区别：本地 runtime 辅助项沿原 target AW，独立 client 及其 data/test 的 runtime common 仍取 main 原 AW；sources primary 不由 Mixin side 推导。run/legacy 使用原 R 与原 AW/extra，项目发布沿原 Jar/remap/include 及 raw compile 解析链，不接 clean/records。发布任务若本来依赖编译，仍可能随编译失败。
- **惰性与失败。** 保持固定拓扑与 Provider 延迟的开关、side、primary，以及原 `withDependencies` / `afterEvaluate` 配置能力；不在配置期读取文件，不用占位回填、freeze 或新 scheduler。必要生产、清理、解析及 I/O 失败阻断消费并保留原因；Codev 正常可选失败/fallback 语义不变，不按日志升级错误，也不增加“clean 失败取 raw”的自动回退。注册/监听失败可能留有部分 Gradle 注册或已发布对象，本决定不引入事务回滚或自动重试。

## 取舍与理由

选择依据是缩小真实改动的传播面，而非追求函数或参数更少。AW 不需要知道 Mixin state，sources 不需要知道 records 协议，消费端不需要重学开关与调度规则；平台版本、家族与 Linkage 的必要联系则保留。这样仍可在局部理解每个操作的实际输入与结果。

提案比较过的主要替代方向如下：

| 方向 | 本决定的取舍 |
| --- | --- |
| 保留薄的全量转换总入口 | 可行；当前唯一完整调用点下会增加参数转发或隐式 receiver 查找。选择原位直接组合；不为旧内部总入口保留兼容委托。 |
| 新增职责对象或统一 TargetContext / 模式 pipeline | 可集中协议，但前者不符合用户的函数式偏好，后者扩大任意导航与时序知识；Common/run 等真实策略也不同，不采用。 |
| `GameShards<T>` 泛型纯数据，而非仅文件的非泛型 JoinedFile/SplitFiles | 文件与 Mixin TaskProvider 确有同形角色投影，泛型可复用结构并减少任务→输出/records 配对分支；非泛型文件形状同样可行，但任务拓扑需另行配对。代价是 T 仅表示句柄槽位，不证明字节阶段或合法 primary，调用处仍须明确阶段和顺序。 |
| 字符串 Map 作为内部主载体，或逐阶段 wrapper / phantom type | Map 允许缺键、混 joined 并外泄插序协议，不保留旧内部 Map 兼容入口；逐阶段类型重复拓扑，当前没有足以承担该成本的需求，不采用。 |
| 完全正交的 environment × source × feature 模型 | 当前没有支持该多对多模型的需求证据，不扩大领域与公开 DSL。 |
| 仅沿用旧函数边界或只拆转换函数 | 仍把消费协议、目录/sync/classifier 及家族装配知识留在不相关调用处，不足以满足整体解耦目标。 |

## 后果与 authority 衔接

正面后果是消费选择和准备协议有单一归属，新增既有类型的消费者可以取得需要的结果与依赖；AW、sources、平台原料和 sync 的变化不必穿透同一总入口。明确保留的兼容出口与新内部消费数据共用结果，避免通过重复注册维持兼容。取消旧内部入口支持意味着依赖它们的 JVM 调用者不获兼容保证；这不是公开 API 的整体兼容豁免。

代价是 `setupFiles` 仍承担显式配对和调用顺序，Gradle 共享可变状态仍存在，编译器不能证明注册恰好一次或副作用时点不变。Mixin/records/state、Linkage、变体选择和不同 fallback 的领域复杂度也不会消失。新增消费准备接线可能改变首次物化及 Transform 参数隔离时机，不能凭静态图断言行为等价或性能改善。

[全局 Spec](../spec/target-compilation-architecture.md) 拥有全局组织、Joined/Split 泛型纯数据形状、任务投影、兼容与时序合同；[Mixin Spec](../spec/mixin-preapplication.md) 引用全局形状、组合与兼容边界，并拥有 Mixin 注册、records/state、消费及失败协议与相应验证义务。唯一注册真源为原位 `setupFiles`，不要求具名全量总入口；Info 纯输入、无平台强转/nullable 哨兵、唯一注册及分片约束继续保留。具体协议与后续维护分别归各原 owner，不由本文复制。这些文档关系不证明生产接线或动态验收通过，也不授予实施许可。

实施规划引用上述规范；本 ADR 不替代 Plan 或执行图，也不据 ADR 作者自检判定实施 ready、任何 item 完成或 gate 通过。Mixin PRD 只拥有其既定产品范围，不成为整个 Cloche 的产品 owner；本决定不新增全局产品 PRD。既有发布 ADR、发布规范与 Codev authority 不变，正式依赖接入、提交/推送和发布均不属于本决定的执行范围。

## 未验证范围与重访条件

本决定没有新的构建或动态证据：Joined/Split 泛型纯数据模型已接受，具体内部标识符与精确签名仍可收敛；外部 JVM 使用及完整 ABI 未审计；新 Provider/TaskDependency 的首次生产、关闭/后配置、动态参与者、Transform 隔离及同请求 Tooling 先准备后建模仍需真实证据。四类消费者的原生依赖、对象身份、实际文件物化须分别证明，不能靠旧预生成结果或 Provider 存在推定通过；适用的 V1–V7 义务仍由 Mixin Spec 拥有。

configuration cache、性能、支持矩阵及正式 Codev 二进制来源没有新增证明。最终 IDE 导入、模块/输出根与符号/PSI 未验证，沿既定取消决定不在本轮，也不换名恢复门槛。角色精度、Forge clientTest、sources/javadoc 普通布尔时序、Common 环/孤立语义及测试环境迁移不因本 ADR 被修复或扩大为本次工作。

出现以下事实时应重访此决定；任何改变公开支持或产品语义的取舍须先由相应 authority 协调：

- 文件与任务不再需要同形角色投影，或真实阶段/合法拓扑分化使泛型结构的配对成本超过复用收益。
- 出现多个真实构造位置，需要复用同一个完整转换组合，薄入口能实质减少重复装配或漏步骤风险。
- 保留原 DSL、本决定明确保留的公开/兼容 ABI、构造返回前副作用、立即/迟到监听或运行/发布隔离与所选函数边界发生不可调和冲突。
- 实际首产、后配置、Transform 或 Tooling 证据表明现准备协议无法在不回流消费者知识、不增加环的条件下满足四消费者合同。
- 角色身份算法的实证缺口需要改变选择语义，或实际出现多对多 environment/source/feature 需求；它们不能被当作普通函数提取顺带处理。
- 真实性能、configuration cache 或依赖来源约束要求调整结果持有方式、组合位置或协议所有权；未测量的预期收益不足以重写边界。

## 前次修订的作者静态自检（历史范围）

本次写前全文读取、写后全文回读本 ADR，并核对实际差异、本地引用可读性及 Markdown 结构。其他文档的同步事实使用 Handoff 中 Router 已可靠采用的原始 Results 摘要，未重新读取或独立验证其正文。接受依据仍为已记录的用户语义接受与正式文档授权；既有静态独立评审结论不作为本作者重新独立验证或动态通过的证明。

作者对照确认本次仅收敛过时协调提示与 owner 导航；同一决定身份、日期、接受范围、模型与内部名字可变的区别、取舍、兼容、未验证范围和重访条件保持不变。静态内容自检通过；不构成独立复核、源码现状验证或实施 ready，也不判定任何 item/gate。未读取其他文档正文、生产实现、历史动态日志或外部 Codev 原件，未运行构建、测试、原型、下载、安装或浏览器复验。
