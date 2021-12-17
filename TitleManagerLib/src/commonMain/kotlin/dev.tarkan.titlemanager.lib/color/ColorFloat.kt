package dev.tarkan.titlemanager.lib.color

import kotlin.jvm.JvmInline
import kotlin.math.round

@JvmInline
value class ColorFloat(val value: Float) {
    companion object {
        val min = ColorFloat(0.0f)
        val max = ColorFloat(1.0f)
    }

    fun toColorByte(): ColorByte {
        val computed = round(value * ColorByte.max.value.toFloat()).toUInt().toUByte()

        return ColorByte(computed)
    }

    operator fun plus(other: ColorFloat): ColorFloat {
        return plus(other.value)
    }

    operator fun plus(other: Float): ColorFloat {
        return ColorFloat(fixValue(value + other))
    }

    operator fun plus(other: ColorByte): ColorFloat {
        return this + other.toColorFloat()
    }

    operator fun minus(other: ColorFloat): ColorFloat {
        return minus(other.value)
    }

    operator fun minus(other: Float): ColorFloat {
        return ColorFloat(fixValue(value - other))
    }

    operator fun minus(other: ColorByte): ColorFloat {
        return this - other.toColorFloat()
    }

    operator fun times(other: ColorFloat): ColorFloat {
        return times(other.value)
    }

    operator fun times(other: Float): ColorFloat {
        return ColorFloat(fixValue(value * other))
    }

    operator fun times(other: ColorByte): ColorFloat {
        return this * other.toColorFloat()
    }

    operator fun div(other: ColorFloat): ColorFloat {
        return div(other.value)
    }

    operator fun div(other: Float): ColorFloat {
        return ColorFloat(fixValue(value / other))
    }

    operator fun div(other: ColorByte): ColorFloat {
        return this / other.toColorFloat()
    }

    operator fun rem(other: ColorFloat): ColorFloat {
        return rem(other.value)
    }

    operator fun rem(other: Float): ColorFloat {
        return ColorFloat(fixValue(value % other))
    }

    operator fun rem(other: ColorByte): ColorFloat {
        return this % other.toColorFloat()
    }

    private fun fixValue(value: Float) = value.coerceIn(min.value, max.value)
}
