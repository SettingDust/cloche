import earth.terrarium.cloche.ClocheExtension
import net.msrandom.minecraftcodev.forge.task.ResolvePatchedMinecraft
import java.util.jar.JarFile

buildscript {
    val metadata = java.util.Properties().apply {
        file(providers.gradleProperty("clocheClasspathMetadata").get()).inputStream().use { load(it) }
    }
    dependencies { classpath(files(metadata.getProperty("implementation-classpath").split(File.pathSeparator))) }
}

configure<ClocheExtension> {
    val forge = singleTarget { forge {} }
    forge.runs {
        test { property("connector.clean.path", forge.gameJars.joined.map { it.asFile.absolutePath }) }
    }

    tasks.register("consumeGameJar") {
        inputs.file(forge.gameJars.joined)
        doLast {
            val jar = inputs.files.singleFile
            JarFile(jar).use { check(it.getJarEntry("net/minecraftforge/common/MinecraftForge.class") != null) }
            println("GAME_JARS_CONSUMED ${jar.absolutePath}")
        }
    }
}

// Run separately to get the independent producer path without executing it.
tasks.register("recordPatchedPath") {
    val patched = tasks.named<ResolvePatchedMinecraft>("resolvePatchedMinecraft")
    doLast { file("expected-patched.txt").writeText(patched.get().output.get().asFile.absolutePath) }
}
