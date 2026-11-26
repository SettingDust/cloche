# 具体游戏 target 的 gameJars 实施计划

依据 [已确认规格](../../spec/target-minecraft-artifacts.md)。交付入口为使用 Cloche 的 Gradle Kotlin 构建脚本。现有脚本通过 target 配置开发 run；新增入口让脚本从同一 target 取出游戏 JAR，按需把绝对路径传给选定的 run。

## 实现顺序

1. 记录候选基线、Gradle、JDK、Codev 版本和可复用的 Forge 1.20.1 测试环境。在相同 target 配置下记录普通开发 run 的 JVM 参数和 classpath，供改后对照。
2. 在具体游戏 target 的公开类型添加只读 `gameJars`。检查已有 `GameShards.Split` 和 `GameShards.Joined` 的可见性及属性名，可满足 DSL 契约时复用。让具体类型限定可用分片，每项使用 `Provider<RegularFile>`。共享源码 Common 通过参与的具体 target 访问产物。
3. 沿各 target 已有任务输出接线。Fabric 的 `common` 和 `client` 分别跟随 `FabricTargetImpl` 中对应的 intermediary 映射输出，跳过映射时分别接直接解析输出。Forge、NeoForge 的 `joined` 取各自 target 的 `ResolvePatchedMinecraft` 输出。保留 Provider 上的生产任务依赖，配置阶段只传句柄。编译公开 API，逐项记录属性、版本分支、生产任务和输出的对应关系。

## Forge 1.20.1 动态验收

使用真实 Kotlin DSL fixture，固定 Forge 1.20.1 和加载器版本。以下两个入口各用独立的缺失输出状态，记录执行前文件状态、Gradle 任务结果及消费端实际打开的路径。

1. 独立任务将 `forge.gameJars.joined` 声明为输入文件，任务关系完全由此输入提供。只请求该消费任务，观察生产任务先恢复文件，随后消费任务打开该 JAR。
2. 在一个开发 run 的 JVM 参数中按需传入 `connector.clean.path`。仅请求此 run，在实际启动的 JVM 内读取系统属性并打开文件。用该 target 的 patched 生产任务输出作为独立路径基准，核对文件身份，并检查已从所用版本映射和加载器核实的 SRG 游戏成员及 Forge 类。运行普通开发 run，与基线对照 JVM 参数和 classpath，并检查选定 run 的新增属性仅在该 run 生效。

## 分工与交回

一名实现者负责 target 公开 API、现有生产任务接线、fixture 及实测。独立审查者从候选版本核对公开入口、Fabric 两条版本分支及 NeoForge 输出归属，复核两条 Forge 冷消费路径和普通 run 对照。交回候选差异、复现命令、平台属性对应表、任务日志和 JVM 检查结果。若本机条件阻止实际启动 JVM，记录具体失败和未验证项。计划确认并授权实施后开始修改实现。
