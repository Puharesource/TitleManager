package dev.tarkan.titlemanager.parser.placeholder.animation

import dev.tarkan.titlemanager.animation.Animation
import dev.tarkan.titlemanager.parser.animation.serialization.AnimationDataSerializer

/**
 * A registry for managing and retrieving animation placeholders.
 *
 * The registry maps unique IDs and aliases to [AnimationPlaceholder] instances, providing
 * an organized way to manage animations by their identifiers.
 *
 * @param TContext The type of the context used in animations.
 * @property animationPlaceholders A map of placeholder IDs and aliases to their respective [AnimationPlaceholder] instances.
 */
class AnimationPlaceholderRegistry<TContext> private constructor(private val animationPlaceholders: Map<String, AnimationPlaceholder<TContext, *>>) {
    companion object {
        /**
         * Creates an [AnimationPlaceholderRegistry] using the provided builder logic.
         *
         * @param builderBody The builder logic for configuring the registry.
         * @return A constructed [AnimationPlaceholderRegistry] instance.
         */
        fun <TContext> build(builderBody: Builder<TContext>.() -> Unit): AnimationPlaceholderRegistry<TContext> {
            val builder = Builder<TContext>()

            builder.builderBody()

            return builder.build()
        }
    }

    /**
     * A set of all placeholder keys registered in the registry.
     */
    val keys: Set<String>
        get() = animationPlaceholders.keys

    /**
     * Retrieves the [AnimationPlaceholder] associated with the given ID or alias.
     *
     * @param id The ID or alias of the placeholder.
     * @return The corresponding [AnimationPlaceholder], or `null` if not found.
     */
    operator fun get(id: String) = animationPlaceholders[id.lowercase()]

    /**
     * Builder for constructing an [AnimationPlaceholderRegistry].
     *
     * @param TContext The type of the context used in animations.
     */
    class Builder<TContext> internal constructor() {
        private val animationPlaceholders =
            mutableMapOf<String, AnimationPlaceholder<TContext, *>>()

        /**
         * Adds a simple [AnimationPlaceholder] to the registry with the provided ID, aliases, and animation logic.
         *
         * @param id The unique identifier for the placeholder.
         * @param aliases Optional aliases for the placeholder.
         * @param dataSerializer The serializer for handling the placeholder's data.
         * @param body The logic to create an animation instance based on the data.
         */
        inline fun <TData> addSimple(
            id: String,
            vararg aliases: String,
            dataSerializer: AnimationDataSerializer<TData>,
            crossinline body: (data: TData?) -> Animation<TContext, String>
        ) {
            val placeholder = object : AnimationPlaceholder<TContext, TData>(id.lowercase(), dataSerializer) {
                override fun createAnimationInstance(data: TData?): Animation<TContext, String> = body(data)
            }

            add(id, *aliases, placeholder = placeholder)
        }

        /**
         * Adds an [AnimationPlaceholder] to the registry with the provided ID and optional aliases.
         *
         * @param id The unique identifier for the placeholder.
         * @param aliases Optional aliases for the placeholder.
         * @param placeholder The [AnimationPlaceholder] to add to the registry.
         * @return The current builder instance.
         */
        fun add(id: String, vararg aliases: String, placeholder: AnimationPlaceholder<TContext, *>): Builder<TContext> {
            tryAdd(id, placeholder)

            for (alias in aliases) {
                tryAdd(alias, placeholder)
            }

            return this
        }

        /**
         * Attempts to add a placeholder to the registry, checking for duplicate IDs or aliases.
         *
         * @param idOrAlias The ID or alias to add.
         * @param placeholder The [AnimationPlaceholder] to associate with the ID or alias.
         * @throws IllegalStateException If the ID or alias already exists.
         */
        private fun tryAdd(idOrAlias: String, placeholder: AnimationPlaceholder<TContext, *>) {
            val key = idOrAlias.lowercase()
            check(!animationPlaceholders.containsKey(key)) { "ID or alias with value $idOrAlias already exists" }

            animationPlaceholders[key] = placeholder
        }

        /**
         * Builds the [AnimationPlaceholderRegistry] with the configured placeholders.
         *
         * @return A constructed [AnimationPlaceholderRegistry] instance.
         */
        fun build() = AnimationPlaceholderRegistry(animationPlaceholders.toMap())
    }
}