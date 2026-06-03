package dev.tarkan.titlemanager.nms.legacy

import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.mockbukkit.mockbukkit.MockBukkit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LegacyDirectNmsRuntimeVersionModuleTest {
    @Test
    fun `legacy runtime delegates covered APIs and exposes direct NMS for missing player list`() {
        val server = MockBukkit.mock()

        try {
            val module = FakeLegacyFactory("v1_12_R1", 6).create(server, runtimeVersion("v1_12_R1", "1.12.2"))

            assertIs<LegacyDirectNmsRuntimeVersionModule>(module)
            assertEquals("legacy-direct-nms-v1_12_R1", module.id)
            assertEquals("Spigot title API", module.capabilities.single { it.name == RuntimeCapability.TITLES }.detail)
            assertEquals("Spigot ChatMessageType API", module.capabilities.single { it.name == RuntimeCapability.ACTIONBAR }.detail)
            assertEquals("direct NMS PacketPlayOutPlayerListHeaderFooter", module.capabilities.single { it.name == RuntimeCapability.PLAYER_LIST }.detail)
            assertEquals(RuntimeCapabilityStatus.AVAILABLE, module.capabilities.single { it.name == RuntimeCapability.DIRECT_NMS }.status)
            assertEquals("direct=[player-list]; api=[titles, actionbar, sidebar]", module.capabilities.single { it.name == RuntimeCapability.DIRECT_NMS }.detail)
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `legacy runtime exposes direct title actionbar and player list when old APIs are incomplete`() {
        val server = MockBukkit.mock()

        try {
            val module = FakeLegacyFactory("v1_8_R3", 2).create(server, runtimeVersion("v1_8_R3", "1.8.8"))

            assertEquals("direct NMS PacketPlayOutTitle", module.capabilities.single { it.name == RuntimeCapability.TITLES }.detail)
            assertEquals("direct NMS PacketPlayOutChat position 2", module.capabilities.single { it.name == RuntimeCapability.ACTIONBAR }.detail)
            assertEquals("direct=[titles, actionbar, player-list]; api=[sidebar]", module.capabilities.single { it.name == RuntimeCapability.DIRECT_NMS }.detail)
        } finally {
            MockBukkit.unmock()
        }
    }

    private fun runtimeVersion(nmsVersion: String, minecraftVersion: String): RuntimeServerVersion {
        return RuntimeServerVersion(
            bukkitVersion = "git-Spigot-test (MC: $minecraftVersion)",
            minecraftVersion = minecraftVersion,
            craftBukkitPackage = "org.bukkit.craftbukkit.$nmsVersion",
            nmsVersion = nmsVersion
        )
    }

    private class FakeLegacyFactory(
        nmsVersion: String,
        versionIndex: Int
    ) : LegacyDirectNmsRuntimeVersionModuleFactory(nmsVersion, versionIndex) {
        override fun createPacketSink(): LegacyDirectNmsPacketSink = FakePacketSink
    }

    private object FakePacketSink : LegacyDirectNmsPacketSink {
        override fun sendTitleTimes(player: Player, ticks: LegacyTitleTicks) = Unit
        override fun sendTitle(player: Player, title: String, ticks: LegacyTitleTicks) = Unit
        override fun sendSubtitle(player: Player, subtitle: String, ticks: LegacyTitleTicks) = Unit
        override fun sendActionbar(player: Player, text: String) = Unit
        override fun sendPlayerListHeaderAndFooter(player: Player, header: String, footer: String) = Unit
    }
}
