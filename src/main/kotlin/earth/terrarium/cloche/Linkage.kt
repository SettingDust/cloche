package earth.terrarium.cloche

import earth.terrarium.cloche.target.common.commonBucketConfigurationName
import earth.terrarium.cloche.target.compilation.*
import net.msrandom.minecraftcodev.core.utils.extension
import net.msrandom.virtualsourcesets.SourceSetStaticLinkageInfo
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationVariant
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet

const val JAVA_EXPECT_ACTUAL_ANNOTATION_PROCESSOR = "net.msrandom:java-expect-actual-processor:1.0.9"
const val JAVA_CLASS_EXTENSIONS_ANNOTATIONS = "net.msrandom:class-extension-annotations:1.0.0"
const val JAVA_CLASS_EXTENSIONS_PROCESSOR = "net.msrandom:java-class-extensions-processor:1.0.1"
const val KOTLIN_MULTIPLATFORM_STUB_PLUGIN = "net.msrandom:kmp-actual-stubs-compiler-plugin:0.1.2"

context(projectContext: Project)
private fun SourceSet.extendConfigurations(dependency: SourceSet, common: Boolean) {
    val apiBucket = dependency.commonBucketConfigurationName(JavaPlugin.API_CONFIGURATION_NAME)
    val compileOnlyApiBucket = dependency.commonBucketConfigurationName(JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME)
    val implementationBucket = dependency.commonBucketConfigurationName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)
    val compileOnlyBucket = dependency.commonBucketConfigurationName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)

    if (common) {
        projectContext.extend(
            commonBucketConfigurationName(JavaPlugin.API_CONFIGURATION_NAME),
            apiBucket,
        )

        projectContext.extend(
            commonBucketConfigurationName(JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME),
            compileOnlyApiBucket,
        )

        projectContext.extend(
            commonBucketConfigurationName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME),
            implementationBucket,
        )

        projectContext.extend(
            commonBucketConfigurationName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME),
            compileOnlyBucket,
        )
    } else {
        projectContext.extend(apiConfigurationName, apiBucket)
        projectContext.extend(compileOnlyApiConfigurationName, compileOnlyApiBucket)
        projectContext.extend(implementationConfigurationName, implementationBucket)
        projectContext.extend(compileOnlyConfigurationName, compileOnlyBucket)
    }

    projectContext.extend(runtimeOnlyConfigurationName, dependency.runtimeOnlyConfigurationName)

    projectContext.extend(localRuntimeConfigurationName, dependency.localRuntimeConfigurationName)
    projectContext.extend(localImplementationConfigurationName, dependency.localImplementationConfigurationName)

    projectContext.extend(externalRuntimeConfigurationName, dependency.externalRuntimeConfigurationName)
    projectContext.extend(externalCompileConfigurationName, dependency.externalCompileConfigurationName)
    projectContext.extend(externalApiConfigurationName, dependency.externalApiConfigurationName)

    projectContext.extend(
        modConfigurationName(implementationConfigurationName),
        modConfigurationName(dependency.implementationConfigurationName),
    )

    projectContext.extend(
        modConfigurationName(apiConfigurationName),
        modConfigurationName(dependency.apiConfigurationName),
    )

    projectContext.extend(
        modConfigurationName(compileOnlyConfigurationName),
        modConfigurationName(dependency.compileOnlyConfigurationName),
    )

    projectContext.extend(
        modConfigurationName(compileOnlyApiConfigurationName),
        modConfigurationName(dependency.compileOnlyApiConfigurationName),
    )

    projectContext.extend(
        modConfigurationName(runtimeOnlyConfigurationName),
        modConfigurationName(dependency.runtimeOnlyConfigurationName),
    )

    projectContext.extend(
        modConfigurationName(localRuntimeConfigurationName),
        modConfigurationName(dependency.localRuntimeConfigurationName),
    )

    projectContext.extend(
        modConfigurationName(localImplementationConfigurationName),
        modConfigurationName(dependency.localImplementationConfigurationName),
    )
}

/**
 * Depend on the api configuration of [dependency]
 */
context(projectContext: Project)
internal fun CommonCompilation.addClasspathDependency(dependency: CommonCompilation) {
    println("(classpath dependency) $this -> $dependency")

    if (dependency.isTest) {
        sourceSet.compileClasspath += dependency.sourceSet.output
        sourceSet.runtimeClasspath += dependency.sourceSet.output
    } else {
        projectContext.dependencies.add(sourceSet.compileOnlyConfigurationName, project().apply {
            capabilities {
                if (dependency.capabilitySuffix.isPresent) {
                    requireFeature(dependency.capabilitySuffix)
                }
            }

            attributes(dependency::baseAttributes)
        })
    }

    if (!isTest && !dependency.isTest) {
        projectContext.artifacts {
            add(sourceSet.apiElementsConfigurationName, projectContext.tasks.named(dependency.sourceSet.jarTaskName))
        }

        val dependencyProvider = projectContext.configurations.named(dependency.sourceSet.apiElementsConfigurationName)

        projectContext.configurations.named(sourceSet.apiElementsConfigurationName) {
            dependencies.addAllLater(dependencyProvider.map(Configuration::getAllDependencies))
            dependencyConstraints.addAllLater(dependencyProvider.map(Configuration::getAllDependencyConstraints))
        }
    }

    accessWideners.from(dependency.accessWideners)
    mixins.from(dependency.mixins)
}

context(projectContext: Project)
private fun TargetCompilation<*>.extendFromDependency(dependency: TargetCompilation<*>) {
    if (!isTest && !dependency.isTest) {
        for (name in listOf(
            SourceSet::getApiElementsConfigurationName,
            SourceSet::getRuntimeElementsConfigurationName
        )) {
            val dependencyProvider = projectContext.configurations.named(name(dependency.sourceSet))

            projectContext.configurations.named(name(sourceSet)) {
                dependencies.addAllLater(dependencyProvider.map(Configuration::getAllDependencies))
                dependencyConstraints.addAllLater(dependencyProvider.map(Configuration::getAllDependencyConstraints))
            }
        }
    }

    includeBucketConfiguration.configure {
        extendsFrom(dependency.includeBucketConfiguration)
    }
    dependency.dependencyHandler.includeTargets.all(dependencyHandler.includeTargets::add)

    accessWideners.from(dependency.accessWideners)
    mixins.from(dependency.mixins)
}

/**
 * Depend on the variant of [dependency]
 */
context(projectContext: Project)
internal fun TargetCompilation<*>.addClasspathDependency(dependency: TargetCompilation<*>) {
    println("(classpath dependency) $this -> $dependency")

    if (dependency.isTest) {
        sourceSet.compileClasspath += dependency.sourceSet.output
        sourceSet.runtimeClasspath += dependency.sourceSet.output
    } else {
        projectContext.dependencies.add(modConfigurationName(sourceSet.localImplementationConfigurationName), project().apply {
            capabilities {
                if (dependency.capabilitySuffix.isPresent) {
                    requireFeature(dependency.capabilitySuffix)
                }
            }

            attributes(dependency::baseAttributes)
        })
    }

    // Test compilations have no outgoing feature; their incoming project/output roles remain native.
    if (isTest) {
        extendFromDependency(dependency)
        return
    }

    val apiElementsClasses = projectContext.configurations.named(dependency.sourceSet.apiElementsConfigurationName).flatMap {
        it.outgoing.variants.named(LibraryElements.CLASSES)
    }

    val runtimeElementsClasses = projectContext.configurations.named(dependency.sourceSet.runtimeElementsConfigurationName).flatMap {
        it.outgoing.variants.named(LibraryElements.CLASSES)
    }

    val runtimeElementsResources = projectContext.configurations.named(dependency.sourceSet.runtimeElementsConfigurationName).flatMap {
        it.outgoing.variants.named(LibraryElements.RESOURCES)
    }

    projectContext.configurations.named(sourceSet.apiElementsConfigurationName) {
        outgoing.variants.named(LibraryElements.CLASSES) {
            artifacts.addAllLater(apiElementsClasses.map(ConfigurationVariant::getArtifacts))
        }
    }

    projectContext.configurations.named(sourceSet.runtimeElementsConfigurationName) {
        outgoing.variants.named(LibraryElements.CLASSES) {
            artifacts.addAllLater(runtimeElementsClasses.map(ConfigurationVariant::getArtifacts))
        }

        outgoing.variants.named(LibraryElements.RESOURCES) {
            artifacts.addAllLater(runtimeElementsResources.map(ConfigurationVariant::getArtifacts))
        }
    }

    extendFromDependency(dependency)
}

context(projectContext: Project)
internal fun TargetCompilation<*>.addDataClasspathDependency(dependency: TargetCompilation<*>) {
    println("(data classpath dependency) $this -> $dependency")

    projectContext.dependencies.add(modConfigurationName(sourceSet.localImplementationConfigurationName), project().apply {
        capabilities {
            if (dependency.capabilitySuffix.isPresent) {
                requireFeature(dependency.capabilitySuffix)
            }
        }

        attributes {
            attribute(WITHOUT_DATA_ATTRIBUTE, true)
            dependency.baseAttributes(this)
        }
    })

    val apiElementsClasses = projectContext.configurations.named(dependency.sourceSet.apiElementsConfigurationName).flatMap {
        it.outgoing.variants.named(LibraryElements.CLASSES)
    }

    val runtimeElementsClasses = projectContext.configurations.named(dependency.sourceSet.runtimeElementsConfigurationName).flatMap {
        it.outgoing.variants.named(LibraryElements.CLASSES)
    }

    val runtimeElementsResources = projectContext.configurations.named(dependency.sourceSet.runtimeElementsConfigurationName).flatMap {
        it.outgoing.variants.named(LibraryElements.RESOURCES)
    }

    projectContext.configurations.named(sourceSet.apiElementsConfigurationName) {
        outgoing.variants.named(LibraryElements.CLASSES) {
            artifacts.addAllLater(apiElementsClasses.map(ConfigurationVariant::getArtifacts))
        }
    }

    projectContext.configurations.named(sourceSet.runtimeElementsConfigurationName) {
        outgoing.variants.named(LibraryElements.CLASSES) {
            artifacts.addAllLater(runtimeElementsClasses.map(ConfigurationVariant::getArtifacts))
        }

        outgoing.variants.named(LibraryElements.RESOURCES) {
            artifacts.addAllLater(runtimeElementsResources.map(ConfigurationVariant::getArtifacts))
        }
    }

    extendFromDependency(dependency)
}

/**
 * Include [dependency] to be compiled alongside the current source, allowing Java platform annotations and Kotlin Multiplatform to function
 */
context(projectContext: Project)
internal fun CompilationInternal.addSourceDependency(dependency: CommonCompilation) {
    println("(source dependency) $this -> $dependency")

    sourceSet.extension<SourceSetStaticLinkageInfo>().link(dependency.sourceSet)

    sourceSet.extendConfigurations(dependency.sourceSet, this is CommonCompilation)

    project.dependencies.add(sourceSet.compileOnlyConfigurationName, JAVA_CLASS_EXTENSIONS_ANNOTATIONS)
    project.dependencies.add(sourceSet.annotationProcessorConfigurationName, JAVA_CLASS_EXTENSIONS_PROCESSOR)
    project.dependencies.add(sourceSet.annotationProcessorConfigurationName, JAVA_EXPECT_ACTUAL_ANNOTATION_PROCESSOR)

    accessWideners.from(dependency.accessWideners)
    mixins.from(dependency.mixins)
}
