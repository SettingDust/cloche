package earth.terrarium.cloche.target

import earth.terrarium.cloche.api.LazyConfigurable
import org.gradle.testfixtures.ProjectBuilder
import kotlin.reflect.KFunction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LazyConfigurableTests {
    @Test
    fun `LazyConfigurable exposes onConfigured`() {
        val onConfigured = LazyConfigurable::class.members.find { member ->
            member.name == "onConfigured" && member is KFunction<*>
        }

        assertNotNull(onConfigured)
    }

    @Test
    fun `onConfigured runs after first configuration`() {
        val project = ProjectBuilder.builder().build()
        val configurable = project.lazyConfigurable { "configured" }
        var captured: String? = null

        configurable.onConfigured { captured = it }

        assertEquals(null, captured)

        configurable.configure()

        assertEquals("configured", captured)
    }

    @Test
    fun `onConfigured runs immediately after configuration`() {
        val project = ProjectBuilder.builder().build()
        val configurable = project.lazyConfigurable { "configured" }
        var captured: String? = null

        configurable.configure()
        configurable.onConfigured { captured = it }

        assertEquals("configured", captured)
    }
}