package earth.terrarium.cloche

import java.io.File
import java.lang.reflect.Proxy
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

import earth.terrarium.cloche.api.MappingsBuilder
import earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute
import earth.terrarium.cloche.api.target.MinecraftTarget
import earth.terrarium.cloche.api.target.compilation.remapNamespace
import earth.terrarium.cloche.fixture.BuildScriptTemplate
import earth.terrarium.cloche.fixture.LoaderKind
import earth.terrarium.cloche.fixture.LoaderScenarioSpec
import earth.terrarium.cloche.fixture.RepositoryPresets
import earth.terrarium.cloche.fixture.TestKitProjectFixture
import earth.terrarium.cloche.fixture.VerificationTaskSnippet
import earth.terrarium.cloche.target.compilation.modConfigurationName
import earth.terrarium.cloche.target.compilation.remapSourceNamespaces
import net.msrandom.minecraftcodev.remapper.MinecraftCodevRemapperPlugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test

class RemapNamespaceDependencyTests {
    @Test
    fun `remapNamespace annotates module dependency source namespace`() {
        val project = ProjectBuilder.builder().build()
        val dependency = project.dependencies.create("example:mod:1.0") as ExternalModuleDependency

        val result = dependency.remapNamespace(RemapNamespaceAttribute.INTERMEDIARY)

        assertSame(dependency, result)
        assertEquals(
            RemapNamespaceAttribute.INTERMEDIARY,
            dependency.attributes.getAttribute(RemapNamespaceAttribute.ATTRIBUTE),
        )
    }

    @Test
    fun `fabric intermediary mapping declaration adds intermediary namespace`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("java")
        project.configurations.create("mappings")
        val target = testMinecraftTarget(project)
        val mappings = MappingsBuilder(target, project)

        mappings.fabricIntermediary()

        assertEquals(listOf("none", "obf", RemapNamespaceAttribute.INTERMEDIARY), mappings.remapNamespaces.get())
    }

    @Test
    fun `mcp searge mapping declaration adds searge namespace without unrelated defaults`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("java")
        project.configurations.create("mappings")
        val target = testMinecraftTarget(project)
        val mappings = MappingsBuilder(target, project)

        mappings.mcpSearge()

        val namespaces = mappings.remapNamespaces.get()
        assertEquals(listOf("none", "obf", RemapNamespaceAttribute.SEARGE), namespaces)
        assertFalse(RemapNamespaceAttribute.INTERMEDIARY in namespaces)
    }

    @Test
    fun `transform namespace collection uses only mapping declarations`() {
        val project = ProjectBuilder.builder().build()
        val target = testMinecraftTarget(project)
        val mappings = MappingsBuilder(target, project)

        mappings.remapNamespaces.add("named")

        val transformSourceNamespaces = mappings.remapNamespaces.get().distinct()

        assertEquals(listOf("none", "obf", "named"), transformSourceNamespaces)
        assertFalse(RemapNamespaceAttribute.INTERMEDIARY in transformSourceNamespaces)
        assertFalse(RemapNamespaceAttribute.SEARGE in transformSourceNamespaces)
        assertTrue("named" in transformSourceNamespaces)
    }

    @Test
    fun `empty default mod remap namespace is not a transform output namespace`() {
        assertEquals(
            setOf(
                RemapNamespaceAttribute.INITIAL,
                RemapNamespaceAttribute.INTERMEDIARY,
                RemapNamespaceAttribute.SEARGE,
                RemapNamespaceAttribute.OBF,
            ),
            remapSourceNamespaces(""),
        )
        assertFalse("" in remapSourceNamespaces(""))
    }

    @Test
    fun `forge can route explicit source namespace DSL into namespace bucket without dependency attribute`() {
        val fixture = TestKitProjectFixture.create("cloche-remap-namespace-routing")
        fixture.writeScenario(
            spec = LoaderScenarioSpec(
                projectName = "routing",
                loaderKind = LoaderKind.Forge,
                loaderVersion = "47.4.0",
                repositories = RepositoryPresets.remapRouting,
                mappingsBlock = """
                    official()
                    parchment("2023.09.03")
                """.trimIndent(),
                dependenciesBlock = """
                    modCompileOnly("net.fabricmc:fabric-loader:0.16.10", sourceNamespace = "intermediary")
                """.trimIndent(),
            ),
            verificationTask = VerificationTaskSnippet.verifyIntermediaryBucket(),
        )

        val result = fixture.run("verifyIntermediaryBucket", "--stacktrace", "--console=plain")

        assertTrue(result.output.contains("verifyIntermediaryBucket"))
    }

@Test
fun `forge legacy remap namespace attribute routes to same intermediary bucket but is stripped`() {
    val projectDir = createTempDir("cloche-remap-namespace-direct-routing")
    projectDir.resolve("settings.gradle.kts").writeText("rootProject.name = \"direct-routing\"\n")
    projectDir.resolve("build.gradle.kts").writeText(
        """
import earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute
import earth.terrarium.cloche.api.target.compilation.remapNamespace
import org.gradle.api.artifacts.ExternalModuleDependency

plugins {
id("earth.terrarium.cloche")
}

group = "test.routing"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.msrandom.net/repository/root/")
    maven("https://maven.msrandom.net/repository/cloche/")
    maven("https://maven.minecraftforge.net/")
}

cloche {
    minecraftVersion = "1.20.1"

    metadata {
        modId = "routing"
    }

    mappings {
        official()
        parchment("2023.09.03")
    }

    forge {
        loaderVersion = "47.4.0"

        dependencies {
            modCompileOnly(project.dependencies.remapNamespace("net.fabricmc:fabric-loader:0.16.10", "intermediary"))
            modCompileOnly("net.fabricmc:fabric-loader:0.16.9") {
                attributes {
                    attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.INTERMEDIARY)
                }
            }
        }
    }
}

tasks.register("verifyIntermediaryBucketContracts") {
    val sourceNamespaceAttribute = org.gradle.api.attributes.Attribute.of(
        "earth.terrarium.cloche.internal.remapSourceNamespace",
        String::class.java,
    )
    val namespaceConfigs = configurations.matching {
        it.name.contains("intermediary", ignoreCase = true) && it.name.endsWith("CompileClasspath")
    }

    doLast {
        val declarableBucket = namespaceConfigs.single { !it.isCanBeResolved }
        val resolvableView = namespaceConfigs.single { it.isCanBeResolved }
        val fabricLoaderDependencies = declarableBucket.dependencies.filter {
            it.group == "net.fabricmc" && it.name == "fabric-loader"
        }

        check(fabricLoaderDependencies.map { it.version }.toSet() == setOf("0.16.10", "0.16.9"))
        check(!declarableBucket.isCanBeResolved)
        check(resolvableView.isCanBeResolved)
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
            ) == earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute.INTERMEDIARY,
        )
        check(fabricLoaderDependencies.filterIsInstance<ExternalModuleDependency>().all {
            it.attributes.getAttribute(RemapNamespaceAttribute.ATTRIBUTE) == null
        })
    }
}
""".trimIndent()
    )

    val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments("verifyIntermediaryBucketContracts", "--stacktrace", "--console=plain")
        .forwardOutput()
        .build()

    assertTrue(result.output.contains("verifyIntermediaryBucketContracts"))
}

@Test
    fun `fabric default unannotated mod dependency is stamped as intermediary in namespace bucket`() {
        val fixture = TestKitProjectFixture.create("cloche-fabric-default-namespace-routing")
        fixture.writeScenario(
        spec = LoaderScenarioSpec(
            projectName = "fabric-routing",
            modId = "fabricrouting",
            loaderKind = LoaderKind.Fabric,
            loaderVersion = "0.16.10",
            repositories = RepositoryPresets.remapRouting,
            mappingsBlock = """
                official()
            """.trimIndent(),
            dependenciesBlock = """
                modCompileOnly("net.fabricmc:fabric-loader:0.16.10")
            """.trimIndent(),
        ),
        verificationTask = VerificationTaskSnippet.verifyIntermediaryBucket("verifyFabricDefaultIntermediaryBucket"),
    )

    val result = fixture.run("verifyFabricDefaultIntermediaryBucket", "--stacktrace", "--console=plain")

        assertTrue(result.output.contains("verifyFabricDefaultIntermediaryBucket"))
    }

    @Test
    fun `forge default unannotated mod dependency is stamped as searge in namespace bucket`() {
        val fixture = TestKitProjectFixture.create("cloche-forge-default-namespace-routing")
        fixture.writeScenario(
            spec = LoaderScenarioSpec(
                projectName = "forge-routing",
                modId = "forgerouting",
                loaderKind = LoaderKind.Forge,
                loaderVersion = "47.4.0",
                repositories = RepositoryPresets.remapRouting,
                mappingsBlock = """
                    official()
                    parchment("2023.09.03")
                """.trimIndent(),
                dependenciesBlock = """
                    modCompileOnly("net.minecraftforge:forge:1.20.1-47.4.0")
                """.trimIndent(),
            ),
            verificationTask = VerificationTaskSnippet.verifySeargeBucket("verifyForgeDefaultSeargeBucket"),
        )

        val result = fixture.run("verifyForgeDefaultSeargeBucket", "--stacktrace", "--console=plain")

        assertTrue(result.output.contains("verifyForgeDefaultSeargeBucket"))
    }

    @Test
    fun `fabric source set compile classpath exposes mod compile dependencies while kotlin compile stays sanitized`() {
        val fixture = TestKitProjectFixture.create("cloche-fabric-compile-classpath-visibility")
        fixture.writeScenario(
            spec = LoaderScenarioSpec(
                projectName = "fabric-compile-classpath-visibility",
                modId = "fabric_compile_visibility",
                buildScriptPreamble = """
                    import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

                    fun org.gradle.api.artifacts.Configuration.allDependenciesRecursive(
                        visited: MutableSet<org.gradle.api.artifacts.Configuration> = mutableSetOf(),
                    ): Sequence<org.gradle.api.artifacts.Dependency> {
                        if (!visited.add(this)) return emptySequence()
                        return dependencies.asSequence() + extendsFrom.asSequence().flatMap { it.allDependenciesRecursive(visited) }
                    }
                """.trimIndent(),
                additionalPluginLines = """
                    id("org.jetbrains.kotlin.jvm") version "${'$'}embeddedKotlinVersion"
                """.trimIndent(),
                loaderKind = LoaderKind.Fabric,
                loaderVersion = "0.16.10",
                repositories = RepositoryPresets.remapRouting.filterNot { it.contains("neoforged") || it.contains("minecraftforge") },
                mappingsBlock = """
                    official()
                """.trimIndent(),
                dependenciesBlock = """
                    fabricApi("0.92.6")
                    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.3+kotlin.2.0.21")
                """.trimIndent(),
            ),
            verificationTask = """
                tasks.register("verifyFabricCompileClasspathVisibility") {
                    val sourceSetName = "fabric"
                    val compileClasspath = configurations.getByName("${'$'}sourceSetName${'$'}{"CompileClasspath"}")
                    val modImplementation = configurations.getByName("mod${'$'}{sourceSetName.replaceFirstChar(Char::titlecase)}Implementation")
                    val kotlinCompile = tasks.named<KotlinCompile>("compileFabricKotlin")

                    dependsOn(kotlinCompile)

                    doLast {
                        check(compileClasspath.extendsFrom.contains(modImplementation)) {
                            "${'$'}{compileClasspath.name} must extend ${'$'}{modImplementation.name}"
                        }

                        val dependencyCoordinates = compileClasspath.allDependenciesRecursive()
                            .mapNotNull { dependency ->
                                val group = dependency.group ?: return@mapNotNull null
                                val version = dependency.version ?: return@mapNotNull null
                                "${'$'}group:${'$'}{dependency.name}:${'$'}version"
                            }
                            .toSet()

                        check("net.fabricmc:fabric-language-kotlin:1.12.3+kotlin.2.0.21" in dependencyCoordinates) {
                            "${'$'}{compileClasspath.name} is missing fabric-language-kotlin: ${'$'}dependencyCoordinates"
                        }
                        check(dependencyCoordinates.any { it.startsWith("net.fabricmc.fabric-api:") }) {
                            "${'$'}{compileClasspath.name} is missing Fabric API modules: ${'$'}dependencyCoordinates"
                        }

                        val libraries = kotlinCompile.get().libraries.files
                        val rawClasspathLeaks = libraries.filter { library ->
                            val path = library.invariantSeparatorsPath
                            path.endsWith(".jar") &&
                                path.contains("/modules-2/files-2.1/") &&
                                (
                                    path.contains("/net.fabricmc.fabric-api/") ||
                                        path.contains("/net.fabricmc/fabric-loader/") ||
                                        path.contains("/fabric-language-kotlin/") ||
                                        library.name.contains("intermediary", ignoreCase = true)
                                    )
                        }
                        check(rawClasspathLeaks.isEmpty()) {
                            "Kotlin compile libraries leaked raw Fabric/Fabric API/mod Kotlin or intermediary cache jars: ${'$'}rawClasspathLeaks"
                        }
                    }
                }
            """.trimIndent(),
        )

        val result = fixture.run(
            "verifyFabricCompileClasspathVisibility",
            "-Dorg.gradle.jvmargs=-Xmx4G -Dfile.encoding=UTF-8",
            "--max-workers=1",
            "--stacktrace",
            "--console=plain",
        )

        assertTrue(result.output.contains("verifyFabricCompileClasspathVisibility"))
    }

@Test
fun `fabric kotlin compile libraries use remapped mod classpath without raw fabric api cache jars`() {
    val projectDir = createTempDir("cloche-fabric-kotlin-sanitized-classpath")
    projectDir.resolve("settings.gradle.kts").writeText("rootProject.name = \"fabric-kotlin-sanitized\"\n")
    projectDir.resolve("build.gradle.kts").writeText(
        """
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.zip.ZipFile

plugins {
    id("org.jetbrains.kotlin.jvm") version "${'$'}embeddedKotlinVersion"
    id("earth.terrarium.cloche")
}

group = "test.routing"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net/")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.msrandom.net/repository/root/")
    maven("https://maven.msrandom.net/repository/cloche/")
}

cloche {
    minecraftVersion = "1.20.1"

    metadata {
        modId = "fabric_kotlin_sanitized"
    }

    mappings {
        official()
    }

    fabric {
        loaderVersion = "0.16.10"

        dependencies {
            fabricApi("0.92.6")
            modImplementation("net.fabricmc:fabric-language-kotlin:1.12.3+kotlin.2.0.21")
        }
    }
}

fun hasIntermediaryMinecraftClassOwner(bytes: ByteArray): Boolean {
    if (bytes.size < 10 || bytes[0] != 0xCA.toByte() || bytes[1] != 0xFE.toByte()) return false

    fun u1(offset: Int): Int = bytes[offset].toInt() and 0xFF
    fun u2(offset: Int): Int = (u1(offset) shl 8) or u1(offset + 1)
    fun u4(offset: Int): Int = (u2(offset) shl 16) or u2(offset + 2)

    val utf8 = mutableMapOf<Int, String>()
    val classNameIndexes = mutableListOf<Int>()
    var offset = 10
    var index = 1
    val constantPoolCount = u2(8)
    while (index < constantPoolCount && offset < bytes.size) {
        when (u1(offset++)) {
            1 -> {
                val length = u2(offset)
                offset += 2
                utf8[index] = bytes.copyOfRange(offset, offset + length).toString(Charsets.UTF_8)
                offset += length
            }
            3, 4 -> offset += 4
            5, 6 -> {
                offset += 8
                index++
            }
            7 -> {
                classNameIndexes += u2(offset)
                offset += 2
            }
            8, 16, 19, 20 -> offset += 2
            9, 10, 11, 12, 18 -> offset += 4
            15 -> offset += 3
            17 -> offset += 4
            else -> return false
        }
        index++
    }

    return classNameIndexes.any { utf8[it]?.startsWith("net/minecraft/class_") == true }
}

tasks.register("verifyFabricKotlinCompileClasspathSanitized") {
    val compileTask = tasks.named<KotlinCompile>("compileFabricKotlin")

    dependsOn(compileTask)

    doLast {
        val libraries = compileTask.get().libraries.files
        val rawFabricCacheJars = libraries.filter {
            val path = it.invariantSeparatorsPath
            path.contains("/modules-2/files-2.1/") &&
                (
                    path.contains("/net.fabricmc.fabric-api/") ||
                        it.name.contains("intermediary", ignoreCase = true)
                    ) &&
                path.endsWith(".jar")
        }
        val remappedFabricApiJars = libraries.filter {
            val path = it.invariantSeparatorsPath
            path.contains("/transforms/") &&
                it.name.startsWith("fabric-") &&
                it.name.endsWith(".jar")
        }
        val intermediaryClassOwners = rawFabricCacheJars.flatMap { jar ->
            ZipFile(jar).use { zip ->
                zip.entries().asSequence()
                    .filter { it.name.endsWith(".class") }
                    .mapNotNull { entry ->
                        zip.getInputStream(entry).use { input ->
                            if (hasIntermediaryMinecraftClassOwner(input.readBytes())) {
                                "${'$'}{jar.name}!/${'$'}{entry.name}"
                            } else {
                                null
                            }
                        }
                    }
                    .take(20)
                    .toList()
            }
        }

        check(rawFabricCacheJars.isEmpty()) {
            "Raw Fabric/Fabric API jars leaked onto Kotlin compile libraries: ${'$'}rawFabricCacheJars"
        }
        check(remappedFabricApiJars.isNotEmpty()) {
            "Expected remapped Fabric API jars on Kotlin compile libraries; got ${'$'}libraries"
        }
        check(intermediaryClassOwners.isEmpty()) {
            "Raw cache jars still expose intermediary Minecraft owners: ${'$'}intermediaryClassOwners"
        }
    }
}
""".trimIndent()
    )

    val result = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments(
            "verifyFabricKotlinCompileClasspathSanitized",
            "-Dorg.gradle.jvmargs=-Xmx4G -Dfile.encoding=UTF-8",
            "--max-workers=1",
            "--stacktrace",
            "--console=plain",
        )
        .forwardOutput()
        .build()

    assertTrue(result.output.contains("verifyFabricKotlinCompileClasspathSanitized"))
}

@Test
fun `fabric default source intermediary remaps to named output`() {
assertEquals(
            RemapPlan(
                explicitDependencyNamespace = null,
                targetModRemapNamespace = RemapNamespaceAttribute.INTERMEDIARY,
                sourceNamespace = RemapNamespaceAttribute.INTERMEDIARY,
                outputNamespace = MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE,
                rawRemappedAttribute = false,
                remappedAttribute = true,
                usesNoop = false,
            ),
            remapPlan(
                explicitDependencyNamespace = null,
                targetModRemapNamespace = RemapNamespaceAttribute.INTERMEDIARY,
            ),
        )
    }

    @Test
    fun `forge default source searge remaps to named output`() {
        assertEquals(
            RemapPlan(
                explicitDependencyNamespace = null,
                targetModRemapNamespace = RemapNamespaceAttribute.SEARGE,
                sourceNamespace = RemapNamespaceAttribute.SEARGE,
                outputNamespace = MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE,
                rawRemappedAttribute = false,
                remappedAttribute = true,
                usesNoop = false,
            ),
            remapPlan(
                explicitDependencyNamespace = null,
                targetModRemapNamespace = RemapNamespaceAttribute.SEARGE,
            ),
        )
    }

    @Test
    fun `explicit dependency override intermediary on forge target remaps to named output`() {
        assertEquals(
            RemapPlan(
                explicitDependencyNamespace = RemapNamespaceAttribute.INTERMEDIARY,
                targetModRemapNamespace = RemapNamespaceAttribute.SEARGE,
                sourceNamespace = RemapNamespaceAttribute.INTERMEDIARY,
                outputNamespace = MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE,
                rawRemappedAttribute = false,
                remappedAttribute = true,
                usesNoop = false,
            ),
            remapPlan(
                explicitDependencyNamespace = RemapNamespaceAttribute.INTERMEDIARY,
                targetModRemapNamespace = RemapNamespaceAttribute.SEARGE,
            ),
        )
    }

    @Test
    fun `noop normalization is only named to named with remapped output`() {
        val plan = remapPlan(
            explicitDependencyNamespace = MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE,
            targetModRemapNamespace = RemapNamespaceAttribute.SEARGE,
        )

        assertEquals(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE, plan.sourceNamespace)
        assertEquals(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE, plan.outputNamespace)
        assertFalse(plan.rawRemappedAttribute)
        assertTrue(plan.remappedAttribute)
        assertTrue(plan.usesNoop)
    }

    @Test
    fun `modRemapNamespace is never the output namespace`() {
        val plan = remapPlan(
            explicitDependencyNamespace = null,
            targetModRemapNamespace = RemapNamespaceAttribute.SEARGE,
        )

        val rawSourceNamespace = plan.explicitDependencyNamespace ?: plan.targetModRemapNamespace
        val remappedConsumerNamespace = plan.outputNamespace

        assertEquals(RemapNamespaceAttribute.SEARGE, plan.sourceNamespace)
        assertEquals(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE, plan.outputNamespace)
        assertFalse(plan.outputNamespace == RemapNamespaceAttribute.SEARGE)
        assertEquals(RemapNamespaceAttribute.SEARGE, rawSourceNamespace)
        assertEquals(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE, remappedConsumerNamespace)
    }

    @Test
    fun `namespaced mod bucket source namespace still requests named remapped classpath`() {
        val plan = remapPlan(
            explicitDependencyNamespace = RemapNamespaceAttribute.INTERMEDIARY,
            targetModRemapNamespace = RemapNamespaceAttribute.INTERMEDIARY,
        )

        assertEquals(RemapNamespaceAttribute.INTERMEDIARY, plan.sourceNamespace)
        assertEquals(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE, plan.outputNamespace)
        assertTrue(plan.remappedAttribute)
        assertFalse(plan.rawRemappedAttribute)
        assertFalse(plan.outputNamespace == plan.sourceNamespace)
    }

    private fun testMinecraftTarget(project: Project): MinecraftTarget {
        project.plugins.apply("java")

        val minecraftVersion = project.objects.property(String::class.java).convention("1.21")
        val sourceSet = project.extensions.getByType(SourceSetContainer::class.java).maybeCreate("main")

        return Proxy.newProxyInstance(
            MinecraftTarget::class.java.classLoader,
            arrayOf(MinecraftTarget::class.java),
        ) { proxy, method, _ ->
            when (method.name) {
                "getName" -> "test"
                "getFeatureName" -> null
                "getMinecraftVersion" -> minecraftVersion
                "getSourceSet" -> sourceSet
                "getTarget" -> proxy
                else -> error("Unexpected MinecraftTarget call: ${method.name}")
            }
        } as MinecraftTarget
    }

    private fun remapPlan(
        explicitDependencyNamespace: String?,
        targetModRemapNamespace: String,
    ): RemapPlan {
        val sourceNamespace = explicitDependencyNamespace ?: targetModRemapNamespace
        val outputNamespace = MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE

        return RemapPlan(
            explicitDependencyNamespace = explicitDependencyNamespace,
            targetModRemapNamespace = targetModRemapNamespace,
            sourceNamespace = sourceNamespace,
            outputNamespace = outputNamespace,
            rawRemappedAttribute = false,
            remappedAttribute = true,
            usesNoop = sourceNamespace == outputNamespace,
        )
    }

    private data class RemapPlan(
        val explicitDependencyNamespace: String? = null,
        val targetModRemapNamespace: String,
        val sourceNamespace: String,
        val outputNamespace: String,
        val rawRemappedAttribute: Boolean,
        val remappedAttribute: Boolean,
        val usesNoop: Boolean,
    )
}
