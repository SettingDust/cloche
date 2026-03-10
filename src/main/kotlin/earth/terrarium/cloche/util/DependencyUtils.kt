package earth.terrarium.cloche.util

import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyFactory

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
