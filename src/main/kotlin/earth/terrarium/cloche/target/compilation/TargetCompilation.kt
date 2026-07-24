package earth.terrarium.cloche.target.compilation

import earth.terrarium.cloche.*
import earth.terrarium.cloche.ClochePlugin.Companion.KOTLIN_JVM_PLUGIN_ID
import earth.terrarium.cloche.MOD_CLASSPATH_PREFERABLE_ATTRIBUTE
import earth.terrarium.cloche.REMAPPED_ATTRIBUTE
import earth.terrarium.cloche.api.attributes.CompilationAttributes
import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import earth.terrarium.cloche.api.attributes.ModDistribution
import earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute
import earth.terrarium.cloche.api.target.ClocheTarget
import earth.terrarium.cloche.remap.ClocheClasspathKind
import earth.terrarium.cloche.remap.ClocheClasspathNormalizationKind
import earth.terrarium.cloche.remap.ClocheRemapAction
import earth.terrarium.cloche.remap.ClasspathNormalizationResult
import earth.terrarium.cloche.remap.isRemappableModArtifactCandidate
import earth.terrarium.cloche.remap.RemapBucketReport
import earth.terrarium.cloche.remap.RemapDeclarationReport
import earth.terrarium.cloche.remap.normalizeModClasspath
import earth.terrarium.cloche.remap.remapReportFromDecisions
import earth.terrarium.cloche.remap.ResolvedModArtifact
import earth.terrarium.cloche.remap.safeGradlePathSegment
import earth.terrarium.cloche.remap.toResolvedModArtifact
import earth.terrarium.cloche.target.MinecraftTargetInternal
import earth.terrarium.cloche.target.addCollectedDependencies
import earth.terrarium.cloche.tasks.ClocheRemapReport
import earth.terrarium.cloche.util.fromJars
import earth.terrarium.cloche.util.optionalDir
import net.msrandom.minecraftcodev.core.VERSION_MANIFEST_URL
import net.msrandom.minecraftcodev.accesswidener.AccessWiden
import net.msrandom.minecraftcodev.core.utils.extension
import net.msrandom.minecraftcodev.core.utils.getGlobalCacheDirectory
import net.msrandom.minecraftcodev.core.utils.isUnobfuscatedVersion
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.decompiler.task.Decompile
import net.msrandom.minecraftcodev.includes.IncludesJar
import net.msrandom.minecraftcodev.mixins.task.Mixin
import net.msrandom.minecraftcodev.remapper.MinecraftCodevRemapperPlugin
import net.msrandom.minecraftcodev.remapper.mappingsConfigurationName
import net.msrandom.minecraftcodev.remapper.task.LoadMappings
import net.msrandom.minecraftcodev.remapper.task.RemapJar
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencyScopeConfiguration
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.*
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
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
import org.gradle.api.tasks.compile.JavaCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.spongepowered.asm.mixin.MixinEnvironment
import java.io.File
import javax.inject.Inject

private fun Project.getUnmappedModFiles(
    configurationName: String,
    attributeAction: AttributeContainer.() -> Unit = {},
): FileCollection = files(provider {
    val classpath = configurations.findByName(resolvableModConfigurationName(configurationName)) ?: return@provider emptySet<File>()

    classpath.incoming.artifactView {
        componentFilter {
            it !is ProjectComponentIdentifier
        }

        attributes {
            attribute(REMAPPED_ATTRIBUTE, false)
            attributeAction()
        }
    }.artifacts.artifactFiles.files
})

private fun Project.getNonModFiles(
    configurationName: String,
    attributeAction: AttributeContainer.() -> Unit = {},
) =
    files(
        filteredClasspathFiles(
            configurationName,
            resolvableNonModConfigurationName(configurationName),
            excludedConfigurationNames = rawModProvenanceConfigurationNames(configurationName),
            attributeAction = attributeAction,
        ),
        retainedNonModFilesFromModProvenance(configurationName, attributeAction),
    )

private fun rawModProvenanceConfigurationNames(configurationName: String) =
    listOf(resolvableModConfigurationName(configurationName)) +
        supportedRemapNamespaces.map { namespace ->
            namespacedResolvableModConfigurationName(configurationName, namespace)
        }

private fun Project.retainedNonModFilesFromModProvenance(
    configurationName: String,
    attributeAction: AttributeContainer.() -> Unit = {},
): FileCollection = files(provider {
    rawModProvenanceConfigurationNames(configurationName)
        .mapNotNull(configurations::findByName)
        .flatMap { configuration ->
            configuration.incoming.artifactView {
                componentFilter { it !is ProjectComponentIdentifier }
                attributes {
                    attribute(REMAPPED_ATTRIBUTE, false)
                    attributeAction()
                }
            }.artifacts.artifactFiles.files
        }
        .filterNot(File::isRemappableModArtifactCandidate)
        .map(File::getCanonicalFile)
        .toSet()
})

private fun Project.filteredClasspathFiles(
    configurationName: String,
    filterConfigurationName: String,
    excludedConfigurationNames: List<String> = emptyList(),
    attributeAction: AttributeContainer.() -> Unit = {},
    componentPredicate: (ComponentIdentifier) -> Boolean = { true },
): FileCollection {
    return project.files(project.provider {
        val classpath = project.configurations.findByName(configurationName) ?: return@provider emptySet<File>()
        val dependencies = project.configurations.findByName(filterConfigurationName) ?: return@provider emptySet<File>()
        val resolutionResult = dependencies.incoming.resolutionResult

        val componentIdentifiers =
            resolutionResult.allComponents.map(ResolvedComponentResult::getId) - resolutionResult.root.id

        val excludedConfigurations = excludedConfigurationNames.mapNotNull(project.configurations::findByName)
        val excludedFiles = excludedConfigurations
            .flatMap { excludedConfiguration ->
                excludedConfiguration.incoming.artifactView {
                    componentFilter { it !is ProjectComponentIdentifier }

                    attributes {
                        attribute(REMAPPED_ATTRIBUTE, false)
                        attributeAction()
                    }
                }.artifacts.artifactFiles.files
            }
            .filter(File::isRemappableModArtifactCandidate)
            .map(File::getCanonicalFile)
            .toSet()

        val filteredIdentifiers = componentIdentifiers.filter { it !is ProjectComponentIdentifier }

        classpath.incoming.artifactView {
            componentFilter { it in filteredIdentifiers && componentPredicate(it) }

            attributes {
                attribute(REMAPPED_ATTRIBUTE, false)
                attributeAction()
            }
        }.artifacts.artifactFiles.files.filter { it.canonicalFile !in excludedFiles }
    })
}

private fun Project.getUnmappedClasspath(
    configurationName: String,
    attributeAction: AttributeContainer.() -> Unit = {},
): FileCollection {
    val classpath = project.configurations.named(configurationName)

    return project.files(classpath.map { classpath ->
        classpath.incoming.artifactView {
            componentFilter {
                it !is ProjectComponentIdentifier
            }

            attributes {
                attribute(REMAPPED_ATTRIBUTE, false)
                attributeAction()
            }
        }.artifacts.artifactFiles
    })
}

private fun Project.getMappedClasspath(
    configurationName: String,
    attributeAction: AttributeContainer.() -> Unit = {},
): FileCollection {
    val classpath = project.configurations.named(configurationName)

    return project.files(classpath.map { classpath ->
        classpath.incoming.artifactView {
            componentFilter {
                it !is ProjectComponentIdentifier
            }

            attributes {
                attribute(REMAPPED_ATTRIBUTE, true)
                attribute(RemapNamespaceAttribute.ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
                attribute(REMAP_ARTIFACT_NAMESPACE_ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
                attribute(REMAP_CONSUMER_NAMESPACE_ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
                attributeAction()
            }
        }.artifacts.artifactFiles
    })
}

private val supportedRemapNamespaces = listOf(
    RemapNamespaceAttribute.INITIAL,
    RemapNamespaceAttribute.INTERMEDIARY,
    RemapNamespaceAttribute.SEARGE,
    RemapNamespaceAttribute.OBF,
)

internal fun remapSourceNamespaces(defaultNamespace: String): Set<String> =
    (supportedRemapNamespaces + defaultNamespace)
        .filter(String::isNotEmpty)
        .toSet()

private fun Project.getNamespacedMappedClasspath(
    configurationName: String,
    sourceNamespace: String,
    attributeAction: AttributeContainer.() -> Unit = {},
): FileCollection {
    val classpath = configurations.findByName(namespacedResolvableModConfigurationName(configurationName, sourceNamespace))
        ?: return files()

    return classpath.incoming.artifactView {
        componentFilter {
            it !is ProjectComponentIdentifier
        }

        attributes {
            attributeAction()
            attribute(REMAPPED_ATTRIBUTE, true)
            attribute(RemapNamespaceAttribute.ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
            attribute(REMAP_ARTIFACT_SOURCE_NAMESPACE_ATTRIBUTE, sourceNamespace)
            attribute(REMAP_ARTIFACT_NAMESPACE_ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
            attribute(REMAP_CONSUMER_NAMESPACE_ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
        }
    }.artifacts.artifactFiles.filter(File::isRemappableModArtifactCandidate)
}

private fun Project.getNamespacedUnmappedModFiles(
    configurationName: String,
    namespace: String,
    attributeAction: AttributeContainer.() -> Unit = {},
): FileCollection {
    val classpath = configurations.findByName(namespacedResolvableModConfigurationName(configurationName, namespace))
        ?: return files()

    return classpath.incoming.artifactView {
        componentFilter {
            it !is ProjectComponentIdentifier
        }

        attributes {
            attributeAction()
            attribute(REMAPPED_ATTRIBUTE, false)
            attribute(RemapNamespaceAttribute.ATTRIBUTE, namespace)
            attribute(REMAP_ARTIFACT_SOURCE_NAMESPACE_ATTRIBUTE, namespace)
            attribute(REMAP_ARTIFACT_NAMESPACE_ATTRIBUTE, namespace)
        }
    }.artifacts.artifactFiles
}

private fun TargetCompilation<*>.getNamespacedMappedClasspaths(configurationName: String): FileCollection =
    project.files(
        supportedRemapNamespaces.map { namespace ->
            project.getNamespacedMappedClasspath(configurationName, namespace) { resolvableAttributes(this) }
        },
        target.modRemapNamespace.map { namespace ->
            if (namespace.isNotEmpty() && namespace !in supportedRemapNamespaces) {
                project.getNamespacedMappedClasspath(configurationName, namespace) { resolvableAttributes(this) }
            } else {
                project.files()
            }
        }
    )

private fun TargetCompilation<*>.getNamespacedUnmappedModClasspaths(configurationName: String): FileCollection =
    project.files(
        supportedRemapNamespaces.map { namespace ->
            project.getNamespacedUnmappedModFiles(configurationName, namespace) { resolvableAttributes(this) }
        },
        target.modRemapNamespace.map { namespace ->
            if (namespace.isNotEmpty() && namespace !in supportedRemapNamespaces) {
                project.getNamespacedUnmappedModFiles(configurationName, namespace) { resolvableAttributes(this) }
            } else {
                project.files()
            }
        }
    )

private fun TargetCompilation<*>.normalizeModClasspath(
    configurationName: String,
    classpathKind: ClocheClasspathKind,
    normalizationKind: ClocheClasspathNormalizationKind,
    consumerClasspathNamespace: String = MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE,
): FileCollection {
    return project.objects.fileCollection()
        .from(project.provider {
            modClasspathDiagnostics(configurationName, classpathKind, normalizationKind, consumerClasspathNamespace).result.files
        })
}

private data class ModClasspathDiagnostics(
    val rawArtifacts: List<ResolvedModArtifact>,
    val remappedArtifacts: List<ResolvedModArtifact>,
    val result: ClasspathNormalizationResult,
)

private fun TargetCompilation<*>.modClasspathDiagnostics(
    configurationName: String,
    classpathKind: ClocheClasspathKind,
    normalizationKind: ClocheClasspathNormalizationKind,
    consumerClasspathNamespace: String = MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE,
): ModClasspathDiagnostics {
    val rawArtifacts = mutableListOf<ResolvedModArtifact>()
    val remappedArtifacts = mutableListOf<ResolvedModArtifact>()
    val decisions = sourceNamespaces.get().flatMap { sourceNamespace ->
        val classpath = project.configurations.findByName(
            namespacedResolvableModConfigurationName(configurationName, sourceNamespace)
        ) ?: return@flatMap emptyList()

        val sourceRawArtifacts = classpath.incoming.artifactView {
            componentFilter { it !is ProjectComponentIdentifier }

            attributes {
                resolvableAttributes(this)
                attribute(REMAPPED_ATTRIBUTE, false)
                attribute(RemapNamespaceAttribute.ATTRIBUTE, sourceNamespace)
                attribute(REMAP_ARTIFACT_SOURCE_NAMESPACE_ATTRIBUTE, sourceNamespace)
                attribute(REMAP_ARTIFACT_NAMESPACE_ATTRIBUTE, sourceNamespace)
                attribute(REMAP_CONSUMER_NAMESPACE_ATTRIBUTE, consumerClasspathNamespace)
            }
        }.artifacts.artifacts
            .filter { artifact -> artifact.file.isRemappableModArtifactCandidate() }
            .map { artifact -> artifact.toResolvedModArtifact(sourceNamespace, consumerClasspathNamespace, classpathKind) }

        val sourceRemappedArtifacts = classpath.incoming.artifactView {
            componentFilter { it !is ProjectComponentIdentifier }

            attributes {
                resolvableAttributes(this)
                attribute(REMAPPED_ATTRIBUTE, true)
                attribute(RemapNamespaceAttribute.ATTRIBUTE, consumerClasspathNamespace)
                attribute(REMAP_ARTIFACT_SOURCE_NAMESPACE_ATTRIBUTE, sourceNamespace)
                attribute(REMAP_ARTIFACT_NAMESPACE_ATTRIBUTE, consumerClasspathNamespace)
                attribute(REMAP_CONSUMER_NAMESPACE_ATTRIBUTE, consumerClasspathNamespace)
            }
        }.artifacts.artifacts
            .filter { artifact -> artifact.file.isRemappableModArtifactCandidate() }
            .map { artifact -> artifact.toResolvedModArtifact(sourceNamespace, consumerClasspathNamespace, classpathKind) }

        rawArtifacts += sourceRawArtifacts
        remappedArtifacts += sourceRemappedArtifacts
        normalizeModClasspath(normalizationKind, sourceRawArtifacts, sourceRemappedArtifacts).decisions
    }

    return ModClasspathDiagnostics(
        rawArtifacts = rawArtifacts,
        remappedArtifacts = remappedArtifacts,
        result = ClasspathNormalizationResult(normalizationKind, decisions),
    )
}

private fun TargetCompilation<*>.registerRemapReportTask(): TaskProvider<ClocheRemapReport> {
    val taskName = lowerCamelCaseGradleName("cloche", target.featureName, collapsedName, "remapReport")
    val reportTask = project.tasks.register<ClocheRemapReport>(taskName) {
        outputDirectory.set(project.layout.buildDirectory.dir("reports/cloche/remap/${target.name.safeGradlePathSegment()}/$namePath"))
        report.set(project.provider {
            val consumerNamespace = MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE
            val compileDiagnostics = modClasspathDiagnostics(
                sourceSet.compileClasspathConfigurationName,
                ClocheClasspathKind.Compile,
                ClocheClasspathNormalizationKind.Compile,
                consumerNamespace,
            )
            val runtimeDiagnostics = modClasspathDiagnostics(
                sourceSet.runtimeClasspathConfigurationName,
                ClocheClasspathKind.Runtime,
                ClocheClasspathNormalizationKind.Runtime,
                consumerNamespace,
            )
            val declarations = sourceNamespaces.get().flatMap { namespace ->
                val bucketName = namespacedModConfigurationName(sourceSet.compileClasspathConfigurationName, namespace)
                val bucket = project.configurations.findByName(bucketName)
                bucket.orEmptyDependencies().map { dependency ->
                    RemapDeclarationReport(
                        displayName = dependency.toString(),
                        declaredSourceNamespace = null,
                        effectiveSourceNamespace = namespace,
                        bucket = bucketName,
                    )
                }
            }
            val buckets = sourceNamespaces.get().flatMap { namespace ->
                listOf(
                    bucketReport(sourceSet.compileClasspathConfigurationName, namespace, "compile"),
                    bucketReport(sourceSet.runtimeClasspathConfigurationName, namespace, "runtime"),
                )
            }

            remapReportFromDecisions(
                target = target.name,
                compilation = name,
                consumerClasspathNamespace = consumerNamespace,
                declarations = declarations,
                buckets = buckets,
                rawArtifacts = compileDiagnostics.rawArtifacts + runtimeDiagnostics.rawArtifacts,
                remappedArtifacts = compileDiagnostics.remappedArtifacts + runtimeDiagnostics.remappedArtifacts,
                retainedNonModLibraries = (
                    project.retainedNonModFilesFromModProvenance(sourceSet.compileClasspathConfigurationName) {
                        resolvableAttributes(this)
                    }.files +
                        project.retainedNonModFilesFromModProvenance(sourceSet.runtimeClasspathConfigurationName) {
                            resolvableAttributes(this)
                        }.files
                    ).toList(),
                classpathResults = listOf(compileDiagnostics.result, runtimeDiagnostics.result),
            )
        })
    }
    project.tasks.named("clocheRemapReport") {
        dependsOn(reportTask)
    }
    return reportTask
}

private fun Configuration?.orEmptyDependencies() = this?.dependencies.orEmpty()

private fun TargetCompilation<*>.bucketReport(
    configurationName: String,
    namespace: String,
    classpathKind: String,
) = RemapBucketReport(
    name = namespacedModConfigurationName(configurationName, namespace),
    sourceNamespace = namespace,
    classpathKind = classpathKind,
    dependencyCount = project.configurations.findByName(namespacedModConfigurationName(configurationName, namespace))?.dependencies?.size ?: 0,
)

private fun Project.getAccessWidenerClasspath(
    configurationName: String,
    sourceNamespaces: Provider<Set<String>>,
    attributeAction: AttributeContainer.() -> Unit,
): FileCollection = files(sourceNamespaces.map { namespaces ->
    namespaces.map { namespace ->
        getNamespacedMappedClasspath(configurationName, namespace, attributeAction)
    }
})

data class FinalJarTasks(
    val accessWidenTask: TaskProvider<AccessWiden>,
    val decompileTask: TaskProvider<Decompile>,
    val mixinTask: TaskProvider<Mixin>
) {
    val libraryArtifact: Provider<RegularFile>
        get() = accessWidenTask.flatMap(AccessWiden::outputFile)

    val sourcesArtifact: Provider<RegularFile>
        get() = decompileTask.flatMap(Decompile::outputFile)
}

internal fun registerCompilationTransformations(
    target: MinecraftTargetInternal,
    compilationName: String,
    sourceSet: SourceSet,
    sourceNamespaces: Provider<Set<String>>,
    resolvableAttributeAction: AttributeContainer.() -> Unit,
    namedMinecraftFile: Provider<RegularFile>,
    extraClasspathFiles: Provider<List<RegularFile>>,
    side: Provider<ModDistribution>
): FinalJarTasks {
    val outputDirectory = target.outputDirectory.map { it.dir(compilationName) }

    val collapsedName = compilationName.takeUnless(SourceSet.MAIN_SOURCE_SET_NAME::equals)

    val project = target.project
    val accessWidenerClasspath = project.getAccessWidenerClasspath(
        sourceSet.compileClasspathConfigurationName,
        sourceNamespaces,
        resolvableAttributeAction,
    )

    val accessWidenTask = project.tasks.register<AccessWiden>(
        lowerCamelCaseGradleName(
            "accessWiden",
            target.featureName,
            collapsedName,
            "minecraft",
        ),
    ) {
        group = "minecraft-transforms"

        inputFile.set(namedMinecraftFile)
        namespace.set(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)

        namedSource.set(target.minecraftVersion.map(::isUnobfuscatedVersion))

        with(project) {
            // TODO Export access wideners as a separate artifact
            accessWideners.from(accessWidenerClasspath)
        }

        outputFile.set(outputDirectory.zip(namedMinecraftFile) { dir, file ->
            dir.file(file.asFile.name)
        })

    }

    val mixinTask = project.tasks.register<Mixin>(
        lowerCamelCaseGradleName("mixin", target.featureName, collapsedName, "minecraft"),
    ) {
        group = "minecraft-transforms"

        inputFile.set(accessWidenTask.flatMap(AccessWiden::outputFile))

        sourceNamespace.set(target.modRemapNamespace)
        targetNamespace.set(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
        mappings.set(target.loadMappingsTask.flatMap(LoadMappings::output))

        val compileClasspath = project.getMappedClasspath(sourceSet.compileClasspathConfigurationName)
        val runtimeClasspath = project.getMappedClasspath(sourceSet.runtimeClasspathConfigurationName)

        mixinFiles.from(compileClasspath)
        mixinFiles.from(runtimeClasspath)

        classpath.from(compileClasspath)
        classpath.from(runtimeClasspath)
        classpath.from(extraClasspathFiles)

        this.side.set(
            when (side.get()) {
                ModDistribution.common -> MixinEnvironment.Side.SERVER
                ModDistribution.client -> MixinEnvironment.Side.CLIENT
            }
        )
    }

    val decompile = project.tasks.register<Decompile>(
        lowerCamelCaseGradleName("decompile", target.featureName, collapsedName, "minecraft"),
    ) {
        group = "sources"

        inputFile.set(accessWidenTask.flatMap(AccessWiden::outputFile))
        classpath.from(project.configurations.named(sourceSet.compileClasspathConfigurationName))
        classpath.from(extraClasspathFiles)

        outputFile.set(outputDirectory.zip(namedMinecraftFile) { dir, file ->
            dir.file("${file.asFile.nameWithoutExtension}-sources.${file.asFile.extension}")
        })
    }

    return FinalJarTasks(accessWidenTask, decompile, mixinTask)
}

internal fun Project.compilationSourceSet(target: ClocheTarget, name: String): SourceSet {
    val sourceSet = extension<SourceSetContainer>().maybeCreate(sourceSetName(target, name))

    if (sourceSet.localRuntimeConfigurationName !in configurations.names) {
        configurations.dependencyScope(sourceSet.localRuntimeConfigurationName)
    }

    if (sourceSet.localImplementationConfigurationName !in configurations.names) {
        configurations.dependencyScope(sourceSet.localImplementationConfigurationName)
    }

    return sourceSet
}

private fun setupModTransformationPipeline(
    project: Project,
    target: MinecraftTargetInternal,
    compilation: TargetCompilation<*>,
) {
    // afterEvaluate needed as the registration of a transform is dependent on a lazy provider
    project.afterEvaluate {
        val outputNamespace = MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE
        val remapNamespaces = compilation.sourceNamespaces.get()

        for (remapNamespace in remapNamespaces) {
            if (remapNamespace == RemapNamespaceAttribute.INITIAL) {
                project.dependencies.registerTransform(NoopAction::class) {
                    from
                        .attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                        .attribute(REMAPPED_ATTRIBUTE, false)
                        .attribute(RemapNamespaceAttribute.ATTRIBUTE, remapNamespace)
                        .attribute(REMAP_ARTIFACT_SOURCE_NAMESPACE_ATTRIBUTE, remapNamespace)
                        .attribute(REMAP_ARTIFACT_NAMESPACE_ATTRIBUTE, remapNamespace)
                        .attribute(REMAP_CONSUMER_NAMESPACE_ATTRIBUTE, outputNamespace)
                        .attribute(ClocheTargetAttribute.ATTRIBUTE, target.name)

                    to
                        .attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                        .attribute(REMAPPED_ATTRIBUTE, true)
                        .attribute(RemapNamespaceAttribute.ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
                        .attribute(REMAP_ARTIFACT_SOURCE_NAMESPACE_ATTRIBUTE, remapNamespace)
                        .attribute(REMAP_ARTIFACT_NAMESPACE_ATTRIBUTE, outputNamespace)
                        .attribute(REMAP_CONSUMER_NAMESPACE_ATTRIBUTE, outputNamespace)
                        .attribute(ClocheTargetAttribute.ATTRIBUTE, target.name)

                    compilation.baseAttributes(from)
                    compilation.baseAttributes(to)
                }

                continue
            }

            project.dependencies.registerTransform(ClocheRemapAction::class) {
                from
                        .attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                        .attribute(REMAPPED_ATTRIBUTE, false)
                        .attribute(RemapNamespaceAttribute.ATTRIBUTE, remapNamespace)
                        .attribute(REMAP_ARTIFACT_SOURCE_NAMESPACE_ATTRIBUTE, remapNamespace)
                        .attribute(REMAP_ARTIFACT_NAMESPACE_ATTRIBUTE, remapNamespace)
                        .attribute(REMAP_CONSUMER_NAMESPACE_ATTRIBUTE, outputNamespace)
                        .attribute(ClocheTargetAttribute.ATTRIBUTE, target.name)

                to
                    .attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.JAR_TYPE)
                    .attribute(REMAPPED_ATTRIBUTE, true)
                    .attribute(RemapNamespaceAttribute.ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
                    .attribute(REMAP_ARTIFACT_SOURCE_NAMESPACE_ATTRIBUTE, remapNamespace)
                    .attribute(REMAP_ARTIFACT_NAMESPACE_ATTRIBUTE, outputNamespace)
                    .attribute(REMAP_CONSUMER_NAMESPACE_ATTRIBUTE, outputNamespace)
                    .attribute(ClocheTargetAttribute.ATTRIBUTE, target.name)

                // TODO Is the usage of the base attributes correct here?
                compilation.baseAttributes(from)
                compilation.baseAttributes(to)

                parameters {
                    val remapClasspath =
                        project.getUnmappedClasspath(compilation.remapClasspathConfiguration.name) {
                            attribute(RemapNamespaceAttribute.ATTRIBUTE, remapNamespace)
                        }

                    val nonModCompileClasspath =
                        project.getNonModFiles(compilation.sourceSet.compileClasspathConfigurationName) {
                            compilation.resolvableAttributes(this)
                        }

                    val nonModRuntimeClasspath =
                        project.getNonModFiles(compilation.sourceSet.runtimeClasspathConfigurationName) {
                            compilation.resolvableAttributes(this)
                        }

                    val modCompileClasspath = project.getNamespacedUnmappedModFiles(
                        compilation.sourceSet.compileClasspathConfigurationName,
                        remapNamespace
                    ) {
                        compilation.resolvableAttributes(this)
                    }

                    val modRuntimeClasspath = project.getNamespacedUnmappedModFiles(
                        compilation.sourceSet.runtimeClasspathConfigurationName,
                        remapNamespace
                    ) {
                        compilation.resolvableAttributes(this)
                    }

                    val mappingConfiguration = project.configurations.named(target.sourceSet.mappingsConfigurationName)
                    val resolvedMappingArtifacts = project.provider {
                        mappingConfiguration.get()
                            .incoming
                            .artifactView {
                                componentFilter { it is ModuleComponentIdentifier }
                            }
                            .files
                            .files
                            .filter(File::isFile)
                    }

                    mappingArtifacts.from(resolvedMappingArtifacts)

                    javaExecutable.set(target.loadMappingsTask.flatMap(LoadMappings::javaExecutable))
                    versionManifestUrl.set(VERSION_MANIFEST_URL)
                    offline.set(project.gradle.startParameter.isOffline)

                    sourceNamespace.set(remapNamespace)

                    extraClasspath.from(compilation.info.transformSafeIntermediaryMinecraftClasspath)
                    extraClasspath.from(remapClasspath)

                    cacheDirectory.set(getGlobalCacheDirectory(project))

                    nonModFiles.from(nonModCompileClasspath)
                    nonModFiles.from(nonModRuntimeClasspath)

                    modFiles.from(modCompileClasspath)
                    modFiles.from(modRuntimeClasspath)
                }
            }
        }
    }
}

internal open class TargetCompilationInfo<T : MinecraftTargetInternal>(
    val name: String,
    val target: T,
    /**
     * Minecraft jars used as secondary inputs by ClocheRemapAction.
     *
     * This classpath must be transform-safe: use already-resolved/cache files or files resolved
     * through Gradle artifact/configuration boundaries that carry producer task dependencies.
     * Do not pass direct task output FileCollections here, because artifact transforms may run
     * during dependency resolution without scheduling hidden TransformParameters producers.
     */
    val transformSafeIntermediaryMinecraftClasspath: FileCollection,
    val namedMinecraftFile: Provider<RegularFile>,
    val extraClasspathFiles: Provider<List<RegularFile>>,
    val side: Provider<ModDistribution>,
    val data: Boolean,
    val test: Boolean,
    val includeState: IncludeTransformationStateAttribute,
    val includeJarType: Class<out IncludesJar>,
)

@Suppress("UnstableApiUsage")
internal abstract class TargetCompilation<T : MinecraftTargetInternal> @Inject constructor(info: TargetCompilationInfo<T>) :
    CompilationInternal() {
    private val _info = info

    open val info
        get() = _info

    override val target get() = _info.target

    override val isTest get() = _info.test

    final override val sourceSet: SourceSet = project.compilationSourceSet(_info.target, _info.name)

    val modOutputs: ConfigurableFileCollection = project.files(sourceSet.output)

    internal val sourceNamespaces: Provider<Set<String>> = target.modRemapNamespace.map(::remapSourceNamespaces)

    protected val setupFiles = registerCompilationTransformations(
        _info.target,
        _info.name,
        sourceSet,
        sourceNamespaces,
        { resolvableAttributes(this) },
        _info.namedMinecraftFile,
        _info.extraClasspathFiles,
        _info.side,
    )

    val metadataDirectory: Provider<Directory>
        @Internal
        get() = project.layout.buildDirectory.dir("generated")
            .map { it.dir("metadata").optionalDir(target.featureName).dir(namePath) }

    abstract val generateMetadataTask: TaskProvider<out Task>

    val finalMinecraftFile get() = setupFiles.libraryArtifact

    val sources
        get() =
            setupFiles.sourcesArtifact

    val includeBucketConfiguration: NamedDomainObjectProvider<DependencyScopeConfiguration> =
        project.configurations.dependencyScope(
            lowerCamelCaseGradleName(
                _info.target.featureName,
                featureName,
                "include"
            )
        ) {
            addCollectedDependencies(dependencyHandler.include)
        }

    private val includeResolvableConfiguration =
        project.configurations.resolvable(
            lowerCamelCaseGradleName(
                _info.target.featureName,
                featureName,
                "includeFiles",
            )
        ) {
            extendsFrom(includeBucketConfiguration)

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

    val remapClasspathConfiguration: Configuration =
        project.configurations.create(
            lowerCamelCaseGradleName(
                _info.target.featureName,
                featureName,
                "remapClasspath"
            )
        ) {
            isCanBeConsumed = false

            shouldResolveConsistentlyWith(project.configurations.getByName(sourceSet.compileClasspathConfigurationName))

            attributes {
                attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class.java, Usage.JAVA_API))
                attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category::class.java, Category.LIBRARY))
                attribute(
                    LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                    project.objects.named(LibraryElements::class.java, LibraryElements.JAR)
                )
                attribute(Bundling.BUNDLING_ATTRIBUTE, project.objects.named(Bundling::class.java, Bundling.EXTERNAL))
            }

            this@TargetCompilation.attributes(attributes)
        }

    val remapJarTask: TaskProvider<RemapJar>? = if (!_info.test) {
        project.tasks.register<RemapJar>(
            lowerCamelCaseGradleName(sourceSet.takeUnless(SourceSet::isMain)?.name, "remapJar"),
        ) {
            val jarFile = project.tasks.named<Jar>(sourceSet.jarTaskName).flatMap(Jar::getArchiveFile)

            destinationDirectory.set(project.cloche.intermediateOutputsDirectory)
            input.set(jarFile)
            sourceNamespace.set(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
            targetNamespace.set(MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
            classpath.from(sourceSet.compileClasspath)

            mappings.set(target.loadMappingsTask.flatMap(LoadMappings::output))

            manifest.fromJars(project.serviceOf(), jarFile)
        }
    } else {
        null
    }

    val includeJarTask: TaskProvider<out IncludesJar>? = if (!_info.test) {
        project.tasks.register(
            lowerCamelCaseGradleName(sourceSet.takeUnless(SourceSet::isMain)?.name, "includeJar"),
            _info.includeJarType,
        ) {
            destinationDirectory.set(project.cloche.finalOutputsDirectory)

            val jar = project.tasks.named<Jar>(sourceSet.jarTaskName)
            val jarFile = jar.flatMap(Jar::getArchiveFile)

            input.set(jarFile)
            manifest.fromJars(project.serviceOf(), jarFile)

            fromResolutionResults(includeResolvableConfiguration)
        }
    } else {
        null
    }

    val generatedResourceOutputs: ConfigurableFileCollection =
        project.files(target.datagenDirectory, target.datagenClientDirectory, metadataDirectory)

    init {
        setupModTransformationPipeline(project, _info.target, this)
        registerRemapReportTask()

        project.configurations.named(sourceSet.compileClasspathConfigurationName) {
            attributes.attributeProvider(
                REMAPPED_ATTRIBUTE,
                _info.target.minecraftVersion.map { !isUnobfuscatedVersion(it) })
            attributes.attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
            attributes.attribute(MOD_CLASSPATH_PREFERABLE_ATTRIBUTE, true)
            attributes.attribute(IncludeTransformationStateAttribute.ATTRIBUTE, _info.includeState)
            attributes.attribute(RemapNamespaceAttribute.ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
            attributes.attribute(REMAP_ARTIFACT_SOURCE_NAMESPACE_ATTRIBUTE, RemapNamespaceAttribute.INITIAL)
            attributes.attribute(REMAP_ARTIFACT_NAMESPACE_ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
            attributes.attribute(REMAP_CONSUMER_NAMESPACE_ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
        }

        project.configurations.named(sourceSet.runtimeClasspathConfigurationName) {
            attributes.attributeProvider(
                REMAPPED_ATTRIBUTE,
                _info.target.minecraftVersion.map { !isUnobfuscatedVersion(it) })
            attributes.attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
            attributes.attribute(MOD_CLASSPATH_PREFERABLE_ATTRIBUTE, true)
            attributes.attribute(IncludeTransformationStateAttribute.ATTRIBUTE, _info.includeState)
            attributes.attribute(RemapNamespaceAttribute.ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
            attributes.attribute(REMAP_ARTIFACT_SOURCE_NAMESPACE_ATTRIBUTE, RemapNamespaceAttribute.INITIAL)
            attributes.attribute(REMAP_ARTIFACT_NAMESPACE_ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
            attributes.attribute(REMAP_CONSUMER_NAMESPACE_ATTRIBUTE, MinecraftCodevRemapperPlugin.NAMED_MAPPINGS_NAMESPACE)
        }

        setupFiles.accessWidenTask.configure {
            accessWideners.from(this@TargetCompilation.accessWideners)
        }

        val compileMinecraftFiles = project.files(setupFiles.accessWidenTask, _info.extraClasspathFiles)
        val compileMinecraftConfiguration =
            project.configurations.detachedConfiguration(project.dependencies.create(compileMinecraftFiles))
        val originalCompileClasspath = sourceSet.compileClasspath
        val normalizedModCompileClasspath = normalizeModClasspath(
            sourceSet.compileClasspathConfigurationName,
            ClocheClasspathKind.Compile,
            ClocheClasspathNormalizationKind.Compile,
        )
        val sourceAndProjectOutputClasspath = originalCompileClasspath.filter { it.isDirectory }
        val nonModCompileClasspath = project.getNonModFiles(sourceSet.compileClasspathConfigurationName) {
            resolvableAttributes(this)
        }
        val sanitizedCompileClasspath = project.files(
            sourceAndProjectOutputClasspath,
            compileMinecraftConfiguration,
            normalizedModCompileClasspath,
            nonModCompileClasspath,
        )

        sourceSet.compileClasspath = sanitizedCompileClasspath

        project.tasks.named<JavaCompile>(sourceSet.compileJavaTaskName) {
            classpath = sanitizedCompileClasspath
        }

        project.plugins.withId(KOTLIN_JVM_PLUGIN_ID) {
            project.tasks.named<KotlinCompile>(sourceSet.getCompileTaskName("kotlin")) {
                libraries.setFrom(sanitizedCompileClasspath)
            }
        }

        val runtimeMinecraftFiles = project.files(finalMinecraftFile, _info.extraClasspathFiles)
        val runtimeMinecraftConfiguration =
            project.configurations.detachedConfiguration(project.dependencies.create(runtimeMinecraftFiles))
        val originalRuntimeClasspath = sourceSet.runtimeClasspath
        val normalizedModRuntimeClasspath = normalizeModClasspath(
            sourceSet.runtimeClasspathConfigurationName,
            ClocheClasspathKind.Runtime,
            ClocheClasspathNormalizationKind.Runtime,
        )
        val runtimeSourceAndProjectOutputClasspath = originalRuntimeClasspath.filter { it.isDirectory }
        val nonModRuntimeClasspath = project.getNonModFiles(sourceSet.runtimeClasspathConfigurationName) {
            resolvableAttributes(this)
        }
        val sanitizedRuntimeClasspath = project.files(
            runtimeSourceAndProjectOutputClasspath,
            runtimeMinecraftConfiguration,
            normalizedModRuntimeClasspath,
            nonModRuntimeClasspath,
        )

        sourceSet.runtimeClasspath = sanitizedRuntimeClasspath
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

    override fun resolvableAttributes(attributes: AttributeContainer) {
        super.resolvableAttributes(attributes)

        attributes
            .attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
            .attribute(ClocheTargetAttribute.ATTRIBUTE, target.name)
    }
}
