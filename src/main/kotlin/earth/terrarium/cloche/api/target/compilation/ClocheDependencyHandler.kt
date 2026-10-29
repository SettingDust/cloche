package earth.terrarium.cloche.api.target.compilation

import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import earth.terrarium.cloche.api.target.MinecraftTarget
import earth.terrarium.cloche.target.compilation.IncludeTargets
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.artifacts.dsl.DependencyModifier
import org.gradle.api.plugins.jvm.JvmComponentDependencies
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class ClocheDependencyHandler @Inject constructor(private val minecraftVersion: Provider<String>) : JvmComponentDependencies {
    internal abstract class IncludeCollectors {
        abstract val ordinary: DependencyCollector
        abstract val generated: DependencyCollector
    }

    private val includeCollectors = objectFactory.newInstance<IncludeCollectors>()
    internal val ordinaryInclude: DependencyCollector get() = includeCollectors.ordinary
    internal val includeTargets = IncludeTargets().apply {
        all { includeCollectors.generated.add(it.request.map { request -> request.dependency }) }
    }

    val include: DependencyCollector = object : DependencyCollector by ordinaryInclude {
        override fun getDependencies(): Provider<Set<Dependency>> =
            ordinaryInclude.dependencies.zip(includeCollectors.generated.dependencies) { ordinary, generated -> ordinary + generated }
    }

    fun include(target: MinecraftTarget) {
        includeTargets.add(target)
    }

    abstract val api: DependencyCollector
    abstract val compileOnlyApi: DependencyCollector
    abstract val localRuntime: DependencyCollector
    abstract val localImplementation: DependencyCollector
    abstract val externalRuntime: DependencyCollector
    abstract val externalCompile: DependencyCollector
    abstract val externalApi: DependencyCollector

    abstract val modApi: DependencyCollector
    abstract val modCompileOnlyApi: DependencyCollector
    abstract val modImplementation: DependencyCollector
    abstract val modRuntimeOnly: DependencyCollector
    abstract val modCompileOnly: DependencyCollector
    abstract val modLocalRuntime: DependencyCollector
    abstract val modLocalImplementation: DependencyCollector

    val skipIncludeTransformation: SkipIncludeTransformationDependencyModifier = objectFactory.newInstance<SkipIncludeTransformationDependencyModifier>()
    val extractIncludes: ExtractIncludesDependencyModifier = objectFactory.newInstance<ExtractIncludesDependencyModifier>()
    val stripIncludes: StripIncludesDependencyModifier = objectFactory.newInstance<StripIncludesDependencyModifier>()

    fun fabricApi(apiVersion: String) {
        modApi.add(minecraftVersion.map {
            fabricApiDependency(apiVersion, it)
        })
    }

    fun fabricApi(apiVersion: Provider<String>) {
        addLazyFabricApi(apiVersion, minecraftVersion)
    }

    fun fabricApi(apiVersion: String, minecraftVersion: String) {
        modApi.add(fabricApiDependency(apiVersion, minecraftVersion))
    }

    fun fabricApi(apiVersion: Provider<String>, minecraftVersion: String) {
        modApi.add(apiVersion.map {
            fabricApiDependency(it, minecraftVersion)
        })
    }

    fun fabricApi(apiVersion: String, minecraftVersion: Provider<String>) {
        modApi.add(minecraftVersion.orElse(this.minecraftVersion).map {
            fabricApiDependency(apiVersion, it)
        })
    }

    fun fabricApi(apiVersion: Provider<String>, minecraftVersion: Provider<String>) {
        addLazyFabricApi(apiVersion, minecraftVersion.orElse(this.minecraftVersion))
    }

    private fun addLazyFabricApi(apiVersion: Provider<String>, minecraftVersion: Provider<String>) {
        modApi.add(
            apiVersion.zip(minecraftVersion, ::Pair)
                .map { (apiVersion, minecraftVersion) ->
                    fabricApiDependency(apiVersion, minecraftVersion)
                }
        )
    }

    private fun fabricApiDependency(apiVersion: String, minecraftVersion: String) =
        dependencyFactory.create("net.fabricmc.fabric-api", "fabric-api", "$apiVersion+$minecraftVersion")

    abstract class SkipIncludeTransformationDependencyModifier : DependencyModifier() {
        override fun modifyImplementation(dependency: ModuleDependency) {
            dependency.attributes {
                attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.None)
            }
        }
    }

    abstract class ExtractIncludesDependencyModifier : DependencyModifier() {
        override fun modifyImplementation(dependency: ModuleDependency) {
            dependency.attributes {
                attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.Extracted)
            }
        }
    }

    abstract class StripIncludesDependencyModifier : DependencyModifier() {
        override fun modifyImplementation(dependency: ModuleDependency) {
            dependency.attributes {
                attribute(IncludeTransformationStateAttribute.ATTRIBUTE, IncludeTransformationStateAttribute.Stripped)
            }
        }
    }
}
