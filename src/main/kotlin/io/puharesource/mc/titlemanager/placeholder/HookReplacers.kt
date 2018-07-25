package io.puharesource.mc.titlemanager.placeholder

import org.bukkit.Bukkit
import org.bukkit.entity.Player

object VanishHookReplacer : HookReplacer() {
    override fun isValid(): Boolean {
        return EssentialsHook.isEnabled() || SuperVanishHook.isEnabled() || PremiumVanishHook.isEnabled() || VanishNoPacketHook.isEnabled()
    }

    override fun value(player: Player): String {
        return Bukkit.getOnlinePlayers().asSequence()
                .filterNot { EssentialsHook.isPlayerVanished(it) }
                .filterNot { VanishNoPacketHook.isPlayerVanished(it) }
                .filterNot {
                    try {
                        return@filterNot PremiumVanishHook.isPlayerVanished(it)
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }

                    return@filterNot false
                }
                .filterNot {
                    try {
                        return@filterNot SuperVanishHook.isPlayerVanished(it)
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }

                    return@filterNot false
                }
                .count().toString()
    }
}