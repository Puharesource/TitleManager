package studio.minekarta.titlemanagerreborn.internal

import studio.minekarta.titlemanagerreborn.TitleManagerReborn
import studio.minekarta.titlemanagerreborn.internal.config.TMConfigMain
import org.bukkit.Bukkit
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent

internal val pluginInstance: TitleManagerReborn by lazy {
    Bukkit.getPluginManager().getPlugin("TitleManager") as TitleManagerReborn
}

internal val pluginConfig: TMConfigMain by lazy {
    pluginInstance.tmConfig
}

internal fun info(message: String) = pluginInstance.logger.info(message)

internal fun debug(message: String) {
    if (pluginInstance.config.getBoolean("debug")) {
        info("[DEBUG] $message")
    }
}

internal fun onPluginDisable(body: () -> Unit) {
    val pluginManager = Bukkit.getPluginManager()
    val listener = object : Listener {}

    pluginManager.registerEvent(
        PluginDisableEvent::class.java,
        listener,
        EventPriority.MONITOR,
        { _, event ->
            val disableEvent = event as PluginDisableEvent

            if (disableEvent.plugin == pluginInstance) {
                body()
            }
        },
        pluginInstance,
        false
    )
}
