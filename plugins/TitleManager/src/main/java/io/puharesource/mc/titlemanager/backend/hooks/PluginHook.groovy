package io.puharesource.mc.titlemanager.backend.hooks

import org.bukkit.Bukkit

abstract class PluginHook {

    private String pluginName

    PluginHook(String pluginName) {
        this.pluginName = pluginName
    }

    boolean isEnabled() {
        Bukkit.getPluginManager().isPluginEnabled(pluginName)
    }
}
