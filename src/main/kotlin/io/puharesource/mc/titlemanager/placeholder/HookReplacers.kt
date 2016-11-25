package io.puharesource.mc.titlemanager.placeholder

import org.bukkit.Bukkit
import org.bukkit.entity.Player

object VanishHookReplacer : HookReplacer() {
    override fun isValid(): Boolean {
        return EssentialsHook.isEnabled() || SuperVanishHook.isEnabled() || PremiumVanishHook.isEnabled() || VanishNoPacketHook.isEnabled()
    }

    override fun value(player: Player): String {
        return Bukkit.getOnlinePlayers()
                .filterNot { EssentialsHook.isPlayerVanished(it) }
                .filterNot { VanishNoPacketHook.isPlayerVanished(it) }
                .filterNot { PremiumVanishHook.isPlayerVanished(it) }
                .filterNot { SuperVanishHook.isPlayerVanished(it) }
                .size.toString()
    }
}