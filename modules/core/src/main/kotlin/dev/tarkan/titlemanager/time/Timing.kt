package dev.tarkan.titlemanager.time

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

/**
 * Represents the timing of an event with fade-in, stay, and fade-out durations, all measured in milliseconds.
 *
 * @property fadeInMilliseconds The duration for the fade-in effect, in milliseconds.
 * @property stayMilliseconds The duration for which the event stays visible, in milliseconds.
 * @property fadeOutMilliseconds The duration for the fade-out effect, in milliseconds.
 */
data class Timing(val fadeInMilliseconds: UInt, val stayMilliseconds: UInt, val fadeOutMilliseconds: UInt) :
    Comparable<Timing> {
    companion object {
        private val TIMED_STRING_REGEX = Regex("""^\[(\d+);(\d+);(\d+)\](.*)$""")

        /** Represents an instant timing with no delays or durations. */
        val instant: Timing = Timing(0u, 0u, 0u)

        /** Represents a timing that never ends, using maximum unsigned integer values. */
        val never: Timing = Timing(UInt.MAX_VALUE, UInt.MAX_VALUE, UInt.MAX_VALUE)

        /** Represents a default timing with minimal durations. */
        val default: Timing = Timing(0u, 1u, 0u)

        /**
         * Creates a static timing where only the stay duration is set, and the fade-in and fade-out are zero.
         *
         * @param stayMilliseconds The duration for the stay phase, in milliseconds.
         * @param scale A multiplier to apply to the duration.
         * @return A new [Timing] instance.
         */
        fun createStatic(stayMilliseconds: UInt, scale: UInt = 1u): Timing =
            Timing(0u, stayMilliseconds, 0u) * scale

        /**
         * Splits a string into a [Timing] and the remaining text based on a specific format.
         *
         * @param input The input string in the format `[fadeIn;stay;fadeOut]text`.
         * @return A pair containing the parsed [Timing] (or null if parsing failed) and the remaining text.
         */
        fun splitTimingsAndText(input: String): Pair<Timing?, String> {
            val match = TIMED_STRING_REGEX.matchEntire(input) ?: return null to input
            val timing = Timing(
                fadeInMilliseconds = parseTimingValue(match.groups[1]?.value),
                stayMilliseconds = parseTimingValue(match.groups[2]?.value),
                fadeOutMilliseconds = parseTimingValue(match.groups[3]?.value)
            )
            val text = match.groups[4]?.value ?: ""

            return timing to text
        }

        private fun parseTimingValue(value: String?): UInt {
            return value?.toUIntOrNull()
                ?: throw IllegalArgumentException("Timing values must be unsigned 32-bit integers")
        }

        /**
         * Creates a [Timing] instance by converting durations from a specific unit to milliseconds.
         *
         * @param fadeIn The fade-in duration in the source unit.
         * @param stay The stay duration in the source unit.
         * @param fadeOut The fade-out duration in the source unit.
         * @param sourceDurationUnit The source unit of the durations.
         * @param scale A multiplier to apply to the durations.
         * @return A new [Timing] instance with durations in milliseconds.
         */
        @OptIn(ExperimentalTime::class)
        fun createWithDurationUnit(
            fadeIn: UInt,
            stay: UInt,
            fadeOut: UInt,
            sourceDurationUnit: DurationUnit,
            scale: UInt = 1u
        ): Timing {
            val convertedFadeIn =
                Duration.convert(fadeIn.toDouble(), sourceDurationUnit, DurationUnit.MILLISECONDS).toUInt()
            val convertedStay =
                Duration.convert(stay.toDouble(), sourceDurationUnit, DurationUnit.MILLISECONDS).toUInt()
            val convertedFadeOut =
                Duration.convert(fadeOut.toDouble(), sourceDurationUnit, DurationUnit.MILLISECONDS).toUInt()

            return Timing(convertedFadeIn, convertedStay, convertedFadeOut) * scale
        }
    }

    /** The total duration in milliseconds, including fade-in, stay, and fade-out phases. */
    @OptIn(ExperimentalUnsignedTypes::class)
    val totalMilliseconds: UInt = saturatingSum(fadeInMilliseconds, stayMilliseconds, fadeOutMilliseconds)

    /**
     * Computes the maximum of this timing and another timing for each phase.
     *
     * @param other The other [Timing] instance.
     * @return A new [Timing] instance with the maximum durations for each phase.
     */
    fun maxOf(other: Timing): Timing {
        return Timing(
            maxOf(fadeInMilliseconds, other.fadeInMilliseconds),
            maxOf(stayMilliseconds, other.stayMilliseconds),
            maxOf(fadeOutMilliseconds, other.fadeOutMilliseconds)
        )
    }

    /**
     * Computes the minimum of this timing and another timing for each phase.
     *
     * @param other The other [Timing] instance.
     * @return A new [Timing] instance with the minimum durations for each phase.
     */
    fun minOf(other: Timing): Timing {
        return Timing(
            minOf(fadeInMilliseconds, other.fadeInMilliseconds),
            minOf(stayMilliseconds, other.stayMilliseconds),
            minOf(fadeOutMilliseconds, other.fadeOutMilliseconds)
        )
    }

    /**
     * Multiplies this timing by another timing, scaling each phase independently.
     *
     * @param other The other [Timing] instance.
     * @return A new [Timing] instance with scaled durations.
     */
    operator fun times(other: Timing): Timing {
        return Timing(
            fadeInMilliseconds.multiplyTiming(other.fadeInMilliseconds),
            stayMilliseconds.multiplyTiming(other.stayMilliseconds),
            fadeOutMilliseconds.multiplyTiming(other.fadeOutMilliseconds)
        )
    }

    /**
     * Multiplies this timing by a scalar value, scaling all phases equally.
     *
     * @param other The scalar value to multiply.
     * @return A new [Timing] instance with scaled durations.
     */
    operator fun times(other: UInt): Timing {
        return Timing(
            fadeInMilliseconds.multiplyTiming(other),
            stayMilliseconds.multiplyTiming(other),
            fadeOutMilliseconds.multiplyTiming(other)
        )
    }

    private fun UInt.multiplyTiming(other: UInt): UInt {
        val product = toULong() * other.toULong()
        require(product <= UInt.MAX_VALUE.toULong()) {
            "Timing multiplication exceeds unsigned 32-bit range"
        }
        return product.toUInt()
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    private fun saturatingSum(vararg values: UInt): UInt {
        val sum = values.fold(0uL) { total, value -> total + value.toULong() }
        return minOf(sum, UInt.MAX_VALUE.toULong()).toUInt()
    }

    /**
     * Compares this timing to another based on the total duration in milliseconds.
     *
     * @param other The other [Timing] instance.
     * @return A negative number if this timing is shorter, zero if equal, or a positive number if longer.
     */
    override fun compareTo(other: Timing) = totalMilliseconds.compareTo(other.totalMilliseconds)
}
