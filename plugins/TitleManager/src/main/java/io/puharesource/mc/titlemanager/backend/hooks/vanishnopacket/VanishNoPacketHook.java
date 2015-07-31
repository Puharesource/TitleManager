package io.puharesource.mc.titlemanager.backend.hooks.vanishnopacket;

import io.puharesource.mc.titlemanager.backend.hooks.VanishPluginHook;
import org.bukkit.entity.Player;
import org.kitteh.vanish.VanishPlugin;

public final class VanishNoPacketHook extends VanishPluginHook {
    public VanishNoPacketHook() {
        super("VanishNoPacket");
    }

    @Override
    public boolean isPlayerVanished(Player player) {
        return ((VanishPlugin) getPlugin()).getManager().isVanished(player);
    }
}
