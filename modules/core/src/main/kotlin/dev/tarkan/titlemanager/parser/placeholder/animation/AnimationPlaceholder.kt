package dev.tarkan.titlemanager.parser.placeholder.animation

import dev.tarkan.titlemanager.animation.Animation
import dev.tarkan.titlemanager.parser.animation.serialization.AnimationDataSerializer

/**
 * Represents a placeholder for creating animations, where animations are dynamically compiled
 * based on serialized data and a specified context.
 *
 * @param TContext The type of the context used during animation creation.
 * @param TData The type of data used to define the animation behavior.
 * @property id The unique identifier for this animation placeholder.
 * @property dataSerializer The serializer responsible for handling the conversion of animation data to and from its serialized form.
 */
abstract class AnimationPlaceholder<TContext, TData>(
    val id: String,
    private val dataSerializer: AnimationDataSerializer<TData>
) {
    /**
     * Creates an animation instance using the provided data.
     *
     * This method must be implemented by subclasses to define how the animation
     * is created using the deserialized data.
     *
     * @param data The deserialized data used to configure the animation instance.
     * @return An instance of [Animation] configured with the provided data.
     */
    protected abstract fun createAnimationInstance(data: TData?): Animation<TContext, String>

    /**
     * Compiles an animation from the unprocessed serialized data.
     *
     * The provided serialized data is deserialized using the [dataSerializer] and then
     * passed to [createAnimationInstance] to generate an animation instance.
     *
     * @param unprocessedData The serialized data defining the animation's behavior.
     * @return An [Animation] instance configured based on the deserialized data.
     */
    fun compile(unprocessedData: String?): Animation<TContext, String> {
        val data = dataSerializer.deserialize(unprocessedData)

        return createAnimationInstance(data)
    }
}
