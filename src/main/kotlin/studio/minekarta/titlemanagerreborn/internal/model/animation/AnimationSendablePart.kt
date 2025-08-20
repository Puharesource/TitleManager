package studio.minekarta.titlemanagerreborn.internal.model.animation

import studio.minekarta.titlemanagerreborn.api.v2.animation.Animation
import studio.minekarta.titlemanagerreborn.api.v2.animation.AnimationPart
import org.bukkit.entity.Player

data class AnimationSendablePart(
    private val player: Player,
    private val part: AnimationPart<Animation>,
    private var done: Boolean = false,
    private val isContinuous: Boolean
) : SendablePart {
    private var currentText = ""
    private var iterator = part.getPart().iterator(player)
    private var nextUpdateTick = 0

    override fun getCurrentText() = currentText

    override fun updateText(currentTick: Int) {
        if (nextUpdateTick > currentTick) return
        if (!iterator.hasNext() && !isContinuous) return

        if (!iterator.hasNext() && isContinuous) {
            iterator = part.getPart().iterator(player)
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
