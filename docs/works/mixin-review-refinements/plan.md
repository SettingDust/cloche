# Mixin 评审修正实施 Plan

[Work](work.md) · [Context](context.md) · [唯一匹配 Waves](waves.md)

状态：限定本地交付完成。独立计划复核无阻断通过，用户已确认披露范围并授权实施及同范围记录维护；Router 已采用 A/B/C/V 完成事实并判定全部必要 gate 满足。实际验收见[独立报告](../../../build/tmp/v-accept-20261105/REPORT.md)，完成与边界见 Context；本次仅维护文档。一个有界评审修正交付使用本 Plan，无另一个 overview 或执行图；items、依赖和 gate 只定义在 Waves。

## 目标、范围及 authority

在 Context 所列已对齐 authority 下，让 Codev 对一个原 JAR 和有序记录集合承担完整清理；Cloche 保留原 C/精确 S、生产关系和消费出口；统一 compilation 入口以平台纯数据登记 AW/Mixin/Strip/Decompile，移除 Fabric 强转和 null 注册信号，保留原平台行为。

Codev PRD/ADR reuse、Cloche PRD reuse/ADR skip，两方 Spec 的 update 已完成，本 Plan reuse 当前正文。具体依据及英/中一致性见 Context 的 authority 评估。本准备不修改 authority，不以旧成功或 Spec 作者自检代替新接口验收。

交付范围包括必要生产调用迁移、既有测试/fixture 的适配和聚焦回归，以及本地新 API 在实际编译与消费链中的验证。正式依赖版本/锁/仓库/传递图、公共出口、引擎/JSON 算法和旧交付不改。排除额外 DTO、公开 helper、平台适配框架、通用 Tooling 调度器、兼容壳、无关角色属性精简和整仓格式化。用户新增的 fixture 环境属性移除与实际环境继承是本 A/B/C/V 图之外的独立追加维护，按 [Spec 的测试运行环境来源](../../spec/mixin-preapplication.md#测试运行环境来源) 约束；旧全局排除不再适用。该维护不由 B/C 承担，不扩充 V、各 predicate、gate、依赖或验证要求，也不添加 Git/发布 gate 或第二执行清单。

## 已交付的组织和迁移（保留确认合同）

### Codev：一份 action 管一个有序集合

已实现的 `StripMixins.Parameters.removalsFile` 保留名字，类型从 `RegularFileProperty` 改为 `ConfigurableFileCollection`，以 `@Classpath` 跟踪普通 N 文件完整内容及有效顺序；调用写法为 `parameters.removalsFile.from(orderedProvider)`，不增加第二个 order 输入。按 File 相等关系仅保留首次出现；不同路径即使 basename 相同也不能合并，去重不以规范路径/内容等价替代 File 语义。零份失败，合法空记录仍产生单独副本。

原 JAR 与全部记录先完成输出别名、硬链接、符号链接/真实父目录及目录包含保护，之后才能失效旧输出或写出。一次 `transform(outputs)` 只登记一个最终结果，持有一次输出占用；每份记录调用不变的 `ExactMixinStripper.rewrite`，以前一步的已关闭暂存 JAR 为下一步输入，全部成功并关闭后原子发布。Codev 统一拥有暂存回收、close/move/finally 故障传播及占用释放；保留原异常、cause/suppressed 和已知原输入/记录/步骤定位。Cloche 不复制这些职责；这也不是多个 operation 的事务。

实际 getter 返回类型及 JVM descriptor 改变，旧二进制兼容不承诺。Codev 的 `StripFixture` 和两个 integration caller 的 `.set` 全部迁至 `.from`；单文件用户仍是单元素集合，不另设旧 API。Fixture 的多记录/指定步故障能力仅为此 action 的验收服务。

### Cloche：按原位置选择后一次委托

现有 `MixinPreapplication.kt` 内保留一份私有薄选择器，删 `StripCompilationMixins.kt`。设计上其参数包含 Codev 的记录集合和 `selectedSources`；`@InputArtifact` / S 使用 `PathSensitivity.ABSOLUTE`，保留内容及完整位置跟踪，选择器不标 `@CacheableTransform`。

入口按原 `inputFile.get().asFile` 判 S 一次；未命中直接 `outputs.file(input)` 返回。命中以现有 Fixture 已采用的覆写方式构造直接 anonymous `StripMixins` adapter，将原 `inputFile` 和同一参数对象交给完整 action，调用一次 `transform(outputs)`。此用法已由实际 Kotlin 编译及 Gradle 参数隔离证据支持；不先登记中间 outputs、不对暂存重新判 S，不新增公开包装层。每片游戏原 `P_i` 的 transform 直接注册 Codev `StripMixins`，不经过来源选择器。

两条注册路径均以 `.removalsFile.from(records)` 绑定独立 N，删平行 `recordOrder`：CLIENT 的 common→client，SERVER/joined 一份。保留原 C、artifact/variant/classifier 精确匹配、非 S 原文件和项目生产关系，raw upstream 及必要条件 producer 依赖保持。Codev 负责写出安全和失败；Cloche 负责选对输入、接全记录和 producer，并让 source/game 清理失败阻断受影响 compile、sources、stub、sync。

源选择器不能使用 build cache；同绝对位置、有效输入/实现相同且 workspace 尚存时的复用须单独判读，不能称为 FROM-CACHE。游戏直接 action 的缓存按 Codev 新集合指纹验收。共享参数变化可能让非 S 节点重进选择器，但仍不能对其执行 Strip。

### 通用 Info：数据输入，基类登记

确认的数据职责如下；字段名仅说明设计职责，实际实现及适用证据由 Context 和 V 报告定位：

| 输入 | Fabric | Forge/NeoForge | 使用者 |
| --- | --- | --- | --- |
| 有序 `namedMinecraftFiles` map | common→client 的 named 原料 Provider | joined 原料 Provider | 基类登记 primary/other AW，建立原拓扑 rawGames |
| `primaryMinecraftPart: Provider<String>` | 原 distribution client 选 client，否则 common | joined | primary AW、sources 原选片及原目录命名 |
| `runtimeExtraMinecraftFiles` 本地原料 map | 只有原来由本 compilation 产生的辅助 common | 空 | 基类用 target AW 登记旧本地 runtime 辅助任务 |
| `extraClasspathFiles: Provider<List<RegularFile>>` | 独立 client 及其 data/test 接 main.finalMinecraftFile；其他为有值空集合 | 有值空集合 | 外部现成 runtime AW，不承担任务注册信号 |

`FabricCompilationInfo`、`FabricTargetImpl`、`FabricClientSecondarySourceSets` 和 `ForgeCompilationInfo` 的构造一起迁移。Info 不接任务工厂，不回填 Property，也不在平台外部提前登记 AW。统一入口按 primary Provider 选原料登记 primary，另片仍使用本 compilation 显式 AW/其 raw 依赖；本地 runtime common 使用 target AW，按原目录和 featureName 规则登记，仅原 distribution client 时惰性加入 runtime extra。独立 client/data/test 的 main AW 不替换成本 compilation 的 preview/common AW。

`preapplicationSide`、primary 及 target 开关分别保持 Provider，不能以 Mixin CLIENT 推导 primary 或 runtime extra。未分割且非 includedClient main 仍 common sources 且无新增 runtime common；独立 client/includedClient sources 仍 client。CLIENT 编译游戏 client→common，两份 N 仍 common→client。保持原 primary/other/common 任务名及目录、FinalJarTasks、compileMinecraftFiles/runtimeExtraClasspathFiles/必要 producer 出口，以及 remap/include/outgoing/legacy 的原输入。

此结构把平台差异留在输入数据，任务和副作用归基类。相比保留强转/null 哨兵，它使来源显式；相比 Info 工厂或平台预注册，它不产生第二任务所有者。相比新增通用转换框架，直接 adapter 复用 Codev 原生命周期，维护成本限参数与调用迁移；没有证据要求这些更大替代路线。

## 开发编译与实际消费来源

以下来源安排保留本图原有的新 API 验证范围。其中 PATH Gradle 9.5.1 / JDK 17 是旧显式环境证据的取得环境；不将它固定为独立追加维护后的 runner/helper 来源，也不以旧 B 证据证明环境继承成功。新增来源合同见上述 Spec；追加维护后的实际环境继承及受影响消费已由 V 核查，原显式环境作者证据仍仅保留原适用范围。Waves 的原合同不变，本次未重跑。

正式依赖来源不变，本地验收已采用 Context 所述私有方法；下列保留来源与验证合同，实际完成证据见 V 报告，不是新的待执行清单：

1. 核对 PATH 实际 Gradle 9.5.1、宿主 JDK 17、Cloche toolchain 17 / Codev 编译 toolchain 11 与测试 launcher 17 可用，不安装替代环境。构建当前 Codev `:minecraft-codev-mixins:jar`，从真实输出识别本地 JAR，检查 getter 已是预期集合类型。
2. 单次 Cloche 私有 init 在 `projectsEvaluated`、任何输入快照前，按 `ModuleComponentIdentifier` 识别原 Mixins 实际文件，仅在任务实际文件集合去旧加新；正式解析图及其他依赖完整保留。覆盖 `compileKotlin` / `compileTestKotlin` 的 libraries、实际 KSP `kspConfig.libraries`、JavaCompile/Test classpaths 和 `pluginUnderTestMetadata.pluginClasspath`。CF 的原 `from/builtBy` 必须复制到独立集合，避免把被改集合放回自身。
3. 产出私有 metadata 并使 `fixture.metadata` 同步；最小调整 `prepare` 去掉旧硬编码单点替换，让原 `paths` 到 JavaCompiler、TestKit 和 Tooling 都来自该 metadata。入口加载已编译 Cloche 与同一本地 Mixins；不添加产品开关，不 publishToMavenLocal，不改共享缓存或正式依赖配置。
4. 在新输入快照及执行时观察：各实际 classpath 唯一新 Mixins JAR、其余依赖完整，getter 返回类型/JVM 调用 descriptor、metadata 字节对应，以及实际宿主与下游加载来源。一次编译成功或只打印候选路径不足以证明此链；出现类冲突/旧 getter/参数隔离失败需在同范围纠正并重验，路线性反例具体报告。

通用 Info 修正在委托迁移前也能独立交付；其检查使用执行时当前、来源明确的本地 Codev 接口。最终合并验收统一使用迁移后的新 JAR。共用源码、根构建、metadata 和本地 JAR 必须按 Waves 的冲突串行，不能把这种资源等待伪造成业务依赖。

## 验证合同、完成证据与成功边界

作者在各生产 item 内留下证明其单一终态的最小必要回归；独立验收是 Waves 中单独的只读结果，不和生产混作一个 Result。不要求固定计数或新增固定报告包，现有测试结果、真实消费产物和必要日志足够，须能关联实际对象、输入、版本/环境、执行命令及限制。

| 成功条件 | 充分验证安排 | Waves 覆盖 |
| --- | --- | --- |
| Codev 新接口与安全 | 扩展现有 StripMixinsTests/StripRegistrationTests/StripStreamTests 和 caller；真实多记录、零/空、重叠/缺配置、同 basename 不同路径、File 去重、顺序/内容指纹及新进程；覆盖后份记录的全部输入保护、后步失败、close/move/清理故障与恢复 | A；V 独立核验 |
| Cloche V1/V2/V6，V7 委托与消费 | 既有 classifier 保留 extra 的真实 Java/Kotlin 正负消费；精确 S 与非 S、C/view/SourceSet、项目 NO-SOURCE/producer、实际 remap/refmap/context/direct 及错误阻断；source/game 首后步失败后恢复；原 sources、stub/sync 的受影响消费 | B；V 核验合并后证据 |
| Cloche V3/V5 及相关 V1/V7 | sides 的 14 类分类基础加原 runtime/AW 差异、split/unsplit/included/client:data/test/joined、Provider 后配置和 target 隔离、任务/目录/featureName；实际原选片 sources 和另一 clean library、runtime/legacy/发布文件仍属原链 | C；V 核验合并后证据 |
| Cloche V4 和各入口首次生产/完整模型 | common 两参与者交集、关闭参与者和回退；file/Maven 的编译、sources、stub、sync 各自缺 P/N/clean/workspace 的私有首产；独立 C/view/SourceSet 无同场游戏代调度；同请求 Tooling 先生产后严格读完整模型，身份/项目/闭包/scope/有效文件 | B/C 按影响留作者证据；V 逐条核实 V1–V7 |

故障测试保留原生产先后和消费未成功断言，适配旧 action 名/暂存前缀或异常包装时不能删保证；观察实际记录/步骤、原始 cause/suppressed、失败后无当前部分/旧结果冒充成功、撤故障后恢复。顺序清理结果可能相同，记录顺序指纹用真实 Transform 执行/缓存失效证据证明，不能仅比较最终 JSON。

新接口和重组影响的证据重新取得；未变引擎全矩阵仅在对象/输入/内容/环境/消费者与新声明仍匹配时复用，记录适用性，不能把旧单记录缓存证据升级成多记录通过。不重做无关完整历史矩阵。include 等必要路径若尚无适用证据，必须补充范围内验证或保留为未满足条件，不凭旧报告标题关闭。

全部必要 gate 以 Waves 为准：`G-API`、`G-CALLERS`、`G-PLATFORM` 的增量结果及最终 `G-ACCEPT` 建立本 Plan 的限定本地完成。最后一个 gate 不保证正式发布或最终 IDE 行为。发布接入、最低 9.4 完整矩阵及最终 IDE 导入均排除；最终 IDE 模块/根/符号、原生 data/test archive/index 和任意重复类/插件风险随最终声明保留。

## 风险、恢复与作者自检

API/ABI 变化要求已有 caller 同步，禁止兼容 getter 掩盖实际调用。私有 classpath 若漏 KSP/宿主/JavaCompiler 会形成混合来源，因此以实际消费核验；当前实际同源 hook/消费证据已由 V 核查，详见报告；本次文档维护未运行 hook。Map/Provider 若提前求值或混淆 side 与 distribution 会改变 runtime，故以 C 的分类和差异 AW 检查约束。共享根构建和 metadata 不能并行改写；基线含既有用户改动，修复只触及本 Scope，不 reset、清共享缓存、停共享进程或重放旧清理。

原作者已完成组织路线和图语义自检，独立计划复核及用户实施确认随后完成。Router 已采用 A/B/C/V 原始 Results，判定 G-API、G-CALLERS、G-PLATFORM、G-ACCEPT 满足；本次只同步既有判断。V 报告支持合并对象 V1–V7：15 个聚焦方法、30 个 fixture、199 项复核及模型 1067 比较通过，恢复连续性核对后定向补原生输入失效证据，不因重启整批重测。各范围的原失败及环境迁移 partial 保留，不能把最终适用成功写成历史每批全绿。

本次文档自检只覆盖四份派生记录、相互链接、必要引用及合同/完成事实一致性；无新独立复核、动态测试或 gate 判断。七条追加评论的最小处理、环境来源及另行 Git/版本事实见 Context，不增加本 Plan 的执行图或验收范围。已确认 outcomes、predicate、证据义务、依赖、conflict、gates 及风险保持。
