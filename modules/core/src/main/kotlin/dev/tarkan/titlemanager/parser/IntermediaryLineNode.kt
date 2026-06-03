package dev.tarkan.titlemanager.parser

/**
 * Represents a node in an intermediary parsed line.
 * This interface acts as a marker for different types of nodes in the parsed structure.
 */
interface IntermediaryLineNode

/**
 * Represents a variable placeholder node in a parsed line.
 *
 * @property id The identifier of the variable placeholder.
 * @property data The associated data for the variable placeholder, or `null` if none is provided.
 */
data class IntermediaryVariablePlaceholderNode(val id: String, val data: String?) : IntermediaryLineNode

/**
 * Represents an animation placeholder node in a parsed line.
 *
 * @property id The identifier of the animation placeholder.
 * @property data The associated data for the animation placeholder, or `null` if none is provided.
 */
data class IntermediaryAnimationPlaceholderNode(val id: String, val data: String?) : IntermediaryLineNode

/**
 * Represents a plain text node in a parsed line.
 *
 * @property text The plain text content of the node.
 */
data class IntermediaryTextNode(val text: String) : IntermediaryLineNode