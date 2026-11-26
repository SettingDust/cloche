import earth.terrarium.cloche.ClocheExtension
import earth.terrarium.cloche.api.attributes.CompilationAttributes
import earth.terrarium.cloche.api.attributes.MinecraftModLoader
import earth.terrarium.cloche.api.attributes.ModDistribution
import earth.terrarium.cloche.api.attributes.TargetAttributes
import earth.terrarium.cloche.api.target.FabricTarget
import earth.terrarium.cloche.api.target.MinecraftTarget
import earth.terrarium.cloche.api.target.compilation.Compilation
import net.msrandom.minecraftcodev.core.MinecraftCodevPlugin
import net.msrandom.minecraftcodev.includes.*
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.component.ProjectComponentSelector
import org.gradle.api.artifacts.dsl.DependencyCollector
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage

plugins { id("earth.terrarium.cloche") }

val scenario = providers.gradleProperty("fixtureScenario").get()
println(
    "FIXTURE_TOOLCHAIN Gradle ${gradle.gradleVersion} JVM ${System.getProperty(
        "java.version",
    )} ${System.getProperty("java.home")} HOME ${gradle.gradleUserHomeDir}",
)
mapOf(
    IncludedJarInfo::class.java to "minecraft-codev-includes-0.1.0+terrarium.0.6.5.jar",
    IncludesJar::class.java to "minecraft-codev-includes-0.1.0+terrarium.0.6.5.jar",
    IncludeTargetOrigin::class.java to "minecraft-codev-includes-0.1.0+terrarium.0.6.5.jar",
    IncludeTargetSource::class.java to "minecraft-codev-includes-0.1.0+terrarium.0.6.5.jar",
    ResolvedIncludeSource::class.java to "minecraft-codev-includes-0.1.0+terrarium.0.6.5.jar",
    MinecraftCodevPlugin::class.java to "minecraft-codev-core-0.1.0+terrarium.0.7.2.jar",
    Class.forName("net.msrandom.minecraftcodev.annotations.UnsafeForClient") to "side-annotations-1.0.1.jar",
).forEach { (type, expected) ->
    val source =
        File(
            type.protectionDomain.codeSource.location
                .toURI(),
        ).canonicalFile.toPath()
    // TestKit may instrument the published JAR without changing its artifact suffix.
    check(source.toFile().isFile && source.fileName.toString().endsWith(expected)) { "Unexpected implementation: $type $source" }
    val resources = type.classLoader.getResources(type.name.replace('.', '/') + ".class").toList()
    check(resources.size == 1) { "Duplicate implementation: $type $resources" }
    println("CODE_SOURCE ${type.name} $source RESOURCES ${resources.size}")
}

allprojects { repositories { maven { url = uri(rootProject.file("repository")) } } }

fun cloche(
    project: Project,
    minecraft: String = "1.20.1",
): ClocheExtension {
    project.pluginManager.apply("earth.terrarium.cloche")
    return project.extensions.getByType<ClocheExtension>().apply {
        minecraftVersion.set(minecraft)
        metadata.modId.set("fixture_" + project.name)
    }
}

val fabric = scenario in listOf("fabric", "inheritance")

fun target(
    extension: ClocheExtension,
    name: String? = null,
): MinecraftTarget =
    when {
        name == null && fabric -> extension.singleTarget { fabric { loaderVersion.set("0.16.10") } }
        name == null -> extension.singleTarget { forge { loaderVersion.set("47.2.0") } }
        fabric -> extension.fabric(name) { loaderVersion.set("0.16.10") }
        else -> extension.forge(name) { loaderVersion.set("47.2.0") }
    }

fun payload(
    project: Project,
    target: MinecraftTarget,
    label: String,
) {
    val jar =
        project.tasks.register<Jar>("fixture" + label.uppercase()) {
            archiveFileName.set("$label.jar")
            destinationDirectory.set(project.layout.buildDirectory.dir("payload"))
            from(rootProject.file("payloads/$label.txt")) { rename { "payload.txt" } }
        }
    project.configurations.named(target.sourceSet.runtimeElementsConfigurationName) {
        // Replace only the expensive producer artifact; keep production capabilities and attributes.
        outgoing.artifacts.clear()
        outgoing.artifact(jar)
    }
}

val producer = project(":producer")
producer.group = "early.targets"
producer.version = "0.1"
val producerCloche = cloche(producer, if (scenario == "incompatible") "1.20.2" else "1.20.1")
val a = target(producerCloche, "demo:a")
val b = target(producerCloche, "demo:b")
payload(producer, a, "a")
payload(producer, b, "b")
a.dependencies { externalRuntime("org.fixture:indirect:9.9") }

val single = project(":single")
single.group = "early.single"
single.version = "0.2"
val rootTarget = target(cloche(single))
check(rootTarget.name.isEmpty())
payload(single, rootTarget, "single")

val library = project(":library")
library.pluginManager.apply("java-library")
library.group = "ordinary.lib"
library.version = "2.3"
library.dependencies.add("runtimeOnly", "org.fixture:indirect:9.9")
library.tasks.named<Jar>("jar") { from(rootProject.file("payloads/library.txt")) { rename { "payload.txt" } } }
library.configurations.named("runtimeElements") {
    outgoing.capability("ordinary.lib:library:2.3")
    outgoing.capability("chosen.group:library-extra:7.6")
}

val collisionTarget =
    if (scenario == "collision") {
        val other = project(":other:producer")
        other.group = "early.targets"
        other.version = "0.1"
        val extension = cloche(other)
        target(extension, "unused")
        target(extension, "demo-a").also { payload(other, it, "a") }
    } else {
        null
    }

val consumer = project(":consumer")
consumer.group = "consumer.group"
consumer.version = "1.0"
val consumerTarget = target(cloche(consumer))
// Establish both real Linkage paths before registering targets, exercising late propagation.
val inheritors: List<Compilation> = if (scenario == "inheritance") {
    val fabricTarget = consumerTarget as FabricTarget
    val client = fabricTarget.client.configure()
    listOf(fabricTarget.data.configure(), client, client.data.configure())
} else emptyList()
lateinit var collector: DependencyCollector
var ordinaryActionCalls = 0
val ordinaryProject = consumer.dependencies.project(mapOf("path" to ":producer")) as ProjectDependency
ordinaryProject.capabilities { requireCapability("late.targets:producer-demo-a") }
consumerTarget.dependencies {
    collector = include
    if (scenario == "mixed-default") {
        include(rootTarget)
        include(consumer.dependencies.project(mapOf("path" to ":single")))
    } else if (scenario != "ordinary") {
        include(a)
        include(a)
        if (scenario in listOf("positive", "inheritance", "fabric") || scenario.startsWith("mixed-")) include(b)
        collisionTarget?.let { include(it) }
    }
    if (scenario in listOf("positive", "single", "inheritance", "ordinary")) {
        if (scenario != "ordinary") include(rootTarget)
        include("org.fixture:string-lib:1.2")
        include(consumer.dependencies.project(mapOf("path" to ":library")))
        include(consumer.provider { consumer.dependencies.create("org.fixture:provider-lib:3.4") })
        include.add("org.fixture:string-lib:1.2") { ordinaryActionCalls++; because("ordinary action overload") }
        include.bundle(consumer.provider { listOf(consumer.dependencies.create("org.fixture:provider-lib:3.4")) })
    }
    if (scenario == "fabric") {
        include("org.fixture:string-lib:1.2")
        include(
            (consumer.dependencies.project(mapOf("path" to ":library")) as ProjectDependency).apply {
                capabilities { requireCapability("chosen.group:library-extra") }
            },
        )
        include("org.fixture:existing-lib:4.5")
    }
    if (scenario in listOf("mixed-strict", "mixed-required", "mixed-preferred", "mixed-project")) {
        val module = consumer.dependencies.create("ordinary:producer") as org.gradle.api.artifacts.ExternalModuleDependency
        module.capabilities { requireCapability("late.targets:producer-demo-a") }
        module.version {
            when (scenario) {
                "mixed-strict" -> {
                    strictly("[5,6)")
                }

                "mixed-preferred" -> {
                    require("")
                    prefer("5.6")
                }

                else -> {
                    require("[5,7)")
                }
            }
        }
        include(module)
        if (scenario == "mixed-project") {
            include(ordinaryProject)
            include(consumer.provider { ordinaryProject })
        }
    }
    if (scenario == "ambiguous") {
        include(
            (consumer.dependencies.project(mapOf("path" to ":producer")) as ProjectDependency).apply {
                capabilities {
                    requireCapability("early.targets:producer")
                    requireFeature("demo-a")
                }
            },
        )
    }
    include.addConstraint(consumer.dependencies.constraints.create("org.fixture:provider-lib:3.4"))
}
// Repeating the same object through each inheritance path must not register another graph.
inheritors.forEach { it.dependencies { include(a) } }

if (scenario == "missing") {
    listOf(a.sourceSet.apiElementsConfigurationName, a.sourceSet.runtimeElementsConfigurationName).forEach { name ->
        val frozen =
            producer.configurations
                .getByName(name)
                .outgoing.capabilities
        check(frozen.any { it.group == "early.targets" && it.name == "producer-demo-a" })
        println("EARLY_CAPABILITIES $name $frozen")
    }
}
producer.group = "late.targets"
producer.version = "5.6"
single.group = "late.single"
single.version = "6.7"
collisionTarget?.let {
    val other = project(":other:producer")
    other.group = "late.targets"
    other.version = "5.6"
}

val ordinary = consumer.configurations.named("includeFiles")
if (scenario in listOf("mixed-strict", "mixed-required", "mixed-preferred", "mixed-project")) {
    ordinary.configure {
        resolutionStrategy.dependencySubstitution {
            substitute(module("ordinary:producer")).using(
                variant(project(":producer")) {
                    capabilities { requireCapability("late.targets:producer-demo-a") }
                },
            )
        }
    }
}

fun targetGraphs(prefix: String = "") =
    consumer.configurations.filter {
        it.name.matches(Regex(prefix + "[Ii]ncludeTarget[0-9]+Files"))
    }

fun verifyGraph(
    configuration: Configuration,
    data: Boolean,
    generated: Boolean,
    client: Boolean = false,
) {
    check(!configuration.isTransitive)
    val attributes = configuration.attributes
    val loader = if (fabric) MinecraftModLoader.fabric else MinecraftModLoader.forge
    val side = if (fabric && !client) ModDistribution.common else ModDistribution.client
    check(attributes.getAttribute(TargetAttributes.MOD_LOADER) == loader)
    check(attributes.getAttribute(TargetAttributes.CLOCHE_MOD_LOADER) == loader)
    check(attributes.getAttribute(TargetAttributes.MINECRAFT_VERSION) == "1.20.1")
    check(attributes.getAttribute(TargetAttributes.CLOCHE_MINECRAFT_VERSION) == "1.20.1")
    check(attributes.getAttribute(CompilationAttributes.DISTRIBUTION) == side)
    check(attributes.getAttribute(CompilationAttributes.CLOCHE_SIDE) == side.legacyName)
    check(attributes.getAttribute(CompilationAttributes.DATA) == data)
    check(attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name == Category.LIBRARY)
    check(attributes.getAttribute(Usage.USAGE_ATTRIBUTE)?.name == Usage.JAVA_RUNTIME)
    check(attributes.getAttribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE)?.name == LibraryElements.JAR)
    check(configuration.allDependencyConstraints.any { it.group == "org.fixture" && it.name == "provider-lib" })
    if (generated) check(configuration.allDependencies.size == 1)
    println(
        "INCLUDE_GRAPH ${configuration.name} ATTRIBUTES $attributes TRANSITIVE ${configuration.isTransitive} DEPS ${configuration.allDependencies} CONSTRAINTS ${configuration.allDependencyConstraints}",
    )
    val root =
        configuration.incoming.resolutionResult.rootComponent
            .get()
    root.dependencies.filterIsInstance<ResolvedDependencyResult>().forEach { edge ->
        println(
            "SELECTED ${edge.selected.id} VARIANT ${edge.resolvedVariant.displayName} REQUEST ${edge.requested} CAPABILITIES ${edge.requested.capabilitySelectors} OUTGOING ${edge.resolvedVariant.capabilities}",
        )
    }
    check(
        configuration.incoming.artifacts.artifacts
            .none { it.file.name.contains("indirect") },
    )
}

val expectedGraphs =
    when (scenario) {
        "ordinary" -> 0
        "positive", "inheritance" -> 3
        "single" -> 2
        "fabric", "collision" -> 2
        "mixed-default" -> 1
        else -> if (scenario.startsWith("mixed-")) 2 else 1
    }
check(targetGraphs().size == expectedGraphs)
check(
    consumer.configurations
        .filter {
            it.name.contains("IncludeTarget") || it.name.startsWith("includeTarget") || it.name == "includeFiles"
        }.all {
            it.state ==
                Configuration.State.UNRESOLVED
        },
)
check(
    ordinary.get().allDependencies.none {
        it is ProjectDependency && it !== ordinaryProject &&
            scenario in listOf("positive", "inheritance", "fabric") &&
            it.path != ":library"
    },
)
println("CONFIGURATION_UNRESOLVED production include registration TARGET_GRAPHS ${targetGraphs().size}")
collector.dependencies.get().filterIsInstance<ProjectDependency>().forEach { dependency ->
    dependency.requestedCapabilities.forEach { capability -> println("REQUEST ${dependency.path} ${capability.group}:${capability.name}") }
}
check(collector.dependencyConstraints.get().size == 1)

val outer =
    consumer.tasks.register<Jar>("fixtureOuter") {
        archiveFileName.set("outer.jar")
        destinationDirectory.set(consumer.layout.buildDirectory.dir("outer"))
        if (fabric) from(rootProject.file("metadata/outer"))
    }
// Configure the production task, retaining its original multi-source wiring and producer dependencies.
consumer.tasks.named<IncludesJar>("includeJar") {
    input.set(outer.flatMap { it.archiveFile })
    manifest = outer.get().manifest
    archiveFileName.set(if (fabric) "fabric.jar" else "forge.jar")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("included"))
    doFirst {
        includedJarInfo.get().forEach { info ->
            println(
                "INCLUDED ${info.group.get()}:${info.moduleName.get()}:${info.artifactVersion.get()} ${info.versionRange.get()} ${info.file.get().asFile}",
            )
        }
        if (scenario == "collision") check(includedJarInfo.get().size == 2)
    }
    doLast {
        check(targetGraphs().size == expectedGraphs)
        verifyGraph(ordinary.get(), false, false)
        targetGraphs().forEach { verifyGraph(it, false, true) }
        inheritors.forEach { compilation ->
            val prefix = compilation.sourceSet.name
            val inheritedGraphs = targetGraphs(prefix)
            check(inheritedGraphs.size == 3)
            val inheritedOrdinary = consumer.configurations.getByName(prefix + "IncludeFiles")
            check(inheritedOrdinary.allDependencies.size == ordinary.get().allDependencies.size)
            val data = prefix == "data" || prefix == "clientData"
            val client = prefix.startsWith("client")
            verifyGraph(inheritedOrdinary, data, false, client)
            inheritedGraphs.forEach { verifyGraph(it, data, true, client) }
            println("INHERITED $prefix TARGET_GRAPHS ${inheritedGraphs.size}")
        }
        if (scenario.startsWith("mixed-")) {
            val last =
                ordinary
                    .get()
                    .incoming.resolutionResult.rootComponent
                    .get()
                    .dependencies
                    .filterIsInstance<ResolvedDependencyResult>()
                    .last()
                    .requested
            if (scenario in listOf("mixed-default", "mixed-project")) {
                check(last is ProjectComponentSelector)
            } else {
                check(last is ModuleComponentSelector)
            }
            println("ORDINARY_LAST $scenario $last")
        }
        // A second read uses the same finalized request snapshot, even if producer coordinates change later.
        producer.group = "after.delivery"
        check(
            collector.dependencies.get().filterIsInstance<ProjectDependency>().flatMap { it.requestedCapabilities }.none {
                it.group ==
                    "after.delivery"
            },
        )
        println("SNAPSHOT_STABLE $scenario")
        if (scenario in listOf("positive", "single", "inheritance", "ordinary")) {
            check(ordinaryActionCalls == 1)
            println("ORDINARY_ACTION_CALLS $ordinaryActionCalls")
        }
    }
}
gradle.taskGraph.whenReady {
    val expected =
        when (scenario) {
            "ordinary" -> listOf(":library:jar")
            "mixed-default" -> {
                listOf(":single:fixtureSINGLE")
            }

            "collision" -> {
                listOf(":producer:fixtureA", ":other:producer:fixtureA")
            }

            else -> {
                listOf(":producer:fixtureA") +
                    (if (expectedGraphs >= 2 && scenario != "single") listOf(":producer:fixtureB") else emptyList()) +
                    (
                        if (scenario in
                            listOf("positive", "single", "inheritance")
                        ) {
                            listOf(":single:fixtureSINGLE", ":library:jar")
                        } else if (fabric) {
                            listOf(":library:jar")
                        } else {
                            emptyList()
                        }
                    )
            }
        }
    expected.forEach {
        check(hasTask(it)) { "Missing automatic producer task $it" }
        println("AUTOMATIC_PRODUCER_TASK $it")
    }
    check(
        allTasks.none { it.name.contains("remap", true) || it.name.contains("resolvePatched", true) || it.name.contains("finalJar", true) },
    )
}
