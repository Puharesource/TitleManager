package dev.tarkan.titlemanager.lib.color

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.math.abs
import kotlin.math.round

@JsExport
@ExperimentalJsExport
data class HslColor(var hue: Float, var saturation: Float, var lightness: Float) {
    companion object {
        fun fromColor(color: Color) = fromRgb(color.red, color.green, color.blue)

        @JsName("fromRgbByte")
        fun fromRgb(red: ColorByte, green: ColorByte, blue: ColorByte) = fromRgb(red.toColorFloat(), green.toColorFloat(), blue.toColorFloat())

        @JsName("fromRgbFloat")
        fun fromRgb(red: ColorFloat, green: ColorFloat, blue: ColorFloat): HslColor {
            val max = red.value.coerceAtLeast(green.value.coerceAtLeast(blue.value))
            val min = red.value.coerceAtMost(green.value.coerceAtMost(blue.value))
            val delta = max - min

            var hue = 0.0f
            var saturation = 0.0f
            val lightness = (max + min) / 2.0f

            if (max != min) {
                hue = when (max) {
                    red.value -> {
                        ((green.value - blue.value) / delta) % 6.0f
                    }
                    green.value -> {
                        (blue.value - red.value) / delta + 2.0f
                    }
                    else -> {
                        (red.value - green.value) / delta + 4.0f
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

        val redValue = ColorByte(red.coerceIn(0, 255).toUByte())
        val greenValue = ColorByte(green.coerceIn(0, 255).toUByte())
        val blueValue = ColorByte(blue.coerceIn(0, 255).toUByte())

        return Color(redValue, greenValue, blueValue)
    }
}
