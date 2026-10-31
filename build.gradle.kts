import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode

plugins {
    kotlin("plugin.serialization") version embeddedKotlinVersion
    id("com.google.devtools.ksp") version "2.3.12"

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
    exclusiveContent {
        forRepository {
            maven("https://raw.githubusercontent.com/SettingDust/minecraft-codev/maven/")
        }
        filter {
            listOf("core", "forge", "fabric", "mixins", "runs", "access-widener", "remapper", "decompiler", "includes").forEach {
                includeModule("net.msrandom", "minecraft-codev-$it")
            }
        }
    }
    exclusiveContent {
        forRepository {
            maven("https://raw.githubusercontent.com/SettingDust/jvm-multiplatform/maven/")
        }
        filter {
            includeModule("net.msrandom", "classpath-api-stubs")
        }
    }
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
    implementation(group = "net.msrandom", name = "minecraft-codev-core", version = "0.1.0+terrarium.0.7.2")
    implementation(group = "net.msrandom", name = "minecraft-codev-forge", version = "0.1.1+terrarium.0.9.2")
    implementation(group = "net.msrandom", name = "minecraft-codev-fabric", version = "0.1.1+terrarium.0.7.2")
    implementation(group = "net.msrandom", name = "minecraft-codev-mixins", version = "0.1.1+terrarium.0.6.0")
    implementation(group = "net.msrandom", name = "minecraft-codev-runs", version = "0.1.0+terrarium.0.7.2")
    implementation(group = "net.msrandom", name = "minecraft-codev-access-widener", version = "0.1.0+terrarium.0.6.3")
    implementation(group = "net.msrandom", name = "minecraft-codev-remapper", version = "0.1.0+terrarium.0.7.2")
    implementation(group = "net.msrandom", name = "minecraft-codev-decompiler", version = "0.1.0+terrarium.0.6.0")
    implementation(group = "net.msrandom", name = "minecraft-codev-includes", version = "0.1.0+terrarium.0.6.5")

    implementation(group = "net.msrandom", name = "class-extensions-gradle-plugin", version = "1.0.12")
    implementation(group = "net.msrandom", name = "jvm-virtual-source-sets", version = "1.3.6")
    implementation(group = "net.msrandom", name = "classpath-api-stubs", version = "0.1.0+terrarium.0.1.12")

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
        // Keep existing internal JVM names across KGP's module-name default change.
        moduleName.set(project.name)
        compilerOptions.freeCompilerArgs.add("-Xcontext-parameters")
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

tasks.register<Test>("includeIdentityTest") {
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()
    include("**/IncludeTargetIdentityTests*")
    dependsOn(tasks.pluginUnderTestMetadata)
    workingDir(layout.projectDirectory)
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(17)) })
    val reportsDirectory = layout.buildDirectory.dir("reports/test/include-identity")
    val temporaryDirectory = layout.buildDirectory.dir("tmp/include-identity/tmp")
    systemProperty("java.io.tmpdir", temporaryDirectory.get().asFile.absolutePath)
    doFirst {
        temporaryDirectory.get().asFile.mkdirs()
        val expected = configurations.implementation.get().dependencies
            .filter { it.group == "net.msrandom" && it.name.startsWith("minecraft-codev-") }
            .associate { it.name to it.version }
        check(expected.size == 9)
        val published = listOf("compileClasspath", "runtimeClasspath").flatMap { name ->
            val artifacts = configurations.getByName(name).incoming.artifacts.artifacts
            expected.flatMap { (module, version) ->
                val selected = artifacts.filter {
                    val id = it.id.componentIdentifier as? org.gradle.api.artifacts.component.ModuleComponentIdentifier
                    id?.group == "net.msrandom" && id.module == module
                }
                check(selected.size == 1) { "Expected one published $name dependency for $module: $artifacts" }
                selected.map { artifact ->
                    val id = artifact.id.componentIdentifier as org.gradle.api.artifacts.component.ModuleComponentIdentifier
                    check(id.version == version) { "Unexpected $name version: $id, expected $version" }
                    check(artifact.file.isFile && artifact.file.name == "$module-$version.jar") { "Not a published JAR: $artifact" }
                    "$name $id ${artifact.file.canonicalPath}"
                }
            }
        }
        reportsDirectory.get().asFile.mkdirs()
        reportsDirectory.get().file("compile-classpath.txt").asFile.writeText(published.joinToString("\n", postfix = "\n"))
    }
    inputs.dir(layout.projectDirectory.dir("integration-test/include-identity"))
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
