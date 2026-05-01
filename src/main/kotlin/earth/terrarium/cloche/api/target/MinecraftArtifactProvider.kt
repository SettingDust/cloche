package earth.terrarium.cloche.api.target

import earth.terrarium.cloche.api.attributes.ModDistribution
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * Provides access to resolved Minecraft JAR artifacts and their associated
 * classpath for different mapping namespaces.
 *
 * Each [MinecraftTarget] implementation supplies its own artifact provider,
 * making Minecraft jar resolution a first-class capability of the target itself.
 */
interface MinecraftArtifactProvider {
    /**
     * The default intermediary namespace for this target type.
     *
     * For Fabric targets this is typically `"intermediary"`,
     * for Forge/NeoForge targets this is `"srg"`.
     */
    val intermediaryNamespace: String

    /**
     * Get Minecraft jar files for a specific mapping namespace.
     *
     * @param namespace the mapping namespace to query (e.g. `"obf"`, `"intermediary"`, `"srg"`)
     * @return a map of [ModDistribution] to jar file provider, or `null` if this namespace
     *         is not directly available.
     */
    fun jars(namespace: String): Map<ModDistribution, Provider<RegularFile>>?

    /**
     * Get the classpath needed when working with a specific namespace.
     *
     * @param namespace the mapping namespace
     * @return the classpath file collection, or an empty file collection if no
     *         specific classpath is needed for the given namespace
     */
    fun classpath(namespace: String): FileCollection
}
