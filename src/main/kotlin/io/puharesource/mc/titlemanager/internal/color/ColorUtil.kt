package io.puharesource.mc.titlemanager.internal.color

import net.md_5.bungee.api.ChatColor
import java.awt.Color

object ColorUtil {
    fun gradientString(text: String, gradient: List<Color>, offset: Int = 0, continuous: Boolean = false, bold: Boolean = false, strikethrough: Boolean = false, underline: Boolean = false, magic: Boolean = false): String {
        val sb = StringBuilder()
        val interpolator = InterpolatorUtil.createRgbGradientInterpolator(gradient, continuous)

        for (i in text.indices) {
            val offsetIndex = (i + offset) % text.length
            val percentage = offsetIndex.toFloat() / text.length.toFloat()

            val color = interpolator.interpolate(percentage).toChatColor()
            sb.append(color)

            if (bold) {
                sb.append(ChatColor.BOLD)
            }

            if (strikethrough) {
                sb.append(ChatColor.STRIKETHROUGH)
            }

            if (underline) {
                sb.append(ChatColor.UNDERLINE)
            }

            if (magic) {
                sb.append(ChatColor.MAGIC)
            }

            sb.append(text[i])
        }

        return sb.toString()
    }

    private fun ChatColor.toColor(): Color {
        if (!this.name.startsWith("#")) throw IllegalArgumentException()

        return Color.decode(this.name)
    }

    private fun Color.toChatColor(): ChatColor = ChatColor.of(this)
}
