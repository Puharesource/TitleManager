package dev.tarkan.titlemanager.color

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Platform-neutral HSL color representation using normalized channel values.
 */
data class HslColor(val H: Double, val S: Double, val L: Double)

/**
 * Extension property to convert an RGB integer value to an HSL color representation.
 *
 * @receiver The RGB color value as an integer.
 * @return The corresponding [HslColor] representation of the RGB color.
 */
val Int.hsl: HslColor
    get() {
        val red = ((this shr 16) and 0xff) / 255.0
        val green = ((this shr 8) and 0xff) / 255.0
        val blue =  (this and 0xff) / 255.0

        val max = max(red, max(green, blue))
        val min = min(red, min(green, blue))
        val lightness = (max + min) / 2.0

        if (max == min) {
            return HslColor(0.0, 0.0, lightness)
        }

        val delta = max - min
        val saturation = delta / (1.0 - abs(2.0 * lightness - 1.0))
        val hue = when (max) {
            red -> ((green - blue) / delta).mod(6.0) / 6.0
            green -> (((blue - red) / delta) + 2.0) / 6.0
            else -> (((red - green) / delta) + 4.0) / 6.0
        }

        return HslColor(hue, saturation, lightness)
    }

val HslColor.rgb: Int
    get() {
        val normalizedHue = H.mod(1.0)
        val normalizedSaturation = S.coerceIn(0.0, 1.0)
        val normalizedLightness = L.coerceIn(0.0, 1.0)

        val red: Double
        val green: Double
        val blue: Double

        if (normalizedSaturation == 0.0) {
            red = normalizedLightness
            green = normalizedLightness
            blue = normalizedLightness
        } else {
            val q = if (normalizedLightness < 0.5) {
                normalizedLightness * (1.0 + normalizedSaturation)
            } else {
                normalizedLightness + normalizedSaturation - normalizedLightness * normalizedSaturation
            }
            val p = 2.0 * normalizedLightness - q

            red = hueToRgb(p, q, normalizedHue + 1.0 / 3.0)
            green = hueToRgb(p, q, normalizedHue)
            blue = hueToRgb(p, q, normalizedHue - 1.0 / 3.0)
        }

        return (red.toRgbChannel() shl 16) or (green.toRgbChannel() shl 8) or blue.toRgbChannel()
    }

private fun hueToRgb(p: Double, q: Double, hue: Double): Double {
    val normalizedHue = hue.mod(1.0)

    return when {
        normalizedHue < 1.0 / 6.0 -> p + (q - p) * 6.0 * normalizedHue
        normalizedHue < 1.0 / 2.0 -> q
        normalizedHue < 2.0 / 3.0 -> p + (q - p) * (2.0 / 3.0 - normalizedHue) * 6.0
        else -> p
    }
}

private fun Double.toRgbChannel() = (this.coerceIn(0.0, 1.0) * 255.0).roundToInt().coerceIn(0, 255)
