package io.puharesource.mc.titlemanager.internal.color

import java.awt.Color
import kotlin.math.floor
import kotlin.math.round

object InterpolatorUtil {
    fun createConstantInterpolator(value: Float) = createInterpolator { value }
    fun createConstantHslInterpolator(hslColor: HslColor) = createInterpolator { hslColor }
    fun createConstantRgbInterpolator(color: Color) = createInterpolator { color }

    fun createRgbInterpolator(start: Int, end: Int) = createRgbInterpolator(start.toFloat(), end.toFloat())

    fun createRgbInterpolator(start: Float, end: Float) = createInterpolator { percentage ->
        floor(start * (1.0f - percentage) + end * percentage)
    }

    fun createRgbInterpolator(start: Color, end: Color): Interpolator<Color> {
        val redInterpolator = createRgbInterpolator(start.red, end.red)
        val greenInterpolator = createRgbInterpolator(start.green, end.green)
        val blueInterpolator = createRgbInterpolator(start.blue, end.blue)

        return createInterpolator { percentage ->
            Color(
                redInterpolator.interpolate(percentage).toInt(),
                greenInterpolator.interpolate(percentage).toInt(),
                blueInterpolator.interpolate(percentage).toInt()
            )
        }
    }

    fun createRgbGradientInterpolator(gradientColors: List<Color>, continuous: Boolean = false): Interpolator<Color> {
        val colors = gradientColors.toMutableList()

        if (colors.isEmpty()) {
            throw IllegalArgumentException("Parameter 'colors' cannot be empty")
        }

        if (colors.size == 1) {
            return createConstantRgbInterpolator(colors.first())
        }

        if (continuous) {
            colors.addAll(colors.subList(0, colors.size - 2).reversed())
        }

        if (colors.size == 2) {
            return createRgbInterpolator(colors.first(), colors[1])
        }

        return createInterpolator { percentage ->
            val startIndex = floor((colors.size - 1) * percentage).toInt()
            val endIndex = startIndex + 1

            val interpolator = createRgbInterpolator(colors[startIndex], colors[endIndex])
            val adjustedPercentage = ((colors.size - 1) * percentage) % 1.0f

            return@createInterpolator interpolator.interpolate(adjustedPercentage)
        }
    }

    fun createLinearInterpolator(start: Float, distance: Float) = createInterpolator { percentage ->
        start + percentage * distance
    }

    fun createNoGammaInterpolator(start: Float, end: Float): Interpolator<Float> {
        val distance = end - start

        if (distance == 0.0f) {
            return createConstantInterpolator(start)
        }

        return createLinearInterpolator(start, distance)
    }

    fun createHueInterpolator(start: Float, end: Float): Interpolator<Float> {
        var distance = end - start

        if (distance == 0.0f) {
            return createConstantInterpolator(start)
        }

        if (distance > 180.0f || distance < -180.0f) {
            distance -= 360.0f * round(distance / 360.0f)
        }

        return createLinearInterpolator(start, distance)
    }

    fun createHslInterpolator(start: HslColor, end: HslColor): Interpolator<HslColor> {
        val hueInterpolator = createHueInterpolator(start.hue, end.hue)
        val saturationInterpolator = createNoGammaInterpolator(start.saturation, end.saturation)
        val lightnessInterpolator = createNoGammaInterpolator(start.lightness, end.lightness)

        return createInterpolator { percentage ->
            HslColor(
                hueInterpolator.interpolate(percentage),
                saturationInterpolator.interpolate(percentage),
                lightnessInterpolator.interpolate(percentage)
            )
        }
    }

    fun createHslGradientInterpolator(gradientColors: List<HslColor>, continuous: Boolean = false): Interpolator<HslColor> {
        val colors = gradientColors.toMutableList()

        if (colors.isEmpty()) {
            throw IllegalArgumentException("Parameter 'colors' cannot be empty")
        }

        if (colors.size == 1) {
            return createConstantHslInterpolator(colors.first())
        }

        if (continuous) {
            colors.addAll(colors.subList(0, colors.size - 2).reversed())
        }

        if (colors.size == 2) {
            return createHslInterpolator(colors.first(), colors[1])
        }

        return createInterpolator { percentage ->
            val startIndex = floor((colors.size - 1) * percentage).toInt()
            val endIndex = startIndex + 1

            val interpolator = createHslInterpolator(colors[startIndex], colors[endIndex])
            val adjustedPercentage = ((colors.size - 1) * percentage) % 1.0f

            return@createInterpolator interpolator.interpolate(adjustedPercentage)
        }
    }

    private inline fun <T> createInterpolator(crossinline interpolator: (Float) -> T): Interpolator<T> = Interpolator { percentage -> interpolator(percentage) }
}
