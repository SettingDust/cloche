package earth.terrarium.cloche

import earth.terrarium.cloche.fixture.LoaderKind
import earth.terrarium.cloche.fixture.LoaderScenarioSpec
import earth.terrarium.cloche.fixture.RepositoryPresets
import earth.terrarium.cloche.fixture.TestKitProjectFixture
import kotlin.test.assertContains
import org.junit.jupiter.api.Test

class ClocheRemapReportChainSentinelTests {
    @Test
    fun `fabric remap report chain writes stable observable report content without secrets`() {
        val fixture = TestKitProjectFixture.create("cloche-remap-report-chain")
        fixture.writeScenario(
            spec = LoaderScenarioSpec(
                projectName = "remap-report-chain",
                modId = "remap_report_chain",
                loaderKind = LoaderKind.Fabric,
                loaderVersion = "0.16.10",
                repositories = RepositoryPresets.remapRouting.filterNot { it.contains("neoforged") || it.contains("minecraftforge") },
                mappingsBlock = """
                    official()
                """.trimIndent(),
                dependenciesBlock = """
                    modCompileOnly("net.fabricmc:fabric-loader:0.16.10")
                """.trimIndent(),
            ),
            verificationTask = """
                tasks.register("verifyClocheRemapReportChain") {
                    dependsOn("clocheRemapReport")

                    doLast {
                        val reportRoot = layout.buildDirectory.dir("reports/cloche/remap").get().asFile
                        val jsonReports = reportRoot.walkTopDown().filter { it.name == "report.json" }.toList()
                        val textReports = reportRoot.walkTopDown().filter { it.name == "report.txt" }.toList()

                        check(jsonReports.size == 1) { "Expected one remap JSON report, found ${'$'}jsonReports" }
                        check(textReports.size == 1) { "Expected one remap text report, found ${'$'}textReports" }

                        val json = jsonReports.single().readText()
                        val text = textReports.single().readText()
                        val combined = json + "\n" + text

                        listOf(
                            "\"target\":\"fabric\"",
                            "\"compilation\":\"clocheFabricRemapReport\"",
                            "\"consumerClasspathNamespace\":\"named\"",
                            "\"declarations\"",
                            "\"buckets\"",
                            "\"rawArtifacts\"",
                            "\"remappedArtifacts\"",
                            "\"retainedNonModLibraries\"",
                            "\"replacements\"",
                            "\"classpaths\"",
                            "\"sourceNamespace\":\"intermediary\"",
                            "\"classpathKind\":\"compile\"",
                            "\"classpathKind\":\"runtime\"",
                            "modIntermediaryBucketFabricCompileClasspath",
                            "net.fabricmc:fabric-loader:0.16.10",
                            "Cloche remap report",
                            "consumerClasspathNamespace: named",
                            "replacement decisions:",
                            "classpath decisions:",
                        ).forEach { expected ->
                            check(combined.contains(expected)) { "Remap report missing stable content: ${'$'}expected\n${'$'}combined" }
                        }

                        listOf("user:pass", "token=", "password=", "GITHUB_TOKEN", "ghp_123456789abcdef").forEach { forbidden ->
                            check(!combined.contains(forbidden, ignoreCase = true)) {
                                "Remap report exposed secret-like content: ${'$'}forbidden\n${'$'}combined"
                            }
                        }
                    }
                }
            """.trimIndent(),
        )

        val result = fixture.run(
            "verifyClocheRemapReportChain",
            "-Dorg.gradle.jvmargs=-Xmx4G -Dfile.encoding=UTF-8",
            "--max-workers=1",
            "--stacktrace",
            "--console=plain",
        )

        assertContains(result.output, "verifyClocheRemapReportChain")
    }
}
