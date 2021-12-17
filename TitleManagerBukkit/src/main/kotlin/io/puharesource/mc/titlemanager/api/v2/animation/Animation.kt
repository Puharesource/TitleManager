package io.puharesource.mc.titlemanager.api.v2.animation

import org.bukkit.entity.Player

/**
 * @since 2.0.0
 */
fun interface Animation {
    fun iterator(player: Player): Iterator<AnimationFrame>
}
