# Mixin 编译预应用

本文约定 Cloche 的产品行为，面向使用依赖 mod 注入 API 的开发者。目标是在下述适用输入范围内，为编译、反编译及 common API stub 呈现依赖 Mixin 处理后的游戏视图，减少依赖实际提供的 API 与开发时可见 API 的差异。本文记录已接受的需求，不表示功能已经实现或通过行为验收。

## 范围与默认行为

1. **默认启用，提供 opt-out。** 默认值允许用户在后续 DSL 配置中关闭。关闭后，编译、反编译、common stub 及编译 sync 恢复原有未预应用的编译视图；不要求用户修改依赖声明才能恢复。公共开关的具体 DSL 名称由技术 Spec 拥有。
2. **sources 从当前 compilation 的外部 mod compile 依赖图中选择，预应用保证限于常规 JAR 原生属性链覆盖的输入。** 沿用现有 mod 分类和依赖解析规则，在该适用范围内包含 compileOnly 以及既有传递依赖；选定的外部 mod sources 记为 `S`。排除项目组件，不额外扫描 nested JAR。普通解析库不因出现在 classpath 就成为 Mixin sources。当前项目的 Mixins 不回灌到此预应用流程。
3. **依赖 scope 保持原义。** runtimeOnly mod 不成为预应用 sources，不因预应用而获得编译可见性；compileOnly 不因参与预应用而成为运行依赖。预览中来自 compileOnly Mixin 的 API 不保证在缺少该依赖的运行环境存在。
4. **在适用范围内消费完整预应用视图。** 编译、反编译、common stub 及编译 sync 使用预应用后并经清理的游戏及 `S` 的相应清理结果；必要清理恰限于 `S` 与本次预应用派生游戏，均依据同次预应用通知。非 `S` 编译依赖保留原文件及既有角色；需要的清理结果必须实际被消费，不能只注册任务就视为完成。保留原依赖身份、所选 variant/classifier、项目/SourceSet 关系和生产依赖，消费者不要求逐 artifact 保序。原始游戏与依赖输入不被覆盖。
5. **运行与发布保持既有行为。** runtime 使用 AW 处理后的游戏及正常 remap、未 StripMixins 的依赖，不使用编译预应用副本；不改变 Mixin agent、Loader 的运行行为，也不改变项目发布产物链。

适用输入范围以已接受的 SettingDust/cloche 固定参考所体现的常规 JAR 原生属性范围为界：参考为 JAR 提供默认处理状态，不为任意归档提供同等保证；具体属性与接线由技术 Spec 约定。混合扩展名 variant、非标准后缀或无后缀 mod 归档不获通用预应用及完整清理保证，也不扩展为 JAR+ZIP 支持列表。这些依赖仍可按原规则存在于原依赖图中；支持保证的收敛不授权删除依赖、重选依赖图或把项目 classes/resources 改成 JAR。不新增通用 unsupported 扫描或报错框架，不要求实现未承诺格式的处理。

## 有效环境与 common API

“分割”指配置了独立 Fabric `client()` compilation，沿用现有 `hasSeparateClient` 分类；`includedClient()` 不属于该分类。有效环境如下：

| compilation / 目标形态 | 预应用环境 |
| --- | --- |
| 分割 Fabric 的 main | SERVER |
| 分割 Fabric 的独立 client | CLIENT |
| Fabric includedClient | CLIENT |
| 未分割目标，包括未配置独立 client 的 Fabric 与 joined Forge/NeoForge | CLIENT |
| data / test | 继承所属源集的有效环境；分割 Fabric main 下为 SERVER，独立 client 下为 CLIENT，非分割为 CLIENT |

不得仅因名称或属性包含 `common` 就一律选择 SERVER。Fabric 的 CLIENT 视图必须同时呈现该环境对 common 游戏类与 client 游戏类的 Mixin 修改，不能遗漏客户端 Mixin 注入 common 类的 API；完整 CLIENT 目标的重名类或资源按 client 优先处理。

common stub 继续对既有参与 target 的有效编译视图求 API 交集，不额外对同一 target 的 CLIENT/SERVER 视图求交，也不合并两侧预应用结果。编译预览不保证另一侧运行具有同一注入 API；例如分割 Fabric main 的 SERVER-only 注入 API 不因此获得客户端运行保证。

## 失败与兼容性

复用 Codev 原有异常、可选失败、部分应用及正常 fallback 语义。引擎正常返回但有日志或部分应用，不额外按日志判失败，也不宣称所有 Mixins 均完整成功。实际向外抛出的引擎异常、上述适用范围内的必要清理及输入输出失败须阻断受影响的消费，不能静默退回旧视图并声称预应用成功。

Cloche 负责选择 sources、有效环境和消费视图；引擎的预应用、通知及精确配置条目清理规则继续由 Codev 拥有。本需求不增加引擎能力或版本升级承诺。将 Fabric common/client AW 分片拼成一个目标是已有设计证据中的内部方案，本文只要求 CLIENT 覆盖 common 类，不将该文件布局固定为产品接口。

本范围不包含双侧合并、同 target 双侧 API 交集、P4 扩展、编译器插件迁移、发布 CI、Maven 发布文档或旧缺陷修复。

## 成功条件

以下是待实现行为的验收条件，并非本次已执行的测试：

- 不配置开关时，适用范围内依赖 Mixin 引入的 API 可供对应环境编译、反编译、common stub 及编译 sync 消费；后配置 opt-out 后恢复原编译视图。
- 在常规 JAR 原生属性适用范围内，compileOnly 与既有传递 mod 可参与预应用；runtimeOnly、项目组件及额外 nested 扫描不被纳入 sources，依赖 scope 不被提升，当前项目 Mixins 不回灌。范围外依赖仍保留原解析关系，不据此宣称混合扩展名 variant、非标准后缀或无后缀 mod 归档已获通用预应用及完整清理保证。
- 各编译消费者实际使用按同次通知清理的 `S` 与派生游戏；非 `S` 原文件、原依赖身份、variant/classifier、项目/SourceSet 关系及生产依赖保留，原始输入不变。
- 按上述环境表处理分割、includedClient、未分割及 data/test；Fabric CLIENT 对 common 游戏类和 client 游戏类的修改均在编译与反编译视图可见。
- common stub 仍是既有跨 target 交集，不附带另一侧运行保证。
- runtime 继续使用 AW 游戏与正常 remap、未 Strip 的依赖，发布链与原始输入保持原状；真实处理或必要清理失败不会被当作成功消费，正常引擎可选失败不会仅因日志被升级为失败。

## 所有权与依据

当前仓内没有另一份同范围 Cloche PRD；本文件拥有上述产品范围。[技术 Spec](../spec/mixin-preapplication.md) 已存在，尚待同步本次适用输入范围；具体 DSL、任务接线、文件布局及 mapping/refmap 策略由其拥有，本次 PRD 修订不表示 Spec 已同步或实现已修复。没有新增独立架构决定，本范围无需新 Cloche ADR。Codev 既有 PRD/ADR/Spec 能力边界沿用既有评估，本文件不复制引擎合同。

适用输入范围依据已接受的固定参考 `b79540d3c6ad336dae4917711be74e2071992261`：[JAR 默认处理状态](https://github.com/SettingDust/cloche/blob/b79540d3c6ad336dae4917711be74e2071992261/src/main/kotlin/earth/terrarium/cloche/ProjectPluginApplicator.kt#L53-L60) 及 [原 Configuration、上游输入与游戏消费接线](https://github.com/SettingDust/cloche/blob/b79540d3c6ad336dae4917711be74e2071992261/src/main/kotlin/earth/terrarium/cloche/target/TargetCompilation.kt)。仅采用其有界输入范围与原生接线依据；参考中同时改变 runtime、扩大清理集合的行为不属于本产品约定。

已核实的当前 Cloche 源码用于说明分类与既有消费位置，不作为新行为已经实现的证据：

- [FabricTargetImpl.kt](../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricTargetImpl.kt)：独立 client 与 includedClient 互斥、`hasSeparateClient` 分类及 main/data/test 所属关系。
- [FabricClientSecondarySourceSets.kt](../../src/main/kotlin/earth/terrarium/cloche/target/fabric/FabricClientSecondarySourceSets.kt)：client data/test 的客户端归属。
- [CommonTargetCreator.kt](../../src/main/kotlin/earth/terrarium/cloche/target/common/CommonTargetCreator.kt)：现有编译 classpath 输入与跨 target stub 交集。
