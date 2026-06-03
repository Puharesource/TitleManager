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

abstract class TitleManagerPluginMockBukkitTestSupport {

    protected fun writeInvalidAdvancedConfig(path: Path) {
        path.writeText(
            """
                configVersion: []
                threadPoolSize: 4
                usingConfig: true
                usingBungeeCord: false
                checkForUpdates: false
                databaseConnectionString: ""
            """.trimIndent()
        )
    }

    protected fun announcerConfig(title: String, actionbar: String): String {
        return """
            enabled: true
            announcements:
              test:
                interval: 1
                timings:
                  fadeIn: 1
                  stay: 2
                  fadeOut: 3
                titles:
                  - "$title"
                actionbar:
                  - "$actionbar"
        """.trimIndent()
    }

    protected fun awaitNextTitle(player: CapturingPlayerMock, expectedTitle: String? = null, expectedSubtitle: String? = null): CapturedTitle {
        var title: CapturedTitle? = null

        assertEventually {
            title = generateSequence { player.titles.poll() }
                .firstOrNull {
                    (expectedTitle == null || it.title == expectedTitle) &&
                        (expectedSubtitle == null || it.subtitle == expectedSubtitle)
                }
            assertNotNull(title)
        }

        return requireNotNull(title)
    }

    protected fun awaitNextPlayerList(
        player: CapturingPlayerMock,
        expectedHeader: String? = null,
        expectedFooter: String? = null
    ): CapturedPlayerList {
        var playerList: CapturedPlayerList? = null

        assertEventually {
            playerList = generateSequence { player.playerLists.poll() }
                .firstOrNull {
                    (expectedHeader == null || it.header == expectedHeader) &&
                        (expectedFooter == null || it.footer == expectedFooter)
                }
            assertNotNull(playerList)
        }

        return requireNotNull(playerList)
    }

    protected fun awaitNextActionBar(player: CapturingPlayerMock, expected: String? = null): String {
        var message: String? = null

        assertEventually(timeoutMilliseconds = 3_000) {
            message = generateSequence { player.actionBars.poll() }
                .firstOrNull { it.isNotEmpty() && (expected == null || it == expected) }
            assertNotNull(message)
        }

        return requireNotNull(message)
    }

    protected fun awaitNextActionBarRaw(player: CapturingPlayerMock, expected: String? = null): String {
        var message: String? = null

        assertEventually(timeoutMilliseconds = 3_000) {
            message = generateSequence { player.actionBars.poll() }
                .firstOrNull { expected == null || it == expected }
            assertNotNull(message)
        }

        return requireNotNull(message)
    }

    protected fun assertNoTitleWithin(player: CapturingPlayerMock, timeoutMilliseconds: Long = 250, unexpectedTitle: String? = null) {
        Thread.sleep(timeoutMilliseconds)

        val matchingTitle = generateSequence { player.titles.poll() }
            .firstOrNull { unexpectedTitle == null || it.title == unexpectedTitle }

        assertNull(matchingTitle)
    }

    protected fun assertNoActionBarWithin(player: CapturingPlayerMock, timeoutMilliseconds: Long = 250, unexpected: String? = null) {
        Thread.sleep(timeoutMilliseconds)

        val matchingMessage = generateSequence { player.actionBars.poll() }
            .firstOrNull { unexpected == null || it == unexpected }

        assertNull(matchingMessage)
    }

    protected fun assertNoPlayerListWithin(
        player: CapturingPlayerMock,
        timeoutMilliseconds: Long = 250,
        unexpectedHeader: String? = null,
        unexpectedFooter: String? = null
    ) {
        Thread.sleep(timeoutMilliseconds)

        val matchingMessage = generateSequence { player.playerLists.poll() }
            .firstOrNull {
                (unexpectedHeader == null || it.header == unexpectedHeader) &&
                    (unexpectedFooter == null || it.footer == unexpectedFooter)
            }

        assertNull(matchingMessage)
    }

    protected fun assertEventually(timeoutMilliseconds: Long = 2_000, assertion: () -> Unit) {
        val deadline = System.nanoTime() + timeoutMilliseconds * 1_000_000
        var lastFailure: AssertionError? = null

        while (System.nanoTime() < deadline) {
            try {
                performMockBukkitTick()
                assertion()
                return
            } catch (error: AssertionError) {
                lastFailure = error
                Thread.sleep(10)
            }
        }

        throw lastFailure ?: AssertionError("Condition was not met within ${timeoutMilliseconds}ms")
    }

    protected fun performMockBukkitTicks(count: Int) {
        repeat(count) {
            performMockBukkitTick()
        }
    }

    protected fun performMockBukkitTick() {
        MockBukkit.getMock()?.scheduler?.performOneTick()
    }

    protected fun pluginMessage(vararg values: Any): ByteArray {
        return ByteArrayOutputStream().use { byteStream ->
            DataOutputStream(byteStream).use { output ->
                values.forEach { value ->
                    when (value) {
                        is String -> output.writeUTF(value)
                        is Int -> output.writeInt(value)
                        else -> error("Unsupported plugin message value: $value")
                    }
                }
            }

            byteStream.toByteArray()
        }
    }

    protected data class CapturedTitle(val title: String, val subtitle: String)

    protected data class CapturedPlayerList(
        val header: String,
        val footer: String,
        val legacyHeader: String = header,
        val legacyFooter: String = footer
    )

    protected class CapturingPlayerMock(server: org.mockbukkit.mockbukkit.ServerMock, name: String, uuid: UUID = UUID.randomUUID()) : PlayerMock(server, name, uuid) {
        val titles: ConcurrentLinkedQueue<CapturedTitle> = ConcurrentLinkedQueue()
        val playerLists: ConcurrentLinkedQueue<CapturedPlayerList> = ConcurrentLinkedQueue()
        val actionBars: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()
        private val serializer = PlainTextComponentSerializer.plainText()
        private val legacySerializer = LegacyComponentSerializer.legacy('§')

        override fun showTitle(title: Title) {
            titles.add(CapturedTitle(
                title = serializer.serialize(title.title()),
                subtitle = serializer.serialize(title.subtitle())
            ))

        }

        override fun <T : Any> sendTitlePart(part: TitlePart<T>, value: T) {
            when (part) {
                TitlePart.TITLE -> titles.add(CapturedTitle(serializer.serialize(value as Component), ""))
                TitlePart.SUBTITLE -> titles.add(CapturedTitle("", serializer.serialize(value as Component)))
                else -> Unit
            }

            super.sendTitlePart(part, value)
        }

        override fun sendPlayerListHeaderAndFooter(header: Component, footer: Component) {
            playerLists.add(CapturedPlayerList(
                header = serializer.serialize(header),
                footer = serializer.serialize(footer),
                legacyHeader = legacySerializer.serialize(header),
                legacyFooter = legacySerializer.serialize(footer)
            ))

            super.sendPlayerListHeaderAndFooter(header, footer)
        }

        override fun sendActionBar(message: Component) {
            actionBars.add(serializer.serialize(message))

            super.sendActionBar(message)
        }
    }

    protected class TpsServerMock(private val tpsSamples: DoubleArray) : org.mockbukkit.mockbukkit.ServerMock() {
        override fun getTPS(): DoubleArray = tpsSamples
    }

    protected object UnsupportedSidebarModuleFactory : RuntimeVersionModuleFactory {
        override val id = "unsupported-sidebar-test"

        override fun isCompatible(serverVersion: RuntimeServerVersion): Boolean = true

        override fun create(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule = UnsupportedSidebarModule
    }

    protected object UnsupportedSidebarModule : RuntimeVersionModule {
        override val id = "unsupported-sidebar-test"
        override val displayName = id
        override val threadingPolicy = RuntimeThreadingPolicy.mainThreadOnly()
        override val capabilities = listOf(
            DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.AVAILABLE, "test module"),
            DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.AVAILABLE, "test module"),
            DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.AVAILABLE, "test module"),
            DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.UNAVAILABLE, "test module")
        )

        override fun sendTitleTimes(player: Player, times: Title.Times) = Unit

        override fun sendTitle(player: Player, title: Component) = Unit

        override fun sendSubtitle(player: Player, subtitle: Component) = Unit

        override fun showTitle(player: Player, title: Title) = Unit

        override fun sendActionBar(player: Player, actionBar: Component) = Unit

        override fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component) = Unit

        override fun createSidebar(player: Player): RuntimeSidebar = UnsupportedRuntimeSidebar
    }

    protected class ThreadRecordingRuntimeModuleFactory(
        private val module: ThreadRecordingRuntimeModule
    ) : RuntimeVersionModuleFactory {
        override val id = "thread-recording-test"

        override fun isCompatible(serverVersion: RuntimeServerVersion): Boolean = true

        override fun create(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule = module
    }

    protected class FixedRuntimeModuleFactory(
        override val id: String,
        private val createModule: (Server) -> RuntimeVersionModule
    ) : RuntimeVersionModuleFactory {
        override fun isCompatible(serverVersion: RuntimeServerVersion): Boolean = true

        override fun create(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule = createModule(server)
    }

    protected class CountingRuntimeModuleFactory : RuntimeVersionModuleFactory {
        var createCount = 0
            private set

        override val id = "counting-runtime-test"

        override fun isCompatible(serverVersion: RuntimeServerVersion): Boolean = true

        override fun create(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule {
            createCount++

            return ThreadRecordingRuntimeModule(displayName = "selected-runtime-$createCount")
        }
    }

    protected class ThreadRecordingRuntimeModule(
        override val threadingPolicy: RuntimeThreadingPolicy = RuntimeThreadingPolicy.mainThreadOnly(),
        override val displayName: String = "thread-recording-test",
        override val capabilities: List<DiagnosticsStatus> = runtimeCapabilities()
    ) : RuntimeVersionModule {
        val operations: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()
        val offMainThreadOperations: ConcurrentLinkedQueue<String> = ConcurrentLinkedQueue()

        override val id = "thread-recording-test"

        override fun sendTitleTimes(player: Player, times: Title.Times) {
            record("title-times")
        }

        override fun sendTitle(player: Player, title: Component) {
            record("title")
        }

        override fun sendSubtitle(player: Player, subtitle: Component) {
            record("subtitle")
        }

        override fun showTitle(player: Player, title: Title) {
            record("show-title")
        }

        override fun sendActionBar(player: Player, actionBar: Component) {
            record("actionbar")
        }

        override fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component) {
            record("player-list")
        }

        override fun createSidebar(player: Player): RuntimeSidebar {
            record("sidebar-create")

            return ThreadRecordingRuntimeSidebar(this, player)
        }

        override fun close() {
            record("module-close")
        }

        fun record(operation: String) {
            operations.add(operation)
            if (!Bukkit.isPrimaryThread()) {
                offMainThreadOperations.add(operation)
            }
        }

        companion object {
            fun runtimeCapabilities(
                titles: DiagnosticsStatus = DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                actionbar: DiagnosticsStatus = DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                playerList: DiagnosticsStatus = DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                sidebar: DiagnosticsStatus = DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.AVAILABLE, "test module"),
                directNms: DiagnosticsStatus = DiagnosticsStatus(RuntimeCapability.DIRECT_NMS, RuntimeCapabilityStatus.UNAVAILABLE, "test module")
            ): List<DiagnosticsStatus> = listOf(titles, actionbar, playerList, sidebar, directNms)
        }
    }

    protected class ThreadRecordingRuntimeSidebar(
        private val module: ThreadRecordingRuntimeModule,
        private val player: Player
    ) : RuntimeSidebar {
        private val lines = mutableMapOf<Int, String>()

        override var title: String = ""
            set(value) {
                field = value
                module.record("sidebar-title")
            }

        override fun isAppliedTo(player: Player): Boolean = player == this.player

        override fun get(index: Int): String? = lines[index]

        override fun set(index: Int, value: String) {
            lines[index] = value
            module.record("sidebar-line")
        }

        override fun remove(index: Int) {
            lines.remove(index)
            module.record("sidebar-remove")
        }

        override fun close() {
            module.record("sidebar-close")
        }
    }

    protected object UnsupportedRuntimeSidebar : RuntimeSidebar {
        override var title: String = ""

        override fun isAppliedTo(player: Player): Boolean = false

        override fun get(index: Int): String? = null

        override fun set(index: Int, value: String) = Unit

        override fun remove(index: Int) = Unit

        override fun close() = Unit
    }

    protected fun runtimeVersion(minecraftVersion: String, nmsVersion: String): RuntimeServerVersion {
        return RuntimeServerVersion(
            bukkitVersion = "git-Spigot-test (MC: $minecraftVersion)",
            minecraftVersion = minecraftVersion,
            craftBukkitPackage = "org.bukkit.craftbukkit.$nmsVersion",
            nmsVersion = nmsVersion
        )
    }

    companion object {
        const val LEGACY_CONFIG = """
            config-version: 7
            using-config: false
            using-bungeecord: true
            check-for-updates: false
            locale: "da-DK"
            player-list:
              enabled: true
              header:
              - ''
              - '${'$'}{shine:[0;2;0][&3;&b]Legacy Server}'
              - ''
              footer: '&7Online: &b%{online}'
            welcome-title:
              enabled: false
              title: "Legacy title"
              subtitle: "Legacy subtitle"
              fade-in: 5
              stay: 6
              fade-out: 7
              delay: 8
              first-join:
                title: "Legacy first title"
                subtitle: "Legacy first subtitle"
            welcome-actionbar:
              enabled: true
              title: "Legacy actionbar"
              delay: 3
              first-join: "First legacy actionbar"
            placeholders:
              number-format:
                enabled: false
                format: "0.00"
              date-format: "HH:mm:ss"
            scoreboard:
              enabled: true
              title: "Legacy Board"
              lines:
              - "&aLine 1"
              - "&bLine 2"
              disabled-worlds:
              - "legacy-disabled-world"
        """

        const val LEGACY_CONFIG_WITH_ANNOUNCER = """
            config-version: 7
            using-config: true
            using-bungeecord: false
            announcer:
              enabled: true
              announcements:
                legacy:
                  interval: 1
                  timings:
                    fade-in: 1
                    stay: 2
                    fade-out: 3
                  titles:
                  - "Legacy title\nLegacy subtitle"
                  actionbar:
                  - "Legacy actionbar"
        """
    }
}
