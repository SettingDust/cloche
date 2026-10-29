import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

plugins {
    kotlin("plugin.serialization") version embeddedKotlinVersion
    id("com.google.devtools.ksp") version embeddedKotlinVersion

    `kotlin-dsl`
    `java-gradle-plugin`

    `maven-publish`
    idea
}

gradlePlugin {
    plugins {
        val pluginName = "cloche"

        create(pluginName) {
            id = "$group.$pluginName"
            implementationClass = "earth.terrarium.cloche.ClochePlugin"
        }
    }
}

repositories {
    mavenCentral()

    maven(url = "https://maven.fabricmc.net/")
    maven(url = "https://maven.neoforged.net/releases")
    maven(url = "https://maven.msrandom.net/repository/cloche/")

    gradlePluginPortal()
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(group = "net.msrandom", name = "minecraft-codev-core", version = "0.7.2")
    implementation(group = "net.msrandom", name = "minecraft-codev-forge", version = "0.9.2")
    implementation(group = "net.msrandom", name = "minecraft-codev-fabric", version = "0.7.2")
    implementation(group = "net.msrandom", name = "minecraft-codev-mixins", version = "0.6.0")
    implementation(group = "net.msrandom", name = "minecraft-codev-runs", version = "0.7.2")
    implementation(group = "net.msrandom", name = "minecraft-codev-access-widener", version = "0.6.3")
    implementation(group = "net.msrandom", name = "minecraft-codev-remapper", version = "0.7.2")
    implementation(group = "net.msrandom", name = "minecraft-codev-decompiler", version = "0.6.0")
    implementation(group = "net.msrandom", name = "minecraft-codev-includes", version = "0.6.5")

    implementation(group = "net.msrandom", name = "class-extensions-gradle-plugin", version = "1.0.12")
    implementation(group = "net.msrandom", name = "jvm-virtual-source-sets", version = "1.3.6")
    implementation(group = "net.msrandom", name = "classpath-api-stubs", version = "0.1.12")

    implementation(group = "net.peanuuutz.tomlkt", name = "tomlkt", version = "0.5.0")
    implementation(group = "org.apache.groovy", name = "groovy-toml", version = "5.0.2")

    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.18.0")

    implementation(kotlin("gradle-plugin"))

    ksp(group = "dev.zacsweers.autoservice", name = "auto-service-ksp", version = "1.2.0")
    compileOnly(group = "com.google.auto.service", name = "auto-service-annotations", version = "1.1.1")

    implementation(projects.clocheModels)

    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.0-M1")
}

tasks.compileKotlin {
    compilerOptions {
        jvmDefault = JvmDefaultMode.ENABLE
        compilerOptions.freeCompilerArgs.add("-Xcontext-receivers")
    }
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "terrarium-earth",
        )
    }
}

publishing {
    repositories {
        mavenLocal()

        maven("https://maven.msrandom.net/repository/cloche/") {
            credentials {
                val mavenUsername: String? by project
                val mavenPassword: String? by project

                username = mavenUsername
                password = mavenPassword
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
    exclude("**/IncludeTargetIdentityTests*")

    dependsOn(tasks.pluginUnderTestMetadata)
}

val includeIdentityCodevRoot = providers.gradleProperty("includeIdentityCodevRoot")
includeIdentityCodevRoot.orNull?.let { codevRoot ->
    tasks.pluginUnderTestMetadata {
        val runtimeClasspath = sourceSets.main.get().runtimeClasspath
        // Directory entries retain local codeSource when TestKit instruments the plugin classpath.
        pluginClasspath.setFrom(
            runtimeClasspath.filter { !it.name.matches(Regex("(minecraft-codev-includes|minecraft-codev-core|side-annotations)-.*\\.jar")) },
            files(
                "$codevRoot/minecraft-codev-includes/build/classes/kotlin/main",
                "$codevRoot/minecraft-codev-includes/build/resources/main",
                "$codevRoot/minecraft-codev-core/build/classes/kotlin/main",
                "$codevRoot/minecraft-codev-core/build/resources/main",
                "$codevRoot/minecraft-codev-core/side-annotations/build/classes/java/main",
                "$codevRoot/minecraft-codev-core/side-annotations/build/resources/main",
            ).builtBy(runtimeClasspath),
        )
    }
}
tasks.register<Test>("includeIdentityTest") {
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()
    include("**/IncludeTargetIdentityTests*")
    dependsOn(tasks.pluginUnderTestMetadata)
    workingDir(layout.projectDirectory)
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(17)) })
    val reportsDirectory = layout.buildDirectory.dir("reports/test/include-identity")
    reports.html.outputLocation.set(reportsDirectory.map { it.dir("html") })
    reports.junitXml.outputLocation.set(reportsDirectory.map { it.dir("xml") })
    binaryResultsDirectory.set(reportsDirectory.map { it.dir("binary") })
    val temporaryDirectory = reportsDirectory.map { it.dir("tmp") }
    systemProperty("java.io.tmpdir", temporaryDirectory.get().asFile.absolutePath)
    doFirst {
        temporaryDirectory.get().asFile.mkdirs()
        val root = file(includeIdentityCodevRoot.get()).canonicalFile
        val local = listOf("compileClasspath", "runtimeClasspath").flatMap { name ->
            val artifacts = configurations.getByName(name).incoming.artifacts.artifacts
            val modules = listOf("minecraft-codev-includes", "minecraft-codev-core") +
                if (name == "runtimeClasspath") listOf("side-annotations") else emptyList()
            modules.flatMap { module ->
                val selected = artifacts.filter {
                    (it.id.componentIdentifier as? org.gradle.api.artifacts.component.ProjectComponentIdentifier)?.projectName == module
                }
                check(selected.isNotEmpty()) { "No local $name project dependency for $module: $artifacts" }
                selected.map { artifact ->
                    check(artifact.file.canonicalFile.toPath().startsWith(root.toPath())) { "Nonlocal $name dependency: $artifact" }
                    "$name $module ${artifact.id.componentIdentifier} ${artifact.file.canonicalPath}"
                }
            }
        }
        reportsDirectory.get().file("compile-classpath.txt").asFile.writeText(local.joinToString("\n", postfix = "\n"))
    }
    inputs.dir(layout.projectDirectory.dir("src/test/resources/include-identity"))
    inputs.property("includeIdentityCodevRoot", includeIdentityCodevRoot)
    includeIdentityCodevRoot.orNull?.let { systemProperty("fixture.codevRoot", file(it).canonicalPath) }
    systemProperty("fixture.gradleJava", System.getProperty("java.home"))
    systemProperty("fixture.gradleUserHome", gradle.gradleUserHomeDir.absolutePath)
    systemProperty("org.gradle.testkit.dir", gradle.gradleUserHomeDir.absolutePath)
    providers.gradleProperty("org.gradle.java.installations.paths").orNull
        ?.takeIf { it.isNotEmpty() }
        ?.let { systemProperty("org.gradle.java.installations.paths", it) }
}

kotlin {
    jvmToolchain(17)
}
