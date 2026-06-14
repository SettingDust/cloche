package earth.terrarium.cloche

import earth.terrarium.cloche.remap.ClocheClasspathKind
import earth.terrarium.cloche.remap.ClocheClasspathNormalizationKind
import earth.terrarium.cloche.remap.ReplacementDecision
import earth.terrarium.cloche.remap.ReplacementFallbackReason
import earth.terrarium.cloche.remap.ResolvedModArtifact
import earth.terrarium.cloche.remap.ResolvedModArtifactIdentity
import earth.terrarium.cloche.remap.ResolvedModArtifactKey
import earth.terrarium.cloche.remap.ResolvedModArtifactKind
import earth.terrarium.cloche.remap.ResolvedModVariantIdentity
import earth.terrarium.cloche.remap.normalizeModClasspath
import earth.terrarium.cloche.remap.replacementIndex
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RemapReplacementIdentityTests {
    @Test
    fun `replacement key includes classifier and extension for multi-artifact components`() {
        val mainRaw = artifact(classifier = null, extension = "jar")
        val sourcesRaw = artifact(classifier = "sources", extension = "jar")
        val mainRemapped = artifact(classifier = null, extension = "jar", fileName = "mod-remapped.jar")

        val index = listOf(mainRemapped).replacementIndex()

        val mainDecision = assertIs<ReplacementDecision.Replace>(index.decide(mainRaw))
        val sourcesDecision = assertIs<ReplacementDecision.Fallback>(index.decide(sourcesRaw))

        assertEquals(mainRemapped, mainDecision.replacement)
        assertEquals(ReplacementFallbackReason.NoRemappedCandidate, sourcesDecision.reason)
    }

    @Test
    fun `replacement key ignores transform display names and file names when component variant and artifact match`() {
        val raw = artifact(
            variant = ResolvedModVariantIdentity(
                attributes = mapOf("org.gradle.usage" to "java-runtime"),
                displayName = "com.example:mod:1.0 configuration runtime",
            ),
            artifactName = "mod",
            displayName = "mod-1.0.jar (com.example:mod:1.0)",
            fileName = "mod-1.0.jar",
            file = File("modules-2/files-2.1/com.example/mod/1.0/mod-1.0.jar"),
        )
        val remapped = artifact(
            variant = ResolvedModVariantIdentity(
                attributes = mapOf("org.gradle.usage" to "java-runtime"),
                displayName = "transformed artifact set for com.example:mod:1.0",
            ),
            artifactName = "mod",
            displayName = "mod-1.0-named.jar (transformed from mod-1.0.jar)",
            fileName = "mod-1.0-named.jar",
            file = File("transforms/remap/mod-1.0-named.jar"),
        )

        val decision = assertIs<ReplacementDecision.Replace>(listOf(remapped).replacementIndex().decide(raw))

        assertEquals(remapped, decision.replacement)
    }

    @Test
    fun `replacement falls back to source artifact identity when transform rewrites variant attributes`() {
        val raw = artifact(
            variant = ResolvedModVariantIdentity(
                attributes = mapOf(
                    "org.gradle.category" to "library",
                    "org.gradle.usage" to "java-api",
                ),
                displayName = "com.example:mod:1.0 apiElements",
            ),
            artifactName = "mod",
            displayName = "mod-1.0.jar (com.example:mod:1.0)",
            fileName = "mod-1.0.jar",
        )
        val remapped = artifact(
            variant = ResolvedModVariantIdentity(
                attributes = mapOf(
                    "org.gradle.category" to "remapped-library",
                    "org.gradle.usage" to "java-runtime",
                ),
                displayName = "transformed artifact set for com.example:mod:1.0",
            ),
            artifactName = "mod",
            displayName = "mod-1.0-named.jar (transformed from mod-1.0.jar)",
            fileName = "mod-1.0-named.jar",
        )

        val decision = assertIs<ReplacementDecision.Replace>(listOf(remapped).replacementIndex().decide(raw))

        assertEquals(remapped, decision.replacement)
        assertTrue(decision.warnings.any { it.reason == ReplacementFallbackReason.VariantIdentityFallback })
    }

    @Test
    fun `replacement key keeps artifact name dimension when classifier extension and type match`() {
        val raw = artifact(artifactName = "main", fileName = "main.jar")
        val differentArtifact = artifact(artifactName = "shadow", fileName = "main-named.jar")

        val decision = assertIs<ReplacementDecision.Fallback>(listOf(differentArtifact).replacementIndex().decide(raw))

        assertEquals(ReplacementFallbackReason.NoRemappedCandidate, decision.reason)
    }

    @Test
    fun `replacement key separates source consumer and remap dimensions outside variant attributes`() {
        val raw = artifact(
            variant = ResolvedModVariantIdentity(
                mapOf(
                    "org.gradle.usage" to "java-runtime",
                    "earth.terrarium.cloche.remapped" to "false",
                    "earth.terrarium.cloche.remapNamespace" to "intermediary",
                    "earth.terrarium.cloche.internal.remapSourceNamespace" to "intermediary",
                    "earth.terrarium.cloche.internal.remapArtifactNamespace" to "intermediary",
                    "earth.terrarium.cloche.internal.remapConsumerNamespace" to "named",
                )
            )
        )
        val remapped = artifact(
            variant = ResolvedModVariantIdentity(
                mapOf(
                    "org.gradle.usage" to "java-runtime",
                    "earth.terrarium.cloche.remapped" to "true",
                    "earth.terrarium.cloche.remapNamespace" to "named",
                    "earth.terrarium.cloche.internal.remapSourceNamespace" to "intermediary",
                    "earth.terrarium.cloche.internal.remapArtifactNamespace" to "named",
                    "earth.terrarium.cloche.internal.remapConsumerNamespace" to "named",
                )
            ),
            fileName = "mod-named.jar",
        )
        val wrongSource = artifact(
            variant = remapped.key.selectedVariantIdentity,
            sourceNamespace = "srg",
            fileName = "mod-srg-named.jar",
        )

        assertIs<ReplacementDecision.Replace>(listOf(remapped).replacementIndex().decide(raw))
        assertIs<ReplacementDecision.Fallback>(listOf(wrongSource).replacementIndex().decide(raw))
    }

    @Test
    fun `replacement key separates classpath kind and namespace dimensions`() {
        val compileRaw = artifact(classpathKind = ClocheClasspathKind.Compile)
        val runtimeRaw = artifact(classpathKind = ClocheClasspathKind.Runtime)
        val compileRemapped = artifact(classpathKind = ClocheClasspathKind.Compile, fileName = "compile-remapped.jar")

        val index = listOf(compileRemapped).replacementIndex()

        assertIs<ReplacementDecision.Replace>(index.decide(compileRaw))
        assertIs<ReplacementDecision.Fallback>(index.decide(runtimeRaw))
    }

    @Test
    fun `ambiguous remapped candidates become fallback with warning`() {
        val raw = artifact()
        val remappedA = artifact(fileName = "a.jar")
        val remappedB = artifact(fileName = "b.jar")

        val decision = assertIs<ReplacementDecision.Fallback>(listOf(remappedA, remappedB).replacementIndex().decide(raw))

        assertEquals(ReplacementFallbackReason.AmbiguousRemappedCandidates, decision.reason)
        assertTrue(decision.warnings.any { it.reason == ReplacementFallbackReason.AmbiguousRemappedCandidates })
    }

    @Test
    fun `no-artifact component is kept with no-artifact warning`() {
        val platform = artifact(kind = ResolvedModArtifactKind.NoArtifact, file = null)

        val decision = assertIs<ReplacementDecision.Keep>(emptyList<ResolvedModArtifact>().replacementIndex().decide(platform))

        assertTrue(decision.warnings.any { it.reason == ReplacementFallbackReason.NoArtifact })
    }

    @Test
    fun `variant fallback is preserved for report consumers`() {
        val raw = artifact(variant = ResolvedModVariantIdentity(fallback = "variant attributes unavailable"))
        val remapped = artifact(variant = ResolvedModVariantIdentity(fallback = "variant attributes unavailable"))

        val decision = assertIs<ReplacementDecision.Replace>(listOf(remapped).replacementIndex().decide(raw))

        assertTrue(decision.warnings.any { it.reason == ReplacementFallbackReason.VariantIdentityFallback })
    }

    @Test
    fun `normalizer replaces original file when safe remapped candidate exists`() {
        val raw = artifact(fileName = "raw.jar", file = File("raw.jar"))
        val remapped = artifact(fileName = "raw-remapped.jar", file = File("raw-remapped.jar"))

        val result = normalizeModClasspath(ClocheClasspathNormalizationKind.Compile, listOf(raw), listOf(remapped))

        assertEquals(listOf(File("raw-remapped.jar")), result.files)
        assertIs<ReplacementDecision.Replace>(result.decisions.single())
    }

    @Test
    fun `normalizer excludes source namespace fallback when consumer namespace differs`() {
        val raw = artifact(fileName = "raw.jar", file = File("raw.jar"))

        val result = normalizeModClasspath(ClocheClasspathNormalizationKind.Runtime, listOf(raw), emptyList())

        assertEquals(emptyList(), result.files)
        val decision = assertIs<ReplacementDecision.Fallback>(result.decisions.single())
        assertEquals(ReplacementFallbackReason.NoRemappedCandidate, decision.reason)
        assertTrue(result.warnings.any { it.reason == ReplacementFallbackReason.NoRemappedCandidate })
    }

    @Test
    fun `normalizer keeps fallback when source already matches consumer namespace`() {
        val raw = artifact(sourceNamespace = "named", consumerNamespace = "named", fileName = "named.jar", file = File("named.jar"))

        val result = normalizeModClasspath(ClocheClasspathNormalizationKind.Compile, listOf(raw), emptyList())

        assertEquals(listOf(File("named.jar")), result.files)
        assertIs<ReplacementDecision.Fallback>(result.decisions.single())
    }

    @Test
    fun `runtime and run classpaths use the same structured decision path as compile`() {
        val rawRuntime = artifact(classpathKind = ClocheClasspathKind.Runtime, fileName = "runtime.jar")
        val remappedRuntime = artifact(classpathKind = ClocheClasspathKind.Runtime, fileName = "runtime-remapped.jar")
        val rawRun = artifact(classpathKind = ClocheClasspathKind.Run, fileName = "run.jar")
        val remappedRun = artifact(classpathKind = ClocheClasspathKind.Run, fileName = "run-remapped.jar")

        val runtime = normalizeModClasspath(
            ClocheClasspathNormalizationKind.Runtime,
            listOf(rawRuntime),
            listOf(remappedRuntime),
        )
        val run = normalizeModClasspath(
            ClocheClasspathNormalizationKind.Run,
            listOf(rawRun),
            listOf(remappedRun),
        )

        assertIs<ReplacementDecision.Replace>(runtime.decisions.single())
        assertIs<ReplacementDecision.Replace>(run.decisions.single())
        assertEquals(listOf(File("runtime-remapped.jar")), runtime.files)
        assertEquals(listOf(File("run-remapped.jar")), run.files)
    }

    private fun artifact(
        component: String = "com.example:mod:1.0",
        variant: ResolvedModVariantIdentity = ResolvedModVariantIdentity(mapOf("org.gradle.usage" to "java-api")),
        classifier: String? = null,
        extension: String = "jar",
        type: String = extension,
        artifactName: String = "mod",
        sourceNamespace: String = "intermediary",
        consumerNamespace: String = "named",
        classpathKind: ClocheClasspathKind = ClocheClasspathKind.Compile,
        kind: ResolvedModArtifactKind = ResolvedModArtifactKind.Module,
        fileName: String = listOfNotNull("mod", classifier).joinToString("-") + ".$extension",
        displayName: String = fileName,
        file: File? = File(fileName),
    ) = ResolvedModArtifact(
        key = ResolvedModArtifactKey(
            selectedComponentIdentity = component,
            selectedComponentKind = "module",
            selectedVariantIdentity = variant,
            artifactIdentity = ResolvedModArtifactIdentity(
                displayName = displayName,
                name = artifactName,
                classifier = classifier,
                extension = extension,
                type = type,
                fileName = fileName,
                kind = kind,
            ),
            sourceNamespace = sourceNamespace,
            consumerNamespace = consumerNamespace,
            classpathKind = classpathKind,
        ),
        file = file,
    )
}
