package earth.terrarium.cloche.remap

import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import java.io.File

internal enum class ClocheClasspathKind(val reportName: String) {
    Compile("compile"),
    Runtime("runtime"),
    Run("run"),
}

internal enum class ResolvedModArtifactKind(val reportName: String) {
    Module("module"),
    Project("project"),
    File("file"),
    IncludedBuild("included_build"),
    Transformed("transformed"),
    NoArtifact("no_artifact"),
    Unknown("unknown"),
}

internal data class ResolvedModVariantIdentity(
    val attributes: Map<String, String> = emptyMap(),
    val displayName: String? = null,
    val fallback: String? = null,
)

internal data class ResolvedModArtifactIdentity(
    val displayName: String,
    val name: String? = null,
    val classifier: String? = null,
    val extension: String? = null,
    val type: String? = null,
    val fileName: String? = null,
    val normalizedAbsolutePath: String? = null,
    val fileLength: Long? = null,
    val fileLastModified: Long? = null,
    val kind: ResolvedModArtifactKind = ResolvedModArtifactKind.Unknown,
)

internal data class ResolvedModArtifactKey(
    val selectedComponentIdentity: String,
    val selectedComponentKind: String,
    val selectedVariantIdentity: ResolvedModVariantIdentity,
    val artifactIdentity: ResolvedModArtifactIdentity,
    val sourceNamespace: String,
    val consumerNamespace: String,
    val classpathKind: ClocheClasspathKind,
)

internal data class ResolvedModArtifact(
    val key: ResolvedModArtifactKey,
    val file: File?,
)

internal enum class ReplacementFallbackReason(val reportName: String) {
    NoRemappedCandidate("no_remapped_candidate"),
    AmbiguousRemappedCandidates("ambiguous_remapped_candidates"),
    NoArtifact("no_artifact"),
    ArtifactIdentityFallback("artifact_identity_fallback"),
    VariantIdentityFallback("variant_identity_fallback"),
}

internal data class ReplacementWarning(
    val reason: ReplacementFallbackReason,
    val message: String,
)

internal sealed class ReplacementDecision {
    abstract val original: ResolvedModArtifact
    abstract val warnings: List<ReplacementWarning>

    data class Replace(
        override val original: ResolvedModArtifact,
        val replacement: ResolvedModArtifact,
        override val warnings: List<ReplacementWarning> = emptyList(),
    ) : ReplacementDecision()

    data class Keep(
        override val original: ResolvedModArtifact,
        override val warnings: List<ReplacementWarning> = emptyList(),
    ) : ReplacementDecision()

    data class Fallback(
        override val original: ResolvedModArtifact,
        val reason: ReplacementFallbackReason,
        override val warnings: List<ReplacementWarning>,
    ) : ReplacementDecision()
}

internal class ResolvedModArtifactReplacementIndex(
    remappedArtifacts: Iterable<ResolvedModArtifact>,
) {
    private val remappedByReplacementKey = remappedArtifacts.groupBy { it.key.replacementLookupKey() }
    private val remappedBySourceArtifactKey = remappedArtifacts.groupBy { it.key.sourceArtifactLookupKey() }

    fun decide(original: ResolvedModArtifact): ReplacementDecision {
        if (original.key.artifactIdentity.kind == ResolvedModArtifactKind.NoArtifact || original.file == null) {
            return ReplacementDecision.Keep(
                original,
                listOf(
                    ReplacementWarning(
                        ReplacementFallbackReason.NoArtifact,
                        "No artifact file participates in replacement for ${original.key.selectedComponentIdentity}.",
                    )
                ),
            )
        }

        val warnings = original.key.identityFallbackWarnings()
        val candidates = remappedByReplacementKey[original.key.replacementLookupKey()].orEmpty()
        if (candidates.isNotEmpty()) {
            return decideFromCandidates(original, candidates, warnings)
        }

        val sourceArtifactCandidates = remappedBySourceArtifactKey[original.key.sourceArtifactLookupKey()].orEmpty()
        if (sourceArtifactCandidates.isNotEmpty()) {
            val sourceArtifactWarnings = warnings + ReplacementWarning(
                ReplacementFallbackReason.VariantIdentityFallback,
                "Selected variant identity did not match remapped artifact metadata; matched by selected component, artifact classifier/extension/type, source, consumer, and classpath kind.",
            )
            return decideFromCandidates(original, sourceArtifactCandidates, sourceArtifactWarnings)
        }

        return ReplacementDecision.Fallback(
            original,
            ReplacementFallbackReason.NoRemappedCandidate,
            warnings + ReplacementWarning(
                ReplacementFallbackReason.NoRemappedCandidate,
                "No remapped artifact matched selected component, variant or source artifact fallback, artifact, source, consumer, and classpath kind.",
            ),
        )
    }

    private fun decideFromCandidates(
        original: ResolvedModArtifact,
        candidates: List<ResolvedModArtifact>,
        warnings: List<ReplacementWarning>,
    ): ReplacementDecision = when (candidates.size) {
            0 -> ReplacementDecision.Fallback(
                original,
                ReplacementFallbackReason.NoRemappedCandidate,
                warnings + ReplacementWarning(
                    ReplacementFallbackReason.NoRemappedCandidate,
                    "No remapped artifact matched selected component, variant, artifact, source, consumer, and classpath kind.",
                ),
            )
            1 -> ReplacementDecision.Replace(original, candidates.single(), warnings)
            else -> ReplacementDecision.Fallback(
                original,
                ReplacementFallbackReason.AmbiguousRemappedCandidates,
                warnings + ReplacementWarning(
                    ReplacementFallbackReason.AmbiguousRemappedCandidates,
                    "Multiple remapped artifacts matched ${original.key.artifactIdentity.displayName}.",
                ),
            )
    }
}

internal fun Iterable<ResolvedModArtifact>.replacementIndex() = ResolvedModArtifactReplacementIndex(this)

internal fun ResolvedArtifactResult.toResolvedModArtifact(
    sourceNamespace: String,
    consumerNamespace: String,
    classpathKind: ClocheClasspathKind,
): ResolvedModArtifact {
    val componentIdentifier = id.componentIdentifier
    val artifactIdentity = artifactIdentity(id, file)

    return ResolvedModArtifact(
        key = ResolvedModArtifactKey(
            selectedComponentIdentity = componentIdentifier.displayName,
            selectedComponentKind = componentIdentifier.javaClass.name,
            selectedVariantIdentity = variantIdentity(),
            artifactIdentity = artifactIdentity,
            sourceNamespace = sourceNamespace,
            consumerNamespace = consumerNamespace,
            classpathKind = classpathKind,
        ),
        file = file,
    )
}

private data class ReplacementLookupKey(
    val selectedComponentIdentity: String,
    val selectedComponentKind: String,
    val selectedVariantIdentity: ResolvedModVariantIdentity,
    val artifactIdentity: ResolvedModArtifactIdentity,
    val sourceNamespace: String,
    val consumerNamespace: String,
    val classpathKind: ClocheClasspathKind,
)

private data class SourceArtifactLookupKey(
    val selectedComponentIdentity: String,
    val selectedComponentKind: String,
    val artifactIdentity: ResolvedModArtifactIdentity,
    val sourceNamespace: String,
    val consumerNamespace: String,
    val classpathKind: ClocheClasspathKind,
)

private fun ResolvedModArtifactKey.replacementLookupKey() = ReplacementLookupKey(
    selectedComponentIdentity,
    selectedComponentKind,
    selectedVariantIdentity.stableLookupIdentity(),
    artifactIdentity.copy(
        displayName = "",
        fileName = null,
        normalizedAbsolutePath = null,
        fileLength = null,
        fileLastModified = null,
    ),
    sourceNamespace,
    consumerNamespace,
    classpathKind,
)

private fun ResolvedModArtifactKey.sourceArtifactLookupKey() = SourceArtifactLookupKey(
    selectedComponentIdentity,
    selectedComponentKind,
    artifactIdentity.stableLookupIdentity(),
    sourceNamespace,
    consumerNamespace,
    classpathKind,
)

private fun ResolvedModArtifactIdentity.stableLookupIdentity() = copy(
    displayName = "",
    fileName = null,
    normalizedAbsolutePath = null,
    fileLength = null,
    fileLastModified = null,
)

private fun ResolvedModVariantIdentity.stableLookupIdentity(): ResolvedModVariantIdentity {
    val stableAttributes = attributes.filterKeys { attributeName -> attributeName !in remapPipelineVariantAttributeNames }

    return when {
        stableAttributes.isNotEmpty() -> copy(attributes = stableAttributes, displayName = null)
        else -> this
    }
}

private fun ResolvedModArtifactKey.identityFallbackWarnings(): List<ReplacementWarning> = buildList {
    selectedVariantIdentity.fallback?.let { fallback ->
        add(ReplacementWarning(ReplacementFallbackReason.VariantIdentityFallback, fallback))
    }
    if (artifactIdentity.normalizedAbsolutePath != null) {
        add(
            ReplacementWarning(
                ReplacementFallbackReason.ArtifactIdentityFallback,
                "Artifact identity includes file metadata fallback for ${artifactIdentity.displayName}.",
            )
        )
    }
}

private fun ResolvedArtifactResult.variantIdentity(): ResolvedModVariantIdentity {
    val variant = runCatching { variant }.getOrNull()
        ?: return ResolvedModVariantIdentity(fallback = "Gradle did not expose selected variant identity.")

    val attributes = variant.attributes.keySet().associate { attribute ->
        attribute.name to variant.attributes.getAttribute(attribute).toString()
    }.filterKeys { attributeName -> attributeName !in remapPipelineVariantAttributeNames }
        .toSortedMap()

    return ResolvedModVariantIdentity(
        attributes = attributes,
        displayName = variant.displayName,
        fallback = if (attributes.isEmpty()) "Selected variant has no attributes; using variant display name fallback." else null,
    )
}

private val remapPipelineVariantAttributeNames = setOf(
    "earth.terrarium.cloche.remapped",
    "earth.terrarium.cloche.remapNamespace",
    "earth.terrarium.cloche.internal.remapSourceNamespace",
    "earth.terrarium.cloche.internal.remapArtifactNamespace",
    "earth.terrarium.cloche.internal.remapConsumerNamespace",
)

private fun artifactIdentity(id: ComponentArtifactIdentifier, file: File): ResolvedModArtifactIdentity {
    val displayName = id.displayName
    val fileKind = artifactKind(id.componentIdentifier, file)
    val extension = reflectString(id, "getExtension") ?: file.extension.takeIf(String::isNotBlank)
    val classifier = reflectString(id, "getClassifier")
    val type = reflectString(id, "getType") ?: extension
    val name = stableArtifactName(id.componentIdentifier, fileKind, file)

    return ResolvedModArtifactIdentity(
        displayName = displayName,
        name = name,
        classifier = classifier,
        extension = extension,
        type = type,
        fileName = file.name,
        normalizedAbsolutePath = if (fileKind == ResolvedModArtifactKind.File) file.normalize().absolutePath else null,
        fileLength = if (fileKind == ResolvedModArtifactKind.File && file.isFile) file.length() else null,
        fileLastModified = if (fileKind == ResolvedModArtifactKind.File && file.exists()) file.lastModified() else null,
        kind = fileKind,
    )
}

private fun stableArtifactName(
    componentIdentifier: ComponentIdentifier,
    kind: ResolvedModArtifactKind,
    file: File,
): String = when (kind) {
    ResolvedModArtifactKind.Module -> componentIdentifier.displayName
        .split(':')
        .getOrNull(1)
        ?.takeIf(String::isNotBlank)
        ?: file.nameWithoutExtension
    ResolvedModArtifactKind.Project,
    ResolvedModArtifactKind.IncludedBuild -> componentIdentifier.displayName
    else -> file.nameWithoutExtension
}

private fun artifactKind(componentIdentifier: ComponentIdentifier, file: File): ResolvedModArtifactKind {
    val className = componentIdentifier.javaClass.name
    val displayName = componentIdentifier.displayName

    return when {
        className.contains("ModuleComponentIdentifier") -> ResolvedModArtifactKind.Module
        className.contains("ProjectComponentIdentifier") && displayName.contains("included build", ignoreCase = true) ->
            ResolvedModArtifactKind.IncludedBuild
        className.contains("ProjectComponentIdentifier") -> ResolvedModArtifactKind.Project
        className.contains("File", ignoreCase = true) || file.isAbsolute && displayName.contains(file.name) ->
            ResolvedModArtifactKind.File
        file.path.replace(File.separatorChar, '/').contains("/transforms/") -> ResolvedModArtifactKind.Transformed
        else -> ResolvedModArtifactKind.Unknown
    }
}

private fun reflectString(target: Any, methodName: String): String? = runCatching {
    target.javaClass.methods.singleOrNull { method -> method.name == methodName && method.parameterCount == 0 }
        ?.invoke(target)
        ?.toString()
        ?.takeIf(String::isNotBlank)
}.getOrNull()
