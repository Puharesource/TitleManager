package studio.minekarta.titlemanagerreborn.internal.placeholder

import studio.minekarta.titlemanagerreborn.internal.debug
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

abstract class PluginHook constructor(private val pluginName: String) {
    init {
        if (!isEnabled()) {
            debug("$pluginName is not enabled, disabling placeholders related to the plugin.")
        }
    }

    open fun infoLog() {
    }

    fun isEnabled(): Boolean = Bukkit.getPluginManager().isPluginEnabled(pluginName)

    fun getPlugin(): Plugin = Bukkit.getPluginManager().getPlugin(pluginName)!!
}
