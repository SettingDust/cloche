# Target / Compilation 全局函数式解耦

[Context](context.md) · [Plan](plan.md) · [唯一执行图](waves.md)

目标是在保留公开 DSL、原 Gradle 对象身份、构造/监听时点和开发/运行/发布语义的条件下，将平台供料、家族装配、依赖选择、转换组合及消费协议置于显式函数边界；内部游戏以已接受的泛型纯数据 `Joined(value)` / `Split(common, client)` 贯穿 named、AW、Strip 及任务角色投影。完成标准既包含动态等价，也包含 sources、sync、平台输出等代表变化不再穿透无关层；只拆转换总函数不能完成本交付。形状不证明字节阶段或 producer 已执行。

本目录沿用同一未切片 Work/Context/Plan/Waves tuple，没有另建执行图或迁移历史 Tasks。

历史基线（指定旧候选）：十项实现的完整 Results 已由 Router 采用；I-SYNC 自身八测试通过，后续作者适用性汇合已完成；I-V67-HARNESS 的 completed Result 已由 Router 采用，闭合最终候选 V6/V7 的剩余作者证据。新的独立 I-ACCEPT Result 也已由 Router 采用，并已判定 `G-STRUCTURE` 与 `G-ACCEPT` 满足。该结论仅是指定本地边界内的 bounded acceptance；旧 I-SYNC blocked/恢复、I-ACCEPT blocked、V6/V7 partial/failed 及其它历史事实不改写。证据入口为 [V67 报告](../../../build/tmp/v67-recovery-20261108-d/REPORT.md)、[V67 observations](../../../build/tmp/v67-recovery-20261108-d/checked-observations.json)、[I-SYNC model checks](../../../build/tmp/i-sync-evidence-recovery-b89893880337/model-checks/checks.json) 与 [独立复核 evidence](../../../build/tmp/i-accept-review-85b722068e/model-comparison/checks.json)。初始准备时的源码定位只描述历史迁移依据，不代表现候选仍未实现。

当前增量：已确认的兼容清理已经发生，但 `cleanup-internal-compatibility/g1` 的 terminal Result 为 partial；指定适配/专用 Legacy caller 已移除，真实 typed 调用及混合断言已迁移，尚缺最终证据结算，不能写成“尚未执行”或恢复旧 Handler。正常测试集中路线已有可靠采用的调查及 authority reuse assessment；本次仅维护这四份派生文档，安排安全恢复、保持覆盖的正常测试集中及最终独立修正审查三个分别可观察的结果，不执行它们。具体已知/未知效果与恢复入口见 [Context](context.md)。

四文档作者准备/自检已完成；Router 已采用后续独立 review-delivery-plan 的 completed、无阻断结论，用户在完整 scope 披露后明确确认测试集中实施与定向验证。首次复核因作者回执截断而 blocked 的历史不改写，后续新独立复核完整消费作者事实后通过。原兼容清理确认及同 tuple 的 C5 图维护授权持续有效；本次仅同步记录，不授予新权限，也不证明三个增量已执行或 gates 满足。C4/C5/C6、当前字节/JIT、实际前置/冲突及 gate 判断仍归 Router。

## Authority

- [全局 ADR](../../adr/target-compilation-functional-composition.md)：函数显式输入、Joined/Split 泛型纯数据、原状态对象、唯一原位组合、兼容及重访边界。
- [全局 Spec](../../spec/target-compilation-architecture.md)：固定拓扑/惰性角色与阶段投影、第 8 节指定内部入口取消支持与明确保留出口、G1–G8。
- [Mixin Spec](../../spec/mixin-preapplication.md)：来源/身份、分片、清理、消费、失败及 V1–V7；当前统一职责、图及模型许可均与全局 Spec 衔接。
- [Mixin PRD](../../prd/mixin-preapplication.md)：限定产品保证，不作为全 Cloche PRD。
- [领域 Context](../../../CONTEXT.md) 与 [Accepted 设计提案](../../proposals/target-compilation-architecture.md)：术语与非权威解释；模型已选，具体标识符/签名仍可调整。

原交付的 `G-STRUCTURE` / `G-ACCEPT` 保持旧候选的历史 bounded acceptance。旧 `I-COMPAT-CLEANUP` 保持 partial，原 `G-CLEANUP` 未释放且退为历史，不成为永远无法满足的新 gate 成员。当前增量以新 `G-CLEANUP-RECOVERY` 承接必要清理谓词，随后 `G-TEST-CONSOLIDATION` 证明正常组保持覆盖且实际减少重复准备/调用，最终 `G-CORRECTION-REVIEW` 核实同一最终候选；三者均未判定。允许的最终结论仍限于指定本地未发布 Codev、Windows、Gradle 9.5.1、JDK 17、offline 且关闭 configuration cache 下的组织及开发消费等价。测试集中须报告实际调用次数和耗时方法，未取得旧逐方法基准不得宣称提速百分比；正式依赖发布、完整 ABI/最低版本矩阵、configuration cache、远程缓存、最终 IDE 及完整平台/版本矩阵仍未证明。
