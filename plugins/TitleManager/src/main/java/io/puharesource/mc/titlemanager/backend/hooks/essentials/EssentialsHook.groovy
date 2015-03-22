package io.puharesource.mc.titlemanager.backend.hooks.essentials

import com.earth2me.essentials.Essentials
import io.puharesource.mc.titlemanager.backend.hooks.PluginHook
import org.bukkit.entity.Player

final class EssentialsHook extends PluginHook {
    EssentialsHook() {
        super("Essentials")
    }

    boolean isPlayerVanished(Player player) { ((Essentials) getPlugin()).getUser(player).isVanished() }
}
