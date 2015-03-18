package io.puharesource.mc.titlemanager.backend.hooks

import org.bukkit.Bukkit

abstract class PluginHook {

    String pluginName

    private static def instance

    PluginHook(String pluginName) {
        this.pluginName = pluginName
    }

    boolean isEnabled() {
        Bukkit.getPluginManager().isPluginEnabled(pluginName)
    }

    static PluginHook getInstance() { instance ?: (instance = this.class.newInstance()) }
}
