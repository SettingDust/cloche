package earth.terrarium.cloche.target.common

import earth.terrarium.cloche.target.addCollectedDependencies
import earth.terrarium.cloche.target.compilation.CommonCompilation
import earth.terrarium.cloche.target.compilation.externalApiConfigurationName
import earth.terrarium.cloche.target.compilation.externalCompileConfigurationName
import earth.terrarium.cloche.target.compilation.externalRuntimeConfigurationName
import earth.terrarium.cloche.target.compilation.localImplementationConfigurationName
import earth.terrarium.cloche.target.compilation.localRuntimeConfigurationName
import earth.terrarium.cloche.target.compilation.modConfigurationName
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

context(projectContext: Project)
internal fun setupConfigurations(compilation: CommonCompilation) {
    val sourceSet = compilation.sourceSet

    projectContext.configurations.dependencyScope(modConfigurationName(sourceSet.implementationConfigurationName)) {
        addCollectedDependencies(compilation.dependencyHandler.modImplementation)
    }

    projectContext.configurations.dependencyScope(modConfigurationName(sourceSet.apiConfigurationName)) {
        addCollectedDependencies(compilation.dependencyHandler.modApi)
    }

    projectContext.configurations.dependencyScope(modConfigurationName(sourceSet.compileOnlyConfigurationName)) {
        addCollectedDependencies(compilation.dependencyHandler.modCompileOnly)
    }

    projectContext.configurations.dependencyScope(modConfigurationName(sourceSet.compileOnlyApiConfigurationName)) {
        addCollectedDependencies(compilation.dependencyHandler.modCompileOnlyApi)
    }

    projectContext.configurations.dependencyScope(modConfigurationName(sourceSet.runtimeOnlyConfigurationName)) {
        addCollectedDependencies(compilation.dependencyHandler.modRuntimeOnly)
    }

    projectContext.configurations.dependencyScope(modConfigurationName(sourceSet.localRuntimeConfigurationName)) {
        addCollectedDependencies(compilation.dependencyHandler.modLocalRuntime)
    }

    projectContext.configurations.dependencyScope(modConfigurationName(sourceSet.localImplementationConfigurationName)) {
        addCollectedDependencies(compilation.dependencyHandler.modLocalImplementation)
    }

    projectContext.configurations.dependencyScope(sourceSet.commonBucketConfigurationName(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME)) {
        addCollectedDependencies(compilation.dependencyHandler.implementation)
    }

    projectContext.configurations.dependencyScope(sourceSet.commonBucketConfigurationName(JavaPlugin.API_CONFIGURATION_NAME)) {
        addCollectedDependencies(compilation.dependencyHandler.api)
    }

    projectContext.configurations.dependencyScope(sourceSet.commonBucketConfigurationName(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME)) {
        addCollectedDependencies(compilation.dependencyHandler.compileOnly)
    }

    projectContext.configurations.dependencyScope(sourceSet.commonBucketConfigurationName(JavaPlugin.COMPILE_ONLY_API_CONFIGURATION_NAME)) {
        addCollectedDependencies(compilation.dependencyHandler.compileOnlyApi)
    }

    projectContext.configurations.named(sourceSet.runtimeOnlyConfigurationName) {
        addCollectedDependencies(compilation.dependencyHandler.runtimeOnly)
    }

    projectContext.configurations.named(sourceSet.localRuntimeConfigurationName) {
        addCollectedDependencies(compilation.dependencyHandler.localRuntime)
    }

    projectContext.configurations.named(sourceSet.localImplementationConfigurationName) {
        addCollectedDependencies(compilation.dependencyHandler.localImplementation)
    }

    projectContext.configurations.named(sourceSet.externalRuntimeConfigurationName) {
        addCollectedDependencies(compilation.dependencyHandler.externalRuntime)
    }

    projectContext.configurations.named(sourceSet.externalCompileConfigurationName) {
        addCollectedDependencies(compilation.dependencyHandler.externalCompile)
    }

    projectContext.configurations.named(sourceSet.externalApiConfigurationName) {
        addCollectedDependencies(compilation.dependencyHandler.externalApi)
    }

    projectContext.configurations.named(sourceSet.annotationProcessorConfigurationName) {
        addCollectedDependencies(compilation.dependencyHandler.annotationProcessor)
    }
}
