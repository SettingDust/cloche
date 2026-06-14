package earth.terrarium.cloche.fixture

import java.io.File
import kotlin.io.path.createTempDirectory

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

class TestKitProjectFixture private constructor(
    private val projectDir: File,
) {
    private fun writeSettings(projectName: String) {
        projectDir.resolve("settings.gradle.kts").writeText("rootProject.name = \"$projectName\"\n")
    }

    private fun writeBuildFile(content: String) {
        projectDir.resolve("build.gradle.kts").writeText(content)
    }

    fun writeScenario(spec: LoaderScenarioSpec, verificationTask: String) {
        writeSettings(spec.projectName)
        writeBuildFile(BuildScriptTemplate.render(spec, verificationTask))
    }

    fun run(vararg arguments: String): BuildResult = GradleRunner.create()
        .withProjectDir(projectDir)
        .withPluginClasspath()
        .withArguments(*arguments)
        .forwardOutput()
        .build()

    companion object {
        fun create(prefix: String): TestKitProjectFixture = TestKitProjectFixture(createTempDirectory(prefix).toFile())
    }
}
