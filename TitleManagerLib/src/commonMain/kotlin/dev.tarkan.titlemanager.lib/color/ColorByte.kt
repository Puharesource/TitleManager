package dev.tarkan.titlemanager.lib.color

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@ExperimentalJsExport
data class ColorByte(val value: UByte) {
    companion object {
        val min = ColorByte(0u)
        val max = ColorByte(255u)
    }

    fun toColorFloat(): ColorFloat {
        val computed = value.toFloat() / max.value.toFloat()

        return ColorFloat(computed)
    }

    @JsName("plusByte")
    operator fun plus(other: ColorByte): ColorByte {
        return ColorByte((value + other.value).toUByte())
    }

    @JsName("plusFloat")
    operator fun plus(other: ColorFloat): ColorFloat {
        return toColorFloat() + other
    }

    @JsName("minusByte")
    operator fun minus(other: ColorByte): ColorByte {
        return ColorByte((value - other.value).toUByte())
    }

    @JsName("minusFloat")
    operator fun minus(other: ColorFloat): ColorFloat {
        return toColorFloat() - other
    }

    @JsName("timesByte")
    operator fun times(other: ColorByte): ColorByte {
        return ColorByte((value * other.value).toUByte())
    }

    @JsName("timesFloat")
    operator fun times(other: ColorFloat): ColorFloat {
        return toColorFloat() * other
    }

    @JsName("divByte")
    operator fun div(other: ColorByte): ColorByte {
        return ColorByte((value / other.value).toUByte())
    }

    @JsName("divFloat")
    operator fun div(other: ColorFloat): ColorFloat {
        return toColorFloat() / other
    }

    @JsName("remByte")
    operator fun rem(other: ColorByte): ColorByte {
        return ColorByte((value % other.value).toUByte())
    }

    @JsName("remFloat")
    operator fun rem(other: ColorFloat): ColorFloat {
        return toColorFloat() % other
    }
}
