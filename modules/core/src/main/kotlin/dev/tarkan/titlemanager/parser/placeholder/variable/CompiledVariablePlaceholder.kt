package dev.tarkan.titlemanager.parser.placeholder.variable

/**
 * Represents a compiled instance of a variable placeholder, binding its data for efficient processing.
 *
 * @param TContext The type of the context used when replacing placeholders.
 * @param TData The type of the data associated with the placeholder.
 * @property variablePlaceholder The original [VariablePlaceholder] instance used for processing the replacement.
 * @property data The data associated with the placeholder, which may be null if no data is provided.
 */
class CompiledVariablePlaceholder<TContext, TData>(private val variablePlaceholder: VariablePlaceholder<TContext, TData>, private val data: TData?) {
    fun replace(context: TContext): String = variablePlaceholder.processReplace(data, context)
}