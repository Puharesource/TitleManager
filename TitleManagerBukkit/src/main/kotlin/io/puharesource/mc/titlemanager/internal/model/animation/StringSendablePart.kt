package io.puharesource.mc.titlemanager.internal.model.animation

import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart

data class StringSendablePart(private val part: AnimationPart<String>, private val isContinuous: Boolean) : SendablePart {
    override fun getCurrentText(): String = part.getPart()
    override fun updateText(currentTick: Int) {}
    override fun isDone() = !isContinuous
}
