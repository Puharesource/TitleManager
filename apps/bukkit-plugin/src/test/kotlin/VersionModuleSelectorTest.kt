import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.ExactNmsRuntimeVersionModuleFactory
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeSidebar
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeThreadingPolicy
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModuleFactory
import dev.tarkan.titlemanager.bukkit.diagnostics.UnsupportedRuntimeVersionModule
import dev.tarkan.titlemanager.bukkit.diagnostics.VersionModuleSelector
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.BukkitApiRuntimeAdapter
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.LegacySpigotRuntimeAdapter
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.LegacySpigotTitleOnlyRuntimeAdapter
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Server
import org.bukkit.entity.Player
import org.mockbukkit.mockbukkit.MockBukkit
import java.net.URLClassLoader
import java.util.ServiceLoader
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class VersionModuleSelectorTest {
    @Test
    fun `parses Bukkit and NMS runtime version metadata`() {
        val serverVersion = RuntimeServerVersion(
            bukkitVersion = "git-Paper-497 (MC: 1.20.4)",
            minecraftVersion = RuntimeServerVersion.parseMinecraftVersion("git-Paper-497 (MC: 1.20.4)"),
            craftBukkitPackage = "org.bukkit.craftbukkit.v1_20_R3",
            nmsVersion = "v1_20_R3"
        )

        assertEquals("1.20.4", serverVersion.minecraftVersion)
        assertEquals("1.20.4 / v1_20_R3", serverVersion.displayVersion)
        assertEquals(true, serverVersion.matchesNmsVersion("v1_20_R3"))
    }

    @Test
    fun `falls back to Bukkit API module when no direct module matches`() {
        val server = MockBukkit.mock()

        try {
            val selector = VersionModuleSelector(listOf(DirectModuleFactory(compatible = false)))

            assertIs<BukkitApiRuntimeAdapter>(selector.select(server))
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `uses unsupported module when no direct module or API fallback matches`() {
        val server = MockBukkit.mock()

        try {
            val selector = VersionModuleSelector(emptyList())
            val runtimeVersion = RuntimeServerVersion("git-Spigot-test (MC: 1.7.10)", "1.7.10", "org.bukkit.craftbukkit.v1_7_R4", "v1_7_R4")
            val module = selector.select(server, runtimeVersion)

            assertIs<UnsupportedRuntimeVersionModule>(module)
            assertEquals("unsupported", module.id)
            assertEquals(
                "no direct module selected and no compatible Bukkit/Spigot API fallback is available",
                module.capabilities.single { it.name == "titles" }.detail
            )
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `uses legacy Spigot API fallback for old versions with title and actionbar APIs`() {
        val server = MockBukkit.mock()

        try {
            val selector = VersionModuleSelector(emptyList())

            assertIs<LegacySpigotRuntimeAdapter>(selector.select(server, runtimeVersion("1.12.2", "v1_12_R1")))
            assertIs<LegacySpigotRuntimeAdapter>(selector.select(server, runtimeVersion("1.16.5", "v1_16_R3")))
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `uses title-only legacy Spigot API fallback for oldest supported Spigot title APIs`() {
        val server = MockBukkit.mock()

        try {
            val selector = VersionModuleSelector(emptyList())

            assertIs<LegacySpigotTitleOnlyRuntimeAdapter>(selector.select(server, runtimeVersion("1.8.8", "v1_8_R3")))
            assertIs<LegacySpigotTitleOnlyRuntimeAdapter>(selector.select(server, runtimeVersion("1.11.2", "v1_11_R1")))
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `selects highest priority compatible non-NMS factory before fallback modules`() {
        val server = MockBukkit.mock()

        try {
            val selector = VersionModuleSelector(
                listOf(
                    DirectModuleFactory(id = "direct-v1", compatible = false, priority = 0),
                    DirectModuleFactory(id = "direct-v2", compatible = true, priority = 10),
                    DirectModuleFactory(id = "direct-v3", compatible = true, priority = 0)
                )
            )

            assertEquals("direct-v2", selector.select(server, runtimeVersion("1.20.4", "v1_20_R3")).id)
            assertEquals("direct-v2", selector.select(server, runtimeVersion("1.12.2", "v1_12_R1")).id)
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `loads direct module factories from service loader`() {
        val servicesRoot = Files.createTempDirectory("titlemanager-service-loader-test")
        val servicesDirectory = servicesRoot.resolve("META-INF/services")
        Files.createDirectories(servicesDirectory)
        servicesDirectory.resolve(RuntimeVersionModuleFactory::class.qualifiedName!!)
            .toFile()
            .writeText("dev.tarkan.titlemanager.bukkit.test.ServiceLoadedRuntimeModuleFactory\n")

        URLClassLoader(arrayOf(servicesRoot.toUri().toURL()), javaClass.classLoader).use { classLoader ->
            val server = MockBukkit.mock()

            try {
                val selector = VersionModuleSelector.fromServiceLoader(classLoader)

                assertEquals("service-loaded-direct", selector.select(server, runtimeVersion("1.16.5", "v1_16_R3")).id)
            } finally {
                MockBukkit.unmock()
            }
        }
    }

    @Test
    fun `complete Bukkit API fallback is preferred over matching exact NMS metadata`() {
        val server = MockBukkit.mock()

        try {
            val selector = VersionModuleSelector.fromServiceLoader()
            val runtimeVersion = RuntimeServerVersion(
                bukkitVersion = "git-Paper-test (MC: 1.20.4)",
                minecraftVersion = "1.20.4",
                craftBukkitPackage = "org.bukkit.craftbukkit.v1_20_R3",
                nmsVersion = "v1_20_R3"
            )

            val module = selector.select(server, runtimeVersion)

            assertEquals("bukkit-api", module.id)
            assertEquals(RuntimeCapabilityStatus.UNAVAILABLE, module.capabilities.single { it.name == RuntimeCapability.DIRECT_NMS }.status)
            assertEquals(
                "no direct module selected for 1.20.4 / v1_20_R3",
                module.capabilities.single { it.name == RuntimeCapability.DIRECT_NMS }.detail
            )
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `runtime selection matrix fails closed for unsupported old anchor versions`() {
        val server = MockBukkit.mock()

        try {
            val selector = VersionModuleSelector.fromServiceLoader()

            assertEquals("legacy-direct-nms-v1_8_R3", selector.select(server, runtimeVersion("1.8.8", "v1_8_R3")).id)
            assertEquals("legacy-direct-nms-v1_12_R1", selector.select(server, runtimeVersion("1.12.2", "v1_12_R1")).id)
            assertEquals("legacy-direct-nms-v1_16_R1", selector.select(server, runtimeVersion("1.16.5", "v1_16_R1")).id)
            assertEquals("bukkit-api", selector.select(server, runtimeVersion("1.20.4", "v1_20_R3")).id)
            assertEquals("bukkit-api", selector.select(server, runtimeVersion("1.21.1", "v1_21_R1")).id)
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `service loader discovers every per-version legacy NMS module`() {
        val expectedLegacyFactories = setOf(
            "legacy-direct-nms-v1_8_R1",
            "legacy-direct-nms-v1_8_R2",
            "legacy-direct-nms-v1_8_R3",
            "legacy-direct-nms-v1_9_R1",
            "legacy-direct-nms-v1_9_R2",
            "legacy-direct-nms-v1_10_R1",
            "legacy-direct-nms-v1_11_R1",
            "legacy-direct-nms-v1_12_R1",
            "legacy-direct-nms-v1_13_R1",
            "legacy-direct-nms-v1_13_R2",
            "legacy-direct-nms-v1_14_R1",
            "legacy-direct-nms-v1_15_R1",
            "legacy-direct-nms-v1_16_R1"
        )

        val loadedLegacyFactories = ServiceLoader.load(RuntimeVersionModuleFactory::class.java, javaClass.classLoader)
            .map { it.id }
            .filter { it.startsWith("legacy-direct-nms-") }
            .toSet()

        assertEquals(expectedLegacyFactories, loadedLegacyFactories)
    }

    @Test
    fun `default service loader discovery does not depend on thread context classloader`() {
        val originalContextClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = URLClassLoader(emptyArray(), null)

        try {
            val server = MockBukkit.mock()

            try {
                val selector = VersionModuleSelector.fromServiceLoader()
                val runtimeVersion = RuntimeServerVersion(
                    bukkitVersion = "git-Paper-test (MC: 1.20.4)",
                    minecraftVersion = "1.20.4",
                    craftBukkitPackage = "org.bukkit.craftbukkit.v1_20_R3",
                    nmsVersion = "v1_20_R3"
                )

                assertEquals("bukkit-api", selector.select(server, runtimeVersion).id)
            } finally {
                MockBukkit.unmock()
            }
        } finally {
            Thread.currentThread().contextClassLoader = originalContextClassLoader
        }
    }

    @Test
    fun `exact NMS factory matches only declared runtime key`() {
        val factory = ExactFactory("v1_20_R3")

        assertEquals(
            true,
            factory.isCompatible(RuntimeServerVersion("test", "1.20.4", "org.bukkit.craftbukkit.v1_20_R3", "v1_20_R3"))
        )
        assertEquals(
            false,
            factory.isCompatible(RuntimeServerVersion("test", "1.20.5", "org.bukkit.craftbukkit.v1_20_R4", "v1_20_R4"))
        )
        assertEquals(
            false,
            factory.isCompatible(RuntimeServerVersion("test", "1.20.4", "org.mockbukkit.mockbukkit", null))
        )
    }

    private class DirectModuleFactory(
        override val id: String = "direct-test",
        private val compatible: Boolean,
        override val priority: Int = 0
    ) : RuntimeVersionModuleFactory {
        override fun isCompatible(serverVersion: RuntimeServerVersion): Boolean = compatible

        override fun create(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule = DirectModule(id)
    }

    private class ExactFactory(version: String) : ExactNmsRuntimeVersionModuleFactory("exact-test", version) {
        override fun create(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule = DirectModule(id)
    }

    private class DirectModule(
        override val id: String
    ) : RuntimeVersionModule {
        override val displayName: String = id
        override val capabilities: List<DiagnosticsStatus> = emptyList()
        override val threadingPolicy: RuntimeThreadingPolicy = RuntimeThreadingPolicy.mainThreadOnly()

        override fun sendTitleTimes(player: Player, times: Title.Times) = Unit

        override fun sendTitle(player: Player, title: Component) = Unit

        override fun sendSubtitle(player: Player, subtitle: Component) = Unit

        override fun showTitle(player: Player, title: Title) = Unit

        override fun sendActionBar(player: Player, actionBar: Component) = Unit

        override fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component) = Unit

        override fun createSidebar(player: Player): RuntimeSidebar = DirectSidebar
    }

    private object DirectSidebar : RuntimeSidebar {
        override var title: String = ""

        override fun isAppliedTo(player: Player): Boolean = false

        override fun get(index: Int): String? = null

        override fun set(index: Int, value: String) = Unit

        override fun remove(index: Int) = Unit

        override fun close() = Unit
    }

    private fun runtimeVersion(minecraftVersion: String, nmsVersion: String): RuntimeServerVersion {
        return RuntimeServerVersion(
            bukkitVersion = "git-Paper-test (MC: $minecraftVersion)",
            minecraftVersion = minecraftVersion,
            craftBukkitPackage = "org.bukkit.craftbukkit.$nmsVersion",
            nmsVersion = nmsVersion
        )
    }
}
