package dev.tarkan.titlemanager.bukkit.test

import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeServerVersion
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeSidebar
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeThreadingPolicy
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModule
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeVersionModuleFactory
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Server
import org.bukkit.entity.Player

class ServiceLoadedRuntimeModuleFactory : RuntimeVersionModuleFactory {
    override val id = "service-loaded-direct"

    override fun isCompatible(serverVersion: RuntimeServerVersion): Boolean = true

    override fun create(server: Server, serverVersion: RuntimeServerVersion): RuntimeVersionModule = ServiceLoadedRuntimeModule
}

private object ServiceLoadedRuntimeModule : RuntimeVersionModule {
    override val id = "service-loaded-direct"
    override val displayName = id
    override val threadingPolicy = RuntimeThreadingPolicy.mainThreadOnly()
    override val capabilities = listOf(
        DiagnosticsStatus(RuntimeCapability.TITLES, RuntimeCapabilityStatus.AVAILABLE, "test module")
    )

    override fun sendTitleTimes(player: Player, times: Title.Times) = Unit

    override fun sendTitle(player: Player, title: Component) = Unit

    override fun sendSubtitle(player: Player, subtitle: Component) = Unit

    override fun showTitle(player: Player, title: Title) = Unit

    override fun sendActionBar(player: Player, actionBar: Component) = Unit

    override fun sendPlayerListHeaderAndFooter(player: Player, header: Component, footer: Component) = Unit

    override fun createSidebar(player: Player): RuntimeSidebar = ServiceLoadedRuntimeSidebar
}

private object ServiceLoadedRuntimeSidebar : RuntimeSidebar {
    override var title: String = ""

    override fun isAppliedTo(player: Player): Boolean = false

    override fun get(index: Int): String? = null

    override fun set(index: Int, value: String) = Unit

    override fun remove(index: Int) = Unit

    override fun close() = Unit
}
