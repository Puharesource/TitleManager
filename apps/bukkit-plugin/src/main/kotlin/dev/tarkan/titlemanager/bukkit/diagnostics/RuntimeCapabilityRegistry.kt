package dev.tarkan.titlemanager.bukkit.diagnostics

import dev.tarkan.titlemanager.bukkit.command.CommandPlatformCapabilities

import org.bukkit.Server

class RuntimeCapabilityRegistry private constructor(
    private val selectedVersionModule: RuntimeVersionModule?,
    private val versionModuleSelector: VersionModuleSelector?
) {
    constructor() : this(
        selectedVersionModule = null,
        versionModuleSelector = VersionModuleSelector.fromServiceLoader()
    )

    constructor(versionModuleSelector: VersionModuleSelector) : this(
        selectedVersionModule = null,
        versionModuleSelector = versionModuleSelector
    )

    constructor(selectedVersionModule: RuntimeVersionModule) : this(
        selectedVersionModule = selectedVersionModule,
        versionModuleSelector = null
    )

    fun normalRuntime(server: Server): RuntimeCapabilities {
        val versionModule = selectedVersionModule ?: requireNotNull(versionModuleSelector).select(server)

        return RuntimeCapabilities(
            versionModule = versionModule.displayName,
            versionModuleThreading = versionModule.threadingPolicy.render(),
            capabilities = versionModule.capabilities + CommandPlatformCapabilities.detect(server).diagnostics
        )
    }

    fun safeModeRuntime(): RuntimeCapabilities = RuntimeCapabilities(
        versionModule = "unavailable (runtime not started)",
        versionModuleThreading = "inactive",
        capabilities = listOf(
            DiagnosticsStatus(RuntimeCapability.RUNTIME_FEATURES, RuntimeCapabilityStatus.UNAVAILABLE, "configuration failed before runtime startup")
        )
    )
}

data class RuntimeCapabilities(
    val versionModule: String,
    val versionModuleThreading: String,
    val capabilities: List<DiagnosticsStatus>
) {
    fun status(name: String): DiagnosticsStatus? = capabilities.firstOrNull { it.name == name }

    fun isAvailable(name: String): Boolean = status(name)?.status == RuntimeCapabilityStatus.AVAILABLE
}
