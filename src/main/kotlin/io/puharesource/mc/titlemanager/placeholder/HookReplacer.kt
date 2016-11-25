package io.puharesource.mc.titlemanager.placeholder

import org.bukkit.entity.Player

abstract class HookReplacer {
    abstract fun isValid(): Boolean

    abstract fun value(player: Player): String
}