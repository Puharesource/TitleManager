package studio.minekarta.titlemanagerreborn.internal.model.animation

import studio.minekarta.titlemanagerreborn.api.v2.animation.AnimationPart

data class StringSendablePart(private val part: AnimationPart<String>, private val isContinuous: Boolean) : SendablePart {
    override fun getCurrentText(): String = part.getPart()
    override fun updateText(currentTick: Int) {}
    override fun isDone() = !isContinuous
}
