package io.puharesource.mc.titlemanager.internal.color

import net.md_5.bungee.api.ChatColor
import java.awt.Color
import kotlin.math.abs
import kotlin.math.round

data class HslColor(var hue: Float, var saturation: Float, var lightness: Float) {
    companion object {
        fun fromChatColor(chatColor: ChatColor) = fromColor(Color.decode(chatColor.name))

        fun fromColor(color: Color) = fromRgb(color.red, color.green, color.blue)

        fun fromRgb(red: Int, green: Int, blue: Int) = fromRgb(red / 255.0f, green / 255.0f, blue / 255.0f)

        fun fromRgb(red: Float, green: Float, blue: Float): HslColor {
            val max = red.coerceAtLeast(green.coerceAtLeast(blue))
            val min = red.coerceAtMost(green.coerceAtMost(blue))
            val delta = max - min

            var hue = 0.0f
            var saturation = 0.0f
            val lightness = (max + min) / 2.0f

            if (max != min) {
                hue = when (max) {
                    red -> {
                        ((green - blue) / delta) % 6.0f
                    }
                    green -> {
                        (blue - red) / delta + 2.0f
                    }
                    else -> {
                        (red - green) / delta + 4.0f
                    }
                }

                saturation = delta / (1.0f - abs(2.0f * lightness - 1.0f))
            }

            hue = (hue * 60.0f) % 360.0f
            if (hue < 0) {
                hue += 360.0f
            }

            return HslColor(
                hue.coerceIn(0.0f, 360.0f),
                saturation.coerceIn(0.0f, 1.0f),
                lightness.coerceIn(0.0f, 1.0f)
            )
        }
    }

    fun toColor(): Color {
        val c = (1.0f - abs(2.0f * lightness - 1.0f)) * saturation
        val m = lightness - 0.5f * c
        val x = c * (1.0f - abs((hue / 60.0f % 2.0f) - 1.0f))

        val hueSegment = round(hue / 60).toInt()

        var red = 0
        var green = 0
        var blue = 0

        when (hueSegment) {
            0 -> {
                red = round(255 * (c + m)).toInt()
                green = round(255 * (x + m)).toInt()
                blue = round(255 * m).toInt()
            }
            1 -> {
                red = round(255 * (x + m)).toInt()
                green = round(255 * (c + m)).toInt()
                blue = round(255 * m).toInt()
            }
            2 -> {
                red = round(255 * m).toInt()
                green = round(255 * (c + m)).toInt()
                blue = round(255 * (x + m)).toInt()
            }
            3 -> {
                red = round(255 * m).toInt()
                green = round(255 * (x + m)).toInt()
                blue = round(255 * (c + m)).toInt()
            }
            4 -> {
                red = round(255 * (x + m)).toInt()
                green = round(255 * m).toInt()
                blue = round(255 * (c + m)).toInt()
            }
            5, 6 -> {
                red = round(255 * (c + m)).toInt()
                green = round(255 * m).toInt()
                blue = round(255 * (x + m)).toInt()
            }
        }

        red = red.coerceIn(0, 255)
        green = green.coerceIn(0, 255)
        blue = blue.coerceIn(0, 255)

        return Color(red, green, blue)
    }

    fun toChatColor() = ChatColor.of(toColor())
}
