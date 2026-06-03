package dev.tarkan.titlemanager.bukkit.diagnostics

import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.BukkitApiRuntimeAdapter
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.LegacySpigotRuntimeAdapter
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.LegacySpigotTitleOnlyRuntimeAdapter
import org.bukkit.Server
import java.util.ServiceLoader

class VersionModuleSelector(
    private val directModuleFactories: List<RuntimeVersionModuleFactory> = emptyList()
) {
    fun select(server: Server): RuntimeVersionModule {
        return select(server, RuntimeServerVersion.from(server))
    }

    fun select(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule {
        val sortedFactories = directModuleFactories
            .sortedWith(compareByDescending<RuntimeVersionModuleFactory> { it.priority }.thenBy { it.id })
        val fallbackModule = createFallbackModule(server, serverVersion)
        val compatibleNonNmsFactory = sortedFactories.firstOrNull { factory ->
            factory !is ExactNmsRuntimeVersionModuleFactory && factory.isCompatible(serverVersion)
        }

        if (compatibleNonNmsFactory != null) {
            return compatibleNonNmsFactory.create(server, serverVersion)
        }

        if (fallbackModule.hasAllRequiredRuntimeCapabilities()) {
            return fallbackModule
        }

        return sortedFactories
            .firstOrNull { it.isCompatible(serverVersion) }
            ?.create(server, serverVersion)
            ?: fallbackModule
    }

    private fun createFallbackModule(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule {
        if (BukkitApiRuntimeAdapter.isCompatible(serverVersion)) {
            return BukkitApiRuntimeAdapter(server, serverVersion)
        }

        if (LegacySpigotRuntimeAdapter.isCompatible(serverVersion)) {
            return LegacySpigotRuntimeAdapter(server, serverVersion)
        }

        if (LegacySpigotTitleOnlyRuntimeAdapter.isCompatible(serverVersion)) {
            return LegacySpigotTitleOnlyRuntimeAdapter(server, serverVersion)
        }

        return UnsupportedRuntimeVersionModule(
            serverVersion = serverVersion,
            reason = "no direct module selected and no compatible Bukkit/Spigot API fallback is available"
        )
    }

    private fun RuntimeVersionModule.hasAllRequiredRuntimeCapabilities(): Boolean {
        val capabilityStatuses = capabilities.associateBy { it.name }

        return REQUIRED_RUNTIME_CAPABILITIES.all { capability ->
            capabilityStatuses[capability]?.status == RuntimeCapabilityStatus.AVAILABLE
        }
    }

    companion object {
        private val REQUIRED_RUNTIME_CAPABILITIES = setOf(
            RuntimeCapability.TITLES,
            RuntimeCapability.ACTIONBAR,
            RuntimeCapability.PLAYER_LIST,
            RuntimeCapability.SIDEBAR
        )

        fun fromServiceLoader(
            classLoader: ClassLoader = VersionModuleSelector::class.java.classLoader
        ): VersionModuleSelector {
            return VersionModuleSelector(
                ServiceLoader.load(RuntimeVersionModuleFactory::class.java, classLoader).toList()
            )
        }
    }
}
