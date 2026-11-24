# Mixin 评审修正 Waves

[Work](work.md) · [Context](context.md) · [匹配 Plan](plan.md)

本文件是唯一执行图。当前 A/B/C/V 均 completed，Router 已采用原始 Results 并判定 G-API、G-CALLERS、G-PLATFORM、G-ACCEPT 满足；独立计划复核及用户范围确认已完成。此处仅记录已采用的判断，不是本次文档作者独立验收、执行 admission 或新释放 gate。每 item 仅一个 Handler、一个终态 Result；实现者不能自行选择子集、改图或兼做另一独立结果。

## Items

### A — Codev 可独立使用的多记录 Strip

- **单终态 predicate**：Codev `StripMixins` 能以同一完整安全生命周期处理一个 JAR 的有序多记录集合，已有单记录调用者均可按新接口工作，有适用组件/注册回归证据；该能力不依赖 Cloche 迁移。
- **范围/写所有权**：Codev `minecraft-codev-mixins` 的 `StripMixins.kt`、`StripFixture.kt`、必要 Strip 测试和 `integration-test/mixins/pipeline/callers/{strip,pipeline}` 调用；必要的测试输入只服务此 predicate。保留 ExactMixinStripper 算法、Mixin producer/独立 N 与引擎；不改其他模块或 authority，不发布/改正式版本。
- **实施要点**：将 removalsFile 改为 `@Classpath ConfigurableFileCollection`；有序 Provider `.from`、File 首次去重、零记录失败/空记录复制；先保护原输入及全部记录，再占用输出、逐步暂存、关闭、一次原子发布和 finally 清理。迁移 getter、Fixture 及 integration caller 的全部 `.set`；明确 API/ABI 不兼容，不加旧壳。
- **必要证据**：扩展现有 StripMixinsTests/StripRegistrationTests/StripStreamTests 及既有 pipeline 参数跟踪用法，证明累积清理、重叠/空/缺配置、同 basename 不同路径、File 去重、JSON/资源保留、记录内容和顺序的真实指纹失效/新进程复用；顺序不能只靠最终内容判定。覆盖后份记录与源输入的同址/硬链接/真实父目录/目录包含保护，输出占用赢家不被失败者失效；首/后步实际失败、close/move/暂存清理失败传播、无当前半结果及恢复。保留原 cause/suppressed，若加步骤诊断包装，断言检查原始故障仍在链中。单记录 caller 回归及真实 Gradle 注册通过。
- **前置 items**：无。
- **冲突**：与 B 的新 JAR 使用由 A→B 保证先后；与 C 的本地 Codev JAR/来源检查、根构建若共享输入或输出时串行，不同时改写/读取中间 JAR。
- **完成输出**：可消费的当前源码/新 API 本地 JAR 和作者聚焦测试证据，明确实际环境、getter/descriptor 与限制；不宣称 Cloche 已迁移或全产品验收。

### B — Cloche 精确选择和 Codev 委托的实际消费

- **单终态 predicate**：Cloche source/game 两条注册均通过同一新 Codev API 完成精确来源清理，主编译到下游实际消费来源一致，原 C/项目/非 S 与受影响消费者不退化，有聚焦迁移证据。
- **范围/写所有权**：`MixinPreapplication.kt` 内的私有来源选择器与注册、删除 `StripCompilationMixins.kt`，`MixinCompilationTests.kt` 的必要 caller/prepare/断言及 `integration-test/mixin-compilation` 相关 fixture；单次私有 init、metadata/执行输出只写本地 build 临时区域。正式 `build.gradle.kts` 的依赖/锁/产品配置不改。通用 Info 修正归 C。
- **实施要点**：原 InputArtifact 完整位置判 S 一次，未中返回原文件，命中 anonymous adapter 一次委托完整 StripMixins；源 action 不缓存，input/S 为 ABSOLUTE。游戏直接注册 Codev action，全部 `.removalsFile.from(records)`，删 `recordOrder`。保留 artifact ID/variant/classifier、原 C、raw upstream、producer 和条件边。按 Plan 的私有来源方法从 A 当前本地 JAR 贯通实际 Kotlin/KSP/Java/Test、metadata、prepare JavaCompiler、TestKit 与 Tooling，不只改 fixture 路径。
- **必要证据**：先核对 getter/JVM descriptor、各实际 classpath 唯一新 Mixins 与完整其余依赖、私有 metadata 字节/宿主及消费加载来源；实际主/测试编译及 Gradle 参数隔离。既有 classifier extra 共存的真实 Java/Kotlin 正负消费、source/game 首后步故障及恢复，保留生产先后/记录定位/无当前部分或旧输出冒充成功断言；适配旧 action 名和 `.cloche-strip-` 前缀断言不能删保证。核对 S 一次选择/单次完整委托、非 S 原文件、目录/NO-SOURCE 原生保留及项目 producer。复用现有 sources、common stub、sync/同请求 Tooling、C/view/SourceSet 和 file/Maven 首产方法，覆盖受本迁移影响的 V1/V2/V4/V6/V7 及 V5 出口；实际动态同源失败须修正重验，不能仅打印计划路径。旧单记录缓存结果不能证明新 action 通过。
- **前置 items**：A；同时等待所在 Wave 的 `G-API`。
- **冲突**：与 C 共写 MixinCompilationTests/fixture、共用 Cloche 源码编译、根 build、私有 metadata 和 JAR 消费链，必须串行，从执行时当前状态衔接；与 V 的观测对象保持稳定。
- **完成输出**：迁移实现与同源编译/受影响消费者的作者证据；不承担 C 的数据输入修正，不宣称最终联合验收。

### C — 平台纯数据输入及原行为保持

- **单终态 predicate**：统一 compilation 入口不再 import/强转 Fabric 或用 null 控制登记，平台只供数据，原 AW/primary/sources/runtime 语义和文件/任务出口保持，有平台行为回归证据；可在 B 迁移前独立交付。
- **范围/写所有权**：`TargetCompilation.kt` 的 Info/统一登记，FabricCompilationImpl、FabricTargetImpl、FabricClientSecondarySourceSets 和 ForgeCompilationImpl 的 Info 构造；MixinCompilationTests 及 sides/paths/identities/decompile 等必要 fixture。保留 FinalJarTasks/文件出口、TargetHandler 角色对齐和现有任务归属。私有来源检查使用 Plan 方法，不改正式环境或产品开关。
- **实施要点**：有序 named 原料 map、primary Provider、本地 runtime 原料 map 和有值 extra Provider；基类依原拓扑登记 primary/other AW，另一片用本 compilation AW/raw 依赖，本地 runtime common 用 target AW。独立 client/data/test 仍接 main.finalMinecraftFile；仅原 distribution client 惰性加入本地 runtime extra。side、primary、开关互不替代，保留 Provider 后配置、任务名/primary-other-common 目录/featureName、compile/runtime/发布/sources/stub/sync 出口。不加任务工厂、回填 Property、平台预登记或 DTO。
- **必要证据**：静态检查通用入口与 Info 数据边界；从 sides 的 14 类断言扩充/收敛验证 split/unsplit/included/client:data/test/Forge-NeoForge joined、两片顺序与 side；解析前后 true→false/false→true、target 隔离，未分割非 included main 不新增 runtime common。区分 target AW 与 compilation preview AW 的真实输入/输出和 runtime 来源，client/data/test 仍用 main AW；原 primary/sources 选片及另一 clean library、原 runtime/legacy/remap/include/outgoing 文件、命名目录、必要 producer 与关闭路径均检查。使用执行时当前、来源明确的本地 Codev；若 B 尚未执行可用当前旧接口，不借此声称新 API 验收。与 B 合并后的最终证据归 V。
- **前置 items**：无；B 或 A 不是本行为修正的业务前置。
- **冲突**：与 B 的源码/测试/根构建/metadata 共写必须串行；与 A 共享本地 Codev JAR 构建/快照时也串行。执行期间不得观察被另一 item 改写的输入，不据此新增 A→C 或 B→C 依赖。
- **完成输出**：平台数据修正及作者回归证据；不重做 Codev 多记录 API，不以自身通过关闭 B/V。

### V — 合并后 Cloche 限定本地独立验收

- **单终态 predicate**：在 A/B/C 的实际合并对象、同源本地新 API 和限定环境下，当前 Cloche Spec V1–V7 的必要保证有充分适用的独立验收证据，无未解决必需缺证或阻断缺陷；不另作 Codev 全产品认证或发布验收。
- **范围/所有权**：不同于 A/B/C 作者的独立执行者；只读生产/测试/authority/本 tuple 和当前组件/作者证据，可运行现有方法并在私有 build/fixture 输出观察，不修改生产/测试或图，不把修复混入验收 Result。
- **必要证据**：独立读取实际变更、必要连接和原始证据；核实 A 的多记录组件证据适用性及 B/C 合并后的实际接线，而非只读总结。按 Plan 验证表逐条判定 V1–V7；重跑变化影响或来源/新鲜度不确定的必要路线，其余有证据说明适用才复用。特别核实主编译/KSP/metadata/JavaCompiler/TestKit/Tooling 同源、source/game 首后步安全及恢复、纯数据平台/原 runtime、真实 Java/Kotlin 正负消费、原选片 sources/另一 library、stub、原 C/项目/variant/classifier/完整 scope 和同请求 Tooling。
- **首产与缓存边界**：编译、sources、stub、sync 各自使用缺 P/N/clean 且无旧 workspace 的私有新 Home；file/Maven 及可独立 C/view/SourceSet 无同场游戏代调度，核对 producer 先于参数读取/清理；直接模型入口从同样缺失状态先准备再解析。新进程检查记录内容/顺序和来源/side/AW/mapping/context 的相关失效，区分 UP-TO-DATE、workspace 复用与适用游戏 action 的缓存恢复。不能以模拟通知、预跑 operation、模型标题或旧接口日志填充缺证；include 等必要路径未覆盖即明确保留缺口。
- **前置 items**：B、C（A 经 B 传递）；同时等待 `G-CALLERS`、`G-PLATFORM`。
- **冲突**：观测期间固定 A/B/C 相关源码/JAR/metadata，全部根构建与同 Home 故障/恢复串行；只在当前验收范围写派生执行输出，不覆写旧历史证据。
- **完成输出**：一个独立验收 Result，列实际方法/对象/输入/环境/结果及必要证据和局限。缺证/失败不能报告本 predicate completed；保留实际或不确定效果和不能安全重放边界。最终 IDE/最低 9.4/正式依赖未验证随声明保留。

## Waves 与 gates

Gate 仍由 Router 根据完整、可靠关联的 Result 与实际证据判断；以下保留原条件及显式消费者，不自动调度或发布成功。已采用完成记录如下，详细独立证据见[验收报告](../../../build/tmp/v-accept-20261105/REPORT.md)，各作者证据与限制见 [Context](context.md)。

| Item / Wave | 已采用终态与判断 |
| --- | --- |
| A / W-API | completed；有序集合 API、caller、安全生命周期及真实顺序/内容指纹证据满足 G-API。 |
| B / W-CALLERS | completed；原位置选择后单次委托、游戏直接 action、实际同源编译/消费证据满足 G-CALLERS；13 个不同聚焦方法及八组模型 1067 比较有适用通过证据。 |
| C / W-PLATFORM | completed；纯数据输入及原 AW/primary/sources/runtime 行为证据满足 G-PLATFORM；11 个不同聚焦方法有适用通过证据。 |
| V / W-ACCEPT | completed；合并对象 V1–V7 supported，无阻断或必需缺证，Router 判定 G-ACCEPT 满足。15 个聚焦方法、30 fixture、199 项核对、当前模型 1067 比较通过。 |

V generation 1 因 403 insufficient balance / child_exited_without_result 未产生 Handler Result；连续性核对后 generation 2 是同一 V 的唯一可采用终态。没有重写既有终态或因恢复重复整批测试。追加维护不扩充这些 predicates/gates，原失败、环境迁移 partial、CT2 已知/未知效果及不可重放界限见 Context。

| Wave | 增量结果 / items | 前置 Wave gates | 释放 predicate / 显式消费者 |
| --- | --- | --- | --- |
| W-API | Codev 独立多记录能力；A | 无 | `G-API`：A completed，集合接口/所有已知 caller/安全及指纹必要证据充分，实际本地 JAR 可供 B；只释放 B 的此项前置。 |
| W-CALLERS | Cloche 精确选择/委托及实际同源消费；B | G-API | `G-CALLERS`：B completed，实际编译/参数隔离、来源和受影响消费证据充分；只释放 V 的此项前置。 |
| W-PLATFORM | 统一入口数据化及平台行为保持；C | 无 | `G-PLATFORM`：C completed，Info 边界及原选片/AW/runtime/开关/目录回归证据充分；只释放 V 的此项前置。 |
| W-ACCEPT | 合并后限定本地验收；V | G-CALLERS、G-PLATFORM | `G-ACCEPT`：V completed 且必要 V1–V7 独立证据充分，无阻断缺陷/缺证，实际对象与已完成 A/B/C 相符；建立本 Plan 限定本地完成，不释放发布或 IDE 声明。 |

partial/failed/blocked、缺失或不可采用 Result、必要证据不足均不满足相应 gate。独立执行者的任务完成不自动代表验收通过。Router 必须完整读取并关联原 Result；现有终态不 resume、不重写为成功，未完成边界只按授权形成后续有界结果并保留旧事实。本图不指定自动恢复或 dispatch 顺序。

## 分组依据、依赖及作者语义自检

1. **独立终态检验**：A 完成后 Codev 的其他 caller 已可消费，B/C 失败不否定该能力，故不能藏进 B。B 完成可保留精确选择/委托和同源来源，C 仍可沿旧强转路径工作；C 完成也可保留数据化入口，而 B 沿旧清理 action 工作，故二者分别是有意义结果。A 的 Fixture/caller、安全回归与 B 的 private init/prepare/来源检查均只证明本项接口/接线可用，没有另一个持久产品结果，不按 helper/文件拆项。V 是合并对象的独立接线验收事实，与作者修复分开；它消费 A 的组件证据，不重做全引擎认证。
2. **真实依赖与共写**：唯一生产业务依赖 A→B；B、C→V，A 经 B 到 V。B/C 的共享根源码、测试、metadata/构建输出要求串行，但不证明业务先后；C 的旧/新本地 API 条件明确。A/C 如使用同一 Codev JAR 也串行。一个安全串行排列在逻辑上存在，但本文不据展示顺序发出调度指令。
3. **联合等待检查**：每项恰属一个非空 Wave；W-API/W-PLATFORM 无前 gate，B 只等 A/G-API，V 只等 B/C 及其 gate；G-API 等 A、G-CALLERS 等 B、G-PLATFORM 等 C、G-ACCEPT 等 V。将 item prerequisites、Wave 前 gate、gate 等全部成员合并后无自等或环；没有 A/C 等后续验收才能开始、亦无 gate 等自身释放。
4. **覆盖和可获得性**：Plan 的 Codev 独立能力→A，精确委托/来源→B，纯数据与平台原行为→C，合并后 V1–V7→V 及最终 gate；API 兼容变化、producer 边、首产、故障安全、完整模型和取消 IDE 的边界均有归属。必要静态接缝与实际 fixture body 已读，hook 的库 API 方法依据来自已采用技术结论；执行才产生的 getter/隔离/缓存/安全证据没有被要求预先存在，也没有以未来研究 item 解决路线未知。
5. **当前事实与自检边界**：原作者自检之后，独立计划复核及用户当前范围确认已完成，Router 已采用实际 item/gate 完成事实。本次维护只检查四文档互链、必要引用可读/锚点、状态与合同一致性；A/B/C/V 的分组、充分证据、A→B、B/C→V、共享资源串行冲突和原 Wave 等待均保持，未创建第二图或追加评论执行清单。本次不是新的独立计划复核或动态验收。
