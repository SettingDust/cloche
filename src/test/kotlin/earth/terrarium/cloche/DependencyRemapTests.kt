package earth.terrarium.cloche

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class DependencyRemapTests {
    @Test
    fun supplementIsScopedToNormalDependencyRemap() = assertAll({ scenario("forge") }, { scenario("neoforge") })

    private fun scenario(loader: String) {
        val parent = File("build/tmp/dependency-remap").absoluteFile.apply { mkdirs() }
        val work = Files.createTempDirectory(parent.toPath(), "$loader-").toFile()
        val project = work.resolve("project")
        File("integration-test/dependency-remap").copyRecursively(project)
        val home = work.resolve("gradle-home")
        val downloads = File(System.getProperty("fixture.gradleUserHome"), "caches")
        // Optional private download seed (e.g. downloads fetched while validating offline).
        System.getenv("CLOCHE_${loader.uppercase()}_DOWNLOAD_HOME")?.let { path ->
            val modules = File(path, "caches/modules-2")
            for (directory in modules.listFiles().orEmpty().filter { it.isDirectory }) {
                directory.copyRecursively(home.resolve("caches/modules-2/${directory.name}"))
            }
        }
        // Only raw downloads. Never seed transforms, build cache, extracted games or cached-operations.
        val raw = System.getenv("CLOCHE_CODEV_DOWNLOADS")?.let(::File) ?: downloads.resolve("minecraft-codev")
        for (name in listOf("download-cache", "metadata-rule-download-cache", "version-metadata", "version-manifest.json")) {
            val source = raw.resolve(name)
            if (source.exists()) source.copyRecursively(home.resolve("caches/minecraft-codev/$name"))
        }
        fun run(label: String, vararg args: String, fail: Boolean = false): BuildResult {
            val options = mutableListOf("--no-configuration-cache", "--no-build-cache", "--stacktrace", "-Ploader=$loader",
                "-Pmetadata=${System.getProperty("fixture.metadata")}", "--gradle-user-home", home.absolutePath)
            if (label == "first") options.add("--info")
            options += listOf("--max-workers=2", "-Dorg.gradle.jvmargs=-Xmx2G")
            if (System.getProperty("fixture.offline").toBoolean()) options.add("--offline")
            System.getProperty("fixture.toolchains")?.let { options.add("-Porg.gradle.java.installations.paths=$it") }
            val runner = GradleRunner.create().withGradleInstallation(File(System.getProperty("fixture.gradleHome")))
                .withTestKitDir(work.resolve("test-kit")).withProjectDir(project).withArguments(options + args)
                .withEnvironment(System.getenv() + ("GRADLE_RO_DEP_CACHE" to downloads.absolutePath))
            return try {
                (if (fail) runner.buildAndFail() else runner.build()).also { work.resolve("$label.log").writeText(it.output) }
            } catch (e: Exception) { throw AssertionError("$loader $label in $work", e) }
        }
        val producer = ":resolveClocheRemapMainGame1PatchedMinecraft"
        val help = run("help", "-Penabled", "help")
        assertFalse(help.output.contains("LOOKUP_RESOLUTION"), loader)
        assertNull(help.task(producer), loader)
        val rejected = run("invalid-placement", "-PinvalidPlacement", "help", fail = true)
        assertTrue(rejected.output.contains("compilation 'test'") && rejected.output.contains("target.dependencies or main.dependencies"), loader)
        assertFalse(rejected.output.contains("LOOKUP_RESOLUTION"), loader)
        assertTrue(rejected.tasks.isEmpty(), loader)
        val baseline = run("baseline", "observeMain", "observeTest", "observeRuntime")
        assertTrue(baseline.output.contains("VERIFIED main"), loader)
        val firstRole = if (loader == "forge") "main" else "test"
        val otherRole = if (loader == "forge") "test" else "main"
        fun observer(role: String) = "observe${role.replaceFirstChar(Char::uppercase)}"
        val first = run("first", "-Penabled", observer(firstRole))
        assertEquals(TaskOutcome.SUCCESS, first.task(producer)?.outcome, loader)
        assertTrue(first.output.lineSequence().any { "Remapping mod" in it && "caller-1.jar" in it }, "$loader cold caller remap")
        assertTrue(first.output.contains("VERIFIED $firstRole"), loader)
        assertNull(first.task(":" + observer(otherRole)), loader)
        val remainder = run("isolation", "-Penabled", observer(otherRole), "observeRuntime", "observeDistribution")
        for (label in listOf(otherRole, "runtime", "packaging and publication")) assertTrue(remainder.output.contains("VERIFIED $label"), loader)
        assertTrue(run("invalid", "-Penabled", "-Pbad", "observeMain", fail = true).output.contains("9999.invalid"), loader)
        if (loader == "forge") {
            val observers = arrayOf("observeAlpha", "observeBeta", "observeAlphaRuntime", "observeBetaRuntime")
            run("dual-baseline", "-Pdual", *observers)
            run("dual-one", "-Pdual", "-Penabled", *observers)
            run("dual-both", "-Pdual", "-Penabled", "-Palternate", *observers, "observeDistribution")
            val kotlinProject = work.resolve("kotlin-dsl").apply { mkdirs() }
            project.resolve("settings.gradle").copyTo(kotlinProject.resolve("settings.gradle"))
            val script = kotlinProject.resolve("build.gradle.kts")
            val positive = project.resolve("scope.gradle.kts.template").readText()
            script.writeText(positive)
            run("kotlin-positive", "-p", kotlinProject.absolutePath, "help")
            script.writeText(positive.replace("modImplementation.add(\"probe:ordinary:1\")", "remapClasspath(forgeMinecraft(\"1.20.1\", \"47.4.20\"))"))
            val negative = run("kotlin-negative", "-p", kotlinProject.absolutePath, "help", fail = true)
            assertTrue(negative.output.contains("Unresolved reference 'remapClasspath'"))
            assertTrue(negative.output.contains("Unresolved reference 'forgeMinecraft'"))
            assertFalse(negative.output.contains("LOOKUP_RESOLUTION"))
            assertTrue(negative.tasks.isEmpty())
        }
    }
}
