# Mixin 从 HEAD 重实现 Plan

本 Plan 只对应 [Work](work.md) 和唯一执行图 [Waves](waves.md)，输入为 [Context](context.md)、[PRD](../../prd/mixin-preapplication.md)、[Spec](../../spec/mixin-preapplication.md) 及 [弃置历史输入](discard-inputs.md)。用户已确认实施及同范围图/记录维护，持续授权未撤回。Router 已完整读取并可靠关联采用 R0V/R1/R2/R3R/R4P/R4M/R5 completed，判定 G0/G1/G2、GP/G3/G4 满足，选定 Plan 的限定本地开发交付完成；原 R0/R3/R4 永久保持 partial。本次只记录已采用完成事实和既有门槛判断，不重新执行 R5、重放已成效果或判新 gate。

## 权威、交付范围与边界

采用 Context 中可见的 PRD reuse / ADR skip / Spec reuse 评估；该评估及原编制时全文读取的 PRD/Spec 继续适用于同一确认范围；本次沿用可靠评估，不声称重新全文复核 authority。Spec 的最新正文优先于旧 Work 的状态和历史实现描述。没有需改写的产品/技术 authority，也不借本 Plan 固化新的引擎或发布决定。

所选实施范围仍有两个交付边界：撤去封闭名单内旧源码/测试/fixture 及旧四份派生 Work 文档，恢复固定 HEAD 工作树和索引；在此基线按独立设计交付 V1–V7 的本地开发功能验收。前者已执行效果按 R0 partial 及独立审计的采用结论保留，不再列作未来写操作。当前 G0 衔接历史恢复/删除、明确保护证据和剩余必要的当前输入可读性，不证明所有忽略内容/缓存/Codev 历史字节相同；G1–G3 证明逐个消费出口，G4 证明最终实现的跨入口首次生产、失败/恢复与缓存和总体合同覆盖。G0/G1/G2、GP/G3/G4 均已由 Router 判断满足，本 Plan 的限定本地交付完成，未知差额及 R0/R3/R4 原 partial 仍保留。路径 gate GP 只证明路径恢复，是完整 common/sources 模型实跑的必要前提，不能替代 G3/G4。执行 item、依赖及 Wave 仅在 Waves 中定义。

编译改动覆盖 target main、Fabric 独立 client、main/data/test 与 client data/test；只实例化既有配置的 compilation。公共开关是 target 的 `mixinPreapplication: Property<Boolean>`，convention true，不给 common 或 compilation 新增开关。各 compilation 自己的 compile 图决定 S，绝不从 runtime 或本项目 Mixins 补 sources。

包含必要的 runtime/发布隔离调整；保留 HEAD remapped/include、项目 capability、variant/outgoing、源码依赖与 Forge legacy 协议。旧附带 capability、test 生命周期和资源修复全部随旧改动弃置，不预先带回；若新实测证明其阻挡明确合同，只作有直接失败证据、最小根因修复及对应回归的范围内修正，不改 test outgoing/archive 或重建项目关系。会改产品范围或主要路线的问题必须报告，不能通过“普通细节”吞下。

不包含任意归档支持、nested 扫描、双侧操作/交集、引擎升级/refmap 重写、编译器插件迁移、发布接入/CI、配置缓存/isolated projects 额外承诺或最终 IDE 导入/PSI。最低 Gradle 9.4、toolchain 17 和生产依赖坐标不变。

## 基线保护与剩余核验边界

R0 的 11 tracked 恢复、129 授权删除、九明确保护文件执行前后字节一致及当时 Git 可见集合/空 diff 已采用；独立审计所见项目写命令只涉及 140 授权路径及对应索引。完整限制和原始获取边界见 Context，不要求新增原始报告文件或外部 Result 长期入口。全量后扫描 PermissionError、前快照丢失、根 `.git`/140 写路径外汇总净少 2 文件/9057 字节且无法定位归因、Codev 后比较未完成，均保持未知，不以“无额外变化”概括。

用户的必要保护是保留 PRD/Spec、新 Work、两个 XML、原始证据、缓存及相邻 Codev 等范围外内容。前九个明确保护文件已有当次历史字节证据，后续授权维护的新 Work 不须与旧字节相同。范围外对象没有新的写权限；丢失的全树历史快照既不可重建，也不是用户要求的全量同一性证明。R0V 已完成的核验面向明确对象/消费者：九个保护文件以及 Context 表内四份设计报告当前存在可读，供后续权威输入与开关/通知/Tooling/命名方法消费。报告内容和限制沿用已采用设计依据，不把可读性升级成其全部原始日志或旧成功的复核。

新的只读核验不能恢复未知文件、伪造基线、重扫两仓/所有缓存、停共享进程或改权限；具体输入缺失/不可读或新发现相关安全 gap 必须保留，阻断其受影响消费者，不能强行判等价。未知汇总差额本身无可定位对象或与后续实现的已证实依赖，不作为所有实现的无限等待条件；若出现新线索，再按实际影响处理。R0V/G0 关系仅在 Waves 定义；R0 原 Result 永远保持 partial，各消费者及最终验收的 V1–V7 保证由各自适用证据建立；R5 已核对并复用 R1/R2/R3R/R4P/R4M 及原 R3/R4 仍适用的有界证据，不继承旧 Work 的成功。

## 已完成 stub 与 R4 恢复边界

原 R3 partial 保留三处 Provider 接线、main 成功与完整 graph 失败，不能 resume。Router 已完整采用 R3R completed：完整六组 on/off stub、四种身份顺序及原生 Java/sources 项目选择回归通过，内部完整惰性 capability 纠正不再列为未来工作。历史方法与限制见 Context；G2 是 Router 已判满足的事实，不由本作者重新验收。后续只按真实改动影响定向复验，不重跑无变化全引擎矩阵。

[原 R4 报告](../../../build/reports/mixin-r4/REPORT.md) 的 partial 永久保留：sync 物化、runtime 准备、sources 接线及 fixture/runner/observer/verifier 已发生；baseline/off 模型通过，on 两次 build 后缺 clean S/游戏，原报告另将 scope 差异记为退化（最新标准语义解释见下节），完整 common sync 被 Windows 路径阻断。不能重做已成接线或清理现有 P/N/clean/sources/模型/Home/失败输出后盲重放。Context 保存派生比较文件被误覆盖后重新生成但无前快照、wrapper --version 非兼容证据等必要限制。

## Windows compilation 输出路径的已执行恢复策略

恢复只承诺既有角色在 Windows 上可用的 AW/sources 声明输出及完整 common/runtime 准备，不改变产品目标、模型 scope 或实质隔离。已采用路径诊断支持 PRD reuse / ADR skip / Spec reuse；既有 Plan 的实证必要最小兼容纠正许可及 Router 同范围判断继续适用，不增加 authority 写作或重复确认门槛。恢复安排及 R5 连接已获独立无阻断评审；R4P completed 与 GP 满足已由 Router 采用/判断，作者自检未代行该保证。

原 registerCompilationTransformations 在 TargetCompilation 内拥有共享 outputDirectory，AW 与 Decompile 均由它 zip namedMinecraftFile 声明输出。逻辑名 client:test/client:data 曾直接进入目录，在 Windows 声明输出访问/stale 清理时失败，早于 AW action。R4P 已在共享目录映射沿用 CompilationInternal.namePath 的 name.replace(TARGET_NAME_PATH_SEPARATOR, '/') 规则，令 client:test→client/test、client:data→client/data；不改公开 name/task/SourceSet/capability/operation key/side，AW 和 sources 的 Provider 继续指向声明输出。target 根、main/client/data/test 和无该分隔符的合成名路径保持原状。不移动/删除旧输出，不新增公共命名、迁移或全局路径框架。

原分组依据是路径可用性可独立接受/失败/恢复：模型失败不要求重做已完成的完整 common 同步、runtime 准备或嵌套 AW/sources 路径修复。同步/反编译回归只证明同一个路径终态，不夹带模型变更。具体 item/predicate/证据与写 ownership 仅由 Waves 的 R4P 定义。它在 G2 后不等待模型或 G3；与生产源码/共享 fixture/runner 写串行，基于最新稳定字节，只有共享只读的消费方研究没有写冲突。

R4P 已满足下列验收安排（具体实物/运行见 [R4P 报告](../../../build/reports/mixin-r4p/REPORT.md)）：保留原 Windows 红例 syncFullCommonFallbacksAndRuntimePrepareBeforeModel，使用新编译 metadata、私有新 Home、无预置 P/N/清理结果及旧 workspace，真实执行全部 common sync 和 runtime 准备。该测试实际只跑 clocheIdeSync 且禁 sources 下载，没有请求模型；R4P 另已实际打开 client:test AW 并请求 sources，核对 client:data 及嵌套 AW/sources 输出唯一/互不重叠、named/unnamed 与普通/合成名不变、原生身份/producer、target 关闭/runtime 隔离和正常复跑。观察的是 Provider 声明输出及真实文件，不能只看目录字符串或任务成功。必要来源/新 metadata 条件沿 Spec；已按实际影响补足 R1/R2/R3R 的定向回归。

主要兼容风险是非 Windows 嵌套角色原有冒号目录的硬编码消费者；Provider 消费者应随声明输出自然跟进，但须用上述实跑核对，不能推断全平台/最低版本已通过。既有输出保留，不引入迁移清理；若出现需新公开协议、迁移或独立产品终态的问题，报告具体 gap，不扩此恢复。

## 同请求 Tooling 模型的已执行恢复策略

原编制已全文读取的[生命周期诊断](../../../build/tmp/r4-lifecycle-fresh-20261023/DIAGNOSIS.md)支持方法可执行；正式完成以 [R4M 报告](../../../build/reports/mixin-r4m/REPORT.md)为入口。R4M 已在私有正式 R4Tooling runner 用一个 `connection.model(Observation.class).forTasks(":clocheIdeSync").withArguments(...).get()` 替换 BuildLauncher.run 后另一次 ModelBuilder.get。真实 ObservationBuilder.buildAll 在该请求内先对所有项目调用 DownloadPolicy、SourceSetArtifactBuildRequest，再调用 SourceSetModel、DependencyModel，完整 getter/index 及严格 C/view/SourceSet 解析同在这次生产之后。普通 dependsOn 不调度独立 model 请求；同一连接/文件已存在不等于下一 build 的 mapped Provider 已完成。

这是对原确认 sync/模型交付的调用方法恢复，不改 producer/Transform/通知合同。原 selectedSources.elements.map、builtBy、ABSOLUTE、removalsFile 的 Task Provider 和实际 clean view 保留；无证据要求新门面、IDEA 专用适配或模型 scope 覆写。复用已成 sync/runtime/sources 生产接线，不重做已完成源码。正式 runner、observer、测试日志/故障断言和比较器已一起对齐同请求，保留以下要求：旧 prepared.txt/prepare.log 的两阶段成功标记不能再作为真实顺序证据，改观察本次请求任务图、生产/读取日志和实际新返回模型。故障后旧模型文件不能当新结果，失败和恢复输出分别保留在私有目录，不覆盖原 R4 证据。

已采用标准 scope 追踪支持 PRD reuse / Spec 行为 reuse / ADR skip；诊断中尚要求另决 scope 的旧解释已被该后续结论消解。不同物理文件的 clean PROVIDED + raw RUNTIME 可表达原 COMPILE；按真实身份、文件和用途核对，完整模型中的 raw RUNTIME 不能当作 raw 进入 compile。完整 getter 原始图/展开差异仍保存，不要求节点计数、数组次序或每个 getScope 字段全等。比较器应解释每个实际差异，保留 build/project/configuration/variant/classifier、项目/SourceSet 关系和必要闭包、确切 clean 编译文件及 runtime 原链断言；未能解释的新增节点、丢失关系、raw 编译用途或 scope 扩大仍失败，不预筛节点或将实际 compile 改 raw 来通过。

安装消费层若经 enrichment 把同 ID 两文件合为 multi-library，可能合 scope 为 COMPILE；完整链及最终导入未跑，只保留具体条件风险，不称已发生退化或必须适配。最终 IDE 导入/模块根/符号仍取消且未验证，不增替代 IDE 门槛，也不删实质隔离合同。

R4M 只交付正式模型恢复一个终态：以相同输入的无 state 原基线/off/on、新 Home/无 P/N/clean/旧 workspace 真实运行上述单请求；保留完整模型与独立 index/miss、严格 C/view/SourceSet 和 sync 实物，打开确切 S/P Strip 输出，并覆盖 sources、完整 common/六组回退、关闭/runtime 与真实故障短路/撤故障恢复。探索仅实跑 on（12 clean S 引用、4 clean game），不能替代正式 runner 的证据；R4M 已新跑 baseline/off/on、sources/full-common、参与 target 关闭及真实 namespace 故障/恢复，七份最终模型 909 项、0 失败。

Windows 的完整 common/sources 会实际经过 client:test/client:data，故此模型终态依赖 R4P 的 GP；不是因为编号或共享写而等待。R4P 不依赖模型，仍可独立完成/失败/恢复。runner/observer/fixture/比较器是该模型终态的必要验收支撑，不另交付一个工具产品；共享写基于最新稳定字节串行。R4M 不完成原 R4，不验收全部 V1–V7。最终 R5 已独立消费 G3 及各出口适用证据，补足跨入口首次生产、失败/缓存/失效和合同覆盖，生产代码与 authority 未改。唯一 item/Wave/Result 连接见 Waves。

## 具体组织设计

下述名称和调用保留已确认范围的内部设计及约束；设计文本本身不证明实现符合，当前完成判断来自 Context 所列各出口证据与 R5 最终验收，普通命名沿实际实现。保留 compilation 为实际所有者，不创建公开 DTO、统一 compile-view 门面或逐 artifact detached graph。

```kotlin
// 在 TargetCompilation 内保留原 Configuration，类型仍是原生 Configuration Provider。
val compileConfiguration = project.configurations.named(sourceSet.compileClasspathConfigurationName)
// AW/runtime 出口保持 finalMinecraftFile；编译主游戏单独持有。
val compileMinecraftFile: Provider<RegularFile> = registerMixinPreapplication(
    compilation = this,
    compileConfiguration = compileConfiguration,
    targetFile = /* Fabric 按有效 side 返回完整原始 AW T；Forge 返回 joined AW */,
    side = /* 按 target.hasSeparateClient 与所属 compilation，非按 common 属性 */,
)
```

`registerMixinPreapplication` 是一次内部注册：拥有该 operation 的 S、Mixin task、P/N、选择器、独立游戏 Strip 以及必要消费者调度接点；返回最终开关选出的单一主游戏 Provider。关闭选择原 AW，附加游戏仍按原规则处理；开启 Fabric CLIENT 只追加 cleanTarget，不再追加 common AW。需要返回可变数量的旧附加游戏时，在现有 info/compilation 的 extraClasspath Provider 处按同一开关选择，不能把它们混入返回的主游戏或另造视图对象。`FinalJarTasks` 的 AW 与 Decompile task 可保留为既有任务持有者；libraryArtifact 不再被编译/运行共同当成唯一语义出口。

任务调度接点以同一注册作用域持有的 `TaskProvider<Mixin>` 实现（内部函数或 compilation 私有 hook 均可），给真正需要补边的任务调用 `dependsOn(enabled.map { if (it) listOf(operationTask) else emptyList() })`。有可靠有效游戏生产边者可直接使用该边。这个 hook 不包装 C，不承诺改变其 buildDependencies，不可挂到 operation 自身或 raw 上游。operation task 可按 Spec 的 preapply/feature/compilation 命名通过 Gradle 原 Task API 配置 mapping/context，不新增 DSL。

### 原 C 与清理链

1. 在 ProjectPluginApplicator 的现有 JAR artifactTypes 默认属性上仅增加私有 `Attribute<String>` 的 raw；classes/resources 和全类型默认不动。operation key 带 project/target/compilation/有效 side 身份，P/N、输出及专用游戏 type 互不重叠。
2. C 保留原属性和 LibraryElements 请求，只在其首次使用前的公开 `withDependencies` hook 中读取最终开关，启用才注册始终 present 的 operation attributeProvider；关闭根本不安装此 state，不能先装 absent Provider，也不能请求 raw。注册时不读取 files，不在 doLast 改 classpath。DSL 后配置必须早于首次依赖发现/解析。
3. rawExternalFiles 来自原 C 的 nonproject artifact view，显式 raw，保留正常 remap/include 最终状态。显式 mod、ordinary 角色 view 也来自同一 C，复制其有效属性后只覆盖 raw，并按既有角色 component 集合筛选；不重选 variant。S 的惰性分类沿 Codev RemapAction 现有优先级：ordinary 排除优先，显式 mod 纳入，其余用既有 `isMod` 发现传递 mod。不是按组件名/文件名匹配；角色只是分类来源，最终 S 是 raw C 中的确切完整文件。该检测只用于既有 mod 分类，不扫描 Mixin 配置或 nested sources，也不另建 JAR/ZIP 支持名单。
4. S 保留 `elements.map` 和 `builtBy(rawExternalFiles, explicitModsRaw, ordinaryRaw)`；producer 和选择器共享同一个集合。完整位置和内容/成员进入选择器 `@InputFiles`、`@InputArtifact` 的 ABSOLUTE 指纹；不得 CacheableTransform。
5. 正常 remap 注册仍按 namespace 条件跳过；新选择器与游戏 Strip 独立注册，空 namespace 不跳过。DSL 完成后读取 C 的正常 remap/include 最终状态作为 selector `from` 条件，另加 state=raw；`to` 只改 operation state，不复制整份 C、不强加 artifactType。这样 selector 看到正常处理后的确切 raw S，而不是先旁路后 remap 或重复 include。
6. 选择器只按完整输入位置是否在跟踪 S 内分支：S 委托具体 Codev StripMixins 实例并转交 input Provider/参数；其他归档 `outputs.file(input)` 原样返回。不得复制 Strip 算法或绕过其输出重叠、暂存/原子写入及失败清理。自身无 state 的项目目录走原生选择，缺失 NO-SOURCE 目录也不登记为 Transform 输出或创建空目录。必要 project JAR 仍保留原文件/producer。
7. 同一 Mixin 输出 P/N；P 用精确文件依赖请求独立 operation 游戏 type，经原 Strip，removalsFile 同样绑定该 task 的 appliedMixins。该游戏请求不复制 compile remap/include 属性。清理恰为 S+P，通知为空仍消费真实清理结果。

### 所有 raw 回读与解析 classpath

在 CompilationInternal 保留 getNonProjectArtifacts 的原消费者语义（继承 C 的有效 state），另以显式 raw helper/参数服务上游。不能对所有 nonproject helper 一刀切。

必须逐调用改到 raw 的位置：TargetCompilation 的 getUnmappedClasspath/filteredClasspathFiles（继续保留 REMAPPED=false）；主 AW 的 getRelevantSyncArtifacts 调用和 Fabric 辅助 common/client AW；S 与 explicitModsRaw/ordinaryRaw/externalFiles/复制 C 属性的 roleFiles；Mixin 解析 compile/runtime views；发布 RemapJar 的解析 compile view。关闭也不能把 raw 施到含项目的原 C/runtime 或消费者 view。

Mixin 解析 classpath 只取上游外部 compile/runtime raw、所需游戏库和既有工具辅助依赖；compile 变体优先，runtime 只作解析辅助不覆盖 compile 输入，不增加消费 scope。禁止 SourceSet.compileClasspath/runtimeClasspath、本项目输出、stub、另一 operation 结果，以及 T 已覆盖的旧 common/client 游戏副本。Codev 查找是 classpath→mixinFiles→inputFile，旧副本会遮蔽完整 T。原 Configuration/Transform nodes 自己维持身份、variant/classifier 和 producer；不按两个 Set 顺序拼配。

### Fabric 与环境所有权

FabricTargetImpl/FabricCompilationInfo 提供当前 compilation 的 named common/client 原始输入和原 AW provider。辅助 AW 从该 compilation 的 raw compile 依赖及有效显式/继承 AW 构造；独立 client 的 common 不复用 main SERVER Mixin，client data/test 也不从父级拿错分片或 AW。相同原始 AW 输入可确实复用，Mixin/通知不可跨 operation 复用。

完整 T 的内部拼接 task 跟踪两份 AW 文件及顺序，用可重复 ZIP entry 顺序/时间戳写独立 JAR，先 client 后 common，以路径 first-wins；类、资源和 Manifest 同样处理，不合并类成员、不用默认 Manifest 覆盖输入。两个独有集合和重复条目可直接断言。

分割 main SERVER 用本 compilation common AW；独立 client CLIENT 用完整 T；includedClient 和未分割 Fabric 均 CLIENT，即使旧 main 只编译 common 也要取得 client AW。Forge/NeoForge joined CLIENT 用原 joined AW。data/test 从所属源集继承 side，但用自身 S/AW。不开另侧 operation，不把 ModDistribution.common 当 SERVER。

编译、decompile、stub 的启用 CLIENT 集合只有 cleanTarget；项目源码/输出关系继续由 Linkage/TargetHandler 原机制提供，不通过删除重建 ProjectDependency 消除旧游戏。runtime 仍按 HEAD 的主 AW/附加 common 分片选择和顺序，非分割也不被新 CLIENT 编译目标扩成新运行形态。

### mapping、失败与隔离

modRemapNamespace 非空时使用 loadMappingsTask.output、该 mod namespace→NAMED 的 tuple；默认 context unset。空 namespace 三项全不设，仍注册/消费清理。自定义沿原 Mixin Task API；清空 tuple 必须同时清空三项。沿 Spec 已有命名限制，不推断 Loader context、不重写 refmap、不支持不相容的混合 context。真实 selector 见证用注入接口/适当非 synthetic API，不能仅凭 javap 出现 bridge 就宣称 javac 可直接调用。

producer、拼接、必要 source/target Strip、解析/IO 实际异常由原 task/Transform 传播并保留 operation/输入原因；不读日志升级正常可选失败，不自动改开关或回退旧 AW/裸 P/空集合。撤故障重跑必须恢复。关闭是用户显式的旧视图恢复，不能作为隐藏失败处理。

原 remapJar.classpath 必须从 SourceSet.compileClasspath 改为原 compile 变体的上游 nonproject raw 解析视图，加原项目/源集输出角色及旧 AW/原附加库；保留项目依赖 producer，不能用 nonproject raw 抹掉项目或换 runtime 变体。项目部分可沿原 C 建仅筛 ProjectComponentIdentifier 的继承状态 view，保留原生选定的目录/JAR 和 producer，不对项目显式请求 raw，也不假装可以从继承 C 的 view 移除 state；必要 project JAR 即使进入选择器也必须按非 S 返回原 InputArtifact。它和 nonproject raw 分工提供原编译文件，不重建依赖图、不把 cleanSources 引回发布。include/outgoing/JAR 输入仍属原链，Forge writeLegacyClasspath 继续 finalMinecraftFile=原 AW。由行为断言证明发布解析未使用 cleanTarget/cleanSources；发布命令本来需要的编译依赖仍按 Gradle 真实依赖运行。

## 四消费者的全部入口与生产安排

表中“依赖 view”均保留 C，sync/stub 的 nonproject 限制继续存在；游戏用 Provider 追加，不把依赖图投影为全文件集合。

| 类别与真实注册处 | 有效游戏/依赖 view | 物化前生产保证 | 失败及关闭恢复 |
| --- | --- | --- | --- |
| Java：TargetHandler.handleTarget/addCompilation 的每个 sourceSet.compileJavaTaskName；Kotlin：既有 withId(KOTLIN_JVM_PLUGIN_ID) 内 getCompileTaskName("kotlin") | 原 SourceSet 编译集合仍含 C；有效主游戏 compileMinecraftFile + 按开关选出的必要旧附加库。覆盖 main/client/data/test/client data/test。 | 原有效游戏文件生产边能到达 operation 的路径保留；在只消费 C/view/SourceSet、或依赖发现不足的受支持任务注册处加条件 operation 边。入口清单及实际图须逐类核对，不能仅验证 compileJava。 | 引擎/清理失败阻断真实编译；关闭无 state、选择旧 AW/附加库，条件边为空，不调度本次操作。 |
| sources/反编译：registerCompilationTransformations 的 Decompile task，公开 compilation.sources Provider；sync 请求 sources 也是此任务 | inputFile 设有效主游戏；解析 classpath=对应 clean C + 原必要库，启用 CLIENT 不额外挂旧 common。 | inputFile Provider 携带同 operation 的 P/游戏 Strip 生产边；核对 Decompile.classpath 隔离前 N 已存在，不足则在 Decompile 注册处条件补边。每种已配置 compilation 均如此。 | 清理/producer 失败不能返回旧 sources；关闭 Decompile 改读原 AW，保留旧 classpath 和来源，图不含本次操作。 |
| common stub：CommonTargetCreator.convertClasspath/intersection 的各 GenerateStubApi；common 源集编译及 sync 经 stub output | 保留参与者和 main/client/data/test 回退；每个实际参与者的有效游戏集合 + 继承有效 state 的 nonproject artifact view，ResolvedArtifact 保留 setComponent。 | 现有 dependsOn(participant finalMinecraftFile/extraClasspath) 改为每个 participant 的有效游戏输入；保留 view 的生产来源，必要时对该 GenerateStubApi 为参与者逐一加条件边。common 自身无 operation。 | 任一必要输入失败阻断 stub；某 target 关闭以旧视图参与既有跨 target 交集，不生成同 target 另一侧交集。 |
| compile sync 任务：CompilationInternal.configureSourceSet / clocheIdeSync，含 common/回退源集、可选 sources | nonproject compile view 继承 C state，明确 inputs.files/执行中解析实际物化 view；有效游戏也实际物化，非只 dependsOn producer。runtime sync 用原 runtime，sources 接上行。 | 有效游戏 Provider 为 target 带入 operation；common 经其 stub 依赖到参与者。如清理参数早于 game 边完成则在此注册处为相应 compilation 补条件边。依赖发现不能读未生产 files。 | 实际失败使 sync 失败；关闭恢复旧非项目 compile 文件/游戏与模型关系，runtime 不受切换。 |
| 直接 Tooling 模型解析：真实 IDEA ExtraModelBuilder 的 SourceSet/Dependency builders；原 TargetsModelBuilder 只做其元数据职责 | 与 sync 相同输入；完整 getter 模型与独立 SourceSet index，不将模型节点当作实际文件消费替代 | 同一个实际产生全部模型的 `connection.model(Observation.class).forTasks(":clocheIdeSync").get()` 内先生产，后所有 builders/严格解析；准备失败阻断本次新模型。sources 沿 Decompile 原出口，完整 common/sources 的 Windows 运行先满足 GP。不得拆成独立 build 后假定前次完成仍有效。 | 从缺 P/N 的新 Home 验证准备→解析全过程，禁止手置通知或模型改读 raw；关闭准备不安排本次 operation/Strip/拼接。无需任意第三方 model 调度框架。 |

只消费 C、继承 view、SourceSet 的独立测试任务若用于验收受支持入口，须调用同一 operation 所有者接点，并各自单独运行，不同场消费 game。此举证明已接线入口，不扩大成任意第三方任务自动支持。直接 Tooling 准备使用既有 clocheIdeSync 正式任务产物，不等同任务验收前手工预跑 operation；后者禁止。

## 已执行的验证安排与停止条件

下列保留已确认并已执行的验证方法和充分性标准，供证据适用性维护；完成记录在本节后，不是新运行安排。从 HEAD 的 TestKit 方式建立新的小型真实 Cloche fixture/测试类；共享输入生成和观察 helper 只在用到时增加，既有 IncludeTargetIdentityTests 与 include-identity fixture 是 HEAD 兼容基线，不搬用旧 28 项或旧 mixin fixture。每个消费者 item 同时留下自身最小回归，不将全部证明推到最终验收。最终验收才补齐跨消费者交叉风险，无需重做 Codev 全引擎矩阵。

| 条件 | 新代码最小有用观察与证据 |
| --- | --- |
| V1 / V3 | 小游戏类先经真实 AW，再真实 Mixin 注入接口/成员，有源 Java/Kotlin 真实编译；CLIENT Mixin 同时修改 common 与 client 类，反编译实际看到；拼接保留双方唯一资源/类、重复路径 client 优先且 Manifest 原样。环境分类参数化覆盖分割/独立/included/未分割/joined/data/test。打开确切 S/P Strip 输出，已通知条目移除、未通知/类/metadata/refmap/唯一资源保留。 |
| V2 | compileOnly、传递 mod、runtimeOnly、ordinary、项目及本项目 Mixins/nested 输入同场，核对 S 精确成员；同组件其他 classifier/variant、同名不同位置含相同 config 条目的非 S 保留原文件。实际 C 与 SourceSet 保存全部 artifact/component/variant/classifier、边、文件及各自 producer；原生 project classes/resources 包含 NO-SOURCE 缺失目录、不变成 archive。运行时不见 compileOnly，编译不见 runtimeOnly；分别观察 Maven 和 file 路线。 |
| V4 | 两个既有 target 的真实交集 stub，只有共有注入 API 保留；common.client 及 data/test 回退正确，单 target 关闭影响交集，ResolvedArtifact component 与生产链保留。 |
| V5 | 无新 state 请求的同输入原基线 vs off 全量比较 C/SourceSet 成员、variant、project/source/output 与 producer；不能把显式 raw 当 baseline。默认 on、解析前 true→false / false→true、target 隔离，以及所有四消费者关闭恢复。runtime/Forge legacy、项目 JAR/remap/include/outgoing 输入沿 HEAD 原链，输入字节不变；无 Mixin/选择器/游戏 Strip/拼接被关闭消费调度。 |
| V6 | 新 Cloche 实际 RemapTask/RemapAction 后，root refmap 原空间 member 和已 remap 无 refmap selector 共用默认 tuple，真实调用区分 member；另有 direct 无 tuple 仍 Strip。通过原 Task API 配置可用 context 与真实不存在 context/namespace；普通 daemon 返回真实因果/配置定位，撤故障恢复，不能把 daemon 通信失败算成功负例。 |
| V7 首次生产 | 编译、反编译、stub、sync 任务与直接模型路线各自从私有新 Home、无 P/N/清理输出/可复用 workspace 运行。fixture 原始游戏/本地 Maven 输入可生成，但 N/P 不预置。记录参数 Provider producer、C/继承 view/SourceSet 各自 buildDependencies、实际任务图和生产→参数读取/清理顺序。只消费 C/view/SourceSet 的入口分别单独跑，无同场 game 帮调度；真实 Cloche compile 有源码并消费注入 API。检查真实 remap/include 最终输入后 selector 命中确切 S，P 独立 Strip，无重复 remap/include、无 raw 上游环；include 路线必须实际覆盖才可完成，不能以原型对照代替。 |
| V7 失败与恢复 | 分别让 Mixin producer、确切 source Strip、target Strip 实际失败（例如隔离 fixture 的对应无效输入/输出冲突）；观察受影响消费者失败、无旧文件回退，撤故障恢复；不需穷举每类异常×所有消费者，但四出口都要有必要失败阻断证据。空 S/空通知和正常可选失败不被日志升级为失败。 |
| V7 缓存 | 复跑及新进程验证 P/N、原 remap/include、独立游戏 Strip 的适用 UP-TO-DATE/FROM-CACHE 或正常重建；选择器无本地/远程 build cache，只可重做或满足同绝对位置/输入/实现/workspace 条件的本地复用。分离新 Home 首次生产与后续 workspace 实验。变更 S 内容/配置/成员/位置、N、side/mapping/AW 对照实际失效及 operation 隔离；共参使非 S 重进 action 可接受，但仍不得 Strip 其文件。 |
| V2/V5/V7 Tooling | 同输入无 state 原基线/off/on，在同一个实际产生全部模型的 ModelBuilder.forTasks 请求内先生产再解析。全量 getter/展开差异、身份/闭包/项目-源集关系与独立 index（含 miss）保留；按文件用途与原生 scope 语义判定，不预筛额外节点或要求字面全等，不以节点计数/文件集相等代替关联。分别核对 C/view/SourceSet 生产依赖、sync 实物、模型所指确切 clean S/唯一游戏并打开文件。覆盖 sources/full-common、继承、多产物/多变体、main/data/test/client 角色及真实故障/恢复。data/test 原生 archive/index miss 与新增退化分报，不修 outgoing 为命中。 |

真实 Tooling 方法沿 [已读原型](../../../build/tmp/tooling-prototype-1793653484927/NOTES.md)：安装内 Tooling API/useInstallation，全项目先 DownloadPolicy、SourceSetArtifactBuildRequest，再 SourceSetModel、DependencyModel；daemon 参数 `-Didea.resolveSourceSetDependencies=true`；独立 index 原生查找及 output→archive 二次查找；完整 getter dump 保留 `$id/$ref`。保留 builders/index 方法，正式 runner 以[同请求实证](../../../build/tmp/r4-lifecycle-fresh-20261023/DIAGNOSIS.md)恢复，禁止沿用已否定的两次独立 build 配方；不能直接解析并假定 P/N 已存在。classpath/反射字段与 IDEA 版本绑定，实施前重核现有安装，普通配方修正限私有 runner，不修改插件/安装。

本地二进制来源严格执行 Spec：从新 plugin-under-test metadata 的 implementation-classpath 生成私有 Properties，仅将恰好一条旧 Mixins JAR 替换为指定本地生产输出；其余顺序不动，规范 metadata 不改，嵌套 runner 显式收到 clocheClasspathMetadata。不使用 Maven Local或旧 JAR fallback。实际 Mixin/StripMixins/ProduceMixinsKt/异常类加载来源及可能的私有缓存副本与本地 JAR 关系、锁定 Sponge 和任务 implementationClasspath 连同真实消费记录；只做当次来源核对，不持久建立 hash ledger。正式发布来源未接入仍是独立限制。

已采用本地验收使用 Windows、PATH 全局 Gradle 9.5.1、Microsoft JDK 17.0.19 及各次私有 `-g` Home；根编译/测试和嵌套 GradleRunner 都固定全局安装，不 wrapper 下载、不修 junction/共享配置。按新增测试类/场景做聚焦执行，改到 remap/include/publication 交界时运行已有 includeIdentityTest 及相关 Cloche 回归；检查通过后不反复扩大无关矩阵。根命令应先编译新插件并生成新 metadata，不能消费旧 build 输出冒充新 HEAD 实现。

## 已采用最终验收与限制

[最终 R5 报告](../../../build/reports/mixin-r5/REPORT.md)是 V1–V7 可移植仓内入口。Router 已完整读取并可靠关联 R4P、R4M、R5 的原始 Result，均为 completed，并分别按既有 predicate 和 gate 的必要实际证据判断 GP、G3、G4 满足；结合此前 G0/G1/G2，选定 Plan 的限定本地开发交付完成。本次只记录该判断，不首次采用 Result、重新验收或放行 gate。

R5 复核 R1/R2/R3R/R4P/R4M、核对 16 个复用入口的实际来源字节，复比较七份最终模型 909 项、0 失败。新增独立 file C/view/SourceSet 首产、真实非空 include/StripIncludes、精确非 S/classifier、有效 context dispatch/缺失 context 失败恢复、空 S/N/可选失败、source/target Strip 真实编译失败恢复、stub producer 失败恢复、S 内容/配置/成员/位置与 side/mapping/AW/N 失效、target 隔离、workspace/新进程逐文件一致性及 Forge main/data/test 真实 legacy writer 两态隔离。32 个选定成功运行和 6 个预期失败有日志/行为证据；这是证据复用与多次定向运行，不是一遍完整套件。

结论限 Windows、PATH Gradle 9.5.1、Microsoft JDK 17.0.19 和 Spec 唯一指定未发布本地 Mixins。正式发布来源未接入、最低 Gradle 9.4 未动态实跑、最终 IDE 导入/模块根/PSI/符号仍取消且未验证；IDEA 下游同 ID 合库扩大 scope 的条件风险未消除。Forge 验证限报告 tiny-input/真实 writer 接线与隔离，不证明完整下载、补丁、启动或运行游戏。非 Windows 嵌套角色硬编码冒号路径风险保留，不外推全平台/版本通过。

原 R0/R3/R4 永久 partial；11 tracked 恢复、129 文件删除不可重放。未知 2 文件/9057 字节、Codev 后比较缺口、原派生比较误覆盖后重生无前快照及 wrapper --version 非兼容证据继续保留，具体已成/可能效果见 Context。R5 早期部分失效快照被后续比较替换，S 内容/配置结论限保留运行日志；后来的两次 workspace 专跑是新证据，不补造旧历史保证。

## 本次记录维护的作者自检

本次只完成四份派生文档的同范围收尾；原确认范围、设计、全部 item predicate、必要证据、依赖/冲突及 gate assurance 保留，唯一执行图仍为 Waves。恢复安排及 R5 连接已获独立无阻断评审，持续 C5 授权有效；本次状态更新和非实质修剪不新增确认或自动再评审，不把记录写入作为实施或独立验收。

检查四文档的互链、Plan/Waves 匹配、仓内相对证据入口可读性、当前完成状态及限制一致性，并对比原图的 predicate/必要证据、item 前置、Wave membership/前置 gate/release predicate 和真实写冲突未变，合并等待仍无环。Spec 三条当前导航修复已完成，不再列为待办，历史原件不由新文档冒充。未重新全文核验所有历史 authority/原始日志，不建立项目外 Result 依赖或永久身份/hash 账本。

本次未改源码、fixture、测试、依赖、Spec/PRD/Codev 或安装，未运行新构建、Gradle、测试、实验、IDE 或 Git 写。上述本地限制、原 partial 和未知均未改写为通过。
