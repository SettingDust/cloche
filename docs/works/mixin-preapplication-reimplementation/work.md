# 从 HEAD 重实现 Mixin 编译预应用

在 [PRD](../../prd/mixin-preapplication.md) 和 [Spec](../../spec/mixin-preapplication.md) 的常规 JAR 原生属性范围内，从固定 HEAD 撤去旧未提交实现，再按已接受独立设计实现编译预应用与完整清理，使编译、反编译、common stub、compile sync/真实 Tooling 模型实际消费正确视图。**Router 已判断选定 Plan 的限定本地开发交付完成。**

用户已确认本 Plan 实施及同范围图/记录维护，持续授权未撤回。Router 已完整读取并可靠关联采用 R0V/R1/R2/R3R/R4P/R4M/R5 的 completed 结果，分别判断 G0/G1/G2、GP/G3/G4 满足。原 R0/R3/R4 永久保持 partial；本次只记录已采用判断，不重新执行验收或放行门槛。

[Windows 路径恢复](../../../build/reports/mixin-r4p/REPORT.md)已完成共享 AW/Decompile 目录的逻辑分隔符映射，完整 common/runtime 首产及 named/unnamed×on/off 六角色 AW/sources、身份/producer 和受影响回归通过。[正式模型恢复](../../../build/reports/mixin-r4m/REPORT.md)已使用同一个 `connection.model(Observation.class).forTasks(":clocheIdeSync").get()` 请求先生产再执行全部 builders/严格读取；新 baseline/off/on、sources、完整 common 六组、参与 target 关闭和真实 namespace 故障/恢复通过，七份最终模型共 909 项检查。

[最终 V1–V7 报告](../../../build/reports/mixin-r5/REPORT.md)是本地验收的仓内入口：复用各出口仍适用的证据并补足独立 file C/view/SourceSet 首产、非空 include/精确非 S、context、真实 Strip/producer 故障恢复、缓存/失效、target 隔离和 Forge legacy writer。32 个选定成功运行及 6 个预期失败有证据；这是复用与多次定向运行，不能表述为一遍完整套件。范围、复用条件及覆盖限制见 [Context](context.md) 和报告。

原生 scope 按真实文件用途解释：clean PROVIDED + raw RUNTIME 可表达原 COMPILE，仍须保持身份、项目/SourceSet/闭包和编译运行隔离。结论限定 Windows、PATH Gradle 9.5.1、Microsoft JDK 17.0.19 及 Spec 唯一指定未发布本地 Mixins。正式发布来源未接入、最低 Gradle 9.4 未动态实跑、最终 IDE 导入/模块根/PSI/符号仍取消且未验证；IDEA 下游同 ID 合库扩大 scope 的条件风险未消除。Forge 仅报告所述 tiny-input/真实 writer 范围，不证明完整下载、补丁、启动或运行游戏。

本目录选定 tuple 为本 `work.md`、[Context](context.md)、唯一 [Plan](plan.md) 及唯一执行图 [Waves](waves.md)。[弃置历史输入](discard-inputs.md)只保存已执行历史，没有 tasks 或第二图。Spec 当前三条导航已修复，不再作为待办；历史原件不由新文档冒充。未来如有 planned 调用，仍须明确选择匹配成员、各恰一次正式 ref，并满足当前字节/身份/JIT、前置、冲突、权限及仍确认范围条件；本文完成记录不授权新工作。

原 R0 的 11 tracked 恢复与 129 文件删除不可重放；未知 2 文件/9057 字节、Codev 后比较缺口及旧派生比较误覆盖后重生无前快照仍保留，详见 Context。R5 早期失效快照被后续比较替换，S 内容/配置结论限保留运行日志，后来的 workspace 两次专跑是新证据，不能补造旧历史保证。实现范围、predicate、依赖及必要 assurance 未改；本次仅维护四份派生文档并作记录检查，未运行新构建、Gradle、测试、实验、IDE 或 Git 写。
