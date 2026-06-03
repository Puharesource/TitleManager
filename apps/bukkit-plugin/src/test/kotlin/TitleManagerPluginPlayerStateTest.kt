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

class TitleManagerPluginPlayerStateTest : TitleManagerPluginMockBukkitTestSupport() {

    @Test
    fun `plugin reload keeps persisted player feature toggles`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            player.setOp(true)

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

            server.executeConsole("titlemanager", "reload").apply {
                assertTrue(hasSucceeded())
            }

            val playerStorage = GlobalContext.get().get<PlayerStorage>()
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)

            playerStorage.get(player.uniqueId).apply {
                assertFalse(isSidebarEnabled)
                assertFalse(isPlayerListEnabled)
                assertFalse(isWelcomeTitleEnabled)
                assertFalse(isWelcomeActionbarEnabled)
            }
            assertFalse(playerContext.hasScoreboard())
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `player quit removes context and connection close unloads cached storage`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            player.setOp(true)
            val playerStorage = GlobalContext.get().get<PlayerStorage>()
            val playerContextManager = GlobalContext.get().get<PlayerContextManager>()

            server.execute("titlemanager", player, "sidebar", "toggle").apply {
                assertTrue(hasSucceeded())
            }
            assertFalse(playerStorage.get(player.uniqueId).isSidebarEnabled)
            assertEquals(1, playerContextManager.activeContextCount)

            assertTrue(player.disconnect())
            assertEquals(0, playerContextManager.activeContextCount)

            server.pluginManager.callEvent(
                PlayerConnectionCloseEvent(
                    player.uniqueId,
                    player.name,
                    InetAddress.getLoopbackAddress(),
                    false
                )
            )

            assertFailsWith<IllegalArgumentException> {
                playerStorage.get(player.uniqueId)
            }

            val reloadedPlayerInfo = runBlocking {
                playerStorage.load(player.uniqueId)
            }

            assertFalse(reloadedPlayerInfo.isSidebarEnabled)
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin disable closes player contexts and storage`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            val playerStorage = GlobalContext.get().get<PlayerStorage>()
            val playerContextManager = GlobalContext.get().get<PlayerContextManager>()

            playerContextManager.getContext(player)
            assertEquals(1, playerContextManager.activeContextCount)

            server.pluginManager.disablePlugin(plugin)

            assertFalse(plugin.isEnabled)
            assertEquals(0, playerContextManager.activeContextCount)
            assertFailsWith<SQLException> {
                runBlocking {
                    playerStorage.load(UUID.randomUUID())
                }
            }
        } finally {
            MockBukkit.unmock()
        }
    }
}
