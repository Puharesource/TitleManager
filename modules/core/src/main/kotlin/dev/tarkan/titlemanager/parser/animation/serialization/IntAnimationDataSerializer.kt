package dev.tarkan.titlemanager.parser.animation.serialization

/**
 * A serializer for animation data of type `Int`.
 *
 * This object provides functionality to serialize integers into string representations
 * and deserialize strings back into integers. Null values are handled gracefully.
 */
object IntAnimationDataSerializer : AnimationDataSerializer<Int> {
    /**
     * Serializes an integer into its string representation.
     *
     * @param data The integer to serialize. If `null`, the result is `null`.
     * @return The string representation of the integer, or `null` if the input is `null`.
     */
    override fun serialize(data: Int?) = data?.toString()

    /**
     * Deserializes a string into an integer.
     *
     * @param serializedString The string to deserialize. If `null` or if the string cannot
     *                         be parsed into an integer, the result is `null`.
     * @return The parsed integer, or `null` if deserialization fails or the input is `null`.
     */
    override fun deserialize(serializedString: String?) = serializedString
        ?.trim()
        ?.toIntOrNull()
}