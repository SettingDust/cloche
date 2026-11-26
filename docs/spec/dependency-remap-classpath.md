# 为依赖 remap 补充 Forge 或 NeoForge 类路径

## 调用者与效果

在 Fabric target 的 `dependencies` 中声明 `remapClasspath(forgeMinecraft("1.20.1", "47.4.4"))`。NeoForge 对应 `remapClasspath(neoforgeMinecraft("1.21.1", "21.1.80"))`。版本号为示例。target.dependencies 委托给该 target 的 main，显式在 main 的 dependencies 中声明具有相同效果。该 target 下所有执行正常依赖 remap 的 compilation 共享补充文件。另一个同版本、同 loader、同 side 的 target 按自己的声明查类。
公共普通依赖 handler 接口提供普通依赖 DSL，所属 target 的 main 使用扩展该接口的专用子接口，额外提供 remapClasspath。Kotlin DSL 在非 main 接收者上直接调用此方法时，构建脚本编译失败，诊断定位调用点。合法入口为 target.dependencies 与显式 main.dependencies。运行 help 可验证脚本编译及补充文件的惰性。
现有公开抽象类 ClocheDependencyHandler 继续承接已编译的普通调用。上述普通 handler 接口可采用独立名称，main 专用接口扩展它。

这些文件用于读取类、父类和接口，不改变源命名空间、目标命名空间或映射表，不替换原有游戏与 unmapped compile/runtime 查类输入。声明本身不向编译、运行、打包或发布依赖中加入 Forge/NeoForge，也不保证重映射后的模组能在 Fabric 上运行。不为无正常 remap 的 compilation 强制开启 remap。
补充文件用于查找原输入缺少的类，不得遮蔽原查类输入中的同名类。查找所需父类、接口及成员名称须与本次源命名空间可匹配；不承诺自动转换整个补充 JAR。运行依赖仍可消费正常 remap 的模组输出，隔离的是补充文件本身，而非该输出的字节码变化。

## 产物准备与兼容

帮助函数与原有同加载器 target 共用坐标、开发包、补丁及所需配套文件的准备逻辑。向 remapper 提供惰性文件集合并保留生产任务依赖；只配置工程或运行 `help` 不得因该声明解析、下载或生产额外游戏。消费时请求的加载器或版本无法解析，必须报告失败，不以另一加载器或版本回退。独立请求不依赖 Forge/NeoForge target 装配补齐 patches 或 libraries。

原有 target 的依赖角色、任务名、输出和惰性版本选择保持。整个 target 没有声明本入口时，各 compilation 使用原 remap 输入。Fabric 游戏准备继续使用原流程。
生产逻辑的共用限于 Forge 和 NeoForge。各 target 可独立注册相同请求的生产任务。

## 验收

本功能新增一个端到端 Gradle 集成测试入口。一个测试方法驱动 Forge、NeoForge 两个隔离场景，各走公开 DSL 和真实依赖 transform，分别断言、按加载器报告结果；不把两套游戏同时加进同一 compilation。复用已有兼容检查，不要求为每条验收断言各建一个测试方法。
- 两个场景均只声明 Fabric target；选取必须借助额外父类或接口才能映射成员的样例。对照未声明和声明后的结果，检查输出字节码的方法引用或覆写名称。首次消费不得预跑补充文件的生产任务或命中已有 transform 结果；须由真实消费触发所需生产，不能只看任务成功或文件存在。
- 一个加载器场景通过 target.dependencies 声明，另一个通过显式 main.dependencies 声明。以同一外部模组验证两个场景中 main 与 test 均得到预期成员名。在 test.dependencies 编写完整调用并运行 help，核对 Kotlin 脚本编译诊断指向 remapClasspath，补充产物解析和生产次数均为零。
- 在同一工程使用两个同版本、同 loader、同 side 的 Fabric target。先验证未声明的 target 得到基线成员名，再让两者各自声明可区分的补充输入，检查同一模组的实际 remap 结果分别匹配各自输入。
- 核对普通 compile/runtime、打包与发布依赖。单跑 `help` 时补充文件保持惰性。
- 以原有 Forge、NeoForge target 的正常构建作兼容对照，并检查两入口使用同一准备逻辑。无效版本在实际消费时失败；不得将配置成功当成产物有效。
