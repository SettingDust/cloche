package earth.terrarium.cloche.remap

import java.io.File

internal enum class ClocheClasspathNormalizationKind(val reportName: String) {
    Compile("compile"),
    Runtime("runtime"),
    Run("run"),
}

internal data class ClasspathNormalizationResult(
    val kind: ClocheClasspathNormalizationKind,
    val decisions: List<ReplacementDecision>,
) {
    val files: List<File>
        get() = decisions.mapNotNull { decision ->
            when (decision) {
                is ReplacementDecision.Replace -> decision.replacement.file
                is ReplacementDecision.Keep -> decision.original.file
                is ReplacementDecision.Fallback -> decision.original.file
                    ?.takeIf { decision.original.key.sourceNamespace == decision.original.key.consumerNamespace }
            }
        }

    val warnings: List<ReplacementWarning>
        get() = decisions.flatMap(ReplacementDecision::warnings)
}

internal fun normalizeModClasspath(
    kind: ClocheClasspathNormalizationKind,
    originalArtifacts: Iterable<ResolvedModArtifact>,
    remappedArtifacts: Iterable<ResolvedModArtifact>,
): ClasspathNormalizationResult {
    val replacementIndex = remappedArtifacts.replacementIndex()

    return ClasspathNormalizationResult(
        kind = kind,
        decisions = originalArtifacts.map(replacementIndex::decide),
    )
}
