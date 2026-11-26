import earth.terrarium.cloche.ClocheExtension
import earth.terrarium.cloche.api.target.compilation.Compilation
import net.msrandom.stubs.GenerateStubApi
import net.msrandom.virtualsourcesets.SourceSetStaticLinkageInfo
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider

buildscript {
    val metadata = java.util.Properties()
    file("../../build/pluginUnderTestMetadata/plugin-under-test-metadata.properties").inputStream().use(metadata::load)
    dependencies {
        classpath(files(metadata.getProperty("implementation-classpath").split(File.pathSeparator)))
    }
}

// Apply to two projects so an accidental outer Project binding is observable.
apply(plugin = "earth.terrarium.cloche")
val subject = project(":subject")
subject.pluginManager.apply("earth.terrarium.cloche")
project(":producer").pluginManager.apply("java-library")
val cloche = subject.extensions.getByType<ClocheExtension>()
val parent = cloche.common("parent")
val child = cloche.common("child") { dependsOn(parent) }

// Configure children first: source links must still arrive from later parent callbacks.
val childData = child.data.configure()
val childClient = child.client.configure()
val childClientData = childClient.data.configure()
val parentData = parent.data.configure()
val parentClient = parent.client.configure()
parentClient.data.configure()

val sync =
    subject.configurations.create("contextSync") {
        isCanBeConsumed = false
        isCanBeResolved = true
    }
val external =
    layout.buildDirectory
        .file("external.jar")
        .get()
        .asFile
external.parentFile.mkdirs()
java.util.jar
    .JarOutputStream(external.outputStream())
    .close()
subject.dependencies.add(sync.name, subject.dependencies.project(mapOf("path" to ":producer")))
subject.dependencies.add(sync.name, files(external))

// Real late additions exercise addAllLater, rather than only inspecting extendsFrom.
subject.dependencies.add(child.sourceSet.apiConfigurationName, "fixture:late-api:1")
subject.dependencies.constraints.add(child.sourceSet.apiConfigurationName, "fixture:late-constraint:1")

tasks.register("verify") {
    doLast {
        val pairs = listOf(child to parent, childData to parentData, childClient to parentClient)
        pairs.forEach { (receiving, dependency) ->
            val links =
                receiving.sourceSet.extensions
                    .getByType<SourceSetStaticLinkageInfo>()
                    .links
            check(dependency.sourceSet in links) { "Missing source link ${receiving.sourceSet.name} -> ${dependency.sourceSet.name}" }
            listOf("Api", "CompileOnlyApi", "Implementation", "CompileOnly").forEach { suffix ->
                val own = subject.configurations.getByName(receiving.sourceSet.name + "Common" + suffix)
                val inherited = subject.configurations.getByName(dependency.sourceSet.name + "Common" + suffix)
                check(inherited in own.extendsFrom) { "Missing bucket inheritance ${own.name} -> ${inherited.name}" }
            }
        }
        val clientApi = subject.configurations.getByName(childClient.sourceSet.apiElementsConfigurationName)
        check(clientApi.allDependencies.any { it.group == "fixture" && it.name == "late-api" })
        check(clientApi.allDependencyConstraints.any { it.group == "fixture" && it.name == "late-constraint" })
        val clientCompileOnly = subject.configurations.getByName(childClient.sourceSet.compileOnlyConfigurationName)
        check(
            clientCompileOnly.dependencies
                .filterIsInstance<ProjectDependency>()
                .single()
                .path == subject.path,
        )
        check(tasks.names.none { it.startsWith("createChild") || it.startsWith("createParent") })
        val compilations: List<Compilation> = listOf(child, childData, childClient, childClientData)
        compilations.forEach { compilation ->
            val prefix = compilation.sourceSet.name
            val results = subject.configurations.getByName(prefix + "IntersectionResults")
            check(results.state == Configuration.State.UNRESOLVED)
            val files =
                results.dependencies
                    .filterIsInstance<FileCollectionDependency>()
                    .single()
                    .files
            val producers = files.buildDependencies.getDependencies(null)
            check(producers.single() is GenerateStubApi)
            val stub = producers.single() as GenerateStubApi
            check(stub.project == subject)
            check(stub.classpaths.get().isEmpty()) // No Minecraft targets are needed for this graph check.
            check(
                subject.configurations
                    .getByName(compilation.sourceSet.compileClasspathConfigurationName)
                    .extendsFrom
                    .contains(results),
            )
        }
        check(sync.state == Configuration.State.UNRESOLVED)
        val method =
            Class
                .forName("earth.terrarium.cloche.target.compilation.CompilationInternalKt")
                .getMethod("getRelevantSyncArtifacts", Project::class.java, String::class.java)
        val artifacts = method.invoke(null, subject, sync.name) as Provider<*>
        val files = artifacts.get() as FileCollection
        check(files.files == setOf(external)) { "Sync must exclude project artifacts: ${files.files}" }
        check(files.buildDependencies.getDependencies(null).isEmpty())
        check(gradle.taskGraph.allTasks.none { it.project.path == ":producer" || it.name.contains("remap", true) })
        println(
            "PASS common Project binding, late source/configuration inheritance, API constraints, four stub producers and sync project exclusion; Gradle ${gradle.gradleVersion}",
        )
    }
}
