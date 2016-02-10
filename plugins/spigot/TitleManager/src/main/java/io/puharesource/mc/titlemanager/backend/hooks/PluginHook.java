package io.puharesource.mc.titlemanager.backend.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class PluginHook {
    private String pluginName;

    public PluginHook(String pluginName) {
        this.pluginName = pluginName;
    }

    public boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(pluginName);
    }
}
