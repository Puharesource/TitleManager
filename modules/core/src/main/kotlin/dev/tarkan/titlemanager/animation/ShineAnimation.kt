package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.parser.animation.serialization.AnimationDataSerializer
import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing

/**
 * Represents a "shine" animation where a highlight effect moves across the text with specified colors and timings.
 *
 * @param TContext The type of context required to execute the animation.
 * @property text The text to be animated.
 * @property primaryColor The primary color for the unhighlighted parts of the text.
 * @property secondaryColor The secondary color for the highlighted part of the text.
 * @property intermediaryTiming The timing for intermediate frames in the animation.
 * @property startTiming The timing for the start frame (optional).
 * @property endTiming The timing for the end frame (optional).
 */
class ShineAnimation<TContext>(
    private val text: String,
    private val primaryColor: String,
    private val secondaryColor: String,
    private val intermediaryTiming: Timing,
    private val startTiming: Timing? = null,
    private val endTiming: Timing? = null
) : TimelineAnimation<TContext, String> {
    /**
     * Produces a flow that emits the frames of the "shine" animation with timings.
     *
     * @param context The context required to execute the animation.
     * @param isInfinite If `true`, the animation will loop infinitely.
     * @return A flow of [TimedItem] representing each frame of the animation.
     */
    override fun singleIterationFramesWithTimings(context: TContext, isInfinite: Boolean): List<TimedItem<String>> {
        val range = 0..text.length + 3

        return range.map { index ->
            val timing = getTimingForFrame(range, index)

            if (index == range.first || index == range.last) {
                return@map TimedItem(timing, primaryColor + text)
            }

            val startIndex = (index - 3).coerceAtLeast(0)
            val endIndex = index.coerceAtMost(text.length)

            val left = primaryColor + text.substring(0, startIndex)
            val center = secondaryColor + text.substring(startIndex, endIndex)
            val right = primaryColor + text.substring(endIndex)

            TimedItem(timing, left + center + right)
        }
    }

    /**
     * Determines the timing for a specific frame of the animation.
     *
     * @param range The range of frames in the animation.
     * @param index The current frame index.
     * @return The [Timing] for the frame.
     */
    private fun getTimingForFrame(range: IntRange, index: Int): Timing {
        if (index == 0) {
            return startTiming ?: intermediaryTiming
        }

        if (index == range.last) {
            return endTiming ?: intermediaryTiming
        }

        return intermediaryTiming
    }

    /**
     * Data class for serializing and deserializing shine animation configurations.
     *
     * @property intermediaryTiming The timing for intermediate frames.
     * @property startTiming The timing for the start frame.
     * @property endTiming The timing for the end frame.
     * @property primaryColor The primary color of the animation.
     * @property secondaryColor The secondary color of the animation.
     * @property text The text to be animated.
     */
    data class Data(
        val intermediaryTiming: Timing = Timing.default,
        val startTiming: Timing? = null,
        val endTiming: Timing? = null,
        val primaryColor: String = "&3",
        val secondaryColor: String = "&b",
        val text: String = "Invalid-Data"
    )

    /**
     * Serializer for the [Data] class, providing methods to serialize and deserialize animation data.
     */
    object DataSerializer : AnimationDataSerializer<Data> {
        private val FULL_LINE_REGEX: Regex = """^\[(.+);(.+)\](.*)$""".toRegex()

        /**
         * Serializes the given [Data] instance into a string.
         *
         * @param data The data to serialize. If `null`, default values are used.
         * @return A serialized string representation of the data.
         */
        override fun serialize(data: Data?): String {
            val dataOrDefault = data ?: Data()

            return buildString {
                append(dataOrDefault.intermediaryTiming.toSerializedString())

                if (dataOrDefault.startTiming != null) {
                    append(dataOrDefault.startTiming.toSerializedString())

                    if (dataOrDefault.endTiming != null) {
                        append(dataOrDefault.endTiming.toSerializedString())
                    }
                }

                append("[${dataOrDefault.primaryColor};${dataOrDefault.secondaryColor}]")
                append(dataOrDefault.text)
            }
        }

        /**
         * Deserializes the given string into a [Data] instance.
         *
         * @param serializedString The string to deserialize. If `null`, default values are used.
         * @return The deserialized [Data] instance.
         */
        override fun deserialize(serializedString: String?): Data {
            if (serializedString == null) {
                return Data()
            }

            val intermediaryTimingSplit = Timing.splitTimingsAndText(serializedString)
            val startTimingSplit = Timing.splitTimingsAndText(intermediaryTimingSplit.second)
            val endTimingSplit = Timing.splitTimingsAndText(startTimingSplit.second)

            val match = FULL_LINE_REGEX.matchEntire(endTimingSplit.second) ?: return Data()

            val primaryColor = match.groups[1]?.value ?: "&3"
            val secondaryColor = match.groups[2]?.value ?: "&b"
            val text = match.groups[3]?.value ?: "Invalid-Data"

            val defaultTiming = intermediaryTimingSplit.first?.times(50u) ?: Timing.default

            return Data(
                intermediaryTiming = defaultTiming,
                startTiming = startTimingSplit.first?.times(50u) ?: defaultTiming,
                endTiming = endTimingSplit.first?.times(50u) ?: defaultTiming,
                primaryColor,
                secondaryColor,
                text
            )
        }

        /**
         * Converts a [Timing] instance to its serialized string representation.
         *
         * @return A string representation of the timing in the format `[fadeIn;stay;fadeOut]`.
         */
        private fun Timing.toSerializedString() = "[$fadeInMilliseconds;$stayMilliseconds;$fadeOutMilliseconds]"
    }
}