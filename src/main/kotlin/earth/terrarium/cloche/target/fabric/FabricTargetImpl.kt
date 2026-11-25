package earth.terrarium.cloche.target.fabric

import earth.terrarium.cloche.ClochePlugin
import org.gradle.api.file.FileCollection
import org.gradle.api.file.Directory
import org.gradle.api.artifacts.Configuration
import org.gradle.api.Project
import earth.terrarium.cloche.target.compilation.GamePart
import earth.terrarium.cloche.target.compilation.GameShards
import earth.terrarium.cloche.api.metadata.FabricMetadata
import earth.terrarium.cloche.api.target.FabricTarget
import earth.terrarium.cloche.api.target.GameJars
import earth.terrarium.cloche.api.target.compilation.FabricIncludedClient
import earth.terrarium.cloche.modId
import earth.terrarium.cloche.target.compilation.CompilationInternal
import earth.terrarium.cloche.target.LazyConfigurableInternal
import earth.terrarium.cloche.target.MinecraftTargetInternal
import earth.terrarium.cloche.target.compilation.compilationSourceSet
import earth.terrarium.cloche.target.lazyConfigurable
import earth.terrarium.cloche.target.compilation.localImplementationConfigurationName
import earth.terrarium.cloche.tasks.data.MetadataFileProvider
import earth.terrarium.cloche.util.fromJars
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import net.msrandom.minecraftcodev.accesswidener.AccessWiden
import net.msrandom.minecraftcodev.core.MinecraftCodevPlugin.Companion.json
import net.msrandom.minecraftcodev.core.MinecraftComponentMetadataRule
import net.msrandom.minecraftcodev.core.MinecraftOperatingSystemAttribute
import net.msrandom.minecraftcodev.core.VERSION_MANIFEST_URL
import net.msrandom.minecraftcodev.core.operatingSystemName
import net.msrandom.minecraftcodev.core.task.ResolveMinecraftClient
import net.msrandom.minecraftcodev.core.task.ResolveMinecraftCommon
import net.msrandom.minecraftcodev.core.utils.getGlobalCacheDirectory
import net.msrandom.minecraftcodev.core.utils.isUnobfuscatedVersion
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.core.utils.zipFileSystem
import net.msrandom.minecraftcodev.fabric.MinecraftCodevFabricPlugin
import net.msrandom.minecraftcodev.fabric.task.MergeAccessWideners
import net.msrandom.minecraftcodev.remapper.MinecraftCodevRemapperPlugin
import net.msrandom.minecraftcodev.remapper.task.LoadMappings
import net.msrandom.minecraftcodev.remapper.task.RemapTask
import net.msrandom.minecraftcodev.runs.task.WriteClasspathFile
import org.gradle.api.Action
import org.gradle.api.InvalidUserCodeException
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.extensions.core.serviceOf
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withModule
import org.gradle.process.CommandLineArgumentProvider
import java.io.File
import java.util.jar.JarFile
import javax.inject.Inject
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Suppress("UnstableApiUsage")
internal abstract class FabricTargetImpl @Inject constructor(name: String) :
    MinecraftTargetInternal(name),
    FabricTarget {
    private val game = registerFabricGame(
        project, name, featureName, minecraftVersion, minecraftRemapNamespace, outputDirectory,
        loadMappingsTask.flatMap(LoadMappings::output),
    )
    override val gameJars get() = game.gameJars
    private val commonLibrariesConfiguration = game.commonLibraries
    private val clientLibrariesConfiguration = game.clientLibraries
    private val intermediaryClasspath = game.intermediary
    private val commonJar = game.named.common
    private val generateMappingsArtifact = game.mappingsArtifact
    val writeRemapClasspathTask: TaskProvider<WriteClasspathFile> = game.remapClasspath
    internal val preapplicationClientLibraries get() = project.files(commonLibrariesConfiguration, clientLibrariesConfiguration)

    override val finalJar: Provider<out Jar>
        get() = client.value.flatMap {
            it.includeJarTask!!
        }.orElse(main.includeJarTask!!)

    final override val main: FabricCompilationImpl

    final override val client: LazyConfigurableInternal<FabricClientSecondarySourceSets> = project.lazyConfigurable {
        if (includedClient.isConfiguredValue) {
            throw InvalidUserCodeException("Used `client()` in $name after previously using `includedClient()`")
        }

        val client =
            project.objects.newInstance<FabricClientSecondarySourceSets>(
                FabricCompilationInfo(
                    ClochePlugin.CLIENT_COMPILATION_NAME,
                    this,
                    intermediaryClasspath,
                    game.named,
                    emptyMap(),
                    project.objects.listProperty(RegularFile::class.java).apply { add(main.finalMinecraftFile) },
                    data = false,
                    test = false,
                    primaryGame = providerFactory.provider { GamePart.CLIENT },
                    preapplicationSide = providerFactory.provider { "CLIENT" },
                    preapplicationLibraries = preapplicationClientLibraries,
                ),
            )

        clientLibrariesConfiguration.shouldResolveConsistentlyWith(project.configurations.getByName(client.sourceSet.runtimeClasspathConfigurationName))

        project.configurations.named(client.sourceSet.localImplementationConfigurationName) {
            extendsFrom(clientLibrariesConfiguration)
        }

        client.data.onConfigured {
            project.configurations.named(it.sourceSet.localImplementationConfigurationName) {
                extendsFrom(clientLibrariesConfiguration)
            }
        }

        client.test.onConfigured {
            project.configurations.named(it.sourceSet.localImplementationConfigurationName) {
                extendsFrom(clientLibrariesConfiguration)
            }
        }

        main.generateMetadataTask.configure {
            clientMixinConfigs.from(client.mixins)
        }

        client
    }

    // TODO Use FabricIncludedClient::mixins
    final override val includedClient: LazyConfigurableInternal<FabricIncludedClient> = project.lazyConfigurable {
        if (client.isConfiguredValue) {
            throw InvalidUserCodeException("Used 'includedClient' in target $name after already configuring client compilation")
        }

        clientLibrariesConfiguration.shouldResolveConsistentlyWith(project.configurations.getByName(sourceSet.runtimeClasspathConfigurationName))

        project.configurations.named(sourceSet.localImplementationConfigurationName) {
            extendsFrom(clientLibrariesConfiguration)
        }

        data.onConfigured {
            project.configurations.named(it.sourceSet.localImplementationConfigurationName) {
                extendsFrom(clientLibrariesConfiguration)
            }
        }

        test.onConfigured {
            project.configurations.named(it.sourceSet.localImplementationConfigurationName) {
                extendsFrom(clientLibrariesConfiguration)
            }
        }

        project.objects.newInstance<FabricIncludedClient>()
    }

    final override val data: LazyConfigurableInternal<FabricCompilationImpl> = project.lazyConfigurable {
        registerCommonCompilation(ClochePlugin.DATA_COMPILATION_NAME)
    }

    final override val test: LazyConfigurableInternal<FabricCompilationImpl> = project.lazyConfigurable {
        registerCommonCompilation(SourceSet.TEST_SOURCE_SET_NAME)
    }

    override val metadata = project.objects.newInstance<FabricMetadata>(this)

    protected abstract val providerFactory: ProviderFactory
        @Inject get

    override val minecraftRemapNamespace: Provider<String>
        get() = minecraftVersion.zip(mappings.isDefault) { version, isDefault ->
            if (isDefault && isUnobfuscatedVersion(version)) {
                ""
            } else {
                MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE
            }
        }

    override val modRemapNamespace: Provider<String>
        get() = minecraftVersion.zip(mappings.isOfficialCompatible) { version, isOfficialCompatible ->
            if (isOfficialCompatible && isUnobfuscatedVersion(version)) {
                ""
            } else {
                MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE
            }
        }

    override val hasSeparateClient = client.isConfigured

    override val runs = project.objects.newInstance<FabricRunConfigurations>(this)

    private val mainTargetMinecraftName = featureName ?: SourceSet.MAIN_SOURCE_SET_NAME
    private val clientTargetMinecraftName = lowerCamelCaseGradleName(featureName, "client")

    private val fabricLoader = loaderVersion.map {
        project.dependencyFactory.create("net.fabricmc", "fabric-loader", it)
    }

    init {
        val commonStub =
            project.dependencies.enforcedPlatform(ClochePlugin.STUB_DEPENDENCY) as ExternalModuleDependency

        val clientStub =
            project.dependencies.enforcedPlatform(ClochePlugin.STUB_DEPENDENCY) as ExternalModuleDependency

        project.dependencies.add(commonLibrariesConfiguration.name, commonStub.apply {
            capabilities {
                requireCapability("net.msrandom:$mainTargetMinecraftName")
            }
        })

        project.dependencies.add(clientLibrariesConfiguration.name, clientStub.apply {
            capabilities {
                requireCapability("net.msrandom:$clientTargetMinecraftName")
            }
        })

        main = registerCommonCompilation(SourceSet.MAIN_SOURCE_SET_NAME)

        project.dependencies.add(sourceSet.runtimeOnlyConfigurationName, project.files(generateMappingsArtifact.flatMap(Zip::getArchiveFile)))

        commonLibrariesConfiguration.shouldResolveConsistentlyWith(project.configurations.getByName(sourceSet.runtimeClasspathConfigurationName))

        project.configurations.named(sourceSet.localImplementationConfigurationName) {
            extendsFrom(commonLibrariesConfiguration)
        }

        data.onConfigured {
            project.configurations.named(it.sourceSet.localImplementationConfigurationName) {
                extendsFrom(commonLibrariesConfiguration)
            }
        }

        test.onConfigured {
            project.configurations.named(it.sourceSet.localImplementationConfigurationName) {
                extendsFrom(commonLibrariesConfiguration)
            }
        }

        mappings.fabricIntermediary()

        registerMappings()

        // afterEvaluate needed because of the component rules using providers
        project.afterEvaluate {
            dependencies.components {
                withModule(
                    ClochePlugin.STUB_MODULE,
                    MinecraftComponentMetadataRule::class,
                ) {
                    params(
                        getGlobalCacheDirectory(project),
                        minecraftVersion.get(),
                        VERSION_MANIFEST_URL,
                        project.gradle.startParameter.isOffline,
                        mainTargetMinecraftName,
                        clientTargetMinecraftName,
                    )
                }
            }
        }

        main.dependencies {
            api.add(fabricLoader)
        }
    }

    private fun registerCommonCompilation(name: String): FabricCompilationImpl =
        project.objects.newInstance<FabricCompilationImpl>(
            FabricCompilationInfo(
                name,
                this,
                intermediaryClasspath,
                game.named,
                mapOf("common" to commonJar),
                providerFactory.provider { emptyList() },
                name == ClochePlugin.DATA_COMPILATION_NAME,
                name == SourceSet.TEST_SOURCE_SET_NAME,
                includedClient.isConfigured.map { if (it) GamePart.CLIENT else GamePart.COMMON },
                providerFactory.provider { hasSeparateClient }.flatMap { it }.map { if (it) "SERVER" else "CLIENT" },
                preapplicationClientLibraries,
            ),
        )

    override fun registerAccessWidenerMergeTask(compilation: CompilationInternal) {
        if (compilation.isTest) {
            return
        }

        val modId = project.modId

        val task = project.tasks.register<MergeAccessWideners>(
            lowerCamelCaseGradleName("merge", name, compilation.featureName, "accessWideners"),
        ) {
            input.from(accessWideners)
            accessWidenerName.set(modId)

            namedSource.set(minecraftVersion.map(::isUnobfuscatedVersion))

            val output = modId.zip(project.layout.buildDirectory.dir("generated")) { modId, directory ->
                directory.dir("mergedAccessWideners").dir(compilation.sourceSet.name).file("$modId.accessWidener")
            }

            this.output.set(output)
        }

        project.tasks.named<Jar>(compilation.sourceSet.jarTaskName) {
            from(task.flatMap(MergeAccessWideners::output))

            doLast {
                this as Jar

                zipFileSystem(archiveFile.get().asFile.toPath()).use {
                    val accessWidenerFileName = "${modId.get()}.accessWidener"
                    val accessWidenerPath = it.getPath(accessWidenerFileName)
                    val modJsonPath = it.getPath(MinecraftCodevFabricPlugin.MOD_JSON)

                    if (!accessWidenerPath.exists() || !modJsonPath.exists()) {
                        return@use
                    }

                    val metadata: JsonObject = modJsonPath.inputStream().use(json::decodeFromStream)

                    val accessWidenerElement = metadata["accessWidener"]

                    if (accessWidenerElement != null && accessWidenerElement !is JsonNull) {
                        return@use
                    }

                    val newMetadata = JsonObject(metadata + ("accessWidener" to JsonPrimitive(accessWidenerFileName)))

                    modJsonPath.outputStream().use {
                        json.encodeToStream(newMetadata, it)
                    }
                }
            }
        }
    }

    override fun addJarInjects(compilation: CompilationInternal) {
        project.tasks.named<Jar>(compilation.sourceSet.jarTaskName) {
            manifest {
                attributes["Fabric-Loom-Mixin-Remap-Type"] = "static"
            }
        }

        if (compilation.name == ClochePlugin.CLIENT_COMPILATION_NAME) {
            val mainJarTask = project.tasks.named<Jar>(main.sourceSet.jarTaskName)

            project.tasks.named<Jar>(compilation.sourceSet.jarTaskName) {
                val mainJarFile = mainJarTask.flatMap(Jar::getArchiveFile)

                from(project.zipTree(mainJarFile)) {
                    exclude(JarFile.MANIFEST_NAME)
                }

                manifest.fromJars(project.serviceOf(), mainJarFile)
            }
        }
    }

    override fun addAnnotationProcessors(compilation: CompilationInternal) {
        compilation.dependencyHandler.annotationProcessor.add(fabricLoader)
        compilation.dependencyHandler.annotationProcessor.add(project.files(generateMappingsArtifact.flatMap(Zip::getArchiveFile)))
        compilation.dependencyHandler.annotationProcessor.add(project.dependencyFactory.create("net.fabricmc", "fabric-mixin-compile-extensions", "0.6.0"))

        project.tasks.named<JavaCompile>(compilation.sourceSet.compileJavaTaskName) {
            val inMapFile = lowerCamelCaseGradleName(
                "inMapFile",
                MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE,
                MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE
            )

            val inMapFileArgument = loadMappingsTask.flatMap(LoadMappings::output).map {
                "-A$inMapFile=$it"
            }

            options.compilerArgumentProviders.add(CommandLineArgumentProvider {
                listOf(
                    inMapFileArgument.get(),
                    "-AdefaultObfuscationEnv=${MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE}:${MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE}",
                )
            })
        }
    }

    override fun onClientIncluded(action: () -> Unit) = includedClient.onConfigured {
        action()
    }

    override fun withMetadataJson(action: Action<MetadataFileProvider<JsonObject>>) = main.withMetadataJson(action)
}

internal data class FabricGame(
    val named: GameShards.Split<Provider<RegularFile>>,
    val gameJars: GameJars.Split,
    val intermediary: FileCollection,
    val commonLibraries: Configuration,
    val clientLibraries: Configuration,
    val mappingsArtifact: TaskProvider<Zip>,
    val remapClasspath: TaskProvider<WriteClasspathFile>,
)

internal fun registerFabricGame(
    project: Project,
    name: String,
    featureName: String?,
    minecraftVersion: Provider<String>,
    minecraftRemapNamespace: Provider<String>,
    outputDirectory: Provider<Directory>,
    mappingFile: Provider<RegularFile>,
): FabricGame {
    fun output(suffix: String? = null) =
        outputDirectory.zip(minecraftVersion) { dir, version ->
            dir.file("minecraft-$version" + (suffix?.let { "-$it" } ?: "") + ".jar")
        }

    val commonLibrariesConfiguration =
        project.configurations.create(lowerCamelCaseGradleName(featureName, "commonMinecraftLibraries")) {
            isCanBeConsumed = false

            attributes.attribute(MinecraftOperatingSystemAttribute.attribute, project.objects.named(operatingSystemName()))
        }

    val clientLibrariesConfiguration =
        project.configurations.create(lowerCamelCaseGradleName(featureName, "clientMinecraftLibraries")) {
            isCanBeConsumed = false

            attributes.attribute(MinecraftOperatingSystemAttribute.attribute, project.objects.named(operatingSystemName()))
        }

    val resolveCommonMinecraft =
        project.tasks.register<ResolveMinecraftCommon>(
            lowerCamelCaseGradleName("resolve", name, "common"),
        ) {
            group = "minecraft-resolution"

            this.minecraftVersion.set(minecraftVersion)

            output.set(
                minecraftRemapNamespace.flatMap {
                    if (it.isEmpty()) {
                        output()
                    } else {
                        output("obf")
                    }
                },
            )
        }

    val resolveClientMinecraft =
        project.tasks.register<ResolveMinecraftClient>(
            lowerCamelCaseGradleName("resolve", name, "client"),
        ) {
            group = "minecraft-resolution"

            this.minecraftVersion.set(minecraftVersion)

            output.set(
                minecraftRemapNamespace.flatMap {
                    if (it.isEmpty()) {
                        output("client")
                    } else {
                        output("client-obf")
                    }
                },
            )
        }

    val remapCommonMinecraftIntermediary =
        project.tasks.register<RemapTask>(
            lowerCamelCaseGradleName(
                "remap",
                name,
                "commonMinecraft",
                MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE,
            ),
        ) {
            group = "minecraft-transforms"

            inputFile.set(resolveCommonMinecraft.flatMap(ResolveMinecraftCommon::output))
            sourceNamespace.set("obf")
            targetNamespace.set(minecraftRemapNamespace)

            classpath.from(commonLibrariesConfiguration)

            mappings.set(mappingFile)

            outputFile.set(output(MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE))
        }

    val remapClientMinecraftIntermediary =
        project.tasks.register<RemapTask>(
            lowerCamelCaseGradleName(
                "remap",
                name,
                "clientMinecraft",
                MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE,
            ),
        ) {
            group = "minecraft-transforms"

            inputFile.set(resolveClientMinecraft.flatMap(ResolveMinecraftClient::output))
            sourceNamespace.set("obf")
            targetNamespace.set(minecraftRemapNamespace)

            classpath.from(commonLibrariesConfiguration)
            classpath.from(clientLibrariesConfiguration)
            classpath.from(resolveCommonMinecraft.flatMap(ResolveMinecraftCommon::output))

            mappings.set(mappingFile)

            outputFile.set(output("client-${MinecraftCodevFabricPlugin.INTERMEDIARY_MAPPINGS_NAMESPACE}"))
        }

    val remapCommon =
        project.tasks.register<RemapTask>(
            lowerCamelCaseGradleName("remap", featureName, "commonMinecraftNamed"),
        ) {
            group = "minecraft-transforms"

            inputFile.set(remapCommonMinecraftIntermediary.flatMap(RemapTask::outputFile))

            classpath.from(commonLibrariesConfiguration)

            mappings.set(mappingFile)

            sourceNamespace.set(minecraftRemapNamespace)

            outputFile.set(output(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE))
        }

    val remapClient =
        project.tasks.register<RemapTask>(
            lowerCamelCaseGradleName("remap", featureName, "clientMinecraftNamed"),
        ) {
            group = "minecraft-transforms"

            inputFile.set(remapClientMinecraftIntermediary.flatMap(RemapTask::outputFile))

            classpath.from(commonLibrariesConfiguration)
            classpath.from(clientLibrariesConfiguration)
            classpath.from(remapCommonMinecraftIntermediary.flatMap(RemapTask::outputFile))

            mappings.set(mappingFile)

            sourceNamespace.set(minecraftRemapNamespace)

            outputFile.set(output("client-${MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE}"))
        }

    val generateMappingsArtifact =
        project.tasks.register<Zip>(
            lowerCamelCaseGradleName("generate", featureName, "mappingsArtifact"),
        ) {
            destinationDirectory.set(temporaryDir)
            archiveBaseName.set(listOfNotNull(featureName, "mappings").joinToString("-"))
            archiveVersion.set(null as String?)

            val fileList =
                minecraftRemapNamespace.map {
                    if (it.isEmpty()) {
                        emptyList()
                    } else {
                        listOf(mappingFile)
                    }
                }

            from(fileList) {
                into("mappings")
            }
        }

    val writeRemapClasspathTask: TaskProvider<WriteClasspathFile> =
        project.tasks.register<WriteClasspathFile>(
            lowerCamelCaseGradleName("write", featureName, "remapClasspath"),
        ) {
            val classpaths =
                minecraftRemapNamespace.map {
                    if (it.isEmpty()) {
                        emptyList()
                    } else {
                        listOf(
                            commonLibrariesConfiguration,
                            clientLibrariesConfiguration,
                            remapCommonMinecraftIntermediary.flatMap(RemapTask::outputFile),
                        )
                    }
                }

            classpath.from(classpaths)
            separator.set(File.pathSeparator)
        }

    val commonJar =
        minecraftRemapNamespace.flatMap {
            if (it.isEmpty()) {
                resolveCommonMinecraft.flatMap(ResolveMinecraftCommon::output)
            } else {
                remapCommon.flatMap(RemapTask::outputFile)
            }
        }

    val clientJar =
        minecraftRemapNamespace.flatMap {
            if (it.isEmpty()) {
                resolveClientMinecraft.flatMap(ResolveMinecraftClient::output)
            } else {
                remapClient.flatMap(RemapTask::outputFile)
            }
        }

    val intermediaryClasspath =
        project.files(
            minecraftRemapNamespace.map {
                if (it.isEmpty()) {
                    emptyList()
                } else {
                    // TODO Once client splitting is done, we might not always need the client Jar
                    listOf(
                        remapCommonMinecraftIntermediary.flatMap(RemapTask::outputFile),
                        remapClientMinecraftIntermediary.flatMap(RemapTask::outputFile),
                    )
                }
            },
        )

    return FabricGame(
        GameShards.Split(commonJar, clientJar),
        GameJars.Split(
            minecraftRemapNamespace.flatMap { namespace ->
                if (namespace.isEmpty()) resolveCommonMinecraft.flatMap(ResolveMinecraftCommon::output)
                else remapCommonMinecraftIntermediary.flatMap(RemapTask::outputFile)
            },
            minecraftRemapNamespace.flatMap { namespace ->
                if (namespace.isEmpty()) resolveClientMinecraft.flatMap(ResolveMinecraftClient::output)
                else remapClientMinecraftIntermediary.flatMap(RemapTask::outputFile)
            },
        ),
        intermediaryClasspath,
        commonLibrariesConfiguration,
        clientLibrariesConfiguration,
        generateMappingsArtifact,
        writeRemapClasspathTask,
    )
}
