# Target / Compilation 全局函数式解耦

[Context](context.md) · [Plan](plan.md) · [唯一执行图](waves.md)

目标是在保留公开 DSL、原 Gradle 对象身份、构造/监听时点和开发/运行/发布语义的条件下，将平台供料、家族装配、依赖选择、转换组合及消费协议置于显式函数边界；内部游戏以已接受的泛型纯数据 `Joined(value)` / `Split(common, client)` 贯穿 named、AW、Strip 及任务角色投影。完成标准既包含动态等价，也包含 sources、sync、平台输出等代表变化不再穿透无关层；只拆转换总函数不能完成本交付。形状不证明字节阶段或 producer 已执行。

本目录是新建的初始准备 tuple：`work.md`、`context.md`、`plan.md`、`waves.md`，只选择此一个未切片 Plan/Waves 对。没有沿用历史 Tasks 或任何旧完成状态。

当前状态：已接受函数式方案及 Joined/Split 模型，现实现仍以 Map/String 传片。本次在同一未执行初始 tuple 内修订形状迁移、中间兼容与验查责任并完成作者自检。已采用的旧独立评审仅对未变部分继续适用：其 G-STRUCTURE 前证据责任阻断已由前次修订解决；本轮 Joined/Split 迁移 delta 及必要连接已完成独立静态复核，无 blocking 或需先纠正的 nonblocking finding；结合旧 review 未变范围，Router 已判断计划 ready。所有未来 item 未执行，所有 gate 未判定通过；架构接受、作者自检和本文均不是实施许可。本次文档生成授权覆盖这份初始图的必要修订，不授予以后无限图维护权限。该 readiness 判断不构成具体实施 scope 的 C4 确认；重大范围变化保留 C6，图维护须有适用授权。

## Authority

- [全局 ADR](../../adr/target-compilation-functional-composition.md)：函数显式输入、Joined/Split 泛型纯数据、原状态对象、唯一原位组合、兼容及重访边界。
- [全局 Spec](../../spec/target-compilation-architecture.md)：固定拓扑/惰性角色与阶段投影、旧 Map/String 兼容及 G1–G8。
- [Mixin Spec](../../spec/mixin-preapplication.md)：来源/身份、分片、清理、消费、失败及 V1–V7；当前统一职责、图及模型许可均与全局 Spec 衔接。
- [Mixin PRD](../../prd/mixin-preapplication.md)：限定产品保证，不作为全 Cloche PRD。
- [领域 Context](../../../CONTEXT.md) 与 [Accepted 设计提案](../../proposals/target-compilation-architecture.md)：术语与非权威解释；模型已选，具体标识符/签名仍可调整。

Plan outcome 的完成信号为 `G-STRUCTURE` 与 `G-ACCEPT` 均有适用证据、经 Router 实际判断满足。当前不作该判断。允许的最终结论限于指定本地未发布 Codev 与实际 Gradle/JDK 下的组织及开发消费等价；正式依赖发布、完整 ABI/最低版本矩阵、configuration cache、性能、最终 IDE 导入/根/PSI 均不因此得到证明。
