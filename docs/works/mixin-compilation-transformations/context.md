# 实施 Context

[Work](work.md) · [Plan](plan.md) · [唯一执行图](waves.md)

## Authority 与范围评估

| family | 处理与 owner | 当前依据 |
| --- | --- | --- |
| PRD | reuse；Cloche [Mixin 产品需求](../../prd/mixin-preapplication.md) | 外部 compile mod、四消费者、原 artifact/variant/classifier、项目关系及运行/发布隔离已覆盖。末尾“Spec 尚待同步”为历史描述，不据此扩范围。 |
| ADR | skip；Cloche 本轮无新 ADR | 本次恢复既有精确选择要求，没有新的架构决定。 |
| Spec | reuse；Cloche [Mixin 技术规范](../../spec/mixin-preapplication.md) | 统一入口、分片/独立 N、原 C、精确 S 与非 S、首产及 V1–V7 已明确；classifier 缺陷违背原 V2，不需改 authority。 |
| Codev authority | reuse；Cloche Spec 所链接的 Codev owning Spec | 继续使用单输入 Mixin 与 ExactMixinStripper，不扩 API、不换正式依赖。 |

已确认的 compilation 职责、`featureName ?: main`、两片重复条目保留、sources 原选片/另一 clean library、无 Join/通知合并均不变。Router 已披露具体实施及同范围图维护，用户回复“可以”；C4/C5 持续适用，身份接续本身不消耗授权。派生文档作者不能据此自行实施、作独立复核或放行 gate；实质变化仍由 Router 判断 C6。

## 当前结果与修复边界

Router 已完整读取并可靠关联 CT1 `implement-mixin-compilation-slices` generation 1 completed、CT2 `independent-mixin-slice-acceptance` generation 1 partial，以及 CT3 `fix-mixin-classifier-selection` generation 1 completed、CT4 `accept-mixin-slices-after-classifier` generation 1 completed。G1、G3、G4 已经 Router 判断满足，当前 Plan 的限定本地完成信号成立；G3 当时仅释放 CT4，原 CT2/G2 保持 partial / 未满足。这里仅保存后续恢复所需事实，不作为完整原 Result 的替代品。

- CT1 已完成统一入口、分片 P/N、顺序 Strip 及消费者/验收器迁移和聚焦作者检查；[CT1 报告](../../../build/reports/mixin-compilation-transformations/ct1/REPORT.md)供按实际输入选择证据。G1 已由 Router 判断满足，当时仅释放原 CT2，不代表修后验收。
- [CT2 完整报告](../../../build/reports/mixin-compilation-transformations/ct2/REPORT.md)为 partial，G2 未满足。它重新判读 CT1 on/off 模型中的 170 项 fixture 条件，重跑比较器反例自检，完成 Fabric 六角色 named/unnamed × on/off 四格 sources 首产/重复运行；这些是限定子证据，不是全部 V1–V7 通过。正式 `paths.gradle` 只修 observer 提前解析及旧路径假设。
- high 缺陷有[失败日志](../../../build/reports/mixin-compilation-transformations/ct2/classifier.log)及[无额外 classifier 对照](../../../build/reports/mixin-compilation-transformations/ct2/classifier-control.log)：`modCompileOnly probe:ref:1` 与普通 `implementation probe:ref:1:extra` 共存时，两次真实 Mixin sources 均漏主 ref，真实 Kotlin 报 `expected 'Ref', actual 'Game'`；仅移除额外 classifier 后 sources 恢复且真实 verifyCompile 成功。对照仅解释根因，不能作为保留 classifier 输入的修复。

[CT3 报告](../../../build/reports/mixin-compilation-transformations/ct3/REPORT.md)记录原 component 扩张根因的修复：role 在原生 variant 选择前惰性继承 C attributes；共享 helper 以原生 artifact ID 与完整 variant 相等关系定位 C 的准确文件，保留两侧 producer，供 S 和必要 remap helper 共用。不按 component/basename 合并，不重建 C/项目图。保留 extra 的真实 Java/Kotlin 正反消费者、两份 N 与 source/game 顺序 Strip、原选片 sources/另一 clean library、off/后配置和失败恢复通过；作者聚焦证据不单独承担完整验收。

失败已产生两片 P/N、AW/映射及部分项目输出；后续对照成功改写 observation，故不能再以该 observation 证明原失败输入当前状态，亦不能称失败零效果。失败日志独立保留。新回归用新的私有输出，保留原报告、原日志及私有对照，不重放已完成效果或清理历史证据。

## 独立验收与证据适用性

[CT4 完整报告](../../../build/reports/mixin-compilation-transformations/ct4/REPORT.md)由不同于 CT1、CT3 作者的执行者完成；独立源码审查未发现新可行动生产缺陷，全部必要 V1–V7 及 classifier 复核有充分适用证据，无剩余必需缺证或阻断失败。报告是仓内证据入口，不冒充完整原 Result；Router 已完成原 Result 阅读、关联及 G4 判断，本次只记录采用事实。

新证据覆盖真实 Java/Kotlin、独立双 N/顺序 Strip、原选片 sources、common 六组交集/回退/关闭参与者、六个 file/Maven C/view/SourceSet 独立冷首产、同请求先准备后四 builders/严格读取、完整 C/项目/variant/classifier/index/闭包/scope、runtime/publication/include/legacy、真实 remap/refmap/direct/context、真实 producer/source/game 首后步失败恢复及必要失效。模型 743/902/352 项有重叠，不加总证明覆盖；结论来自多次定向运行及适用证据复用。UP-TO-DATE/workspace 复用不是 FROM-CACHE，根 Kotlin 编译 UP-TO-DATE 不称重新编译。

实际复用限 CT2 无 classifier 重叠的 Fabric 六角色 named/unnamed × on/off sources 原选片/路径、关闭/runtime 及 source-only 冷首产/重复路线，以及 CT3 当前生产/引擎和保留 classifier 输入下的 common producer→Decompile 失败/恢复。CT4 已核对输入、observer、来源和影响；角色修复影响的 S/P/N/clean、身份和完整模型另有新跑。旧 170 项/CT1 局部模型不承担当前完整通过依据；未变 Codev 引擎穷举未重跑，Cloche 当前顺序 action 组合另测。证据仅适用于报告当次对象与条件，不保证未来变更后永久适用。

原 FullCommon annotations 与 Sources Kotlin build-tools 缺项已分别在私有 offline resolveClosure 验证完整传递闭包；仅复制现有同版本 modules-2/metadata，未复制 locks/transforms/P/N/clean/build，准备不运行 operations，随后检查冷状态再首产。原准备缺口已解决。CT4 早期缺父目录、full-model 请求未配置角色等错误及已产生目录/输出保留；后续在新冷目录或修正 observer 后通过，不删除旧效果冒充未运行。模型时点不同的 P→clean 中间差异保留，最终同请求恢复并重建模型后 743/902 比较通过；详见 CT4 报告。

恢复图作者自检与 `review-mixin-classifier-recovery` generation 1 独立复核在 CT3/CT4 前已完成，无准备度阻断；本次状态维护不改变原 predicate、依赖、风险或必要保证，不新增复核/审批。方法与日志从 CT3/CT4 报告直接导航。

## 环境与安全边界

沿用 PATH Gradle 9.5.1、Microsoft JDK 17.0.19、Spec 允许的唯一 Mixins JAR 私有 metadata 替换；执行时 JIT 核对实际加载来源、Sponge/metadata/classpath、工具与新目录。root 构建、fixture、metadata、同 Home 故障/恢复串行，不改 Codev、正式依赖、共享环境，不清共享缓存、不停共享进程，不提交、推送或发布。

正式来源、最低 Gradle 9.4 动态矩阵、最终 IDEA 导入/根/符号未验证；原生 data/test archive/index miss、潜在同 ID scope 合并及任意重复类/插件风险保留，不扩大本轮完成声明。

[旧 Work](../mixin-preapplication-reimplementation/work.md)的完成和限制继续有效：不重放旧 11 tracked 恢复、129 文件删除，不扫描缓存追补未知 2 文件/9057 bytes；未知差额不改成不存在。历史 partial 不能成为恢复 item 永远等待的成功前置。
