package dev.tarkan.titlemanager.parser.placeholder.variable

import kotlin.time.DurationUnit

/**
 * Represents a variable placeholder that dynamically generates a string value based on context and optional data.
 *
 * @param TContext The type of context used to generate the placeholder's value.
 * @param TData The type of data associated with the placeholder.
 * @param id The unique identifier for this placeholder.
 * @param cacheTime The duration for which the placeholder's value is cached, in the specified [durationUnit].
 * @param durationUnit The unit of time for [cacheTime].
 */
abstract class VariablePlaceholder<TContext, TData>(val id: String, val cacheTime: UInt = 1u, val durationUnit: DurationUnit = DurationUnit.SECONDS) {
    /**
     * Processes the raw, unprocessed data provided to the placeholder and converts it into a structured format.
     *
     * @param unprocessedData The raw data passed to the placeholder.
     * @return The processed data as an instance of [TData], or `null` if no data is provided.
     */
    protected abstract fun processData(unprocessedData: String?): TData?

    /**
     * Generates the final string value for the placeholder based on the processed data and the provided context.
     *
     * @param data The processed data for this placeholder.
     * @param context The context used to generate the placeholder's value.
     * @return The final string value for the placeholder.
     */
    internal abstract fun processReplace(data: TData?, context: TContext): String

    /**
     * Compiles the placeholder by processing the unprocessed data and returns a [CompiledVariablePlaceholder].
     *
     * @param unprocessedData The raw data passed to the placeholder.
     * @return A compiled instance of [CompiledVariablePlaceholder] containing the processed data.
     */
    fun compile(unprocessedData: String?): CompiledVariablePlaceholder<TContext, TData> {
        val data = processData(unprocessedData)

        return CompiledVariablePlaceholder(this, data)
    }
}

/**
 * A specialized implementation of [VariablePlaceholder] where the data type is always a [String].
 *
 * @param TContext The type of context used to generate the placeholder's value.
 * @param id The unique identifier for this placeholder.
 * @param cacheTime The duration for which the placeholder's value is cached, in the specified [durationUnit].
 * @param durationUnit The unit of time for [cacheTime].
 */
abstract class StringDataVariablePlaceholder<TContext>(id: String, cacheTime: UInt = 1u, durationUnit: DurationUnit = DurationUnit.SECONDS) : VariablePlaceholder<TContext, String>(id, cacheTime, durationUnit) {
    override fun processData(unprocessedData: String?) = unprocessedData
}
