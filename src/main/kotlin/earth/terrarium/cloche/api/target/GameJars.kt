package earth.terrarium.cloche.api.target

import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

/** Target-owned game files before remapping into the development namespace. */
sealed interface GameJars {
    class Split internal constructor(val common: Provider<RegularFile>, val client: Provider<RegularFile>) : GameJars

    class Joined internal constructor(val joined: Provider<RegularFile>) : GameJars
}
