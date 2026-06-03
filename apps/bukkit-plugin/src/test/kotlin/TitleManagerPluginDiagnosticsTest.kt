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

class TitleManagerPluginDiagnosticsTest : TitleManagerPluginMockBukkitTestSupport() {

    @Test
    fun `runtime module operations run on the declared main-thread boundary`() {
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
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)

            module.operations.clear()
            module.offMainThreadOperations.clear()

            playerContext.sendTitle("Threaded Title")
            api.sendActionbar(player, "Threaded Actionbar")
            api.setPlayerListHeaderAndFooter(player, "Threaded Header", "Threaded Footer")
            api.setSidebar(player, "Threaded Sidebar", listOf("Threaded Line"))
            playerContext.setScoreboardValue(1, "Manual Threaded Line")

            assertEventually {
                assertTrue(module.operations.contains("title"), "operations=${module.operations}")
                assertTrue(module.operations.contains("actionbar"), "operations=${module.operations}")
                assertTrue(module.operations.contains("player-list"), "operations=${module.operations}")
                assertTrue(module.operations.contains("sidebar-create"), "operations=${module.operations}")
                assertTrue(module.operations.contains("sidebar-line"), "operations=${module.operations}")
            }
            assertTrue(module.offMainThreadOperations.isEmpty(), "Runtime operations ran off the main thread: ${module.offMainThreadOperations}")
        } finally {
            TitleManagerPlugin.versionModuleSelectorFactory = originalSelectorFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `thread-safe runtime module operations may stay on the async playback thread`() {
        val server = MockBukkit.mock()
        val originalSelectorFactory = TitleManagerPlugin.versionModuleSelectorFactory
        val module = ThreadRecordingRuntimeModule(
            threadingPolicy = RuntimeThreadingPolicy(
                title = RuntimeThreadingMode.THREAD_SAFE,
                actionbar = RuntimeThreadingMode.THREAD_SAFE,
                playerList = RuntimeThreadingMode.THREAD_SAFE,
                sidebar = RuntimeThreadingMode.MAIN_THREAD
            )
        )

        try {
            TitleManagerPlugin.versionModuleSelectorFactory = {
                VersionModuleSelector(listOf(ThreadRecordingRuntimeModuleFactory(module)))
            }

            MockBukkit.load(TitleManagerPlugin::class.java)
            val player = server.addPlayer("Alice")
            val api = requireNotNull(server.servicesManager.load(TitleManagerApi::class.java))
            val playerContext = GlobalContext.get().get<PlayerContextManager>().getContext(player)

            module.operations.clear()
            module.offMainThreadOperations.clear()

            playerContext.sendTitle("Thread-Safe Title")
            api.sendActionbar(player, "Thread-Safe Actionbar")
            api.setPlayerListHeaderAndFooter(player, "Thread-Safe Header", "Thread-Safe Footer")

            assertEventually {
                assertTrue(module.offMainThreadOperations.contains("title"), "offMain=${module.offMainThreadOperations}")
                assertTrue(module.offMainThreadOperations.contains("actionbar"), "offMain=${module.offMainThreadOperations}")
                assertTrue(module.offMainThreadOperations.contains("player-list"), "offMain=${module.offMainThreadOperations}")
            }
        } finally {
            TitleManagerPlugin.versionModuleSelectorFactory = originalSelectorFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `diagnostics command reports runtime capabilities through Bukkit startup`() {
        val server = MockBukkit.mock()

        try {
            MockBukkit.load(TitleManagerPlugin::class.java)

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                assertEquals("TitleManager diagnostics", sender.nextMessage())
                assertEquals("Mode: normal", sender.nextMessage())
                assertEquals("Plugin version: 3.0.0-SNAPSHOT", sender.nextMessage())
                assertTrue(sender.nextMessage()!!.startsWith("Server: "))
                assertTrue(sender.nextMessage()!!.startsWith("Bukkit API:"))
                assertTrue(sender.nextMessage()!!.startsWith("Version module: bukkit-api"))
                assertEquals("Version module threading: title=main-thread, actionbar=main-thread, player-list=main-thread, sidebar=main-thread", sender.nextMessage())
                assertEquals("Scheduler: Bukkit main-thread executor + virtual-thread async dispatcher", sender.nextMessage())
                assertEquals("Loaded animation files: 2", sender.nextMessage())
                assertTrue(sender.nextMessage()!!.startsWith("Registered animation placeholders: "))
                assertTrue(sender.nextMessage()!!.contains("direct-nms=unavailable"))
                sender.nextMessage()!!.let { integrations ->
                    assertTrue(integrations.contains("BungeeCord=inactive"))
                    assertTrue(integrations.contains("Announcer=inactive"))
                    assertTrue(integrations.contains("PlaceholderAPI=inactive"))
                    assertTrue(integrations.contains("Vault=inactive"))
                }
                assertEquals("Validation errors: none", sender.nextMessage())
                assertNull(sender.nextMessage())
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `diagnostics command reports selected direct runtime module`() {
        val server = MockBukkit.mock()
        val originalSelectorFactory = TitleManagerPlugin.versionModuleSelectorFactory

        try {
            val module = ThreadRecordingRuntimeModule(capabilities = listOf(
                DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                DiagnosticsStatus(
                    RuntimeCapability.DIRECT_NMS,
                    RuntimeCapabilityStatus.AVAILABLE,
                    "direct=[titles, actionbar, player-list, sidebar]"
                )
            ))
            TitleManagerPlugin.versionModuleSelectorFactory = {
                VersionModuleSelector(listOf(ThreadRecordingRuntimeModuleFactory(module)))
            }

            MockBukkit.load(TitleManagerPlugin::class.java)

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                assertEquals("TitleManager diagnostics", sender.nextMessage())
                assertEquals("Mode: normal", sender.nextMessage())
                assertEquals("Plugin version: 3.0.0-SNAPSHOT", sender.nextMessage())
                assertTrue(sender.nextMessage()!!.startsWith("Server: "))
                assertTrue(sender.nextMessage()!!.startsWith("Bukkit API:"))
                assertEquals("Version module: thread-recording-test", sender.nextMessage())
                assertEquals("Version module threading: title=main-thread, actionbar=main-thread, player-list=main-thread, sidebar=main-thread", sender.nextMessage())
                assertEquals("Scheduler: Bukkit main-thread executor + virtual-thread async dispatcher", sender.nextMessage())
                assertEquals("Loaded animation files: 2", sender.nextMessage())
                assertTrue(sender.nextMessage()!!.startsWith("Registered animation placeholders: "))
                assertTrue(sender.nextMessage()!!.contains("direct-nms=available (direct=[titles, actionbar, player-list, sidebar])"))
                assertTrue(sender.nextMessage()!!.contains("PlaceholderAPI=inactive"))
                assertEquals("Validation errors: none", sender.nextMessage())
                assertNull(sender.nextMessage())
            }
        } finally {
            TitleManagerPlugin.versionModuleSelectorFactory = originalSelectorFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `startup diagnostics use the same runtime module selected for the active runtime`() {
        val server = MockBukkit.mock()
        val originalSelectorFactory = TitleManagerPlugin.versionModuleSelectorFactory
        val factory = CountingRuntimeModuleFactory()

        try {
            TitleManagerPlugin.versionModuleSelectorFactory = {
                VersionModuleSelector(listOf(factory))
            }

            MockBukkit.load(TitleManagerPlugin::class.java)

            assertEquals(1, factory.createCount)
            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                assertEquals("TitleManager diagnostics", sender.nextMessage())
                assertEquals("Mode: normal", sender.nextMessage())
                repeat(3) {
                    sender.nextMessage()
                }
                assertEquals("Version module: selected-runtime-1", sender.nextMessage())
            }
            assertEquals(1, factory.createCount)
        } finally {
            TitleManagerPlugin.versionModuleSelectorFactory = originalSelectorFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `diagnostics command reports loaded animation file count`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val animationsFolder = plugin.dataFolder.toPath().resolve("animations")
            animationsFolder.createDirectories()
            animationsFolder.resolve("welcome-cycle.txt").writeText("[1]\${text_write:Hi}")

            server.pluginManager.enablePlugin(plugin)

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                assertEquals("TitleManager diagnostics", sender.nextMessage())
                repeat(7) {
                    sender.nextMessage()
                }
                assertEquals("Loaded animation files: 3", sender.nextMessage())
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `diagnostics command reports enabled announcer status`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().apply {
                toFile().mkdirs()
                resolve("announcer.yml").writeText(announcerConfig("Diagnostic title", "Diagnostic actionbar"))
            }

            server.pluginManager.enablePlugin(plugin)

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                val messages = generateSequence { sender.nextMessage() }.toList()
                assertTrue(messages.any { it.contains("Announcer=active (1 active / 1 configured)") }, "messages=$messages")
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `diagnostics command reports debug configuration status`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("advanced.yml").writeText(
                """
                    threadPoolSize: 4
                    debug: true
                    checkForUpdates: false
                    preventDuplicatePackets: true
                    databaseConnectionString: "username"
                """.trimIndent()
            )

            server.pluginManager.enablePlugin(plugin)

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                val messages = generateSequence { sender.nextMessage() }.toList()
                assertTrue(messages.any { it.contains("Debug=active (advanced.yml)") }, "messages=$messages")
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `diagnostics command reports config feature toggle status`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("advanced.yml").writeText(
                """
                    threadPoolSize: 4
                    debug: false
                    usingConfig: false
                    usingBungeeCord: false
                    checkForUpdates: false
                    preventDuplicatePackets: true
                    databaseConnectionString: "username"
                """.trimIndent()
            )

            server.pluginManager.enablePlugin(plugin)

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                val messages = generateSequence { sender.nextMessage() }.toList()
                assertTrue(messages.any { it.contains("Config=inactive (advanced.yml)") }, "messages=$messages")
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `diagnostics command reports disabled CombatLogX hook status`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            plugin.dataFolder.toPath().resolve("hooks.yml").writeText(
                """
                    combatLogX: false
                """.trimIndent()
            )

            server.pluginManager.enablePlugin(plugin)

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                val messages = generateSequence { sender.nextMessage() }.toList()
                assertTrue(messages.any { it.contains("CombatLogX=inactive (disabled in hooks.yml)") }, "messages=$messages")
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin metadata soft-depends on configured external integrations`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val metadata = plugin.getResource("plugin.yml").use { resource ->
                requireNotNull(resource) { "plugin.yml resource missing" }
                YamlConfiguration.loadConfiguration(InputStreamReader(resource))
            }
            val softDepend = metadata.getStringList("softdepend")

            assertTrue(softDepend.contains("PlaceholderAPI"))
            assertTrue(softDepend.contains("Vault"))
            assertTrue(softDepend.contains("CombatLogX"))
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin registers bStats metrics service on startup`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin

            server.pluginManager.enablePlugin(plugin)

            assertEquals(MetricsService.BSTATS_PLUGIN_ID, 7318)
            assertEquals(false, GlobalContext.get().get<MetricsService>().enabled)
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `diagnostics command reports bStats metrics status`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin

            server.pluginManager.enablePlugin(plugin)

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                val messages = generateSequence { sender.nextMessage() }.toList()
                assertTrue(messages.any { it.contains("Metrics=inactive (bStats)") }, "messages=$messages")
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `diagnostics command reports Vault provider status`() {
        val server = MockBukkit.mock()

        try {
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin
            val economy = mockk<Economy>()
            val permissions = mockk<Permission>()
            every { permissions.hasGroupSupport() } returns true
            server.servicesManager.register(Economy::class.java, economy, plugin, ServicePriority.Normal)
            server.servicesManager.register(Permission::class.java, permissions, plugin, ServicePriority.Normal)

            server.pluginManager.enablePlugin(plugin)

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                val messages = generateSequence { sender.nextMessage() }.toList()
                assertTrue(messages.any { it.contains("Vault=active (economy=active, permissions=active/groups)") }, "messages=$messages")
            }
        } finally {
            MockBukkit.unmock()
        }
    }


    @Test
    fun `diagnostics command reports update checker status`() {
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

            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                val messages = generateSequence { sender.nextMessage() }.toList()
                assertTrue(messages.any { it.contains("Updates=update-available (latest=3.0.0)") }, "messages=$messages")
            }
        } finally {
            TitleManagerPlugin.updateClientFactory = originalUpdateClientFactory
            MockBukkit.unmock()
        }
    }


    @Test
    fun `plugin enters safe mode when selected runtime module cannot support enabled sidebar`() {
        val server = MockBukkit.mock()
        val originalSelectorFactory = TitleManagerPlugin.versionModuleSelectorFactory

        try {
            TitleManagerPlugin.versionModuleSelectorFactory = {
                VersionModuleSelector(listOf(UnsupportedSidebarModuleFactory))
            }
            val plugin = server.pluginManager.loadPlugin(TitleManagerPlugin::class.java, emptyArray()) as TitleManagerPlugin

            server.pluginManager.enablePlugin(plugin)

            assertTrue(plugin.isEnabled)
            server.executeConsole("titlemanager", "diagnostics").apply {
                assertTrue(hasSucceeded())
                val messages = generateSequence { sender.nextMessage() }.toList()
                assertTrue(messages.contains("TitleManager diagnostics"))
                assertTrue(messages.contains("Mode: safe-mode"))
                assertTrue(messages.contains("Version module: unsupported-sidebar-test"))
                assertTrue(messages.any { it.contains("sidebar=unavailable (test module)") })
                assertTrue(messages.contains("Validation errors:"))
                assertTrue(messages.any { it.contains("scoreboards requires capability 'sidebar'") })
            }
        } finally {
            TitleManagerPlugin.versionModuleSelectorFactory = originalSelectorFactory
            MockBukkit.unmock()
        }
    }
}
