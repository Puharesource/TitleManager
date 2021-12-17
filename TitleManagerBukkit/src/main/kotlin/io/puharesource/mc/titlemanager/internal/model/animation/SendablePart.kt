package io.puharesource.mc.titlemanager.internal.model.animation

interface SendablePart {
    fun getCurrentText(): String
    fun updateText(currentTick: Int)
    fun isDone(): Boolean
}
