package dev.tarkan.titlemanager.nms.common

import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.ExactNmsRuntimeVersionModuleFactory
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import dev.tarkan.titlemanager.bukkit.runtime.adapter.bukkitapi.BukkitApiRuntimeAdapter
import org.bukkit.Server

abstract class DelegatingNmsRuntimeVersionModuleFactory(
    id: String,
    supportedNmsVersion: String,
    private val directPacketStatusDetail: String
) : ExactNmsRuntimeVersionModuleFactory(id, supportedNmsVersion) {
    override val priority: Int = 100

    override fun create(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule {
        return DelegatingNmsRuntimeVersionModule(
            id = id,
            delegate = BukkitApiRuntimeAdapter(server, serverVersion),
            serverVersion = serverVersion,
            directPacketStatusDetail = directPacketStatusDetail
        )
    }
}

private class DelegatingNmsRuntimeVersionModule(
    override val id: String,
    private val delegate: RuntimeVersionModule,
    serverVersion: RuntimeServerVersion,
    directPacketStatusDetail: String
) : RuntimeVersionModule by delegate {
    override val displayName = "$id delegating to ${delegate.id} (${serverVersion.displayVersion})"
    override val capabilities = delegate.capabilities.map { capability ->
        if (capability.name == RuntimeCapability.DIRECT_NMS) {
            DiagnosticsStatus(
                RuntimeCapability.DIRECT_NMS,
                RuntimeCapabilityStatus.UNAVAILABLE,
                directPacketStatusDetail
            )
        } else {
            capability
        }
    }
}
