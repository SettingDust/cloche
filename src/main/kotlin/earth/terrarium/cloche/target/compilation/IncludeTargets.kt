package earth.terrarium.cloche.target.compilation

import earth.terrarium.cloche.api.target.MinecraftTarget
import earth.terrarium.cloche.target.MinecraftTargetInternal
import net.msrandom.minecraftcodev.includes.IncludeTargetOrigin
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.Provider
import java.util.IdentityHashMap

internal data class IncludeTargetRequest(
    val origin: IncludeTargetOrigin,
    val dependency: ProjectDependency,
)

internal class IncludeTargetRecord(
    val target: MinecraftTarget,
) {
    private val producer = target as MinecraftTargetInternal

    // All consumers use one late snapshot, including the public collector and inherited graphs.
    private val snapshot by lazy(LazyThreadSafetyMode.NONE) {
        val project = producer.project
        val name = target.capabilitySuffix?.let { "${project.name}-$it" } ?: project.name
        val origin = IncludeTargetOrigin(project.rootDir.canonicalPath, project.path, target.name, project.group.toString(), name)
        IncludeTargetRequest(
            origin,
            producer.project().apply {
                capabilities { requireCapability("${origin.group}:${origin.name}") }
            },
        )
    }

    val request: Provider<IncludeTargetRequest> = producer.project.provider { snapshot }
}

internal class IncludeTargets {
    private val records = IdentityHashMap<MinecraftTarget, IncludeTargetRecord>()
    private val listeners = mutableListOf<(IncludeTargetRecord) -> Unit>()

    fun add(target: MinecraftTarget) {
        if (!records.containsKey(target)) add(IncludeTargetRecord(target))
    }

    fun add(record: IncludeTargetRecord) {
        if (records.containsKey(record.target)) return
        records[record.target] = record
        listeners.forEach { it(record) }
    }

    fun all(action: (IncludeTargetRecord) -> Unit) {
        records.values.forEach(action)
        listeners.add(action)
    }
}
