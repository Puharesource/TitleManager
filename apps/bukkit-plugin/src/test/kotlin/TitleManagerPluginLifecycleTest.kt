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

class TitleManagerPluginLifecycleTest : TitleManagerPluginMockBukkitTestSupport() {
    @Test
    fun `plugin enables successfully through Bukkit startup with default configs`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)

            assertTrue(plugin.isEnabled)
            assertNotNull(server.getPluginCommand("titlemanager"))
            assertTrue(plugin.dataFolder.toPath().resolve("playerinfo.sqlite").toFile().isFile)

            server.executeConsole("titlemanager", "version").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("TitleManager"))
                assertNull(sender.nextMessage())
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin migrates old sqlite playerinfo schema during startup`() {
        val server = MockBukkit.mock()

        try {
            Class.forName("org.sqlite.JDBC")
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val playerId = UUID.randomUUID()
            val databaseFile = plugin.dataFolder.toPath().resolve("playerinfo.sqlite")
            databaseFile.parent.createDirectories()
            DriverManager.getConnection("jdbc:sqlite:${databaseFile.toAbsolutePath()}").use { connection ->
                connection.createStatement().use { statement ->
                    statement.executeUpdate(
                        """
                            CREATE TABLE playerinfo (
                                uuid VARCHAR(36) NOT NULL,
                                scoreboard_toggled INTEGER NOT NULL
                            );
                        """.trimIndent()
                    )
                    statement.executeUpdate("INSERT INTO playerinfo (uuid, scoreboard_toggled) VALUES ('$playerId', 0);")
                }
            }

            server.pluginManager.enablePlugin(plugin)
            val player = CapturingPlayerMock(server, "Migrated", playerId)
            server.addPlayer(player)
            val playerStorage = GlobalContext.get().get<PlayerStorage>()
            val migratedInfo = playerStorage.get(player.uniqueId)

            assertFalse(migratedInfo.isSidebarEnabled)
            assertTrue(migratedInfo.isPlayerListEnabled)
            assertTrue(migratedInfo.isWelcomeTitleEnabled)
            assertTrue(migratedInfo.isWelcomeActionbarEnabled)

            DriverManager.getConnection("jdbc:sqlite:${databaseFile.toAbsolutePath()}").use { connection ->
                connection.prepareStatement("SELECT is_sidebar_enabled, is_player_list_enabled, is_welcome_title_enabled, is_welcome_actionbar_enabled FROM playerinfo WHERE uuid = ?;").use { statement ->
                    statement.setString(1, playerId.toString())
                    statement.executeQuery().use { resultSet ->
                        assertTrue(resultSet.next())
                        assertFalse(resultSet.getBoolean("is_sidebar_enabled"))
                        assertTrue(resultSet.getBoolean("is_player_list_enabled"))
                        assertTrue(resultSet.getBoolean("is_welcome_title_enabled"))
                        assertTrue(resultSet.getBoolean("is_welcome_actionbar_enabled"))
                    }
                }
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `using config false suppresses config driven feature listeners`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val dataFolder = plugin.dataFolder.toPath()
            dataFolder.resolve("advanced.yml").writeText(
                """
                    configVersion: 7
                    threadPoolSize: 4
                    usingConfig: false
                    usingBungeeCord: false
                    checkForUpdates: false
                    databaseConnectionString: ""
                """.trimIndent()
            )
            dataFolder.resolve("welcome-title.yml").writeText(
                """
                    enabled: true
                    delayMilliseconds: 0
                    title: "Disabled Config Title"
                    subtitle: "Disabled Config Subtitle"
                    fadeIn: 0
                    stay: 1
                    fadeOut: 0
                    firstJoin:
                      enabled: true
                      delayMilliseconds: 0
                      title: "Disabled First Title"
                      subtitle: "Disabled First Subtitle"
                      fadeIn: 0
                      stay: 1
                      fadeOut: 0
                    worlds: {}
                """.trimIndent()
            )
            dataFolder.resolve("welcome-actionbar.yml").writeText(
                """
                    enabled: true
                    delayMilliseconds: 0
                    title: "Disabled Config Actionbar"
                    firstJoin:
                      enabled: true
                      delayMilliseconds: 0
                      title: "Disabled First Actionbar"
                    worlds: {}
                """.trimIndent()
            )
            dataFolder.resolve("player-list.yml").writeText(
                """
                    enabled: true
                    header: "Disabled Header %{balance}"
                    footer: "Disabled Footer %{safe-online}"
                    worlds: {}
                """.trimIndent()
            )
            dataFolder.resolve("scoreboard.yml").writeText(
                """
                    enabled: true
                    title: "Disabled Board"
                    content: "Disabled Line"
                    worlds: {}
                """.trimIndent()
            )

            server.pluginManager.enablePlugin(plugin)
            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)

            assertNoTitleWithin(player)
            assertNoActionBarWithin(player)
            assertNoPlayerListWithin(player)
            assertFalse(GlobalContext.get().get<PlayerContextManager>().getContext(player).hasScoreboard())
            assertNotNull(server.servicesManager.load(TitleManagerApi::class.java))
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin reload clears active runtime sessions before rebuilding player contexts`() {
        val server = MockBukkit.mock()
        val originalSelectorFactory = TitleManagerPlugin.versionModuleSelectorFactory
        val module = ThreadRecordingRuntimeModule()

        try {
            TitleManagerPlugin.versionModuleSelectorFactory = {
                VersionModuleSelector(listOf(ThreadRecordingRuntimeModuleFactory(module)))
            }

            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            val api = requireNotNull(server.servicesManager.load(TitleManagerApi::class.java))

            api.showTitle(player, "Reload Title", "Reload Subtitle", Timing.default)
            api.sendActionbar(player, "Reload Actionbar")
            api.setPlayerListHeaderAndFooter(player, "Reload Header", "Reload Footer")
            api.setSidebar(player, "Reload Sidebar", listOf("Reload Line"))

            assertEventually {
                assertTrue(module.operations.contains("show-title"), "operations=${module.operations}")
                assertTrue(module.operations.contains("actionbar"), "operations=${module.operations}")
                assertTrue(module.operations.contains("player-list"), "operations=${module.operations}")
                assertTrue(module.operations.contains("sidebar-line"), "operations=${module.operations}")
            }

            module.operations.clear()
            module.offMainThreadOperations.clear()

            server.executeConsole("titlemanager", "reload").apply {
                assertTrue(hasSucceeded())
            }

            assertEventually {
                assertTrue(module.operations.contains("title"), "operations=${module.operations}")
                assertTrue(module.operations.contains("subtitle"), "operations=${module.operations}")
                assertTrue(module.operations.contains("actionbar"), "operations=${module.operations}")
                assertTrue(module.operations.contains("player-list"), "operations=${module.operations}")
                assertTrue(module.operations.contains("sidebar-close"), "operations=${module.operations}")
                assertTrue(module.operations.contains("module-close"), "operations=${module.operations}")
                assertTrue(module.operations.contains("sidebar-create"), "operations=${module.operations}")
                val moduleCloseIndex = module.operations.indexOf("module-close")
                assertTrue(module.operations.indexOf("title") < moduleCloseIndex, "operations=${module.operations}")
                assertTrue(module.operations.indexOf("subtitle") < moduleCloseIndex, "operations=${module.operations}")
                assertTrue(module.operations.indexOf("actionbar") < moduleCloseIndex, "operations=${module.operations}")
                assertTrue(module.operations.indexOf("player-list") < moduleCloseIndex, "operations=${module.operations}")
                assertTrue(module.operations.indexOf("sidebar-close") < moduleCloseIndex, "operations=${module.operations}")
                assertTrue(moduleCloseIndex < module.operations.lastIndexOf("sidebar-create"), "operations=${module.operations}")
            }
            assertTrue(module.offMainThreadOperations.isEmpty(), "Runtime reload cleanup ran off the main thread: ${module.offMainThreadOperations}")
        } finally {
            TitleManagerPlugin.versionModuleSelectorFactory = originalSelectorFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin reload replaces runtime resources and restores online players`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            player.setOp(true)
            val originalPlayerStorage = GlobalContext.get().get<PlayerStorage>()
            val originalPlayerContextManager = GlobalContext.get().get<PlayerContextManager>()
            val originalApi = requireNotNull(server.servicesManager.load(TitleManagerApi::class.java))

            server.execute("titlemanager", player, "sidebar", "toggle").apply {
                assertTrue(hasSucceeded())
            }
            assertFalse(originalPlayerStorage.get(player.uniqueId).isSidebarEnabled)
            assertEquals(1, originalPlayerContextManager.activeContextCount)

            server.executeConsole("titlemanager", "reload").apply {
                assertTrue(hasSucceeded())
            }

            val reloadedPlayerStorage = GlobalContext.get().get<PlayerStorage>()
            val reloadedPlayerContextManager = GlobalContext.get().get<PlayerContextManager>()
            val reloadedApi = requireNotNull(server.servicesManager.load(TitleManagerApi::class.java))

            assertNotSame(originalPlayerStorage, reloadedPlayerStorage)
            assertNotSame(originalPlayerContextManager, reloadedPlayerContextManager)
            assertNotSame(originalApi, reloadedApi)
            assertEquals(0, originalPlayerContextManager.activeContextCount)
            assertFailsWith<SQLException> {
                runBlocking {
                    originalPlayerStorage.load(UUID.randomUUID())
                }
            }

            assertFalse(reloadedPlayerStorage.get(player.uniqueId).isSidebarEnabled)
            assertEquals(1, reloadedPlayerContextManager.activeContextCount)

            val joinedAfterReload = server.addPlayer("Bob")
            assertNotNull(reloadedPlayerStorage.get(joinedAfterReload.uniqueId))
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin reload failure keeps existing runtime active`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            val originalPlayerStorage = GlobalContext.get().get<PlayerStorage>()
            val originalPlayerContextManager = GlobalContext.get().get<PlayerContextManager>()

            writeInvalidAdvancedConfig(plugin.dataFolder.toPath().resolve("advanced.yml"))

            server.executeConsole("titlemanager", "reload").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("Reloading TitleManager configuration"))
                val failureMessage = sender.nextMessage()!!
                assertTrue(failureMessage.contains("reload failed"))
                assertTrue(failureMessage.contains("Failed to load configuration file 'advanced.yml'"))
                assertNull(sender.nextMessage())
            }

            assertSame(originalPlayerStorage, GlobalContext.get().get<PlayerStorage>())
            assertSame(originalPlayerContextManager, GlobalContext.get().get<PlayerContextManager>())
            assertNotNull(originalPlayerStorage.get(player.uniqueId))

            val joinedAfterFailedReload = server.addPlayer("Bob")
            joinedAfterFailedReload.setOp(true)
            assertNotNull(originalPlayerStorage.get(joinedAfterFailedReload.uniqueId))
            server.execute("titlemanager", joinedAfterFailedReload, "sidebar", "toggle").apply {
                assertTrue(hasSucceeded())
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin starts BungeeCord integration and renders network placeholders`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val dataFolder = plugin.dataFolder.toPath()
            dataFolder.resolve("advanced.yml").writeText(
                """
                    configVersion: 7
                    threadPoolSize: 4
                    usingConfig: true
                    usingBungeeCord: true
                    checkForUpdates: false
                    databaseConnectionString: ""
                """.trimIndent()
            )
            dataFolder.resolve("player-list.yml").writeText(
                """
                    enabled: true
                    header: "%{server}|%{server-name}|%{bungeecord-online}|%{bungeecord-online-players}|%{online:lobby}|%{online:hub,lobby}"
                    footer: "Bungee Footer"
                    worlds: {}
                """.trimIndent()
            )

            server.pluginManager.enablePlugin(plugin)
            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)
            val bungeeCordService = GlobalContext.get().get<BungeeCordService>()

            bungeeCordService.onPluginMessageReceived("BungeeCord", player, pluginMessage("GetServers", "hub, lobby"))
            bungeeCordService.onPluginMessageReceived("BungeeCord", player, pluginMessage("GetServer", "lobby"))
            bungeeCordService.onPluginMessageReceived("BungeeCord", player, pluginMessage("PlayerCount", "hub", 3))
            bungeeCordService.onPluginMessageReceived("BungeeCord", player, pluginMessage("PlayerCount", "lobby", 2))
            GlobalContext.get().get<PlayerContextManager>().getContext(player).setConfigPlayerList()

            assertTrue(plugin.isEnabled)
            assertNotNull(server.servicesManager.load(TitleManagerApi::class.java))
            awaitNextPlayerList(player, expectedHeader = "lobby|lobby|5|5|2|5")

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                repeat(11) { sender.nextMessage() }
                assertTrue(sender.nextMessage()!!.contains("BungeeCord=active"))
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin migrates legacy config during Bukkit startup`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val dataFolder = plugin.dataFolder.toPath()
            dataFolder.resolve("config.yml").writeText(
                LEGACY_CONFIG
                    .replace("using-config: false", "using-config: true")
                    .replace("using-bungeecord: true", "using-bungeecord: false")
            )

            server.pluginManager.enablePlugin(plugin)

            assertTrue(plugin.isEnabled)
            assertTrue(dataFolder.resolve("config.yml.legacy-backup").toFile().isFile)
            assertTrue(dataFolder.resolve("advanced.yml").toFile().isFile)
            assertEquals(7, GlobalContext.get().get<AdvancedConfiguration>().configVersion)
            assertEquals("da-DK", GlobalContext.get().get<PlaceholderConfiguration>().locale)
            assertEquals("\n\${shine:[0;2;0][&3;&b]Legacy Server}\n", GlobalContext.get().get<PlayerListConfiguration>().header)
            assertEquals(false, GlobalContext.get().get<ScoreboardConfiguration>().worlds["legacy-disabled-world"]?.enabled)

            val player = server.addPlayer("Migrated")
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)
            assertEventually {
                assertEquals("Legacy Board", playerContext.getScoreboardTitle())
                assertEquals("§aLine 1", playerContext.getScoreboardValue(1))
                assertEquals("§bLine 2", playerContext.getScoreboardValue(2))
            }

            val oldWorld = player.world
            val disabledWorld = server.addSimpleWorld("legacy-disabled-world")
            assertTrue(player.teleport(Location(disabledWorld, 0.0, 64.0, 0.0)))
            server.pluginManager.callEvent(PlayerChangedWorldEvent(player, oldWorld))
            assertFalse(playerContext.hasScoreboard())

            server.executeConsole("titlemanager", "version").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("Running TitleManager"))
                assertNull(sender.nextMessage())
            }
        } finally {
            MockBukkit.unmock()
        }
    }
}
