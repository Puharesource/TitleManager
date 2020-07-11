package io.puharesource.mc.titlemanager.internal.model.script.builtin

import io.puharesource.mc.titlemanager.internal.color.ChatColorGradient
import io.puharesource.mc.titlemanager.internal.color.ColorUtil
import io.puharesource.mc.titlemanager.internal.model.script.AnimationScript
import net.md_5.bungee.api.ChatColor
import java.util.regex.Pattern

class GradientScript(text: String, index: Int) : AnimationScript(text, index, fadeIn = 0, stay = 2, fadeOut = 0) {
    private val pattern: Pattern = """\[(?<colors>.+)](?<text>.+)""".toRegex().toPattern()

    private var colors: MutableList<ChatColor> = listOf("#ff0000", "#00ff00").map { ChatColor.of(it) }.toMutableList()
    private val gradient: ChatColorGradient
        get() = ChatColorGradient(colors)

    private val startColor: ChatColor
        get() = gradient.getColorAt(text.length, index)

    private val endColor: ChatColor
        get() = gradient.getColorAt(text.length, index + text.length)

    override fun generateFrame() {
        done = gradient.isOutOfBounds(text.length, index + 1)

        text = ColorUtil.gradientString(text, startColor, endColor)
    }

    override fun decode() {
        super.decode()

        val matcher = pattern.matcher(text)

        if (matcher.find()) {
            text = matcher.group("text")
            colors = matcher.group("colors").split(",")
                .asSequence()
                .map { it.trim() }
                .map { ChatColor.of(it) }
                .toMutableList()
        }

        if (colors.size == 1) {
            colors.add(colors.first())
        }
    }
}
