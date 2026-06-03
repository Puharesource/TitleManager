package dev.tarkan.titlemanager.bukkit.configuration

import dev.tarkan.titlemanager.api.TitleManagerCoreApi
import dev.tarkan.titlemanager.color.ColorGradient
import dev.tarkan.titlemanager.color.HslColor
import dev.tarkan.titlemanager.color.hsl
import dev.tarkan.titlemanager.color.rgb
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Serializable
data class GradientsConfiguration(
    val gradients: Map<String, GradientConfiguration>
) {
    @Transient
    private val caseInsensitiveGradients = TreeMap<String, GradientConfiguration>(String.CASE_INSENSITIVE_ORDER)

    init {
        caseInsensitiveGradients.putAll(gradients)
    }

    fun findGradient(name: String): GradientConfiguration? {
        return caseInsensitiveGradients[name]
    }
}

@SerialName("GradientConfigurationConfiguration")
@Serializable
data class GradientConfiguration(
    val colorSpace: String,
    val precision: Int,
    val colors: List<String>
) {
    @Transient
    private val isShort = colorSpace.equals("hsl", ignoreCase = true)

    init {
        require(colorSpace.equals("hsl", ignoreCase = true) || colorSpace.equals("hsl long", ignoreCase = true)) {
            "Gradient color space must be either HSL or HSL Long"
        }
        require(precision > 0) { "Gradient precision must be greater than zero" }
        require(colors.size >= 2) { "Gradient must contain at least two colors" }
        colors.forEach { TitleManagerCoreApi.parseLegacyHexColorRgb(it) }
    }

    private val hslColors by lazy {
        colors.map {
            TitleManagerCoreApi.parseLegacyHexColorRgb(it).hsl
        }.toTypedArray()
    }

    val cachedGradient: ColorGradient<HslColor> by lazy {
        if (isShort) {
            ColorGradient.hsl(*hslColors)
        } else {
            ColorGradient.longHsl(*hslColors)
        }
    }

    val cachedRadialGradient: ColorGradient<HslColor> by lazy {
        if (isShort) {
            ColorGradient.hsl(*hslColors, *hslColors.take(hslColors.size - 1).asReversed().toTypedArray())
        } else {
            ColorGradient.longHsl(*hslColors, *hslColors.take(hslColors.size - 1).asReversed().toTypedArray())
        }
    }

    val cachedColors: List<String> by lazy { getColors(false) }

    val cachedRadialColors: List<String> by lazy { getColors(true) }

    fun getColors(isRadial: Boolean, precision: Int = this.precision): List<String> {
        val gradient = if (isRadial) cachedRadialGradient else cachedGradient
        val precisionFloat = precision.toFloat()

        return (0 until precision).map {  i ->
            val hslColor = gradient.interpolate(i / precisionFloat)
            TitleManagerCoreApi.legacyRgbIntColorCode(hslColor.rgb)
        }
    }
}
