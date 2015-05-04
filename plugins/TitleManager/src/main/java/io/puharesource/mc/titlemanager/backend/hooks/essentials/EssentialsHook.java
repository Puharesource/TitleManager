package io.puharesource.mc.titlemanager.backend.hooks.essentials;

import com.earth2me.essentials.Essentials;
import io.puharesource.mc.titlemanager.backend.hooks.PluginHook;
import org.bukkit.entity.Player;

public final class EssentialsHook extends PluginHook {
    public EssentialsHook() {
        super("Essentials");
    }

    public boolean isPlayerVanished(Player player) {
        return ((Essentials) getPlugin()).getUser(player).isVanished();
    }

}
