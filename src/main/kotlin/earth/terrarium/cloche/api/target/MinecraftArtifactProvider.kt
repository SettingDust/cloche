package earth.terrarium.cloche.api.target

import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/**
 * Artifacts for a single-distribution namespace (e.g. Forge's SEARGE).
 */
interface NamespaceArtifacts {
    val jar: Provider<RegularFile>
    val classpath: FileCollection
}

/**
 * Artifacts for a namespace with common/client distribution split (e.g. Fabric).
 */
interface DistributedNamespaceArtifacts {
    val common: Provider<RegularFile>
    val client: Provider<RegularFile>
    val classpath: FileCollection
}

/**
 * Base provider interface on [MinecraftTarget].
 *
 * Each target type refines this with a loader-specific sub-interface that
 * exposes only the namespaces actually supported by that loader, making
 * the available artifacts discoverable and type-safe at compile time.
 *
 * @see FabricArtifactProvider
 * @see ForgeLikeArtifactProvider
 */
interface MinecraftArtifactProvider {
    /**
     * The default intermediary namespace for this target type.
     *
     * For Fabric targets this is typically `"intermediary"`,
     * for Forge/NeoForge targets this is `"srg"`.
     */
    val intermediaryNamespace: String
}

/**
 * Fabric-specific artifact provider.
 *
 * Exposes [obf] and [intermediary] namespaces, each providing
 * common and client distribution jars.
 */
interface FabricArtifactProvider : MinecraftArtifactProvider {
    val obf: DistributedNamespaceArtifacts
    val intermediary: DistributedNamespaceArtifacts
}

/**
 * Forge / NeoForge artifact provider.
 *
 * Exposes the [searge] namespace with a single merged jar.
 */
interface ForgeLikeArtifactProvider : MinecraftArtifactProvider {
    val searge: NamespaceArtifacts
}
