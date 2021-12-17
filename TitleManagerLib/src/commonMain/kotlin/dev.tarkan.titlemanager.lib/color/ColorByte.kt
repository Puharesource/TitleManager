package dev.tarkan.titlemanager.lib.color

import kotlin.jvm.JvmInline

@JvmInline
value class ColorByte(val value: UByte) {
    companion object {
        val min = ColorByte(0u)
        val max = ColorByte(255u)
    }

    fun toColorFloat(): ColorFloat {
        val computed = value.toFloat() / max.value.toFloat()

        return ColorFloat(computed)
    }

    operator fun plus(other: ColorByte): ColorByte {
        return ColorByte((value + other.value).toUByte())
    }

    operator fun plus(other: ColorFloat): ColorFloat {
        return toColorFloat() + other
    }

    operator fun minus(other: ColorByte): ColorByte {
        return ColorByte((value - other.value).toUByte())
    }

    operator fun minus(other: ColorFloat): ColorFloat {
        return toColorFloat() - other
    }

    operator fun times(other: ColorByte): ColorByte {
        return ColorByte((value * other.value).toUByte())
    }

    operator fun times(other: ColorFloat): ColorFloat {
        return toColorFloat() * other
    }

    operator fun div(other: ColorByte): ColorByte {
        return ColorByte((value / other.value).toUByte())
    }

    operator fun div(other: ColorFloat): ColorFloat {
        return toColorFloat() / other
    }

    operator fun rem(other: ColorByte): ColorByte {
        return ColorByte((value % other.value).toUByte())
    }

    operator fun rem(other: ColorFloat): ColorFloat {
        return toColorFloat() % other
    }
}
