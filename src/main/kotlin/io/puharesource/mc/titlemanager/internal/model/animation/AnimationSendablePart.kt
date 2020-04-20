package io.puharesource.mc.titlemanager.internal.model.animation

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import org.bukkit.entity.Player

data class AnimationSendablePart(
    private val player: Player,
    private val part: AnimationPart<Animation>,
    private var done: Boolean = false,
    private val isContinuous: Boolean
) : SendablePart {
    private var currentText = ""
    private var iterator = part.part.iterator(player)
    private var nextUpdateTick = 0

    override fun getCurrentText() = currentText

    override fun updateText(currentTick: Int) {
        if (nextUpdateTick > currentTick) return
        if (!iterator.hasNext() && !isContinuous) return

        if (!iterator.hasNext() && isContinuous) {
            iterator = part.part.iterator(player)
        }

        val frame = iterator.next()

        currentText = frame.text
        nextUpdateTick = (if (frame.totalTime <= 0) 1 else frame.totalTime) + currentTick

        if (!iterator.hasNext() && !isContinuous) {
            done = true
        }
    }

    override fun isDone() = done
}
