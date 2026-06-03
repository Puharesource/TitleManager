import dev.tarkan.titlemanager.bukkit.configuration.ConfigurationManager
import dev.tarkan.titlemanager.bukkit.configuration.ConfigurationResourceProvider
import dev.tarkan.titlemanager.bukkit.configuration.ConfigurationException
import dev.tarkan.titlemanager.bukkit.configuration.RuntimeConfigurationValidator
import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilities
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.LegacySpigotRuntimeAdapter
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.LegacySpigotTitleOnlyRuntimeAdapter
import org.mockbukkit.mockbukkit.MockBukkit
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RuntimeConfigurationValidatorTest {
    @Test
    fun `accepts default configuration when current Bukkit API capabilities are available`() {
        RuntimeConfigurationValidator().validate(configurationManager(), capabilities())
    }

    @Test
    fun `rejects enabled title configuration when titles are unavailable`() {
        val exception = assertFailsWith<ConfigurationException> {
            RuntimeConfigurationValidator().validate(
                configurationManager(),
                capabilities(
                    DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.UNAVAILABLE, "unsupported in test")
                )
            )
        }

        assertTrue(exception.message!!.contains("welcome titles requires capability 'titles'"))
        assertTrue(exception.message!!.contains("unsupported in test"))
    }

    @Test
    fun `reports all unavailable configured feature capabilities`() {
        val exception = assertFailsWith<ConfigurationException> {
            RuntimeConfigurationValidator().validate(
                configurationManager(),
                RuntimeCapabilities(
                    versionModule = "test-module",
                    versionModuleThreading = "title=main-thread",
                    capabilities = listOf(
                        DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.UNAVAILABLE, "title unsupported"),
                        DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.UNAVAILABLE, "actionbar unsupported"),
                        DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.UNAVAILABLE, "player-list unsupported"),
                        DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.UNAVAILABLE, "sidebar unsupported")
                    )
                )
            )
        }

        val message = exception.message!!
        assertTrue(message.contains("welcome titles requires capability 'titles'"))
        assertTrue(message.contains("welcome actionbars requires capability 'actionbar'"))
        assertTrue(message.contains("player-list headers and footers requires capability 'player-list'"))
        assertTrue(message.contains("scoreboards requires capability 'sidebar'"))
    }

    @Test
    fun `legacy Spigot API fallback rejects default player-list configuration before runtime startup`() {
        val server = MockBukkit.mock()

        try {
            val module = LegacySpigotRuntimeAdapter(server, runtimeVersion("1.12.2", "v1_12_R1"))
            val exception = assertFailsWith<ConfigurationException> {
                RuntimeConfigurationValidator().validate(configurationManager(), module.capabilitySnapshot())
            }

            val message = exception.message!!
            assertTrue(message.contains("player-list headers and footers requires capability 'player-list'"))
            assertTrue(message.contains("player-list header/footer requires Bukkit API 1.17+ or a direct NMS module"))
            assertTrue(!message.contains("welcome titles requires capability 'titles'"))
            assertTrue(!message.contains("welcome actionbars requires capability 'actionbar'"))
            assertTrue(!message.contains("scoreboards requires capability 'sidebar'"))
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `title-only legacy Spigot fallback rejects default actionbar and player-list configuration before runtime startup`() {
        val server = MockBukkit.mock()

        try {
            val module = LegacySpigotTitleOnlyRuntimeAdapter(server, runtimeVersion("1.8.8", "v1_8_R3"))
            val exception = assertFailsWith<ConfigurationException> {
                RuntimeConfigurationValidator().validate(configurationManager(), module.capabilitySnapshot())
            }

            val message = exception.message!!
            assertTrue(message.contains("welcome actionbars requires capability 'actionbar'"))
            assertTrue(message.contains("actionbar requires Spigot 1.12+ or a direct NMS module"))
            assertTrue(message.contains("player-list headers and footers requires capability 'player-list'"))
            assertTrue(!message.contains("welcome titles requires capability 'titles'"))
            assertTrue(!message.contains("scoreboards requires capability 'sidebar'"))
        } finally {
            MockBukkit.unmock()
        }
    }

    @Test
    fun `rejects enabled announcer when needed runtime capabilities are unavailable`() {
        val exception = assertFailsWith<ConfigurationException> {
            RuntimeConfigurationValidator().validate(
                configurationManager {
                    resolve("announcer.yml").writeText(
                        """
                            enabled: true
                            announcements:
                              test:
                                interval: 60
                                timings:
                                  fadeIn: 20
                                  stay: 40
                                  fadeOut: 20
                                titles:
                                  - "Announced title"
                                actionbar:
                                  - "Announced actionbar"
                        """.trimIndent()
                    )
                },
                RuntimeCapabilities(
                    versionModule = "test-module",
                    versionModuleThreading = "title=main-thread",
                    capabilities = listOf(
                        DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.UNAVAILABLE, "title unsupported"),
                        DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.UNAVAILABLE, "actionbar unsupported"),
                        DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.AVAILABLE, "player-list supported"),
                        DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.AVAILABLE, "sidebar supported")
                    )
                )
            )
        }

        val message = exception.message!!
        assertTrue(message.contains("announcer titles requires capability 'titles'"))
        assertTrue(message.contains("announcer actionbars requires capability 'actionbar'"))
    }

    @Test
    fun `accepts enabled BungeeCord integration`() {
        RuntimeConfigurationValidator().validate(
            configurationManager {
                resolve("advanced.yml").writeText(
                    """
                        configVersion: 7
                        threadPoolSize: 4
                        usingConfig: true
                        usingBungeeCord: true
                        checkForUpdates: false
                        databaseConnectionString: ""
                    """.trimIndent()
                )
            },
            capabilities()
        )
    }

    @Test
    fun `rejects BungeeCord-only placeholders when BungeeCord integration is disabled`() {
        val exception = assertFailsWith<ConfigurationException> {
            RuntimeConfigurationValidator().validate(
                configurationManager {
                    resolve("player-list.yml").writeText(
                        """
                            enabled: true
                            header: "%{server} %{server-name}"
                            footer: "%{bungeecord-online} %{bungeecord-online-players}"
                            worlds: {}
                        """.trimIndent()
                    )
                },
                capabilities()
            )
        }

        val message = exception.message!!
        assertTrue(message.contains("player-list header uses unsupported legacy placeholder '%{server}'"))
        assertTrue(message.contains("player-list header uses unsupported legacy placeholder '%{server-name}'"))
        assertTrue(message.contains("player-list footer uses unsupported legacy placeholder '%{bungeecord-online}'"))
        assertTrue(message.contains("player-list footer uses unsupported legacy placeholder '%{bungeecord-online-players}'"))
    }

    @Test
    fun `accepts BungeeCord-only placeholders when BungeeCord integration is enabled`() {
        RuntimeConfigurationValidator().validate(
            configurationManager {
                resolve("advanced.yml").writeText(
                    """
                        configVersion: 7
                        threadPoolSize: 4
                        usingConfig: true
                        usingBungeeCord: true
                        checkForUpdates: false
                        databaseConnectionString: ""
                    """.trimIndent()
                )
                resolve("player-list.yml").writeText(
                    """
                        enabled: true
                        header: "%{server} %{server-name}"
                        footer: "%{bungeecord-online} %{bungeecord-online-players}"
                        worlds: {}
                    """.trimIndent()
                )
            },
            capabilities()
        )
    }

    @Test
    fun `accepts Vault placeholders when Vault integration is available`() {
        RuntimeConfigurationValidator(
            vaultEconomyAvailable = true,
            vaultPermissionGroupsAvailable = true
        ).validate(
            configurationManager {
                resolve("player-list.yml").writeText(
                    """
                        enabled: true
                        header: "%{balance} %{money}"
                        footer: "%{group} %{group-name}"
                        worlds: {}
                    """.trimIndent()
                )
            },
            capabilities()
        )
    }

    @Test
    fun `accepts unavailable config-driven capabilities and placeholders when config listeners are disabled`() {
        RuntimeConfigurationValidator().validate(
            configurationManager {
                resolve("advanced.yml").writeText(
                    """
                        configVersion: 7
                        threadPoolSize: 4
                        usingConfig: false
                        usingBungeeCord: false
                        checkForUpdates: false
                        databaseConnectionString: ""
                    """.trimIndent()
                )
                resolve("player-list.yml").writeText(
                    """
                        enabled: true
                        header: "%{balance}"
                        footer: "%{safe-online}"
                        worlds: {}
                    """.trimIndent()
                )
            },
            RuntimeCapabilities(
                versionModule = "test-module",
                versionModuleThreading = "title=main-thread",
                capabilities = listOf(
                    DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.UNAVAILABLE, "title unsupported"),
                    DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.UNAVAILABLE, "actionbar unsupported"),
                    DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.UNAVAILABLE, "player-list unsupported"),
                    DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.UNAVAILABLE, "sidebar unsupported")
                )
            )
        )
    }

    @Test
    fun `rejects known unsupported legacy placeholders in enabled runtime configuration`() {
        val exception = assertFailsWith<ConfigurationException> {
            RuntimeConfigurationValidator().validate(
                configurationManager {
                    resolve("player-list.yml").writeText(
                        """
                            enabled: true
                            header: "%{balance}"
                            footer: "%{money}"
                            worlds:
                              world_nether:
                                enabled: false
                                header: "%{tps} %{c:#ff0000} %{gradient:[#ff0000,#00ff00]Text}"
                                footer: "%{group-name}"
                        """.trimIndent()
                    )
                },
                capabilities()
            )
        }

        val message = exception.message!!
        assertTrue(message.contains("player-list header uses unsupported legacy placeholder '%{balance}'"))
        assertTrue(message.contains("player-list footer uses unsupported legacy placeholder '%{money}'"))
        assertTrue(!message.contains("%{tps}"))
        assertTrue(!message.contains("%{group-name}"))
    }

    @Test
    fun `rejects unsupported placeholders in enabled announcer text`() {
        val exception = assertFailsWith<ConfigurationException> {
            RuntimeConfigurationValidator().validate(
                configurationManager {
                    resolve("announcer.yml").writeText(
                        """
                            enabled: true
                            announcements:
                              test:
                                interval: 60
                                timings:
                                  fadeIn: 20
                                  stay: 40
                                  fadeOut: 20
                                titles:
                                  - "%{balance}"
                                actionbar:
                                  - "%{money}"
                        """.trimIndent()
                    )
                },
                capabilities()
            )
        }

        val message = exception.message!!
        assertTrue(message.contains("announcer 'test' title 1 uses unsupported legacy placeholder '%{balance}'"))
        assertTrue(message.contains("announcer 'test' actionbar 1 uses unsupported legacy placeholder '%{money}'"))
    }

    @Test
    fun `rejects scoreboards with more than fifteen lines before runtime updates`() {
        val exception = assertFailsWith<ConfigurationException> {
            RuntimeConfigurationValidator().validate(
                configurationManager {
                    resolve("scoreboard.yml").writeText(
                        buildString {
                            appendLine("enabled: true")
                            appendLine("title: \"Too Tall\"")
                            appendLine("content: |-")
                            (1..16).forEach { lineNumber ->
                                appendLine("  Line $lineNumber")
                            }
                            appendLine("worlds: {}")
                        }
                    )
                },
                capabilities()
            )
        }

        assertTrue(exception.message!!.contains("Configured scoreboard has 16 lines"))
    }

    private fun configurationManager(configure: java.nio.file.Path.() -> Unit = {}): ConfigurationManager {
        val dataFolder = Files.createTempDirectory("titlemanager-configuration-validator")
        val resourceProvider = ConfigurationResourceProvider { path ->
            javaClass.classLoader.getResourceAsStream(path)
        }
        dataFolder.configure()

        return ConfigurationManager(resourceProvider, dataFolder)
    }

    private fun capabilities(vararg overrides: DiagnosticsStatus): RuntimeCapabilities {
        val overrideByName = overrides.associateBy { it.name }

        return RuntimeCapabilities(
            versionModule = "test-module",
            versionModuleThreading = "title=main-thread",
            capabilities = listOf(
                overrideByName[RuntimeCapability.TITLES] ?: DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.AVAILABLE, "title supported"),
                overrideByName[RuntimeCapability.ACTIONBAR] ?: DiagnosticsStatus(RuntimeCapability.ACTIONBAR, RuntimeCapabilityStatus.AVAILABLE, "actionbar supported"),
                overrideByName[RuntimeCapability.PLAYER_LIST] ?: DiagnosticsStatus(RuntimeCapability.PLAYER_LIST, RuntimeCapabilityStatus.AVAILABLE, "player-list supported"),
                overrideByName[RuntimeCapability.SIDEBAR] ?: DiagnosticsStatus(RuntimeCapability.SIDEBAR, RuntimeCapabilityStatus.AVAILABLE, "sidebar supported")
            )
        )
    }

    private fun RuntimeVersionModule.capabilitySnapshot(): RuntimeCapabilities {
        return RuntimeCapabilities(
            versionModule = id,
            versionModuleThreading = threadingPolicy.render(),
            capabilities = capabilities
        )
    }

    private fun runtimeVersion(minecraftVersion: String, nmsVersion: String): RuntimeServerVersion {
        return RuntimeServerVersion(
            bukkitVersion = "git-Spigot-test (MC: $minecraftVersion)",
            minecraftVersion = minecraftVersion,
            craftBukkitPackage = "org.bukkit.craftbukkit.$nmsVersion",
            nmsVersion = nmsVersion
        )
    }
}
