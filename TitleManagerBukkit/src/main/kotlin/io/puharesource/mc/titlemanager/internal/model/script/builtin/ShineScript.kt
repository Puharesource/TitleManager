package io.puharesource.mc.titlemanager.internal.model.script.builtin

import io.puharesource.mc.titlemanager.internal.model.animation.StandardAnimationFrame
import io.puharesource.mc.titlemanager.internal.model.script.AnimationScript
import net.md_5.bungee.api.ChatColor
import java.util.regex.Pattern

class ShineScript(text: String, index: Int) : AnimationScript(text, index) {
    private val pattern: Pattern =
        """\[(?<mainColor>.+);(?<secondaryColor>.+)](?<text>.+)""".toRegex().toPattern()

    private var mainColor = ChatColor.GOLD.toString()
    private var secondaryColor = ChatColor.YELLOW.toString()

    override fun generateFrame() {
        val length = text.length
        text = manipulateText(length, mainColor, secondaryColor)
        done = index >= length + 3
    }

    override fun decode() {
        decodeTimings()
        decodeColors()
    }

    private fun decodeTimings() {
        for (i in 0..2) {
            val frame = StandardAnimationFrame.createFrame(text) ?: break

            text = frame.text

            if (i == 0 || (i == 1 && index == 0) || (i == 2 && index - 3 >= getShineTextLength(text))) {
                fadeIn = frame.fadeIn
                stay = frame.stay
                fadeOut = frame.fadeOut
            }
        }
    }

    private fun decodeColors() {
        val matcher = pattern.matcher(text)

        if (matcher.find()) {
            text = matcher.group("text")
            mainColor = matcher.group("mainColor")
            secondaryColor = matcher.group("secondaryColor")
        }
    }

    private fun manipulateText(length: Int, mainColor: String, secondaryColor: String): String {
        if (index == 0 || index >= length + 3) {
            return mainColor + text
        }

        var startIndex = index - 3
        if (startIndex < 0) {
            startIndex = 0
        }

        var endIndex = index
        if (endIndex >= text.length) {
            endIndex = text.length
        }

        val left = mainColor + text.substring(0, startIndex)
        val center = secondaryColor + text.substring(startIndex, endIndex)
        val right = mainColor + text.substring(endIndex)

        return left + center + right
    }

    private fun getShineTextLength(text: String): Int {
        val matcher = pattern.matcher(text)

        if (matcher.find()) {
            return matcher.group("text").length
        }

        return text.length
    }
}
