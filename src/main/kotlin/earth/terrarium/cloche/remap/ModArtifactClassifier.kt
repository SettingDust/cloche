package earth.terrarium.cloche.remap

import java.io.File
import java.util.zip.ZipFile

private val modMetadataEntries = setOf(
    "fabric.mod.json",
    "quilt.mod.json",
    "META-INF/mods.toml",
    "META-INF/neoforge.mods.toml",
)

internal fun File.isRemappableModArtifactCandidate(): Boolean {
    if (!isFile || extension != "jar") {
        return false
    }

    return runCatching {
        ZipFile(this).use { zip ->
            modMetadataEntries.any { entry -> zip.getEntry(entry) != null }
        }
    }.getOrDefault(false)
}
