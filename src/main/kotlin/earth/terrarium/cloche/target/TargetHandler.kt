package earth.terrarium.cloche.target

import earth.terrarium.cloche.*
import earth.terrarium.cloche.api.attributes.CompilationAttributes
import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import earth.terrarium.cloche.api.attributes.ModDistribution
import earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute
import earth.terrarium.cloche.ClochePlugin
import earth.terrarium.cloche.WITHOUT_DATA_ATTRIBUTE
import earth.terrarium.cloche.addClasspathDependency
import earth.terrarium.cloche.addDataClasspathDependency
import earth.terrarium.cloche.target.compilation.*
import earth.terrarium.cloche.target.fabric.FabricTargetImpl
import earth.terrarium.cloche.util.CLASSES_AND_RESOURCES_VARIANT_NAME
import earth.terrarium.cloche.util.configureClassesAndResourcesVariant
import earth.terrarium.cloche.util.optionalDir
import earth.terrarium.cloche.util.withIdeaModule
import earth.terrarium.cloche.util.withoutDataName
import net.msrandom.minecraftcodev.core.MinecraftOperatingSystemAttribute
import net.msrandom.minecraftcodev.core.getVersionList
import net.msrandom.minecraftcodev.core.operatingSystemName
import net.msrandom.minecraftcodev.core.task.CachedMinecraftParameters
import net.msrandom.minecraftcodev.core.utils.extension
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.runs.downloadAssetsTaskName
import net.msrandom.minecraftcodev.runs.extractNativesTaskName
import net.msrandom.minecraftcodev.runs.task.DownloadAssets
import net.msrandom.minecraftcodev.runs.task.ExtractNatives
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal const val MOD_ID_CATEGORY = "mod-id"
internal const val REMAPPED_VARIANT_NAME = "remapped"

fun Project.javaExecutableFor(
    version: Provider<String>,
    cacheParameters: CachedMinecraftParameters,
): Provider<RegularFile> {
    val javaVersion = version.flatMap { version ->
        cacheParameters.directory.flatMap { cacheDirectory ->
            cacheParameters.versionManifestUrl.flatMap { url ->
                cacheParameters.getIsOffline().map { offline ->
                    val version = getVersionList(cacheDirectory.asFile.toPath(), url, offline).version(version)

                    JavaLanguageVersion.of(version.javaVersion.majorVersion)
                }
            }
        }
    }

    return extension<JavaToolchainService>().launcherFor {
        languageVersion.set(javaVersion)
    }.map { it.executablePath }
}

@Suppress("UnstableApiUsage")
private fun TargetCompilation<*>.addModDependencies(
    configurationName: String,
    collector: DependencyCollector,
    modCollector: DependencyCollector
) {
    project.configurations.dependencyScope(modConfigurationName(configurationName)) {
        addCollectedDependencies(modCollector)

        attributes
            .remapped(false)
    }

    project.configurations.named(configurationName) {
        addCollectedDependencies(collector)
    }
}

private val supportedRemapNamespaces = listOf(
    RemapNamespaceAttribute.INITIAL,
    RemapNamespaceAttribute.INTERMEDIARY,
    RemapNamespaceAttribute.SEARGE,
    RemapNamespaceAttribute.OBF,
)

@Suppress("DEPRECATION")
private fun Dependency.remapNamespaceOrNull(): String? =
    (this as? ExternalModuleDependency)
        ?.attributes
        ?.getAttribute(RemapNamespaceAttribute.ATTRIBUTE)

private fun Project.copyDependencyToNamespaceBucket(
    dependency: Dependency,
    bucket: Configuration,
) {
    bucket.dependencies.add(dependency.copyWithoutPublicRemapNamespace(project))
}

@Suppress("UNCHECKED_CAST")
private fun AttributeContainer.copyAttributeFrom(source: AttributeContainer, attribute: Attribute<*>) {
    val typedAttribute = attribute as Attribute<Any>

    source.getAttribute(typedAttribute)?.let { value ->
        attribute(typedAttribute, value)
    }
}

@Suppress("DEPRECATION")
private fun Dependency.copyWithoutPublicRemapNamespace(project: Project): Dependency {
    if (this !is ExternalModuleDependency || attributes.getAttribute(RemapNamespaceAttribute.ATTRIBUTE) == null) {
        return copy()
    }

    val notation = mutableMapOf<String, String>("name" to name)

    group?.let { notation["group"] = it }
    version?.let { notation["version"] = it }

    val copy = project.dependencies.create(notation) as ExternalModuleDependency

    copy.isTransitive = isTransitive
    copy.isChanging = isChanging

    copy.version {
        versionConstraint.branch?.let { branch = it }
        if (versionConstraint.requiredVersion.isNotEmpty()) {
            require(versionConstraint.requiredVersion)
        }
        if (versionConstraint.preferredVersion.isNotEmpty()) {
            prefer(versionConstraint.preferredVersion)
        }
        if (versionConstraint.strictVersion.isNotEmpty()) {
            strictly(versionConstraint.strictVersion)
        }
        if (versionConstraint.rejectedVersions.isNotEmpty()) {
            reject(*versionConstraint.rejectedVersions.toTypedArray())
        }
    }

    reason?.let(copy::because)

    targetConfiguration?.let { copy.targetConfiguration = it }

    attributes.keySet().forEach { attribute ->
        if (attribute != RemapNamespaceAttribute.ATTRIBUTE) {
            copy.attributes.copyAttributeFrom(attributes, attribute)
        }
    }

    artifacts.forEach { artifact ->
        copy.artifact {
            name = artifact.name
            type = artifact.type
            extension = artifact.extension
            classifier = artifact.classifier
            url = artifact.url
        }
    }

    requestedCapabilities.forEach { capability ->
        copy.capabilities {
            val version = capability.version

            requireCapability(
                if (version == null) {
                    "${capability.group}:${capability.name}"
                } else {
                    "${capability.group}:${capability.name}:$version"
                }
            )
        }
    }

    if (isEndorsingStrictVersions) {
        copy.endorseStrictVersions()
    } else {
        copy.doNotEndorseStrictVersions()
    }

    excludeRules.forEach { rule ->
        copy.exclude(
            buildMap {
                rule.group?.let { put("group", it) }
                rule.module?.let { put("module", it) }
            }
        )
    }

    return copy
}

private fun Configuration.allDependenciesRecursive(visited: MutableSet<Configuration> = mutableSetOf()): Sequence<Dependency> {
    if (!visited.add(this)) {
        return emptySequence()
    }

    return dependencies.asSequence() + extendsFrom.asSequence().flatMap { it.allDependenciesRecursive(visited) }
}

private fun TargetCompilation<*>.configureNamespaceBuckets(
    classpathConfigurationName: String,
    defaultNamespace: Provider<String>,
    explicitSourceNamespaceCollectors: Collection<DependencyCollector>,
    extendClasspath: Configuration.() -> Unit,
) {
    val dependencyBuckets = linkedMapOf<String, Provider<out Configuration>>()

    fun namespaceBucket(namespace: String): Provider<out Configuration> =
        dependencyBuckets.getOrPut(namespace) {
            val dependencyBucket = project.configurations.dependencyScope(namespacedModConfigurationName(classpathConfigurationName, namespace)) {
                isCanBeDeclared = true
                isCanBeResolved = false
                isCanBeConsumed = false
                isVisible = false
                isTransitive = false

                attributes.artifactSourceNamespace(namespace)
                attributes.artifactNamespace(namespace)
            }

            project.configurations.resolvable(namespacedResolvableModConfigurationName(classpathConfigurationName, namespace)) {
                isCanBeDeclared = false
                isCanBeResolved = true
                isCanBeConsumed = false
                isVisible = false

                shouldResolveConsistentlyWith(project.configurations.getByName(classpathConfigurationName))
                extendsFrom(dependencyBucket.get())

                attributes
                    .remapped(false)
                    .artifactSourceNamespace(namespace)
                    .artifactNamespace(namespace)

                @Suppress("DEPRECATION")
                attributes.attribute(RemapNamespaceAttribute.ATTRIBUTE, RemapNamespaceAttribute.INITIAL)

                attributes {
                    resolvableAttributes(this)
                }
            }

            dependencyBucket
        }

    val allModDependencies = project.configurations.resolvable(resolvableModConfigurationName(classpathConfigurationName)) {
        shouldResolveConsistentlyWith(project.configurations.getByName(classpathConfigurationName))

        attributes {
            resolvableAttributes(this)
            remapped(false)
        }

        extendClasspath()
    }

    fun routeDependencies() {
        val fallbackNamespace = defaultNamespace.get().ifEmpty { RemapNamespaceAttribute.INITIAL }
        val explicitDependencies = dependencyHandler.declaredModDependenciesFor(explicitSourceNamespaceCollectors)
        val explicitDependencyObjects = explicitDependencies.mapTo(hashSetOf()) { it.dependency }

        explicitDependencies.forEach { declaration ->
            project.copyDependencyToNamespaceBucket(
                declaration.dependency,
                namespaceBucket(declaration.sourceNamespace).get(),
            )
        }

        allModDependencies.get().allDependenciesRecursive().forEach { dependency ->
            if (dependency in explicitDependencyObjects) {
                return@forEach
            }

            val namespace = dependency.remapNamespaceOrNull() ?: fallbackNamespace

            if (namespace.isNotEmpty()) {
                project.copyDependencyToNamespaceBucket(dependency, namespaceBucket(namespace).get())
            }
        }
    }

    project.afterEvaluate {
        routeDependencies()
    }

}

@Suppress("UnstableApiUsage")
private fun TargetCompilation<*>.addDependencies() {
    addModDependencies(
        sourceSet.implementationConfigurationName,
        dependencyHandler.implementation,
        dependencyHandler.modImplementation,
    )

    addModDependencies(
        sourceSet.runtimeOnlyConfigurationName,
        dependencyHandler.runtimeOnly,
        dependencyHandler.modRuntimeOnly,
    )

    addModDependencies(
        sourceSet.compileOnlyConfigurationName,
        dependencyHandler.compileOnly,
        dependencyHandler.modCompileOnly,
    )

    addModDependencies(
        sourceSet.localRuntimeConfigurationName,
        dependencyHandler.localRuntime,
        dependencyHandler.modLocalRuntime,
    )

    addModDependencies(
        sourceSet.localImplementationConfigurationName,
        dependencyHandler.localImplementation,
        dependencyHandler.modLocalImplementation,
    )

    if (!isTest) {
        addModDependencies(
            sourceSet.compileOnlyApiConfigurationName,
            dependencyHandler.compileOnlyApi,
            dependencyHandler.modCompileOnlyApi,
        )

        addModDependencies(sourceSet.apiConfigurationName, dependencyHandler.api, dependencyHandler.modApi)
    }

    project.configurations.named(sourceSet.annotationProcessorConfigurationName) {
        addCollectedDependencies(dependencyHandler.annotationProcessor)
    }

    project.configurations.named(sourceSet.externalRuntimeConfigurationName) {
        addCollectedDependencies(dependencyHandler.externalRuntime)
    }

    project.configurations.named(sourceSet.externalCompileConfigurationName) {
        addCollectedDependencies(dependencyHandler.externalCompile)
    }

    project.configurations.named(sourceSet.externalApiConfigurationName) {
        addCollectedDependencies(dependencyHandler.externalApi)
    }

    fun Configuration.extendModCompileClasspath() {
        extendsFrom(project.configurations.named(modConfigurationName(sourceSet.compileOnlyConfigurationName)))
        extendsFrom(project.configurations.named(modConfigurationName(sourceSet.implementationConfigurationName)))
        extendsFrom(project.configurations.named(modConfigurationName(sourceSet.localImplementationConfigurationName)))

        if (!isTest) {
            extendsFrom(project.configurations.named(modConfigurationName(sourceSet.compileOnlyApiConfigurationName)))
            extendsFrom(project.configurations.named(modConfigurationName(sourceSet.apiConfigurationName)))
        }
    }

    fun Configuration.extendModRuntimeClasspath() {
        extendsFrom(project.configurations.named(modConfigurationName(sourceSet.runtimeOnlyConfigurationName)))
        extendsFrom(project.configurations.named(modConfigurationName(sourceSet.implementationConfigurationName)))

        extendsFrom(project.configurations.named(modConfigurationName(sourceSet.localRuntimeConfigurationName)))
        extendsFrom(project.configurations.named(modConfigurationName(sourceSet.localImplementationConfigurationName)))

        if (!isTest) {
            extendsFrom(project.configurations.named(modConfigurationName(sourceSet.apiConfigurationName)))
        }
    }

    configureNamespaceBuckets(
        sourceSet.compileClasspathConfigurationName,
        target.modRemapNamespace,
        listOf(
            dependencyHandler.modImplementation,
            dependencyHandler.modCompileOnly,
            dependencyHandler.modLocalImplementation,
            dependencyHandler.modCompileOnlyApi,
            dependencyHandler.modApi,
        ),
    ) {
        extendModCompileClasspath()
    }

    configureNamespaceBuckets(
        sourceSet.runtimeClasspathConfigurationName,
        target.modRemapNamespace,
        listOf(
            dependencyHandler.modImplementation,
            dependencyHandler.modRuntimeOnly,
            dependencyHandler.modLocalRuntime,
            dependencyHandler.modLocalImplementation,
            dependencyHandler.modApi,
        ),
    ) {
        extendModRuntimeClasspath()
    }

    project.configurations.resolvable(resolvableNonModConfigurationName(sourceSet.compileClasspathConfigurationName)) {
        shouldResolveConsistentlyWith(project.configurations.getByName(sourceSet.compileClasspathConfigurationName))

        attributes {
            resolvableAttributes(this)
        }

        extendsFrom(project.configurations.getByName(sourceSet.compileOnlyConfigurationName))
        extendsFrom(project.configurations.getByName(sourceSet.implementationConfigurationName))
        extendsFrom(project.configurations.getByName(sourceSet.localImplementationConfigurationName))
        extendsFrom(project.configurations.named(sourceSet.compileOnlyConfigurationName))
        extendsFrom(project.configurations.named(sourceSet.implementationConfigurationName))
        extendsFrom(project.configurations.named(sourceSet.localImplementationConfigurationName))

        if (!isTest) {
            extendsFrom(project.configurations.named(sourceSet.compileOnlyApiConfigurationName))
            extendsFrom(project.configurations.named(sourceSet.apiConfigurationName))
        }
    }

    project.configurations.resolvable(resolvableNonModConfigurationName(sourceSet.runtimeClasspathConfigurationName)) {
        shouldResolveConsistentlyWith(project.configurations.getByName(sourceSet.runtimeClasspathConfigurationName))

        attributes {
            resolvableAttributes(this)
        }

        extendsFrom(project.configurations.getByName(sourceSet.runtimeOnlyConfigurationName))
        extendsFrom(project.configurations.getByName(sourceSet.implementationConfigurationName))
        extendsFrom(project.configurations.named(sourceSet.runtimeOnlyConfigurationName))
        extendsFrom(project.configurations.named(sourceSet.implementationConfigurationName))

        extendsFrom(project.configurations.named(sourceSet.localRuntimeConfigurationName))
        extendsFrom(project.configurations.named(sourceSet.localImplementationConfigurationName))

        if (!isTest) {
            extendsFrom(project.configurations.named(sourceSet.apiConfigurationName))
        }
    }

    project.configurations.named(sourceSet.compileClasspathConfigurationName) {
        extendsFrom(project.configurations.named(sourceSet.localImplementationConfigurationName))

        extendModCompileClasspath()
        extendsFrom(project.configurations.getByName(sourceSet.localImplementationConfigurationName))
    }

    project.configurations.named(sourceSet.runtimeClasspathConfigurationName) {
        extendModRuntimeClasspath()
        extendsFrom(project.configurations.getByName(sourceSet.localImplementationConfigurationName))
        extendsFrom(project.configurations.getByName(sourceSet.localRuntimeConfigurationName))
    }

    if (!isTest) {
        project.configurations.named(sourceSet.apiElementsConfigurationName) {
            extendsFrom(project.configurations.getByName(modConfigurationName(sourceSet.apiConfigurationName)))
            extendsFrom(project.configurations.getByName(modConfigurationName(sourceSet.compileOnlyApiConfigurationName)))

            extendsFrom(project.configurations.getByName(sourceSet.externalCompileConfigurationName))
            extendsFrom(project.configurations.getByName(sourceSet.externalApiConfigurationName))
        }

        project.configurations.named(sourceSet.runtimeElementsConfigurationName) {
            extendsFrom(project.configurations.getByName(modConfigurationName(sourceSet.runtimeOnlyConfigurationName)))
            extendsFrom(project.configurations.getByName(modConfigurationName(sourceSet.implementationConfigurationName)))

            extendsFrom(project.configurations.getByName(sourceSet.externalRuntimeConfigurationName))
            extendsFrom(project.configurations.getByName(sourceSet.externalApiConfigurationName))
        }
    }

    project.configurations.named(remapClasspathConfiguration.name) {
        addCollectedDependencies(dependencyHandler.remapClasspath)
        extendsFrom(project.configurations.named(sourceSet.localImplementationConfigurationName))
        extendsFrom(project.configurations.named(sourceSet.localRuntimeConfigurationName))

        extendModRuntimeClasspath()
    }

    project.configurations.named(sourceSet.apiElementsConfigurationName) {
        extendsFrom(project.configurations.named(modConfigurationName(sourceSet.apiConfigurationName)))
        extendsFrom(project.configurations.named(modConfigurationName(sourceSet.compileOnlyApiConfigurationName)))

        extendsFrom(project.configurations.named(sourceSet.externalCompileConfigurationName))
        extendsFrom(project.configurations.named(sourceSet.externalApiConfigurationName))
    }

    project.configurations.named(sourceSet.runtimeElementsConfigurationName) {
        extendsFrom(project.configurations.named(modConfigurationName(sourceSet.apiConfigurationName)))
        extendsFrom(project.configurations.named(modConfigurationName(sourceSet.implementationConfigurationName)))
        extendsFrom(project.configurations.named(modConfigurationName(sourceSet.runtimeOnlyConfigurationName)))

        extendsFrom(project.configurations.named(sourceSet.externalRuntimeConfigurationName))
        extendsFrom(project.configurations.named(sourceSet.externalApiConfigurationName))
    }
}

context(Project)
internal fun handleTarget(target: MinecraftTargetInternal) {
    fun addCompilation(compilation: TargetCompilation<*>, testName: String? = null) {
        val sourceSet = compilation.sourceSet

        if (!compilation.isTest) {
            createCompilationVariants(compilation, sourceSet, true)
        } else {
            createExternalDependencyConfigurations(sourceSet)
        }

        configureSourceSet(sourceSet, target, compilation)

        compilation.addDependencies()

        sourceSet.output.dir(mapOf("builtBy" to compilation.generateMetadataTask), compilation.metadataDirectory)

        val copyMixins = tasks.register<Copy>(
            lowerCamelCaseGradleName("copy", target.featureName, compilation.featureName, "mixins"),
        ) {
            from(compilation.mixins)

            destinationDir =
                layout.buildDirectory.dir("mixins").get().optionalDir(target.namePath).dir(compilation.namePath).asFile
        }

        project.tasks.named<ProcessResources>(sourceSet.processResourcesTaskName) {
            from(copyMixins.map(Copy::getDestinationDir))
        }

        project.withIdeaModule(sourceSet) {
            it.resourceDirs.add(copyMixins.get().destinationDir)
        }

        target.registerAccessWidenerMergeTask(compilation)

        if (!compilation.isTest) {
            target.addJarInjects(compilation)
        }

        configureJvmTarget(sourceSet, target.jvmVersion)

        val resolvableConfigurationNames = listOf(
            sourceSet.compileClasspathConfigurationName,
            sourceSet.runtimeClasspathConfigurationName,
        )

        val libraryConsumableConfigurationNames = listOf(
            sourceSet.apiElementsConfigurationName,
            sourceSet.runtimeElementsConfigurationName,
        )

        val consumableConfigurationNames = libraryConsumableConfigurationNames + listOf(
            sourceSet.javadocElementsConfigurationName,
            sourceSet.sourcesElementsConfigurationName,
        )

        for (name in libraryConsumableConfigurationNames) {
            if (compilation.isTest) {
                continue
            }

            configurations.named(name) {
                val configuration = this

                // TODO Can this be avoided? maybe publishing remapped Jars in a separate variant if our named namespace is stable(like mojang mappings)?
                //  Alternatively could we find the exact artifact by checking if the files match? or via the build dependencies?
                artifacts.clear()

                project.artifacts.add(name, compilation.includeJarTask!!)

                val remappedVariant = outgoing.variants.create(REMAPPED_VARIANT_NAME) {
                    attributes
                        .attribute(REMAPPED_ATTRIBUTE, true)
                        .attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)

                    artifact(compilation.remapJarTask!!)
                }

                val includesStrippedVariant = outgoing.variants.create("${REMAPPED_VARIANT_NAME}IncludesStripped") {
                    attributes
                        .attribute(REMAPPED_ATTRIBUTE, true)
                        .attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
                        .attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.Stripped)

                    artifact(compilation.remapJarTask!!)
                }

                components.named("java") {
                    this as AdhocComponentWithVariants

                    withVariantsFromConfiguration(configuration) {
                        if (configurationVariant.name in listOf(remappedVariant.name, includesStrippedVariant.name)) {
                            skip()
                        }
                    }
                }

                configuration.outgoing.variants.named { it == "classes" || it == "resources" }.configureEach {
                    attributes
                        .attribute(REMAPPED_ATTRIBUTE, true)
                        .attribute(INCLUDE_TRANSFORMED_OUTPUT_ATTRIBUTE, false)
                }
            }
        }

        if (!compilation.isTest) {
            configureClassesAndResourcesVariant(sourceSet)
        }

        for (name in resolvableConfigurationNames) {
            configurations.named(name) {
                attributes(compilation::resolvableAttributes)

                attributes.attribute(
                    MinecraftOperatingSystemAttribute.attribute,
                    objects.named(operatingSystemName()),
                )
            }
        }

        for (name in consumableConfigurationNames) {
            configurations.findByName(name)?.attributes(compilation::consumableAttributes)
        }

        configurations.named(sourceSet.compileClasspathConfigurationName) {
            attributes {
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, named(LibraryElements.CLASSES))
            }
        }

        configurations.named(sourceSet.runtimeClasspathConfigurationName) {
            attributes {
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, named(LibraryElements.CLASSES_AND_RESOURCES))
            }
        }

        if (testName != null) {
            val sourceSetName = sourceSetName(target, testName)

            project.extension<SourceSetContainer>().named { it == sourceSetName }.configureEach {
                for (name in listOf(compileClasspathConfigurationName, runtimeClasspathConfigurationName)) {
                    project.configurations.named(name) {
                        attributes(compilation::resolvableAttributes)
                    }
                }
            }
        }
    }

    fun configureMainForData(main: TargetCompilation<*>) {
        val runtimeElements = configurations.named(main.sourceSet.runtimeElementsConfigurationName)

        main.sourceSet.output.dir(mapOf("builtBy" to target.datagenDirectoryBuildDependencies), target.datagenDirectory)

        configurations.named(target.main.sourceSet.runtimeElementsConfigurationName) {
            outgoing {
                variants {
                    val resourcesWithoutDataVariantName = withoutDataName(LibraryElements.RESOURCES)

                    named(LibraryElements.RESOURCES) {
                        artifact(main.metadataDirectory) {
                            type = ArtifactTypeDefinition.JVM_RESOURCES_DIRECTORY

                            builtBy(main.generateMetadataTask)
                        }

                        artifact(target.datagenDirectory) {
                            type = ArtifactTypeDefinition.JVM_RESOURCES_DIRECTORY

                            builtBy(target.datagenDirectoryBuildDependencies)
                        }

                        target.onClientIncluded {
                            artifact(target.datagenClientDirectory) {
                                type = ArtifactTypeDefinition.JVM_RESOURCES_DIRECTORY

                                builtBy(target.datagenClientDirectoryBuildDependencies)
                            }
                        }
                    }

                    register(resourcesWithoutDataVariantName) {
                        val resources = runtimeElements.flatMap {
                            it.outgoing.variants.named(LibraryElements.RESOURCES)
                        }

                        attributes {
                            attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, named(LibraryElements.RESOURCES))
                            attribute(WITHOUT_DATA_ATTRIBUTE, true)
                        }

                        artifacts.addAllLater(resources.map {
                            it.artifacts.matching { !main.generatedResourceOutputs.contains(it.file) }
                        })
                    }

                    configureClassesAndResourcesVariant(
                        withoutDataName(CLASSES_AND_RESOURCES_VARIANT_NAME),
                        this@named,
                        resourcesWithoutDataVariantName,
                        withoutData = true,
                    )
                }
            }
        }
    }

    addCompilation(target.main, SourceSet.TEST_SOURCE_SET_NAME)
    configureMainForData(target.main)

    target.data.onConfigured {
        addCompilation(it)
        it.addDataClasspathDependency(target.main)
    }

    target.test.onConfigured {
        addCompilation(it)
        it.addClasspathDependency(target.main)

        it.sourceSet.output.dir(mapOf("builtBy" to target.datagenDirectoryBuildDependencies), target.datagenDirectory)
    }

    if (target is FabricTargetImpl) {
        target.client.onConfigured { client ->
            addCompilation(client, ClochePlugin.CLIENT_TEST_COMPILATION_NAME)
            client.addClasspathDependency(target.main)

            configureMainForData(client)

            client.sourceSet.output.dir(mapOf("builtBy" to target.datagenClientDirectoryBuildDependencies), target.datagenDirectory)

            configurations.named(target.main.sourceSet.runtimeElementsConfigurationName) {
                outgoing {
                    variants {
                        named(LibraryElements.RESOURCES) {
                            artifact(target.datagenClientDirectory) {
                                type = ArtifactTypeDefinition.JVM_RESOURCES_DIRECTORY

                                builtBy(target.datagenClientDirectoryBuildDependencies)
                            }
                        }
                    }
                }
            }

            client.data.onConfigured { data ->
                addCompilation(data)
                data.addDataClasspathDependency(client)
                data.addDataClasspathDependency(target.main)

                target.data.onConfigured {
                    data.addDataClasspathDependency(it)
                }
            }

            client.test.onConfigured { test ->
                addCompilation(test)
                test.addClasspathDependency(client)
                test.addClasspathDependency(target.main)

                target.test.onConfigured {
                    test.addClasspathDependency(it)
                }

                test.sourceSet.output.dir(mapOf("builtBy" to target.datagenDirectoryBuildDependencies), target.datagenDirectory)
                test.sourceSet.output.dir(mapOf("builtBy" to target.datagenClientDirectoryBuildDependencies), target.datagenDirectory)
            }
        }
    }

    tasks.named<DownloadAssets>(target.sourceSet.downloadAssetsTaskName) {
        minecraftVersion.set(target.minecraftVersion)
    }

    tasks.named<ExtractNatives>(target.sourceSet.extractNativesTaskName) {
        minecraftVersion.set(target.minecraftVersion)
    }

    artifacts.add(Dependency.ARCHIVES_CONFIGURATION, target.finalJar)
}
