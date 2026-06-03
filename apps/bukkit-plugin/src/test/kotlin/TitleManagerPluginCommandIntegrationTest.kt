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

class TitleManagerPluginCommandIntegrationTest : TitleManagerPluginMockBukkitTestSupport() {

    @Test
    fun `player join loads player storage and enables player commands`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            player.setOp(true)
            val playerStorage = GlobalContext.get().get<PlayerStorage>()

            assertNotNull(playerStorage.get(player.uniqueId))

            server.execute("titlemanager", player, "sidebar", "toggle").apply {
                assertTrue(hasSucceeded())
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `player command without permission reports denial and leaves state unchanged`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            val playerStorage = GlobalContext.get().get<PlayerStorage>()
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)

            server.execute("titlemanager", player, "sidebar", "toggle").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("titlemanager.command.sidebar.toggle"))
                assertNull(sender.nextMessage())
            }

            assertTrue(playerStorage.get(player.uniqueId).isSidebarEnabled)
            assertTrue(playerContext.hasScoreboard())
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `console player-only command reports denial instead of failing`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.sidebar.toggle", true)

            server.executeConsole("titlemanager", "sidebar", "toggle").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("This command can only be run as a player."))
                assertNull(sender.nextMessage())
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `actionbar broadcast respects nested world parameter`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.actionbar.broadcast", true)
            val overworldPlayer = CapturingPlayerMock(server, "Alice")
            val endPlayer = CapturingPlayerMock(server, "Bob")
            val endWorld = server.addSimpleWorld("world_the_end")

            server.addPlayer(overworldPlayer)
            server.addPlayer(endPlayer)
            assertTrue(endPlayer.teleport(Location(endWorld, 0.0, 64.0, 0.0)))
            performMockBukkitTicks(3)
            overworldPlayer.actionBars.clear()
            endPlayer.actionBars.clear()

            server.executeConsole("titlemanager", "actionbar", "bc", "-world=world_the_end", "Hello", "End").apply {
                assertTrue(hasSucceeded())
            }

            assertEquals("Hello End", awaitNextActionBar(endPlayer))
            assertNoActionBarWithin(overworldPlayer)
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `actionbar broadcast radius includes sender and nearby players only`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val sender = CapturingPlayerMock(server, "Alice")
            val nearbyPlayer = CapturingPlayerMock(server, "Bob")
            val farPlayer = CapturingPlayerMock(server, "Charlie")

            server.addPlayer(sender)
            server.addPlayer(nearbyPlayer)
            server.addPlayer(farPlayer)
            sender.setOp(true)
            sender.teleport(Location(sender.world, 0.0, 64.0, 0.0))
            nearbyPlayer.teleport(Location(sender.world, 3.0, 64.0, 0.0))
            farPlayer.teleport(Location(sender.world, 100.0, 64.0, 0.0))
            sender.actionBars.clear()
            nearbyPlayer.actionBars.clear()
            farPlayer.actionBars.clear()

            server.execute("titlemanager", sender, "actionbar", "bc", "-radius=5.5", "Nearby").apply {
                assertTrue(hasSucceeded())
            }

            assertEquals("Nearby", awaitNextActionBar(sender, expected = "Nearby"))
            assertEquals("Nearby", awaitNextActionBar(nearbyPlayer, expected = "Nearby"))
            assertNoActionBarWithin(farPlayer, unexpected = "Nearby")
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `title and actionbar message commands target named players`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.title.message", true)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.actionbar.message", true)
            val target = CapturingPlayerMock(server, "Bob")
            server.addPlayer(target)
            target.titles.clear()
            target.actionBars.clear()

            server.executeConsole("titlemanager", "title", "msg", "Bob", "Main<nl>Sub").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("Bob"))
                assertNull(sender.nextMessage())
            }
            server.executeConsole("titlemanager", "actionbar", "msg", "Bob", "Action", "Message").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("Bob"))
                assertNull(sender.nextMessage())
            }

            awaitNextTitle(target, expectedTitle = "Main", expectedSubtitle = "Sub").apply {
                assertEquals("Main", title)
                assertEquals("Sub", subtitle)
            }
            assertEquals("Action Message", awaitNextActionBar(target, expected = "Action Message"))
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `legacy flat title and actionbar message commands target named players`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.title.message", true)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.actionbar.message", true)
            val target = CapturingPlayerMock(server, "Bob")
            server.addPlayer(target)
            target.titles.clear()
            target.actionBars.clear()

            server.executeConsole("titlemanager", "msg", "Bob", "Legacy<nl>Title").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("Bob"))
                assertNull(sender.nextMessage())
            }
            server.executeConsole("titlemanager", "amsg", "Bob", "Legacy", "Action").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("Bob"))
                assertNull(sender.nextMessage())
            }

            awaitNextTitle(target, expectedTitle = "Legacy", expectedSubtitle = "Title").apply {
                assertEquals("Legacy", title)
                assertEquals("Title", subtitle)
            }
            assertEquals("Legacy Action", awaitNextActionBar(target, expected = "Legacy Action"))
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `legacy flat message commands honor silent parameter while still sending output`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.title.message", true)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.actionbar.message", true)
            val target = CapturingPlayerMock(server, "Bob")
            server.addPlayer(target)
            target.titles.clear()
            target.actionBars.clear()

            server.executeConsole("titlemanager", "msg", "-silent", "Bob", "Silent<nl>Title").apply {
                assertTrue(hasSucceeded())
                assertNull(sender.nextMessage())
            }
            server.executeConsole("titlemanager", "amsg", "-silent", "Bob", "Silent", "Action").apply {
                assertTrue(hasSucceeded())
                assertNull(sender.nextMessage())
            }

            awaitNextTitle(target, expectedTitle = "Silent", expectedSubtitle = "Title").apply {
                assertEquals("Silent", title)
                assertEquals("Title", subtitle)
            }
            assertEquals("Silent Action", awaitNextActionBar(target, expected = "Silent Action"))
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `legacy flat title and actionbar broadcast commands target online players`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.title.broadcast", true)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.actionbar.broadcast", true)
            val first = CapturingPlayerMock(server, "Alice")
            val second = CapturingPlayerMock(server, "Bob")
            server.addPlayer(first)
            server.addPlayer(second)
            first.titles.clear()
            second.titles.clear()
            first.actionBars.clear()
            second.actionBars.clear()

            server.executeConsole("titlemanager", "broadcast", "Legacy Broadcast<nl>Legacy Subtitle").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("Legacy Broadcast"))
                assertNull(sender.nextMessage())
            }
            server.executeConsole("titlemanager", "abc", "Legacy", "Actionbar").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("Legacy Actionbar"))
                assertNull(sender.nextMessage())
            }

            assertEventually {
                assertTrue(first.titles.any { it.title == "Legacy Broadcast" })
                assertTrue(first.titles.any { it.subtitle == "Legacy Subtitle" })
                assertTrue(second.titles.any { it.title == "Legacy Broadcast" })
                assertTrue(second.titles.any { it.subtitle == "Legacy Subtitle" })
            }
            assertEquals("Legacy Actionbar", awaitNextActionBar(first, expected = "Legacy Actionbar"))
            assertEquals("Legacy Actionbar", awaitNextActionBar(second, expected = "Legacy Actionbar"))
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `message command tab completion suggests online players through Bukkit command`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.title.message", true)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.actionbar.message", true)
            server.addPlayer("Alex")
            server.addPlayer("Steve")
            server.addPlayer("Avery")
            val command = requireNotNull(server.getPluginCommand("titlemanager"))

            assertContentEquals(
                listOf("Alex", "Avery"),
                command.tabComplete(server.consoleSender, "titlemanager", arrayOf("title", "message", "A"))
            )
            assertContentEquals(
                listOf("Steve"),
                command.tabComplete(server.consoleSender, "titlemanager", arrayOf("actionbar", "message", "st"))
            )
            assertContentEquals(
                listOf("Alex", "Avery"),
                command.tabComplete(server.consoleSender, "titlemanager", arrayOf("msg", "A"))
            )
            assertContentEquals(
                listOf("Steve"),
                command.tabComplete(server.consoleSender, "titlemanager", arrayOf("amsg", "st"))
            )
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `legacy command parameter tab completion suggests old flags`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.title.broadcast", true)
            val command = requireNotNull(server.getPluginCommand("titlemanager"))

            assertContentEquals(
                listOf("-fadein", "-fadeout"),
                command.tabComplete(server.consoleSender, "titlemanager", arrayOf("broadcast", "-f"))
            )
            assertContentEquals(
                listOf("-radius"),
                command.tabComplete(server.consoleSender, "titlemanager", arrayOf("broadcast", "-silent", "-r"))
            )
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `message commands report invalid target players`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.title.message", true)
            server.consoleSender.addAttachment(plugin, "titlemanager.command.actionbar.message", true)

            server.executeConsole("titlemanager", "title", "msg", "Missing", "Hello").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("Missing"))
                assertNull(sender.nextMessage())
            }
            server.executeConsole("titlemanager", "actionbar", "msg", "Missing", "Hello").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("Missing"))
                assertNull(sender.nextMessage())
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `player commands toggle persisted feature flags`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            player.setOp(true)
            val playerStorage = GlobalContext.get().get<PlayerStorage>()
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)

            assertTrue(playerStorage.get(player.uniqueId).isSidebarEnabled)
            assertTrue(playerStorage.get(player.uniqueId).isPlayerListEnabled)
            assertTrue(playerStorage.get(player.uniqueId).isWelcomeTitleEnabled)
            assertTrue(playerStorage.get(player.uniqueId).isWelcomeActionbarEnabled)
            assertTrue(playerContext.hasScoreboard())

            server.execute("titlemanager", player, "sidebar", "toggle").apply {
                assertTrue(hasSucceeded())
            }
            server.execute("titlemanager", player, "playerlist", "toggle").apply {
                assertTrue(hasSucceeded())
            }
            server.execute("titlemanager", player, "title", "toggle").apply {
                assertTrue(hasSucceeded())
            }
            server.execute("titlemanager", player, "actionbar", "toggle").apply {
                assertTrue(hasSucceeded())
            }

            playerStorage.get(player.uniqueId).apply {
                assertFalse(isSidebarEnabled)
                assertFalse(isPlayerListEnabled)
                assertFalse(isWelcomeTitleEnabled)
                assertFalse(isWelcomeActionbarEnabled)
            }
            assertFalse(playerContext.hasScoreboard())

            server.execute("titlemanager", player, "sidebar", "toggle").apply {
                assertTrue(hasSucceeded())
            }
            server.execute("titlemanager", player, "playerlist", "toggle").apply {
                assertTrue(hasSucceeded())
            }
            server.execute("titlemanager", player, "title", "toggle").apply {
                assertTrue(hasSucceeded())
            }
            server.execute("titlemanager", player, "actionbar", "toggle").apply {
                assertTrue(hasSucceeded())
            }

            playerStorage.get(player.uniqueId).apply {
                assertTrue(isSidebarEnabled)
                assertTrue(isPlayerListEnabled)
                assertTrue(isWelcomeTitleEnabled)
                assertTrue(isWelcomeActionbarEnabled)
            }
            assertTrue(playerContext.hasScoreboard())
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `player feature toggle commands do not persist enabled flags when runtime lacks capability`() {
        val server = MockBukkit.mock()
        val originalSelectorFactory = TitleManagerPlugin.versionModuleSelectorFactory
        val module = ThreadRecordingRuntimeModule(
            capabilities = listOf(
                DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.UNAVAILABLE, "test missing player-list"),
                DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.UNAVAILABLE, "test missing sidebar"),
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
            player.setOp(true)
            val playerStorage = GlobalContext.get().get<PlayerStorage>()

            runBlocking {
                playerStorage.setSidebarEnabled(player.uniqueId, false)
                playerStorage.setPlayerListEnabled(player.uniqueId, false)
            }

            server.execute("titlemanager", player, "sidebar", "toggle").apply {
                assertTrue(hasSucceeded())
            }
            player.nextMessage()!!.let { message ->
                assertTrue(message.contains("capability 'sidebar'"))
                assertTrue(message.contains("test missing sidebar"))
            }
            assertFalse(playerStorage.get(player.uniqueId).isSidebarEnabled)

            server.execute("titlemanager", player, "playerlist", "toggle").apply {
                assertTrue(hasSucceeded())
            }
            player.nextMessage()!!.let { message ->
                assertTrue(message.contains("capability 'player-list'"))
                assertTrue(message.contains("test missing player-list"))
            }
            assertFalse(playerStorage.get(player.uniqueId).isPlayerListEnabled)
            assertTrue(module.operations.isEmpty())
        } finally {
            TitleManagerPlugin.versionModuleSelectorFactory = originalSelectorFactory
            MockBukkit.unmock()
        }
    }
}
