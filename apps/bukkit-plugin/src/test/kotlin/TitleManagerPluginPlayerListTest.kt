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

class TitleManagerPluginPlayerListTest : TitleManagerPluginMockBukkitTestSupport() {

    @Test
    fun `player list feature listener sends configured header and footer on join`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            val player = CapturingPlayerMock(server, "Alice")

            server.addPlayer(player)

            for (fileName in DefaultAnimationFiles.FILE_NAMES) {
                val animationFile = plugin.dataFolder.toPath().resolve(DefaultAnimationFiles.ANIMATIONS_DIRECTORY_NAME).resolve(fileName)

                assertTrue(animationFile.exists(), "$fileName should be installed with the default plugin data")
                assertTrue(animationFile.fileSize() > 0L, "$fileName should not be empty")
            }

            awaitNextPlayerList(player).apply {
                assertTrue(header.contains("My Server"))
                assertTrue(footer.contains("Online Players"))
                assertFalse(footer.contains("Unknown-Animation-Placeholder"))
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `player list renders legacy-safe player and world placeholder aliases`() {
        val server = MockBukkit.mock(TpsServerMock(doubleArrayOf(20.0, 20.0, 20.0)))
        mockkStatic(PlaceholderAPI::class)

        try {
            MockBukkit.createMockPlugin("PlaceholderAPI")
            every { PlaceholderAPI.setPlaceholders(any<Player>(), any<String>()) } answers {
                secondArg<String>().replace("%placeholder_test%", "PAPI")
            }
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("placeholder.yml").writeText(
                """
                    locale: "en-US"
                    numberFormat:
                      enabled: true
                      format: "#,###.##"
                    dateFormat: "EEE, dd MMM yyyy HH:mm:ss z"
                    aliases:
                      custom-alias: "Alias Value"
                """.trimIndent()
            )
            plugin.dataFolder.toPath().resolve("player-list.yml").writeText(
                """
                    enabled: true
                    header: "External:%placeholder_test%|%{custom-alias}|%{c:#ff0000}Red|%{player}|%{username}|%{world}|%{world-name}|%{online:lobby}|%{online-players:lobby}|%{safe-online}|%{safe-online-players}"
                    footer: "%{display-name}|%{nick}|%{stripped-displayname}|%{stripped-nick}|%{world-time}|%{24h-world-time}|%{12h-world-time}|%{ping}|%{balance}|%{money}|%{group}|%{group-name}|%{tps}|%{tps:5}|%{tps:15}|%{tps:2}|%{tps:short}|%{tps:full}|%{gradient:[#ff0000,#00ff00,bold]Go}"
                    worlds: {}
                """.trimIndent()
            )
            val economy = mockk<Economy>()
            val permissions = mockk<Permission>()
            every { economy.getBalance(any<Player>()) } returns 1234.5
            every { permissions.hasGroupSupport() } returns true
            every { permissions.getPrimaryGroup(any<Player>()) } returns "&aAdmin"
            server.servicesManager.register(Economy::class.java, economy, plugin, ServicePriority.Normal)
            server.servicesManager.register(Permission::class.java, permissions, plugin, ServicePriority.Normal)
            server.pluginManager.enablePlugin(plugin)
            val hiddenPlayer = CapturingPlayerMock(server, "Bob")
            server.addPlayer(hiddenPlayer)
            val player = CapturingPlayerMock(server, "Alice")
            player.displayName(Component.text("Fancy Alice"))
            player.world.time = 6000L
            server.addPlayer(player)
            player.hidePlayer(plugin, hiddenPlayer)
            GlobalContext.get().get<PlayerContextManager>().getContext(player).setConfigPlayerList()

            val expectedHeader = "External:PAPI|Alias Value|Red|Alice|Alice|world|world|2|2|1|1"
            awaitNextPlayerList(player, expectedHeader = expectedHeader).apply {
                assertEquals(expectedHeader, header)
                assertTrue(legacyHeader.contains("§#ff0000Red") || legacyHeader.contains("§x§f§f§0§0§0§0Red") || legacyHeader.contains("§cRed"))
                assertTrue(footer.startsWith("Fancy Alice|Fancy Alice|Fancy Alice|Fancy Alice|"), footer)
                assertTrue(footer.contains("|12:00|12:00 AM|0|1,234.5|1,234.5|Admin|Admin|"), footer)
                assertTrue(legacyFooter.contains("§aAdmin"))
                assertTrue(footer.contains("|20.0|20.0|20.0|20.0, 20.0, 20.0|20.0, 20.0, 20.0| TPS from last 1m, 5m, 15m: 20.0, 20.0, 20.0"))
                assertTrue(footer.endsWith("|Go"))
                assertTrue(legacyFooter.contains("§#ff0000§lG") || legacyFooter.contains("§x§f§f§0§0§0§0§lG") || legacyFooter.contains("§c§lG"))
            }
        } finally {
            unmockkStatic(PlaceholderAPI::class)
            MockBukkit.unmock()
        }
    }


    @Test
    fun `player list world change reapplies world overrides and clears disabled worlds`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("player-list.yml").writeText(
                """
                    enabled: true
                    header: "Overworld Header"
                    footer: "Overworld Footer"
                    worlds:
                      "world_the_end":
                        enabled: true
                        header: "End Header"
                        footer: "End Footer"
                      "world_nether":
                        enabled: false
                        header: "Nether Header"
                        footer: "Nether Footer"
                """.trimIndent()
            )
            server.pluginManager.enablePlugin(plugin)

            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)
            val overworld = player.world
            val endWorld = server.addSimpleWorld("world_the_end")
            val netherWorld = server.addSimpleWorld("world_nether")

            awaitNextPlayerList(player, expectedHeader = "Overworld Header", expectedFooter = "Overworld Footer")
            player.playerLists.clear()

            assertTrue(player.teleport(Location(endWorld, 0.0, 64.0, 0.0)))
            server.pluginManager.callEvent(PlayerChangedWorldEvent(player, overworld))

            awaitNextPlayerList(player, expectedHeader = "End Header", expectedFooter = "End Footer")
            player.playerLists.clear()

            assertTrue(player.teleport(Location(netherWorld, 0.0, 64.0, 0.0)))
            server.pluginManager.callEvent(PlayerChangedWorldEvent(player, endWorld))

            awaitNextPlayerList(player, expectedHeader = "", expectedFooter = "")
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `player-list skips duplicate animated configuration output when enabled`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("player-list.yml").writeText(
                """
                    enabled: true
                    updateIntervalMilliseconds: 1
                    header: '${'$'}{marquee:[0;1;0][2]AAA}'
                    footer: "Static Footer"
                    worlds: {}
                """.trimIndent()
            )
            server.pluginManager.enablePlugin(plugin)
            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)

            awaitNextPlayerList(player, expectedHeader = "AA", expectedFooter = "Static Footer")
            assertNoPlayerListWithin(player, timeoutMilliseconds = 150)
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `player-list sends duplicate animated configuration output when disabled`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("advanced.yml").writeText(
                """
                    threadPoolSize: 4
                    preventDuplicatePackets: false
                    databaseConnectionString: "username"
                """.trimIndent()
            )
            plugin.dataFolder.toPath().resolve("player-list.yml").writeText(
                """
                    enabled: true
                    updateIntervalMilliseconds: 1
                    header: '${'$'}{marquee:[0;1;0][2]AAA}'
                    footer: "Static Footer"
                    worlds: {}
                """.trimIndent()
            )
            server.pluginManager.enablePlugin(plugin)
            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)

            awaitNextPlayerList(player, expectedHeader = "AA", expectedFooter = "Static Footer")
            awaitNextPlayerList(player, expectedHeader = "AA", expectedFooter = "Static Footer")
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `player list toggle clears active header and footer`() {
        val server = MockBukkit.mock()

        try {
            val plugin = MockBukkit.load(TitleManagerPlugin::class.java)
            val player = CapturingPlayerMock(server, "Alice")
            server.addPlayer(player)
            player.setOp(true)

            awaitNextPlayerList(player)
            player.playerLists.clear()

            server.execute("titlemanager", player, "playerlist", "toggle").apply {
                assertTrue(hasSucceeded())
            }

            awaitNextPlayerList(player).apply {
                assertEquals("", header)
                assertEquals("", footer)
            }
        } finally {
            MockBukkit.unmock()
        }
    }
}
