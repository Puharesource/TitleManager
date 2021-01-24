package io.puharesource.mc.titlemanager.internal.model.script.builtin

import io.puharesource.mc.titlemanager.internal.model.script.AnimationScript
import java.util.regex.Pattern

class RepeatScript(text: String, index: Int) : AnimationScript(text, index, fadeIn = 0, stay = 20, fadeOut = 0) {
    private val pattern: Pattern =
        """\[(?<totalStay>\d+)](?<text>.+)""".toRegex().toPattern()

    private var totalStay = 60

    override fun generateFrame() {
        val indexStay = index * 20

        if (totalStay - indexStay <= 0) {
            text = " "
            stay = 1
            done = true
        } else if (totalStay - indexStay - 20 < 20) {
            stay = totalStay - indexStay - 20
        }
    }

    override fun decode() {
        val matcher = pattern.matcher(text)

        if (matcher.find()) {
            totalStay = matcher.group("totalStay").toIntOrNull() ?: 60
            text = matcher.group("text")
        }
    }
}
