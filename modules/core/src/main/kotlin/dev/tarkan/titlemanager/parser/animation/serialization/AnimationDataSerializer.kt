package dev.tarkan.titlemanager.parser.animation.serialization

/**
 * An interface for serializing and deserializing animation data.
 *
 * @param TData The type of the data being serialized or deserialized.
 */
interface AnimationDataSerializer<TData> {
    /**
     * Serializes the given data into a string representation.
     *
     * @param data The data to serialize. If `null`, the method should handle this case
     *             and return an appropriate string or `null`.
     * @return A string representation of the serialized data, or `null` if serialization fails.
     */
    fun serialize(data: TData?): String?

    /**
     * Deserializes a string into the corresponding data object.
     *
     * @param serializedString The string to deserialize. If `null`, the method should handle this
     *                         case and return a default instance or `null`.
     * @return The deserialized data object, or `null` if deserialization fails.
     */
    fun deserialize(serializedString: String?): TData?
}