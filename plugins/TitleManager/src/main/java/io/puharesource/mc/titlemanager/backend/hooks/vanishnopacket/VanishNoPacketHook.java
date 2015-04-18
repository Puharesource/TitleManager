package io.puharesource.mc.titlemanager.backend.hooks.vanishnopacket;

import io.puharesource.mc.titlemanager.backend.hooks.PluginHook;
import org.bukkit.entity.Player;
import org.kitteh.vanish.VanishPlugin;

public final class VanishNoPacketHook extends PluginHook {
    public VanishNoPacketHook() {
        super("VanishNoPacket");
    }

    public boolean isPlayerVanished(Player player) {
        return ((VanishPlugin) getPlugin()).getManager().isVanished(player);
    }
}
