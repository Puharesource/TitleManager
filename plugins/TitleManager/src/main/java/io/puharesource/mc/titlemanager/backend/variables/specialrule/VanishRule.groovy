package io.puharesource.mc.titlemanager.backend.variables.specialrule

import io.puharesource.mc.titlemanager.TitleManager
import io.puharesource.mc.titlemanager.backend.hooks.essentials.EssentialsHook
import io.puharesource.mc.titlemanager.backend.hooks.vanishnopacket.VanishNoPacketHook
import io.puharesource.mc.titlemanager.backend.variables.VariableRule
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class VanishRule extends VariableRule {

    @Override
    boolean rule(Player player) {
        return getEssentials().isEnabled() || getVanishHook().isEnabled()
    }

    @Override
    String[] replace(Player player, String text) {
        return new String[0]
    }

    static int getOnlinePlayers() {
        int nonVanished = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getEssentials().isEnabled() && getEssentials().isPlayerVanished(player)) continue
            if (getVanishHook().isEnabled() && getVanishHook().isPlayerVanished(player)) continue
            nonVanished++
        }
        return nonVanished
    }

    private static EssentialsHook getEssentials() {
        TitleManager.getInstance().getHook("ESSENTIALS")
    }

    private static VanishNoPacketHook getVanishHook() {
        TitleManager.getInstance().getHook("VANISHNOPACKET")
    }
}
