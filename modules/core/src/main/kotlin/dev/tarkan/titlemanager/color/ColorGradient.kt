package dev.tarkan.titlemanager.color

import kotlin.math.abs

/**
 * Abstract class representing a color gradient, capable of interpolating between multiple colors.
 *
 * @param T The type of color used in the gradient.
 * @property colors The list of colors that make up the gradient.
 * @constructor Ensures the gradient has at least two colors for interpolation.
 */
abstract class ColorGradient<T>(protected val colors: List<T>) {
    companion object {
        /**
         * Creates an HSL color gradient using the specified colors.
         *
         * @param colors The HSL colors to use in the gradient.
         * @return An [HslColorGradient] instance.
         */
        fun hsl(vararg colors: HslColor) = HslColorGradient(colors.toList())

        /**
         * Creates a long HSL color gradient using the specified colors.
         *
         * @param colors The HSL colors to use in the gradient.
         * @return A [LongHslColorGradient] instance.
         */
        fun longHsl(vararg colors: HslColor) = LongHslColorGradient(colors.toList())
    }

    init {
        require(colors.size >= 2) { "The gradient must have at least 2 colors to interpolate between" }
    }

    /**
     * Interpolates between colors in the gradient.
     *
     * @param position A value between 0 and 1 representing the position in the gradient.
     * @return The interpolated color.
     */
    abstract fun interpolate(position: Float): T

    /**
     * Performs linear interpolation between two values.
     *
     * @param start The starting value.
     * @param end The ending value.
     * @param t A value between 0 and 1 representing the interpolation position.
     * @return The interpolated value.
     */
    protected fun lerp(start: Double, end: Double, t: Float): Double {
        if (t <= 0) {
            return start
        }

        if (t >= 1) {
            return end
        }

        // return (1 - t) * start + t * end
        return start + t * (end - start)
    }

    /**
     * Determines the two colors to interpolate between and the adjusted position.
     *
     * @param position The original position in the gradient.
     * @return An [AdjustedColorsAndPosition] containing the start color, end color, and adjusted position.
     */
    protected fun adjust(position: Float): AdjustedColorsAndPosition<T> {
        val numberOfColors = colors.size
        val segment = 1f / (numberOfColors - 1f)

        val normalizedPosition = position.coerceIn(0f, 1f)
        val segmentIndex = (normalizedPosition * (numberOfColors - 1)).toInt().coerceAtMost(numberOfColors - 2)

        val startColor = colors[segmentIndex]
        val endColor = colors[segmentIndex + 1]
        val t = (normalizedPosition - segmentIndex * segment) / segment

        return AdjustedColorsAndPosition(startColor, endColor, t)
    }

    /**
     * Data class representing the adjusted start color, end color, and position for interpolation.
     */
    protected data class AdjustedColorsAndPosition<T>(val start: T, val end: T, val position: Float)
}

/**
 * A color gradient using the HSL (Hue, Saturation, Lightness) color model.
 */
class HslColorGradient(colors: List<HslColor>) : ColorGradient<HslColor>(colors) {
    override fun interpolate(position: Float): HslColor {
        val currentValues = adjust(if (position == Float.POSITIVE_INFINITY || position == Float.NEGATIVE_INFINITY) 0f else position)

        val hue = interpolateHue(currentValues.start.H, currentValues.end.H, currentValues.position)
        val saturation = lerp(currentValues.start.S, currentValues.end.S, currentValues.position)
        val lightness = lerp(currentValues.start.L, currentValues.end.L, currentValues.position)

        return HslColor(hue, saturation, lightness)
    }

    private fun interpolateHue(start: Double, end: Double, position: Float): Double {
        var startDegrees = start * 360
        var endDegrees = end * 360

        val distance = abs(endDegrees - startDegrees)

        if (distance > 180) {
            if (end > start) {
                startDegrees += 360
            } else {
                endDegrees += 360
            }
        }

        return (lerp(startDegrees, endDegrees, position) % 360) / 360
    }
}


/**
 * A color gradient using the HSL color model with "long" interpolation logic for hue.
 */
class LongHslColorGradient(colors: List<HslColor>) : ColorGradient<HslColor>(colors) {
    override fun interpolate(position: Float): HslColor {
        val currentValues = adjust(if (position == Float.POSITIVE_INFINITY || position == Float.NEGATIVE_INFINITY) 0f else position)

        val hue = interpolateHue(currentValues.start.H, currentValues.end.H, currentValues.position)
        val saturation = lerp(currentValues.start.S, currentValues.end.S, currentValues.position)
        val lightness = lerp(currentValues.start.L, currentValues.end.L, currentValues.position)

        return HslColor(hue, saturation, lightness)
    }

    private fun interpolateHue(start: Double, end: Double, position: Float): Double {
        var startDegrees = start * 360
        var endDegrees = end * 360

        val distance = abs(endDegrees - startDegrees)

        if (distance < 180) {
            if (end < start) {
                startDegrees += 360
            } else {
                endDegrees += 360
            }
        }

        return (lerp(startDegrees, endDegrees, position) % 360) / 360
    }
}