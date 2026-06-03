import dev.tarkan.titlemanager.bukkit.configuration.ConfigurationException
import dev.tarkan.titlemanager.bukkit.lifecycle.TransactionalTitleManagerReloader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TransactionalTitleManagerReloaderTest {
    @Test
    fun `started runtime validates configuration before disabling and enabling`() {
        val events = mutableListOf<String>()
        val reloader = TransactionalTitleManagerReloader(
            isRuntimeStarted = { true },
            validateConfiguration = { events += "validate" },
            disableRuntime = { events += "disable" },
            enableRuntime = { events += "enable" }
        )

        reloader.reload()

        assertEquals(listOf("validate", "disable", "enable"), events)
    }

    @Test
    fun `started runtime remains running when configuration validation fails`() {
        val events = mutableListOf<String>()
        val reloader = TransactionalTitleManagerReloader(
            isRuntimeStarted = { true },
            validateConfiguration = {
                events += "validate"
                throw ConfigurationException("Invalid config")
            },
            disableRuntime = { events += "disable" },
            enableRuntime = { events += "enable" }
        )

        assertFailsWith<ConfigurationException> {
            reloader.reload()
        }

        assertEquals(listOf("validate"), events)
    }

    @Test
    fun `safe mode reload skips preflight and attempts enable`() {
        val events = mutableListOf<String>()
        val reloader = TransactionalTitleManagerReloader(
            isRuntimeStarted = { false },
            validateConfiguration = { events += "validate" },
            disableRuntime = { events += "disable" },
            enableRuntime = { events += "enable" }
        )

        reloader.reload()

        assertEquals(listOf("disable", "enable"), events)
    }
}
