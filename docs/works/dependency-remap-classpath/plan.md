# 依赖 remap 查类路径实施计划

依据[已确认的行为规格](../../spec/dependency-remap-classpath.md)，在已有 Forge/NeoForge 帮助函数和工作树集成测试上调整声明范围。交付包括 Cloche 实现、一个真实 Gradle 集成测试方法、运行记录和独立复核。

## 入口与迁移

target.dependencies 与显式 main.dependencies 都通过 main 专用 handler 向所属 target 添加输入，包括 test 在内的所有正常 remap 消费该声明。此前写在 test 等其他 compilation 中的调用移到 target.dependencies，Kotlin 脚本编译器在错误接收者的调用处报告诊断。

## 实现和验证

1. 同一执行者从 ClocheDependencyHandler 的公开调用入口和 TargetCompilation 的 normal RemapAction 入手。用所属 target 的真实 main 身份判断声明位置，采用惰性文件集合保留 producer。检查嵌套帮助函数的参数求值及 registration 时机。target 内复用补充集合，各 compilation 继续提供自己的基本查类输入。根据同属性双 target 的 transform 选择决定是否需要 target 级属性，消除以源集名为选择条件的逻辑。
2. 修改现有单个 TestKit 方法。在独立冷工程和 Gradle Home 中，一个加载器场景使用 target.dependencies，另一个使用显式 main.dependencies。分别读取同一外部模组的输出字节码，观察两场景 main 与 test 均从基线 getPos 变为 patchedPosition，原同名类仍选 ordinaryPosition。记录首次真实消费触发补丁生产及依赖 remap 的日志。允许预置原始下载，两个场景使用新补丁和 remap 输出。
   两个冷场景分别只由 main、只由 test 发起首次消费，每次先核实该入口独立调度补丁生产和 remap，再消费另一 compilation。
3. 在同一工程并列两个同 loader、版本、side 的 Fabric target。先检查 A 有声明、B 无声明时 B 的基线，再给 B 独立且可区分的补充输入，检查 A、B 对同一模组各自产出与自身输入对应的成员名。使用不同 target 名定位产物，检查 transform 实际选择和普通 compile/runtime、JAR、发布元数据中的成员。
4. 将公开 handler 分为普通接口与 main 子接口，分别验证普通依赖与 main 专有方法的静态接收类型。在该 TestKit 方法内以 test.dependencies 编写完整嵌套调用并执行 help，核对 Kotlin 脚本编译错误的位置与补充文件零解析和零生产。合法声明下的 help 应成功，错误版本在实际消费时报对应坐标。
   用变更前 API 编译一个向 `dependencies(Action)` 传递普通 `Action<ClocheDependencyHandler>` 的消费者，将其原字节码在最终插件上执行并检查声明生效。
5. 核对共享坐标、补丁及 libraries 准备和原 target 入口兼容。沿用已有最终候选中可证明等价的证据，受影响部分在新候选上运行。记录 Gradle、JDK、固定本地 Codev 版本、命令、结果与候选指纹。独立审查者从公开 DSL 核对最终候选和同一测试方法的两个加载器结果，区分原有 NeoForge 默认 Mixin 故障与本次行为。

工作范围限于本地 Cloche 的第三项。交付位置为工作树和本地验证目录。
