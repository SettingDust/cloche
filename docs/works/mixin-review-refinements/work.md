# Mixin 评审修正

[Context](context.md) · [Plan](plan.md) · [唯一执行图](waves.md)

本 Work 承接本轮已对齐的 Cloche/Codev Spec：将有序多记录清理及完整安全生命周期归 Codev，Cloche 保留精确来源选择和注册；同时让 compilation 的统一转换入口只消费平台数据，保留原编译、sources、runtime 和发布语义。

当前限定本地交付已完成：独立计划复核无阻断通过，用户已确认披露范围并授权实施及同范围完成记录维护；Router 已采用 A/B/C/V 的完成事实并判定 G-API、G-CALLERS、G-PLATFORM、G-ACCEPT 满足。此处仅同步既有判断，本次文档维护不重新验收或释放 gate。实际对象、V1–V7 及原始证据见[独立验收报告](../../../build/tmp/v-accept-20261105/REPORT.md)，边界和追加评论处理见 [Context](context.md)。

## Authority

- Cloche [PRD](../../prd/mixin-preapplication.md)与[Spec](../../spec/mixin-preapplication.md)，尤其统一注册、精确选择、运行来源、开发编译来源及 V1–V7。
- Codev [PRD](../../../../minecraft-codev/docs/prd/codev-mixin-rework.md)、[既有 isolation ADR](../../../../minecraft-codev/docs/adr/mixin-operation-isolation.md)及 [English owning Spec](../../../../minecraft-codev/docs/spec/mixin-preapplication.md) / [中文对照](../../../../minecraft-codev/docs/spec/mixin-preapplication.zh-CN.md)。

本目录只含 `work.md`、`context.md`、`plan.md`、`waves.md` 一个匹配 tuple。Plan 描述实施范围与方法，Waves 独占执行图；不创建 Tasks、第二 Plan 或第二执行图。

## 完成边界

本 Plan 的限定本地交付以 Waves 全部必要 gate、最终 `G-ACCEPT` 为完成信号，Router 已判定满足。本次仅同步四份派生记录并自检文档一致性，不将文字维护当作动态测试。

不改 authority、旧 tuple 或无关代码，不重开[既有交付](../mixin-compilation-transformations/work.md)。正式发布依赖接入、最低 Gradle 9.4 完整动态矩阵、最终 IDE 导入、stage/commit/push/publish 均排除。用户新增的 fixture 环境属性移除与实际运行环境继承属于本 A/B/C/V 图之外的独立追加维护，来源要求见 [Spec 的测试运行环境来源](../../spec/mixin-preapplication.md#测试运行环境来源)；不再作全局排除，也不将其实施或新证据并入本图各 predicate、gate 或 B/C 的责任。
