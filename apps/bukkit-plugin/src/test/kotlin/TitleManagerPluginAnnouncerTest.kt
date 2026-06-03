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

class TitleManagerPluginAnnouncerTest : TitleManagerPluginMockBukkitTestSupport() {

    @Test
    fun `plugin migrates legacy announcer and sends paired announcements`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val dataFolder = plugin.dataFolder.toPath()
            dataFolder.resolve("config.yml").writeText(LEGACY_CONFIG_WITH_ANNOUNCER)

            server.pluginManager.enablePlugin(plugin)

            assertTrue(plugin.isEnabled)
            assertTrue(dataFolder.resolve("config.yml.legacy-backup").toFile().isFile)
            assertTrue(dataFolder.resolve("announcer.yml").toFile().isFile)
            assertNotNull(server.servicesManager.load(TitleManagerApi::class.java))
            assertTrue(GlobalContext.get().get<AnnouncerConfiguration>().enabled)

            val player = CapturingPlayerMock(server, "Announced")
            server.addPlayer(player)
            performMockBukkitTicks(20)

            awaitNextTitle(player, expectedTitle = "Legacy title", expectedSubtitle = "Legacy subtitle")
            assertEquals("Legacy actionbar", awaitNextActionBar(player, expected = "Legacy actionbar"))
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `announcer uses one shared index for mismatched title and actionbar lists`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val dataFolder = plugin.dataFolder.toPath()
            dataFolder.toFile().mkdirs()
            dataFolder.resolve("announcer.yml").writeText(
                """
                    enabled: true
                    announcements:
                      test:
                        interval: 1
                        timings:
                          fadeIn: 1
                          stay: 2
                          fadeOut: 3
                        titles:
                          - "First title"
                          - "Second title"
                        actionbar:
                          - "First actionbar"
                """.trimIndent()
            )

            server.pluginManager.enablePlugin(plugin)

            val player = CapturingPlayerMock(server, "Indexed")
            server.addPlayer(player)

            performMockBukkitTicks(20)
            awaitNextTitle(player, expectedTitle = "First title")
            assertEquals("First actionbar", awaitNextActionBar(player, expected = "First actionbar"))

            player.titles.clear()
            player.actionBars.clear()
            performMockBukkitTicks(20)
            awaitNextTitle(player, expectedTitle = "Second title")
            assertNoActionBarWithin(player)

            player.titles.clear()
            player.actionBars.clear()
            performMockBukkitTicks(20)
            awaitNextTitle(player, expectedTitle = "First title")
            assertEquals("First actionbar", awaitNextActionBar(player, expected = "First actionbar"))
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `announcer does not run when config features are disabled globally`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val dataFolder = plugin.dataFolder.toPath()
            dataFolder.toFile().mkdirs()
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
            dataFolder.resolve("announcer.yml").writeText(announcerConfig("Suppressed title", "Suppressed actionbar"))

            server.pluginManager.enablePlugin(plugin)

            val player = CapturingPlayerMock(server, "Suppressed")
            server.addPlayer(player)
            performMockBukkitTicks(20)

            assertNoTitleWithin(player)
            assertNoActionBarWithin(player)
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `reload replaces active announcer schedules`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val dataFolder = plugin.dataFolder.toPath()
            dataFolder.toFile().mkdirs()
            val announcerConfigFile = dataFolder.resolve("announcer.yml")
            announcerConfigFile.writeText(announcerConfig("Old title", "Old actionbar"))

            server.pluginManager.enablePlugin(plugin)

            assertTrue(plugin.isEnabled)
            assertNotNull(server.servicesManager.load(TitleManagerApi::class.java))

            val player = CapturingPlayerMock(server, "Reloaded")
            server.addPlayer(player)
            performMockBukkitTicks(20)
            awaitNextTitle(player, expectedTitle = "Old title")
            assertEquals("Old actionbar", awaitNextActionBar(player, expected = "Old actionbar"))

            player.titles.clear()
            player.actionBars.clear()
            announcerConfigFile.writeText(announcerConfig("New title", "New actionbar"))

            server.executeConsole("titlemanager", "reload").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("Reloading TitleManager configuration"))
                assertTrue(sender.nextMessage()!!.contains("TitleManager configuration reload complete"))
                assertNull(sender.nextMessage())
            }

            performMockBukkitTicks(20)
            awaitNextTitle(player, expectedTitle = "New title")
            assertEquals("New actionbar", awaitNextActionBar(player, expected = "New actionbar"))
            assertNoTitleWithin(player, unexpectedTitle = "Old title")
        } finally {
            MockBukkit.unmock()
        }
    }
}
