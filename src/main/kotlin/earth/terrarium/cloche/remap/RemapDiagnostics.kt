package earth.terrarium.cloche.remap

import java.io.File

internal data class RemapReport(
    val target: String,
    val compilation: String,
    val consumerClasspathNamespace: String,
    val declarations: List<RemapDeclarationReport>,
    val buckets: List<RemapBucketReport>,
    val rawArtifacts: List<RemapArtifactReport>,
    val remappedArtifacts: List<RemapArtifactReport>,
    val retainedNonModLibraries: List<String>,
    val replacements: List<RemapReplacementReport>,
    val classpaths: List<RemapClasspathReport>,
    val warnings: List<String>,
    val errors: List<String>,
) {
    fun toJson(): String = buildString {
        appendObject {
            field("target", target)
            field("compilation", compilation)
            field("consumerClasspathNamespace", consumerClasspathNamespace)
            field("declarations", declarations) { declaration -> writeJson(declaration) }
            field("buckets", buckets) { bucket -> writeJson(bucket) }
            field("rawArtifacts", rawArtifacts) { artifact -> writeJson(artifact) }
            field("remappedArtifacts", remappedArtifacts) { artifact -> writeJson(artifact) }
            field("retainedNonModLibraries", retainedNonModLibraries)
            field("replacements", replacements) { replacement -> writeJson(replacement) }
            field("classpaths", classpaths) { classpath -> writeJson(classpath) }
            field("warnings", warnings)
            field("errors", errors)
        }
    }

    fun toText(): String = buildString {
        appendLine("Cloche remap report")
        appendLine("target: ${target.redactSecrets()}")
        appendLine("compilation: ${compilation.redactSecrets()}")
        appendLine("consumerClasspathNamespace: ${consumerClasspathNamespace.redactSecrets()}")
        appendLine()
        appendLine("declarations:")
        declarations.forEach { appendLine("- ${it.displayName.redactSecrets()} declared=${it.declaredSourceNamespace ?: "<default>"} effective=${it.effectiveSourceNamespace} bucket=${it.bucket}") }
        appendLine("buckets:")
        buckets.forEach { appendLine("- ${it.name}: sourceNamespace=${it.sourceNamespace} classpathKind=${it.classpathKind} dependencies=${it.dependencyCount}") }
        appendLine("raw artifacts:")
        rawArtifacts.forEach { appendLine("- [${it.classpathKind}] ${it.component} ${it.fileName ?: "<no-file>"} source=${it.sourceNamespace} output=${it.artifactNamespace} consumer=${it.consumerClasspathNamespace}") }
        appendLine("remapped artifacts/candidates:")
        remappedArtifacts.forEach { appendLine("- [${it.classpathKind}] ${it.component} ${it.fileName ?: "<no-file>"} source=${it.sourceNamespace} output=${it.artifactNamespace} consumer=${it.consumerClasspathNamespace}") }
        appendLine("retained non-mod/library deps:")
        retainedNonModLibraries.forEach { appendLine("- ${it.redactSecrets()}") }
        appendLine("replacement decisions:")
        replacements.forEach { replacement ->
            appendLine("- [${replacement.original.classpathKind}] ${replacement.action} ${replacement.original.fileName ?: replacement.original.component} -> ${replacement.replacement?.fileName ?: "<none>"}${replacement.fallbackReason?.let { " fallback=$it" }.orEmpty()}")
            replacement.warnings.forEach { appendLine("  warning: ${it.redactSecrets()}") }
        }
        appendLine("classpath decisions:")
        classpaths.forEach { classpath -> appendLine("- ${classpath.kind}: kept=${classpath.kept} replaced=${classpath.replaced} fallback=${classpath.fallback} files=${classpath.files.joinToString()}") }
        if (warnings.isNotEmpty()) {
            appendLine("warnings:")
            warnings.forEach { appendLine("- ${it.redactSecrets()}") }
        }
        if (errors.isNotEmpty()) {
            appendLine("errors:")
            errors.forEach { appendLine("- ${it.redactSecrets()}") }
        }
    }
}

internal fun String.safeGradlePathSegment(): String {
    val sanitized = buildString(length) {
        this@safeGradlePathSegment.forEach { char ->
            append(
                when {
                    char in setOf('<', '>', ':', '"', '/', '\\', '|', '?', '*') -> '_'
                    char.code < 32 -> '_'
                    else -> char
                }
            )
        }
    }.trim().trimEnd('.')

    return sanitized.takeIf(String::isNotEmpty) ?: "_"
}

internal data class RemapDeclarationReport(
    val displayName: String,
    val declaredSourceNamespace: String?,
    val effectiveSourceNamespace: String,
    val bucket: String,
)

internal data class RemapBucketReport(
    val name: String,
    val sourceNamespace: String,
    val classpathKind: String,
    val dependencyCount: Int,
)

internal data class RemapArtifactReport(
    val component: String,
    val variant: String?,
    val artifact: String,
    val fileName: String?,
    val displayPath: String?,
    val sourceNamespace: String,
    val artifactNamespace: String,
    val consumerClasspathNamespace: String,
    val classpathKind: String,
    val kind: String,
)

internal data class RemapReplacementReport(
    val action: String,
    val original: RemapArtifactReport,
    val replacement: RemapArtifactReport?,
    val fallbackReason: String?,
    val warnings: List<String>,
)

internal data class RemapClasspathReport(
    val kind: String,
    val kept: Int,
    val replaced: Int,
    val fallback: Int,
    val files: List<String>,
)

internal fun remapReportFromDecisions(
    target: String,
    compilation: String,
    consumerClasspathNamespace: String,
    declarations: List<RemapDeclarationReport>,
    buckets: List<RemapBucketReport>,
    rawArtifacts: List<ResolvedModArtifact>,
    remappedArtifacts: List<ResolvedModArtifact>,
    retainedNonModLibraries: List<File> = emptyList(),
    classpathResults: List<ClasspathNormalizationResult>,
): RemapReport {
    val replacements = classpathResults.flatMap { result ->
        result.decisions.map { decision -> decision.toReport() }
    }
    val warnings = replacements.flatMap(RemapReplacementReport::warnings).distinct()
    return RemapReport(
        target = target.redactSecrets(),
        compilation = compilation.redactSecrets(),
        consumerClasspathNamespace = consumerClasspathNamespace.redactSecrets(),
        declarations = declarations.map { it.redacted() },
        buckets = buckets.map { it.redacted() },
        rawArtifacts = rawArtifacts.map { it.toReport(artifactNamespace = it.key.sourceNamespace) },
        remappedArtifacts = remappedArtifacts.map { it.toReport(artifactNamespace = it.key.consumerNamespace) },
        retainedNonModLibraries = retainedNonModLibraries.map { it.safeDisplayPath() }.distinct().sorted(),
        replacements = replacements,
        classpaths = classpathResults.map { it.toReport() },
        warnings = warnings,
        errors = emptyList(),
    )
}

internal fun String.redactSecrets(): String {
    var value = this
    value = Regex("(?i)([?&;,_-]?(?:token|password|passwd|pwd|secret|credential|credentials|apikey|api_key|access_key|private_key|auth)[^=:\\s]*\\s*[=:]\\s*)([^&;\\s]+)")
        .replace(value) { it.groupValues[1] + "<redacted>" }
    value = Regex("(?i)(https?://)([^/@:\\s]+):([^/@\\s]+)@")
        .replace(value) { it.groupValues[1] + "<redacted>:<redacted>@" }
    value = Regex("(?i)\\b(?:ghp|github_pat|glpat|xox[baprs]|AKIA|ASIA)[A-Za-z0-9_\\-]{8,}\\b")
        .replace(value, "<redacted>")
    value = Regex("(?i)\\b[A-Z_]*(?:TOKEN|PASSWORD|PASSWD|PWD|SECRET|CREDENTIAL|API_KEY|ACCESS_KEY|PRIVATE_KEY|AUTH)[A-Z_]*\\b")
        .replace(value, "<redacted>")
    return value
}

internal fun File.safeDisplayPath(projectDir: File? = null): String {
    val base = projectDir?.normalize()
    val normalized = normalize()
    val relative = base?.let { runCatching { normalized.relativeTo(it).path }.getOrNull() }
        ?.takeUnless { it.startsWith("..") }
    return (relative ?: name).replace(File.separatorChar, '/').redactSecrets()
}

private fun ReplacementDecision.toReport(): RemapReplacementReport = when (this) {
    is ReplacementDecision.Replace -> RemapReplacementReport(
        action = "replace",
        original = original.toReport(artifactNamespace = original.key.sourceNamespace),
        replacement = replacement.toReport(artifactNamespace = replacement.key.consumerNamespace),
        fallbackReason = null,
        warnings = warnings.map { it.message.redactSecrets() },
    )
    is ReplacementDecision.Keep -> RemapReplacementReport(
        action = "keep",
        original = original.toReport(artifactNamespace = original.key.sourceNamespace),
        replacement = null,
        fallbackReason = null,
        warnings = warnings.map { it.message.redactSecrets() },
    )
    is ReplacementDecision.Fallback -> RemapReplacementReport(
        action = "fallback",
        original = original.toReport(artifactNamespace = original.key.sourceNamespace),
        replacement = null,
        fallbackReason = reason.reportName,
        warnings = warnings.map { it.message.redactSecrets() },
    )
}

private fun ResolvedModArtifact.toReport(artifactNamespace: String) = RemapArtifactReport(
    component = key.selectedComponentIdentity.redactSecrets(),
    variant = (key.selectedVariantIdentity.displayName ?: key.selectedVariantIdentity.attributes.toString()).redactSecrets(),
    artifact = key.artifactIdentity.displayName.redactSecrets(),
    fileName = key.artifactIdentity.fileName?.redactSecrets(),
    displayPath = file?.safeDisplayPath(),
    sourceNamespace = key.sourceNamespace.redactSecrets(),
    artifactNamespace = artifactNamespace.redactSecrets(),
    consumerClasspathNamespace = key.consumerNamespace.redactSecrets(),
    classpathKind = key.classpathKind.reportName,
    kind = key.artifactIdentity.kind.reportName,
)

private fun ClasspathNormalizationResult.toReport() = RemapClasspathReport(
    kind = kind.reportName,
    kept = decisions.count { it is ReplacementDecision.Keep },
    replaced = decisions.count { it is ReplacementDecision.Replace },
    fallback = decisions.count { it is ReplacementDecision.Fallback },
    files = files.map { it.safeDisplayPath() },
)

private fun RemapDeclarationReport.redacted() = copy(
    displayName = displayName.redactSecrets(),
    declaredSourceNamespace = declaredSourceNamespace?.redactSecrets(),
    effectiveSourceNamespace = effectiveSourceNamespace.redactSecrets(),
    bucket = bucket.redactSecrets(),
)

private fun RemapBucketReport.redacted() = copy(
    name = name.redactSecrets(),
    sourceNamespace = sourceNamespace.redactSecrets(),
    classpathKind = classpathKind.redactSecrets(),
)

private fun StringBuilder.appendObject(body: JsonWriter.() -> Unit) {
    JsonWriter(this).apply(body).close()
}

private class JsonWriter(private val out: StringBuilder) {
    private var first = true

    init {
        out.append('{')
    }

    fun close() {
        out.append('}')
    }

    fun field(name: String, value: String?) {
        comma(); string(name); out.append(':'); if (value == null) out.append("null") else string(value.redactSecrets())
    }

    fun field(name: String, values: List<String>) {
        comma(); string(name); out.append(':'); out.append('[')
        values.forEachIndexed { index, value -> if (index > 0) out.append(','); string(value.redactSecrets()) }
        out.append(']')
    }

    fun <T> field(name: String, values: List<T>, writer: JsonWriter.(T) -> Unit) {
        comma(); string(name); out.append(':'); out.append('[')
        values.forEachIndexed { index, value -> if (index > 0) out.append(','); writer(value) }
        out.append(']')
    }

    private fun comma() {
        if (first) first = false else out.append(',')
    }

    fun string(value: String) {
        out.append('"')
        value.forEach { char ->
            when (char) {
                '\\' -> out.append("\\\\")
                '"' -> out.append("\\\"")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\t' -> out.append("\\t")
                else -> out.append(char)
            }
        }
        out.append('"')
    }
}

private fun JsonWriter.writeJson(report: RemapDeclarationReport) {
    val (displayName, declaredSourceNamespace, effectiveSourceNamespace, bucket) = report
    field("displayName", displayName)
    field("declaredSourceNamespace", declaredSourceNamespace)
    field("effectiveSourceNamespace", effectiveSourceNamespace)
    field("bucket", bucket)
}

private fun RemapDeclarationReport.writeJson(out: StringBuilder) = out.appendObject {
    field("displayName", displayName)
    field("declaredSourceNamespace", declaredSourceNamespace)
    field("effectiveSourceNamespace", effectiveSourceNamespace)
    field("bucket", bucket)
}

private fun JsonWriter.writeJson(report: RemapBucketReport) {
    val (name, sourceNamespace, classpathKind, dependencyCount) = report
    field("name", name)
    field("sourceNamespace", sourceNamespace)
    field("classpathKind", classpathKind)
    field("dependencyCount", dependencyCount.toString())
}

private fun RemapBucketReport.writeJson(out: StringBuilder) = out.appendObject {
    field("name", name)
    field("sourceNamespace", sourceNamespace)
    field("classpathKind", classpathKind)
    field("dependencyCount", dependencyCount.toString())
}

private fun JsonWriter.writeJson(report: RemapArtifactReport) {
    val (component, variant, artifact, fileName, displayPath, sourceNamespace, artifactNamespace, consumerClasspathNamespace, classpathKind, kind) = report
    field("component", component)
    field("variant", variant)
    field("artifact", artifact)
    field("fileName", fileName)
    field("displayPath", displayPath)
    field("sourceNamespace", sourceNamespace)
    field("artifactNamespace", artifactNamespace)
    field("consumerClasspathNamespace", consumerClasspathNamespace)
    field("classpathKind", classpathKind)
    field("kind", kind)
}

private fun RemapArtifactReport.writeJson(out: StringBuilder) = out.appendObject {
    field("component", component)
    field("variant", variant)
    field("artifact", artifact)
    field("fileName", fileName)
    field("displayPath", displayPath)
    field("sourceNamespace", sourceNamespace)
    field("artifactNamespace", artifactNamespace)
    field("consumerClasspathNamespace", consumerClasspathNamespace)
    field("classpathKind", classpathKind)
    field("kind", kind)
}

private fun JsonWriter.writeJson(report: RemapReplacementReport) {
    field("action", report.action)
    field("original", listOf(report.original)) { writeJson(it) }
    field("replacement", listOfNotNull(report.replacement)) { writeJson(it) }
    field("fallbackReason", report.fallbackReason)
    field("warnings", report.warnings)
}

private fun RemapReplacementReport.writeJson(out: StringBuilder) = out.appendObject {
    field("action", action)
    field("original", listOf(original)) { writeJson(it) }
    field("replacement", listOfNotNull(replacement)) { writeJson(it) }
    field("fallbackReason", fallbackReason)
    field("warnings", warnings)
}

private fun JsonWriter.writeJson(report: RemapClasspathReport) {
    val (kind, kept, replaced, fallback, files) = report
    field("kind", kind)
    field("kept", kept.toString())
    field("replaced", replaced.toString())
    field("fallback", fallback.toString())
    field("files", files)
}

private fun RemapClasspathReport.writeJson(out: StringBuilder) = out.appendObject {
    field("kind", kind)
    field("kept", kept.toString())
    field("replaced", replaced.toString())
    field("fallback", fallback.toString())
    field("files", files)
}
