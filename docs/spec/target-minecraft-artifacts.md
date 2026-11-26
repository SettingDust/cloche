# 各 Minecraft target 的中间游戏产物

每个具体 Minecraft 游戏 target 通过只读属性 `gameJars` 暴露映射到开发命名空间之前使用的游戏文件。Kotlin DSL 根据 target 的具体类型提供对应的属性。每个文件以带生产任务依赖的只读 `Provider<RegularFile>` 提供，取自当前 target 配置的 Minecraft 版本、加载器版本和映射。调用者在配置阶段取得句柄，在消费阶段取得实际路径。

Fabric 按 `common` 和 `client` 分别提供游戏分片。有 intermediary 映射时取各自的 intermediary 输出，跳过映射的版本取直接解析的对应分片。Forge 和 NeoForge 按 `joined` 提供各自的 patched 游戏 JAR，使用该版本实际的命名空间。Forge 1.20.1 的 `joined` 包含 Forge 类和补丁，游戏成员使用 SRG 命名。共享源码的 Common target 通过其参与的具体游戏 target 取得相应产物。

## 动态验收场景

在 Forge 1.20.1 的开发 run 中，将 `forge.gameJars.joined` 对应的绝对路径作为系统属性 `connector.clean.path` 传给 JVM。从目标 JAR 缺失的状态直接执行该 run，在启动的 JVM 中读取属性并打开 JAR，检查它指向当前 target 的 patched 输出，且包含代表性的 SRG 游戏成员和 Forge 类。以普通 run 的 JVM 参数及 classpath 作对照。

在同一 Forge 场景用独立 Gradle 任务将该句柄声明为文件输入，仅通过该输入依赖生产任务。从缺少目标文件的状态直接执行消费任务，应先生成并成功读取 JAR。其他平台按接口及现有任务输出核对其产物角色和归属。动态验收范围为上述 Forge 1.20.1 场景。
