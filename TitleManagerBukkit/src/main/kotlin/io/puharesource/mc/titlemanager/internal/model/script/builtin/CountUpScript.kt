package io.puharesource.mc.titlemanager.internal.model.script.builtin

import io.puharesource.mc.titlemanager.internal.model.script.AnimationScript

class CountUpScript(text: String, index: Int) : AnimationScript(text, index, fadeIn = 0, stay = 20, fadeOut = 0) {
    override fun generateFrame() {
        val limit = text.toIntOrNull() ?: 1
        val number = index + 1

        text = number.toString()
        done = number >= limit
    }
}
