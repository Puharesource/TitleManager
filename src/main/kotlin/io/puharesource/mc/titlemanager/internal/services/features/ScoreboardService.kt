package io.puharesource.mc.titlemanager.internal.services.features

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import org.bukkit.World
import org.bukkit.entity.Player

interface ScoreboardService {
    fun startPlayerTasks()
    fun stopPlayerTasks()

    fun hasScoreboard(player: Player): Boolean
    fun giveScoreboard(player: Player)
    fun giveDefaultScoreboard(player: Player)
    fun removeScoreboard(player: Player)

    fun getScoreboardTitle(player: Player): String?
    fun setScoreboardTitle(player: Player, title: String, withPlaceholders: Boolean = false)
    fun setProcessedScoreboardTitle(player: Player, title: String)

    fun getScoreboardValue(player: Player, index: Int): String?
    fun setScoreboardValue(player: Player, index: Int, value: String, withPlaceholders: Boolean = false)
    fun setProcessedScoreboardValue(player: Player, index: Int, value: String)
    fun removeScoreboardValue(player: Player, index: Int)

    fun createScoreboardTitleSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean = false): SendableAnimation
    fun createScoreboardValueSendableAnimation(animation: Animation, player: Player, index: Int, withPlaceholders: Boolean = false): SendableAnimation

    fun createScoreboardTitleSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean = false): SendableAnimation
    fun createScoreboardValueSendableAnimation(parts: List<AnimationPart<*>>, player: Player, index: Int, withPlaceholders: Boolean = false): SendableAnimation

    fun isScoreboardDisabledWorld(world: World): Boolean
    fun toggleScoreboardInWorld(player: Player, world: World)
}
