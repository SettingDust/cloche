package earth.terrarium.cloche.target.forge

import earth.terrarium.cloche.ClochePlugin
import earth.terrarium.cloche.api.metadata.CommonMetadata
import earth.terrarium.cloche.api.metadata.ForgeMetadata
import earth.terrarium.cloche.api.target.ForgeLikeTarget
import earth.terrarium.cloche.api.target.NeoforgeTarget
import earth.terrarium.cloche.target.*
import earth.terrarium.cloche.target.compilation.GamePart
import earth.terrarium.cloche.target.compilation.GameShards
import earth.terrarium.cloche.target.compilation.CompilationInternal
import earth.terrarium.cloche.target.compilation.localImplementationConfigurationName
import earth.terrarium.cloche.tasks.data.MetadataFileProvider
import net.msrandom.minecraftcodev.core.utils.isUnobfuscatedVersion
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.forge.patchesConfigurationName
import net.msrandom.minecraftcodev.forge.task.GenerateAccessTransformer
import net.msrandom.minecraftcodev.forge.task.GenerateForgeClientExtra
import net.msrandom.minecraftcodev.forge.task.ResolvePatchedMinecraft
import net.msrandom.minecraftcodev.remapper.MinecraftCodevRemapperPlugin
import net.msrandom.minecraftcodev.remapper.mappingsConfigurationName
import net.msrandom.minecraftcodev.remapper.task.LoadMappings
import net.msrandom.minecraftcodev.remapper.task.RemapTask
import net.peanuuutz.tomlkt.TomlTable
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.Action
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import javax.inject.Inject

@Suppress("UnstableApiUsage")
internal abstract class ForgeLikeTargetImpl @Inject constructor(name: String) :
    MinecraftTargetInternal(name), ForgeLikeTarget {
    private val game = registerJoinedGame(
        project, name, featureName, loaderName, minecraftVersion, loaderVersion,
        minecraftRemapNamespace, outputDirectory, loadMappingsTask.flatMap(LoadMappings::output),
        this is NeoforgeTarget,
    )
    internal val minecraftLibrariesConfiguration = game.libraries
    private val universal = game.universal
    internal val resolvePatchedMinecraft = game.patched
    internal val generateClientExtra = game.clientExtra
    private val clientExtra = game.implementationClientExtra
    private val primaryGame = providerFactory.provider { GamePart.JOINED }
    private val preapplicationSide = providerFactory.provider { "CLIENT" }

    override val finalJar
        get() = main.includeJarTask!!

    final override val main: ForgeCompilationImpl = objectFactory.newInstance<ForgeCompilationImpl>(
        ForgeCompilationInfo(
            SourceSet.MAIN_SOURCE_SET_NAME,
            this,
            game.intermediary,
            game.named,
            primaryGame,
            preapplicationSide,
            data = false,
            test = false,
            providerFactory = providerFactory,
        ),
    )

    final override val data: LazyConfigurableInternal<ForgeCompilationImpl> = project.lazyConfigurable {
        val data = objectFactory.newInstance<ForgeCompilationImpl>(
            ForgeCompilationInfo(
                ClochePlugin.DATA_COMPILATION_NAME,
                this,
                game.intermediary,
                game.named,
                primaryGame,
                preapplicationSide,
                data = true,
                test = false,
                providerFactory = providerFactory,
            ),
        )

        data.dependencies {
            implementation.add(clientExtra)
        }

        data
    }

    final override val test: LazyConfigurableInternal<ForgeCompilationImpl> = project.lazyConfigurable {
        val test = objectFactory.newInstance<ForgeCompilationImpl>(
            ForgeCompilationInfo(
                SourceSet.TEST_SOURCE_SET_NAME,
                this,
                game.intermediary,
                game.named,
                primaryGame,
                preapplicationSide,
                data = false,
                test = true,
                providerFactory = providerFactory,
            ),
        )

        test.dependencies {
            implementation.add(clientExtra)
        }

        test
    }

    protected abstract val providerFactory: ProviderFactory
        @Inject get

    override val hasSeparateClient: Provider<Boolean> =
        providerFactory.provider { false }

    override val runs = objectFactory.newInstance<ForgeRunConfigurations<out ForgeLikeTargetImpl>>(this)

    override val metadata = objectFactory.newInstance<ForgeMetadata>(this)
    override val legacyClasspath = main.legacyClasspath

    init {
        val userdev = wirePatchedGame(
            project, resolvePatchedMinecraft, minecraftLibrariesConfiguration, universal,
            project.configurations.getByName(sourceSet.patchesConfigurationName), this is NeoforgeTarget, ::forgeDependency,
        )

        metadata.modLoader.convention("javafml")

        minecraftLibrariesConfiguration.shouldResolveConsistentlyWith(project.configurations.getByName(sourceSet.runtimeClasspathConfigurationName))

        project.configurations.named(sourceSet.localImplementationConfigurationName) {
            extendsFrom(minecraftLibrariesConfiguration)
        }

        data.onConfigured {
            project.configurations.named(it.sourceSet.localImplementationConfigurationName) {
                extendsFrom(minecraftLibrariesConfiguration)
            }
        }

        test.onConfigured {
            project.configurations.named(it.sourceSet.localImplementationConfigurationName) {
                extendsFrom(minecraftLibrariesConfiguration)
            }
        }

        main.dependencyHandler.implementation(clientExtra)


        project.dependencies.addProvider(sourceSet.mappingsConfigurationName, userdev)

        registerMappings()
    }

    internal fun loaderVersionRange(version: String): CommonMetadata.VersionRange =
        objectFactory.newInstance<CommonMetadata.VersionRange>().apply {
            start.set(version)
        }

    protected fun forgeDependency(configure: ExternalModuleDependency.() -> Unit): Provider<ExternalModuleDependency> =
        loaderDependency(project, group, artifact, minecraftVersion, loaderVersion, ::version, configure)

    protected abstract fun version(minecraftVersion: String, loaderVersion: String): String

    override fun registerAccessWidenerMergeTask(compilation: CompilationInternal) {
        if (compilation.isTest) {
            return
        }

        val task = project.tasks.register<GenerateAccessTransformer>(
            lowerCamelCaseGradleName("generate", name, compilation.featureName, "accessTransformer"),
        ) {
            input.from(accessWideners)

            val output = project.layout.buildDirectory.dir("generated")
                .map { directory ->
                    directory.dir("accessTransformers").dir(compilation.sourceSet.name).file("accesstransformer.cfg")
                }

            this.output.set(output)
        }

        project.tasks.named<Jar>(compilation.sourceSet.jarTaskName) {
            from(task.flatMap(GenerateAccessTransformer::output)) {
                into("META-INF")
            }
        }
    }

    override fun addAnnotationProcessors(compilation: CompilationInternal) {
        project.configurations.named(compilation.sourceSet.annotationProcessorConfigurationName) {
            extendsFrom(minecraftLibrariesConfiguration)
        }

        // TODO Add forge mixin arguments
    }

    override fun onClientIncluded(action: () -> Unit) {
        action()
    }

    override fun withMetadataToml(action: Action<MetadataFileProvider<TomlTable>>) = main.withMetadataToml(action)
}

internal data class JoinedGame(
    val libraries: Configuration,
    val universal: Configuration,
    val patched: TaskProvider<ResolvePatchedMinecraft>,
    val clientExtra: TaskProvider<GenerateForgeClientExtra>,
    val intermediary: FileCollection,
    val named: GameShards.Joined<Provider<RegularFile>>,
    val implementationClientExtra: FileCollection,
)

internal fun registerJoinedGame(
    project: Project,
    name: String,
    featureName: String?,
    loaderName: String,
    minecraftVersion: Provider<String>,
    loaderVersion: Provider<String>,
    minecraftRemapNamespace: Provider<String>,
    outputDirectory: Provider<Directory>,
    mappings: Provider<RegularFile>,
    neoforge: Boolean,
): JoinedGame {
    fun output(suffix: Provider<String>) = suffix.flatMap { suffix ->
        outputDirectory.zip(minecraftVersion.zip(loaderVersion) { a, b -> "$a-$b" }) { dir, version ->
            dir.file("$loaderName-$version-$suffix.jar")
        }
    }

    val (libraries, universal, patched) = registerPatchedGame(project, featureName, minecraftVersion,
        output(minecraftRemapNamespace.map { it.ifEmpty { MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE } }))
    val clientExtra = project.tasks.register<GenerateForgeClientExtra>(
        lowerCamelCaseGradleName("generate", featureName, "clientExtra"),
    ) {
        group = "minecraft-resolution"
        this.minecraftVersion.set(minecraftVersion)
        outputFile.set(output(project.provider { "client-extra" }))
        this.neoforge.set(neoforge)
    }
    val remap = project.tasks.register<RemapTask>(lowerCamelCaseGradleName("remap", name, "minecraftNamed")) {
        group = "minecraft-transforms"
        inputFile.set(patched.flatMap(ResolvePatchedMinecraft::output))
        classpath.from(libraries)
        this.mappings.set(mappings)
        sourceNamespace.set(minecraftRemapNamespace)
        outputFile.set(output(project.provider { MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE }))
    }
    val named = minecraftRemapNamespace.flatMap {
        if (it.isEmpty()) patched.flatMap(ResolvePatchedMinecraft::output) else remap.flatMap(RemapTask::outputFile)
    }
    val implementationClientExtra = project.files(minecraftVersion.map {
        if (isUnobfuscatedVersion(it)) emptyList() else listOf(clientExtra.flatMap(GenerateForgeClientExtra::outputFile))
    })
    return JoinedGame(
        libraries, universal, patched, clientExtra,
        project.files(patched.flatMap(ResolvePatchedMinecraft::output)),
        GameShards.Joined(named), implementationClientExtra,
    )
}
