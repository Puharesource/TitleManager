package io.puharesource.mc.titlemanager.internal.services.features

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import org.bukkit.entity.Player

interface TitleService {
    fun sendTitle(player: Player, title: String, fadeIn: Int = -1, stay: Int = -1, fadeOut: Int = -1, withPlaceholders: Boolean = false)
    fun sendSubtitle(player: Player, subtitle: String, fadeIn: Int = -1, stay: Int = -1, fadeOut: Int = -1, withPlaceholders: Boolean = false)
    fun sendTitles(player: Player, title: String, subtitle: String, fadeIn: Int = -1, stay: Int = -1, fadeOut: Int = -1, withPlaceholders: Boolean = false)
    fun sendTimings(player: Player, fadeIn: Int, stay: Int, fadeOut: Int)
    fun clearTitle(player: Player)
    fun clearSubtitle(player: Player)
    fun clearTitles(player: Player)

    fun sendProcessedTitle(player: Player, text: String, fadeIn: Int = 0, stay: Int = 0, fadeOut: Int = 0)
    fun sendProcessedSubtitle(player: Player, text: String, fadeIn: Int = 0, stay: Int = 0, fadeOut: Int = 0)

    fun createTitleSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation
    fun createSubtitleSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation

    fun createTitleSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation
    fun createSubtitleSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation
}
