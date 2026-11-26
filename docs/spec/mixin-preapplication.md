# Mixin 编译预应用技术规范

本文拥有 Cloche Mixin 预应用的依赖选择、环境分类、任务接线及消费出口细则；全局组织、兼容与装配时序由 [Target / Compilation Spec](target-compilation-architecture.md) 拥有。产品范围以 [PRD](../prd/mixin-preapplication.md) 为准；下文“必须”“不得”“可以”分别表示要求、禁止及允许的实现选择。本文描述待实现行为，文档完成不表示接线已实现、构建已通过或行为已验收。

## 所有权与兼容边界

| authority | 处理 | 边界与理由 |
| --- | --- | --- |
| Cloche PRD | reuse | 产品目标不变；保留常规 JAR 原生属性链的预应用及清理、默认启用、外部 mod compile sources、有效环境及运行/发布隔离；本次组织协调不改变这些范围。 |
| Cloche ADR | reuse | [函数式组合 ADR](../adr/target-compilation-functional-composition.md) 拥有已接受的全局方向与直接组合取舍；此前“无需新增独立架构决定”仅指当时的 Codev 接口及测试环境来源修订，不排除本独立全局决定。 |
| Target / Compilation Spec | reuse / 衔接 | [全局 owning Spec](target-compilation-architecture.md) 拥有职责边界、唯一原位组合、Joined/Split 固定拓扑与阶段投影、兼容与时序；本文独占预应用来源/分片、records/state、四消费者、失败/缓存及 V1–V7 细则。具名入口协调已完成；本次定点承接其第 2、5、8 节的模型、公开兼容与已收窄的内部兼容边界，不据此建立实施许可或 readiness。 |
| 本 Spec | update | 本次仅同步全局 owner 已确认取消的旧内部 Map/String、synthetic accessor 及 preview adapter 兼容承诺；统一注册所有权仍在 compilation 的唯一 `setupFiles` 组合处，消费准备及明确保留的公开/兼容出口合同不变；保留 Codev 多记录清理、Cloche 选择/绑定/消费、平台纯输入与必要私有编译来源。既有测试沿实际运行环境、移除 `fixture.gradleInstallation` / `fixture.gradleJava` 及其覆盖链的合同不变；分片输出、JAR 默认 raw、原 Configuration、精确选择、原生项目关系及已取消最终 IDE 导入的决定不变。作者自检不表示接口或环境迁移、动态验收已通过。 |
| Codev PRD / ADR | reuse | 沿用已确认的产品与 operation 隔离边界，不在本文件修改。 |
| Codev Spec | 已更新，本文对齐 | [当前 Codev owning Spec][codev-spec] 拥有引擎、映射、精确清理及多记录 `StripMixins` 的完整安全生命周期；本文只规定 Cloche 的记录选择、调用和消费义务。 |
| Maven 发布 authority | skip | 本次不改变其决定或交付范围。 |

必须复用 Codev `Mixin` Task 的单输入 JAR 接口，以及既有 `StripMixins` 改为有序记录集合后的完整 Transform；不得在 Cloche 复制逐记录清理循环、暂存、发布、输入保护或异常清理。`StripMixins.Parameters.removalsFile` 保留属性名，类型由 `RegularFileProperty` 改为 `ConfigurableFileCollection`，调用方必须迁移至 `.from(...)`；getter 的 JVM 返回类型实际变化，不承诺旧 API/ABI 兼容，不增加双套接口。本轮开发验收可以使用下节限定的显式本地 Codev 修正版，不要求其先发布。不引入多目标引擎、分区协议、通知合并或 refmap 重写器；多分片仍多次调用同一单目标 Mixin 注册实现。Codev 允许输出的下游用途由调用者决定；编译/运行隔离是 Cloche 的产品选择。

必须保留现有 [构建依赖与工具链](../../build.gradle.kts) 和 [插件最低版本检查](../../src/main/kotlin/earth/terrarium/cloche/ClochePlugin.kt)：最低 Gradle 为 `9.4.0`，Cloche Kotlin JVM toolchain 为 17，不把该 toolchain 等同于 Gradle daemon 的 JVM 要求。当前 Codev fork 版本如下，本规范不要求改号、升级或重发；锁定引擎仍为 `net.fabricmc:sponge-mixin:0.15.0+mixin.0.8.7`，见 [Codev 模块构建][codev-build]。

| `net.msrandom` 模块 | 版本 |
| --- | --- |
| `minecraft-codev-core` | `0.1.0+terrarium.0.7.2` |
| `minecraft-codev-forge` | `0.1.1+terrarium.0.9.2` |
| `minecraft-codev-fabric` | `0.1.1+terrarium.0.7.2` |
| `minecraft-codev-mixins` | `0.1.1+terrarium.0.6.0` |
| `minecraft-codev-runs` | `0.1.0+terrarium.0.7.2` |
| `minecraft-codev-access-widener` | `0.1.0+terrarium.0.6.3` |
| `minecraft-codev-remapper` | `0.1.0+terrarium.0.7.2` |
| `minecraft-codev-decompiler` | `0.1.0+terrarium.0.6.0` |
| `minecraft-codev-includes` | `0.1.0+terrarium.0.6.5` |

本方案通过公开的 `dependencies.artifactTypes` 为 `ArtifactTypeDefinition.JAR_TYPE` 设置默认 raw 状态，依据已接受固定参考的 [JAR 默认状态][historical-jar-defaults] 与 [原 Configuration 接线][historical-compilation]。撤去 `DependencyHandlerInternal.getDefaultArtifactAttributes()` 全类型默认及为此新增直接 Gradle internal 依赖的要求。该方法不要求提高最低 Gradle 版本；既有 `9.4.0` / `9.8.2` 定点静态证据可用于理解属性与 Transform 行为，但不等于本次 JAR 接线已运行。保留 from/to 属性、Transform 参数处理与 ABSOLUTE 限制的版本依据：[9.4 Transform][gradle94-transform] / [9.8.2 Transform][gradle98-transform] 及 [9.4 workspace 复用][gradle94-workspace]；这些链接不是动态验收结果，也不能证明 Transform 参数的生产来源会自动进入消费者任务图。通知生产必须满足下文独立的物化时序合同。

## 开发验收来源与正式依赖边界

用户已明确接受本轮以未发布的 Codev 局部修正版作为开发验收来源。许可覆盖因上述 API/ABI 变化而必要的 Cloche 插件自身私有编译及同源行为验收；不改变上述正式依赖声明、坐标和版本、锁定引擎或 V1–V7 的行为与证据要求。无需先提交或发布修复，也不新增重复批准门槛。

必须先取得本地 Codev Mixins 生产 JAR `E:/Projects/cloche/minecraft-codev/minecraft-codev-mixins/build/libs/minecraft-codev-mixins-0.1.1+terrarium.0.6.0.jar`。允许在单次 Cloche 运行的私有配置中，在相关输入被解析、快照或编译前，按模块 identity 在实际 `compileKotlin`、测试编译、KSP、`JavaCompile`、`Test` 和 `pluginUnderTestMetadata` 输入中定位并替换恰好一份旧 Mixins JAR；不得只改 fixture 而让插件自身继续按旧 getter 编译。替换必须保留其余依赖、条目次序、原传递图及 `builtBy`，不得构成文件集合自引用、混入测试 SPI 或使用 Codev 整份 classpath 替换 Cloche 依赖。

plugin-under-test metadata 输出必须重定向至私有目录，并使 fixture 的 `clocheClasspathMetadata` 与 `fixture.metadata` 指向同一私有来源，保证既有 prepare 路径、JavaCompiler、嵌套 TestKit 和 Tooling 消费同源字节；不能仅凭根命令参数推定已送达。正式依赖声明、锁、规范 metadata 和共享模块缓存必须保持原状；不得使用 `publishToMavenLocal`、隐式同坐标覆盖或输入不可用时静默回落发布 JAR。此处允许必要来源安排，不固定 init 脚本实现或宣称其已经运行。

来源证据必须与本轮真实 Cloche 编译及消费相连：核对实际输入中唯一 Mixins JAR 的模块 identity、路径与字节，验证编译及运行使用的新 `removalsFile` getter 返回 `ConfigurableFileCollection` 的 JVM 描述符；核对实际加载的 `Mixin`、`StripMixins`、`ProduceMixinsKt` 及宿主异常类的 code source 为同一本地 JAR，classpath 无旧 Mixins，并观察实际 Mixin Task 的 `implementationClasspath`、锁定 Sponge Mixin 来源及 Gradle/JVM/Home。路径、文件名、类存在或 Codev 组件测试通过均不能单独证明 Cloche 已消费新接口。实际加载与不同依赖组合的兼容性、真实失败及撤故障恢复必须由 Cloche 行为证据验证；context 故障须由正常 daemon 返回真实原因及诊断类型，不能以通信异常或任意非零退出冒充失败传播。

### 测试运行环境来源

用户最新要求已明确移除 `fixture.gradleInstallation` 与 `fixture.gradleJava`，测试必须沿用实际运行环境。必须删除 [构建任务](../../build.gradle.kts) 中 `mixinCompilationTest` 的两项声明及 `includeIdentityTest` 的 Java 声明，并移除 [MixinCompilationTests](../../src/test/kotlin/earth/terrarium/cloche/MixinCompilationTests.kt) 与 [IncludeTargetIdentityTests](../../src/test/kotlin/earth/terrarium/cloche/IncludeTargetIdentityTests.kt) 对这些值的消费及相应发行版/JVM 覆盖，包括普通 `GradleRunner` 的 `withGradleInstallation(...)` 和 `-Dorg.gradle.java.home` 参数；不得换成新版本选择、同义属性、环境变量或其他可配置覆盖。普通 `GradleRunner` 必须采用其默认的实际 TestKit 安装和客户端 worker JVM 来源，不能把外层配置 JVM 的 `System.getProperty("java.home")` 当成 worker 或子构建的来源证明。现有两个 Test 任务的 Java 17 launcher、`fixture.metadata`、私有 Home 及必要同源字节核对保持。

独立 [R4Tooling helper](../../integration-test/mixin-compilation/R4Tooling.java) 必须由 worker 实际 `java.home` 下的 `javac` / `java` 编译、启动。必须记录实际已加载 `GradleRunner` 的 code source；其编译和启动所需的 Gradle 工具库必须沿实际默认 Runner 的同一类空间安装定位机制取得：复用 `org.gradle.internal.installation.CurrentGradleInstallation.get()`，取得非空 `GradleInstallation.getGradleHome()`，规范化安装根并核对该根及其 `lib` 工具库与实际 locator / `InstallationBeacon` 来源一致。反射调用时必须由实际 Runner 的 class loader 解析 locator；直接引用时也必须核对其为默认 Runner 链接的同一份类。安装路径必须由运行时取得，不得写成常量或新增 fixture 覆盖；不得从 generated API/TestKit 缓存父目录猜测安装或只比较版本后切换安装。generated API/TestKit JAR 本身不是安装根，TestKit JAR 位于 `lib/plugins` 仅是旧静态调查的布局观察，不是必需物理位置。helper 必须从自己实际已加载 `GradleConnector` 的 `lib` JAR 定位安装根，与 worker 观察的安装根核对一致，再以 `useInstallation(...)` 绑定；这是已加载工具库的来源绑定，不是可配置环境覆盖。工具库只取该安装的 `lib`，不得传递整个 worker classpath。定位为空、locator/beacon 来源缺失或不符、安装/工具库不完整、helper 来源不符或 JDK 工具缺失时必须带具体来源诊断显式失败；不得改为裸 Tooling 连接而隐式选 wrapper/下载，也不得静默回落 PATH、其他安装/JDK 或下载。

`CurrentGradleInstallation`、`GradleInstallation` 及 beacon 属于 Gradle internal 安装机制，JVM 方法为 public 不表示受支持的公开 API；公开 `GradleRunner` 没有 installation getter。升级 Gradle 时必须定点复核 internal 签名、Runner 类空间及 beacon 布局，并重新验证启动来源。[generated TestKit 定位诊断](../../build/tmp/generated-tooling-diagnosis-20261105-a/REPORT.md)记录了当前 Windows / Gradle `9.5.1` / JDK 17 下，generated TestKit、generated API 的 locator 与实际安装 beacon 可以同时存在；重建加载器的正向与移除 beacon 负向探针，以及安装 `lib` 隔离加载 Connector 的来源检查支持该定位方法。这些探针不是原 worker 原位拓扑，也没有执行真实 helper 生命周期、Tooling 连接或模型请求，不证明最低 `9.4.0`、其他版本/平台或任意布局兼容，亦不要求 generated 重定位 JAR 与发行版 JAR 整文件字节相等。

helper 参数必须缩为 `root / scenario / options`，不再接收 `installation` / `javaHome` 或传递 `-Dorg.gradle.java.home`；同请求先准备、后建模以及真实失败阻断和撤故障恢复要求保留。这里的工具库仅供独立 helper 使用；prepare 中编译真实插件的 `JavaCompiler` 必须继续使用 metadata classpath，不得换成 Gradle 工具 `lib`。上文私有 metadata、新 JAR getter/descriptor、实际宿主/下游字节及引擎来源检查均不变。

环境继承验收必须区分外层配置 JVM、Test worker、独立 helper 与子构建，观察实际 `java.home`、已加载工具类及 locator/beacon 的 code sources、安装根和 Tooling `BuildEnvironment` 所报的 Gradle/JVM 路径，不能只比较版本或命令字符串。worker 的 `java.home` 仅用于 helper 编译、启动工具，子构建不得添加显式 Java 覆盖。实际继承仍须取得运行证据；本修订不改变既有环境迁移 partial：已采用的普通 TestKit 与 include 成功保留原范围，Tooling 在 helper 启动前因旧物理布局 guard 失败不等于真实 namespace 故障传播。恢复实现后仍须取得真实成功、namespace 故障阻断、撤故障恢复及同请求准备先于 builder 的证据，并核对原输入保护；locator 探针不替代这些义务。所采用方法的已核对边界仍为 Windows / Gradle `9.5.1` / JDK 17 和指定本地新 Codev；它既不提升最低版本，也不证明最低支持矩阵、其他安装布局、正式发布来源或最终 IDE 消费通过。

环境迁移后，必须针对实际输入、来源或运行条件受影响的 V1–V7 重新取得证据。原 `migrate-cloche-strip-callers` 的旧显式环境作者证据可以保留在其原 Scope 下，本次不改其 predicate、不宣告其结果无效或 gate 满足，也不以这些旧证据证明环境继承成功。本修订承接用户最新移除要求，不表示已独立复核新增实施图或已取得其 C4 确认；不新增 Plan/Waves 或调整实施顺序。

完整满足 V1–V7 后，可以据证据声明“本地修正版下的开发功能验收完成”，覆盖编译、反编译、stub 和 sync 的 Gradle/真实 Tooling 模型及相关现有消费检查；该结论必须注明本地未发布来源及最终 IDE 导入、模块/输出根、符号行为未验证，不得扩展为最终 IDE 消费验收。切换二进制或消费输入时须定向重验受影响证据，不能把旧发布版或 Codev 六项组件回归扩展为本地 Cloche 消费成功。完整 `S + {P_i}` 清理、原依赖身份、scope、失败传播、关闭、缓存及运行/发布隔离要求全部保留。

正式发布依赖接入是独立的证据边界：本地开发验收完成不表示原发布坐标含有修复，也不表示正式发布版可用。当前局部修复尚未发布，正式发布依赖接入仍未完成；本规范不授权构建、提交、推送、发布或版本变更，也不规定新的发布步骤。后续正式来源的可用性和受影响消费者证据必须据实区分，不能将其未完成状态改写为本地开发验收的先决发布门槛。

## 启用配置与继承

公共入口确定为 `MinecraftTarget.mixinPreapplication: Property<Boolean>`，约定值为 `true`。在 Fabric、Forge、NeoForge 的具体 target DSL 内执行 `mixinPreapplication.set(false)` 即关闭该 target 的预应用；不要求重写依赖声明。

此开关属于具体 Minecraft target，其 main、独立 client、data/test 及 client 下 data/test 必须读取同一个 target 属性。首版不增加 compilation 局部覆盖或 common stub 自身的开关。`includedClient()` 属于所在 target 的 main 形态，同样继承该值。一个 target 关闭不影响其他 target；common stub 按原有参与关系读取每个 target 各自的有效视图，允许其中包含关闭后的旧视图。

开关必须独立于 `Compilation.mixins`、`withMixinAgent`、Loader metadata、AW 和发布开关。`Compilation.mixins` 仍表示本项目声明的 Mixins，不是预应用 sources。开关关闭时，编译、反编译、stub 和 IDE sync 必须恢复原 AW 游戏及正常 remap、未 StripMixins 的编译依赖视图；其消费链不得执行或依赖本次 Mixin/Strip。底层 AW、正常 remap 和既有 runtime 解析仍按原规则工作。注册但未被请求的任务可以存在。

配置值必须通过 Gradle Provider 接入消费选择；不得在 DSL 尚未配置完时固定默认值。启用状态、side 和 sources 的确定必须先于受影响视图解析及消费，不能通过任务 `doLast` 修改已解析的 classpath。

## 有效环境

分类必须使用 target 的 `hasSeparateClient` 以及 compilation 的所属关系；不得将 Gradle `ModDistribution.common` 或名称中的 `common` 直接映射为 SERVER。`includedClient()` 不属于独立 client 分割。

| 具体 compilation | `Mixin.side` | 游戏目标 |
| --- | --- | --- |
| 分割 Fabric main | `SERVER` | 本 compilation 的原始 AW common |
| 分割 Fabric 独立 client | `CLIENT` | 本 compilation 的原始 AW client、common 两片，分别预应用 |
| Fabric includedClient main | `CLIENT` | 同上 |
| 未分割 Fabric main，包括未调用 includedClient 的形态 | `CLIENT` | 同上；两片进入编译视图，不因原 sources 仅选 common 而遗漏 client 分片 |
| joined Forge / NeoForge main | `CLIENT` | 本 compilation 的原始 AW joined JAR |
| data / test | 继承所属源集环境 | 按自身 compile 图、AW 输入与所属游戏形态构造；分割 main 下 SERVER，独立 client 下 CLIENT，非分割 CLIENT |

Cloche 不提供新的 side 选择 DSL，不使用 `UNKNOWN` 代替明确环境，不额外对同 target 执行另一侧 operation。joined 表示游戏文件形态，不表示两侧一起应用。

## 输入集合与依赖身份

以下名称是内部职责，不要求公共 DTO 或新增通用框架。“原始”表示相对于本次 Mixin/Strip 未处理，可以已经过既有 named remap、AW、include 处理。

| 符号 | 定义 |
| --- | --- |
| `C` | 当前 compilation 的原 compile Configuration 及其依赖图；保留既有 remap/include、compile 变体、scope 和项目关系。启用时 `C` 自身请求本 compilation 消费视图状态以供清理消费；关闭时不安装本私有 state 请求，恢复原请求。需要未清理输入时，从排除项目组件的上游 artifact view 按需显式请求 `state=raw`；该 view 不选择或证明项目文件/目录角色，项目角色由原 `C` 保留。 |
| `R` | 对应 runtime configuration 的原始外部解析结果；正常 remap、未 StripMixins，保留 runtime 变体和 scope。 |
| `S` | `C` 的上游非项目 raw view 中，处于下述常规 JAR 原生属性适用范围、按 Cloche 既有 mod 分类选出的外部 mod artifacts；包含 compileOnly 和既有传递成员，排除 `ProjectComponentIdentifier`。 |
| `T_i` | 本 compilation 在上表环境下所需的第 `i` 份原始 AW 游戏分片，处于实际开发字节码命名空间；Fabric SERVER 仅 common，Fabric CLIENT 为 client、common，Forge/NeoForge 为 joined。 |
| `P_i` / `N_i` | 对 `T_i` 调用 `Mixin` 所得的 `outputFile` 派生分片及其独立 `appliedMixins` 记录；同一消费视图保留各份文件，不生成汇总通知。 |
| `cleanP_i` / `cleanSources` | `P_i` 及确切 `S` 分别按该视图全部记录顺序清理后的最终结果。非 `S` 保留原文件或原生目录角色，不属于 `cleanSources`。 |
| `compileMinecraftFiles` | 本 compilation 的有序清理游戏集合；Fabric CLIENT 为 `cleanP_client → cleanP_common`，SERVER/ joined 各一片。该游戏分片次序不同于原 `C` 无需逐 artifact 保序的约定。 |

支持保证由 JAR 默认 raw 状态及正常 remap/include 后的实际候选属性链界定，不按文件后缀再建通用输入分类。混合扩展名 variant、非标准后缀或无后缀 mod 归档不获通用预应用与完整清理保证，也不另加 JAR+ZIP 支持列表；它们仍可按原规则留在 `C` 中，不删除依赖、不重选原图、不新增 unsupported 扫描或报错框架。Transform 注册不必硬编码 `artifactType=jar`，原 `C` 也不得因此请求 `artifactType=jar`；其他显式带有相应状态的候选可能进入转换，不据此扩大产品保证。

`S` 必须来自当前 compilation 的 compile 图，而非从 runtime 图补齐、按 basename 找 JAR 或扫描任意 classpath。runtimeOnly、项目组件及本项目输出不得因预应用获得编译可见性，不额外递归发现 nested JAR 的 Mixins。保留既有正常 remap/include 行为不等于新增 nested sources 扫描。普通库即使出现在解析 classpath 上也不得成为 sources。

producer 的解析 classpath 必须来自按需显式 `state=raw`、排除项目组件的上游 compile/runtime artifact views、该 compilation 的原始 AW 游戏及所需原始游戏库/插件辅助依赖，不读取已追加 Minecraft 的 `SourceSet.compileClasspath`、`runtimeClasspath` 或新的清理 view。可以用 `C` 的 raw 外部输入与 `R` 提供原有解析所需的依赖；runtime 辅助输入不得覆盖 compile 变体。二者只影响解析，不改变 `S` 或消费者 scope。不得让当前项目输出、其他 compilation 的 stub 或任何预应用/清理结果回灌 producer。普通编译依赖消费者不要求逐 artifact 保序，不免除 producer 对实际有序输入的跟踪。

Codev 的普通任务域查找顺序为 `classpath → mixinFiles → inputFile`。每片 `Mixin` 的解析必须明确跟踪同 compilation 所需的 raw AW 分片及有效次序，沿既有 client 优先关系供给解析输入；另一片只能来自 raw AW，不能读 `P_i` 或 `cleanP_i`。重复类仍允许保留，不能以 Join 或预过滤消除重复，也不能声称任意插件在该布局下已验证等价。辅助依赖不得替换 Codev 锁定的引擎/服务实现。实际配置发现仍由 Codev 针对 `S` 与各 `T_i` 完成；Cloche 不增加日志判定、Mixin 配置预扫描或清理算法。

清理副本必须通过原生解析/Transform 链关联回确切原编译输入，保留原 artifact、component、所选 variant/classifier、项目/SourceSet 关系及 stub 所需的 component 信息。该关联在原解析与外层消费中保留，不要求转换后 artifact ID 与 raw ID 字面相等，也不要求结果占据原 `C` 的位置或维持逐 artifact 次序。不能按 basename、GAV、仅 component ID 或两个 Set 的遍历顺序猜测对应关系，也不能用 runtime artifact 替换 compile artifact。同一组件的另一变体或 classifier 若不是 `S` 的成员，必须保留原文件及既有角色。此身份关联不改变 Codev 通知按配置路径和原始条目字符串匹配的规则，也不得序列化为额外来源证明清单。

## Fabric 分片与独立应用记录

每个启用的 Fabric CLIENT compilation 必须取得自身 named client/common 原始 AW 结果，分别以 `CLIENT` 调用同一 Mixin 注册实现，得到两份 `P_i/N_i`。分割 main 的 SERVER 只处理 common；Forge/NeoForge joined 只处理一片。data/test 使用自己的 compile 图、`S`、AW 和 mapping/context，不得任意复用 main 的 SERVER 预应用结果。可以复用确实相同的 raw AW Provider，不能复用另一有效输入或环境下的 Mixin/清理输出。

两片都必须体现该 compilation 有效 AW，包括其外部 compile 依赖提供的 AW 和既有显式 AW；此处用于预应用的 common AW 不替代下文旧 runtime 辅助 common AW 来源。保留 client/common 两份输出及各自全部条目，允许重复类、资源和 metadata；编译游戏顺序固定为 **client 优先、common 随后**，按既有 classpath 语义处理重名。不得在 Mixin 前或后 Join，不预过滤重复条目，不合并重名类的字段或方法，不新建默认 Manifest 替换原资源。两个 CLIENT Task 不是两个专用注册函数，也不是对同一 target 额外执行 SERVER operation。

每个 Mixin 各自产生 `P_i` 和 `N_i`。Fabric CLIENT 对每个选中外部 mod 和每片 `P_i`，均将 `N_common → N_client` 的有序 Provider 来源通过 `removalsFile.from(records)` 交给 **一次** Codev `StripMixins` 调用；SERVER/joined 只有对应一份记录。Codev 在内部顺序累积清理，只登记和发布一个最终结果。记录顺序与编译游戏的 `client → common` 分别定义，不得混淆；不生成通知并集、合并文件或汇总 Task。

Cloche sources 选择器只在原 `@InputArtifact` 上按完整位置精确判断 `S` 一次：未命中直接返回原文件，命中才把完整记录集合委托给 Codev；后续暂存文件不再参与选择。游戏清理直接注册 Codev `StripMixins`，不得经选择器或 Cloche 逐步发布层。迁移必须删除现有 `StripCompilationMixins` 实现，sources 仅保留薄选择器，不再维护独立 records/order 协议或对同一个 `outputs` 逐次调用 Transform。

Codev 拥有逐记录清理及完整安全生命周期，详见 owning Spec 的 [有序输入][codev-records] 与 [完成/发布合同][codev-lifecycle]。Cloche 必须保留其记录内容/顺序跟踪和失败传播：合法空记录、重复删除及无匹配配置按组件语义处理；零份记录不能作为空成功，实际解析、DOM、I/O、后步、close、move 或清理失败均不得产生可消费的部分/旧结果。所有原始游戏、依赖及每一份记录都受保护；Cloche 不重写 JSON 算法、不增加有效性预扫描，也不复制输入保护或暂存清理实现。

所有必要 `P_i/N_i` producer 和最终清理都成功后才允许消费。raw AW、runtime 游戏分片及原有次序不变。上述是迁移要求；当前 Cloche 自管生命周期的旧实现和历史通过均不证明新接口接线已验证。

## mapping、refmap 与 context

依据当前 [Cloche target namespace 接口](../../src/main/kotlin/earth/terrarium/cloche/target/MinecraftTargetInternal.kt)、[Fabric 分支](../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricTargetImpl.kt)、[NeoForge 分支](../../src/main/kotlin/earth/terrarium/cloche/target/forge/neo/NeoForgeTargetImpl.kt) 以及 [Codev JarRemapper][jar-remapper]：

- 正常 mod remap 将类及适用的 Mixin annotation remap 到开发空间。当前 `JarRemapper.hasRefmaps` 仅检查 JAR 根目录的 `*refmap.json`；命中时只启用 TinyRemapper MixinExtension 的 `HARD` annotation 处理，未命中时启用全部 annotation 目标。
- 当前资源 remap 扩展不构成通用 refmap JSON 重写；不能假定正常 remap 后 refmap 选出的成员已全部变成 named。反之，不能假定所有 selector 都仍处于 mod namespace。
- Mixin producer 的 source namespace 表示 **refmap 解析之后 selector 的空间**，target namespace 表示 **`T_i` 的实际字节码空间**，context 是独立输入。既有空 namespace 分支代表不需要 mod remap，不是一个可填入 mapping header 的命名空间。

Cloche 的默认接线确定如下：

| 输入形态 | Task 参数 |
| --- | --- |
| `modRemapNamespace` 非空的既有 remap 分支 | `mappingFile` 接 `target.loadMappingsTask.output`，`sourceNamespace` 接该 target 的 `modRemapNamespace`，`targetNamespace` 接既有 `NAMED_MAPPINGS_NAMESPACE`。该 tuple 用于现有 remap 未改写的原空间 refmap selector；未命中 source 映射的 selector 沿用 Codev 正常 passthrough，这不保证任意已 remap selector 都不会与 source 名称碰撞。 |
| 无 remap、名称直接兼容 | 三个 mapping 参数均不设置，不强造 `official → named`、空 source 或无用 mapping 文件依赖。Strip 注册与消费仍照常。 |
| refmap context | 默认不设置 `Mixin.refmapContext`，沿用引擎可用且无歧义的 default/context；不从 Loader 名称或 namespace 字符串推导 context。 |

以上是有边界的默认接线，不是任意 mod/refmap 输入的通用兼容承诺。必须通过真实 remap 后 fixture 证明实际 selector 链到达预期 named owner/member，不能仅以 target namespace 属性、配置字符串或“任务成功”证明映射正确。根目录 refmap 检测也不等于任意命名/目录的 refmap 都已被一致处理；本范围不修 remapper 的该限制。

需要显式 context 或其他既有命名参数时，可以通过 Gradle 配置所注册 `Mixin` Task 的现有 `mappingFile/sourceNamespace/targetNamespace/refmapContext` 属性；本范围不另造 Cloche mapping/context DSL。清除 mapping 模式必须一并清除 tuple 三项。显式 context 必须满足 Codev 对本 operation 配置的要求；当前实现会拒绝给 default/no-refmap mapper 强加显式 context，不能声称它能与任意混合 sources 共用。自定义有效输入必须仍由 Task 跟踪，不允许只改宿主属性并绕过其实际追踪。

同一 compilation 消费视图的各片 operation 使用该 compilation 的 mapping tuple 和有效 context，每个 Task 仍只有一组 tuple 和至多一个显式 context。同一 `S` 中若确实需要无法共用的 namespace/context，或者真实 selector 与默认 tuple 不相容，不得猜测、按文件名推断、重写 refmap、拆成两侧 operation 或升级引擎掩盖。必须报告具体受限 artifact/configuration 和实际命名链；需要改变产品语义或 Codev 合同才能解决时属于上游缺口。引擎抛出的缺失 namespace/context 等错误必须阻断消费；引擎正常的可选失败或 default mapper fallback 继续保留，Cloche 不保证识别每个语义错误 tuple，也不新增完整应用证明。

## compilation 统一转换职责

真实 compilation 必须在原构造位置的 `setupFiles` 直接组合 AW、Mixin preview、consumer preparation 与 sources，由此处拥有唯一装配与注册真源；具体职责和时序沿 [全局组合合同](target-compilation-architecture.md#5-setupfiles-的唯一原位组合)。不再要求 `registerCompilationTransformations` 作为内部全量总入口，也不得另设全参数转发层作为主组织或在各平台复制流程。注册先后保持 preview AW → 原本需要的本地 runtime 辅助 AW → Mixin/Strip → sources，各项在原位置登记一次，不放入可重复求值的 Provider 闭包。平台负责向纯输入 `Info` 提供有序 named 游戏分片 Provider、原 sources 的 primary 选片 Provider、本地 runtime 辅助 AW 的 named 原料及相应显式 AW 输入、已有外部 AW extra Provider；`setupFiles` 展开并配对这些输入，窄机制只取得所需领域值和原 Gradle 句柄，不借整个 Info、target 或 compilation 导航。不得 import、识别或强转 `FabricCompilationInfo`，也不得以 `finalCommonJar == null` 作为注册哨兵；该旧 null 所表达的本地 runtime 辅助 AW 注册条件必须改由明确数据输入表达。

`Info` 不得反向接收整个 compilation、提供任务工厂、在组合处外预注册 AW/Decompile 或回填占位 Property。内部游戏主载体必须采用 [全局固定游戏拓扑与句柄阶段](target-compilation-architecture.md#固定游戏拓扑与句柄阶段) 的泛型纯数据有限形状 `Joined(value)` / `Split(common, client)`，贯穿 named、AW、Mixin→Strip 后游戏及内部 Mixin 任务投影；容器不承担行为，类型本身不证明字节阶段或 producer 已执行。固定拓扑与有效选择分开：SERVER 保留 Split 静态注册，仅 common 进入有效 games/operations/records；CLIENT 查找及编译按 client→common、records 按 common→client 分别投影。side、独立 primary 及开关继续由 Provider 延迟决定，后配不重新注册。旧 Info Map/String 构造、getter 及旧 `registerMixinPreapplication` preview adapter 不再受兼容支持，其他已取消的内部入口与明确保留的公开出口以 [全局兼容边界](target-compilation-architecture.md#8-兼容出口运行发布及失败归属) 为准，不要求旧 Map/String 适配或专用兼容测试。内部保持 `GameShards<T>` / typed `Part` 形状，primary 与形状不匹配仍须失败；任务名、目录、operation/type key 及属性中的 `joined` / `common` / `client` 原字符串值必须保留，内部 `legacyName` 字段名可改，不要求恢复旧 `Provider<String>` getter。另行的 runtime 辅助输入保持原协议；不得建立 named→AW→preview 的跨机制 Map 角色协议或逐阶段往返 Map。具体内部标识符及精确签名仍可调整，不得按 SERVER/CLIENT 或 common/client 复制注册函数。原 primary/other 任务身份及其惰性角色映射沿 [全局组合合同](target-compilation-architecture.md#5-setupfiles-的唯一原位组合) 保留，任务/目录的 primary、other、common 及既有输出名字不在本轮改名范围。

Mixin 注册、独立 records、Strip、确切 `S` 选择及原 `C` 的消费 state 接线必须保持同一一致性边界；可以内部细分函数，不得把 state/records 交给消费者任意配对。`setupFiles` 基于同一 preview 结果形成一次相关的有效游戏、sources primary、stub 游戏顺序及必要 operation 选择，再组成 consumer preparation，供编译、sources、stub 与 sync 复用。消费者不得重判 enabled/side/records 或自行枚举 Mixin；角色算法、Transform 与反编译细节不移入组合处。消费准备与实际物化按下文入口合同及 [全局消费衔接](target-compilation-architecture.md#6-统一消费输入准备与实际物化) 分别履行。

预应用分片的 AW 使用当前 compilation 的外部及显式 AW；旧 runtime 辅助 common 若本地生成，显式来源仍是 target AW，不得用当前 compilation 的 preview/common AW 替换。独立 client 及其 data/test 的 runtime common 仍取 `main.finalMinecraftFile`，不改用各自的 clean 或 preview common。仅原 distribution 为 client 时惰性加入本地辅助 extra；未分割且未 includedClient 的 main 即使 `preapplicationSide=CLIENT`，也不得因此新增 runtime common。sources 原选片同样不由预应用 side 推导：未分割 main 的 CLIENT 预应用仍选 common，独立 client/includedClient 选 client。

组合结果必须继续区分原运行游戏、有序 `compileMinecraftFiles` 和原选片 sources。`FinalJarTasks` 在组合末端投影同组结果，新消费输入与原 getters 共用同一生产链；其可见布局、签名和生成接口沿 [全局兼容合同](target-compilation-architecture.md#8-兼容出口运行发布及失败归属)，不得删除实际 compile/runtime 出口。上述内部兼容收窄不允许删除这些明确保留的出口，也不要求恢复旧 preview adapter；不得重新注册或恢复旧总函数为内部主组织。编译、stub、sync 读取同一编译游戏集合及原 `C` 的清理视图。raw AW/extra 供 Mixin 与原协议使用，clean 依赖及另一 clean 游戏片仅供 Decompile 等下游；不得把 clean extra 回接 Mixin，形成 `Mixin → Strip → Decompile/extra → Mixin` 环。关闭时返回原 AW/extra 与原选片 sources，不调度 Mixin/Strip；所有必要 AW/Mixin/Strip 失败继续传给受影响消费者。

## Task、Transform 与无环依赖

本文件以“消费视图”表示一个 project、target、compilation 及有效 side 下共同消费的游戏和依赖；视图内每个分片的 Mixin 调用是一份 operation。不同 compilation 必须具有各自 producer、记录和清理请求，不因 side 相同便共用。复用 `featureName ?: main` 命名，输出路径在 target 外层隔离、其内按 feature 和分片区分；任务名沿既有 Gradle 命名规则并包含必要的 target/feature/分片身份。名称与路径必须在 project 中唯一，不与输入或其他输出重叠，不另造 collapsed compilation 命名协议。

接线必须满足：

```text
compilation → setupFiles（唯一原位组合；各项注册一次）
                ├─ preview AW → 必要的本地 runtime 辅助 AW → Mixin preview → sources/Decompile
                │                                           └─ Mixin / records / Strip / C state 同一边界
                ├─ 同一 preview 的有效选择 → consumer preparation → 四类消费者原接线位置
                └─ 同组结果 → FinalJarTasks 兼容投影及原 getters
C/R 的上游非项目 raw views + raw AW ─────────→ 各片 Mixin 解析 classpath
C 的 raw 外部 artifacts 中的 S ──────────────→ 各片 Mixin.mixinFiles
named 游戏分片 → 本 compilation 原始 AW(T_i) → Mixin_i
                                                ├─ outputFile = P_i
                                                └─ appliedMixins = N_i（各自独立）
C 请求消费视图状态 → 原生候选选择
                      ├─ 无该 state 的 classes/resources → 原生目录及 producer
                      └─ raw 归档 + 正常 remap/include 最终状态 → 选择性 action
                           ├─ 原 input 精确命中 S → 单次委托 Codev StripMixins(有序 N_i) → cleanSources
                           └─ 非 S → 原 InputArtifact 文件
每片 P_i → 独立游戏请求 → 直接注册 Codev StripMixins(有序 N_i) → cleanP_i
clean C + 有序 compileMinecraftFiles ─────────→ 编译 / 非项目 stub、sync
原选片的 cleanP_i + 另一 clean 片作为 library → Decompile → 原 sources 出口
原始 AW 游戏 + 原 runtime 请求的未 Strip artifacts → runtime
```

下文 `operationKey` 作为内部属性值表示整个消费视图的清理请求；不要求将各片的独立记录汇总成一份 operation 记录。Fabric CLIENT 的游戏集合为 `cleanP_client → cleanP_common`，各 artifact 的清理顺序为 `N_common → N_client`；SERVER/joined 各只有一份。

1. 必须使用 Cloche 自有且无冲突的内部 `Attribute<String>` 表示 `state=raw` 或唯一 operation key；仅通过公开的 `dependencies.artifactTypes` 在 `ArtifactTypeDefinition.JAR_TYPE` 的 `attributes` 设置默认 raw。不得为所有 artifact types 填充 raw，也不向原生 classes/resources 目录补该状态。原 `C` 仅在最终启用时通过 Provider 请求 `operationKey`，关闭时不得请求本私有 state（包括 raw），必须恢复无本次 state 请求的原属性集；保留原 remap/include、compilation、Usage、LibraryElements 及 scope 请求，不强加 `artifactType=jar`；默认 raw 本身不执行 Transform。必须支持解析前的后配置 `true → false` 与 `false → true`，不得把早期默认值固化成请求。可以在公开 API 的适当配置/依赖 hook 中按最终开关条件注册 `attributeProvider`，或采用满足同一合同的等效公开 API；不得无条件注册可能 absent 的 Provider 来表示关闭，已注册的属性 Provider 必须始终有值。
2. 每个 compilation 消费视图在 project 级注册选择性 `TransformAction`，由上述默认状态链驱动归档候选：`from` 必须包含 `state=raw` 及本 compilation 正常 remap/include 等管线的最终处理状态条件，`to` 仅设置 `state=operationKey`。注册不必显式限制 artifactType；必须保留源 artifactType/LibraryElements，不强造 JAR，也不把消费者整份属性复制到 `to`。只注册无最终状态条件的 `raw → operation` 不足以保证选择器读到 `S` 的确切最终原输入；不能先旁路再 remap/include，也不能为了清理再次 remap/include 或重新选择另一 artifact。注册最终状态值可在 DSL 配置完成后读取属性，但不得因此提前解析文件。
3. 对已进入转换的归档，薄选择器必须按原 `@InputArtifact` 的完整文件位置是否属于跟踪的 raw `S` 决定分支，只判定一次：确切 `S` 将完整有序记录委托 Codev `StripMixins.transform(outputs)` 一次，由 Codev 只登记最终结果；非 `S` 调用 `outputs.file(input)` 返回原文件，不复制、不执行 Strip 写出、不读取其内部 Mixin 条目。同组件未选 classifier、普通库及必要 project JAR 若进入选择器，均按非 `S` 分支处理。不得复制 Codev 的逐记录循环、输出重叠校验、暂存/原子发布或异常清理；不得对同一个 `outputs` 多次调用 `StripMixins.transform`。自身未声明该 state、没有冲突状态的原生 project classes/resources 由 Gradle 原生候选选择保留，不进入本选择器；不得以 `outputs.dir` 或 `outputs.file` 将目录登记成 Transform 输出，不为 NO-SOURCE 的缺失输出建空目录。此条件不保证任意自定义冲突状态或目录 variant 均可旁路，也不授权更改原项目请求来绕过它。
4. 原生解析与 Transform nodes 必须承接原 `C` 的项目/变体关系和生产依赖。SourceSet 消费集合继续使用原 `C`（启用时请求 operation，关闭时恢复原请求），只为有效游戏 Provider 追加文件配置，并保留既有项目输出追加关系；detached Configuration 可以包装新增游戏，不能用于重建原依赖。不得重建 ProjectDependency、增加 synthetic outgoing clean variant、使用 `withVariantReselection()`，也不以逐 artifact detached configurations 或整段 Callable 文件投影代替原 `C`。转换后 ID 可以改变，原 artifact/component/variant 关联仍须可追溯。
5. 选择器的 `selectedSources` 必须复用 producer 的 raw `S` FileCollection，作为 `@InputFiles` 跟踪内容、成员及完整路径，并保留文件集合及其真实生产来源。每份记录分别绑定对应 `mixinTask.flatMap { it.appliedMixins }`，按本视图规定顺序通过 `removalsFile.from(records)` 供给；沿 Codev `@Classpath` 跟踪普通记录文件的完整内容及集合顺序，不另加 `recordOrder` 输入或 ABI 归一化。Codev 按 `File` 首次出现去重，不得按 basename、相同内容或 real-path 再合并 distinct paths；记录来源不得依赖无序 FileTree/Set 的偶然遍历。每片游戏 `P_i` 保留独立精确文件请求，输入绑定该 Task 的 `outputFile`，从 JAR 到本消费视图的专用游戏清理 type，直接注册 Codev `StripMixins` 并绑定同一有序记录集合，只消费最终 `cleanP_i`；不附带会再次 remap/include 游戏的 compile 属性。不能仅保存路径字符串、依赖同进程内存、在注册时执行 `sources.files`，或在依赖发现时读取尚未满足生产依赖的文件 Provider；不能用预生成文件或去掉 build dependencies 掩盖时序问题。Provider 绑定保留参数来源与输入跟踪，不等于 `C`、继承 artifact view 或 `SourceSet.compileClasspath` 的 `buildDependencies` 会自动包含各 operation；参数来源、文件集合依赖与消费者任务图必须分别核对，具体无环与物化时序仍须动态验证。
6. 所有需要未清理输入的 producer/正常处理回读必须在排除项目组件的上游 view 中按需显式覆盖 `state=raw`，不能随 `C` 的 operation 请求回读清理链而形成 `AW → 选择器 → Mixin → AW`。至少包括 remap 辅助 views（仍保留其 `REMAPPED=false`）、AW 的 `getRelevantSyncArtifacts(compileConfiguration)` 及 CLIENT 辅助 AW、`externalFiles`、复制 `C` 属性的 `roleFiles`、显式 mod/普通依赖角色、`S`、Mixin 解析 classpath，以及发布 remap/include 的 compile 解析 view。runtime 保持原解析请求及项目角色；需要显式状态的上游非项目 runtime view 请求 raw。不得将 raw 请求施加到整份 `C`、runtime Configuration 或包含项目的消费 view，也不得用排除项目的 raw view 替代原项目消费关系。消费 sync/stub 必须使用继承原 `C` 有效状态的非项目 artifact view（启用时继承 operation，关闭时不新增私有 state），保留 componentFilter 和实际 files 物化，不能把所有 nonproject helper 一律改为 raw。
7. `setupModTransformationPipeline` 的 `modRemapNamespace.isEmpty()` 早退只允许跳过原 RemapAction 注册，不得跳过 `setupFiles` 所组合的 Mixin preview 中 Mixin、选择器、游戏 Strip 的注册、请求与实际消费。关闭时 `C` 不请求本私有 state，恢复原属性请求，游戏与 extra 选择旧 AW Provider；本次 action 和各片 Mixin 均不被该消费链选择或依赖。
8. 适用范围内实际清理集合必须恰为 `S + {P_i}`，每个成员都按本视图规定次序应用全部独立 `N_i`。只有进入上述属性转换链的归档执行选择/旁路 action；非 `S` 原生目录不因此进入 Transform。不得引入其他 compilation/消费视图记录、汇总通知、清理解析专用依赖或用 raw `T_i` 代替 `P_i`。即使记录为空、重复条目已删除或某 source 不含对应配置，也必须消费顺序 action 的最终结果，不增加“有无 Mixin”预扫描。
9. 每个受支持的物化入口必须在读取记录或隔离相应 Transform 参数前，使本消费视图所有必要分片的生产完成。任务图内的入口可以沿有效游戏输入已有的生产关系带入各 operation；若该关系不足，必须由 compilation 的 `setupFiles` 组合所有者通过同一 consumer preparation，在受影响消费者任务的原注册处补充随最终开关生效的条件依赖，覆盖所有必要 Mixin Task。preparation 同时保留完整有效非项目 `C` 的原生 files/Transform producer 及有效游戏 producer，不能用必要 operation 或游戏关系替代外部文件依赖；artifact/component 投影只保留身份与分组，不负责发现 Task，不通过 `.files`、`resolvedArtifacts.get()`、游戏值或记录内容枚举来猜测 producer。关闭时仍保留完整外部 producer、旧 AW 与正常 remap；stub 游戏仍沿原 `runtime extra → primary` 次序。已有可靠生产关系的入口不必重复接边，不能靠不相关任务同场执行补足时序，也不能只用 `mustRunAfter` 代替调度。不新增汇总 Task；补边不要求 `C`/view 自身的 `buildDependencies` 新增 operation，不得重建原 Configuration 或改掉各记录输出 Provider。不得向 Mixin 自身或其 raw 上游反向添加此边；关闭时不得由该边或有效游戏选择引入本次 Mixin/Strip。条件必须读取同一 target 的最终 Property，支持既定后配置切换。

Cloche 不实现 JSON 清理，不删 Mixin 类、metadata、refmap 或整个配置文件。精确删除和无匹配语义由 Codev 保证。Strip 的用途是履行所选完整清理流程，不应声称 javac 会自动重放 JSON 配置从而必须防止二次应用。

## 消费出口

内部可以保留 `finalMinecraftFile` 为 AW/runtime 别名，并沿现有转换返回值区分原运行游戏、有序 `compileMinecraftFiles` 和原选片 sources；保留 `libraryArtifact` 的既有 AW/runtime 用途，编译使用独立编译集合。无需新增统一 operation-result 对象或重命名现有输出。

通知生产时序合同覆盖以下全部四类编译消费者的每个受支持物化入口，包括既有 Java/Kotlin 编译任务、sources/反编译、各参与 compilation 的 common stub、compile sync 及其 Tooling 模型解析；main、独立 client、data/test 和既有回退关系均不得遗漏。实现与后续 Plan 必须按实际入口记录由有效游戏输入携带生产关系、必要条件任务边或模型解析前生产步骤中的哪一种保证时序，并验证实际清理消费。此要求不新增任意第三方任务的自动支持承诺，也不缩减既定四消费者范围；单独接通一个观察任务或 compileJava 不能证明全部入口成立。

| 消费者 | 启用时的必须输入 |
| --- | --- |
| Java/Kotlin 等既有源集编译 | 同一有序 `compileMinecraftFiles`；以 `cleanSources` 精确替换 `S`；非 `S` 编译依赖及项目输出保持既有角色。Fabric CLIENT 保留两片及重复条目，不得经继承再引入 raw AW 副本或把 `R` 当作编译依赖补入。 |
| 反编译 / sources | 沿原选片规则，以对应 `cleanP_i` 为 `Decompile.inputFile`，另一 clean 片作 library classpath，并使用清理后的 compile 依赖及原有必要库。未分割 main 仍选 common，独立 client/includedClient 选 client；SERVER/joined 沿原单片。不要求一个 sources JAR 包含两片，也不能仍以 raw AW 为反编译输入；两片注入 API 的编译可见性和所选 sources 内容须分别验证。 |
| common API stub | 每个既有参与 compilation 的同一有序编译游戏集合、清理后的编译 artifact view 和 component 信息；任务依赖必须到达这些 Provider、全部必要分片 producer 及 Strip。 |
| IDE sync / IDE 编译模型 | 实际解析/物化同一编译游戏及清理依赖 view，保留 sync 对非项目外部依赖的既有约束；不是只依赖 producer 或注册 Transform。项目关系、scope 及实际消费须满足下节“Gradle 编译视图与 IDE 原生投影”，允许 IDE 原生项目排序。既有 runtime sync 工作仍读原始 runtime view；请求 sources 时接本规范反编译出口。 |
| run / runtimeClasspath / Forge `writeLegacyClasspath` | 原有 AW 游戏及分片、正常 remap 且未 StripMixins 的 runtime artifacts；不得消费 `P_i`、`cleanP_i` 或 `cleanSources`。 |
| 项目 remap/include/outgoing/publication | 项目自身 JAR、原有 includes、metadata 和 outgoing 链；不引入预应用游戏或依赖副本。若原发布 remap 的解析 classpath 从 `SourceSet.compileClasspath` 取值，必须显式保留其旧 AW + 原 compile 变体的解析视图，避免跟随新的编译消费集合切换；不得借机换成 runtime 变体。 |

common stub 对参与平台求 API 交集。未声明 `stubFrom` 时沿用既有参与者；声明时按[架构规格第 6 节](target-compilation-architecture.md)确定参与者。两种情况下，每个平台的 compilation 选择及回退不变：common main 读取对应 main；common.client 在分割 Fabric 读取独立 client，其余读取其有效 CLIENT main；data/test 使用现有对应 compilation 或父级回退。common 不另建自己的 Mixin operation，不为同一 target 再生成 SERVER/CLIENT 交集，不合并两侧结果。选中的代表环境不保证另一侧运行也有相同 API；compileOnly 提供的注入 API 也不保证在缺少该 mod 的运行环境存在。

已核实的消费修改面为 [TargetCompilation](../../src/main/kotlin/earth/terrarium/cloche/target/compilation/TargetCompilation.kt)、[CompilationInternal / sync](../../src/main/kotlin/earth/terrarium/cloche/target/compilation/CompilationInternal.kt)、[FabricCompilationImpl](../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricCompilationImpl.kt)、[FabricTargetImpl](../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricTargetImpl.kt)、[client data/test 归属](../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricClientSecondarySourceSets.kt)、[ForgeCompilationImpl](../../src/main/kotlin/earth/terrarium/cloche/target/forge/ForgeCompilationImpl.kt) 和 [CommonTargetCreator](../../src/main/kotlin/earth/terrarium/cloche/target/common/CommonTargetCreator.kt)。公共属性入口为 [MinecraftTarget](../../src/main/kotlin/earth/terrarium/cloche/api/target/MinecraftTarget.kt)。这些是当前实现定位，不是实现已经符合本合同的证据。

## Gradle 编译视图与 IDE 原生投影

Gradle 实际 compile view 必须保留原 `C` 的完整成员、artifact/component/所选 variant/classifier 关联、非 `S` 原文件、项目源码/输出角色及原 `C.buildDependencies`，消费确切 `cleanSources` 与本 compilation 的有序 `compileMinecraftFiles`。Gradle 与 IDE 消费均不要求原 `C` 的逐 artifact 序列或原位置，允许原生排序、分组与模型重叠；不得将严格保序作为隐藏验收门槛。该许可不放宽完整成员、身份、选择、闭包、scope、任务依赖及真实消费要求，也不授权把 classes 改成 archive、改变 outgoing 语义或重新选择依赖图。Fabric CLIENT 两片游戏的 client 优先、producer 实际有序输入跟踪、runtime 和发布原链仍按各自合同保持。

IDE 编译模型及其模块投影可以沿用 IDEA 原生项目分组、排序和重叠，不要求 raw Tooling 展开计数相同或与原 `C` 逐 artifact 同序。必须保留实际 build/project、configuration/所选 variant、SourceSet 及必要闭包的真实关联、项目源码/输出角色和 scope；不得用扁平文件库替代项目关系。外部编译文件仍必须使用确切 `cleanSources`、非 `S` 原文件及有序有效游戏分片，不得让 raw `S`、raw AW 游戏副本或 runtimeOnly 输入进入编译模型；runtime 视图仍走原链。sync 仍只物化既有非项目外部依赖及有效游戏，stub 保持非项目 artifact/component/producer 边界，不因模型项目关系而增加项目输出物化。

模型中的内部 archive 路径可以是项目/源集归属标识，不等于 IDE 实际读取该 JAR，也不授权替换 Gradle 编译文件。模型节点的 `classpathOrder`、组内 artifacts 及重复展开必须结合原选择、完整闭包、SourceSet index 与 scope 解释；raw 模型重复数不能单独证明最终重复根、符号遮蔽或无害。不得因允许原生排序就宣告既有候选通过。

本轮 sync 验收必须对照同输入的原 Configuration/游戏基线，通过真实 Tooling 模型保留完整节点、原依赖身份/选择及独立 SourceSet index（含未命中），核对 build/project/SourceSet 关系、必要闭包、scope、清理文件及有效游戏。应覆盖受影响的继承、多产物/多变体及 data/test 角色，区分原生 grouping/order/重叠与候选新增遗漏、scope 改变或文件库退化；不得先过滤额外成员再声称相等。Gradle 有效成员/关联及任务依赖、sync 实际物化、真实 Tooling 模型必须分别核对，不能仅以文件集合相等、节点计数或 resolver 返回成功代替上述检查；不要求逐 artifact 保序，相关编译、sources/反编译及 stub 实际消费仍按各自合同验证。

Tooling/同步必须区分任务图内物化与直接模型解析：前者按上述有效游戏生产关系或必要条件依赖保证所有必要分片先生产；后者不会因存在 `Task.dependsOn` 自动执行任务，必须在解析消费视图前，通过既有同步/准备流程完成该视图各份 operation 及其必要上游和游戏生产，再解析并实际物化清理 view。同请求 Tooling 的既有生产后建模方法可以继续适用，不要求另开请求，但必须核实实际先后关系。不得在配置阶段读取 `.files` 或隔离参数时假定尚未执行的生产已完成，也不得用手工预置 `N_i`、改读 raw/旧文件或只返回模型掩盖缺口。直接模型入口的生产步骤及与本次解析所用消费视图/有效输入的对应关系必须可验证；本合同不新增通用 Tooling/第三方任务调度框架，未有执行证据的入口须明确保留为验收缺口。

用户已取消本次私有真实 IDE 导入与六文件符号消费验证：不再要求建立新基线/candidate 副本及六文件见证、启动独立 IDE 实例、执行真实导入、最终模块/输出根或实际符号/PSI 消费观察，也不将这些步骤或实例收尾作为本轮必跑或隐含完成门槛，不另设替代 IDE 侧门槛。项目关系、闭包、scope 和有效清理视图的行为目标不变，但本轮可作出的声明限于 Gradle/真实 Tooling 模型及相关现有消费检查；Tooling 模型不等同最终 IDE 模块、根或符号行为，不能声称已获得等效动态证据。

最终 IDE 导入、模块合并/输出根、实际文件归属及符号解析、歧义或遮蔽行为明确未运行/未验证，其风险须随验收声明保留。已有 data/test 标准 archive/index 缺失属于原生基线限制，必须记录并与候选新增退化区分，不能假定最终 IDE 必然退化或自动恢复；不得通过修复 test、扩展 tooling/plugin 或改变 outgoing 语义满足本合同。取消验证不授权身份丢失、raw 依赖回流、scope 扩大或减少 Gradle 原 `C` 的完整成员和任务依赖。

## 失败、空输入与缓存

无 sources 或合法空记录必须沿用 Codev 正常行为；合法空记录仍产生单独清理副本，零份记录则失败。仍消费完整清理结果，不将“未发现通知”解释成所有 Mixins 已完整应用。未通知条目、引擎排除条目、正常可选失败及正常 fallback 均保持组件语义。Cloche 不解析日志决定成功，不增加独立注入语义审计。

实际抛出的任一分片 producer 异常、必要 source/派生游戏任一步 Strip 失败、解析和输入输出失败必须阻断受影响的编译、反编译、stub 或 sync 消费，保留原因及已知 compilation/分片/记录/输入定位。不得静默回退旧 AW、裸 `P_i`、仅完成部分记录的临时结果、缺失清理文件对应的空集合或旧缓存路径并声称成功。可以通过已定义的 opt-out 恢复旧视图，但不能自动修改开关来吞掉失败。

所有原始游戏、依赖、mapping 文件和项目产物必须保持不变。不同消费视图的状态 key、各分片记录、输出及专用游戏清理 type 不得串用；同视图清理按既定顺序使用本视图各份记录。runtime 与发布不因编译出口失败被改接到其他文件；具体任务若确实依赖失败的编译仍按 Gradle 真实依赖失败，不承诺所有发布命令能绕过编译失败。

必须保留每份 Codev Mixin 对 `T_i`、有序 `S`、完整有序解析 classpath、side、mapping/context、引擎实现和实际生效 native 属性的输入跟踪，以及独立 `P_i` / `N_i` 两份输出跟踪。这里跟踪实际有效顺序，不要求消费者恢复原 `C` 序列。每个清理 action 跟踪原 artifact，并按 Codev `@Classpath` 跟踪本视图记录文件的完整内容与集合顺序，不增加平行 order 输入；暂存输出不作为另一原 artifact 重新选 `S`。有序游戏集合必须保留分片生产关系与 client 优先。选择器的 `@InputArtifact` 与 `selectedSources` 必须使用 `PathSensitivity.ABSOLUTE`，同时跟踪输入内容、S 成员及完整位置；不得照搬原 Strip 的 `PathSensitivity.NONE` 而丢失决定选择分支的位置。编译 view、stub 和 sync 仍须保留 producer/Transform build dependencies。

选择器不得标记 `@CacheableTransform`，不能从本地或远程 build cache 加载/存储结果；单次委托 Codev 不赋予薄选择器缓存资格，也不能借 Codev `StripMixins` 注解承诺必要 `S` 由 build cache 恢复。此限制不关闭各片 `P_i/N_i` producer 及原 remap/include 的原有缓存能力，也不承诺这些单元总能命中。游戏直接注册的 Codev `StripMixins` 按其 `@CacheableTransform`、原 artifact 的 `PathSensitivity.NONE` 及记录集合的 `@Classpath` 内容/顺序跟踪适用，不能以旧单记录证据证明新接口缓存通过。对适用单元，正常 `UP-TO-DATE` 或 `FROM-CACHE` 可复用当前有效输入的已完成结果，无需重新运行引擎；缓存命中不是新的引擎应用证据。

选择器仍可在同一绝对位置、相同有效输入/参数与实现、完整一致的本地 Transform workspace 尚存时复用结果，包括有条件的跨进程复用；这独立于 build cache，不得称为 `FROM-CACHE` 或无条件恢复保证。输入或 S 的路径变化会影响复用；S 内容/成员、任一 `N_i` 内容、记录顺序与实现变化也参与失效。消费视图共用 `selectedSources` 与各份 `N_i`，因此一份 S/记录变化可能使多个已进入转换的 artifact 节点（包括非 `S` 普通库或 project JAR）重新进入 action，不能承诺只重做改变的那一份 `S`。这些非 `S` 归档仍承担参数准备/等待、指纹和 workspace 管理开销，但不执行 Strip 写出；这种等待不证明参数 producer 已进入任务图，生产时序由上述入口合同保证；原生保留、未进入转换的 classes/resources 不承担此 action 的开销。尚无性能量化证据。

新进程必须能恢复或重新生成各份 `P_i`、`N_i`，并执行或按上述条件正确复用全部必要顺序清理；workspace 不可用时不得静默使用旧结果。sources 内容/配置/成员/路径、各记录内容或次序、mapping、context、side、AW 或输出相关实现变化必须得到相应有效视图。无需另建 hash ledger、成功清单、跨组件事务、锁框架或自定义超时策略。

## 必须满足的验证条件

以下是后续实现的充分接线验收合同，**本次未执行**。其中预应用及完整清理保证均限于上文常规 JAR 原生属性适用范围；范围外依赖保留原图和既有角色，不以验收条件恢复任意归档支持。应复用现有 [TestKit 测试入口](../../src/test/kotlin/earth/terrarium/cloche/ClocheIntegrationTests.kt) 和小型真实引擎 fixture；不要求启动完整游戏，也不重做 Codev 的全引擎矩阵。可以在同一个 fixture 中覆盖多个条件，但不能用任务注册、mock transformer 或字符串配置断言替代关键的实际消费。

| ID | 所验证行为 | 最小充分观察 |
| --- | --- | --- |
| V1 | 默认启用及真实注入 API | 使用先 AW、再依赖真实 Mixin 增加方法/接口的目标；无开关时实际 Java/Kotlin 对应编译消费者可调用新增 API，原选片的清理输入经反编译可见对应新成员，另一 clean 片参与 library 解析，stub 消费完整有序编译视图。检查 `S` 和每个 `P_i` 均消费顺序 Strip 最终结果，各 `N_i` 独立产生，配置含已通知与未通知条目并保留类、配置文件、metadata、refmap 和唯一资源。 |
| V2 | sources 与身份/scope | 同一接线 fixture 包含 compileOnly、传递 mod、runtimeOnly、普通库、项目组件及本项目 Mixin；断言只有既有外部 mod compile 图进入 `S`，runtimeOnly 不产生预览 API，compileOnly 不进入 runtime。Gradle 实际 compile view 核对原 `C` 完整成员、原 artifact/component/variant/classifier 关联、非 `S` 原文件及原生产依赖，不要求原序列或原位置；IDE sync 通过真实 Tooling 模型按“Gradle 编译视图与 IDE 原生投影”核对同输入基线、完整模型身份/项目与源集角色、必要闭包、scope、清理成员及有效游戏，保留最终 IDE 模块/输出根和符号消费未验证的限制，不要求保序或 raw 展开计数相等。用适用属性链内的同组件不同 artifact/变体/classifier 或同名文件证明按完整位置选择确切 `S` 并通过原生链关联回原 compile 输入，非 `S` 归档即使含相同配置路径/条目也返回原 InputArtifact，未用 runtime 副本顶替。自身无该 state 的 project classes/resources 必须直接原生保留，包含 producer 为 `NO-SOURCE`、目录不存在但无本次 state 请求的原基线可解析的情形；不得触发选择器或创建空目录。必要 project JAR 保留原文件及生产依赖，不强造 archive，不要求转换后 ID 字面相等。混合扩展名 variant、非标准后缀或无后缀依赖不以此声称获得通用预应用及完整清理。不能靠 basename/GAV 或 Set 顺序配对，nested 不额外扫描。 |
| V3 | 有效环境、统一注册与 Fabric 分片 | 静态检查 `setupFiles` 是唯一原位组合点，按实际 compilation 恰好一次组合 AW、Mixin preview、consumer preparation 与 sources；窄机制只消费所需显式输入，无平台导航、Fabric import/强转、nullable 注册哨兵、回填 Property 或平台任务工厂，Info 保持纯输入。检查 Mixin/records/Strip/确切 S/C state 职责集中、preparation 保留完整有效非项目 C/游戏及条件 operation 的生产关系、FinalJarTasks 与明确保留的原 getters 投影同组结果，不另行注册；不要求恢复已取消兼容承诺的旧 JVM 入口。不得以旧总函数名存在或转发全量参数代替这些检查。接线观察须核对固定分片拓扑及 Provider 延迟的 side、primary 选片和开关。一个 CLIENT Mixin 修改 common 类、另一个修改 client 类；两者在同一 CLIENT 编译视图可见，Decompile 的原选片输入和另一 clean library 覆盖对应修改。观察 `setupFiles` 原位组合的 AW/Mixin/Strip/Decompile 注册真源与原注册先后：分割 main 一份 SERVER common，Fabric CLIENT 两份 Task 且都为 CLIENT，各自独立 `P_i/N_i`；Forge/NeoForge joined 一份 CLIENT。两片包含唯一类/资源及重复条目，验证各片条目均保留、编译游戏 `client → common` 次序、不 Join/预过滤、不额外引入 raw AW。核对未分割 main sources 选 common、独立 client/includedClient 选 client，另一 clean 片只作 library，不以单 sources JAR 覆盖两片为门槛。data/test 核对自身 `S`、AW、mapping/context 及 side 继承，不任意复用 main SERVER 结果；输出按 target/`featureName ?: main`/分片隔离。分类可用参数化接线断言，不要求每种形态重复真实引擎矩阵；允许重复文件不证明任意插件等价。 |
| V4 | common stub | 至少两个既有参与 target，只有共有的注入 API 留在 stub；common.client 使用正确有效视图；同 target 不增加另一侧 operation。一个关闭的参与 target 使用旧视图参与交集。保留 component 归属及实际生产依赖。 |
| V5 | runtime、发布和 opt-out | 核对本地 runtime 辅助 common 的 target AW 与 compilation preview/common AW 可区分；独立 client 及其 data/test 继续取 main 的 AW/runtime common，未分割未 includedClient main 不因预应用 CLIENT 增加 runtime common，本地 extra 仅原 distribution client 时惰性加入。检查 runtime/Forge legacy 文件集合仍指向原 AW 与 raw、未 Strip 依赖，项目 JAR、remap/include/outgoing 的文件输入仍属原链，发布解析使用 raw compile 变体与原 AW，原始输入未改。target 关闭后各 compilation、反编译、stub、sync 恢复旧视图，`C` 不请求本私有 state（包括 raw），与无本次 state 请求的原基线核对完整成员、所选 variant、SourceSet 文件、producer 及 NO-SOURCE 角色，且不执行或依赖本次选择器/Mixin/Strip；sync 的关闭视图同样通过真实 Tooling 模型对照原基线核对项目/源集角色、闭包、scope 及旧文件视图，保留最终 IDE 消费未验证的限制。Gradle 与 IDE 均不强制原 `C` 排序，但须保持完整成员、选择/身份、非 `S` 原文件及生产依赖。默认值、解析前 `true → false` / `false → true` 后配置及 target 间隔离均须验证；不得以 absent 属性 Provider 代替关闭时不安装 state 请求。上游非项目 raw view 不得承担项目输出选择证明，clean sync/stub view 启用时须继承 operation。 |
| V6 | mapping/refmap/context 与无 remap | 一个真实 mod remap 后仍由 refmap 选择原空间 member 的 fixture 经过实际注入后供编译调用，确认 source→target 方向和实际 context，并包含已 remap 的无 refmap selector，检查两种输入共用默认 tuple 的实际结果；另一个无 remap、无 refmap且名称兼容的 fixture 不设 tuple 仍预应用并实际 Strip。必要的显式 context 用现有 Task API 接线验证，并验证不存在的 context/namespace 的抛出错误阻断消费；不宣称互不兼容的多 context 被支持。 |
| V7 | 无环、顺序清理、失败与缓存 | 请求编译、反编译、stub 和 sync 能解析无环图；sync 物化与真实 Tooling 模型的身份/闭包/scope/有效文件须分别留有证据，最终 IDE 导入、模块/输出根及符号消费不属本轮必跑验收且明确未验证。未预先生成输入时，raw mapping/remap/include、S、AW 与各 `P_i/N_i` 的必要 producers 须由有效游戏生产关系或必要条件任务边按依赖先完成；直接模型解析先完成规定的生产流程，同请求 Tooling 也须实证先后关系。不得仅凭 Transform 参数 Provider 推断所有 Mixin 已被调度；文件 Provider 不被提前读取，remap/AW/角色/发布回读不请求清理状态，clean 非项目 sync/stub 实际物化。真实 Transform 在正常 remap/include 最终状态后按原 input 完整位置判定 `S` 一次，命中只调用完整 Codev `StripMixins` 一次，非 `S` 返回原文件；游戏直接注册 Codev action，均以 `.from(...)` 绑定 `N_common → N_client`（SERVER/joined 一份）。观察 Codev 后步读前步暂存、仅一个最终结果，无 Cloche 生命周期副本或逐步 outputs 登记。实际清理恰为 `S + {P_i}`；非 `S` 归档返回原文件，原生目录不进入 Transform，不再 remap/include，不产生合并通知或汇总 Task。覆盖零记录失败、合法空记录副本、跨记录重复删除、缺配置，以及实际解析/DOM/I/O 失败；distinct 记录路径保持独立，同一 File 仅首次处理，普通记录完整内容及顺序以 `@Classpath` 跟踪且无额外 order 输入。include 等未实际覆盖的路径须记录缺口。分别使任一分片 producer、source 和派生游戏的首步/后步清理失败，确认消费阻断、无临时/旧结果回退，撤故障后重新得到最终视图；核对包括后份记录在内的全部输入保护和 Codev close/move/清理故障传播的必要集成证据，组件生命周期穷举仍归 Codev。正常复跑和新进程核对各 `P_i/N_i` producer、remap/include 及实际可缓存游戏清理的恢复；选择器不承诺本地/远程 build cache，可重做或按同位置/相同输入与实现/workspace 尚存条件复用，后者不得记为 `FROM-CACHE`。变更 source 配置/成员/位置、任一记录内容或顺序、AW、side、mapping/context 时核对相应失效；允许共参使非 `S` 节点重进 action，仍不得执行其 Strip 或串用其他视图输出。raw AW/依赖只向上游供 Mixin、clean 域只向下游供 Decompile，无 clean extra 回边。真实引擎正常可选失败/日志不被升级为失败。 |

V7 的首次生产验收必须分别从私有新 Gradle Home、缺失各份 `P_i/N_i` 与相应清理输出且无可复用旧 Transform workspace 的状态，执行受支持的编译、反编译、stub 和 sync 路线；任务入口不得预跑任何分片 operation 充当接线，直接模型入口则从同样缺失状态验证规定的先生产再解析流程。须保留参数 Provider 来源、`C`/继承 view/SourceSet 文件集合依赖、实际任务图及生产先于参数读取/清理物化的执行证据，区分“集合不含 operation，但消费者图已带入”与依赖缺失。对受支持的可独立物化 `C`/继承 view/SourceSet 入口，必须在没有同场游戏消费代为调度的条件下验证；沿游戏生产关系成立的入口则核实其真实依赖，不强制加重复边。真实 Cloche 编译必须有源码并实际消费注入 API，不能以替身通知、任务注册或缓存命中替代；其余三类仍须分别证明实际清理文件消费。覆盖现有 file 与 Maven 输入路线并分别确认生产关系，不能按来源种类推断通知依赖可传递；关闭及后配置切换须核对图中不引入本次 Mixin/Strip，仍按 V5 恢复原视图。无需为本合同增加任意第三方消费者支持。

验证只证明所覆盖的 Cloche 参数供应、依赖关系和消费路线。精确清理的重复条目穷举、引擎服务隔离、引擎 Mixin selector 全矩阵、资源关闭/异常穷举等继续由 Codev owning 测试承担；Cloche 选择性 Transform 的 `S`/非 `S` 归档分支，以及 project classes/resources 原生选择、不进入 Transform 的行为由 V2/V7 验证。P4、编译器插件迁移、CI、旧缺陷修复、双侧合并及同 target 双侧交集不在本规范范围内。

## 源码依据及尚未建立的证据

本规范的初始实现观察以 Cloche `5613cd2b4ce781aa71aa3e8b4af231b2b3f13d42` 和 Codev `ce16b667c1a4aee49f789c8f7cc79491a64cba1b` 的只读检查为依据；当时 Cloche 共享 AW 编译/运行出口。该历史观察不描述当前实施状态，也不证明已符合本规范。[历史 Codev Spec][historical-codev-spec] 及源码链接固定到所检查 revision，不能作为本轮本地修正版的消费证据；当前多记录合同另由本地 owning Spec 链接指向。

[历史 Mixin Task][mixin-task]、[实际调用][apply-mixins]、[context 接线][context-recorder] 和 [旧 StripMixins][strip-mixins] 说明当时的单目标、side、mapping/context、两份输出及 source-independent 参数依据；不证明当前多记录接口已实现。当前多记录 API、指纹与安全生命周期以 [Codev owning Spec][codev-spec] 为准；旧 getter 已不能作为兼容依据。任意已发布 mod 的 remap/refmap 组合仍未获通用保证，实际输入限制与不兼容处理见上文。该 Codev 接口迁移当时未发现需要修改 Cloche PRD 或新增 Cloche ADR 的实质冲突；此历史判断不排除后来已接受的全局函数式组合决定。受影响 V1–V7 必须取得迁移后的证据。

本次修订沿用 [PRD](../prd/mixin-preapplication.md) 的产品边界，采用已确认的 compilation 统一转换与独立记录顺序清理设计。PRD 不固定 Fabric 的单文件布局；其 CLIENT common/client 修改覆盖、同次通知精确清理及完整消费要求，在本文落实为两片 `P_i`、各自 `N_i`、每个 `S/P_i` 按本视图全部记录顺序清理，以及有序编译集合与原选片 sources。反编译使用所选 clean 分片和另一 clean library，不要求一个 sources JAR 含两片。原 `C`、JAR 默认 raw、非项目 raw 上游、非 `S` 原文件、身份/scope、runtime/发布隔离和四消费者要求保持；未发现须越权修改 PRD 的实质冲突。

旧设计曾由 Cloche action 依序调用 `ExactMixinStripper`、管理暂存并只发布最终结果；该方法的历史静态依据及其当时适用证据保留，不再作为当前职责要求。原修订记载曾完整读取 Cloche 与 Codev owning Spec，并核对 `MixinPreapplication.kt` 当时的双路径 `StripCompilationMixins` 注册、records/order 与 raw `S` 选择，以及 `TargetCompilation.kt` 的 Fabric 强转、primary/other/runtime AW 和返回出口；这属于原修订观察，不证明当前迁移状态。本规范采用 Codev owning Spec 的多记录完整生命周期；本次环境来源修订未重读其生产实现或执行 Gradle，文档合同不证明 Codev 或 Cloche 新接口实现通过。原单目标/Join、单通知及完整目标反编译条款继续由分片合同替代；选择器无 build cache、条件性 workspace 复用、共参失效与非 `S` action 开销仍保留。

[历史 Cloche 接线][historical-compilation] 与 [JAR 默认状态][historical-jar-defaults] 固定到 `b79540d3c6ad336dae4917711be74e2071992261`，支持上述方法和有界范围；它不保证任意混合扩展名、自定义归档或冲突状态目录的完整处理。参考对 compile/runtime 同时请求 stripped 并使用 Mixin 游戏、扩大清理集合及旧 side 分类的行为不照搬，本规范仍以 PRD 的编译/运行隔离、精确 `S + {P_i}` 与有效环境为准。

历史生产候选 `implement-native-selective-strip / generation 1` 的内嵌结果记为 failed：当时全类型默认使 NO-SOURCE 项目目录进入 Transform，输出存在性校验回归未修，记录涉及五个候选生产文件及 fixture。本次保留该历史失败及旧 Spec 同步尝试的 blocked，不以新合同改写其结论，也不将它们当作当前实现未修的独立证明。历史局部映射编译及单 main 模型的正向证据不足以证明本次 V1–V7；分片注册、各记录首次生产与顺序清理、四消费者、关闭/失败恢复及新进程行为仍须针对重组后的实现验证。

历史修订记录说明，依赖本 Spec 的旧 Plan（`docs/works/mixin-preapplication/plan.md`）、旧 Waves（`docs/works/mixin-preapplication/waves.md`）及旧 Work/Context、派生复核曾未同步 JAR 默认状态、原生目录保留与有界保证，旧四份派生文档随后已删除；原记录的替代导航为重实现 [Work](../works/mixin-preapplication-reimplementation/work.md)、[Plan](../works/mixin-preapplication-reimplementation/plan.md) 与 [Waves](../works/mixin-preapplication-reimplementation/waves.md)，不是旧文档原件。本次未读取这些派生文件，不断言其当前内容或 readiness；本文对统一注册、分片/独立记录、全部入口生产时序、raw/clean 方向及 V2/V3/V5/V7 的要求不能由旧计划或参数 Provider 自动调度的假定代替。历史 MX4E5 六文件见证、独立 IDE/真实导入/最终模块根/PSI/实例收尾及相关 GE5、MX4R1/G2 安排按已取消验证的决定保留为历史，不得换名恢复门槛。本次不修改派生材料、不编写 Plan/Waves、不调度、不释放 gate，也不据文档作者自检判断实施 ready。当时提供的“开始实现吧”及 Spec 对齐授权支持该历史修订；不构成本轮整体组织调整的实施确认，本文不代替未就绪实施 Plan。

历史 MX4E4 仍是当时合同下的有界否定；已采用 enrichment 调查的 classes/archive 键不匹配及命中后复用项目产物组的限定结论仍是证据，不能因排序许可或取消验证改写为成功或据此选定实现。test candidate 未测及原 test 标准 archive/index 限制保留。六文件/准确 builder 导入配方的已采用独立审查 completed 仅说明准备审查结论，不是实验执行；尚未创建六文件、启动 IDE 或运行该实验，未证明参数实际送达、最终模块/符号或收尾，不存在需按该实验清理或撤回的活动实例。MX4E5 仍未执行，取消不记为成功或完成实验；既有编译、反编译及 common stub 完成事实不变。MX4 历史 partial、MX4R1 非 ready、GE5/G2 未满足保持原状，文档修订不自动建立生产 ready。

原记录所采用的 Gradle `9.5.1` 隔离实验结论为：整份 `C` 请求 raw 可将项目 `classes/compileJava` 选择改为 `JAR/jar`；关闭时不安装 state 请求，才与无 state 基线的完整成员、variant、SourceSet 文件、producer 及 NO-SOURCE 角色一致。排除项目的 raw view 自身不选择项目输出，clean 非项目消费者应继承 operation。公开 `withDependencies` 中按最终开关条件注册 `attributeProvider` 已覆盖 `true → false` 和 `false → true`；这是可用的有界实现证据，不固定为唯一 hook。无条件 absent Provider 会报 `must always be present`，不得用作关闭实现。这些结论未由本次文档修订重新运行，也不证明完整 Cloche 接线通过。

已采用的[通知生产依赖诊断报告](../../build/tmp/notification-wiring-diagnosis-20261103/REPORT.md)将该首次生产缺口定位到 Gradle `9.5.1` 所测任务依赖发现边界：参数 Provider 保留通知 producer 来源，但单独消费 `C` 时 file 与 Maven 均未自动调度该任务；旧成功来自同场 game 输入的独立生产关系，不能归因为 file 输入比 Maven 更会传播 Transform 参数依赖。报告中的条件 `dependsOn(operationTask)` 使 Maven 的 `C`、继承 view、SourceSet 及有真实 Java 源的原 `compileJava` 在新 Home 首次生产成功，关闭时不调度；修正后 `C`/view/SourceSet 自身 `buildDependencies` 仍不含 operation。通知参数继续来自同一 operation 输出 Provider。以上保留为历史诊断的有界观察：原修订记载曾全文读取报告；本次未重读报告、重跑脚本或独立复核底层日志，不将该历史阅读写作本次验证。

该原型使用通知与 remap 替身，不能证明真实 Mixin、真实 remap/mapping/AW、全部 Cloche 消费者、直接 Tooling/IDE 解析、configuration cache、classifier/多变体或其他 Gradle 版本已通过；报告中四种消费形态不等于产品的四类消费者。已定位原因与最小公开接法不等于真实 Cloche 接线已修复；后续从 HEAD 重实现必须按上述入口生产合同与 V7 验收，不能继续假定 Transform 参数自动调度通知任务，也不能以本原型成功宣布生产链或功能验收通过。

已提供的当前实施摘要说明，产品实现及限定本地验收曾在 Windows 全局 Gradle `9.5.1` / JDK 17 和获准的本地 Codev 修正版下完成；本次未独立复核其日志。该成功只适用于原实现及当时所测输入，不能证明本次统一注册、两片输出和独立记录顺序清理的重组通过，也不覆盖正式发布来源或最终 IDEA 消费。本次不把历史成功记作重组后的 V1–V7 成功，亦不将历史失败或未执行实验改写为成功。

前次环境来源修订仅修改本 Spec 并作文本自检；未实施接线，未执行 Gradle、新实验、IDE 导入、构建、测试或发布。本地未发布 Codev、原记录中的 splitClientJar duplicate metadata 及正式发布依赖接入未完成的限制保留为其原适用边界，不能凭允许分片重复文件就宣告旧故障已动态排除。独立 test 生命周期/capability/remap helper 修复仍不在本次范围；旧“fixture 环境整理不属于本次结构收敛”的排除，现由用户最新的移除两项属性及沿用实际运行环境要求定点替代，具体合同见“测试运行环境来源”。该次授权仅覆盖本文环境来源条款修订，不表示已改实现、正式依赖、Codev 或环境；不提交/推送/发布，不恢复最终 IDEA 导入。上文允许未来实现中的必要私有编译来源安排，不表示本次已构建或替换任何二进制。

已读[结构调整 Context](../works/mixin-compilation-transformations/context.md)所保存的采用事实：CT1、CT3、CT4 为各自原范围下的 completed，CT2 原 Result 仍为 partial。此处只保留已记录事实，不重新采用原 Result、独立复核报告或判断 gate；旧成功仅在对象、接口、输入与消费者仍适用时可复用，不证明本轮 Codev 多记录委托和通用输入迁移已经通过。原失败已产生 P/N、AW 和部分输出，后续对照曾改写 observation；旧不可重放清理及未知差额仍保留，未知不等于无效果，不由本次文本修订恢复、删除或倒填成功。

前次环境来源修订的作者自检记载，曾完整读取本 Spec 与 generated TestKit 定位诊断报告，沿用 Handoff 已采用的环境继承事实及 authority assessment；未重读源码、旧调查原 Result 或独立重验动态结论。实际 delta 限于测试工具库的定位合同、internal API 限制及直接相关的证据措辞：保留 TestKit code source 观察，沿默认 Runner 同类空间 locator/beacon 定位安装，再核对 helper Connector 来源绑定。Cloche PRD reuse、Cloche ADR skip、本 Spec update，Codev 合同不改；移除两项属性及覆盖链、默认 TestKit/worker 来源、独立 helper 来源绑定和插件 metadata 编译路径分别表述，历史显式环境证据保留原适用范围。精确原位置选择、原 artifact/variant/classifier 与 Configuration 关系、两记录次序/指纹、输入保护及失败、raw→clean 方向、runtime 来源与 side/primary/开关懒值，以及 Java/Kotlin、sources、stub、sync 的必要 producer 和直接模型先生产后解析义务均保留。该次仅核对新增诊断报告链接的本地目标可读，未重验其底层证据链接或其余历史链接；固定 revision 外链只保留历史含义，未作联网或动态验证。没有由该次自检建立实施 readiness、接口/环境迁移后的开发功能验收或最终 IDE 符号结论。

本次组织协调的作者自检完整读取本 Spec、全局 owning Spec、函数式组合 ADR 与 Mixin PRD，并按受影响接口/依赖图/消费合同读取 Draft 提案。实际修改限于 owner 衔接、`setupFiles` 唯一原位组合、同一 Mixin 一致性边界、消费准备、兼容投影、图首部及 V3 静态观察，并定点限定此前“无需 ADR”和环境修订授权/自检的历史范围。全文检索旧总入口及同义命令，旧函数名只保留在取消其内部全量总入口硬要求的说明中；职责、图与 V3 已同向，不再要求第二条装配路径，全局 Spec 第 1 节限定的具名入口协调已在本 owner 落地。逐节文本对比确认原来源/环境/身份、分片/records、mapping、消费、失败/缓存正文及 V1/V2/V4–V7 不变，V3 的分片动态验收与 runtime AW 来源段亦保留；V3 静态要求改为核对唯一注册、职责集中、输入权限与同结果投影，未删除必要 assurance 后宣称通过。已机械核对本地链接目标可读及 Markdown 表格/围栏，固定 revision 外链未联网重验。本次唯一写入是本文，未改全局 Spec、ADR、提案或派生计划，未构建、测试、试验、下载、安装或执行生产接线；本自检不构成独立评审、实施 ready、生产动态等价或 V1–V7 新通过，既有开发/正式来源与最终 IDE 未验证限制不变。

[historical-jar-defaults]: https://github.com/SettingDust/cloche/blob/b79540d3c6ad336dae4917711be74e2071992261/src/main/kotlin/earth/terrarium/cloche/ProjectPluginApplicator.kt#L53-L60
[gradle94-transform]: https://raw.githubusercontent.com/gradle/gradle/v9.4.0/platforms/software/dependency-management/src/main/java/org/gradle/api/internal/artifacts/transform/DefaultTransform.java
[gradle98-transform]: https://raw.githubusercontent.com/gradle/gradle/v9.8.2/platforms/software/dependency-management/src/main/java/org/gradle/api/internal/artifacts/transform/DefaultTransform.java
[gradle94-workspace]: https://raw.githubusercontent.com/gradle/gradle/v9.4.0/platforms/core-execution/execution/src/main/java/org/gradle/internal/execution/steps/AssignImmutableWorkspaceStep.java
[historical-compilation]: https://github.com/SettingDust/cloche/blob/b79540d3c6ad336dae4917711be74e2071992261/src/main/kotlin/earth/terrarium/cloche/target/TargetCompilation.kt
[historical-codev-spec]: https://github.com/SettingDust/minecraft-codev/blob/ce16b667c1a4aee49f789c8f7cc79491a64cba1b/docs/spec/mixin-preapplication.md
[codev-spec]: ../../../minecraft-codev/docs/spec/mixin-preapplication.md
[codev-records]: ../../../minecraft-codev/docs/spec/mixin-preapplication.md#ordered-record-input-and-migration
[codev-lifecycle]: ../../../minecraft-codev/docs/spec/mixin-preapplication.md#build-copy-completion-and-consumption
[codev-build]: https://github.com/SettingDust/minecraft-codev/blob/ce16b667c1a4aee49f789c8f7cc79491a64cba1b/minecraft-codev-mixins/build.gradle.kts
[mixin-task]: https://github.com/SettingDust/minecraft-codev/blob/ce16b667c1a4aee49f789c8f7cc79491a64cba1b/minecraft-codev-mixins/src/main/kotlin/net/msrandom/minecraftcodev/mixins/task/Mixin.kt
[strip-mixins]: https://github.com/SettingDust/minecraft-codev/blob/ce16b667c1a4aee49f789c8f7cc79491a64cba1b/minecraft-codev-mixins/src/main/kotlin/net/msrandom/minecraftcodev/mixins/StripMixins.kt
[jar-remapper]: https://github.com/SettingDust/minecraft-codev/blob/ce16b667c1a4aee49f789c8f7cc79491a64cba1b/minecraft-codev-remapper/src/main/kotlin/net/msrandom/minecraftcodev/remapper/JarRemapper.kt
[apply-mixins]: https://github.com/SettingDust/minecraft-codev/blob/ce16b667c1a4aee49f789c8f7cc79491a64cba1b/minecraft-codev-mixins/src/main/kotlin/net/msrandom/minecraftcodev/mixins/runtime/applyMixins.kt
[context-recorder]: https://github.com/SettingDust/minecraft-codev/blob/ce16b667c1a4aee49f789c8f7cc79491a64cba1b/minecraft-codev-mixins/src/main/kotlin/net/msrandom/minecraftcodev/mixins/runtime/GradleMixinRecorderExtension.kt
