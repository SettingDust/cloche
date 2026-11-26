import earth.terrarium.cloche.ClocheExtension
import earth.terrarium.cloche.ClochePlugin

buildscript {
    val metadata = java.util.Properties().apply {
        file(providers.gradleProperty("clocheClasspathMetadata").get()).inputStream().use { load(it) }
    }
    dependencies { classpath(files(metadata.getProperty("implementation-classpath").split(File.pathSeparator))) }
}

pluginManager.apply(ClochePlugin::class.java)
group = "probe"
version = "1"
repositories {
    mavenCentral()
    maven("https://maven.msrandom.net/repository/root/")
    maven("https://maven.minecraftforge.net/")
    maven("https://libraries.minecraft.net/")
}

configure<ClocheExtension> {
    metadata.modId.set("game_jars_probe")
    val forge = singleTarget {
        forge {
            minecraftVersion.set("1.20.1")
            loaderVersion.set("47.4.20")
            mixinPreapplication.set(false)
            test {}
        }
    }
    forge.runs {
        server {
            jvmArgs("-javaagent:${file("probe.jar").absolutePath}=absent")
        }
        test {
            jvmArgs("-javaagent:${file("probe.jar").absolutePath}=required")
        }
    }
}

if (!providers.gradleProperty("baseline").isPresent) apply(from = "acceptance.gradle.kts")

