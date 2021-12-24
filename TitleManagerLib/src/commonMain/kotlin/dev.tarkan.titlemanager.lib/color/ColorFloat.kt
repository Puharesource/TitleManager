package dev.tarkan.titlemanager.lib.color

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName
import kotlin.math.round

@JsExport
@ExperimentalJsExport
data class ColorFloat(val value: Float) {
    companion object {
        val min = ColorFloat(0.0f)
        val max = ColorFloat(1.0f)
    }

    fun toColorByte(): ColorByte {
        val computed = round(value * ColorByte.max.value.toFloat()).toUInt().toUByte()

        return ColorByte(computed)
    }

    @JsName("plusColorFloat")
    operator fun plus(other: ColorFloat): ColorFloat {
        return plus(other.value)
    }

    @JsName("plusFloat")
    operator fun plus(other: Float): ColorFloat {
        return ColorFloat(fixValue(value + other))
    }

    @JsName("plusByte")
    operator fun plus(other: ColorByte): ColorFloat {
        return this + other.toColorFloat()
    }

    @JsName("minusColorFloat")
    operator fun minus(other: ColorFloat): ColorFloat {
        return minus(other.value)
    }

    @JsName("minusFloat")
    operator fun minus(other: Float): ColorFloat {
        return ColorFloat(fixValue(value - other))
    }

    @JsName("minusByte")
    operator fun minus(other: ColorByte): ColorFloat {
        return this - other.toColorFloat()
    }

    @JsName("timesColorFloat")
    operator fun times(other: ColorFloat): ColorFloat {
        return times(other.value)
    }

    @JsName("timesFloat")
    operator fun times(other: Float): ColorFloat {
        return ColorFloat(fixValue(value * other))
    }

    @JsName("timesByte")
    operator fun times(other: ColorByte): ColorFloat {
        return this * other.toColorFloat()
    }

    @JsName("divColorFloat")
    operator fun div(other: ColorFloat): ColorFloat {
        return div(other.value)
    }

    @JsName("divFloat")
    operator fun div(other: Float): ColorFloat {
        return ColorFloat(fixValue(value / other))
    }

    @JsName("divByte")
    operator fun div(other: ColorByte): ColorFloat {
        return this / other.toColorFloat()
    }

    @JsName("remColorFloat")
    operator fun rem(other: ColorFloat): ColorFloat {
        return rem(other.value)
    }

    @JsName("remFloat")
    operator fun rem(other: Float): ColorFloat {
        return ColorFloat(fixValue(value % other))
    }

    @JsName("remByte")
    operator fun rem(other: ColorByte): ColorFloat {
        return this % other.toColorFloat()
    }

    private fun fixValue(value: Float) = value.coerceIn(min.value, max.value)
}
