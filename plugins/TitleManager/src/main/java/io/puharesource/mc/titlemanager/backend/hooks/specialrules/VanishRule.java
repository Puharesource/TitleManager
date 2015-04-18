package io.puharesource.mc.titlemanager.backend.hooks.specialrules;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.hooks.essentials.EssentialsHook;
import io.puharesource.mc.titlemanager.backend.hooks.vanishnopacket.VanishNoPacketHook;
import io.puharesource.mc.titlemanager.api.variables.VariableRule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class VanishRule extends VariableRule {
    @Override
    public boolean rule(Player player) {
        return getEssentials().isEnabled() || getVanishHook().isEnabled();
    }

    @Override
    public String[] replace(Player player, String text) {
        return new String[0];
    }

    public static int getOnlinePlayers() {
        int nonVanished = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getEssentials().isEnabled() && getEssentials().isPlayerVanished(player)) continue;
            if (getVanishHook().isEnabled() && getVanishHook().isPlayerVanished(player)) continue;
            nonVanished++;
        }

        return nonVanished;
    }

    private static EssentialsHook getEssentials() {
        return ((EssentialsHook) (TitleManager.getInstance().getVariableManager().getHook("ESSENTIALS")));
    }

    private static VanishNoPacketHook getVanishHook() {
        return ((VanishNoPacketHook) (TitleManager.getInstance().getVariableManager().getHook("VANISHNOPACKET")));
    }
}
