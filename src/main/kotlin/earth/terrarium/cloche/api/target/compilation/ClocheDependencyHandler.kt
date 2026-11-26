package earth.terrarium.cloche.api.target.compilation

import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import earth.terrarium.cloche.api.target.MinecraftTarget
import org.gradle.api.file.FileCollection
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
abstract class ClocheDependencyHandler @Inject constructor(private val minecraftVersion: Provider<String>) : CompilationDependencies {

    internal abstract class IncludeCollectors {
        abstract val ordinary: DependencyCollector
        abstract val generated: DependencyCollector
    }

    private val includeCollectors = objectFactory.newInstance<IncludeCollectors>()
    internal val ordinaryInclude: DependencyCollector get() = includeCollectors.ordinary
    internal val includeTargets = IncludeTargets().apply {
        all { includeCollectors.generated.add(it.request.map { request -> request.dependency }) }
    }

    override val include: DependencyCollector = object : DependencyCollector by ordinaryInclude {
        override fun getDependencies(): Provider<Set<Dependency>> =
            ordinaryInclude.dependencies.zip(includeCollectors.generated.dependencies) { ordinary, generated -> ordinary + generated }
    }

    override fun include(target: MinecraftTarget) {
        includeTargets.add(target)
    }

    override abstract val api: DependencyCollector
    override abstract val compileOnlyApi: DependencyCollector
    override abstract val localRuntime: DependencyCollector
    override abstract val localImplementation: DependencyCollector
    override abstract val externalRuntime: DependencyCollector
    override abstract val externalCompile: DependencyCollector
    override abstract val externalApi: DependencyCollector

    override abstract val modApi: DependencyCollector
    override abstract val modCompileOnlyApi: DependencyCollector
    override abstract val modImplementation: DependencyCollector
    override abstract val modRuntimeOnly: DependencyCollector
    override abstract val modCompileOnly: DependencyCollector
    override abstract val modLocalRuntime: DependencyCollector
    override abstract val modLocalImplementation: DependencyCollector

    override val skipIncludeTransformation: SkipIncludeTransformationDependencyModifier = objectFactory.newInstance<SkipIncludeTransformationDependencyModifier>()
    override val extractIncludes: ExtractIncludesDependencyModifier = objectFactory.newInstance<ExtractIncludesDependencyModifier>()
    override val stripIncludes: StripIncludesDependencyModifier = objectFactory.newInstance<StripIncludesDependencyModifier>()

    override fun fabricApi(apiVersion: String) {
        modApi.add(minecraftVersion.map {
            fabricApiDependency(apiVersion, it)
        })
    }

    override fun fabricApi(apiVersion: Provider<String>) {
        addLazyFabricApi(apiVersion, minecraftVersion)
    }

    override fun fabricApi(apiVersion: String, minecraftVersion: String) {
        modApi.add(fabricApiDependency(apiVersion, minecraftVersion))
    }

    override fun fabricApi(apiVersion: Provider<String>, minecraftVersion: String) {
        modApi.add(apiVersion.map {
            fabricApiDependency(it, minecraftVersion)
        })
    }

    override fun fabricApi(apiVersion: String, minecraftVersion: Provider<String>) {
        modApi.add(minecraftVersion.orElse(this.minecraftVersion).map {
            fabricApiDependency(apiVersion, it)
        })
    }

    override fun fabricApi(apiVersion: Provider<String>, minecraftVersion: Provider<String>) {
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

internal abstract class ScopedDependencyHandler @Inject constructor(version: Provider<String>) :
    ClocheDependencyHandler(version), MainCompilationDependencies {
    internal lateinit var requestGame: (String, String, Boolean) -> FileCollection
    internal lateinit var addRemapClasspath: (FileCollection) -> Unit
    override fun forgeMinecraft(minecraftVersion: String, loaderVersion: String) = requestGame(minecraftVersion, loaderVersion, false)
    override fun neoforgeMinecraft(minecraftVersion: String, loaderVersion: String) = requestGame(minecraftVersion, loaderVersion, true)
    override fun remapClasspath(files: FileCollection) = addRemapClasspath(files)
}
