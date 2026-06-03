package dev.tarkan.titlemanager.parser.placeholder.variable

import kotlin.time.DurationUnit

/**
 * A registry for managing and accessing variable placeholders by their unique identifiers or aliases.
 *
 * @param TContext The type of context used when resolving placeholder values.
 * @property placeholderVariables A map of placeholder identifiers to their respective [VariablePlaceholder] implementations.
 */
class VariablePlaceholderRegistry<TContext> private constructor(private val placeholderVariables: Map<String, VariablePlaceholder<TContext, String>>) {
    companion object {
        /**
         * Constructs a [VariablePlaceholderRegistry] using a builder pattern.
         *
         * @param builderBody A lambda for configuring the builder.
         * @return A fully constructed [VariablePlaceholderRegistry].
         */
        fun <TContext> build(builderBody: Builder<TContext>.() -> Unit): VariablePlaceholderRegistry<TContext> {
            val builder = Builder<TContext>()

            builder.builderBody()

            return builder.build()
        }
    }

    /**
     * Retrieves a placeholder by its identifier or alias.
     *
     * @param id The unique identifier or alias of the placeholder.
     * @return The corresponding [VariablePlaceholder], or `null` if not found.
     */
    operator fun get(id: String) = placeholderVariables[id.lowercase()]

    /**
     * A builder for constructing instances of [VariablePlaceholderRegistry].
     *
     * @param TContext The type of context used when resolving placeholder values.
     */
    class Builder<TContext> internal constructor() {
        private val placeholderVariables = mutableMapOf<String, VariablePlaceholder<TContext, String>>()

        /**
         * Adds a simple string-based placeholder with a custom processing logic.
         *
         * @param id The unique identifier of the placeholder.
         * @param aliases Alternative names for the placeholder.
         * @param body A lambda for processing the placeholder value.
         */
        inline fun addSimple(id: String, vararg aliases: String, crossinline body: (unprocessedData: String?) -> String) {
            val placeholder = object : StringDataVariablePlaceholder<TContext>(id.lowercase()) {
                override fun processReplace(data: String?, context: TContext) = body(data)
            }

            add(id, *aliases, placeholder = placeholder)
        }

        /**
         * Adds a placeholder that depends on context but not on additional data.
         *
         * @param id The unique identifier of the placeholder.
         * @param aliases Alternative names for the placeholder.
         * @param cacheTime The duration for which the placeholder's value is cached.
         * @param durationUnit The unit of time for [cacheTime].
         * @param body A lambda for generating the placeholder value based on context.
         */
        inline fun addWithContextNoData(id: String, vararg aliases: String, cacheTime: UInt = 1u, durationUnit: DurationUnit = DurationUnit.SECONDS, crossinline body: (context: TContext) -> String) {
            val placeholder = object : StringDataVariablePlaceholder<TContext>(id.lowercase(), cacheTime, durationUnit) {
                override fun processReplace(data: String?, context: TContext) = body(context)
            }

            add(id, *aliases, placeholder = placeholder)
        }

        /**
         * Adds a placeholder that depends on context and optional string data.
         *
         * @param id The unique identifier of the placeholder.
         * @param aliases Alternative names for the placeholder.
         * @param cacheTime The duration for which the placeholder's value is cached.
         * @param durationUnit The unit of time for [cacheTime].
         * @param body A lambda for generating the placeholder value from the optional data and context.
         */
        inline fun addWithContext(id: String, vararg aliases: String, cacheTime: UInt = 1u, durationUnit: DurationUnit = DurationUnit.SECONDS, crossinline body: (unprocessedData: String?, context: TContext) -> String) {
            val placeholder = object : StringDataVariablePlaceholder<TContext>(id.lowercase(), cacheTime, durationUnit) {
                override fun processReplace(data: String?, context: TContext) = body(data, context)
            }

            add(id, *aliases, placeholder = placeholder)
        }

        /**
         * Adds a placeholder with its identifier and aliases to the registry.
         *
         * @param id The unique identifier of the placeholder.
         * @param aliases Alternative names for the placeholder.
         * @param placeholder The [VariablePlaceholder] to add.
         * @return The current [Builder] instance for chaining.
         */
        fun add(id: String, vararg aliases: String, placeholder: VariablePlaceholder<TContext, String>): Builder<TContext> {
            tryAdd(id, placeholder)

            for (alias in aliases) {
                tryAdd(alias, placeholder)
            }

            return this
        }

        /**
         * Attempts to add a placeholder to the registry.
         *
         * @param idOrAlias The identifier or alias of the placeholder.
         * @param placeholder The [VariablePlaceholder] to add.
         * @throws IllegalStateException If a placeholder with the same identifier or alias already exists.
         */
        private fun tryAdd(idOrAlias: String, placeholder: VariablePlaceholder<TContext, String>) {
            val key = idOrAlias.lowercase()
            check(!placeholderVariables.containsKey(key)) { "ID or alias with value $idOrAlias already exists" }

            placeholderVariables[key] = placeholder
        }

        /**
         * Builds a [VariablePlaceholderRegistry] instance with the added placeholders.
         *
         * @return A new [VariablePlaceholderRegistry] containing all the added placeholders.
         */
        fun build() = VariablePlaceholderRegistry(placeholderVariables.toMap())
    }
}