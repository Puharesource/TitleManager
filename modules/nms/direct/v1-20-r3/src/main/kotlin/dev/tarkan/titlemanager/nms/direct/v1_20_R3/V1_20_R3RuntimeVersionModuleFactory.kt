package dev.tarkan.titlemanager.nms.direct.v1_20_R3

import dev.tarkan.titlemanager.bukkit.diagnostics.ExactNmsRuntimeVersionModuleFactory
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import org.bukkit.Server

class V1_20_R3RuntimeVersionModuleFactory : ExactNmsRuntimeVersionModuleFactory(
    id = "nms-v1_20_R3",
    supportedNmsVersion = "v1_20_R3"
) {
    override val priority: Int = 100

    override fun create(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule {
        return V1_20_R3RuntimeVersionModule(server, serverVersion)
    }
}
