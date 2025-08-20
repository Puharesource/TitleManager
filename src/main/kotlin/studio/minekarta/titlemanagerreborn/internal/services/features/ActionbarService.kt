package studio.minekarta.titlemanagerreborn.internal.services.features

import studio.minekarta.titlemanagerreborn.api.v2.animation.Animation
import studio.minekarta.titlemanagerreborn.api.v2.animation.AnimationPart
import studio.minekarta.titlemanagerreborn.api.v2.animation.SendableAnimation
import org.bukkit.entity.Player

interface ActionbarService {
    fun sendProcessedActionbar(player: Player, text: String)
    fun sendActionbar(player: Player, text: String, withPlaceholders: Boolean = false)
    fun clearActionbar(player: Player)

    fun createActionbarSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation
    fun createActionbarSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation
}
