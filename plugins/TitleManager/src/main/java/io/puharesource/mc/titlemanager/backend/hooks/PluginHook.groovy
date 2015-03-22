package io.puharesource.mc.titlemanager.backend.hooks

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

abstract class PluginHook {

    private String pluginName

    PluginHook(String pluginName) {
        this.pluginName = pluginName
    }

    boolean isEnabled() { Bukkit.getPluginManager().isPluginEnabled(pluginName) }

    Plugin getPlugin() { Bukkit.getPluginManager().getPlugin(pluginName) }
}
