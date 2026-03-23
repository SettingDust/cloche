package earth.terrarium.cloche.api.target.compilation

import earth.terrarium.cloche.api.attributes.IncludeTransformationStateAttribute
import earth.terrarium.cloche.api.attributes.ModDistribution
import earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute
import earth.terrarium.cloche.maybeRegister
import earth.terrarium.cloche.util.createLoaderDependency
import net.msrandom.minecraftcodev.core.task.ResolveMinecraftClient
import net.msrandom.minecraftcodev.core.task.ResolveMinecraftCommon
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.forge.task.ResolvePatchedMinecraft
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.artifacts.dsl.DependencyModifier
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.plugins.jvm.JvmComponentDependencies
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class ClocheDependencyHandler @Inject constructor(
    private val minecraftVersion: Provider<String>,
    private val project: Project,
) : JvmComponentDependencies {
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

    val skipIncludeTransformation: SkipIncludeTransformationDependencyModifier =
        project.objects.newInstance<SkipIncludeTransformationDependencyModifier>()
    val extractIncludes: ExtractIncludesDependencyModifier =
        project.objects.newInstance<ExtractIncludesDependencyModifier>()
    val stripIncludes: StripIncludesDependencyModifier = project.objects.newInstance<StripIncludesDependencyModifier>()

    fun forgeMinecraft(version: String): FileCollection =
        forgeMinecraft(project.provider { version })

    fun forgeMinecraft(version: Provider<String>): FileCollection =
        patchedMinecraft("net.minecraftforge", "forge", version)

    fun neoforgeMinecraft(version: String): FileCollection =
        neoforgeMinecraft(project.provider { version })

    fun neoforgeMinecraft(version: Provider<String>): FileCollection =
        patchedMinecraft("net.neoforged", "neoforge", version)

    fun patchedMinecraft(group: String, artifact: String, version: Provider<String>): FileCollection {
        val taskName = lowerCamelCaseGradleName("resolve", artifact, "patchedMinecraft", version.get())
        val taskProvider = project.tasks.maybeRegister<ResolvePatchedMinecraft>(taskName) {
            val universalConfigName = lowerCamelCaseGradleName(artifact, "universal", version.get())
            val universalConfig = project.configurations.maybeCreate(universalConfigName)
            universalConfig.isCanBeConsumed = false

            project.dependencies.add(
                universalConfig.name,
                dependencyFactory.createLoaderDependency(group, artifact, version.get())
            )

            this.group = "minecraft-resolution"
            this.minecraftVersion.set(version)
            universal.from(universalConfig)
            output.set(output(artifact, version.get(), RemapNamespaceAttribute.SEARGE))
        }

        return project.files(taskProvider).builtBy(taskProvider)
    }

    fun fabricMinecraft(version: String, side: ModDistribution = ModDistribution.common): FileCollection =
        fabricMinecraft(project.provider { version }, side)

    fun fabricMinecraft(version: Provider<String>, side: ModDistribution = ModDistribution.common): FileCollection {
        val taskName = lowerCamelCaseGradleName("resolve", "fabric", side.name, version.get())
        return project.files(
            when (side) {
            ModDistribution.common -> {
                project.tasks.maybeRegister<ResolveMinecraftCommon>(taskName) {
                    group = "minecraft-resolution"
                    this.minecraftVersion.set(version)
                    output.set(output("fabric-common", version.get()))
                }
            }

            ModDistribution.client -> {
                project.tasks.maybeRegister<ResolveMinecraftClient>(taskName) {
                    group = "minecraft-resolution"
                    this.minecraftVersion.set(version)
                    output.set(output("fabric-client", version.get(), "client"))
                }
            }
        })
    }

    private fun output(name: String, version: String, suffix: String = ""): Provider<RegularFile> =
        project.layout.buildDirectory.file("minecraft-resolution/$name-$version${if (suffix.isNotEmpty()) "-$suffix" else ""}.jar")

    fun fabricApi(apiVersion: String) {
        modImplementation.add(minecraftVersion.map {
            fabricApiDependency(apiVersion, it)
        })
    }

    fun fabricApi(apiVersion: Provider<String>) {
        addLazyFabricApi(apiVersion, minecraftVersion)
    }

    fun fabricApi(apiVersion: String, minecraftVersion: String) {
        modImplementation.add(fabricApiDependency(apiVersion, minecraftVersion))
    }

    fun fabricApi(apiVersion: Provider<String>, minecraftVersion: String) {
        modImplementation.add(apiVersion.map {
            fabricApiDependency(it, minecraftVersion)
        })
    }

    fun fabricApi(apiVersion: String, minecraftVersion: Provider<String>) {
        modImplementation.add(minecraftVersion.orElse(this.minecraftVersion).map {
            fabricApiDependency(apiVersion, it)
        })
    }

    fun fabricApi(apiVersion: Provider<String>, minecraftVersion: Provider<String>) {
        addLazyFabricApi(apiVersion, minecraftVersion.orElse(this.minecraftVersion))
    }

    private fun addLazyFabricApi(apiVersion: Provider<String>, minecraftVersion: Provider<String>) {
        modImplementation.add(
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
