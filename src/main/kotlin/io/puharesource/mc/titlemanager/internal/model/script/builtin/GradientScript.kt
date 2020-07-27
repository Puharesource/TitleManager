package io.puharesource.mc.titlemanager.internal.model.script.builtin

import io.puharesource.mc.titlemanager.internal.color.ColorUtil
import io.puharesource.mc.titlemanager.internal.model.script.AnimationScript
import java.awt.Color
import java.util.regex.Pattern

class GradientScript(text: String, index: Int) : AnimationScript(text, index, fadeIn = 0, stay = 2, fadeOut = 0) {
    private val pattern: Pattern = """\[(?<colors>.+)](?<text>.+)""".toRegex().toPattern()
    private var colors = listOf("#ff0000", "#00ff00").map { Color.decode(it) }.toList()

    private var bold = false
    private var strikethrough = false
    private var underline = false
    private var magic = false

    override fun generateFrame() {
        done = index + 1 >= text.length
        text = ColorUtil.gradientString(text, colors,
            offset = index,
            continuous = true,
            bold = bold,
            strikethrough = strikethrough,
            underline = underline,
            magic = magic
        )
    }

    override fun decode() {
        super.decode()

        val matcher = pattern.matcher(text)

        if (matcher.find()) {
            text = matcher.group("text")
            colors = matcher.group("colors").split(",")
                .asSequence()
                .map { it.trim() }
                .filter { it.startsWith("#") }
                .map { Color.decode(it) }
                .toList()

            matcher.group("colors").split(",")
                .asSequence()
                .map { it.trim() }
                .filter { !it.startsWith("#") }
                .forEach {
                    when {
                        it.equals("bold", ignoreCase = true) -> {
                            bold = true
                        }
                        it.equals("strikethrough", ignoreCase = true) -> {
                            strikethrough = true
                        }
                        it.equals("underline", ignoreCase = true) -> {
                            underline = true
                        }
                        it.equals("magic", ignoreCase = true) -> {
                            magic = true
                        }
                    }
                }
        }
    }
}
