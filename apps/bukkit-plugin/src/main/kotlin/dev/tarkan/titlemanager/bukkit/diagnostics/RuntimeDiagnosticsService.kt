package dev.tarkan.titlemanager.bukkit.diagnostics

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.configuration.AdvancedConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.AnnouncerConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.HooksConfiguration
import dev.tarkan.titlemanager.bukkit.context.PlayerContext
import dev.tarkan.titlemanager.bukkit.metrics.MetricsService
import dev.tarkan.titlemanager.bukkit.update.UpdateService
import dev.tarkan.titlemanager.parser.placeholder.animation.AnimationPlaceholderRegistry
import org.bukkit.Server

class RuntimeDiagnosticsService(
    private val plugin: TitleManagerPlugin,
    private val advancedConfiguration: AdvancedConfiguration,
    private val announcerConfiguration: AnnouncerConfiguration,
    private val hooksConfiguration: HooksConfiguration,
    private val animationPlaceholderRegistry: AnimationPlaceholderRegistry<PlayerContext>,
    private val runtimeCapabilityRegistry: RuntimeCapabilityRegistry,
    private val runtimeAnimationDiagnostics: RuntimeAnimationDiagnostics,
    private val updateService: UpdateService,
    private val metricsService: MetricsService
) {
    fun snapshot(): DiagnosticsSnapshot {
        val announcerEnabled = advancedConfiguration.usingConfig && announcerConfiguration.enabled

        return normalSnapshot(
            pluginVersion = plugin.pluginVersion,
            server = plugin.server,
            loadedAnimationFiles = runtimeAnimationDiagnostics.loadedAnimationFiles,
            registeredAnimationPlaceholders = animationPlaceholderRegistry.keys.size,
            runtimeCapabilities = runtimeCapabilityRegistry.normalRuntime(plugin.server),
            debugEnabled = advancedConfiguration.debug,
            configEnabled = advancedConfiguration.usingConfig,
            bungeeCordEnabled = advancedConfiguration.usingBungeeCord,
            combatLogXHookEnabled = hooksConfiguration.combatLogX,
            announcerEnabled = announcerEnabled,
            configuredAnnouncements = announcerConfiguration.announcements.size,
            activeAnnouncements = if (announcerEnabled) {
                announcerConfiguration.announcements.values.count { it.titles.isNotEmpty() || it.actionbar.isNotEmpty() }
            } else {
                0
            },
            updateCheckerEnabled = updateService.enabled,
            latestVersion = updateService.latestVersion,
            updateAvailable = updateService.isUpdateAvailable,
            metricsEnabled = metricsService.enabled
        )
    }

    companion object {
        fun normalSnapshot(
            pluginVersion: String,
            server: Server,
            loadedAnimationFiles: Int,
            registeredAnimationPlaceholders: Int,
            runtimeCapabilities: RuntimeCapabilities = RuntimeCapabilityRegistry().normalRuntime(server),
            debugEnabled: Boolean = false,
            configEnabled: Boolean = true,
            bungeeCordEnabled: Boolean = false,
            combatLogXHookEnabled: Boolean = true,
            announcerEnabled: Boolean = false,
            configuredAnnouncements: Int = 0,
            activeAnnouncements: Int = 0,
            updateCheckerEnabled: Boolean = false,
            latestVersion: String? = null,
            updateAvailable: Boolean = false,
            metricsEnabled: Boolean = false
        ) = DiagnosticsSnapshot(
            mode = DiagnosticsMode.NORMAL,
            pluginVersion = pluginVersion,
            serverName = server.name,
            serverVersion = server.version,
            bukkitVersion = server.bukkitVersion,
            versionModule = runtimeCapabilities.versionModule,
            versionModuleThreading = runtimeCapabilities.versionModuleThreading,
            schedulerMode = "Bukkit main-thread executor + virtual-thread async dispatcher",
            loadedAnimationFiles = loadedAnimationFiles,
            registeredAnimationPlaceholders = registeredAnimationPlaceholders,
            capabilities = runtimeCapabilities.capabilities,
            integrations = listOf(
                DiagnosticsStatus("SQLite", "active", "player storage"),
                DiagnosticsStatus("Debug", if (debugEnabled) "active" else "inactive", "advanced.yml"),
                DiagnosticsStatus("Config", if (configEnabled) "active" else "inactive", "advanced.yml"),
                DiagnosticsStatus(
                    "BungeeCord",
                    if (bungeeCordEnabled) "active" else "inactive",
                    "plugin messaging"
                ),
                server.combatLogXStatus(combatLogXHookEnabled),
                DiagnosticsStatus(
                    "Announcer",
                    if (announcerEnabled && activeAnnouncements > 0) "active" else "inactive",
                    "$activeAnnouncements active / $configuredAnnouncements configured"
                ),
                DiagnosticsStatus(
                    "Updates",
                    if (updateCheckerEnabled) {
                        if (updateAvailable) "update-available" else "active"
                    } else {
                        "inactive"
                    },
                    latestVersion?.let { "latest=$it" } ?: "latest=unknown"
                ),
                DiagnosticsStatus("Metrics", if (metricsEnabled) "active" else "inactive", "bStats"),
                DiagnosticsStatus(
                    "PlaceholderAPI",
                    if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) "active" else "inactive",
                    "plugin lookup"
                ),
                DiagnosticsStatus(
                    "Vault",
                    if (server.hasService(VAULT_ECONOMY_CLASS) || server.hasService(VAULT_PERMISSION_CLASS)) "active" else "inactive",
                    "economy=${server.serviceStatus(VAULT_ECONOMY_CLASS)}, permissions=${server.permissionStatus()}"
                )
            ),
            validationErrors = emptyList()
        )

        fun safeModeSnapshot(
            pluginVersion: String,
            server: Server,
            validationError: String,
            runtimeCapabilities: RuntimeCapabilities = RuntimeCapabilityRegistry().safeModeRuntime()
        ) = DiagnosticsSnapshot(
            mode = DiagnosticsMode.SAFE_MODE,
            pluginVersion = pluginVersion,
            serverName = server.name,
            serverVersion = server.version,
            bukkitVersion = server.bukkitVersion,
            versionModule = runtimeCapabilities.versionModule,
            versionModuleThreading = runtimeCapabilities.versionModuleThreading,
            schedulerMode = "inactive (safe mode)",
            loadedAnimationFiles = 0,
            registeredAnimationPlaceholders = 0,
            capabilities = runtimeCapabilities.capabilities,
            integrations = listOf(
                DiagnosticsStatus("Announcer", "inactive", "safe mode"),
                DiagnosticsStatus("BungeeCord", "inactive", "safe mode"),
                DiagnosticsStatus("CombatLogX", "inactive", "safe mode"),
                DiagnosticsStatus(
                    "PlaceholderAPI",
                    if (server.pluginManager.isPluginEnabled("PlaceholderAPI")) "active" else "inactive",
                    "plugin lookup"
                ),
                DiagnosticsStatus(
                    "Vault",
                    if (server.hasService(VAULT_ECONOMY_CLASS) || server.hasService(VAULT_PERMISSION_CLASS)) "active" else "inactive",
                    "economy=${server.serviceStatus(VAULT_ECONOMY_CLASS)}, permissions=${server.permissionStatus()}"
                )
            ),
            validationErrors = listOf(validationError)
        )

        private fun Server.serviceStatus(className: String): String {
            return if (hasService(className)) "active" else "inactive"
        }

        private fun Server.hasService(className: String): Boolean {
            return try {
                @Suppress("UNCHECKED_CAST")
                val serviceClass = Class.forName(className) as Class<Any>
                servicesManager.getRegistration(serviceClass) != null
            } catch (_: ClassNotFoundException) {
                false
            }
        }

        private fun Server.permissionStatus(): String {
            return try {
                @Suppress("UNCHECKED_CAST")
                val serviceClass = Class.forName(VAULT_PERMISSION_CLASS) as Class<Any>
                val provider = servicesManager.getRegistration(serviceClass)?.provider ?: return "inactive"
                if (provider.javaClass.getMethod("hasGroupSupport").invoke(provider) == true) {
                    "active/groups"
                } else {
                    "active/no-groups"
                }
            } catch (_: ClassNotFoundException) {
                "inactive"
            } catch (_: ReflectiveOperationException) {
                "active/unknown-groups"
            }
        }

        private fun Server.combatLogXStatus(hookEnabled: Boolean): DiagnosticsStatus {
            return when {
                !hookEnabled -> DiagnosticsStatus("CombatLogX", "inactive", "disabled in hooks.yml")
                !pluginManager.isPluginEnabled("CombatLogX") -> DiagnosticsStatus("CombatLogX", "inactive", "plugin not installed")
                else -> DiagnosticsStatus("CombatLogX", "unsupported", "scoreboard combat suppression pending direct hook")
            }
        }

        private const val VAULT_ECONOMY_CLASS = "net.milkbowl.vault.economy.Economy"
        private const val VAULT_PERMISSION_CLASS = "net.milkbowl.vault.permission.Permission"
    }
}
