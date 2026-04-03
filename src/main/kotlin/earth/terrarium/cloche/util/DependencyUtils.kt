package earth.terrarium.cloche.util

import earth.terrarium.cloche.ClocheExtension
import earth.terrarium.cloche.ClocheTargetAttribute
import earth.terrarium.cloche.api.target.ClocheTarget
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.jvm.JvmComponentDependencies
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.the

fun DependencyFactory.createLoaderDependency(
    group: String,
    artifact: String,
    version: String,
    configure: ExternalModuleDependency.() -> Unit = {}
): ExternalModuleDependency {
    return create(group, artifact, null).apply {
        version {
            strictly(version)
        }

        configure()
    }
}

@Suppress("UnstableApiUsage")
private fun <T : ModuleDependency> T.forTarget(target: ClocheTarget) = apply {
    capabilities {
        requireFeature(target.capabilitySuffix!!)
    }

    attributes {
        attribute(ClocheTargetAttribute.ATTRIBUTE, target.name)
    }
}

@Suppress("UnstableApiUsage")
fun DependencyHandler.target(target: ClocheTarget) = project(":").forTarget(target)

fun DependencyHandler.target(name: String) = target(the<ClocheExtension>().targets[name])

@Suppress("UnstableApiUsage")
fun JvmComponentDependencies.target(target: ClocheTarget) = project.dependencies.target(target)

fun JvmComponentDependencies.target(name: String) = target(project.the<ClocheExtension>().targets[name])