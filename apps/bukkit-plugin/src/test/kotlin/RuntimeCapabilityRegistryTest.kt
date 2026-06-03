import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityRegistry
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeSidebar
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeThreadingPolicy
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import io.mockk.every
import io.mockk.mockk
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.plugin.PluginManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RuntimeCapabilityRegistryTest {
    @Test
    fun `normal runtime reports current Bukkit API capability path`() {
        val capabilities = RuntimeCapabilityRegistry().normalRuntime(server("1.20.4-R0.1-SNAPSHOT"))

        assertEquals("bukkit-api (1.20.4)", capabilities.versionModule)
        assertEquals(
            "title=main-thread, actionbar=main-thread, player-list=main-thread, sidebar=main-thread",
            capabilities.versionModuleThreading
        )
        assertTrue(capabilities.capabilities.any { it.name == "titles" && it.status == "available" })
        assertTrue(capabilities.capabilities.any { it.name == "direct-nms" && it.status == "unavailable" })
        assertTrue(capabilities.capabilities.any { it.name == "commands" && it.status == "available" })
        assertTrue(capabilities.capabilities.any { it.name == "command-suggestions" && it.status == "available" })
    }

    @Test
    fun `normal runtime can snapshot an already selected module`() {
        val capabilities = RuntimeCapabilityRegistry(SelectedModule).normalRuntime(server("1.8.8-R0.1-SNAPSHOT"))

        assertEquals("selected-test-module", capabilities.versionModule)
        assertEquals("title=main-thread, actionbar=main-thread, player-list=main-thread, sidebar=main-thread", capabilities.versionModuleThreading)
        assertEquals("selected-capability", capabilities.capabilities.first().name)
        assertTrue(capabilities.capabilities.any { it.name == "commands" })
    }

    @Test
    fun `safe mode runtime reports unavailable feature capabilities`() {
        val capabilities = RuntimeCapabilityRegistry().safeModeRuntime()

        assertEquals("unavailable (runtime not started)", capabilities.versionModule)
        assertEquals("inactive", capabilities.versionModuleThreading)
        assertEquals("runtime-features", capabilities.capabilities.single().name)
        assertEquals("unavailable", capabilities.capabilities.single().status)
    }

    private fun server(bukkitVersion: String): Server {
        val server = mockk<Server>(relaxed = true)
        val pluginManager = mockk<PluginManager>(relaxed = true)

        every { server.bukkitVersion } returns bukkitVersion
        every { server.pluginManager } returns pluginManager

        return server
    }

    private object SelectedModule : RuntimeVersionModule {
        override val id: String = "selected-test"
        override val displayName: String = "selected-test-module"
        override val capabilities = listOf(DiagnosticsStatus("selected-capability", "available", "selected"))
        override val threadingPolicy: RuntimeThreadingPolicy = RuntimeThreadingPolicy.mainThreadOnly()

        override fun sendTitleTimes(player: Player, times: Title.Times) = Unit

        override fun sendTitle(player: Player, title: Component) = Unit

        override fun sendSubtitle(player: Player, subtitle: Component) = Unit

        override fun showTitle(player: Player, title: Title) = Unit

        override fun sendActionBar(player: Player, actionBar: Component) = Unit

        override fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component) = Unit

        override fun createSidebar(player: Player): RuntimeSidebar = object : RuntimeSidebar {
            override var title: String = ""

            override fun isAppliedTo(player: Player): Boolean = false

            override fun get(index: Int): String? = null

            override fun set(index: Int, value: String) = Unit

            override fun remove(index: Int) = Unit

            override fun close() = Unit
        }
    }
}
