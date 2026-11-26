package earth.terrarium.cloche.target.compilation

import earth.terrarium.cloche.ClochePlugin
import earth.terrarium.cloche.ClochePlugin.Companion.IDE_SYNC_TASK_NAME
import earth.terrarium.cloche.api.attributes.ClocheAttributes
import earth.terrarium.cloche.api.attributes.MinecraftModLoader
import earth.terrarium.cloche.api.target.ClocheTarget
import earth.terrarium.cloche.api.target.TARGET_NAME_PATH_SEPARATOR
import earth.terrarium.cloche.api.target.compilation.ClocheDependencyHandler
import earth.terrarium.cloche.api.target.compilation.ScopedDependencyHandler
import earth.terrarium.cloche.api.target.compilation.Compilation
import earth.terrarium.cloche.api.target.isSingleTarget
import earth.terrarium.cloche.api.target.targetName
import earth.terrarium.cloche.cloche
import earth.terrarium.cloche.target.ClocheTargetInternal
import earth.terrarium.cloche.target.forge.independentGame
import earth.terrarium.cloche.target.MinecraftTargetInternal
import earth.terrarium.cloche.util.optionalDir
import earth.terrarium.cloche.util.withIdeaModel
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import org.gradle.api.Action
import org.gradle.api.Buildable
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ArtifactView
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.domainObjectSet
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.newInstance
import org.jetbrains.annotations.ApiStatus

internal fun resolvableModConfigurationName(name: String) =
    lowerCamelCaseGradleName("mod", name)

internal fun resolvableNonModConfigurationName(name: String) =
    lowerCamelCaseGradleName("nonMod", name)

internal fun modConfigurationName(name: String) =
    lowerCamelCaseGradleName("mod", name)

internal fun getNonProjectArtifacts(configuration: Provider<out Configuration>): Provider<ArtifactView> = configuration.map {
    it.incoming.artifactView {
        componentFilter {
            // We do *not* want to build anything during sync.
            it !is ProjectComponentIdentifier
        }
    }
}

internal fun getRawNonProjectArtifacts(configuration: Provider<out Configuration>): Provider<ArtifactView> =
    configuration.map {
        it.incoming.artifactView {
            componentFilter { it !is ProjectComponentIdentifier }
            attributes.attribute(MIXIN_STATE_ATTRIBUTE, RAW_MIXIN_STATE)
        }
    }

val SourceSet.localRuntimeConfigurationName
    get() = lowerCamelCaseGradleName(takeUnless(SourceSet::isMain)?.name, "localRuntime")

val SourceSet.localImplementationConfigurationName
    get() = lowerCamelCaseGradleName(takeUnless(SourceSet::isMain)?.name, "localImplementation")

val SourceSet.externalRuntimeConfigurationName
    get() = lowerCamelCaseGradleName(takeUnless(SourceSet::isMain)?.name, "externalRuntime")

val SourceSet.externalCompileConfigurationName
    get() = lowerCamelCaseGradleName(takeUnless(SourceSet::isMain)?.name, "externalCompile")

val SourceSet.externalApiConfigurationName
    get() = lowerCamelCaseGradleName(takeUnless(SourceSet::isMain)?.name, "externalApi")

context(projectContext: Project)
internal fun getRelevantSyncArtifacts(configurationName: String): Provider<Buildable> =
    getNonProjectArtifacts(projectContext.configurations.named(configurationName)).map(ArtifactView::getFiles)

internal abstract class CompilationInternal : Compilation, Dependencies {
    abstract val isTest: Boolean

    abstract override val target: ClocheTargetInternal
        @Internal get

    val dependencyHandler: ClocheDependencyHandler by lazy(LazyThreadSafetyMode.NONE) {
        project.objects.newInstance<ScopedDependencyHandler>(target.minecraftVersion).also { handler ->
            var requests = 0
            handler.addRemapClasspath = { files ->
                val owner = target as? MinecraftTargetInternal
                require(owner != null && this === owner.main) {
                    "Cannot declare remapClasspath on compilation '${sourceSet.name}'; move it to target.dependencies or main.dependencies."
                }
                owner.additionalRemapClasspath.from(files)
            }
            handler.requestGame = { mc, loader, neo ->
                independentGame(project, lowerCamelCaseGradleName("clocheRemap", sourceSet.name, "game${++requests}"), mc, loader, neo)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val attributeActions: DomainObjectCollection<Action<AttributeContainer>> =
        project.objects.domainObjectSet(Action::class) as DomainObjectCollection<Action<AttributeContainer>>

    @Suppress("UNCHECKED_CAST")
    val resolvableAttributeActions: DomainObjectCollection<Action<AttributeContainer>> =
        project.objects.domainObjectSet(Action::class) as DomainObjectCollection<Action<AttributeContainer>>

    var withJavadoc: Boolean = false
    var withSources: Boolean = false

    val featureName
        get() = collapsedName?.let { lowerCamelCaseGradleName(it) }

    val capabilitySuffix: Provider<String>
        get() = target.hasSeparateClient.map {
            val collapsedName = collapsedName

            val base = if (it) {
                if (collapsedName == null) {
                    "server"
                } else if (collapsedName == ClochePlugin.CLIENT_COMPILATION_NAME) {
                    null
                } else {
                    val prefix = "${ClochePlugin.CLIENT_COMPILATION_NAME}:"

                    if (collapsedName.startsWith(prefix)) {
                        collapsedName.substring(prefix.length)
                    } else {
                        "server-$collapsedName"
                    }
                }
            } else {
                collapsedName
            }

            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
            base?.replace(TARGET_NAME_PATH_SEPARATOR, '-')
        }

    val capability: Provider<String>
        get() {
            val baseName = target.capabilitySuffix?.let { "${project.name}-$it" } ?: project.name
            return capabilitySuffix
                .map {
                    "${project.group}:$baseName-$it:${project.version}"
                }.orElse(project.provider { "${project.group}:$baseName:${project.version}" })
        }

    val namePath
        get() = name.replace(TARGET_NAME_PATH_SEPARATOR, '/')

    val collapsedName
        get() = name.takeUnless(SourceSet.MAIN_SOURCE_SET_NAME::equals)

    override fun withJavadocJar() {
        withJavadoc = true
    }

    override fun withSourcesJar() {
        withSources = true
    }

    override fun dependencies(action: Action<ClocheDependencyHandler>) =
        action.execute(dependencyHandler)

    override fun attributes(action: Action<AttributeContainer>) {
        attributeActions.add(action)
    }

    @ApiStatus.OverrideOnly
    protected open fun attributes(attributes: AttributeContainer) {
        attributeActions.all {
            execute(attributes)
        }
    }

    // Explicitly use base attributes, since the `attributes` method is meant to not be used(in favor of resolvableAttributes/consumableAttributes based on the usecase)
    open fun baseAttributes(attributes: AttributeContainer) = attributes(attributes)

    fun resolvableAttributes(action: Action<AttributeContainer>) {
        resolvableAttributeActions.add(action)
    }

    open fun resolvableAttributes(attributes: AttributeContainer) {
        attributes(attributes)

        resolvableAttributeActions.all {
            execute(attributes)
        }
    }

    open fun consumableAttributes(attributes: AttributeContainer) {
        attributes(attributes)

        if (ClochePlugin.VERSION != null) {
            attributes.attribute(ClocheAttributes.CLOCHE_VERSION, ClochePlugin.VERSION)
        }
    }

    override fun toString(): String {
        val name = listOfNotNull(target.targetName, name).joinToString(TARGET_NAME_PATH_SEPARATOR.toString())

        if (project == project.rootProject) {
            return project.path + name
        }

        return project.path + TARGET_NAME_PATH_SEPARATOR + name
    }
}

internal fun sourceSetName(target: ClocheTarget, compilationName: String) = when {
    target.isSingleTarget || target.targetName == MinecraftModLoader.common.name -> lowerCamelCaseGradleName(compilationName)
    compilationName == SourceSet.MAIN_SOURCE_SET_NAME -> target.featureName ?: SourceSet.MAIN_SOURCE_SET_NAME
    else -> lowerCamelCaseGradleName(target.featureName, compilationName)
}

internal fun Project.configureSourceSet(
    sourceSet: SourceSet,
    target: ClocheTarget,
    compilation: CompilationInternal,
) {
    if (!target.isSingleTarget) {
        val compilationDirectory =
            project.layout.projectDirectory.dir("src").optionalDir(target.namePath).dir(compilation.namePath)

        configureSourceSetDirectories(sourceSet, compilationDirectory)
    }

    val compileArtifacts = if (compilation is TargetCompilation<*>) {
        compilation.compilationConsumption.artifacts
    } else {
        getNonProjectArtifacts(configurations.named(sourceSet.compileClasspathConfigurationName))
    }
    val compileFiles = files(compileArtifacts.map { it.files })
    val syncTask = registerSourceSetSync(compileFiles)

    if (compilation is TargetCompilation<*>) {
        val consumption = compilation.compilationConsumption
        configureMinecraftSync(
            syncTask,
            files(consumption.games),
            files(
                compilation.finalMinecraftFile,
                compilation.runtimeExtraClasspathFiles,
                getNonProjectArtifacts(configurations.named(sourceSet.runtimeClasspathConfigurationName)).map { it.files },
            ),
            provider { consumption.preparation },
            compilation.sources,
        )
    }

    if (compilation.isTest) {
        return
    }

    val prefix = if (target.isSingleTarget || target.targetName == MinecraftModLoader.common.name) {
        null
    } else {
        target.capabilitySuffix
    }

    configureCompilationArchives(
        tasks.named<Jar>(sourceSet.jarTaskName),
        project.cloche.intermediateOutputsDirectory,
        prefix,
        compilation.capabilitySuffix,
        (target as? MinecraftTargetInternal)?.modRemapNamespace,
        if (compilation is TargetCompilation<*>) compilation.includeJarTask!! else null,
        if (compilation is TargetCompilation<*>) compilation.remapJarTask!! else null,
    )
}

internal fun Project.configureSourceSetDirectories(sourceSet: SourceSet, compilationDirectory: Directory) {
    sourceSet.java.srcDir(compilationDirectory.dir("java"))
    sourceSet.resources.srcDir(compilationDirectory.dir("resources"))

    plugins.withId(ClochePlugin.KOTLIN_JVM_PLUGIN_ID) {
        val kotlin = sourceSet.extensions.getByName("kotlin") as SourceDirectorySet
        kotlin.srcDir(compilationDirectory.dir("kotlin"))
    }

    // TODO Groovy + Scala?
}

internal fun Project.registerSourceSetSync(compileFiles: FileCollection): TaskProvider<Task> =
    tasks.named(IDE_SYNC_TASK_NAME) {
        inputs.files(compileFiles)
        doLast { compileFiles.files }
    }

internal fun Project.configureMinecraftSync(
    syncTask: TaskProvider<Task>,
    gameFiles: FileCollection,
    runtimeFiles: FileCollection,
    preparation: Provider<*>,
    sources: Provider<RegularFile>,
) {
    syncTask.configure {
        inputs.files(gameFiles, runtimeFiles)
        dependsOn(preparation)
        doLast {
            gameFiles.files
            runtimeFiles.files
        }
    }

    withIdeaModel {
        syncTask.configure {
            if (it.module.isDownloadSources) {
                dependsOn(sources)
            }
        }
    }
}

internal fun configureCompilationArchives(
    jarTask: TaskProvider<Jar>,
    intermediateOutputsDirectory: Provider<Directory>,
    prefix: String?,
    suffix: Provider<String>,
    remapNamespace: Provider<String>?,
    includeJarTask: TaskProvider<out Jar>?,
    remapJarTask: TaskProvider<out Jar>?,
) {
    val classifier = if (prefix == null) {
        suffix
    } else {
        suffix.map {
            "$prefix-$it"
        }.orElse(prefix)
    }

    val devClassifier = if (remapNamespace != null) {
        classifier.orElse("").zip(remapNamespace) { classifier, namespace ->
            if (namespace.isEmpty()) {
                classifier.ifEmpty { null }
            } else if (classifier.isNotEmpty()) {
                "$classifier-dev"
            } else {
                "dev"
            }
        }
    } else {
        classifier
    }

    jarTask.configure {
        destinationDirectory.set(intermediateOutputsDirectory)
        archiveClassifier.set(devClassifier)
    }
    includeJarTask?.configure {
        archiveClassifier.set(classifier)
    }
    remapJarTask?.configure {
        archiveClassifier.set(classifier)
    }
}
