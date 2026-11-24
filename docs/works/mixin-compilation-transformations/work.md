# Mixin compilation 统一转换与分片记录

依据 [PRD](../../prd/mixin-preapplication.md) 与当前 [Spec](../../spec/mixin-preapplication.md)，将既有编译预应用收敛到 compilation 的统一转换入口：Fabric CLIENT 保留 common/client 两片及独立应用记录，消费者使用顺序清理后的有效视图。

本 Work 是结构调整交付；[既有本地交付](../mixin-preapplication-reimplementation/work.md)已完成，不重开、重放或改写其历史结果。Router 已完整读取并可靠关联 CT1、CT3、CT4 的 completed 与 CT2 的 partial，已判断 G1、G3、G4 满足：G1 当时仅释放原 CT2，G3 仅释放 CT4，G4 建立修后独立限定验收。原 CT2 及 G2 保持 partial / 未满足，不能倒填成功。

[CT3 报告](../../../build/reports/mixin-compilation-transformations/ct3/REPORT.md)记录精确 artifact 角色修复及作者回归；[CT4 报告](../../../build/reports/mixin-compilation-transformations/ct4/REPORT.md)记录不同于 CT1/CT3 作者的独立 V1–V7 验收、新跑与适用复用及限制。恢复图作者自检及独立受影响边界复核已完成，无准备度阻断；用户“可以”的持续同范围授权不变。本次仅同步四份完成记录并作作者自检，不重新实施、验收或判断 gate。

- [Context](context.md)：采用事实、修复与独立验收结论、证据适用边界。
- [Plan](plan.md)：已确认范围、职责、风险及验证安排。
- [Waves](waves.md)：与该 Plan 匹配的唯一执行图及历史恢复连接。

当前交付完成信号为 Plan 的 G1、G3、G4 分别有充分证据并经 Router 判断满足；Router 已判断上述完成信号满足，本 Work 的限定本地交付完成。G4 承接原 G2 的完整 V1–V7 保证并补核 classifier 修复，不能据此把原 CT2/G2 改成成功。最终声明仍限于指定未发布本地 Codev、Windows/PATH Gradle 9.5.1/Microsoft JDK 17.0.19 下的开发功能验收；正式来源、最低版本动态矩阵及最终 IDEA 导入不在本轮完成声明内。
