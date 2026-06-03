package dev.tarkan.titlemanager.parser.animation.serialization

/**
 * A serializer for animation data of type `String`.
 *
 * This object provides functionality to directly serialize and deserialize strings.
 * It acts as a pass-through for string data, ensuring no transformation occurs.
 */
object StringAnimationDataSerializer : AnimationDataSerializer<String> {
    /**
     * Serializes a string without any transformation.
     *
     * @param data The string to serialize. If `null`, the result is `null`.
     * @return The input string as-is, or `null` if the input is `null`.
     */
    override fun serialize(data: String?) = data

    /**
     * Deserializes a string without any transformation.
     *
     * @param serializedString The string to deserialize. If `null`, the result is `null`.
     * @return The input string as-is, or `null` if the input is `null`.
     */
    override fun deserialize(serializedString: String?) = serializedString
}