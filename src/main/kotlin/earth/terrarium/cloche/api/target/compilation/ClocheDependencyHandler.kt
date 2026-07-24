package earth.terrarium.cloche.api.target.compilation

import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import org.gradle.api.Action
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.artifacts.dsl.DependencyModifier
import org.gradle.api.file.FileCollection
import org.gradle.api.Project
import org.gradle.api.plugins.jvm.JvmComponentDependencies
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class ClocheDependencyHandler @Inject constructor(
    private val minecraftVersion: Provider<String>
) : JvmComponentDependencies {
    private val declaredModDependencies = mutableListOf<DeclaredModDependency>()

    abstract val include: DependencyCollector

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

    abstract val remapClasspath: DependencyCollector

    fun modApi(dependencyNotation: Any, sourceNamespace: String) =
        addModDependency(modApi, dependencyNotation, sourceNamespace)

    fun modCompileOnlyApi(dependencyNotation: Any, sourceNamespace: String) =
        addModDependency(modCompileOnlyApi, dependencyNotation, sourceNamespace)

    fun modImplementation(dependencyNotation: Any, sourceNamespace: String) =
        addModDependency(modImplementation, dependencyNotation, sourceNamespace)

    fun modRuntimeOnly(dependencyNotation: Any, sourceNamespace: String) =
        addModDependency(modRuntimeOnly, dependencyNotation, sourceNamespace)

    fun modCompileOnly(dependencyNotation: Any, sourceNamespace: String) =
        addModDependency(modCompileOnly, dependencyNotation, sourceNamespace)

    fun modLocalRuntime(dependencyNotation: Any, sourceNamespace: String) =
        addModDependency(modLocalRuntime, dependencyNotation, sourceNamespace)

    fun modLocalImplementation(dependencyNotation: Any, sourceNamespace: String) =
        addModDependency(modLocalImplementation, dependencyNotation, sourceNamespace)

    fun modApi(dependencyNotation: Any, sourceNamespace: String, configure: Action<in ModuleDependency>) =
        addModDependency(modApi, dependencyNotation, sourceNamespace, configure)

    fun modCompileOnlyApi(dependencyNotation: Any, sourceNamespace: String, configure: Action<in ModuleDependency>) =
        addModDependency(modCompileOnlyApi, dependencyNotation, sourceNamespace, configure)

    fun modImplementation(dependencyNotation: Any, sourceNamespace: String, configure: Action<in ModuleDependency>) =
        addModDependency(modImplementation, dependencyNotation, sourceNamespace, configure)

    fun modRuntimeOnly(dependencyNotation: Any, sourceNamespace: String, configure: Action<in ModuleDependency>) =
        addModDependency(modRuntimeOnly, dependencyNotation, sourceNamespace, configure)

    fun modCompileOnly(dependencyNotation: Any, sourceNamespace: String, configure: Action<in ModuleDependency>) =
        addModDependency(modCompileOnly, dependencyNotation, sourceNamespace, configure)

    fun modLocalRuntime(dependencyNotation: Any, sourceNamespace: String, configure: Action<in ModuleDependency>) =
        addModDependency(modLocalRuntime, dependencyNotation, sourceNamespace, configure)

    fun modLocalImplementation(dependencyNotation: Any, sourceNamespace: String, configure: Action<in ModuleDependency>) =
        addModDependency(modLocalImplementation, dependencyNotation, sourceNamespace, configure)

    private fun addModDependency(
        collector: DependencyCollector,
        dependencyNotation: Any,
        sourceNamespace: String,
        configure: Action<in ModuleDependency>? = null,
    ) {
        val dependency = createDependency(dependencyNotation)
        val moduleDependency = dependency as? ModuleDependency
        if (configure != null) {
            require(moduleDependency != null) {
                "sourceNamespace dependency configuration requires a ModuleDependency, got ${dependency.javaClass.name}"
            }
            configure.execute(moduleDependency)
        }
        require(sourceNamespace.isNotBlank()) { "sourceNamespace must not be blank" }
        declaredModDependencies += DeclaredModDependency(collector, dependency, sourceNamespace)
    }

    internal fun declaredModDependenciesFor(collectors: Collection<DependencyCollector>): List<DeclaredModDependency> =
        declaredModDependencies.filter { it.collector in collectors }

    val skipIncludeTransformation: SkipIncludeTransformationDependencyModifier =
        project.objects.newInstance<SkipIncludeTransformationDependencyModifier>()
    val extractIncludes: ExtractIncludesDependencyModifier =
        project.objects.newInstance<ExtractIncludesDependencyModifier>()
    val stripIncludes: StripIncludesDependencyModifier = project.objects.newInstance<StripIncludesDependencyModifier>()

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

    private fun createDependency(dependencyNotation: Any): Dependency =
        when (dependencyNotation) {
            is CharSequence -> dependencyFactory.create(dependencyNotation) as ExternalModuleDependency
            is FileCollection -> dependencyFactory.create(dependencyNotation) as FileCollectionDependency
            is Project -> dependencyFactory.create(dependencyNotation) as ProjectDependency
            is Dependency -> dependencyNotation
            else -> error("Unsupported mod dependency notation type: ${dependencyNotation.javaClass.name}")
        }

    internal data class DeclaredModDependency(
        val collector: DependencyCollector,
        val dependency: Dependency,
        val sourceNamespace: String,
    )

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
