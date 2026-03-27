package earth.terrarium.cloche.api.target

import earth.terrarium.cloche.api.attributes.ModDistribution
import earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute
import earth.terrarium.cloche.util.createLoaderDependency
import earth.terrarium.cloche.util.maybeRegister
import net.msrandom.minecraftcodev.core.MinecraftOperatingSystemAttribute
import net.msrandom.minecraftcodev.core.operatingSystemName
import net.msrandom.minecraftcodev.core.task.ResolveMinecraftClient
import net.msrandom.minecraftcodev.core.task.ResolveMinecraftCommon
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.forge.task.ResolvePatchedMinecraft
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.named

// ==================== Internal functions ====================

internal fun Project.createResolvePatchedMinecraftTask(
    group: String,
    artifact: String,
    minecraftVersion: Provider<String>,
    loaderVersion: Provider<String>,
    neoforge: Boolean = false
): Provider<ResolvePatchedMinecraft> {
    val taskName = lowerCamelCaseGradleName("resolve", artifact, "patchedMinecraft", loaderVersion.get())
    return tasks.maybeRegister<ResolvePatchedMinecraft>(taskName) {
        fun createConfiguration(name: String): Configuration =
            project.configurations.create(lowerCamelCaseGradleName(loaderVersion.get(), name)) {
                isCanBeConsumed = false

                attributes.attribute(
                    MinecraftOperatingSystemAttribute.attribute,
                    objects.named(operatingSystemName()),
                )

                attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
            }

        val minecraftLibrariesConfiguration = createConfiguration("minecraftLibraries")
        val universalConfiguration = createConfiguration("universal")
        val patchesConfiguration = createConfiguration("patches")

        fun forgeDependency(configure: ExternalModuleDependency.() -> Unit): Provider<ExternalModuleDependency> =
            loaderVersion.map { forgeVersion ->
                dependencyFactory.createLoaderDependency(
                    group,
                    artifact,
                    forgeVersion,
                    configure
                )
            }

        dependencies.add(minecraftLibrariesConfiguration.name, forgeDependency {
            capabilities {
                requireFeature("dependencies")
            }
        })

        dependencies.add(universalConfiguration.name, forgeDependency {})

        dependencies.add(patchesConfiguration.name, forgeDependency {
            capabilities {
                requireFeature("moddev-bundle")
            }
        })

        this.group = "minecraft-resolution"
        this.minecraftVersion.set(minecraftVersion)
        this.universal.from(universalConfiguration)
        this.output.set(
            layout.buildDirectory.file(
                "minecraft-resolution/$artifact-${loaderVersion.get()}-${RemapNamespaceAttribute.SEARGE}.jar"
            )
        )
        this.neoforge.set(neoforge)
        this.patches.from(patchesConfiguration)
        this.libraries.from(minecraftLibrariesConfiguration)
    }
}

internal fun Project.getPatchedMinecraftFiles(
    group: String,
    artifact: String,
    minecraftVersion: Provider<String>,
    loaderVersion: Provider<String>,
    neoforge: Boolean = false
): FileCollection {
    val taskProvider = createResolvePatchedMinecraftTask(group, artifact, minecraftVersion, loaderVersion, neoforge)
    return files(taskProvider.flatMap { it.output }).builtBy(taskProvider)
}

// ==================== Project extensions ====================

fun Project.patchedMinecraft(
    group: String,
    artifact: String,
    minecraftVersion: String,
    loaderVersion: String,
    neoforge: Boolean = false
): FileCollection {
    return getPatchedMinecraftFiles(
        group,
        artifact,
        provider { minecraftVersion },
        provider { loaderVersion },
        neoforge
    )
}

fun Project.patchedMinecraft(
    group: String,
    artifact: String,
    minecraftVersion: Provider<String>,
    loaderVersion: Provider<String>,
    neoforge: Boolean = false
): FileCollection {
    return getPatchedMinecraftFiles(group, artifact, minecraftVersion, loaderVersion, neoforge)
}

fun Project.forgeMinecraft(minecraftVersion: String, loaderVersion: String): FileCollection {
    return patchedMinecraft("net.minecraftforge", "forge", minecraftVersion, "$minecraftVersion-$loaderVersion", false)
}

fun Project.forgeMinecraft(minecraftVersion: Provider<String>, loaderVersion: Provider<String>): FileCollection {
    return patchedMinecraft(
        "net.minecraftforge",
        "forge",
        minecraftVersion,
        minecraftVersion.zip(loaderVersion) { minecraftVersion, loaderVersion -> "$minecraftVersion-$loaderVersion" },
        false
    )
}

fun Project.neoForgeMinecraft(minecraftVersion: String, loaderVersion: String): FileCollection {
    return patchedMinecraft("net.neoforged", "neoforge", minecraftVersion, loaderVersion, true)
}

fun Project.neoForgeMinecraft(minecraftVersion: Provider<String>, loaderVersion: Provider<String>): FileCollection {
    return patchedMinecraft("net.neoforged", "neoforge", minecraftVersion, loaderVersion, true)
}


fun Project.fabricMinecraft(version: String, side: ModDistribution = ModDistribution.common): FileCollection =
    fabricMinecraft(provider { version }, side)

fun Project.fabricMinecraft(version: Provider<String>, side: ModDistribution = ModDistribution.common): FileCollection {
    val taskName = lowerCamelCaseGradleName("resolve", "fabric", side.name, version.get())
    return files(
        when (side) {
            ModDistribution.common -> {
                tasks.maybeRegister<ResolveMinecraftCommon>(taskName) {
                    group = "minecraft-resolution"
                    this.minecraftVersion.set(version)
                    output.set(output("fabric-common", version.get()))
                }
            }

            ModDistribution.client -> {
                tasks.maybeRegister<ResolveMinecraftClient>(taskName) {
                    group = "minecraft-resolution"
                    this.minecraftVersion.set(version)
                    output.set(output("fabric-client", version.get(), "client"))
                }
            }
        })
}

private fun Project.output(name: String, version: String, suffix: String = ""): Provider<RegularFile> =
    layout.buildDirectory.file("minecraft-resolution/$name-$version${if (suffix.isNotEmpty()) "-$suffix" else ""}.jar")