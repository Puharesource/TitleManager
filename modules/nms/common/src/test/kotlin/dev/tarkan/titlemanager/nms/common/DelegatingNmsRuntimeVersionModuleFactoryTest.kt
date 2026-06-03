package dev.tarkan.titlemanager.nms.common

import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import org.mockbukkit.mockbukkit.MockBukkit
import kotlin.test.Test
import kotlin.test.assertEquals

class DelegatingNmsRuntimeVersionModuleFactoryTest {
    @Test
    fun `creates exact NMS module that delegates current features and reports direct packets pending`() {
        val server = MockBukkit.mock()

        try {
            val serverVersion = RuntimeServerVersion(
                bukkitVersion = "git-Paper-test (MC: 1.20.4)",
                minecraftVersion = "1.20.4",
                craftBukkitPackage = "org.bukkit.craftbukkit.v1_20_R3",
                nmsVersion = "v1_20_R3"
            )
            val module = TestFactory.create(server, serverVersion)

            assertEquals("nms-test", module.id)
            assertEquals("nms-test delegating to bukkit-api (1.20.4 / v1_20_R3)", module.displayName)
            assertEquals(RuntimeCapabilityStatus.AVAILABLE, module.capabilities.single { it.name == RuntimeCapability.TITLES }.status)
            assertEquals(RuntimeCapabilityStatus.UNAVAILABLE, module.capabilities.single { it.name == RuntimeCapability.DIRECT_NMS }.status)
            assertEquals("test direct packets pending", module.capabilities.single { it.name == RuntimeCapability.DIRECT_NMS }.detail)
        } finally {
            MockBukkit.unmock()
        }
    }

    private object TestFactory : DelegatingNmsRuntimeVersionModuleFactory(
        id = "nms-test",
        supportedNmsVersion = "v1_20_R3",
        directPacketStatusDetail = "test direct packets pending"
    )
}
