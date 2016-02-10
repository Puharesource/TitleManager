package io.puharesource.mc.titlemanager.backend.hooks.essentials;

import com.earth2me.essentials.Essentials;
import io.puharesource.mc.titlemanager.backend.hooks.VanishPluginHook;
import org.bukkit.entity.Player;

public final class EssentialsHook extends VanishPluginHook {
    public EssentialsHook() {
        super("Essentials");
    }

    @Override
    public boolean isPlayerVanished(final Player player) {
        return ((Essentials) getPlugin()).getUser(player).isVanished();
    }
}
