package io.puharesource.mc.titlemanager.internal.model.script.builtin

import io.puharesource.mc.titlemanager.internal.model.script.AnimationScript

class CountDownScript(text: String, index: Int) : AnimationScript(text, index, fadeIn = 0, stay = 20, fadeOut = 0) {
    override fun generateFrame() {
        val countdown = (text.toIntOrNull() ?: 1) - index

        text = countdown.toString()
        done = countdown <= 1
    }
}
