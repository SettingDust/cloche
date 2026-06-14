package earth.terrarium.cloche

import earth.terrarium.cloche.remap.ClocheClasspathKind
import earth.terrarium.cloche.remap.ClocheClasspathNormalizationKind
import earth.terrarium.cloche.remap.RemapBucketReport
import earth.terrarium.cloche.remap.RemapDeclarationReport
import earth.terrarium.cloche.remap.ReplacementFallbackReason
import earth.terrarium.cloche.remap.ResolvedModArtifact
import earth.terrarium.cloche.remap.ResolvedModArtifactIdentity
import earth.terrarium.cloche.remap.ResolvedModArtifactKey
import earth.terrarium.cloche.remap.ResolvedModArtifactKind
import earth.terrarium.cloche.remap.ResolvedModVariantIdentity
import earth.terrarium.cloche.remap.normalizeModClasspath
import earth.terrarium.cloche.remap.redactSecrets
import earth.terrarium.cloche.remap.remapReportFromDecisions
import earth.terrarium.cloche.remap.safeGradlePathSegment
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertFalse

class ClocheRemapReportTests {
    @Test
    fun `redaction removes secret keys values url credentials and env secret names`() {
        val redacted = "https://user:pass@example.test/repo?token=abc123 password=hunter2 GITHUB_TOKEN ghp_123456789abcdef".redactSecrets()

        assertContains(redacted, "<redacted>")
        assertFalse(redacted.contains("user:pass"))
        assertFalse(redacted.contains("abc123"))
        assertFalse(redacted.contains("hunter2"))
        assertFalse(redacted.contains("GITHUB_TOKEN"))
        assertFalse(redacted.contains("ghp_123456789abcdef"))
    }

    @Test
    fun `report contains fallback replacement diagnostics and namespace dimensions`() {
        val raw = artifact(fileName = "raw.jar", file = File("raw.jar"))
        val result = normalizeModClasspath(ClocheClasspathNormalizationKind.Compile, listOf(raw), emptyList())

        val report = remapReportFromDecisions(
            target = "fabric",
            compilation = "main",
            consumerClasspathNamespace = "named",
            declarations = listOf(
                RemapDeclarationReport(
                    displayName = "com.example:mod:1.0?password=secret",
                    declaredSourceNamespace = "intermediary",
                    effectiveSourceNamespace = "intermediary",
                    bucket = "modIntermediaryBucketImplementation",
                )
            ),
            buckets = listOf(RemapBucketReport("modIntermediaryBucketImplementation", "intermediary", "compile", 1)),
            rawArtifacts = listOf(raw),
            remappedArtifacts = emptyList(),
            retainedNonModLibraries = listOf(File("kotlin-stdlib.jar")),
            classpathResults = listOf(result),
        )

        val json = report.toJson()
        val text = report.toText()

        assertContains(json, "\"declarations\"")
        assertContains(json, "\"buckets\"")
        assertContains(json, "\"rawArtifacts\"")
        assertContains(json, "\"remappedArtifacts\"")
        assertContains(json, "\"retainedNonModLibraries\"")
        assertContains(json, "kotlin-stdlib.jar")
        assertContains(json, "\"replacements\"")
        assertContains(json, "no_remapped_candidate")
        assertFalse(json.contains("NO_REMAPPED_CANDIDATE"))
        assertContains(json, "\"classpathKind\":\"compile\"")
        assertContains(json, "\"kind\":\"module\"")
        assertFalse(json.contains("\"COMPILE\""))
        assertFalse(json.contains("\"MODULE\""))
        assertContains(json, "intermediary")
        assertContains(json, "named")
        assertContains(text, "fallback=no_remapped_candidate")
        assertFalse(json.contains("secret"))
    }

    @Test
    fun `report path segment escapes windows-invalid target characters`() {
        assertFalse("fabric:20.1".safeGradlePathSegment().contains(':'))
        assertContains("fabric:20.1".safeGradlePathSegment(), "fabric_20.1")
    }

    private fun artifact(
        fileName: String,
        file: File?,
    ) = ResolvedModArtifact(
        key = ResolvedModArtifactKey(
            selectedComponentIdentity = "com.example:mod:1.0",
            selectedComponentKind = "module",
            selectedVariantIdentity = ResolvedModVariantIdentity(mapOf("org.gradle.usage" to "java-api")),
            artifactIdentity = ResolvedModArtifactIdentity(
                displayName = fileName,
                name = "mod",
                extension = "jar",
                type = "jar",
                fileName = fileName,
                kind = ResolvedModArtifactKind.Module,
            ),
            sourceNamespace = "intermediary",
            consumerNamespace = "named",
            classpathKind = ClocheClasspathKind.Compile,
        ),
        file = file,
    )
}
