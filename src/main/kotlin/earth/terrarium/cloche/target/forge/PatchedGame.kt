package earth.terrarium.cloche.target.forge

import net.msrandom.minecraftcodev.core.MinecraftOperatingSystemAttribute
import net.msrandom.minecraftcodev.core.operatingSystemName
import net.msrandom.minecraftcodev.core.utils.lowerCamelCaseGradleName
import net.msrandom.minecraftcodev.forge.task.ResolvePatchedMinecraft
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

internal enum class LoaderModule(val group: String, val artifact: String) {
    FORGE("net.minecraftforge", "forge"),
    NEOFORGE("net.neoforged", "neoforge"),
}

internal fun forgeModuleVersion(minecraft: String, loader: String) = "$minecraft-$loader"
internal fun neoForgeModuleVersion(@Suppress("UNUSED_PARAMETER") minecraft: String, loader: String) = loader

internal fun loaderDependency(
    project: Project, group: Provider<String>, artifact: Provider<String>, minecraft: Provider<String>, loader: Provider<String>,
    version: (String, String) -> String, configure: ExternalModuleDependency.() -> Unit,
): Provider<ExternalModuleDependency> = minecraft.zip(loader, version).flatMap { coordinateVersion ->
    group.zip(artifact) { g, a ->
        project.dependencyFactory.create(g, a, null).apply {
            version { strictly(coordinateVersion) }
            configure()
        }
    }
}

internal data class PatchedGame(val libraries: Configuration, val universal: Configuration, val producer: TaskProvider<ResolvePatchedMinecraft>)

internal fun registerPatchedGame(project: Project, feature: String?, minecraft: Provider<String>, output: Provider<RegularFile>): PatchedGame {
    val libraries = project.configurations.create(lowerCamelCaseGradleName(feature, "minecraftLibraries")) {
        isCanBeConsumed = false
        attributes.attribute(MinecraftOperatingSystemAttribute.attribute, project.objects.named(operatingSystemName()))
        attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
    }
    val universal = project.configurations.create(lowerCamelCaseGradleName(feature, "forgeUniversal")) { isCanBeConsumed = false }
    val patched = project.tasks.register<ResolvePatchedMinecraft>(lowerCamelCaseGradleName("resolve", feature, "patchedMinecraft")) {
        group = "minecraft-resolution"
        minecraftVersion.set(minecraft)
        this.universal.from(universal)
        this.output.set(output)
    }
    return PatchedGame(libraries, universal, patched)
}

internal fun wirePatchedGame(
    project: Project, producer: TaskProvider<ResolvePatchedMinecraft>, libraries: Configuration, universal: Configuration,
    patches: Configuration, neo: Boolean, dependency: (ExternalModuleDependency.() -> Unit) -> Provider<ExternalModuleDependency>,
): Provider<ExternalModuleDependency> {
    val userdev = dependency { capabilities { requireFeature("moddev-bundle") } }
    patches.dependencies.addLater(userdev)
    libraries.dependencies.addLater(dependency { capabilities { requireFeature("dependencies") } })
    universal.dependencies.addLater(dependency {})
    if (neo) {
        libraries.attributes.attribute(Attribute.of("net.neoforged.distribution", String::class.java), "client")
        libraries.attributes.attribute(Attribute.of("net.neoforged.operatingsystem", String::class.java), operatingSystemName())
    }
    producer.configure {
        neoforge.set(neo)
        this.patches.from(patches)
        this.libraries.from(libraries)
    }
    return userdev
}

internal fun independentGame(project: Project, feature: String, mc: String, loader: String, neo: Boolean): FileCollection {
    val minecraft = project.provider { mc }
    val version = project.provider { loader }
    val module = if (neo) LoaderModule.NEOFORGE else LoaderModule.FORGE
    val group = project.provider { module.group }
    val artifact = project.provider { module.artifact }
    val (libraries, universal, producer) = registerPatchedGame(project, feature, minecraft,
        project.layout.buildDirectory.file("cloche/remap-classpath/$feature/patched.jar"))
    val patches = project.configurations.create(lowerCamelCaseGradleName(feature, "patches")) { isCanBeConsumed = false }
    wirePatchedGame(project, producer, libraries, universal, patches, neo) {
        loaderDependency(project, group, artifact, minecraft, version, if (neo) ::neoForgeModuleVersion else ::forgeModuleVersion, it)
    }
    return project.files(producer.flatMap(ResolvePatchedMinecraft::output), libraries)
}
