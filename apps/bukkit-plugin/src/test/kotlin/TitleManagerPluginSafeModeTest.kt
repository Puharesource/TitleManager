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

class TitleManagerPluginSafeModeTest : TitleManagerPluginMockBukkitTestSupport() {

    @Test
    fun `scripts command reports legacy scripting is unsupported`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)

            server.executeConsole("titlemanager", "scripts").apply {
                assertTrue(hasSucceeded())
                assertTrue(sender.nextMessage()!!.contains("Legacy TitleManager scripts are not supported in TitleManager Next because arbitrary script execution is disabled."))
                assertNull(sender.nextMessage())
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin enters safe mode when legacy script files are present`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val scriptsDirectory = plugin.dataFolder.toPath().resolve("scripts")
            scriptsDirectory.createDirectories()
            scriptsDirectory.resolve("legacy-script.js").writeText("print('legacy')")

            server.pluginManager.enablePlugin(plugin)

            assertTrue(plugin.isEnabled)
            assertNull(server.servicesManager.load(TitleManagerApi::class.java))

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                assertEquals("TitleManager diagnostics", sender.nextMessage())
                assertEquals("Mode: safe-mode", sender.nextMessage())
                repeat(10) {
                    sender.nextMessage()
                }
                assertEquals("Validation errors:", sender.nextMessage())
                assertTrue(sender.nextMessage()!!.contains("Legacy script files in the scripts folder are not supported"))
                assertNull(sender.nextMessage())
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `safe mode reload recovers after legacy script files are removed`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val scriptFile = plugin.dataFolder.toPath().resolve("scripts").also { it.createDirectories() }.resolve("legacy-script.js")
            scriptFile.writeText("print('legacy')")

            server.pluginManager.enablePlugin(plugin)
            assertTrue(plugin.isEnabled)
            assertNull(server.servicesManager.load(TitleManagerApi::class.java))

            scriptFile.deleteExisting()

            server.executeConsole("titlemanager", "reload").apply {
                assertTrue(hasSucceeded())
                assertEquals("Reloading TitleManager from safe mode...", sender.nextMessage())
                assertEquals("Reload finished. TitleManager is now running normally.", sender.nextMessage())
                assertNull(sender.nextMessage())
            }

            assertNotNull(server.servicesManager.load(TitleManagerApi::class.java))
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin enters safe mode during Bukkit startup when configuration is invalid`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val pluginVersion = plugin.pluginVersion
            writeInvalidAdvancedConfig(plugin.dataFolder.toPath().resolve("advanced.yml"))

            server.pluginManager.enablePlugin(plugin)

            assertTrue(plugin.isEnabled)
            assertNotNull(server.getPluginCommand("titlemanager"))
            assertNull(server.servicesManager.load(TitleManagerApi::class.java))

            server.executeConsole("titlemanager").apply {
                assertTrue(hasSucceeded())
                assertEquals("TitleManager is running in safe mode because configuration failed to load.", sender.nextMessage())
                assertTrue(sender.nextMessage()!!.contains("Failed to load configuration file 'advanced.yml'"))
                assertEquals("Available safe-mode commands: /tm version, /tm diagnostics, /tm reload", sender.nextMessage())
                assertNull(sender.nextMessage())
            }

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                assertEquals("TitleManager diagnostics", sender.nextMessage())
                assertEquals("Mode: safe-mode", sender.nextMessage())
                assertEquals("Plugin version: $pluginVersion", sender.nextMessage())
                assertTrue(sender.nextMessage()!!.startsWith("Server: "))
                assertTrue(sender.nextMessage()!!.startsWith("Bukkit API:"))
                assertEquals("Version module: unavailable (runtime not started)", sender.nextMessage())
                assertEquals("Version module threading: inactive", sender.nextMessage())
                assertEquals("Scheduler: inactive (safe mode)", sender.nextMessage())
                assertEquals("Loaded animation files: 0", sender.nextMessage())
                assertEquals("Registered animation placeholders: 0", sender.nextMessage())
                assertTrue(sender.nextMessage()!!.contains("runtime-features=unavailable"))
                assertTrue(sender.nextMessage()!!.contains("PlaceholderAPI=inactive"))
                assertEquals("Validation errors:", sender.nextMessage())
                assertTrue(sender.nextMessage()!!.contains("Failed to load configuration file 'advanced.yml'"))
                assertNull(sender.nextMessage())
            }

            server.executeConsole("titlemanager", "version").apply {
                assertTrue(hasSucceeded())
                assertEquals("TitleManager v$pluginVersion is running in safe mode.", sender.nextMessage())
                assertNull(sender.nextMessage())
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin enters safe mode when legacy Spigot fallback cannot support enabled player-list`() {
        val server = MockBukkit.mock()
        val originalSelectorFactory = TitleManagerPlugin.versionModuleSelectorFactory

        try {
            TitleManagerPlugin.versionModuleSelectorFactory = {
                VersionModuleSelector(
                    listOf(
                        FixedRuntimeModuleFactory("legacy-spigot-api-test") { bukkitServer ->
                            LegacySpigotRuntimeAdapter(bukkitServer, runtimeVersion("1.12.2", "v1_12_R1"))
                        }
                    )
                )
            }
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin

            server.pluginManager.enablePlugin(plugin)

            assertTrue(plugin.isEnabled)
            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                val messages = generateSequence { sender.nextMessage() }.toList()
                assertTrue(messages.contains("Mode: safe-mode"))
                assertTrue(messages.any { it.contains("Version module: legacy-spigot-api") })
                assertTrue(messages.any { it.contains("player-list=unavailable (player-list header/footer requires Bukkit API 1.17+ or a direct NMS module)") })
                assertTrue(messages.any { it.contains("player-list headers and footers requires capability 'player-list'") })
                assertTrue(messages.any { it.contains("player-list header/footer requires Bukkit API 1.17+ or a direct NMS module") })
                assertTrue(messages.none { it.contains("welcome titles requires capability 'titles'") })
                assertTrue(messages.none { it.contains("welcome actionbars requires capability 'actionbar'") })
                assertTrue(messages.none { it.contains("scoreboards requires capability 'sidebar'") })
            }
        } finally {
            TitleManagerPlugin.versionModuleSelectorFactory = originalSelectorFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin enters safe mode when title-only legacy Spigot fallback cannot support enabled actionbar and player-list`() {
        val server = MockBukkit.mock()
        val originalSelectorFactory = TitleManagerPlugin.versionModuleSelectorFactory

        try {
            TitleManagerPlugin.versionModuleSelectorFactory = {
                VersionModuleSelector(
                    listOf(
                        FixedRuntimeModuleFactory("legacy-spigot-title-api-test") { bukkitServer ->
                            LegacySpigotTitleOnlyRuntimeAdapter(bukkitServer, runtimeVersion("1.8.8", "v1_8_R3"))
                        }
                    )
                )
            }
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin

            server.pluginManager.enablePlugin(plugin)

            assertTrue(plugin.isEnabled)
            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                val messages = generateSequence { sender.nextMessage() }.toList()
                assertTrue(messages.contains("Mode: safe-mode"))
                assertTrue(messages.any { it.contains("Version module: legacy-spigot-title-api") })
                assertTrue(messages.any { it.contains("actionbar=unavailable (actionbar requires Spigot 1.12+ or a direct NMS module)") })
                assertTrue(messages.any { it.contains("welcome actionbars requires capability 'actionbar'") })
                assertTrue(messages.any { it.contains("actionbar requires Spigot 1.12+ or a direct NMS module") })
                assertTrue(messages.any { it.contains("player-list headers and footers requires capability 'player-list'") })
                assertTrue(messages.none { it.contains("welcome titles requires capability 'titles'") })
                assertTrue(messages.none { it.contains("scoreboards requires capability 'sidebar'") })
            }
        } finally {
            TitleManagerPlugin.versionModuleSelectorFactory = originalSelectorFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `safe mode reload recovers full runtime after configuration is fixed`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val pluginVersion = plugin.pluginVersion
            val advancedConfigFile = plugin.dataFolder.toPath().resolve("advanced.yml")
            writeInvalidAdvancedConfig(advancedConfigFile)

            server.pluginManager.enablePlugin(plugin)
            assertTrue(plugin.isEnabled)

            server.executeConsole("titlemanager").apply {
                assertTrue(hasSucceeded())
                assertEquals("TitleManager is running in safe mode because configuration failed to load.", sender.nextMessage())
                assertTrue(sender.nextMessage()!!.contains("Failed to load configuration file 'advanced.yml'"))
                assertEquals("Available safe-mode commands: /tm version, /tm diagnostics, /tm reload", sender.nextMessage())
                assertNull(sender.nextMessage())
            }

            val defaultAdvancedConfig = requireNotNull(plugin.getResource("DefaultConfigs/advanced.yml")) {
                "Missing bundled advanced.yml default config"
            }.reader().use { it.readText() }
            advancedConfigFile.writeText(defaultAdvancedConfig)

            server.executeConsole("titlemanager", "reload").apply {
                assertTrue(hasSucceeded())
                assertEquals("Reloading TitleManager from safe mode...", sender.nextMessage())
                assertEquals("Reload finished. TitleManager is now running normally.", sender.nextMessage())
                assertNull(sender.nextMessage())
            }

            server.executeConsole("titlemanager", "version").apply {
                assertTrue(hasSucceeded())
                val versionMessage = sender.nextMessage()!!
                assertTrue(versionMessage.contains("Running TitleManager"))
                assertTrue(versionMessage.contains("v$pluginVersion"))
                assertNull(sender.nextMessage())
            }

            val player = server.addPlayer("Recovered")
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
    fun `safe mode reload failure keeps plugin in safe mode`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val pluginVersion = plugin.pluginVersion
            writeInvalidAdvancedConfig(plugin.dataFolder.toPath().resolve("advanced.yml"))

            server.pluginManager.enablePlugin(plugin)
            assertTrue(plugin.isEnabled)

            server.executeConsole("titlemanager", "reload").apply {
                assertTrue(hasSucceeded())
                assertEquals("Reloading TitleManager from safe mode...", sender.nextMessage())
                assertEquals("Reload failed. TitleManager is still running in safe mode.", sender.nextMessage())
                assertTrue(sender.nextMessage()!!.contains("Failed to load configuration file 'advanced.yml'"))
                assertNull(sender.nextMessage())
            }

            server.executeConsole("titlemanager", "version").apply {
                assertTrue(hasSucceeded())
                assertEquals("TitleManager v$pluginVersion is running in safe mode.", sender.nextMessage())
                assertNull(sender.nextMessage())
            }
        } finally {
            MockBukkit.unmock()
        }
    }
}
