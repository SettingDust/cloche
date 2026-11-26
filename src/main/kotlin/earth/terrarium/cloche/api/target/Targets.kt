package earth.terrarium.cloche.api.target

import earth.terrarium.cloche.api.LazyConfigurable
import earth.terrarium.cloche.api.metadata.FabricMetadata
import earth.terrarium.cloche.api.metadata.ForgeMetadata
import earth.terrarium.cloche.api.target.compilation.FabricCompilation
import earth.terrarium.cloche.api.target.compilation.FabricIncludedClient
import earth.terrarium.cloche.api.target.compilation.ForgeCompilation
import earth.terrarium.cloche.api.target.compilation.FabricSecondarySourceSets
import org.gradle.api.Action
import org.gradle.api.provider.Property

@JvmDefaultWithoutCompatibility
interface FabricTarget : MinecraftTarget, FabricSecondarySourceSets, FabricCompilation {
    /** Common and client shards in intermediary names, or the resolved files when mapping is skipped. */
    val gameJars: GameJars.Split

    override val metadata: FabricMetadata

    val client: LazyConfigurable<FabricSecondarySourceSets>
    val includedClient: LazyConfigurable<FabricIncludedClient>

    fun metadata(action: Action<FabricMetadata>) = action.execute(metadata)
}

@JvmDefaultWithoutCompatibility
interface ForgeLikeTarget : MinecraftTarget, ForgeCompilation {
    /** The patched, joined Minecraft JAR in the loader's namespace. */
    val gameJars: GameJars.Joined

    override val data: LazyConfigurable<ForgeCompilation>
    override val test: LazyConfigurable<ForgeCompilation>

    override val metadata: ForgeMetadata

    val group: Property<String>
    val artifact: Property<String>

    fun metadata(action: Action<ForgeMetadata>) = action.execute(metadata)
}

interface ForgeTarget : ForgeLikeTarget
interface NeoforgeTarget : ForgeLikeTarget
