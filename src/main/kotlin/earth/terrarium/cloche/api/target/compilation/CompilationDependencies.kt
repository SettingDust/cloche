package earth.terrarium.cloche.api.target.compilation

import earth.terrarium.cloche.api.target.MinecraftTarget
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.jvm.JvmComponentDependencies
import org.gradle.api.provider.Provider

@DslMarker
annotation class ClocheDependenciesDsl

@ClocheDependenciesDsl
interface CompilationDependencies : JvmComponentDependencies {
    val include: DependencyCollector
    fun include(target: MinecraftTarget)
    val api: DependencyCollector
    val compileOnlyApi: DependencyCollector
    val localRuntime: DependencyCollector
    val localImplementation: DependencyCollector
    val externalRuntime: DependencyCollector
    val externalCompile: DependencyCollector
    val externalApi: DependencyCollector
    val modApi: DependencyCollector
    val modCompileOnlyApi: DependencyCollector
    val modImplementation: DependencyCollector
    val modRuntimeOnly: DependencyCollector
    val modCompileOnly: DependencyCollector
    val modLocalRuntime: DependencyCollector
    val modLocalImplementation: DependencyCollector
    val skipIncludeTransformation: ClocheDependencyHandler.SkipIncludeTransformationDependencyModifier
    val extractIncludes: ClocheDependencyHandler.ExtractIncludesDependencyModifier
    val stripIncludes: ClocheDependencyHandler.StripIncludesDependencyModifier
    fun fabricApi(apiVersion: String)
    fun fabricApi(apiVersion: Provider<String>)
    fun fabricApi(apiVersion: String, minecraftVersion: String)
    fun fabricApi(apiVersion: Provider<String>, minecraftVersion: String)
    fun fabricApi(apiVersion: String, minecraftVersion: Provider<String>)
    fun fabricApi(apiVersion: Provider<String>, minecraftVersion: Provider<String>)
}

interface MainCompilationDependencies : CompilationDependencies {
    fun forgeMinecraft(minecraftVersion: String, loaderVersion: String): FileCollection
    fun neoforgeMinecraft(minecraftVersion: String, loaderVersion: String): FileCollection
    fun remapClasspath(files: FileCollection)
}

interface MainDependencyScope : Compilation {
    @JvmSynthetic
    fun dependencies(configure: MainCompilationDependencies.() -> Unit) {
        dependencies(org.gradle.api.Action { configure(this as MainCompilationDependencies) })
    }
}
