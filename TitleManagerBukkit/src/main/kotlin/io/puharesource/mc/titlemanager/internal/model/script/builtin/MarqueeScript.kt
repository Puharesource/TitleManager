package io.puharesource.mc.titlemanager.internal.model.script.builtin

import io.puharesource.mc.titlemanager.internal.model.script.AnimationScript
import java.util.regex.Pattern

class MarqueeScript(text: String, index: Int) : AnimationScript(text, index, fadeIn = 0, stay = 5, fadeOut = 0) {
    private val pattern: Pattern =
        """\[(?<width>\d+)](?<text>.+)""".toRegex().toPattern()

    private var width: Int = text.length

    override fun generateFrame() {
        val marqueeText = StringBuilder()
        val chars = text.toCharArray()

        for (i in 0 until width) {
            marqueeText.append(chars[(index + i) % text.length])
        }

        text = marqueeText.toString()
        done = index >= text.length
    }

    override fun decode() {
        super.decode()

        val matcher = pattern.matcher(text)

        if (matcher.find()) {
            width = matcher.group("width").toInt()
            text = matcher.group("text")
        }

        if (width <= 0 || width > text.length) {
            width = text.length
        }
    }
}
