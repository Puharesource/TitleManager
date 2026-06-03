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

class TitleManagerPluginWelcomeTest : TitleManagerPluginMockBukkitTestSupport() {

    @Test
    fun `welcome title feature listener sends configured title on join and world change`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)
            val oldWorld = player.world
            val endWorld = server.addSimpleWorld("world_the_end")

            assertEquals("Welcome to My Server", awaitNextTitle(player).title)

            assertTrue(player.teleport(Location(endWorld, 0.0, 64.0, 0.0)))
            server.pluginManager.callEvent(PlayerChangedWorldEvent(player, oldWorld))

            assertEquals("Welcome to the End!", awaitNextTitle(player, expectedTitle = "Welcome to the End!").title)
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `welcome title and actionbar use first join config only for new players`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("welcome-title.yml").writeText(
                """
                    enabled: true
                    delayMilliseconds: 0
                    title: "Regular Title"
                    subtitle: "Regular Subtitle"
                    fadeIn: 0
                    stay: 1
                    fadeOut: 0
                    firstJoin:
                      enabled: true
                      delayMilliseconds: 0
                      title: "First Join Title"
                      subtitle: "First Join Subtitle"
                      fadeIn: 0
                      stay: 1
                      fadeOut: 0
                    worlds: {}
                """.trimIndent()
            )
            plugin.dataFolder.toPath().resolve("welcome-actionbar.yml").writeText(
                """
                    enabled: true
                    delayMilliseconds: 0
                    title: "Regular Actionbar"
                    firstJoin:
                      enabled: true
                      delayMilliseconds: 0
                      title: "First Join Actionbar"
                    worlds: {}
                """.trimIndent()
            )
            server.pluginManager.enablePlugin(plugin)

            val newPlayer = CapturingPlayerMock(server, "Alice", UUID.randomUUID())
            server.addPlayer(newPlayer)

            awaitNextTitle(newPlayer, expectedTitle = "First Join Title", expectedSubtitle = "First Join Subtitle").apply {
                assertEquals("First Join Title", title)
                assertEquals("First Join Subtitle", subtitle)
            }
            assertEquals("First Join Actionbar", awaitNextActionBar(newPlayer, expected = "First Join Actionbar"))

            val returningPlayerId = UUID.randomUUID()
            server.playerList.setFirstPlayed(returningPlayerId, 1L)
            val returningPlayer = CapturingPlayerMock(server, "Bob", returningPlayerId)
            server.addPlayer(returningPlayer)

            awaitNextTitle(returningPlayer, expectedTitle = "Regular Title", expectedSubtitle = "Regular Subtitle").apply {
                assertEquals("Regular Title", title)
                assertEquals("Regular Subtitle", subtitle)
            }
            assertEquals("Regular Actionbar", awaitNextActionBar(returningPlayer, expected = "Regular Actionbar"))
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `welcome title honors configured join delay`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("welcome-title.yml").writeText(
                """
                    enabled: true
                    delayMilliseconds: 2000
                    title: "Delayed Title"
                    subtitle: "Delayed Subtitle"
                    fadeIn: 0
                    stay: 1
                    fadeOut: 0
                    firstJoin:
                      enabled: true
                      delayMilliseconds: 2000
                      title: "Delayed First Title"
                      subtitle: "Delayed First Subtitle"
                      fadeIn: 0
                      stay: 1
                      fadeOut: 0
                    worlds: {}
                """.trimIndent()
            )
            plugin.dataFolder.toPath().resolve("welcome-actionbar.yml").writeText(
                """
                    enabled: false
                    delayMilliseconds: 0
                    title: ""
                    firstJoin:
                      enabled: false
                      delayMilliseconds: 0
                      title: ""
                    worlds: {}
                """.trimIndent()
            )
            server.pluginManager.enablePlugin(plugin)

            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)

            assertNoTitleWithin(player)
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `welcome actionbar feature listener sends configured message on join and world change`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)
            val oldWorld = player.world
            val endWorld = server.addSimpleWorld("world_the_end")

            assertEquals("Welcome to My Server", awaitNextActionBar(player))

            assertTrue(player.teleport(Location(endWorld, 0.0, 64.0, 0.0)))
            server.pluginManager.callEvent(PlayerChangedWorldEvent(player, oldWorld))

            assertEquals("Welcome to My Server", awaitNextActionBar(player))
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `disabled welcome title flag suppresses title on next join`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val uuid = UUID.randomUUID()
            val player = CapturingPlayerMock(server, "Alice", uuid)
            server.addPlayer(player)
            player.setOp(true)

            assertEquals("Welcome to My Server", awaitNextTitle(player).title)

            server.execute("titlemanager", player, "title", "toggle").apply {
                assertTrue(hasSucceeded())
            }

            player.disconnect()
            server.pluginManager.callEvent(PlayerConnectionCloseEvent(player.uniqueId, player.name, InetAddress.getLoopbackAddress(), false))

            val rejoinedPlayer = CapturingPlayerMock(server, "Alice", uuid)
            server.addPlayer(rejoinedPlayer)

            assertNoTitleWithin(rejoinedPlayer)
        } finally {
            MockBukkit.unmock()
        }
    }
}
