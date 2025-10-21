import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // TODO Switch to kotlin-dsl
    kotlin("plugin.serialization") version embeddedKotlinVersion

    `embedded-kotlin`
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
    maven(url = "https://maven.neoforged.net/")
    maven(url = "https://maven.msrandom.net/repository/cloche/")

    gradlePluginPortal()
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    implementation(group = "net.msrandom", name = "minecraft-codev-core", version = "0.6.2")
    implementation(group = "net.msrandom", name = "minecraft-codev-forge", version = "0.6.5")
    implementation(group = "net.msrandom", name = "minecraft-codev-fabric", version = "0.6.6")
    implementation(group = "net.msrandom", name = "minecraft-codev-mixins", version = "0.5.32")
    implementation(group = "net.msrandom", name = "minecraft-codev-runs", version = "0.6.4")
    implementation(group = "net.msrandom", name = "minecraft-codev-access-widener", version = "0.5.32")
    implementation(group = "net.msrandom", name = "minecraft-codev-remapper", version = "0.6.7")
    implementation(group = "net.msrandom", name = "minecraft-codev-decompiler", version = "0.5.32")
    implementation(group = "net.msrandom", name = "minecraft-codev-includes", version = "0.6.2")

    implementation(group = "net.msrandom", name = "class-extensions-gradle-plugin", version = "1.0.11")
    implementation(group = "net.msrandom", name = "jvm-virtual-source-sets", version = "1.3.4")
    implementation(group = "net.msrandom", name = "classpath-api-stubs", version = "0.1.9")

    implementation(group = "net.peanuuutz.tomlkt", name = "tomlkt", version = "0.4.0")
    implementation(group = "org.apache.groovy", name = "groovy-toml", version = "5.0.2")

    implementation(group = "org.apache.commons", name = "commons-lang3", version = "3.18.0")

    implementation(kotlin("gradle-plugin"))

    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
}

tasks.withType<KotlinCompile> {
    compilerOptions.freeCompilerArgs.addAll("-Xcontext-receivers", "-Xjvm-default=all")
}

publishing {
    repositories {
        mavenLocal()

        maven("file://${rootProject.projectDir}/publish") {
            name = "project"
        }

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

    dependsOn(tasks.pluginUnderTestMetadata)
}

kotlin {
    jvmToolchain(17)
}
