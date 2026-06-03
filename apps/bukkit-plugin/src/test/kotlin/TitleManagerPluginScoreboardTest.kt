import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.animation.DefaultAnimationFiles
import dev.tarkan.titlemanager.bukkit.api.TitleManagerApi
import dev.tarkan.titlemanager.bukkit.api.TitleManagerSessionType
import dev.tarkan.titlemanager.bukkit.api.TitleManagerServices
import dev.tarkan.titlemanager.bukkit.configuration.AdvancedConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.AnnouncerConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.PlaceholderConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.PlayerListConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.ScoreboardConfiguration
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeSidebar
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeThreadingPolicy
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeThreadingMode
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModuleFactory
import dev.tarkan.titlemanager.bukkit.diagnostics.VersionModuleSelector
import dev.tarkan.titlemanager.bukkit.integration.BungeeCordService
import dev.tarkan.titlemanager.bukkit.metrics.MetricsService
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.LegacySpigotRuntimeAdapter
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.LegacySpigotTitleOnlyRuntimeAdapter
import dev.tarkan.titlemanager.bukkit.storage.PlayerStorage
import dev.tarkan.titlemanager.bukkit.update.UpdateClient
import dev.tarkan.titlemanager.bukkit.update.UpdateService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.runBlocking
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.koin.core.context.GlobalContext
import org.bukkit.Location
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.ServicePriority
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart

import me.clip.placeholderapi.PlaceholderAPI
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.permission.Permission
import dev.tarkan.titlemanager.time.Timing
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.InetAddress
import java.nio.file.Path
import java.sql.DriverManager
import java.sql.SQLException
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.UUID
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteExisting
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TitleManagerPluginScoreboardTest : TitleManagerPluginMockBukkitTestSupport() {

    @Test
    fun `plugin disable closes active runtime sidebars through the selected module`() {
        val server = MockBukkit.mock()
        val originalSelectorFactory = TitleManagerPlugin.versionModuleSelectorFactory
        val module = ThreadRecordingRuntimeModule()

        try {
            TitleManagerPlugin.versionModuleSelectorFactory = {
                VersionModuleSelector(listOf(ThreadRecordingRuntimeModuleFactory(module)))
            }

            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            server.addPlayer("Alice")

            assertEventually {
                assertTrue(module.operations.contains("sidebar-create"), "operations=${module.operations}")
            }

            module.operations.clear()
            module.offMainThreadOperations.clear()

            server.pluginManager.disablePlugin(plugin)

            assertTrue(module.operations.contains("sidebar-close"), "operations=${module.operations}")
            assertTrue(module.operations.contains("module-close"), "operations=${module.operations}")
            assertTrue(module.operations.indexOf("sidebar-close") < module.operations.indexOf("module-close"), "operations=${module.operations}")
            assertTrue(module.offMainThreadOperations.isEmpty(), "Sidebar close ran off the main thread: ${module.offMainThreadOperations}")
        } finally {
            TitleManagerPlugin.versionModuleSelectorFactory = originalSelectorFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin disable clears active title actionbar player-list and sidebar sessions before closing runtime`() {
        val server = MockBukkit.mock()
        val originalSelectorFactory = TitleManagerPlugin.versionModuleSelectorFactory
        val module = ThreadRecordingRuntimeModule()

        try {
            TitleManagerPlugin.versionModuleSelectorFactory = {
                VersionModuleSelector(listOf(ThreadRecordingRuntimeModuleFactory(module)))
            }

            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            val api = requireNotNull(server.servicesManager.load(TitleManagerApi::class.java))

            api.showTitle(player, "Runtime Title", "Runtime Subtitle", Timing.default)
            api.sendActionbar(player, "Runtime Actionbar")
            api.setPlayerListHeaderAndFooter(player, "Runtime Header", "Runtime Footer")
            api.setSidebar(player, "Runtime Sidebar", listOf("Runtime Line"))

            assertEventually {
                assertTrue(module.operations.contains("show-title"), "operations=${module.operations}")
                assertTrue(module.operations.contains("actionbar"), "operations=${module.operations}")
                assertTrue(module.operations.contains("player-list"), "operations=${module.operations}")
                assertTrue(module.operations.contains("sidebar-line"), "operations=${module.operations}")
            }

            module.operations.clear()
            module.offMainThreadOperations.clear()

            server.pluginManager.disablePlugin(plugin)

            assertTrue(module.operations.contains("title"), "operations=${module.operations}")
            assertTrue(module.operations.contains("subtitle"), "operations=${module.operations}")
            assertTrue(module.operations.contains("actionbar"), "operations=${module.operations}")
            assertTrue(module.operations.contains("player-list"), "operations=${module.operations}")
            assertTrue(module.operations.contains("sidebar-close"), "operations=${module.operations}")
            assertTrue(module.operations.contains("module-close"), "operations=${module.operations}")
            val moduleCloseIndex = module.operations.indexOf("module-close")
            assertTrue(module.operations.indexOf("title") < moduleCloseIndex, "operations=${module.operations}")
            assertTrue(module.operations.indexOf("subtitle") < moduleCloseIndex, "operations=${module.operations}")
            assertTrue(module.operations.indexOf("actionbar") < moduleCloseIndex, "operations=${module.operations}")
            assertTrue(module.operations.indexOf("player-list") < moduleCloseIndex, "operations=${module.operations}")
            assertTrue(module.operations.indexOf("sidebar-close") < moduleCloseIndex, "operations=${module.operations}")
            assertTrue(module.offMainThreadOperations.isEmpty(), "Runtime cleanup ran off the main thread: ${module.offMainThreadOperations}")
        } finally {
            TitleManagerPlugin.versionModuleSelectorFactory = originalSelectorFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `legacy scoreboard command alias toggles sidebar state`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            player.setOp(true)
            val playerStorage = GlobalContext.get().get<PlayerStorage>()
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)

            server.execute("titlemanager", player, "scoreboard", "toggle").apply {
                assertTrue(hasSucceeded())
            }

            assertFalse(playerStorage.get(player.uniqueId).isSidebarEnabled)
            assertFalse(playerContext.hasScoreboard())
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `player join applies scoreboard feature listener`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)

            assertTrue(playerContext.hasScoreboard())
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `scoreboard feature renders configured title and lines on join`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("scoreboard.yml").writeText(
                """
                    enabled: true
                    title: "&aRuntime Board"
                    content: |-
                      Line One
                      Line Two
                    worlds: {}
                """.trimIndent()
            )
            server.pluginManager.enablePlugin(plugin)
            val player = server.addPlayer("Alice")
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)

            assertEventually {
                assertEquals("§aRuntime Board", playerContext.getScoreboardTitle())
                assertEquals("Line One", playerContext.getScoreboardValue(1))
                assertEquals("Line Two", playerContext.getScoreboardValue(2))
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `world change reapplies scoreboard feature configuration`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)
            val scoreboardConfiguration = GlobalContext.get().get<ScoreboardConfiguration>()
            val oldWorld = player.world
            val endWorld = server.addSimpleWorld("world_the_end")

            assertTrue(playerContext.hasScoreboard())
            assertFalse(scoreboardConfiguration.worlds.getValue("world_the_end").enabled)

            assertTrue(player.teleport(Location(endWorld, 0.0, 64.0, 0.0)))
            assertEquals("world_the_end", player.world.name)
            server.pluginManager.callEvent(PlayerChangedWorldEvent(player, oldWorld))

            assertFalse(playerContext.hasScoreboard())
        } finally {
            MockBukkit.unmock()
        }
    }
}
