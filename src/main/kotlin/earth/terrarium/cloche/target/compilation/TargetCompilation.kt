package earth.terrarium.cloche.target.compilation

import earth.terrarium.cloche.MOD_CLASSPATH_PREFERABLE_ATTRIBUTE
import earth.terrarium.cloche.REMAPPED_ATTRIBUTE
import earth.terrarium.cloche.api.attributes.CompilationAttributes
import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import earth.terrarium.cloche.api.attributes.ModDistribution
import earth.terrarium.cloche.api.target.ClocheTarget
import earth.terrarium.cloche.cloche
import earth.terrarium.cloche.target.MinecraftTargetInternal
import earth.terrarium.cloche.target.addCollectedDependencies
import earth.terrarium.cloche.util.fromJars
import earth.terrarium.cloche.util.optionalDir
import net.msrandom.minecraftcodev.accesswidener.AccessWiden
import net.msrandom.minecraftcodev.core.utils.extension
import net.msrandom.minecraftcodev.core.utils.getGlobalCacheDirectory
import net.msrandom.minecraftcodev.core.utils.isUnobfuscatedVersion
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.decompiler.task.Decompile
import net.msrandom.minecraftcodev.includes.IncludeTargetSource
import net.msrandom.minecraftcodev.includes.IncludesJar
import net.msrandom.minecraftcodev.mixins.task.Mixin
import net.msrandom.minecraftcodev.remapper.MinecraftCodevRemapperPlugin
import net.msrandom.minecraftcodev.remapper.RemapAction
import net.msrandom.minecraftcodev.remapper.task.LoadMappings
import net.msrandom.minecraftcodev.remapper.task.RemapJar
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.*
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.internal.extensions.core.serviceOf
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerTransform
import javax.inject.Inject

internal val REMAP_SCOPE = Attribute.of("earth.terrarium.cloche.remap-scope", String::class.java)
internal const val RAW_REMAP_SCOPE = "raw"

private fun Project.getUnmappedModFiles(configurationName: String) =
    getRawRoleFiles(configurationName, resolvableModConfigurationName(configurationName), unmapped = true)

private fun Project.getNonModFiles(configurationName: String) =
    getRawRoleFiles(configurationName, resolvableNonModConfigurationName(configurationName), unmapped = true)

internal fun Project.getRawRoleFiles(
    configurationName: String,
    filterConfigurationName: String,
    unmapped: Boolean = false,
): FileCollection =
    getRawRoleFiles(
        configurations.named(configurationName),
        provider { configurations.getByName(filterConfigurationName) },
        unmapped,
    )

@Suppress("UNCHECKED_CAST")
internal fun Project.getRawRoleFiles(
    classpath: Provider<out Configuration>,
    role: Provider<out Configuration>,
    unmapped: Boolean = false,
): FileCollection =
    project.files(
        classpath.zip(role) { classpath, role ->
            fun view(configuration: Configuration) =
                configuration.incoming.artifactView {
                    componentFilter { it !is ProjectComponentIdentifier }
                    attributes {
                        classpath.attributes.keySet().forEach { key ->
                            val attribute = key as Attribute<Any>
                            attribute(attribute, classpath.attributes.getAttribute(attribute)!!)
                        }
                        if (unmapped) {
                            attribute(REMAPPED_ATTRIBUTE, false)
                            attribute(REMAP_SCOPE, RAW_REMAP_SCOPE)
                        }
                        attribute(MIXIN_STATE_ATTRIBUTE, RAW_MIXIN_STATE)
                    }
                }
            val selected = view(role)
            val raw = view(classpath)
            // The role's transform may have different dependency inputs; retain C's exact output location.
            project
                .files(
                    raw.artifacts.resolvedArtifacts.zip(selected.artifacts.resolvedArtifacts) { artifacts, selected ->
                        val identities = selected.map { it.id to it.variant }.toSet()
                        artifacts.filter { (it.id to it.variant) in identities }.map { it.file }
                    },
                ).builtBy(raw.files, selected.files)
        },
    )

internal fun Project.getUnmappedClasspath(classpath: Provider<out Configuration>): FileCollection =
    project.files(
        classpath.map { classpath ->
            classpath.incoming
                .artifactView {
                    componentFilter {
                        it !is ProjectComponentIdentifier
                    }

                    attributes {
                        attribute(REMAPPED_ATTRIBUTE, false)
                        attribute(REMAP_SCOPE, RAW_REMAP_SCOPE)
                        attribute(MIXIN_STATE_ATTRIBUTE, RAW_MIXIN_STATE)
                    }
                }.artifacts.artifactFiles
        },
    )

data class FinalJarTasks(
    val accessWidenTask: TaskProvider<AccessWiden>,
    val decompileTask: TaskProvider<Decompile>,
    val runtimeExtraFiles: Provider<List<RegularFile>>,
    val compileMinecraftFiles: Provider<List<RegularFile>>,
    val preapplyMixinsTasks: Provider<List<TaskProvider<Mixin>>>,
) {
    val libraryArtifact: Provider<RegularFile>
        get() = accessWidenTask.flatMap(AccessWiden::outputFile)

    val sourcesArtifact: Provider<RegularFile>
        get() = decompileTask.flatMap(Decompile::outputFile)
}

internal fun Project.registerAccessWidening(
    taskName: String,
    input: Provider<RegularFile>,
    outputDirectory: Provider<Directory>,
    namedSource: Provider<Boolean>,
    remapNamespace: Provider<String>,
    compileFiles: FileCollection,
    explicitWideners: FileCollection,
    remapHelpers: FileCollection,
): TaskProvider<AccessWiden> =
    tasks.register<AccessWiden>(taskName) {
        group = "minecraft-transforms"
        inputFile.set(input)
        namespace.set(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
        this.namedSource.set(namedSource)
        dependsOn(remapNamespace.map { if (it.isEmpty()) emptyList() else listOf(remapHelpers) })
        accessWideners.from(compileFiles, explicitWideners)
        outputFile.set(outputDirectory.zip(input) { dir, file -> dir.file(file.asFile.name) })
    }

internal data class AccessWidenedGames(
    val primaryTask: TaskProvider<AccessWiden>,
    val otherTask: TaskProvider<AccessWiden>?,
    val awGames: GameShards<Provider<RegularFile>>,
)

internal fun Project.registerMinecraftAccessWidening(
    targetName: String?,
    compilationName: String?,
    outputDirectory: Provider<Directory>,
    namedSource: Provider<Boolean>,
    remapNamespace: Provider<String>,
    namedGames: GameShards<Provider<RegularFile>>,
    primary: Provider<GamePart>,
    compileFiles: FileCollection,
    explicitWideners: FileCollection,
    remapHelpers: FileCollection,
): AccessWidenedGames {
    val selected =
        primary.map { part ->
            selectGame(namedGames, part)
            part
        }

    fun widen(
        part: String?,
        input: Provider<RegularFile>,
    ) = registerAccessWidening(
        lowerCamelCaseGradleName("accessWiden", targetName, compilationName, part, "minecraft"),
        input,
        outputDirectory.map { if (part == null) it else it.dir(part) },
        namedSource,
        remapNamespace,
        compileFiles,
        explicitWideners,
        remapHelpers,
    )
    val primaryTask = widen(null, selected.flatMap { selectGame(namedGames, it) })
    val primaryFile = primaryTask.flatMap(AccessWiden::outputFile)
    return when (namedGames) {
        is GameShards.Joined -> {
            AccessWidenedGames(primaryTask, null, GameShards.Joined(primaryFile))
        }

        is GameShards.Split -> {
            val otherTask =
                widen(
                    "other",
                    selected.flatMap {
                        if (it == GamePart.COMMON) namedGames.client else namedGames.common
                    },
                )
            val otherFile = otherTask.flatMap(AccessWiden::outputFile)
            AccessWidenedGames(
                primaryTask,
                otherTask,
                GameShards.Split(
                    selected.flatMap { if (it == GamePart.COMMON) primaryFile else otherFile },
                    selected.flatMap { if (it == GamePart.CLIENT) primaryFile else otherFile },
                ),
            )
        }
    }
}

internal fun Project.registerMinecraftSources(
    taskName: String,
    input: Provider<RegularFile>,
    libraries: FileCollection,
    preparation: FileCollection,
    output: Provider<RegularFile>,
): TaskProvider<Decompile> =
    tasks.register<Decompile>(taskName) {
        group = "sources"
        inputFile.set(input)
        classpath.from(libraries)
        dependsOn(preparation)
        outputFile.set(output)
    }

// The legacy entry reads the already assembled files; it never registers a second chain.
internal fun TargetCompilation<*>.registerCompilationTransformations(info: TargetCompilationInfo<*>): FinalJarTasks {
    require(info === this.info) { "Transformations belong to a different compilation" }
    return compilationTransformations.first
}

// Keep the former constructor/lambda accessors for already compiled consumers.
@JvmSynthetic
internal fun `access$registerCompilationTransformations`(
    compilation: TargetCompilation<*>,
    info: TargetCompilationInfo<*>,
    configuration: NamedDomainObjectProvider<Configuration>,
): Pair<FinalJarTasks, MixinPreview> {
    require(info === compilation.info && configuration === compilation.compileConfiguration) {
        "Transformations belong to a different compilation"
    }
    return compilation.compilationTransformations
}

@JvmSynthetic
internal fun `access$registerCompilationTransformations$files`(
    compilation: TargetCompilation<*>,
    parts: List<Provider<RegularFile>>,
): Provider<List<RegularFile>> =
    compilation.project.objects
        .listProperty(RegularFile::class.java)
        .apply { parts.forEach { add(it) } }

internal fun Project.compilationSourceSet(
    target: ClocheTarget,
    name: String,
): SourceSet {
    val sourceSet = extension<SourceSetContainer>().maybeCreate(sourceSetName(target, name))

    if (sourceSet.localRuntimeConfigurationName !in configurations.names) {
        configurations.dependencyScope(sourceSet.localRuntimeConfigurationName)
    }

    if (sourceSet.localImplementationConfigurationName !in configurations.names) {
        configurations.dependencyScope(sourceSet.localImplementationConfigurationName)
    }

    for (name in listOf(
        sourceSet.externalCompileConfigurationName,
        sourceSet.externalRuntimeConfigurationName,
        sourceSet.externalApiConfigurationName,
    )) {
        if (name !in configurations.names) configurations.dependencyScope(name)
    }

    return sourceSet
}

private fun setupModTransformationPipeline(
    project: Project,
    target: MinecraftTargetInternal,
    compilation: TargetCompilation<*>,
) {
    // afterEvaluate needed as the registration of a transform is dependent on a lazy provider
    //  this can potentially be changed to a no-op transform, but that's far slower
    project.afterEvaluate {
        if (target.modRemapNamespace.get().isEmpty()) {
            return@afterEvaluate
        }

        project.dependencies.registerTransform(RemapAction::class) {
            val extra = target.additionalRemapClasspath
            // Supplemental files belong to the target; ordinary lookup files belong to the compilation.
            val scope = "compilation:${compilation.sourceSet.name}"
            from.attribute(REMAP_SCOPE, RAW_REMAP_SCOPE)
            to.attribute(REMAP_SCOPE, scope)
            // Gradle derives @InputArtifactDependencies from `from`; transitive remap inputs must stay raw too.
            from
                .attribute(MIXIN_STATE_ATTRIBUTE, RAW_MIXIN_STATE)
                .attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                .attribute(REMAPPED_ATTRIBUTE, false)

            to
                .attribute(MIXIN_STATE_ATTRIBUTE, RAW_MIXIN_STATE)
                .attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                .attribute(REMAPPED_ATTRIBUTE, true)

            // TODO Is the usage of the base attributes correct here?
            compilation.baseAttributes(from)
            compilation.baseAttributes(to)

            parameters {
                val compileClasspath =
                    project.getUnmappedClasspath(compilation.compileConfiguration)

                val runtimeClasspath =
                    project.getUnmappedClasspath(project.configurations.named(compilation.sourceSet.runtimeClasspathConfigurationName))

                val nonModCompileClasspath =
                    project.getNonModFiles(compilation.sourceSet.compileClasspathConfigurationName)

                val nonModRuntimeClasspath =
                    project.getNonModFiles(compilation.sourceSet.runtimeClasspathConfigurationName)

                val modCompileClasspath =
                    project.getUnmappedModFiles(compilation.sourceSet.compileClasspathConfigurationName)

                val modRuntimeClasspath =
                    project.getUnmappedModFiles(compilation.sourceSet.runtimeClasspathConfigurationName)

                mappings.set(target.loadMappingsTask.flatMap(LoadMappings::output))

                sourceNamespace.set(target.modRemapNamespace.get())

                extraClasspath.from(compilation.info.intermediaryMinecraftClasspath)
                extraClasspath.from(compileClasspath)
                extraClasspath.from(runtimeClasspath)
                extraClasspath.from(extra)

                cacheDirectory.set(getGlobalCacheDirectory(project))

                nonModFiles.from(nonModCompileClasspath)
                nonModFiles.from(nonModRuntimeClasspath)

                modFiles.from(modCompileClasspath)
                modFiles.from(modRuntimeClasspath)
            }
        }
    }
}

internal open class TargetCompilationInfo<T : MinecraftTargetInternal>(
    val name: String,
    val target: T,
    val intermediaryMinecraftClasspath: FileCollection,
    val namedGames: GameShards<Provider<RegularFile>>,
    primaryGame: Provider<GamePart>,
    val runtimeExtraMinecraftFiles: Map<String, Provider<RegularFile>>,
    val extraClasspathFiles: Provider<List<RegularFile>>,
    val side: Provider<ModDistribution>,
    val data: Boolean,
    val test: Boolean,
    val includeState: IncludeTransformationStateAttribute,
    val includeJarType: Class<out IncludesJar>,
    val preapplicationLibraries: FileCollection,
    val preapplicationSide: Provider<String>,
) {
    val primaryGame: Provider<GamePart> =
        primaryGame.map { part ->
            selectGame(namedGames, part)
            part
        }

}

@Suppress("UnstableApiUsage")
internal abstract class TargetCompilation<T : MinecraftTargetInternal>
    @Inject
    constructor(
        info: TargetCompilationInfo<T>,
    ) : CompilationInternal(), earth.terrarium.cloche.api.target.compilation.MainDependencyScope {
        private val _info = info

        open val info
            get() = _info

        override val target get() = _info.target

        override val isTest get() = _info.test

        final override val sourceSet: SourceSet = project.compilationSourceSet(_info.target, _info.name)

        val modOutputs: ConfigurableFileCollection = project.files(sourceSet.output)

        val compileConfiguration = project.configurations.named(sourceSet.compileClasspathConfigurationName)

        val preapplicationSide: Provider<String> = _info.preapplicationSide

        protected val setupFiles: FinalJarTasks
        internal val mixinPreview: MixinPreview
        internal val compilationConsumption: CompilationConsumption
        internal val compilationTransformations: Pair<FinalJarTasks, MixinPreview>

        init {
            val outputDirectory = target.outputDirectory.map { it.dir(namePath) }
            val namedGames = _info.namedGames
            val namedMinecraftFile = _info.primaryGame.flatMap { selectGame(namedGames, it) }
            val compileFiles = project.files(getRawNonProjectArtifacts(compileConfiguration).map { it.files })
            val namedSource = target.minecraftVersion.map(::isUnobfuscatedVersion)
            val remapHelpers = project.getUnmappedClasspath(project.configurations.named(sourceSet.runtimeClasspathConfigurationName))
            val aw =
                project.registerMinecraftAccessWidening(
                    target.featureName,
                    featureName,
                    outputDirectory,
                    namedSource,
                    target.modRemapNamespace,
                    namedGames,
                    _info.primaryGame,
                    compileFiles,
                    project.files(project.provider { accessWideners }),
                    remapHelpers,
                )
            val primary = aw.primaryTask
            val primaryFile = primary.flatMap(AccessWiden::outputFile)

            fun files(parts: List<Provider<RegularFile>>): Provider<List<RegularFile>> =
                project.objects.listProperty(RegularFile::class.java).apply { parts.forEach { add(it) } }
            // The legacy auxiliary common AW uses target AW, independently of compilation preview inputs.
            val localRuntimeExtra =
                _info.runtimeExtraMinecraftFiles.map { (part, input) ->
                    val directory = target.outputDirectory.map { it.dir(lowerCamelCaseGradleName(featureName, part)) }
                    project
                        .registerAccessWidening(
                            lowerCamelCaseGradleName("accessWiden", target.featureName, featureName, part, "minecraft"),
                            input,
                            directory,
                            namedSource,
                            target.modRemapNamespace,
                            compileFiles,
                            project.files(project.provider { target.accessWideners }),
                            remapHelpers,
                        ).flatMap(AccessWiden::outputFile)
                }
            val runtimeExtra =
                _info.side.flatMap { side ->
                    if (side == ModDistribution.client) {
                        files(localRuntimeExtra).zip(_info.extraClasspathFiles) { local, extra -> local + extra }
                    } else {
                        _info.extraClasspathFiles
                    }
                }

            mixinPreview =
                project.registerMixinPreview(
                    target.featureName,
                    featureName,
                    "${project.path}:${target.name}:$name",
                    outputDirectory,
                    compileConfiguration,
                    project.provider {
                        project.configurations.getByName(
                            resolvableModConfigurationName(sourceSet.compileClasspathConfigurationName),
                        )
                    },
                    project.provider {
                        project.configurations.getByName(
                            resolvableNonModConfigurationName(sourceSet.compileClasspathConfigurationName),
                        )
                    },
                    project.files(
                        getRawNonProjectArtifacts(
                            project.configurations.named(sourceSet.runtimeClasspathConfigurationName),
                        ).map { it.files },
                    ),
                    aw.awGames,
                    preapplicationSide,
                    target.mixinPreapplication,
                    _info.preapplicationLibraries,
                    target.modRemapNamespace,
                    target.loadMappingsTask.flatMap(LoadMappings::output),
                )
            val selected =
                target.mixinPreapplication.map { enabled ->
                    if (enabled) {
                        val games = preapplicationSide.flatMap { files(gamesInCompileOrder(mixinPreview.strippedGames, it)) }
                        MinecraftSelection(
                            games,
                            games,
                            _info.primaryGame.flatMap { selectGame(mixinPreview.strippedGames, it) },
                            project.files(
                                preapplicationSide.map {
                                    if (it ==
                                        "CLIENT"
                                    ) {
                                        _info.preapplicationLibraries
                                    } else {
                                        project.files()
                                    }
                                },
                            ),
                            mixinPreview.operations,
                        )
                    } else {
                        MinecraftSelection(
                            primaryFile.zip(runtimeExtra) { primary, extra -> listOf(primary) + extra },
                            runtimeExtra.zip(primaryFile) { extra, primary -> extra + primary },
                            primaryFile,
                            project.files(),
                            project.provider { emptyList() },
                        )
                    }
                }
            val compileGames = selected.flatMap { it.games }
            val source = selected.flatMap { it.source }
            val artifacts = getNonProjectArtifacts(compileConfiguration)
            val preparation = project.files(artifacts.map { it.files }, compileGames).builtBy(selected.flatMap { it.operations })
            compilationConsumption = CompilationConsumption(artifacts, compileGames, selected.flatMap { it.stubGames }, preparation)
            val sourceLibraries =
                project.files(
                    compileConfiguration,
                    compileGames.zip(source) { games, input -> games.filter { it != input } },
                    selected.map { it.libraries },
                )
            val decompile =
                project.registerMinecraftSources(
                    lowerCamelCaseGradleName("decompile", target.featureName, featureName, "minecraft"),
                    source,
                    sourceLibraries,
                    preparation,
                    outputDirectory.zip(namedMinecraftFile) { dir, file ->
                        dir.file("${file.asFile.nameWithoutExtension}-sources.${file.asFile.extension}")
                    },
                )
            setupFiles = FinalJarTasks(primary, decompile, runtimeExtra, compileGames, mixinPreview.operations)
            compilationTransformations = setupFiles to mixinPreview
        }

        open val metadataDirectory: Provider<Directory>
            @Internal
            get() =
                project.layout.buildDirectory.dir("generated").map {
                    it.dir("metadata").optionalDir(target.featureName).dir(namePath)
                }

        abstract val generateMetadataTask: TaskProvider<out Task>

        val finalMinecraftFile get() =
            setupFiles.libraryArtifact

        val preapplyMixinsTasks get() = setupFiles.preapplyMixinsTasks
        val compileMinecraftFiles get() = setupFiles.compileMinecraftFiles
        val runtimeExtraClasspathFiles get() = setupFiles.runtimeExtraFiles

        val sources get() =
            setupFiles.sourcesArtifact

        val includeBucketConfiguration: NamedDomainObjectProvider<DependencyScopeConfiguration> =
            project.configurations.dependencyScope(lowerCamelCaseGradleName(_info.target.featureName, featureName, "include")) {
                addCollectedDependencies(dependencyHandler.ordinaryInclude)
            }

        private val includeResolvableConfiguration =
            project.configurations.resolvable(
                lowerCamelCaseGradleName(
                    _info.target.featureName,
                    featureName,
                    "includeFiles",
                ),
            ) {
                extendsFrom(includeBucketConfiguration)

                configureIncludeResolution()
            }

        private fun Configuration.configureIncludeResolution() {
            this@TargetCompilation.attributes(attributes)
            attributes
                .attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.LIBRARY))
                .attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.JAVA_RUNTIME))
                .attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objectFactory.named(LibraryElements.JAR))
                .attributeProvider(CompilationAttributes.DISTRIBUTION, _info.side)
                .attributeProvider(CompilationAttributes.CLOCHE_SIDE, _info.side.map(ModDistribution::legacyName))
                .attribute(CompilationAttributes.DATA, _info.data)
            isTransitive = false
        }

        private val includeTargetSources = mutableListOf<Provider<IncludeTargetSource>>()

        init {
            dependencyHandler.includeTargets.all { record ->
                val configuration =
                    project.configurations.resolvable(
                        lowerCamelCaseGradleName(_info.target.featureName, featureName, "includeTarget${includeTargetSources.size}Files"),
                    ) {
                        configureIncludeResolution()
                        dependencies.addLater(record.request.map { it.dependency })
                        dependencyConstraints.addAllLater(includeBucketConfiguration.map { it.allDependencyConstraints })
                    }
                includeTargetSources.add(
                    configuration.zip(record.request) { files, request ->
                        IncludeTargetSource(request.origin, files)
                    },
                )
            }
        }

        val remapJarTask: TaskProvider<RemapJar>? =
            if (!_info.test) {
                project.tasks.register<RemapJar>(
                    lowerCamelCaseGradleName(sourceSet.takeUnless(SourceSet::isMain)?.name, "remapJar"),
                ) {
                    val jarFile = project.tasks.named<Jar>(sourceSet.jarTaskName).flatMap(Jar::getArchiveFile)

                    destinationDirectory.set(project.cloche.intermediateOutputsDirectory)
                    input.set(jarFile)
                    sourceNamespace.set(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
                    targetNamespace.set(target.modRemapNamespace)
                    classpath.from(getRawNonProjectArtifacts(compileConfiguration).map { it.files })
                    classpath.from(
                        compileConfiguration.map { c ->
                            c.incoming.artifactView { componentFilter { it is ProjectComponentIdentifier } }.files
                        },
                    )
                    classpath.from(finalMinecraftFile, runtimeExtraClasspathFiles)

                    mappings.set(target.loadMappingsTask.flatMap(LoadMappings::output))

                    manifest.fromJars(project.serviceOf(), jarFile)
                }
            } else {
                null
            }

        val includeJarTask: TaskProvider<out IncludesJar>? =
            if (!_info.test) {
                project.tasks.register(
                    lowerCamelCaseGradleName(sourceSet.takeUnless(SourceSet::isMain)?.name, "includeJar"),
                    _info.includeJarType,
                ) {
                    destinationDirectory.set(project.cloche.finalOutputsDirectory)

                    val jar = project.tasks.named<Jar>(sourceSet.jarTaskName)
                    val remapped = target.modRemapNamespace.map(String::isNotEmpty)

                    val jarFile =
                        remapped.flatMap {
                            val jarTask =
                                if (it) {
                                    remapJarTask!!
                                } else {
                                    jar
                                }

                            jarTask.flatMap(Jar::getArchiveFile)
                        }

                    input.set(jarFile)
                    manifest.fromJars(project.serviceOf(), jarFile)

                    fromResolutionSources(includeResolvableConfiguration, project.provider { includeTargetSources.map { it.get() } })
                }
            } else {
                null
            }

        val generatedResourceOutputs: ConfigurableFileCollection =
            project.files(target.datagenDirectory, target.datagenClientDirectory, metadataDirectory)

        init {
            setupModTransformationPipeline(project, _info.target, this)

            val remapped = _info.target.modRemapNamespace.map(String::isNotEmpty)
            val scope = project.provider {
                if (remapped.get()) "compilation:${sourceSet.name}" else RAW_REMAP_SCOPE
            }

            val minecraftBuildDependenciesHolder: Configuration =
                project.configurations.detachedConfiguration(
                    project.dependencies.create(
                        project.files().builtBy(_info.intermediaryMinecraftClasspath),
                    ),
                    project.dependencies.create(
                        project.files().builtBy(_info.target.modRemapNamespace.map { namespace ->
                            if (namespace.isEmpty()) emptyList() else listOf(target.additionalRemapClasspath)
                        }),
                    ),
                )

            project.configurations.named(sourceSet.compileClasspathConfigurationName) {
                attributes.attribute(MOD_CLASSPATH_PREFERABLE_ATTRIBUTE, true)
                attributes.attributeProvider(REMAPPED_ATTRIBUTE, remapped)
                attributes.attributeProvider(REMAP_SCOPE, scope)
                attributes.attribute(IncludeTransformationStateAttribute.ATTRIBUTE, _info.includeState)

                extendsFrom(target.mappingsBuildDependenciesHolder, minecraftBuildDependenciesHolder)
            }

            project.configurations.named(sourceSet.runtimeClasspathConfigurationName) {
                attributes.attribute(MOD_CLASSPATH_PREFERABLE_ATTRIBUTE, true)
                attributes.attributeProvider(REMAPPED_ATTRIBUTE, remapped)
                attributes.attributeProvider(REMAP_SCOPE, scope)
                attributes.attribute(IncludeTransformationStateAttribute.ATTRIBUTE, _info.includeState)

                extendsFrom(target.mappingsBuildDependenciesHolder, minecraftBuildDependenciesHolder)
            }

            // Use detached configuration for idea compat
            val minecraftFiles = project.files(finalMinecraftFile, runtimeExtraClasspathFiles)
            val minecraftFileConfiguration =
                project.configurations.detachedConfiguration(project.dependencies.create(minecraftFiles))

            sourceSet.compileClasspath +=
                project.configurations.detachedConfiguration(project.dependencies.create(project.files(compileMinecraftFiles)))
            sourceSet.runtimeClasspath += minecraftFileConfiguration
        }

        override fun getName() = _info.name

        override fun attributes(attributes: AttributeContainer) {
            super.attributes(attributes)

            target.attributes(attributes)

            attributes
                .attributeProvider(CompilationAttributes.DISTRIBUTION, _info.side)
                .attributeProvider(CompilationAttributes.CLOCHE_SIDE, _info.side.map(ModDistribution::legacyName))
                .attribute(CompilationAttributes.DATA, _info.data)
        }
    }
