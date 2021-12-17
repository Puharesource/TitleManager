package io.puharesource.mc.titlemanager.internal.model.script.builtin

import io.puharesource.mc.titlemanager.internal.model.script.AnimationScript

class TextDeleteScript(text: String, index: Int) : AnimationScript(text, index, fadeIn = 0, stay = 5, fadeOut = 0) {
    override fun generateFrame() {
        done = index >= text.length
        text = text.substring(0, text.length - index)
    }
}
