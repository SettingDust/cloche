package earth.terrarium.cloche.api.target.compilation

import earth.terrarium.cloche.api.attributes.RemapNamespaceAttribute
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler

fun <T : ModuleDependency> T.remapNamespace(namespace: String): T {
    @Suppress("DEPRECATION")
    attributes {
        attribute(RemapNamespaceAttribute.ATTRIBUTE, namespace)
    }

    return this
}

@Deprecated(
    "Use modImplementation(notation, sourceNamespace = namespace) or the matching mod* dependency DSL instead. " +
        "This compatibility helper writes a legacy declaration attribute that Cloche strips before internal buckets.",
    ReplaceWith("modImplementation(dependencyNotation, sourceNamespace = namespace)"),
)
fun DependencyHandler.remapNamespace(dependencyNotation: Any, namespace: String): ModuleDependency =
    (create(dependencyNotation) as ModuleDependency).remapNamespace(namespace)
