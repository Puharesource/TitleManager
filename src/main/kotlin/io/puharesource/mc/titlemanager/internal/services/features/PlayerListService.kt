package io.puharesource.mc.titlemanager.internal.services.features

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import org.bukkit.entity.Player

interface PlayerListService {
    fun startPlayerTasks()

    fun createHeaderSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean = false): SendableAnimation
    fun createFooterSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean = false): SendableAnimation

    fun createHeaderSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean = false): SendableAnimation
    fun createFooterSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean = false): SendableAnimation

    fun getHeader(player: Player): String
    fun setHeader(player: Player, header: String, withPlaceholders: Boolean = false)
    fun setProcessedHeader(player: Player, header: String)

    fun getFooter(player: Player): String
    fun setFooter(player: Player, footer: String, withPlaceholders: Boolean = false)
    fun setProcessedFooter(player: Player, footer: String)

    fun setHeaderAndFooter(player: Player, header: String, footer: String, withPlaceholders: Boolean = false)
    fun setProcessedHeaderAndFooter(player: Player, header: String, footer: String)

    fun clearHeaderAndFooterCache(player: Player)
}
