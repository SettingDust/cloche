# Target / Compilation 函数式组织与行为等价性

本文是 Cloche 全局 target / compilation 内部组织的技术规范入口，承接 [函数式组合 ADR](../adr/target-compilation-functional-composition.md) 已接受的函数式方向与 Joined/Split 泛型纯数据模型。本文中的“必须”“不得”“可以”分别表示要求、禁止与允许的实现选择；要求适用于组织调整后的实现，不表示当前源码已经满足、实施已获授权或行为已验收。

## 1. 范围、所有权与生效关系

本规范拥有跨 Fabric、Forge、NeoForge 及 Common 的职责边界、依赖方向、状态归属、原配置与输出身份、装配时序和行为等价性条件。范围覆盖平台供料、家族装配、依赖选择、转换组合、编译消费、Common、Linkage、run 及发布出口；只拆转换函数不足以满足本合同。

| 依据 / owner | 本文采用范围 |
| --- | --- |
| [领域 Context](../../CONTEXT.md) | target、compilation、Common、feature、variant、capability 等领域含义；不重命名公开领域。 |
| [函数式组合 ADR](../adr/target-compilation-functional-composition.md) | 已接受的函数式方向、Joined/Split 泛型纯数据模型、唯一原位组合、兼容承诺、非目标及重访条件。 |
| [全局设计提案](../proposals/target-compilation-architecture.md) | 解释耦合、数据流及方案取舍；仍为非权威设计来源。本文不把其示例函数、参数清单和类型名提升为长期 API。提案的历史 Draft/ADR skip 说明不撤销后来 ADR 的接受。 |
| [Mixin PRD](../prd/mixin-preapplication.md) | 仅复用既定预应用产品范围、默认与 opt-out、环境、消费及运行/发布隔离；它不拥有整个 Cloche 的产品要求。 |
| [Mixin Spec](mixin-preapplication.md) | 独占 `S/C/R` 的预应用含义与来源/身份、环境及 AW 分片、独立 records、Strip、原 `C` 消费 state、mapping/context、四消费者的精确文件语义、失败/缓存与 V1–V7。本文拥有它们在全局组织中的衔接，不复制第二套表或算法。Codev 引擎及精确清理继续经该 owner 引用。 |

### 规范衔接

compilation 的原 `setupFiles` 是唯一原位组合与注册真源；[Mixin 统一转换职责](mixin-preapplication.md#compilation-统一转换职责)、其 Task/Transform 依赖图及 V3 与本文第 5 节采用同一所有权，不要求 `registerCompilationTransformations` 作为内部全量总入口。必要旧入口仅按第 8 节提供兼容适配，不构成第二条装配路径。

本文拥有内部固定拓扑、阶段投影与兼容合同，Mixin owner 继续拥有有效环境、分片与 records/state、四消费者精确文件及失败/缓存合同。两者必须同时成立：Mixin 的输入表述须与本文 Joined/Split 形状及第 8 节旧 Map/String 边界一致，旧 Map 兼容许可不得作为内部各阶段继续以 Map 为主载体的例外。Info 纯输入、无平台强转/nullable 哨兵、唯一注册、身份、完整消费与生产时序义务不因模型或入口变化而放宽。各 owner 的历史 authority/验收记录保持其原范围，组织条款一致不等于模型衔接、实现或动态验收已经全部完成。

本文不修改 PRD、ADR、Mixin Spec、发布或 Codev authority；不规定实施步骤或任务图，不形成实施许可或 readiness 判断。不增加公开 DSL、职责服务、总 context、registry、通用 pipeline、freeze 或调度器。不修角色精度、Forge clientTest、sources/javadoc 普通布尔时序、Common 环/孤立语义或测试环境迁移；正式依赖升版、发布、提交和推送不在本范围。最终 IDE 验证沿 Mixin owner 的取消决定，不换名恢复。

## 2. 领域与状态归属

具体 target 必须继续聚合 loader、游戏/loader 版本、mappings、metadata conventions、main/client/data/test 家族、run 和最终产物选择。compilation 必须继续拥有自己的 SourceSet、依赖 collectors/attributes、AW/Mixin 声明、原 compile/runtime Configuration 以及输出。顶层 `MinecraftTarget` 的公开继承与 main 转发保持；target AW 仍是 main AW 的转发，不得变为全家族默认 AW。`Compilation.mixins` 仍是本项目资源声明，不等于外部预应用 sources。

Common 是共享源码关系及既有参与 target 的 API 交集，不是游戏 common 分片、独立游戏 runtime 或可伪造的 clean main。feature 是非 test 的 Gradle 输出接线，不是另一个 compilation；多 target 中的 main 不一定是 Java main SourceSet。

可变状态必须继续由原 DSL、Property、Configuration、SourceSet 与 Task 实例持有。函数可以返回被多个真实消费者使用的 Provider、TaskProvider、FileCollection 或无行为的小型结果；结果不得含服务、可执行闭包集合、target 回引用或另一套注册生命周期。不得为缩短参数而把任意导航藏进 receiver、巨型输入包或隐式全局状态。

平台家族装配可以使用具体 target、`target.main` 和关系双方；下层选择、转换、消费机制必须只取得所需领域值与原 Gradle 句柄。真实 `Project` 注册能力可以作为输入，不需包装成服务。建立惰性 artifact view 可能在后续求值触发解析，不能把这种函数组织宣称为无副作用的纯计算。

### 固定游戏拓扑与句柄阶段

内部游戏主载体必须采用泛型纯数据的有限形状，本文记为 `GameShards<T>`：单片 `Joined(value)` 或双片 `Split(common, client)`。同形结构贯穿 named、AW 后、Mixin 派生与 Strip 后游戏句柄，以及内部 Mixin `TaskProvider` 角色投影；映射、选择和注册由显式输入的函数承担，容器不提供职责方法、注册能力、可执行闭包或状态管理。`T` 复用句柄槽位及任务→输出/records 配对，不增加逐阶段 wrapper、phantom type 或执行器。具体类型、字段和函数标识符可以调整，有限形状及投影语义已接受，不再作为未选方案。

文件阶段可分别记为 named、`awGames` 和 `strippedGames`；后者必须是 AW→Mixin→Strip 的最终游戏结果，不代表 vanilla 或全部编译依赖。容器类型本身不证明实际字节阶段、primary 合法、producer 完整、文件物化或任务执行；各阶段必须绑定对应原生产句柄并按第 6、9 节验证。运行辅助 extra、外部依赖和完整消费输入各守原协议，不强塞进游戏拓扑。

固定形状与有效消费必须分开：Fabric 的 SERVER 仍保留 Split 静态注册，仅 common 进入该视图有效游戏、operations 和 records；单元素有效列表不得改称 Joined。CLIENT 的游戏查找与编译次序为 client→common，records 次序为 common→client；joined 各一片。顺序必须按用途显式投影，不依赖容器遍历、Map 插序或反转偶然得到，具体记录及清理行为仍归 Mixin owner。

side 与 primary 必须独立惰性选择：未分割且未 includedClient 的 main 可以是 CLIENT + primary COMMON；独立 client/includedClient 的 sources primary 为 CLIENT，joined 为 JOINED，data/test 沿所属家族事实。不得从 primary、distribution 的 common 字样或名称前缀推导 Mixin 环境。选择在合法首次解析前读取原 Provider 的最终值，开关、side 或 primary 后配不得重新注册拓扑；形状与 primary 不匹配必须失败，不猜测或回退主片。

## 3. 依赖方向与知识边界

下表的职责可以用不同函数名、文件划分和符合第 2 节的内部结果实现；必须保留输入权限、实际副作用及输出的对应关系，不把已接受的有限游戏形状重新开放为任意 Map。单行 `dependsOn` 等接线可以原位表达，不要求逐项创建浅包装函数。

| 边界 | 必须掌握与产生 | 不得向其传播的知识 |
| --- | --- | --- |
| 平台原料注册 | 显式版本、namespace、目录、mappings 等原料；在原位置注册一次，返回现有游戏/库/patches/运行辅助句柄 | 不接管 compilation 的 runtime 对齐、家族监听或 Mixin 消费规则；不从 `R` 解析供料。 |
| 平台家族装配 | 具体平台、main 与 secondary 配对；库一致性/继承、metadata、平台事实及原登记时点 | 不在各平台复制 AW/Mixin/Strip/sources 全流程。 |
| 依赖选择与 buckets | 原配置及明确角色输入；惰性 view、角色选择和原 collectors/继承接线 | 不查找游戏 side、records、sources 或 run；view 求值不注册任务。 |
| compilation 的 `setupFiles` | 配对自身输入与家族事实；原位组合 AW、preview、消费输入、sources，末端形成兼容投影 | 不实现角色算法、records 协议、Transform 细则、反编译内部配置。 |
| AW 注册 | 具体原料、AW 文件和原条件准备依赖；返回原任务/分片句柄 | 不解释 Mixin 开关、records、消费 state 或 Decompile。 |
| Mixin preview 一致性边界 | 同 compilation 的 sources、原始解析输入、分片、mapping/context、operation；共同拥有 Mixin/records/Strip 与原 `C` state 接线 | 不允许消费者独立拼装 state/records；不接 runtime extra 或 sources 输出选择职责。 |
| sources 注册 | 已选择的 input、有效 libraries、preparation 与原输出 | 不再判断 enabled/side，不读取 records 或自行枚举 operation。 |
| 消费准备与接线 | 同次选择的有效外部 compile view、游戏及完整准备依赖；向现任务和 stub 投影供给需要的部分 | Java/Kotlin、Common stub、sync 不重学开关、side、records 或条件 operation。 |
| Linkage / Common 源码 / run / 归档 | 各自的真实关系与出口，保留平台差异 | 不依赖 preview 结果决定 runtime/发布；不合并 Common/run 的不同回退。 |

必须使 sources 输出/反编译配置的变化不穿透 AW 注册接口，sync 物化策略的变化不穿透 classifier 接线，平台输出/版本规则的变化不扩散到 Common/Mixin 消费机制。库一致性、家族归属及 Linkage 的成组关系则必须保持联系，不能以函数数量或参数更短代替解耦。

数据流必须单向：平台 named 原料和上游 raw views → 本 compilation 的 AW → Mixin 派生游戏与独立 records → Strip 最终游戏/确切 sources → 编译、sources、stub、sync。游戏与内部任务各按第 2 节保形配对，外部依赖沿原生 view/Transform 链保持身份；原 runtime 和项目发布保留各自原链。clean 不得回读为 Mixin/AW/remap 的上游输入，依赖准备不得回接自身 raw producer 形成环。

## 4. 平台供料与配置身份

### 平台注册和家族对齐分离

Fabric 必须保留 common/client 原料、原库配置、intermediary/mappings 及运行 remap classpath 的原 Provider 分支、任务名、输出和注册时点。库依赖、main 创建、库与 main `R` 的一致性、localImplementation 继承、secondary listeners、mappings 与 metadata rule 必须留在原装配位置；不得把供料函数变成 detached runtime 解析。client 库只在原独立 client/includedClient 路径对齐相应原 `R`；secondary 继承既有库，不把共享库重新对齐每个 secondary `R`。

joined Forge/NeoForge 必须保留单片供料，以及 patches、mappings、universal、libraries、client-extra 的不同用途。版本条件控制的 implementation client-extra 集合与 legacy 原直接使用的 generateClientExtra 输出不得混用；不为 joined 伪造 Fabric 双片接口，也不因 server run 创建另一 SERVER preview。

平台在原 Info 构造位置以纯输入提供 Fabric Split 或 joined Joined 的 named Provider，并分别提供惰性 primary、家族 side 事实、本地 runtime 辅助 named/AW 输入及已有外部 extra 句柄。建立形状不解析文件、不预注册下游任务、不提前读取 primary，也不以是否创建独立 client compilation 改变 Fabric named 拓扑。原供料任务及 secondary 复用关系不因包装形状而重新登记。

joined 输出必须继续惰性依赖原 `minecraftVersion`、`loaderVersion`、namespace/suffix 和 outputDirectory：目录仍为原 `build/minecraft` capabilitySuffix 目录，文件名仍为 `$loaderName-$minecraftVersion-$loaderVersion-$suffix.jar`；patched suffix 为空 namespace 时沿原 `named`，client-extra 为 `client-extra`，named remap 为 `named`。`loaderVersion` 必须保留原 Property/Provider 链，不得提前 `get()`、拍快照或认为目录已含该版本。原 namespace 决定 patched/remap 输出的分支、producer 和任务名保持。

### 原配置、角色和 producer

原 compile/runtime Configuration（下文沿 Mixin owner 记为 `C/R`）与 SourceSet 必须保持原实例、名称、scope、attributes、capability、所选 variant/classifier、classes/resources 与生产依赖。不得重建 ProjectDependency、强制 JAR、重选原图或覆盖用户追加 classpath；新增游戏可以沿原方式以文件配置包装，不能据此重建 `C/R`。

必须区分以下查询，不得用一个通用“外部文件”结果互相替代：

- 上游非项目 raw view：显式 raw，仍保正常 remap 与各自 compile/runtime 角色；供 AW、Mixin 及需未清理输入的处理链。
- 有效非项目 compile view：继承原 `C` 的最终消费状态，供 stub/sync 及准备依赖；不得一律改为 raw。
- 完整 unmapped C/R：分别直接从原 C/R 排项目并请求 `raw + REMAPPED=false`，保留其他属性和原生 producer；normal remap extraClasspath 仍取得两份及原 intermediary 游戏。不能由 mod/ordinary 角色并集重建。
- 角色 view：保留现主/辅助图 `(id, variant)` 交集及其原生依赖，compile/runtime 各自 mod/nonMod 四个辅助配置保持。角色分类和完整 unmapped 是不同用途。
- 发布解析：raw 非项目 compile 加原 `C` 的项目 view，另外接原 AW/extra，不改用 R 或 clean SourceSet。

角色算法的精度未由本合同证明，不在此次用单图替代、删除辅助图或新增 fallback 修复；保持算法不等于证明它对任意 classifier/substitution/capability/file 输入完整。预应用确切来源/身份要求继续由 Mixin Spec 拥有。

原 buckets/collectors 必须在 feature 之后的既有登记位置接通一次。早于 buckets 建立的 view 必须延迟按原配置定位，不提前解析未创建的角色配置。不得取 `.files` 后重新包成无来源集合；投影必须保留原 FileCollection/Transform producer 关系。

## 5. setupFiles 的唯一原位组合

内部主组织必须是 compilation 构造中原 `setupFiles` 位置直接组合窄函数，不再把全量输入转发给另一个总入口，也不把同一流程复制到平台。各项 Gradle 注册在原位置执行一次，不能放进可重复求值的 Provider 闭包。必要旧签名的兼容保留按第 8 节处理。

组合必须保持 preview AW → 原本需要的本地 runtime 辅助 AW → Mixin/Strip → sources 的注册先后。固定分片拓扑与 Provider 延迟的 enabled、side、primary 选择必须分开；不得靠 nullable 哨兵、平台强转、占位 Property 回填、任务工厂或新全局阶段完成初始化。Info 继续只提供输入，在组合点展开实际所需内容，不把整个 Info/compilation 传入每个机制函数。side 的家族事实由装配显式提供，不从名称前缀猜测。

AW preview 使用同 compilation 的外部 compile 输入与自身显式 AW；原本地 runtime 辅助项沿 target AW，独立 client 及其 data/test 的 runtime common 沿 main 原 AW。primary/sources 的原选择不由 Mixin side 推导；未分割且未 includedClient 的 main 即使为 CLIENT 也不新增 runtime common，本地辅助 extra 仍只在原 distribution client 分支惰性加入。上述具体分片、来源、顺序及开关合同以 [Mixin Spec 的统一转换职责](mixin-preapplication.md#compilation-统一转换职责) 和相关分片条款为准。

AW 注册必须保留原 primary/other 任务身份、数量、名称、目录与输出规则：Joined 沿原 primary，Split 沿原 primary/other，再由 primary Provider 把两份输出惰性投影回 common/client。primary 切换只改变原任务的输入和角色投影，不把任务按 common/client 重命名或重注册。原本地 runtime common 辅助 AW 继续独立沿原 common 名称与路径；primary/other 输出及 sources 输出仍随原输入/目录 Provider 惰性变化。

| 阶段 | 必须输入与输出的配对 |
| --- | --- |
| named → preview AW | 固定游戏形状、独立 primary、同 compilation 的 raw compile 与自身 AW → 原 primary 任务及保形 `awGames`。 |
| 原 runtime 辅助 AW | 原本地 named、raw compile 与 target AW → 原辅助句柄；独立 client 家族改取 main 原 AW，不取本份 preview common。 |
| AW → Mixin | `awGames`、确切外部 sources、raw 查找输入、side、mapping/context → 同形内部任务；各任务分别投影 `outputFile` 与独立 records。 |
| Mixin → Strip | 每片派生游戏及本视图全部有效有序 records → 同形 `strippedGames`；确切外部 sources 经同一 records 清理并保原生身份。 |
| 选择 → sources / 消费 | 最终 enabled、独立 primary/side 与同一 preview → 有序有效游戏、sources 主输入、stub 次序及必要 operation，再接 preparation 与原出口。 |

表中只定义阶段、权限与对应关系，不冻结函数数量、参数表或字段名。形状存在不证明这些生产阶段已完成；SERVER 未选中的 client 静态句柄不得泄漏到有效消费链。

Mixin 注册、独立 records、Strip、确切 sources 选择和原 `C` 消费 state 必须处在同一一致性边界。可以内部细分函数，但不得导出让调用者任意配对的“安装 clean state”协议，产生有 state 无 records 或串用别的 compilation 的结果。正常 mod remap 的空 namespace 早退只影响原 remap，不得跳过 preview/Strip/sources 的注册与有效选择；精确清理和安全生命周期仍由 Codev 拥有。

最终开关、有效游戏、sources primary、stub 游戏顺序和必要 operation 必须在组合处形成一次相关选择。source input 与 compile games 共用同一 preview 结果，runtime extra 沿原 Provider 独立组合。开启时才把最终清理形状投影为本视图有效游戏及 sources primary，并以同组任务按记录次序形成有效 operations/records；关闭时恢复旧游戏/依赖请求，必要 operation 直接为空，不为得到空列表而读取本轮 operations Provider，保留正常外部 producer、AW 与 remap。未被请求的 preview 注册可以存在；不得因关闭复制整条注册链，或把关闭解释为原 `C` 请求 raw（包括不得安装本私有 state）。

`FinalJarTasks` 必须在组合末端由同组句柄投影；新消费输入与兼容出口共用唯一生产链，不得为兼容额外注册一份。内部结果名及非游戏数据的存储方式可以选择，游戏形状遵守第 2 节；不得破坏第 8 节布局，也不得把兼容的完整 operation Provider 改写为关闭分支的条件空列表。

## 6. 统一消费输入、准备与实际物化

每个具体 compilation 必须对外提供一次已相关联的内部消费输入：完整有效非项目 `C` view、有序有效游戏、stub 所需的既有游戏次序，以及 preparation。游戏形状只在相应用途的消费边界惰性投影为列表，保留每个原 Provider/producer，不配置期取文件值或借投影注册任务。内部数据只承载现句柄，不能成为完整 compilation 快照、服务容器或任务发现器。Java/Kotlin 只取得必要准备依赖并继续消费原 SourceSet；Common 先选参与 compilation 再投影其消费输入；sync 另接原 runtime 和可选 sources，不能要求全部消费者接收 `FinalJarTasks`。

preparation 必须保留完整有效非项目 `C` 的原生 files/Transform producer、有效游戏 producer 及随最终开关选择的必要 operation。可以用真实 FileCollection 的文件关系与 builtBy 等原生依赖能力组合；不得新增汇总 Task。关闭时仍需完整外部 producer、原 AW/正常 remap，且不由该消费链调度本次 Mixin/Strip。

preparation、artifact/component 投影和真正文件物化是三个不同义务：

- 任务依赖发现必须从原生文件依赖及必要条件 operation 接通，不通过 `stubGames.get()`、`resolvedArtifacts.get()`、`.files` 枚举或读取记录/输出内容猜测任务。
- Common stub 必须按同一参与者 Provider 保留分组、外部 component 信息及游戏原角色；对象投影不能代替 producer 闭包，也不伪造游戏 component。关闭时 stub 的游戏次序沿原 `runtime extra → primary`，与 SourceSet 的 `primary → extra` 追加不同。
- Java/Kotlin 保留原 `C`/SourceSet 文件身份和用户追加；sources 使用已选输入和有效 libraries，并接同一 preparation；sync 必须在既有执行位置实际物化有效外部 compile/游戏及独立原 runtime，按原 downloadSources 选择 sources Provider。只执行 producer 或保存 Provider 不算完成消费。

消费者不得重新判断 enabled、side、records 或枚举 Mixin。模型解析不会因存在 `Task.dependsOn` 自动执行任务；直接 Tooling 必须沿既有准备流程，在相应 Transform 参数读取/隔离和消费解析前完成所有必要生产。同请求先任务后模型的既有方式可以复用，真实先后必须有动态证据；不得在 model builder 内另建调度器，不支持任意第三方物化入口的自动执行推定。

四类消费者的完整成员、身份/scope、sources 选片、原生项目关系、清理与首次生产要求继续以 [Mixin Spec 的消费出口](mixin-preapplication.md#消费出口)、[原生投影](mixin-preapplication.md#gradle-编译视图与-ide-原生投影) 及 V1–V7 为准。本节不建立另一套消费者表。Gradle 原生排序/分组许可不取消游戏优先次序，也不允许用文件集相等代替身份、闭包、scope 和生产关系检查。

## 7. 构造时点、关系与回退

### 构造、发布和监听

[LazyConfigurableInternal](../../src/main/kotlin/earth/terrarium/cloche/target/LazyConfigurableInternal.kt) 的顺序必须保持：首次 construct 完成 → 发布 valueProperty/isConfigured/internalValue → 按登记顺序执行 listeners → 用户 action。对已有对象登记 onConfigured 必须立即执行，对迟到对象必须在其发布时执行；重复 configure 只执行新的 action，不重新构造或注册。main 沿原 single/multi-target 容器入口装配，不套用新的全局“配置完成”事件。

以下副作用必须在各自原 construct 返回前完成，不能挪到对象发布后的 listener：

- Fabric 独立 client 的互斥检查、库对齐与继承、其 data/test 库 listeners，以及 main metadata 对 client Mixins 的接线。
- includedClient 的互斥检查、client 库对齐 main R、main 继承和 target data/test 库 listeners，再创建并返回 includedClient。已存在 data/test 的立即路径保持。
- Fabric client data/test 对 target 对应 data/test 的 metadata listener 登记；它与父 client 工厂的库 listener 不是同一个协议。父库 listener 须继续早于 Handler 公共编译/Linkage listener。
- Forge/NeoForge data/test 自身的原 client-extra implementation 接线。

Handler 必须保持原相对顺序：非 test feature → SourceSet 接线（含原 sync 位置）→ buckets → metadata/copyMixins/AW merge/Jar inject 的适用分支 → Java/Kotlin 准备接线 → 原 outgoing/attributes；同一发布 listener 再执行原 Linkage/data 资源关系。游戏 classpath 追加及正常 remap 的 afterEvaluate 登记继续留在 compilation 构造位置。Common 源码关系仍沿 TargetApplicator 在 addTarget/handleTarget 后的原登记路径，不按函数类别重新排程。

SourceSet 目录、sync、归档 classifier 必须形成可独立变化的接线边界，但保留同一调用位置与先后。非 single-target 的 Java/resources/Kotlin 目录、plugins.withId、原 Jar 的 devClassifier/输出目录及 include/remap 的 classifier 保持；test 的原提前返回保持。`withSources/withJavadoc` 的普通布尔值时序不在此次改善范围。

### Linkage 与 Common

Linkage 必须继续成组维护 compile/runtime、include/constraints、AW/Mixins、outgoing classes/resources 及 data/test 例外；不得拆成调用者必须自行排序的一组独立动作。依赖为 test 时保留原 SourceSet.output 追加，不伪造 outgoing feature；test 消费者保持其原 incoming 角色。data 的 WITHOUT_DATA 请求、资源变体和迟到 secondary listeners 必须保持，避免改变 datagen 回环关系。

Common 源码 static linkage、buckets、AW/Mixins、Expect/Actual、class-extension 与 includedClient weakTreeLink 仍是原关系；它们与 stub classpath 分开。不得为消费输入重构顺带改掉这些关系或现有 Common secondary 创建路径。

Common 与 run 必须分别选择已有家族值，不使用带模式参数的统一 selector，不为选择自行创建缺失的具体 secondary。Common main 取具体 main；Common main 的 data/test 优先 target 对应 secondary，否则 main；Common client 取独立 Fabric client 或 main，其 data/test 在独立 client 存在时优先 client secondary、缺失则该 client，否则取 target secondary、再 main。Common stub 仍仅对既有参与 target 求交，不额外增加同 target 另一侧 operation。

run 保留自己的原 Provider 回退：Fabric server 取 main、client 取独立 client 或 main；clientData/clientTest 取独立 client 对应 secondary，否则 target 对应 secondary。例如独立 client 已存在但缺 data，Common client:data 回退该 client，run clientData 回退 target.data；若 run 所需 secondary 最终仍不存在，沿既有 afterEvaluate 检查失败，不自动创建。joined run 沿原 main/data/test 与平台差异，clientTest TODO 不在此次修复。

## 8. 兼容出口、运行/发布及失败归属

公开 target/compilation DSL、继承、main 转发和原 Gradle 配置能力必须保留。非 test 沿原规则复用真实 Java main 变体或注册 feature；test 无 outgoing feature 或归档任务。capability、variant/classifier、SourceSet/classes/resources、task 名、目录/输出、原 Property/TaskProvider 后配置及用户追加 classpath 都是兼容边界。

现有 `earth.terrarium.cloche.target.compilation.FinalJarTasks` 是无 internal 修饰的 data class。必须保持其包/类身份、data class 生成接口、原构造字段的名称、顺序和类型，以及原 getters：`accessWidenTask: TaskProvider<AccessWiden>`、`decompileTask: TaskProvider<Decompile>`、`runtimeExtraFiles: Provider<List<RegularFile>>`、`compileMinecraftFiles: Provider<List<RegularFile>>`、`preapplyMixinsTasks: Provider<List<TaskProvider<Mixin>>>`，并保留 `libraryArtifact`、`sourcesArtifact`。`setupFiles` 保持 protected 及原类型；`finalMinecraftFile`、`sources` 和原 compile/runtime/operation getter 继续投影同组结果。不能因新内部消费结果更窄而删除或重排旧字段。

必要旧 Map/String 入口只在兼容边界校验并单向转成新形状或角色：named Map 的完整键集合必须恰为 `{joined}` 或 `{common, client}`，空、缺键、额外键或混合形状必须失败；primary 的旧 common/client/joined 标签必须精确转换，未知标签或与 Joined/Split 不匹配时在相应边界求值失败，不默认选片。旧 `primaryMinecraftPart: Provider<String>` 等可见 getter 必须继续投影原 common/client/joined 字符串值；即使 JVM 泛型擦除后 descriptor 相同，也不得把值协议改为 enum。新内部角色可以用枚举等有限表示，必要反向 getter 仅投影原标签/槽位，不得逐阶段往返 Map、排序或重新注册。既有 Info/fixture 对字符串值和键的观察属于兼容核对输入，不能仅改测试期望就宣称兼容。

原 internal Kotlin 函数也可能有外部 JVM 调用者；不能凭源码可见性就证明安全删除或改变 JVM owner/descriptor。必要旧签名可以单向适配到同一已组合结果；不得借适配重新注册、保留第二套逻辑或恢复旧总函数为内部主组织。内部 helper 名称、文件和无公开承诺的结果字段可以变化，但必须保留第 2 节形状语义；受影响可见入口的兼容性必须有针对性证据，保留布局不等于全库 ABI 已审计。

runtime/run/Forge legacy 必须继续使用原 R、AW/extra 与平台辅助输入，不读取 clean/records。Mod outputs 沿原 runtime 项目/Mod ID 归属，不改用 compile 角色选择。项目产物仍走原 Jar → 按 namespace 选择 RemapJar/原 Jar → IncludeJar；include 保留原非传递解析、属性、collectors、includeTargets 监听及 manifest。发布 remap 解析仍为 raw compile/原项目 view 加原 AW/extra，不读取新的 clean SourceSet.compileClasspath。Fabric client Jar 原 main 注入、target.finalJar 的 client include 或 main include 选择，以及 Forge main include 保持。发布任务若原本依赖编译，仍可能随编译失败，不承诺绕过失败。

失败按实际 owner 传播：平台原料/依赖解析负责自身原因，AW/Mixin/Strip/Decompile 负责其处理失败，消费接线负责使相应消费者等待并观察必要失败；家族互斥、缺失 run 输入等原配置检查仍留原时点。不得吞错、把空集合或旧文件当成功，也不提供 clean 失败自动改读 raw 的回退。Codev 正常可选失败、部分应用及正常 fallback 沿其语义，不按日志升级为错误。

注册、construct 或 listener 抛错可能留下部分 Gradle 注册，发布后 listener 失败可能留下已发布对象；本规范不引入事务回滚、自动重试、once registry 或用户时序新修复。不同 target/compilation 的消费状态、输出和 producer 不得串用，保留 Gradle 原有任务调度及后配置能力；不新增锁、超时框架或并发服务。合法空输入、零记录失败、输入保护、缓存资格及失效全部沿 [Mixin owner](mixin-preapplication.md#失败空输入与缓存)，不以函数化放宽。

## 9. 可检查的等价性验收

以下是组织调整实现所需的检查，**不是本次已执行结果**。应复用既有测试与小型 fixture，比较同输入、同实际依赖来源和运行环境下的原行为与候选；对当前源码尚未满足的 Mixin 合同必须明确区分原缺口与重构回归，不能把既有错误冻结成新标准。无需为本文新建全仓引擎矩阵、通用证据账本或重复整套 V1–V7。

| 条件 | 最小充分方法与判据 |
| --- | --- |
| G1：全局职责及单一注册真源 | 静态沿平台→家族→setupFiles→窄机制→消费/关系/出口核对输入权限、状态位置、无隐藏 target 导航及原位一次注册。核对 sources/sync/classifier 的独立变化面、Mixin 一致性边界、无总参数转发及与 Mixin 职责/图/V3 的同向关系。检查 Joined/Split 泛型纯数据跨阶段/任务投影、无容器行为或内部逐阶段 Map 往返，且未以类型存在替代阶段/producer 证明。静态图不能证明实际无环。 |
| G2：平台供料、名字与版本惰性 | 对 Fabric split/included/未分割及 joined 的受影响接线比较原 task/configuration 名称、输出、Provider producer 与 namespace 分支；改变合法后配 loaderVersion 验证 joined 路径随原 Provider 更新。核对 primary/other 任务与角色的惰性映射、后配 primary/side/开关不改变注册数量/名称/路径规则；SERVER 保 Split 注册而有效 games/ops/records 只有 common，CLIENT 查找/编译 client→common 与 records common→client 分别验证。覆盖未分割 CLIENT+primary COMMON、独立 client:data 自身 preview AW/main runtime AW 和 Joined 单片，复用受影响 V3/V5。核对库仅原 R 一致性/继承与原 metadata/patches/mappings 位置，不以结果字段存在代替。 |
| G3：身份、依赖与公开兼容 | 比较原 C/R/SourceSet 实例、完整成员及 artifact/component/所选 variant/classifier、项目输出角色、scope、原生产依赖和用户追加；分别核对完整 unmapped C/R 与角色 view，不要求逐 artifact 保序。检查 DSL 使用及受影响 JVM descriptor、FinalJarTasks 五字段/生成接口/getters、protected setupFiles 原合同与旧适配未重复注册；核对旧 `Provider<String>` 实际字符串值及合法/非法 Map 键集合、未知角色和形状/primary 不匹配失败，不能只看擦除签名或改 fixture 期望。完整 ABI 未覆盖须明示。Mixin 精确清理及身份路线复用 V2/V5/V7。 |
| G4：构造与监听时序 | 覆盖 secondary 已存在和迟到两条路径、重复 DSL action、includedClient/client 互斥及原失败点；观察 construct 返回前的库/metadata/继承和 listeners 登记，return→publish→listeners→action 次序、父库与公共接线顺序。不能只验证最终配置值相同。 |
| G5：关系与策略 | 对 Linkage compile/runtime/include/outgoing、test 输出追加、data WITHOUT_DATA/资源及 Common static linkage 核对原关系；动态选择包含“独立 client 存在而缺 secondary”的 Common/run 不同回退，随后配置 secondary 再核对惰性参与关系。不新增具体 compilation，不顺带修 Common 环或 run TODO。 |
| G6：四消费者首产、后配与实际消费 | 按 Mixin V1–V7 核对 Java/Kotlin、sources、stub、sync 各自实际文件/身份、完整 preparation 与必要生产；开启、关闭及解析前双向后配都需覆盖。尤其关闭冷启动仍保完整原生外部 producer/AW/remap，stub extra→primary；不安装私有 state，条件 operation 为空且不为取空列表读取本轮 operations。核对同组 preview 的游戏/sources/records 投影及独立 runtime，不能以句柄形状代替生产。缺输出/记录及无旧 workspace 的首次生产、新进程和 file/Maven 路线按 V7；不能预跑 operation、用缓存或对象投影掩盖依赖缺边。新增 preparation 的 Transform 参数隔离时机必须实测。 |
| G7：Tooling 与隔离 | sync 实际物化与真实 Tooling 模型分别核对；验证同请求先准备后 builder 的实际顺序、原身份/项目/SourceSet/闭包/scope/有效文件，保留原生分组许可。run/legacy/项目发布输入沿原 R/AW/raw compile，检查无 clean/records 回流。复用 V2/V5/V7，不以结构模型或文件集相等代替真实模型。 |
| G8：失败与失效 | 组织调整不能吞掉原配置错误、必要 producer/清理/解析/I/O 失败或改变 Codev 可选失败；按受影响 V6/V7 验证阻断、撤故障恢复、输入保护与缓存失效。静态核对 raw→clean 无回边；实际任务图仍需动态无环证据。不增加自动 raw fallback、事务或重试承诺。 |

参数化接线观察可以覆盖平台/家族分类，不要求每个形态重复真实引擎全矩阵；但不能将某一 compileJava 成功扩展为四消费者成功。必须分开保留 Provider 来源、原生 TaskDependency、实际任务图、参数读取/隔离先后和实际消费的证据。结果必须注明所测来源/版本/平台与未覆盖条件；不得把历史独立 Gradle/JDK 或本地 Codev 路径写成新的全局基线。现工具链、测试运行来源与开发/正式二进制边界沿原 owner，不由本文变更。

文档作者自检只证明本文合同的静态一致性；未来实施等价性必须有上述实际证据。configuration cache、性能、支持矩阵、正式发布二进制可用性及完整外部 ABI 不能由组织图推导。最终 IDE 导入、模块/输出根、符号/PSI 不在本轮必跑验收，也明确未验证；真实 Tooling 模型不等于该证据。出现 ADR 重访条件或必须改变公开支持/产品语义才能满足合同的事实时，须报告具体冲突交相应 owner，不能按普通函数提取静默放宽。

## 10. 源码导航与验证边界

以下保留原规范调查的源码导航，用于定位结构、出口与时序约束；不表示每次文档同步均重新读取源码，也不是候选函数已经实现或动态通过的证明：

- [TargetCompilation](../../src/main/kotlin/earth/terrarium/cloche/target/compilation/TargetCompilation.kt)：原 setupFiles/FinalJarTasks、raw/unmapped/角色、AW/runtime extra/sources、remap/include 和 classpath 追加；[MixinPreapplication](../../src/main/kotlin/earth/terrarium/cloche/target/compilation/MixinPreapplication.kt)：sources 选择、records/state/Strip 及原条件依赖。
- [CompilationInternal](../../src/main/kotlin/earth/terrarium/cloche/target/compilation/CompilationInternal.kt)、[CompilationVariantCreator](../../src/main/kotlin/earth/terrarium/cloche/target/compilation/CompilationVariantCreator.kt) 和 [TargetHandler](../../src/main/kotlin/earth/terrarium/cloche/target/TargetHandler.kt)：view、目录/sync/classifier、feature/test、buckets、公共编译及家族 listeners。
- [FabricTargetImpl](../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricTargetImpl.kt) 的原料/家族构造与 init、[FabricCompilationImpl](../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricCompilationImpl.kt) 和 [client secondary](../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricClientSecondarySourceSets.kt)：库/metadata、primary、runtime AW 来源及立即/迟到路径；[ForgeLikeTargetImpl](../../src/main/kotlin/earth/terrarium/cloche/target/forge/ForgeLikeTargetImpl.kt) 与 [ForgeCompilationImpl](../../src/main/kotlin/earth/terrarium/cloche/target/forge/ForgeCompilationImpl.kt)：joined 版本路径、init、secondary 与 legacy 差异。
- [CommonTargetCreator](../../src/main/kotlin/earth/terrarium/cloche/target/common/CommonTargetCreator.kt)、[Linkage](../../src/main/kotlin/earth/terrarium/cloche/Linkage.kt) 和 [TargetApplicator](../../src/main/kotlin/earth/terrarium/cloche/target/TargetApplicator.kt)：参与者/对象投影、关系成组与 Common 源码登记。[Fabric run](../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricRunConfigurations.kt)、[Forge run](../../src/main/kotlin/earth/terrarium/cloche/target/forge/ForgeRunConfigurations.kt) 的对应选择路径和 [withCompilation](../../src/main/kotlin/earth/terrarium/cloche/api/run/RunConfigurations.kt)：原回退与缺失检查。
- [MinecraftTarget API](../../src/main/kotlin/earth/terrarium/cloche/api/target/MinecraftTarget.kt)、[Compilation API](../../src/main/kotlin/earth/terrarium/cloche/api/target/compilation/Compilation.kt)、[MinecraftTargetInternal](../../src/main/kotlin/earth/terrarium/cloche/target/MinecraftTargetInternal.kt)、[SingleTargetConfigurator](../../src/main/kotlin/earth/terrarium/cloche/ClocheExtension.kt) 的 target 构造路径与 [addTarget](../../src/main/kotlin/earth/terrarium/cloche/ClochePlugin.kt)：公开继承/main 转发、开关与装配前后位置。

上述导航不另立实现 owner；实际接线须同时满足本文 G1–G8 与 Mixin owner 的受影响 V1–V7。作者静态自检只核对文档合同、引用与结构，不能据此声明独立评审通过、规范全部同步、后继 Plan 就绪、生产等价或动态验收完成；每次实际检查范围及限制应随结果报告，不在本文累积运行记录。
