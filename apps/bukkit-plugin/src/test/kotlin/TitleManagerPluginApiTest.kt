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

class TitleManagerPluginApiTest : TitleManagerPluginMockBukkitTestSupport() {

    @Test
    fun `plugin registers Bukkit API service for managed sends`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)
            val api = requireNotNull(server.servicesManager.load(TitleManagerApi::class.java))
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)

            val titleSession = api.showTitle(player, "Api Title", "Api Subtitle", Timing.default)
            val actionbarSession = api.sendActionbar(player, "Api Actionbar")
            val playerListSession = api.setPlayerListHeaderAndFooter(player, "Api Header", "Api Footer")
            val sidebarSession = api.setSidebar(player, "Api Sidebar", listOf("Api Line"))

            assertSame(api, TitleManagerServices.require(plugin))
            assertEquals(player.uniqueId, titleSession.playerUniqueId)
            assertEquals(TitleManagerSessionType.TITLE, titleSession.type)
            assertEquals(TitleManagerSessionType.ACTIONBAR, actionbarSession.type)
            assertEquals(TitleManagerSessionType.PLAYER_LIST, playerListSession.type)
            assertEquals(TitleManagerSessionType.SIDEBAR, sidebarSession.type)
            assertFalse(titleSession.isClosed)
            assertFalse(actionbarSession.isClosed)
            awaitNextTitle(player, expectedTitle = "Api Title", expectedSubtitle = "Api Subtitle")
            assertEquals("Api Actionbar", awaitNextActionBar(player, expected = "Api Actionbar"))
            awaitNextPlayerList(player, expectedHeader = "Api Header", expectedFooter = "Api Footer")
            assertEventually {
                assertEquals("Api Sidebar", playerContext.getScoreboardTitle())
                assertEquals("Api Line", playerContext.getScoreboardValue(1))
            }

            api.clearPlayerListHeaderAndFooter(player)
            api.clearSidebar(player)

            awaitNextPlayerList(player, expectedHeader = "", expectedFooter = "")
            assertEventually {
                assertFalse(playerContext.hasScoreboard())
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `Bukkit API rejects sidebars exceeding Bukkit line limit before applying scoreboard`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)
            val api = requireNotNull(server.servicesManager.load(TitleManagerApi::class.java))
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)
            playerContext.removeScoreboard()
            assertFalse(playerContext.hasScoreboard())

            val exception = assertFailsWith<IllegalArgumentException> {
                api.setSidebar(player, "Too Many Lines", (1..16).map { "Line $it" })
            }

            assertTrue(exception.message!!.contains("Sidebars support at most 15 lines"))
            assertFalse(playerContext.hasScoreboard())
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `Bukkit API rejects sends when selected runtime lacks capability`() {
        val server = MockBukkit.mock()
        val originalSelectorFactory = TitleManagerPlugin.versionModuleSelectorFactory
        val module = ThreadRecordingRuntimeModule(
            capabilities = listOf(
                DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.UNAVAILABLE, "test missing actionbar"),
                DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                DiagnosticsStatus(RuntimeCapability.DIRECT_NMS, RuntimeCapabilityStatus.UNAVAILABLE, "test module")
            )
        )

        try {
            TitleManagerPlugin.versionModuleSelectorFactory = {
                VersionModuleSelector(listOf(ThreadRecordingRuntimeModuleFactory(module)))
            }
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("advanced.yml").writeText(
                """
                    configVersion: 7
                    threadPoolSize: 4
                    usingConfig: false
                    usingBungeeCord: false
                    checkForUpdates: false
                    databaseConnectionString: ""
                """.trimIndent()
            )
            server.pluginManager.enablePlugin(plugin)
            val player = server.addPlayer("Alice")
            val api = requireNotNull(server.servicesManager.load(TitleManagerApi::class.java))

            val exception = assertFailsWith<UnsupportedOperationException> {
                api.sendActionbar(player, "Unsupported")
            }

            assertTrue(exception.message!!.contains("capability 'actionbar'"))
            assertTrue(exception.message!!.contains("test missing actionbar"))
            assertTrue(module.operations.isEmpty())

            server.executeConsole("titlemanager", "actionbar", "msg", "Alice", "Unsupported").apply {
                val message = sender.nextMessage()!!
                assertTrue(message.contains("capability 'actionbar'"))
                assertTrue(message.contains("test missing actionbar"))
            }
            server.executeConsole("titlemanager", "amsg", "Alice", "Unsupported").apply {
                val message = sender.nextMessage()!!
                assertTrue(message.contains("capability 'actionbar'"))
                assertTrue(message.contains("test missing actionbar"))
            }
            assertTrue(module.operations.isEmpty())
        } finally {
            TitleManagerPlugin.versionModuleSelectorFactory = originalSelectorFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `API session close only clears the active session it created`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)
            val api = requireNotNull(server.servicesManager.load(TitleManagerApi::class.java))

            val firstSession = api.sendActionbar(player, "First")
            assertFalse(firstSession.isClosed)
            assertEquals("First", awaitNextActionBar(player, expected = "First"))

            val secondSession = api.sendActionbar(player, "Second")
            assertEquals("Second", awaitNextActionBar(player, expected = "Second"))

            firstSession.close()
            assertTrue(firstSession.isClosed)
            assertNoActionBarWithin(player, unexpected = "")

            secondSession.close()
            assertTrue(secondSession.isClosed)
            assertEquals("", awaitNextActionBarRaw(player, expected = ""))
            secondSession.close()
            assertTrue(secondSession.isClosed)
            assertNoActionBarWithin(player, unexpected = "")
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `API session close only clears active player-list and sidebar sessions`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)
            val api = requireNotNull(server.servicesManager.load(TitleManagerApi::class.java))
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)

            val firstPlayerListSession = api.setPlayerListHeaderAndFooter(player, "First Header", "First Footer")
            awaitNextPlayerList(player, expectedHeader = "First Header", expectedFooter = "First Footer")

            val secondPlayerListSession = api.setPlayerListHeaderAndFooter(player, "Second Header", "Second Footer")
            awaitNextPlayerList(player, expectedHeader = "Second Header", expectedFooter = "Second Footer")

            firstPlayerListSession.close()
            assertNoPlayerListWithin(player, unexpectedHeader = "", unexpectedFooter = "")

            secondPlayerListSession.close()
            awaitNextPlayerList(player, expectedHeader = "", expectedFooter = "")

            val firstSidebarSession = api.setSidebar(player, "First Sidebar", listOf("First Line"))
            assertEventually {
                assertEquals("First Sidebar", playerContext.getScoreboardTitle())
                assertEquals("First Line", playerContext.getScoreboardValue(1))
            }

            val secondSidebarSession = api.setSidebar(player, "Second Sidebar", listOf("Second Line"))
            assertEventually {
                assertEquals("Second Sidebar", playerContext.getScoreboardTitle())
                assertEquals("Second Line", playerContext.getScoreboardValue(1))
            }

            firstSidebarSession.close()
            Thread.sleep(250)
            assertEquals("Second Sidebar", playerContext.getScoreboardTitle())
            assertEquals("Second Line", playerContext.getScoreboardValue(1))

            secondSidebarSession.close()
            assertEventually {
                assertFalse(playerContext.hasScoreboard())
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin unregisters Bukkit API service on disable`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)

            assertNotNull(server.servicesManager.load(TitleManagerApi::class.java))

            server.pluginManager.disablePlugin(plugin)

            assertNull(server.servicesManager.load(TitleManagerApi::class.java))
        } finally {
            MockBukkit.unmock()
        }
    }
}
