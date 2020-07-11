package io.puharesource.mc.titlemanager.internal.model.script

import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.internal.model.animation.StandardAnimationFrame

abstract class AnimationScript(protected var text: String, protected var index: Int, protected var fadeIn: Int = 0, protected var stay: Int = 20, protected var fadeOut: Int = 0) {
    abstract fun generateFrame()

    var done: Boolean = false
        protected set

    val result: ScriptResult
        get() = ScriptResult(text, done, fadeIn, stay, fadeOut)

    open fun decode() {
        StandardAnimationFrame.createFrame(text)?.let { loadFrame(it) }
    }

    protected fun loadFrame(frame: AnimationFrame) {
        text = frame.text
        fadeIn = frame.fadeIn
        stay = frame.stay
        fadeOut = frame.fadeOut
    }
}
