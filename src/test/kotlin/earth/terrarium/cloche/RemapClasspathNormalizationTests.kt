package earth.terrarium.cloche

import earth.terrarium.cloche.remap.isRemappableModArtifactCandidate
import org.junit.jupiter.api.Test
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RemapClasspathNormalizationTests {
    @Test
    fun `jar metadata identifies remappable mod candidates`() {
        val jar = createTempFile("fabric-mod", ".jar")
        writeJar(jar, "fabric.mod.json" to "{}")

        assertTrue(jar.isRemappableModArtifactCandidate())
    }

    @Test
    fun `ordinary kotlin libraries are not remappable mod candidates`() {
        val jar = createTempFile("kotlin-stdlib", ".jar")
        writeJar(
            jar,
            "kotlin/Unit.class" to "class-bytes",
            "META-INF/kotlin-stdlib.kotlin_module" to "module",
        )

        assertFalse(jar.isRemappableModArtifactCandidate())
    }

    private fun writeJar(file: java.io.File, vararg entries: Pair<String, String>) {
        ZipOutputStream(file.outputStream()).use { zip ->
            entries.forEach { (name, content) ->
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
        }
    }
}
