package dev.tarkan.titlemanager.nms.direct.v1_20_R2

import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeSidebar
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeThreadingPolicy
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModuleFactory
import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsStatus
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
import net.minecraft.network.protocol.game.ClientboundTabListPacket
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.bukkit.Server
import org.bukkit.entity.Player
import java.lang.reflect.Proxy
import java.time.Duration
import java.util.ServiceLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class V1_20_R2RuntimeVersionModuleFactoryTest {
    @Test
    fun `service-loaded factory does not require NMS classes during discovery`() {
        val factories = ServiceLoader.load(RuntimeVersionModuleFactory::class.java)
            .toList()

        assertTrue(factories.isNotEmpty())
        assertEquals(
            listOf("nms-v1_20_R2"),
            factories.map { it.id }
        )
    }

    @Test
    fun `factory public surface does not reference server internals`() {
        val publicTypes = buildList {
            V1_20_R2RuntimeVersionModuleFactory::class.java.declaredFields.mapTo(this) { it.type }
            V1_20_R2RuntimeVersionModuleFactory::class.java.declaredConstructors.flatMapTo(this) { constructor ->
                constructor.parameterTypes.toList()
            }
            V1_20_R2RuntimeVersionModuleFactory::class.java.declaredMethods.flatMapTo(this) { method ->
                method.parameterTypes.toList() + method.returnType
            }
        }

        assertTrue(publicTypes.none { it.name.startsWith("net.minecraft.") })
        assertTrue(publicTypes.none { it.name.startsWith("com.mojang.") })
        assertTrue(publicTypes.none { it.name.startsWith("org.bukkit.craftbukkit.") })
    }

    @Test
    fun `matches only the exact v1_20_R2 runtime key`() {
        val factory = V1_20_R2RuntimeVersionModuleFactory()

        assertEquals(true, factory.isCompatible(runtimeVersion("1.20.2", "v1_20_R2")))
        assertEquals(false, factory.isCompatible(runtimeVersion("1.20.5", "v1_20_R4")))
        assertEquals(false, factory.isCompatible(RuntimeServerVersion("test", "1.20.2", null, null)))
    }

    @Test
    fun `uses direct packets for all supported visual features`() {
        val serverVersion = runtimeVersion("1.20.2", "v1_20_R2")
        val module = V1_20_R2RuntimeVersionModuleFactory().create(fakeServer(), serverVersion)

        assertEquals("nms-v1_20_R2", module.id)
        assertEquals("nms-v1_20_R2 with direct titles/actionbar/player-list/sidebar (1.20.2 / v1_20_R2)", module.displayName)
        assertEquals(RuntimeCapabilityStatus.AVAILABLE, module.capabilities.single { it.name == RuntimeCapability.TITLES }.status)
        assertEquals(
            "direct v1_20_R2 title, subtitle, and timing packets",
            module.capabilities.single { it.name == RuntimeCapability.TITLES }.detail
        )
        assertEquals(RuntimeCapabilityStatus.AVAILABLE, module.capabilities.single { it.name == RuntimeCapability.ACTIONBAR }.status)
        assertEquals(
            "direct v1_20_R2 ClientboundSetActionBarTextPacket",
            module.capabilities.single { it.name == RuntimeCapability.ACTIONBAR }.detail
        )
        assertEquals(RuntimeCapabilityStatus.AVAILABLE, module.capabilities.single { it.name == RuntimeCapability.PLAYER_LIST }.status)
        assertEquals(
            "direct v1_20_R2 ClientboundTabListPacket",
            module.capabilities.single { it.name == RuntimeCapability.PLAYER_LIST }.detail
        )
        assertEquals(RuntimeCapabilityStatus.AVAILABLE, module.capabilities.single { it.name == RuntimeCapability.SIDEBAR }.status)
        assertEquals(
            "direct v1_20_R2 scoreboard packets",
            module.capabilities.single { it.name == RuntimeCapability.SIDEBAR }.detail
        )
        assertEquals(RuntimeCapabilityStatus.AVAILABLE, module.capabilities.single { it.name == RuntimeCapability.DIRECT_NMS }.status)
        assertEquals(
            "direct=[titles, actionbar, player-list, sidebar]",
            module.capabilities.single { it.name == RuntimeCapability.DIRECT_NMS }.detail
        )
    }

    @Test
    fun `direct packet methods fall back when player is not CraftPlayer`() {
        val delegate = RecordingRuntimeVersionModule()
        val module = V1_20_R2RuntimeVersionModule(delegate, runtimeVersion("1.20.2", "v1_20_R2"))
        val player = fakePlayer()
        val times = Title.Times.times(Duration.ZERO, Duration.ofSeconds(1), Duration.ZERO)
        val title = Title.title(Component.text("Title"), Component.text("Subtitle"), times)

        module.sendTitleTimes(player, times)
        module.sendTitle(player, Component.text("Title"))
        module.sendSubtitle(player, Component.text("Subtitle"))
        module.showTitle(player, title)
        module.sendActionBar(player, Component.text("Actionbar"))
        module.sendPlayerListHeaderAndFooter(player, Component.text("Header"), Component.text("Footer"))
        module.createSidebar(player)

        assertEquals(
            listOf("times", "title", "subtitle", "show-title", "actionbar", "player-list", "sidebar"),
            delegate.calls
        )
    }

    @Test
    fun `direct packet sink sends title actionbar and player-list packets`() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        val packets = mutableListOf<Packet<*>>()
        val packetSink = V1_20_R2RuntimePacketSink(packets::add)
        val times = Title.Times.times(
            Duration.ofMillis(50),
            Duration.ofMillis(100),
            Duration.ofMillis(150)
        )

        packetSink.sendTitleTimes(times)
        packetSink.sendTitle(Component.text("Title"))
        packetSink.sendSubtitle(Component.text("Subtitle"))
        packetSink.showTitle(Title.title(Component.text("Combined Title"), Component.text("Combined Subtitle"), times))
        packetSink.sendActionBar(Component.text("Actionbar"))
        packetSink.sendPlayerListHeaderAndFooter(Component.text("Header"), Component.text("Footer"))

        assertEquals(
            listOf(
                ClientboundSetTitlesAnimationPacket::class,
                ClientboundSetTitleTextPacket::class,
                ClientboundSetSubtitleTextPacket::class,
                ClientboundSetTitlesAnimationPacket::class,
                ClientboundSetTitleTextPacket::class,
                ClientboundSetSubtitleTextPacket::class,
                ClientboundSetActionBarTextPacket::class,
                ClientboundTabListPacket::class
            ),
            packets.map { it::class }
        )
        assertEquals(
            listOf(Triple(1, 2, 3), Triple(1, 2, 3)),
            packets.filterIsInstance<ClientboundSetTitlesAnimationPacket>()
                .map { Triple(it.fadeIn, it.stay, it.fadeOut) }
        )
    }

    @Test
    fun `direct sidebar sends objective team score and cleanup packets`() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        val packets = mutableListOf<Packet<*>>()
        val player = fakePlayer()
        val sidebar = V1_20_R2RuntimeSidebar(player, packets::add, initialTitle = "Initial")

        assertEquals(true, sidebar.isAppliedTo(player))
        assertEquals(
            listOf(ClientboundSetObjectivePacket::class, ClientboundSetDisplayObjectivePacket::class),
            packets.map { it::class }
        )

        sidebar.title = "Updated"
        sidebar.set(1, "First")
        sidebar.set(1, "First")
        sidebar.set(1, "Second")
        sidebar.set(2, "Other")
        assertEquals("Second", sidebar.get(1))
        assertEquals("Other", sidebar.get(2))

        sidebar.remove(1)
        sidebar.close()

        assertEquals(null, sidebar.get(1))
        assertEquals(null, sidebar.get(2))
        assertEquals(false, sidebar.isAppliedTo(player))
        assertEquals(
            listOf(
                ClientboundSetObjectivePacket::class,
                ClientboundSetDisplayObjectivePacket::class,
                ClientboundSetObjectivePacket::class,
                ClientboundSetPlayerTeamPacket::class,
                ClientboundSetScorePacket::class,
                ClientboundSetPlayerTeamPacket::class,
                ClientboundSetPlayerTeamPacket::class,
                ClientboundSetScorePacket::class,
                ClientboundSetScorePacket::class,
                ClientboundSetPlayerTeamPacket::class,
                ClientboundSetScorePacket::class,
                ClientboundSetPlayerTeamPacket::class,
                ClientboundSetObjectivePacket::class
            ),
            packets.map { it::class }
        )
    }

    private fun runtimeVersion(minecraftVersion: String, nmsVersion: String): RuntimeServerVersion {
        return RuntimeServerVersion(
            bukkitVersion = "git-Paper-test (MC: $minecraftVersion)",
            minecraftVersion = minecraftVersion,
            craftBukkitPackage = "org.bukkit.craftbukkit.$nmsVersion",
            nmsVersion = nmsVersion
        )
    }

    private fun fakeServer(): Server {
        return Proxy.newProxyInstance(
            Server::class.java.classLoader,
            arrayOf(Server::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "toString" -> "FakeServer"
                "hashCode" -> 0
                "equals" -> false
                else -> error("Unexpected server call during module construction: ${method.name}")
            }
        } as Server
    }

    private fun fakePlayer(): Player {
        return Proxy.newProxyInstance(
            Player::class.java.classLoader,
            arrayOf(Player::class.java)
        ) { proxy, method, args ->
            when (method.name) {
                "toString" -> "FakePlayer"
                "hashCode" -> 0
                "equals" -> proxy === args?.singleOrNull()
                else -> error("Unexpected player call during direct NMS fallback test: ${method.name}")
            }
        } as Player
    }

    private class RecordingRuntimeVersionModule : RuntimeVersionModule {
        val calls = mutableListOf<String>()
        override val id = "recording"
        override val displayName = "recording"
        override val capabilities = listOf(
            DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.AVAILABLE, "recording"),
            DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.AVAILABLE, "recording"),
            DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.AVAILABLE, "recording"),
            DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.AVAILABLE, "recording"),
            DiagnosticsStatus(RuntimeCapability.DIRECT_NMS, RuntimeCapabilityStatus.UNAVAILABLE, "recording")
        )
        override val threadingPolicy = RuntimeThreadingPolicy.mainThreadOnly()

        override fun sendTitleTimes(player: Player, times: Title.Times) {
            calls.add("times")
        }

        override fun sendTitle(player: Player, title: Component) {
            calls.add("title")
        }

        override fun sendSubtitle(player: Player, subtitle: Component) {
            calls.add("subtitle")
        }

        override fun showTitle(player: Player, title: Title) {
            calls.add("show-title")
        }

        override fun sendActionBar(player: Player, actionBar: Component) {
            calls.add("actionbar")
        }

        override fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component) {
            calls.add("player-list")
        }

        override fun createSidebar(player: Player): RuntimeSidebar {
            calls.add("sidebar")
            return RecordingSidebar
        }
    }

    private object RecordingSidebar : RuntimeSidebar {
        override var title = ""
        override fun isAppliedTo(player: Player): Boolean = true
        override fun get(index: Int): String? = null
        override fun set(index: Int, value: String) = Unit
        override fun remove(index: Int) = Unit
        override fun close() = Unit
    }
}
