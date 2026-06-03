package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.parser.animation.serialization.AnimationDataSerializer
import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing

/**
 * A marquee-style animation that scrolls text horizontally, displaying a fixed-width portion of the text at a time.
 *
 * @param TContext The type of context required to execute the animation.
 * @property text The text to scroll in the marquee.
 * @property timing The timing for how long each frame of the marquee is displayed.
 * @property width The width of the marquee (i.e., how many characters are visible at once). Defaults to the length of the text.
 */
class MarqueeAnimation<TContext>(
    private val text: String,
    private val timing: Timing,
    private val width: Int = text.length
) : TimelineAnimation<TContext, String> {
    /**
     * Produces a flow of marquee frames with timing information.
     *
     * @param context The context required to execute the animation.
     * @param isInfinite If `true`, the animation will loop indefinitely; otherwise, it runs once.
     * @return A flow of [TimedItem] representing the marquee frames.
     */
    override fun singleIterationFramesWithTimings(context: TContext, isInfinite: Boolean): List<TimedItem<String>> =
        text.indices.map { outerIndex ->
            TimedItem(timing, frameText(outerIndex))
        }

    private fun frameText(outerIndex: Int): String = buildString {
        for (innerIndex in 0 until width) {
            append(text[(outerIndex + innerIndex) % text.length])
        }
    }

    /**
     * Data class representing the serialized data for the marquee animation.
     *
     * @property timing The timing for each frame of the marquee.
     * @property text The text to display in the marquee.
     * @property width The width of the marquee (i.e., the number of characters visible at a time).
     */
    data class Data(
        val timing: Timing = Timing.default,
        val text: String = "Invalid-Data",
        val width: Int = text.length
    )

    /**
     * Serializer for the marquee animation's data, allowing for serialization and deserialization.
     */
    object DataSerializer : AnimationDataSerializer<Data> {
        private val FULL_LINE_REGEX: Regex = """^\[(\d+)\](.+)$""".toRegex()

        /**
         * Serializes the animation data into a string representation.
         *
         * @param data The [Data] object to serialize.
         * @return A serialized string representing the data.
         */
        override fun serialize(data: Data?): String {
            val dataOrDefault = data ?: Data()

            return buildString {
                append(dataOrDefault.timing.toSerializedString())
                append("[${dataOrDefault.width}]")
                append(dataOrDefault.text)
            }
        }

        /**
         * Deserializes a string representation of the animation data into a [Data] object.
         *
         * @param serializedString The serialized string to deserialize.
         * @return A [Data] object representing the animation data.
         */
        override fun deserialize(serializedString: String?): Data {
            if (serializedString == null) {
                return Data()
            }

            val timingSplit = Timing.splitTimingsAndText(serializedString)

            val match = FULL_LINE_REGEX.matchEntire(timingSplit.second) ?: return Data()

            val text = match.groups[2]?.value ?: "Invalid-Data"
            var width = match.groups[1]?.value?.toIntOrNull() ?: text.length

            if (width <= 0) {
                width = text.length
            }

            val timing = timingSplit.first?.times(50u) ?: Timing.default

            return Data(
                timing,
                text,
                width
            )
        }

        /**
         * Converts a [Timing] object into a serialized string representation.
         *
         * @return A serialized string representing the timing values.
         */
        private fun Timing.toSerializedString() = "[$fadeInMilliseconds;$stayMilliseconds;$fadeOutMilliseconds]"
    }
}