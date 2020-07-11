package io.puharesource.mc.titlemanager.internal.model.script.builtin

import io.puharesource.mc.titlemanager.internal.color.ColorUtil
import io.puharesource.mc.titlemanager.internal.model.script.AnimationScript
import net.md_5.bungee.api.ChatColor
import java.util.regex.Pattern
import kotlin.math.floor

class GradientColorScript(text: String, index: Int) : AnimationScript(text, index, fadeIn = 0, stay = 1, fadeOut = 0) {
    private val pattern: Pattern = """\[(?<colors>.+)](?<precision>\d+)""".toRegex().toPattern()

    private var colors: MutableList<ChatColor> = listOf("#ff0000", "#00ff00").map { ChatColor.of(it) }.toMutableList()
    private var precision: Int = 20

    private val percentage: Float
        get() = 1.0f / precision * (index % precision)

    private val colorIndex: Int
        get() = floor(index / precision.toFloat()).toInt()

    private val nextColorIndex: Int
        get() = floor((index + 1) / precision.toFloat()).toInt()

    private val startColor: ChatColor
        get() = colors[colorIndex]

    private val endColor: ChatColor
        get() = colors[colorIndex + 1]

    override fun generateFrame() {
        text = ColorUtil.gradientColor(startColor, endColor, percentage).toString()
        done = nextColorIndex + 1 >= colors.size
    }

    override fun decode() {
        super.decode()

        val matcher = pattern.matcher(text)

        if (matcher.find()) {
            colors = matcher.group("colors").split(",")
                .asSequence()
                .map { it.trim() }
                .map { ChatColor.of(it) }
                .toMutableList()
            text = matcher.group("precision")
            text.toIntOrNull()?.let { precision = it }
        }

        if (colors.size == 1) {
            colors.add(colors.first())
        }
    }
}
