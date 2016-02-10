package io.puharesource.mc.titlemanager.backend.hooks.supervanish;

import de.myzelyam.api.vanish.VanishAPI;
import io.puharesource.mc.titlemanager.backend.hooks.VanishPluginHook;
import org.bukkit.entity.Player;

public abstract class SuperVanishAPIHook extends VanishPluginHook {
    public SuperVanishAPIHook(final String pluginName) {
        super(pluginName);
    }

    @Override
    public boolean isPlayerVanished(final Player player) {
        return VanishAPI.isInvisible(player);
    }
}
