package earth.terrarium.cloche.fixture

object VerificationTaskSnippet {
    fun verifyNamespaceBucket(
        taskName: String,
        sourceNamespace: String,
        dependencyGroup: String,
        dependencyName: String,
        dependencyVersion: String,
    ): String =
        """
        tasks.register("$taskName") {
            val sourceNamespaceAttribute = org.gradle.api.attributes.Attribute.of(
                "earth.terrarium.cloche.internal.remapSourceNamespace",
                String::class.java,
            )
            val namespaceConfigs = configurations.matching {
                it.name.contains("$sourceNamespace", ignoreCase = true) && it.name.endsWith("CompileClasspath")
            }

            doLast {
                val declarableBucket = namespaceConfigs.single { !it.isCanBeResolved }
                val resolvableView = namespaceConfigs.single { it.isCanBeResolved }

                check(declarableBucket.dependencies.any {
                    it.group == "$dependencyGroup" && it.name == "$dependencyName" && it.version == "$dependencyVersion"
                })
                check(declarableBucket.dependencies.filterIsInstance<org.gradle.api.artifacts.ExternalModuleDependency>().all {
                    it.attributes.getAttribute(earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute.ATTRIBUTE) == null
                })
                check(!declarableBucket.isCanBeResolved)
                check(declarableBucket.isCanBeDeclared)
                check(!declarableBucket.isCanBeConsumed)
                check(resolvableView.isCanBeResolved)
                check(!resolvableView.isCanBeDeclared)
                check(!resolvableView.isCanBeConsumed)
                check(resolvableView.extendsFrom.contains(declarableBucket))
                check(resolvableView.attributes.getAttribute(earth.terrarium.cloche.REMAPPED_ATTRIBUTE) == false)
                check(
                    resolvableView.attributes.getAttribute(
                        earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute.ATTRIBUTE,
                    ) == earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute.INITIAL,
                )
                check(
                    resolvableView.attributes.getAttribute(
                        sourceNamespaceAttribute,
                    ) == "$sourceNamespace",
                )
            }
        }
        """.trimIndent()

    fun verifyIntermediaryBucket(taskName: String = "verifyIntermediaryBucket"): String =
        verifyNamespaceBucket(
            taskName = taskName,
            sourceNamespace = earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute.INTERMEDIARY,
            dependencyGroup = "net.fabricmc",
            dependencyName = "fabric-loader",
            dependencyVersion = "0.16.10",
        )

    fun verifySeargeBucket(taskName: String = "verifySeargeBucket"): String =
        verifyNamespaceBucket(
            taskName = taskName,
            sourceNamespace = earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute.SEARGE,
            dependencyGroup = "net.minecraftforge",
            dependencyName = "forge",
            dependencyVersion = "1.20.1-47.4.0",
        )
}
