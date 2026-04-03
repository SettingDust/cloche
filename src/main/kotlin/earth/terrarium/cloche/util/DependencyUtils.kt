package earth.terrarium.cloche.util

import earth.terrarium.cloche.ClocheExtension
import earth.terrarium.cloche.ClocheTargetAttribute
import earth.terrarium.cloche.api.target.ClocheTarget
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyFactory
import org.gradle.api.artifacts.dsl.DependencyHandler
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
fun DependencyHandler.target(target: ClocheTarget) = project(":").apply {
    capabilities {
        requireFeature(target.capabilitySuffix!!)
    }

    attributes {
        attribute(ClocheTargetAttribute.ATTRIBUTE, target.name)
    }
}

fun DependencyHandler.target(name: String) = target(the<ClocheExtension>().targets[name])