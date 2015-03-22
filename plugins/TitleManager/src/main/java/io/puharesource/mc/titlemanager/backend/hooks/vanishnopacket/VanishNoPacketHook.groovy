package io.puharesource.mc.titlemanager.backend.hooks.vanishnopacket

import io.puharesource.mc.titlemanager.backend.hooks.PluginHook
import org.bukkit.entity.Player
import org.kitteh.vanish.VanishPlugin

final class VanishNoPacketHook extends PluginHook {
    VanishNoPacketHook() {
        super("VanishNoPacket")
    }

    boolean isPlayerVanished(Player player) { ((VanishPlugin) getPlugin()).getManager().isVanished(player) }
}
