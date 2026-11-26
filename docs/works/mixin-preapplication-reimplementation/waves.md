# Mixin 重实现 Waves

匹配 [Work](work.md)、唯一 [Plan](plan.md) 与 [Context](context.md)。这是本实施范围的唯一执行图；[弃置历史输入](discard-inputs.md) 只识别 R0 已执行路径。用户已确认实施及同范围维护，持续 C5 授权未撤回。Router 已完整读取并可靠关联采用 R0V/R1/R2/R3R/R4P/R4M/R5 completed，判定 G0/G1/G2、GP/G3/G4 满足；选定 Plan 的限定本地开发交付完成，原 R0/R3/R4 永久保持 partial。本次只维护四文档的完成记录和作者自检，不实施、不重新独立验收或判新 gate。

## 共同执行条件与结果纪律

未来每次调用须明确选定同目录 Work/Plan/Waves tuple、各成员恰一次正式 ref，检查当前 authority/确认范围、item 前置结果及所属 Wave 前置 gates、当前字节/身份/JIT、冲突与工具权限。旧 Work 的结果、原型或本文状态不代替这些证据。一个 item 只交付一个独立终态，由一个 Handler 返回一个完整 Result。

只有 adoptable completed Result 加足够证据才满足 item；Router 读取/关联完整 Result 后另行判断 gate。partial/failed/blocked、缺失结果或证据不足都不释放其依赖。既有终态 Result 不能 resume；修复后的恢复身份与 graph 维护由 Router 在授权内处理，保留旧已知/可能效果，不重放已完成独立终态。

## 历史终态：R0 — partial，不可重启

原 predicate 为“封闭名单内旧源码/测试改动从工作树和索引撤去、旧四份派生 Work 文档逐文件删除，固定 HEAD 基线恢复，保护集合及忽略证据不受改变”。R0 曾属 W0；其 generation 1 完整 Result 为 partial，保持该终态，不再列入当前 Wave 的可执行 membership，也没有新的 R0 Handler/Result。

已采用的历史证据支持 11 tracked 工作树/索引恢复到固定 HEAD、129 授权文件逐项删除、九个明确保护文件前后字节相同及 Git 可见集合/两类 diff 核对。独立审计所见项目写命令只涉及 140 个授权路径及相应索引；不能推成全树无影响。PermissionError 后前快照丢失；随后根 `.git`/140 写路径外汇总净少 2 文件、9057 字节，不能定位/归因，Codev 后比较未完成。详见 Context 的已采用结论与原始获取限制。不得重放已完成恢复/删除、把新同名实现当旧文件删除、把未知改写为无影响或将新恢复结果转记为 R0 completed。

## 历史终态：R3 — partial，不可重启

原 R3 generation 1 的唯一 predicate 是“common main/client/data/test 既有参与者及回退关系的实际 stub，取各 target 当前有效编译视图交集，并保留 component/生产关系”。[原报告](../../../build/reports/mixin-r3/REPORT.md) 保留 CommonTargetCreator 三处有效 Provider/dependsOn 接线、stub fixture/测试的已完成效果；两个 target 的 main 真实共有/独有 API 使用者、单 target 关闭仍参与交集、component/producer 与实际 clean 文件通过。完整 client/data/test 因本地 feature server 请求不匹配（提供方实际 left-server/common-server）在 graph 解析失败，完整 off 未执行。配置阶段生成过 fixture 源码；未进入 stub/Mixin 不等于零写入。

原 R3 永久 partial，不 resume、不改 completed，不再是当前 Wave membership 或 completed 前置。必要完整 stub、回退、清理/关闭和生产保证转由 R3R 的新 Result 证明；不能用 main 旧通过或诊断 completed 代替。已完成 Provider 接线不重做；main 证据仅在源码/输入/环境/消费者仍适用处复用，共享身份变更后作定向真实复验。新 Result 不自动补写旧 Result 或释放 gate。

## 历史终态：R4 — partial，不可重启

原 R4 generation 1 的 predicate 是“sync（含 sources/common 回退）与显式准备后的真实模型实际消费清理视图，保持原生项目/SourceSet/闭包/scope，关闭恢复基线”。[原 REPORT](../../../build/reports/mixin-r4/REPORT.md) 保留已采用历史：CompilationInternal 的 sync 物化/条件生产边、runtime 准备、sources 请求及 fixture/runner/observer/verifier 已完成；baseline/off 模型通过。on 准备成功但后续独立 model build 缺 12 clean source/4 clean game，并观察 scope 投影差异；完整 common sync 在 client:test AW 声明输出目录失败，未请求模型。sources 实物与准备故障阻断/撤故障恢复有证，恢复模型仍失败。

R4 永久 partial，不 resume、不改 completed，不列当前 membership 或 completed 前置。已成接线、P/N/clean/sources/模型/日志、私有 Home/daemon 与失败部分输出保留，不盲重放或清理。Context 保存误覆盖派生比较文件后重生、无前快照不能证明字节恢复及 wrapper --version 非兼容证据的限制。路径与模型恢复分别接原 R4 的未完成边界，不改写历史或回填 R4。

## 已采用完成记录

以下只记录 Router 已采用的结果与门槛判断；没有新 item、Result、验收或 gate 放行。

| items / gates | 已采用结论与仓内证据入口 |
| --- | --- |
| R0V / G0 | completed / 满足；必要保护与当前输入可读，R0 历史未知及不可重放边界保留。 |
| R1 / G1 | completed / 满足；[编译/清理/隔离报告](../../../build/reports/mixin-r1/REPORT.md)。 |
| R2、R3R / G2 | 均 completed / 满足；[sources](../../../build/reports/mixin-r2/REPORT.md)、[完整六组 stub/身份回归](../../../build/reports/mixin-r3r/REPORT.md)。 |
| R4P / GP | completed / 满足；[路径报告](../../../build/reports/mixin-r4p/REPORT.md)：完整 common/runtime 首产、named/unnamed×on/off 六角色 AW/sources、身份/producer、关闭/runtime、受影响回归及正常复跑充分。 |
| R4M / G3 | completed / 满足；[正式模型报告](../../../build/reports/mixin-r4m/REPORT.md)：同请求先生产、全部 builders 后严格读取；新 baseline/off/on、sources、完整 common 六组/参与 target 关闭、真实 namespace 失败/恢复，七份最终模型 909 项零失败。 |
| R5 / G4 | completed / 满足；[最终 V1–V7 报告](../../../build/reports/mixin-r5/REPORT.md)：同一最终实现/指定本地来源下，复用及新增定向运行的证据充分；32 个选定成功运行、6 个预期失败，909 项模型复比较零失败，并非一遍完整套件。 |

## Items（保留已执行定义与证据义务）

以下保留原确认的 predicate、必要证据、ownership 与依赖，供后续判断证据适用性；其中实施/验收措辞描述已执行安排，不是未来任务或重放许可。实际完成与 gate 状态以上表为准，原 partial 不回填。

### R0V — 必要保护对象与设计输入当前可供后续消费

- **前置 item**：无；**所属 Wave**：W0。输入前提是上述 R0 partial 及独立证据审计的已采用结论/限制，非等待 R0 completed；不是重复执行 R0 或继续其终端会话。
- **唯一 predicate**：在已采用恢复/删除和明确保护历史证据的边界内，九个明确保护对象及四份具体设计报告当前存在可读，足以供后续既定消费者使用；结论准确保留未知差额和原始证据获取限制，不声称历史全树同一。一个 Handler 只读核验这些对象后返回一个完整 Result，不另交付恢复、删除或实现结果。
- **范围/预期效果**：只读 [历史输入](discard-inputs.md) 保留集合内 PRD/Spec、两个 XML、本 Work 五文件，以及 Context 证据表中开关、通知、Tooling、Codev 命名四份报告的精确相对入口。前九项落实明确保留对象当前可读；四份报告分别供 R1/R4 的设计和验收方法使用。只以当前存在可读为本次读取结论，不重新证明已采用的 11/129 操作、九文件历史字节或全 Git 状态，不比较维护后的 Work 与旧字节。
- **证据**：列明这 13 个具体对象的当前存在/可读检查方法及结果，关联其保护用途/实际消费者；区分当前读取、已采用历史结论和无证的历史不变主张。完整 Result 保留 R0 partial、未知 2 文件/9057 字节差额、Codev 后比较未完成及不可重放。任一必要输入缺失/不可读或新发现具体安全 gap，须报告对象、受影响消费者和证据限制，不能宣称 predicate 成立或擅自补造/恢复。
- **写 ownership/冲突**：无项目写权限；不恢复/删除、不运行 Gradle、不做 Git 写，不重扫 Cloche/Codev 全树、全部缓存或停共享进程/改权限，不建立快照账本。与这些对象的并发写互斥，读取最新稳定对象；共享只读不冲突。R0V completed 及 G0 满足已由 Router 采用/判定；所有后续实施保留 tuple/当前字节/身份/JIT 义务；与具体消费者无关的历史全量比较失败不再单独阻断实现。

### R1 — 新 compilation 编译视图按合同实际工作

- **前置 item**：R0V；**所属 Wave**：W1。
- **唯一 predicate**：从 HEAD 构建的新 compilation 预应用接线，使实际有源编译消费正确的有效游戏与精确 clean S，关闭及 runtime/发布保留合同基线。
- **范围/预期效果**：公共 target Property/default；compilation 拥有 C/有效编译游戏，一次内部注册 sources/Mixin/N/两路 Strip/必要任务接点；Fabric 的本 compilation AW 和完整 CLIENT 单目标、环境继承；所有 producer/remap/AW/角色/发布 raw 回读；Java/Kotlin 入口生产时序；原 runtime/Forge legacy、发布解析/JAR/outgoing 隔离。只建立后续出口需要的有效 Provider/内部接点，反编译、stub、sync 的实际出口迁移分别属于其 item。
- **证据**：按 Plan V1/V2/V3/V5/V6 有源 Java/Kotlin 最小真实引擎 fixture，确切 S+P 清理、原始输入/非 S/项目目录/producer、环境与 client 优先、正常 remap/include 最终输入及无 remap、双向后配置/target 隔离、runtime/发布输入。新源码必须编译；每条新增非平凡逻辑用最小断言，不搬旧 28 项测试。记录实际本地 JAR/锁定引擎/metadata 嵌套传递来源。第一次运行用新插件 metadata，不复用旧生产输出。首次生产应有核心编译正向证据；最终跨入口/失败缓存矩阵由 R5 汇总验收。
- **写 ownership/冲突**：MinecraftTarget API/Internal、ProjectPluginApplicator/内部属性、TargetCompilation/CompilationInternal 的 producer/raw helper、Fabric compilation/info/target/secondary 和拼接 task、TargetHandler 的编译入口及必要发布解析连接；新 fixture/core 测试及最小 build 测试接入。保持 Linkage/CompilationVariantCreator 的 HEAD 协议，只有实证阻挡需求才允许最小修正并回归。与 R2/R3R/R4P/R4M 的生产/fixture 写有冲突，必须基于 R1 最终字节串接；不改 PRD/Spec/ADR/旧 Work/Codev/正式依赖。

### R2 — sources/反编译实际呈现有效编译视图

- **前置 item**：R1；**所属 Wave**：W2。
- **唯一 predicate**：所有既有已配置 compilation 的 sources Provider/Decompile 出口实际消费有效 cleanTarget 和 clean compile 解析依赖，关闭恢复旧 sources 行为。
- **范围/效果**：按 Plan 消费表配置 registerCompilationTransformations/Decompile.inputFile/classpath 与必要条件生产边；启用 CLIENT 不追加旧 common 游戏；保留 sources 输出 Provider/路径生产关系。不扩到最终 IDE 或新 sources 发布格式。
- **证据**：真实注入 API 在输出 sources 可见，客户端 common/client 修改都可观察；打开实际依赖清理文件证明解析使用清理 view。独立请求反编译成功，关闭/后配置图无本次 operation/Strip/拼接且输出来自旧 AW；失败不消费旧输出。环境/compilation 注册可参数化，但至少一条真实完整 CLIENT 反编译，不能只查 input 字符串。
- **写 ownership/冲突**：TargetCompilation 的 Decompile/FinalJarTasks 相关段及该出口 fixture/测试；与 R3R 共享 fixture/build/test 或项目选择写时互斥，串行应用最新字节；不因此制造 R2→R3R 语义依赖。

### R3R — 完整 common stub 恢复为既有有效视图交集

- **前置 item**：R1；**所属 Wave**：W2。原 R3 partial/报告及已采用诊断是输入，不是等待 R3 completed 或恢复其会话；R2 保持独立结果要求，不为共享文件冲突制造 R2→R3R 语义依赖。本 item 的 completed 已由 Router 可靠采用；以下保留原 predicate/验收范围供证据适用性核对，不重启或重放。
- **唯一 predicate**：在保留原 R3 已完成接线和 main 效果的基础上，common main/client/data/test 及 client.data/client.test 六组合法参与/回退的真实 stub 与使用者均消费各 target 的有效编译视图交集，完整 on/off、component/producer 和原生项目角色成立。必要内部身份纠正及其回归仅用于建立这一终态，不另交付身份产品/API/outgoing 或 authority 结果。
- **范围/效果**：沿原 variant 公式统一内部惰性完整 capability 坐标（project.name + 可选 target suffix + 最终 compilation suffix，保留 group/version）；Linkage 三处本地请求与 CompilationVariantCreator 两个 outgoing 构造分支共享计算，必要 owner 在 CompilationInternal/variant 内部身份处。保留 suffix 原义、outgoing capability 集合、baseAttributes、WITHOUT_DATA、classes/resources/项目 producer、test 原生输出分支；不改 Forge 外部模块 requireFeature。common 无自身 operation，不新增同 target 双侧交集，不重写已完成三处 Provider 接线；仅在真实新失败证明必要时补同一 stub 合同内的最小接点。
- **必需 stub 证据**：保留 commonStubsIntersectEffectiveParticipantsAndFallbacks 原红例；完整 on/off 实跑全部六组，main=left.main/right.main，data=left.data/right.data，test=left.test/right.main，client=left.client/right.main，client.data=left.client/right.data，client.test=left.client.test/right.main。每组核对实际参与者、生成 stub 共有 API 的正向使用者和独有 API 的反向拒绝、真实 clean 文件/未通知项和原始输入不变、ResolvedArtifact component、必要游戏/依赖 producer、关闭者旧视图仍参与交集且无该 target operation/Strip/CLIENT 拼接。不得把 view 改 raw、删 project dependency、跳过红例或用 mock/仅注册替代消费；身份修后只有 graph 成功也不完成 predicate。
- **身份与受影响回归证据**：最小观察区分 left-server/left/right/common-server/common，覆盖三类本地请求、data/test→client 与 client→data/test 两种配置顺序、named/unnamed 及局部 absent；实际核对原 outgoing 集合、compile/runtime 选择、baseAttributes/WITHOUT_DATA、原 classes/resources/producer 和 test 原生 output。main 旧 on/off 受共享身份变更须定向真实复验；R1 编译/runtime、R2 sources 的项目选择按实际 delta 核对其证据适用性，只补受影响回归，不每组重复完整引擎或无变化 R1/R2 矩阵。按 Plan 记录 Gradle/JVM/私有 Home/实际本地 Mixins 来源，最低 Gradle 未实跑仍明确保留。
- **写 ownership/冲突**：Linkage 三个本地请求、CompilationInternal/CompilationVariantCreator 内部身份共享处、必要 stub 接点及现有 stub/身份 fixture/测试最小断言。与 R1 的项目选择/编译 owner、R2 的项目 sources 选择、R4P/R4M 的 compilation/共享 build/fixture/测试写互斥，按最新字节串行；共享只读无冲突。禁止增加 alias/公共协议、改 outgoing 集合/test archive/生命周期、重建项目图、改 authority/正式依赖/Codev 或重放旧弃置。若实际后续问题需要独立终态或扩合同，报告具体 gap/效果，不能隐藏在本 predicate。

### R4P — Windows 嵌套 compilation 的 AW/sources 路径恢复可用

- **前置 item**：R1、R2、R3R；**所属 Wave**：W3P，前置 gate G2。原 R4 partial/报告、已采用路径诊断和当前共享 owner 是输入，不等 R4 completed、R4M或 G3。该独立终态已由 R4P completed 建立，Router 已判 GP 满足；以下原定义保留，不重新实施。
- **唯一 predicate**：既有嵌套 compilation 在 Windows 下的 AW/sources 声明输出合法、唯一且可实际生产消费，完整 common 回退同步与 runtime 准备不再被该目录阻断；普通路径、原生身份/producer、关闭与 runtime 隔离保持。模型关系/标准 scope 验收不是此 predicate，成功不能完成 R4/G3。
- **范围/预期效果**：仅在 TargetCompilation.registerCompilationTransformations 共享 outputDirectory 映射中复用 CompilationInternal.namePath 的分隔规则，将 TARGET_NAME_PATH_SEPARATOR 转为 '/'。client:test→client/test、client:data→client/data；AW 和 Decompile 同获纠正。保留 target 根及 main/client/data/test、无分隔符合成名目录，公开 name/task/SourceSet/capability/operation key/side、声明输出 Provider 和生产关系不变；不移动/删除旧输出，不改 selectedSources/Transform 生命周期、scope、模型适配或 outgoing。
- **必要证据**：用新源码编译生成的新 metadata，按 Spec 只替换唯一指定本地 Mixins JAR，核对真实来源。Windows 原红例 syncFullCommonFallbacksAndRuntimePrepareBeforeModel 必须在私有新 Home、无预置 P/N/清理输出及旧 workspace 下实际跑完全部 common 参与/回退同步与原 runtime 准备，记录任务图及文件物化。此测试实际只执行 clocheIdeSync，不取模型且关闭 sources 下载，不能以其名称/返回成功声称模型或全部 sources 通过。
- **同终态的输出/隔离回归**：另实际请求 client:test sources 并打开 client:test AW；核对额外 client:data 的 AW/sources 实际输出、嵌套与普通角色路径唯一/不重叠，named/unnamed target 和普通/无分隔符合成名位置保持；Provider 消费声明的新输出，无硬编码旧冒号路径。包含 target 关闭场景，核对原生身份/项目选择/producer、runtime 原 AW 与未 Strip 依赖、关闭无本次 operation/Strip/CLIENT 拼接，以及一次正常复跑的真实状态。按共享路径实际影响定向复验 R1/R2/R3R 相关边界，不重复无变化全引擎矩阵，不用单 target 代全 common。
- **写 ownership/冲突**：上述 TargetCompilation 目录 owner 与现有路径/common sync/sources fixture、测试和必要私有验收输出；不重写 R4 已成 sync 接线。与其他生产源码、共享 fixture/runner/build/test 写串行，基于最新稳定字节；R4M 共享这些对象时也串行。只读语义研究不读写本 Work，无冲突。保留 R0/R3/R4 partial、既存输出与未知效果，不清理历史 Home/daemon、不做 Git 写或改安装/Codev/缓存/权限/进程/authority/正式依赖。
- **风险/失败边界**：非 Windows 嵌套角色旧硬编码冒号路径可能受影响，Provider 使用须由实际输出观察证明；不承诺全平台或最低 Gradle 动态通过。不在此加入迁移框架或未知模型修正；如有需要扩合同/独立终态的新失败，保留已成/可能效果并报告具体缺口，不吞入本 predicate。

### R4M — 同请求准备后的真实 Tooling 模型恢复实际清理消费

- **前置 item**：R1、R2、R3R、R4P；**所属 Wave**：W3M，前置 gate GP。R4P 及 GP 是 Windows 完整 common/sources 实跑的实际路径前提；不等待原 R4 completed、不 resume 原会话。该独立终态已由 R4M completed 建立，Router 已判 G3 满足；以下原定义保留，不重新实施。
- **唯一 predicate**：正式私有 runner 在实际产生所有模型的同一次 Tooling 请求内先完成同 operation 的 sync/必要生产，再由真实 builders 解析清理视图；同输入 baseline/off/on、sources 和完整 common/回退的真实模型按文件用途保持原生身份/项目/SourceSet/闭包/scope，关闭及真实故障短路/撤故障恢复成立。runner/observer/比较器与最小测试只服务此终态，不另交付工具产品、路径修复或全产品验收。
- **范围/最小入口**：将 [正式 R4Tooling.java](../../../integration-test/mixin-compilation/R4Tooling.java) 的两次独立请求替换为 `connection.model(Observation.class).forTasks(":clocheIdeSync").withArguments(...).get()`；采用 [实跑 runner](../../../build/tmp/r4-lifecycle-fresh-20261023/R4Tooling.java) 及 [严格 observer](../../../build/tmp/r4-lifecycle-fresh-20261023/observe.init.gradle) 的方法。全部项目 DownloadPolicy/SourceSetArtifactBuildRequest → SourceSetModel/DependencyModel → 严格 C/view/SourceSet/configuration 解析须在同一次 buildAll 中，保留完整 getter `$id/$ref`、全节点/闭包、独立 index 及 output→archive 查找/miss。若后来拆多个 get，每次都须对应生产保证，不能凭同连接/旧文件存在复用已否定的生命周期假设。不改 TargetsModelBuilder 的元数据职责或引入 IDEA 适配。
- **比较与实质隔离证据**：在新插件 metadata、Spec 指定唯一替换的本地 Mixins 来源下，分别从私有新 Home、无 P/N/clean/旧 workspace 的同输入无 state 原基线/off/on 运行正式单请求。分别保存参数 Provider 来源、C/view/SourceSet 的 buildDependencies、实际任务图/生产先于参数读取、sync 实际物化及模型所指文件。按原 compile 输入的 artifact/component/variant/classifier 关系定位确切 S/P，打开实际 clean JAR 核对通知删除及未通知/类/metadata/refmap/资源、非 S 原文件与原始输入不变，游戏唯一；不得仅以字符串出现或数量相等判通过。
- **完整模型核对**：原始完整 getter 与展开差异保留，分别对照全部 build/project/configuration/SourceSet、所选身份/多产物/多变体/继承及必要闭包、原项目 classes/resources/NO-SOURCE 和 producer、独立 index（含 data/test 原生 miss）。按真实文件用途解释 clean PROVIDED + raw RUNTIME 与原 COMPILE；compileOnly 不流入 runtime、runtimeOnly/raw S/旧游戏不流入 compile，runtime 仍读原链。可解释原生 order/grouping/重复展开和 scope 拆分，但未解释的新节点/关系丢失/用途扩大仍失败。修正 [原比较器](../../../integration-test/mixin-compilation/verify-sync-models.py) 的过强 getter/顺序/逐字段全等及旧 prepared.txt/prepare.log 假设，不能预筛节点、删用途断言、按 basename/GAV/Set 顺序配对或把 compile view 改 raw 来过关。
- **受影响真实运行**：新 runner 的 baseline/off/on 都须新跑；[syncToolingSources](../../../src/test/kotlin/earth/terrarium/cloche/MixinCompilationTests.kt) 须请求真实 sources 并打开注入 API，再完成同请求模型观察。完整 common/六组既有参与/回退须在 GP 后以真实 builder 和严格读取验证，含参与 target 关闭，核对各 target 有效游戏/依赖、stub 与 common 源集模型、项目关系/runtime 准备；以现有 stubs fixture 与 common sync 入口添加同请求观察，不把只跑 clocheIdeSync 且 downloadSources=false 的原测试名当模型证据。无需把每个组合变成重复全引擎矩阵，覆盖 sources 和完整 common 的缺口即可。
- **失败与恢复证据**：真实 namespace 故障由正常 daemon 返回具体 producer/operation 原因，单请求失败须阻断本次新模型，不以旧 JSON/日志当返回；保存失败的已成/可能部分输出。撤故障后同 fixture 用该正式请求重新成功并验证 clean 文件/模型，不能仅准备成功。按同请求改变定向复验相关生产关系、关闭/后配置和 R1/R2/R3R 受影响边界。探索只证明 on 方法可行，旧 baseline/off/sources 只可复用未变化内容，不能代替此次新调用的实跑。
- **写 ownership/冲突**：私有 R4Tooling/observer、sync/full-common fixture、verify-sync-models、MixinCompilationTests 及必要测试接入和私有新输出；正式模型消费沿已成 CompilationInternal sync/runtime/sources 接线，不重做该生产效果。与 R4P 的共享 fixture/测试/runner/build 写及其他生产/验收写串行，使用最新稳定字节；只读输入不冲突。不得盲改 selectedSources.elements.map/builtBy/ABSOLUTE、通知 Provider、生产 scope/原 C、项目图/outgoing、安装/Codev/authority/正式依赖；有真实额外生产缺陷时报告受影响 predicate 与效果，不偷偷扩大 owner。
- **限制**：[已读诊断](../../../build/tmp/r4-lifecycle-fresh-20261023/DIAGNOSIS.md) 的 12 clean S 引用/4 clean game、严格读取和 namespace 恢复是方法实证，不是此 item 完成；其旧 scope 不可行解释已由后续可靠采用的原生语义结论消解。诊断没有新增同请求 off/baseline、sources/full-common 通过。最终 IDE 导入取消且未验证，条件性 multi-library 合 scope 风险保留；不据此新增适配或承诺 IDE 通过。R4 原 partial 及现有输出/证据覆盖限制不变。

### R5 — 最终同一实现的 V1–V7 本地开发验收有充分证据

- **前置 item**：R1、R2、R3R、R4P、R4M；**所属 Wave**：W4，前置 gate G3。消费各出口最终实现和适用证据，R4P/GP 经 R4M/G3 合并等待；不等原 R0/R3/R4 completed。R5 的完整 Result 已由 Router 读取、关联并采用 completed，G4 已判满足；一个 Handler/完整 Result 只交付最终验收判断，不重启任何终端 Handler。
- **唯一 predicate**：对同一最终实现、Spec 指定本地二进制与真实输入，Plan V1–V7 的全部必要保证有适用且足够的证据，可限定声明本地修正版下开发功能验收完成；未知、失败、未跑或过期证据不得记为通过。它是跨出口接受证据的终态，不能与新生产功能/恢复合并。
- **范围与复用**：建立 V1–V7→具体消费入口/文件/结果/适用条件的最小覆盖说明，复用 R1/R2/R3R、R4P/R4M 及原 R3/R4 仍适用的有界证据，说明源码/输入/来源/环境/消费者与新 runner 变化的影响，只补缺失/受影响实跑，不重复无变化全引擎矩阵。旧探索、旧 failed/partial、取消 IDE 验证或文件可读均非通过证据；新二进制/fixture 输入不以旧成功替代。
- **必须补足的验证**：四类出口及直接模型各自私有新 Home、缺 P/N/clean/旧 workspace 的首次生产；Maven/file、C-only/view-only/SourceSet-only 无同场 game 代调度和真实有源 Java/Kotlin 注入 API；参数来源、集合 buildDependencies、任务图/物化时序；精确 S+P/非 S/项目 NO-SOURCE、原生身份/闭包/index miss、全部有效环境/CLIENT 优先与 common 交集/回退；默认/双向后配置/target 关闭、runtime/Forge legacy/项目 JAR/remap/include/outgoing 隔离与原始输入不变、真实 mapping/refmap/context 和无 tuple。include 必须有实际覆盖，不能以原型补缺。
- **失败/缓存与失效**：producer、source Strip、target Strip 分别真实失败并阻断受影响消费、无旧文件回退且撤故障恢复；四出口有必要失败阻断证据，不要求异常×消费者笛卡尔积。空 S/空通知和正常可选失败保留。正常复跑/新进程核对适用 P/N、remap/include、游戏 Strip 的 UP-TO-DATE/FROM-CACHE 或正常重建；选择器只可重新执行或同绝对位置/有效输入/实现/workspace 条件复用，不称 FROM-CACHE。变更 S 内容/配置/成员/位置、N、side/mapping/AW 验证失效和 operation 隔离，共参非 S 重进 action 仍不 Strip。新 runner 的 same-build on/off/baseline、sources/full-common、故障/恢复与生产关系受影响证据由 R4M 提供，若随后变化则定向重验。
- **写 ownership/冲突**：生产代码只读；仅验收 fixture/测试/比较器的必要断言和私有新输出可写，不夹带生产改动。与任何影响该实现、共享测试/runner/build/fixture 的写互斥，观察最新稳定状态；若发现需生产纠正或独立恢复的缺陷，保留失败/已成/可能效果，报告具体受影响完成条件、owner、可用证据与安全重放边界，不将其写成通过或盲重放兄弟结果。保留历史证据目录，不覆盖原 R4 派生比较文件。
- **结论边界**：正式发布来源未接入、最低 Gradle 9.4 未动态实跑（最低版本/toolchain 不改）、最终 IDE 导入/模块根/符号未验证，以及 R0 未知差额/Codev 后比较未完成均随结论保留；不新增全平台/版本兼容或发布验收承诺。这些限制不由 Tooling 成功消除，且不恢复已取消 IDE 门槛。独立验收证据完成仍由 Router 判断 G4，不由本 item 自放 gate。

## 当前 Waves 与门槛

| Wave | 增量结果 | items（各恰一次） | 前置 Wave gates | 本 Wave gate 的可判定 release predicate |
| --- | --- | --- | --- | --- |
| W0 | 已执行基线的必要保护/当前输入衔接 | R0V | 无 | G0：R0V completed，13 明确对象当前可读，结合已采用恢复/删除/保护及独立审计，保留未知差额/原始获取限制，无未处理相关具体安全 gap；不要求 R0 completed、不证明全树未变。只衔接 W1。 |
| W1 | 实际 compilation 编译出口 | R1 | G0 | G1：R1 completed，有源真实编译/清理/来源、环境及关闭隔离证据满足 predicate。只衔接 W2。 |
| W2 | sources 与完整 stub 各自有效消费 | R2、R3R | G1 | G2：两项 completed，sources 独立证据和六组 on/off stub 的使用者/清理/component/producer、身份纠正及定向回归分别充分。原 R3 partial 不列等待。衔接 W3P，不代 G3/G4。 |
| W3P | Windows compilation 路径可用性恢复 | R4P | G2 | GP：R4P completed，Windows 全 common/runtime 首次生产、client:test 与 client:data AW/sources 实物和输出唯一性、named/unnamed/普通/合成名、原生身份/producer、关闭/runtime、正常复跑及受影响回归证据足够。只释放 W3M 的路径前提，不完成原 R4/G3/G4。 |
| W3M | 正式单请求模型的真实清理消费恢复 | R4M | GP | G3：R4M completed，当前正式 runner 的同输入 baseline/off/on、sources/full-common、严格 C/view/SourceSet/任务图/sync 实物及模型 clean 文件、完整 getter/原生身份/闭包/scope 用途/index miss、关闭/runtime、真实故障短路/撤故障恢复证据满足该 predicate；沿 GP 取得路径/common/runtime 保证，不以探索或 R4 partial 补齐缺口。只衔接 W4。 |
| W4 | 最终 V1–V7 本地开发验收 | R5 | G3 | G4：R5 completed，最终同一实现及限定本地来源的 V1–V7 全覆盖证据充分，缺口/过期影响已处理、失败与缓存/失效及跨入口实际消费成立，并如实保留本地未发布/最低版本/最终 IDE/历史未知限制。建立本 Plan 的本地交付结论，不表示正式发布或最终 IDE 通过。 |

G0/G1/G2、GP/G3/G4 均已满足，沿用 Router 完整读取并可靠关联各原始 Result 后的既有判断；本次未新增 item 或 gate 判断。Plan 完成要求 G0–G4 及实际路径前提 GP；无空 Wave、虚构模型 Result 或永久 partial 的 completed 等待。gate 仅在 Router 完整读取并可靠关联其所有 item Result、证据充分且 gate 检查通过后判断，文档编制或有利 review 均不释放依赖。

## 分组、连接与记录维护自检

R4P 的独立路径成功不依赖模型成功；R4M 的 runner/严格观察/比较与受影响运行只建立正式模型恢复；R5 判定最终跨出口全部合同的证据充分性。三项各自的 terminal predicate、证据义务及独立完成事实保留，最终验收未夹带生产纠正。历史 R0/R3/R4 仍 partial 且不列 membership；恢复完成不回填或重启旧结果。

membership：W0={R0V}，W1={R1}，W2={R2,R3R}，W3P={R4P}，W3M={R4M}，W4={R5}。每 item 恰一次；item 边 R0V→R1，R1→R2/R3R，R1/R2/R3R→R4P，R1/R2/R3R/R4P→R4M，R1/R2/R3R/R4P/R4M→R5。所属 Wave gate 等本 Wave 全部成员；前置 gate 边 G0→W1、G1→W2、G2→W3P、GP→W3M、G3→W4。合并拓扑 R0V,G0,R1,G1,{R2,R3R},G2,R4P,GP,R4M,G3,R5,G4；ID 可解析、无自等待/回边/循环，无 R4 completed 或 G3→R4P 等待。R4M 等 GP 的理由是 Windows 完整 common/sources 的路径需求，不能把共享写冲突本身当语义前置。

R4P 与 R4M 的共享 fixture/测试/build/runner 写串行；R5 消费最终稳定实现和证据，与影响该对象的写互斥；共享只读无冲突。保留各原 predicate、owner、必要证据及风险。恢复安排及 R5 连接已获独立无阻断评审，C5 持续授权有效；本次只是记录采用结果和非实质修剪，不改变范围、依赖、冲突、门槛或必要 assurance，不新增确认或自动再评审。

本次核对四文档互链、唯一 Plan/Waves 匹配、相对证据入口可读性、状态/限制一致性、原 item/Wave 合并等待与完成要求未变及格式差异；未重验底层模型/日志，未运行新构建、Gradle、测试、实验、IDE 或 Git 写。文档维护不充当独立验收或门槛判断。

## 完成结论的限制

完成仅指 Windows、PATH Gradle 9.5.1、Microsoft JDK 17.0.19 及 Spec 唯一指定未发布本地 Mixins 下的 V1–V7 本地开发交付。正式发布来源未接入、最低 Gradle 9.4 未动态实跑、最终 IDE 导入/模块根/PSI/符号仍取消且未验证；IDEA 下游同 ID 合库扩大 scope 的条件风险未消除。Forge 验证限报告 tiny-input/真实 main/data/test legacy writer 两态接线与隔离，不证明完整 Forge 下载、补丁、启动或运行游戏。

原 R0/R3/R4 永久 partial；11 tracked 恢复与 129 文件删除不可重放，未知 2 文件/9057 字节、Codev 后比较缺口、旧派生比较误覆盖后重生无前快照仍保留。R5 早期部分失效快照被后续比较替换，S 内容/配置结论限保留运行日志；后来的两次 workspace 专跑是新证据，不能补造旧历史保证。具体历史效果与适用限制见 Context 和 R5 报告，不改写为通过。
