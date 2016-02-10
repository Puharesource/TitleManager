package io.puharesource.mc.titlemanager.backend.hooks;

import org.bukkit.entity.Player;

public abstract class VanishPluginHook extends PluginHook {
    public VanishPluginHook(final String pluginName) {
        super(pluginName);
    }

    public abstract boolean isPlayerVanished(final Player player);
}
