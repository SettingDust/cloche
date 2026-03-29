package earth.terrarium.cloche.tasks.data

import org.gradle.api.Action

/**
 * Provides various ways to access the content of a metadata file.
 */
interface MetadataFileProvider<ElementT> {
    /**
     * Returns the metadata file as a [StringBuilder]. Changes to the returned instance will be applied to the file.
     * The returned instance is only valid until one of the other methods on this interface is called.
     *
     * @return A `[StringBuilder]` representation of the metadata file.
     */
    fun asString(): StringBuilder

    /**
     * Directly edit the metadata file as a [MutableMap]. Changes to the delegated instance will be applied
     * to the metadata file.
     *
     * @param action The action with the delegated instance of [MutableMap]
     */
    fun withContents(action: Action<MutableMap<String, Any?>>)

    /**
     * Make a modified version of an immutable [ElementT] via the supplied action. The instance returned from the supplied action will be applied
     * to the metadata file.
     * This function is recommended in the Kotlin DSL over [withContents] or other overloads
     *
     * @param action The action to change the [ElementT]
     */
    fun withElement(action: ElementT.() -> ElementT)
}
