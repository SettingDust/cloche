# 重实现 Context

入口：[Work](work.md)；范围/策略：[Plan](plan.md)；唯一执行关系：[Waves](waves.md)；弃置历史名单：[discard-inputs.md](discard-inputs.md)。本文只保存后续消费及安全恢复需要的处理后事实，不拥有产品或技术合同。

## 权威与持续授权

| family | 处理与 owner | 依据及适用范围 |
| --- | --- | --- |
| PRD | reuse，Cloche 产品行为，[现 PRD](../../prd/mixin-preapplication.md) | 默认开/后配置关闭、既有全部 compilation 角色、四消费者、项目关系及 runtime/发布隔离覆盖路径、模型恢复和原 V1–V7 验收。结尾“Spec 尚待同步”是历史文字，现 Spec 正文优先。 |
| ADR | skip，Cloche 同范围架构决定 | 共享内部目录纠正、私有 runner 的同请求准备/建模及原生 scope 解释不新增架构、公开协议或发布决定。 |
| Spec | reuse，Cloche 接线，[现 Spec](../../spec/mixin-preapplication.md) | 内部路径唯一、原生身份/producer、关闭/runtime、先生产再模型解析及完整 V1–V7 覆盖恢复；scope 按文件用途解释，不要求 getScope 字符串全等，仍保留实质隔离。 |
| Codev authority | reuse，Spec 所链引擎 owner | 仍用既有 Mixin/Strip 公共 API、单目标及通知合同，不扩引擎、版本或发布能力。 |

已接受 compilation 拥有原 C/有效编译游戏及一次内部注册、Fabric 拥有本 compilation AW/完整 CLIENT 单目标、runtime/发布沿原链；不增加统一视图门面或重建项目图。用户已确认从 HEAD 弃置旧实现重做及同范围图/记录维护，无需 backup/stash，该 C5 授权未撤回。Plan 的实证必要最小既有合同修复许可仍适用；Router 已判断路径恢复在原确认范围内，不由本文扩大许可。

## 基线与不可重放边界

R0 partial 及独立证据审计已采用：11 tracked 工作树/索引恢复至固定 HEAD `5613cd2b4ce781aa71aa3e8b4af231b2b3f13d42`；125 源码/fixture 加四旧派生文档共 129 文件逐项删除；九个明确保护文件执行前后字节相同，当时完整 Git status 恰九个 `??`，两类 diff 为空。审计所见项目写命令仅涉及 140 授权路径及对应索引；Temp 两脚本创建后删除在两仓扫描根之外，不能解释仓内差额。

全量后扫描 PermissionError、内存前快照丢失；根 `.git`/140 写路径外汇总净少 2 文件/9057 字节无法定位归因，Codev 后比较未完成。不能据此称恰有两文件删除、越界已证明或没有影响，也没有全部忽略内容/缓存/历史证据/Codev 字节同一证明。原 R0 不重启、不改 completed，恢复/删除不可重放；同名新文件不属旧删除授权。PRD/Spec、XML、新 Work、原始证据、缓存、IDEA 生成文件及相邻 Codev 继续保护。新线索仅阻断其实际受影响消费者，不以未知汇总差额制造所有实施的无限等待。

原始 R0 Result/会话无仓内完整副本；当前记录不冒充原件、不建立外部 Result/session 长期入口。后续确需而不可取得的原件须报告具体主张缺口，不能推断可安全重放。R0V 已核验九保护对象和以下四报告当前存在可读，Router 已采用 completed 并判定 G0 满足；可读性不补历史全树证明。

## 已采用完成与门槛

Router 已完整读取并可靠关联采用 R0V/R1/R2/R3R/R4P/R4M/R5 completed，判定 G0/G1/G2、GP/G3/G4 满足，选定 Plan 的限定本地开发交付完成。以下沿用该可靠采用与门槛判断，不是本次首次采用 Result、重新独立验收或自行放行 gate。

| 仓内入口 | 已采用范围及必要验证结论 |
| --- | --- |
| [R1 编译](../../../build/reports/mixin-r1/REPORT.md) | 真实 Java/Kotlin 编译、精确清理/关闭、项目角色及 runtime/发布隔离；AW 补边后四场景及 include 身份 10 项复验通过。 |
| [R2 sources](../../../build/reports/mixin-r2/REPORT.md) | 真实 CLIENT sources/清理/关闭及失败恢复通过。 |
| [R3R 完整 stub](../../../build/reports/mixin-r3r/REPORT.md) | 完整六组 on/off stub 正负使用者、四种身份配置顺序及原生 Java/sources 项目选择回归通过；内部完整惰性 capability 纠正已完成。 |
| [R4P Windows 路径](../../../build/reports/mixin-r4p/REPORT.md) | 共享 AW/Decompile 目录仅将逻辑分隔符映射为层级；完整 common/runtime 首产、named/unnamed×on/off 六角色 AW/sources 实物、唯一性、身份/producer、关闭/runtime、受影响编译回归及正常复跑充分。GP 已满足。 |
| [R4M 正式模型](../../../build/reports/mixin-r4m/REPORT.md) | 同请求先生产、全部 builders 后严格解析；新 baseline/off/on、sources、完整 common 六组和参与 target 关闭、真实 namespace 故障/撤故障恢复通过。七份最终模型 909 项、0 失败；G3 已满足。 |
| [R5 最终 V1–V7](../../../build/reports/mixin-r5/REPORT.md) | 同一最终实现及指定本地来源下，复核 R1/R2/R3R/R4P/R4M、核对 16 个复用入口来源字节、复比较七模型 909 项零失败并补足下述覆盖；Router 已判 G4 满足。 |

未变化部分按源码、输入、来源、环境和消费者的适用范围复用；纯记录/导航变动不否定行为证据。Spec 当前 Work/Plan/Waves 导航已完成修复，历史路径已标为历史，规范行为未变。恢复安排及 R5 连接已获独立无阻断评审；本次仅作完成记录与非实质修剪，不改变确认范围或必要 assurance，不新增实施确认或自动再评审。

## 原 partial、已发生效果与不可重放限制

- 原 R3 永久 partial：[原报告](../../../build/reports/mixin-r3/REPORT.md)保留三处 stub Provider 接线及 main 共有/独有 API、关闭、component/producer 成功；完整 graph 请求局部 server 失败，完整 off 未执行。配置期已生成 fixture 源码，未进 stub/Mixin 不等于无效果。R3R 恢复成功不回填 R3，不重做既成接线。
- 原 R4 永久 partial：[原报告](../../../build/reports/mixin-r4/REPORT.md)保留 sync 实际物化、runtime 准备、sources 请求及 fixture/runner/observer/verifier 的已成效果。旧两次独立 build 的 baseline/off 完整模型通过，on 缺十二 clean source/四 clean game，原 verifier 22 项失败、off 0/on 4 getter 差异。sources 已生成并打开，准备故障阻断/撤故障恢复有证，恢复后模型仍失败；完整 common sync 被 Windows client:test AW 路径阻断，未请求模型。恢复后的 R4P/R4M 不将旧失败改为通过；旧 scope 差异的过强解释由下节原生语义限定。
- 现有 P/N、clean JAR、sources、模型/日志、私有 Homes/daemon 及失败准备的部分输出保留，不清理或盲重放。原 R4 [evidence.json](../../../build/reports/mixin-r4/evidence.json)只作仓内证据入口，记录整理未复核其每项来源/摘要。模型诊断曾误把 verifier 输出指向原 R4 目录并覆盖派生比较；随后用未改三模型/verifier 重生 22 失败/off0/on4，无覆盖前快照，不能声称字节恢复。原 REPORT/evidence/模型/日志未被该命令写。误跑过根 wrapper --version（9.8.2），无构建/测试，非兼容证据，其启动副作用未全树审计。

## 路径与模型的已实现方法及适用边界

R4P 在 TargetCompilation.registerCompilationTransformations 的共享目录中沿用 CompilationInternal.namePath 的分隔规则，将 client:test/client:data 映射为 client/test、client/data；公开 name/task/SourceSet/capability/operation key/side、普通/无分隔符合成名和 target 根保持。AW 与 sources Provider 消费声明的新输出，不移动或删除旧输出。原 common sync 测试只跑 clocheIdeSync 且关闭 sources 下载，因此单个测试不证明模型或全部 sources；R4P 另有六角色 AW/sources 实物，R4M 另有实际模型。非 Windows 嵌套角色旧硬编码冒号路径可能受影响，不能由 Windows 验收外推全平台。

正式 Tooling 入口已恢复为 connection.model(Observation.class).forTasks(":clocheIdeSync").get()：同一个产生全部模型的请求中先生产，再依次执行各项目 DownloadPolicy、SourceSetArtifactBuildRequest、SourceSetModel、DependencyModel 和严格 C/view/SourceSet/configuration 读取，保留完整 getter/index（含 miss）、身份/闭包/producer 和真实物化。已否定的两次独立 build 配方不复用；若未来拆多个 model.get，每次仍需对应生产保证。selectedSources.elements.map、builtBy、通知 Task Provider 和 ABSOLUTE 未为模型恢复盲改。

原生 Merger 按不同物理文件将原 COMPILE 拆为 clean PROVIDED + raw RUNTIME 是合理表达，Spec 不要求 getScope 字符串全等。验收按实际文件用途、身份及闭包核对；raw RUNTIME 出现在完整模型不等于 raw 进入 compile。外部 clean 编译文件、项目/SourceSet/必要闭包及 runtime 原链隔离继续必需，不预筛节点、删用途断言或把 compile 改 raw。IDEA 下游若经 enrichment 将同 ID 两文件合成 multi-library，可能扩大 scope；完整 enrichment 链及最终导入未验证，只保留条件风险，不增 IDEA 专用适配或最终导入待办。

## 最终本地验收覆盖与证据限制

R5 在同一最终生产实现上复用各出口仍适用证据，并新增独立 file C/view/SourceSet 冷首产、真实非空 include/StripIncludes、精确非 S/classifier、有效 context 真实 dispatch 与缺失 context 失败恢复、空 S/N/可选失败、source/target Strip 真实编译失败恢复、stub producer 失败恢复、S 内容/配置/成员/位置与 side/mapping/AW/N 失效、target 隔离、workspace/新进程逐文件一致性及 Forge main/data/test 真实 legacy writer 两态隔离。报告所列 32 个选定成功运行及 6 个预期失败有日志/行为证据；这是证据复用和多次定向运行，不是一遍完整套件，也没有重跑全部 R1–R4 或完整引擎矩阵。

R5 早期部分 cache/S-content/S-config 快照被后续运行替换，不能恢复其历史快照保证；S 内容/配置失效结论限定保留日志中的实际 producer 执行和消费断言。后来两次 workspace 专跑及逐文件快照是新证据，不能补造旧保证。旧 failed/MX4 partial/取消实验不改为成功，旧 28 测试不作新结构；最终验收未改生产或 authority。

## 可复用方法及环境限制

以下入口保留已采用方法及限制；本次仅检查链接可读性，不重跑或复核其底层日志：

| 入口 | 用途与限制 |
| --- | --- |
| [关闭省略原型](../../../build/tmp/state-omission-prototype-1793655235893/NOTES.md) | 9.5.1 公开 withDependencies 条件安装 state；整份 C 显式 raw 会改变项目选择，absent Provider 非法。非完整 Cloche 成功。 |
| [通知因果诊断](../../../build/tmp/notification-wiring-diagnosis-20261103/REPORT.md) | C/view/SourceSet 单独消费须核实任务调度；参数来源不保证 producer 入图。含替身，不证明四消费者。 |
| [真实 Tooling 原型](../../../build/tmp/tooling-prototype-1793653484927/NOTES.md) | 真实 builders/getter/index 与独立 lookup 方法；data/test 原生 miss 保留，非最终 IDE，不单独证明新 Cloche 模型通过。 |
| [Codev 命名原型](../../../build/tmp/codev-contract-20260720/NOTES.md) | 本地修正版 remap/refmap/context/正常 daemon 因果方法；无全引擎或 Cloche 全链证明，见证应实际 dispatch。 |

本地完成只覆盖 Windows、PATH 全局 Gradle 9.5.1、Microsoft JDK 17.0.19 及 Spec 唯一指定未发布本地 Mixins。验收使用私有 Home/新 metadata，只替换唯一 Mixins 项并核对实际类/Task 来源，生产坐标与规范 metadata 未改，没有 Maven Local/旧 JAR fallback。最低 Gradle 9.4/toolchain17 不变，9.4 未动态实跑；正式发布来源未接入。最终 IDE 导入/模块根/PSI/符号取消且未验证，条件性同 ID 合库 scope 风险未消除。Forge 只证明报告 tiny-input/真实 writer 接线与隔离，不是完整下载、补丁、启动或运行游戏验收。历史 R0/R3/R4 partial 和上述未知继续保留。

本次仅维护 Work/Context/Plan/Waves 四文档并作互链、状态、图和格式检查，不执行新构建、Gradle、测试、实验、IDE 或 Git 写；完成记录不构成新的行为验收或门槛判断。
