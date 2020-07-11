package io.puharesource.mc.titlemanager.internal.color

import net.md_5.bungee.api.ChatColor
import java.awt.Color
import kotlin.math.floor

object ColorUtil {
    fun gradientColor(startChatColor: ChatColor, endChatColor: ChatColor, percentage: Float): ChatColor {
        val startColor = startChatColor.toColor()
        val endColor = endChatColor.toColor()

        return Color(
            interpolate(startColor.red, endColor.red, percentage),
            interpolate(startColor.green, endColor.green, percentage),
            interpolate(startColor.blue, endColor.blue, percentage)
        ).toChatColor()
    }

    fun gradientString(text: String, startChatColor: ChatColor, endChatColor: ChatColor, offset: Int = 0): String {
        val sb = StringBuilder()

        for (i in text.indices) {
            val offsetIndex = (i + offset) % text.length
            val percentage = offsetIndex.toFloat() / text.length.toFloat()

            val color = gradientColor(startChatColor, endChatColor, percentage)
            sb.append(color)
            sb.append(text[i])
        }

        return sb.toString()
    }

    fun gradientString(text: String, gradient: ChatColorGradient, offset: Int = 0): String {
        val sb = StringBuilder()

        for (i in text.indices) {
            val offsetIndex = (i + offset) % text.length
            val percentage = offsetIndex.toFloat() / text.length.toFloat()

            val color = gradient.getColorAt(percentage)
            sb.append(color)
            sb.append(text[i])
        }

        return sb.toString()
    }

    private fun ChatColor.toColor(): Color {
        if (!this.name.startsWith("#")) throw IllegalArgumentException()

        return Color.decode(this.name)
    }

    private fun Color.toChatColor(): ChatColor = ChatColor.of(this)

    private fun interpolate(start: Int, end: Int, percentage: Float) = interpolate(start.toFloat(), end.toFloat(), percentage)
    private fun interpolate(start: Float, end: Float, percentage: Float) = floor(start * (1.0f - percentage) + end * percentage).toInt()
}
