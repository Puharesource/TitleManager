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

class TitleManagerPluginUpdateTest : TitleManagerPluginMockBukkitTestSupport() {

    @Test
    fun `version command reports available update`() {
        val server = MockBukkit.mock()
        val originalUpdateClientFactory = TitleManagerPlugin.updateClientFactory

        try {
            TitleManagerPlugin.updateClientFactory = { UpdateClient { "3.0.0" } }
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("advanced.yml").writeText(
                """
                    threadPoolSize: 4
                    checkForUpdates: true
                    preventDuplicatePackets: true
                    databaseConnectionString: "username"
                """.trimIndent()
            )
            server.pluginManager.enablePlugin(plugin)
            GlobalContext.get().get<UpdateService>().refresh()

            server.executeConsole("titlemanager", "version").apply {
                assertTrue(hasSucceeded())
                val messages = generateSequence { sender.nextMessage() }.toList()
                assertTrue(messages.any { it.contains("Running TitleManager") }, "messages=$messages")
                assertTrue(messages.any { it.contains("3.0.0") }, "messages=$messages")
            }
        } finally {
            TitleManagerPlugin.updateClientFactory = originalUpdateClientFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `update notification is sent to permitted players on join`() {
        val server = MockBukkit.mock()
        val originalUpdateClientFactory = TitleManagerPlugin.updateClientFactory

        try {
            TitleManagerPlugin.updateClientFactory = { UpdateClient { "3.0.0" } }
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("advanced.yml").writeText(
                """
                    threadPoolSize: 4
                    checkForUpdates: true
                    preventDuplicatePackets: true
                    databaseConnectionString: "username"
                """.trimIndent()
            )
            server.pluginManager.enablePlugin(plugin)
            GlobalContext.get().get<UpdateService>().refresh()

            val player = server.addPlayer("Updater")
            player.addAttachment(plugin, "titlemanager.update.notify", true)
            generateSequence { player.nextMessage() }.toList()
            server.pluginManager.callEvent(PlayerJoinEvent(player, Component.empty()))

            assertEventually {
                val messages = generateSequence { player.nextMessage() }.toList()
                assertTrue(messages.any { it.contains("3.0.0") }, "messages=$messages")
            }
        } finally {
            TitleManagerPlugin.updateClientFactory = originalUpdateClientFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `player-list update interval throttles animated configuration output`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("player-list.yml").writeText(
                """
                    enabled: true
                    updateIntervalMilliseconds: 500
                    header: '${'$'}{marquee:[0;1;0][2]ABC}'
                    footer: "Static Footer"
                    worlds: {}
                """.trimIndent()
            )
            server.pluginManager.enablePlugin(plugin)
            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)

            awaitNextPlayerList(player, expectedHeader = "AB", expectedFooter = "Static Footer")
            assertNoPlayerListWithin(player, timeoutMilliseconds = 150)
        } finally {
            MockBukkit.unmock()
        }
    }
}
