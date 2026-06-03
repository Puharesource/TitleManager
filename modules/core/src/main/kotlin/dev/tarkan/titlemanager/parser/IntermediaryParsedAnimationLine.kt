package dev.tarkan.titlemanager.parser

import dev.tarkan.titlemanager.time.Timing

/**
 * Represents a parsed line of an animation with its timing and text content.
 *
 * @property timing The timing associated with the line, which defines fade-in, stay, and fade-out durations.
 * @property text The raw text content of the line.
 */
data class IntermediaryParsedAnimationLine(val timing: Timing, val text: String) {
    companion object {
        /**
         * Regex to match animation placeholders in the format `${animationName:optionalData}`.
         */
        private val ANIMATION_PLACEHOLDER_REGEX: Regex =
            """\$\{([^}:]+)(?::((?:[^}\\]|\\.)*))?\}""".toRegex()

        /**
         * Regex to match variable placeholders in the format `%{variableName:optionalData}`.
         */
        private val VARIABLE_PLACEHOLDER_REGEX: Regex =
            """%\{([^}:]+)(?::((?:[^}\\]|\\.)*))?\}""".toRegex()
    }

    /**
     * Parses the line content into a list of intermediary nodes.
     *
     * The method identifies and separates animations and variables into structured nodes while preserving plain text.
     *
     * @return A list of [IntermediaryLineNode] representing parsed nodes.
     */
    fun parse(): List<IntermediaryLineNode> {
        val nodes = mutableListOf<IntermediaryLineNode>()
        val animationNodes = parseAnimations()

        for (existingNode in animationNodes) {
            if (existingNode is IntermediaryTextNode) {
                nodes.addAll(parseVariables(existingNode))
            } else {
                nodes.add(existingNode)
            }
        }

        return nodes.toList()
    }

    /**
     * Parses animation placeholders in the line content.
     *
     * @return A list of [IntermediaryLineNode], with placeholders and remaining plain text separated.
     */
    private fun parseAnimations(): List<IntermediaryLineNode> {
        val animationMatches = ANIMATION_PLACEHOLDER_REGEX.findAll(text).toList()
        if (animationMatches.isEmpty()) {
            return listOf(IntermediaryTextNode(text))
        }

        val nodes = mutableListOf<IntermediaryLineNode>()

        var startOfPotentialString = 0
        for (match in animationMatches) {
            val range = match.range

            // Add text before the match as a text node
            if (range.first > startOfPotentialString) {
                val nodeText = text.substring(startOfPotentialString..<range.first)

                nodes.add(IntermediaryTextNode(nodeText))
            }

            // Parse animation placeholder
            val animationName = match.groups[1]?.value
                ?: throw IllegalStateException("Match should contain animation name group")
            val data = match.groups[2]?.value?.escapeData()

            nodes.add(IntermediaryAnimationPlaceholderNode(animationName, data))

            startOfPotentialString = range.last + 1
        }

        // Add remaining text after the last match
        if (startOfPotentialString < text.length) {
            val remainingText: String = text.substring(startOfPotentialString)

            nodes.add(IntermediaryTextNode(remainingText))
        }

        return nodes.toList()
    }

    /**
     * Parses variable placeholders within a given text node.
     *
     * @param textNode The text node to parse for variable placeholders.
     * @return A list of [IntermediaryLineNode], with placeholders and remaining plain text separated.
     */
    private fun parseVariables(textNode: IntermediaryTextNode): List<IntermediaryLineNode> {
        val text = textNode.text
        val animationMatches = VARIABLE_PLACEHOLDER_REGEX.findAll(text).toList()
        if (animationMatches.isEmpty()) {
            return listOf(textNode)
        }

        val nodes = mutableListOf<IntermediaryLineNode>()

        var startOfPotentialString = 0
        for (match in animationMatches) {
            val range = match.range

            // Add text before the match as a text node
            if (range.first > startOfPotentialString) {
                val nodeText = text.substring(startOfPotentialString..<range.first)

                nodes.add(IntermediaryTextNode(nodeText))
            }

            // Parse variable placeholder
            val animationName = match.groups[1]?.value
                ?: throw IllegalStateException("Match should contain variable name group")
            val data = match.groups[2]?.value?.escapeData()

            nodes.add(IntermediaryVariablePlaceholderNode(animationName, data))

            startOfPotentialString = range.last + 1
        }

        // Add remaining text after the last match
        if (startOfPotentialString < text.length) {
            val remainingText: String = text.substring(startOfPotentialString)

            nodes.add(IntermediaryTextNode(remainingText))
        }

        return nodes.toList()
    }

    /**
     * Escapes the placeholder data by replacing escaped braces with literal braces.
     *
     * @receiver The raw placeholder data.
     * @return The processed placeholder data with escaped braces resolved.
     */
    private fun String.escapeData() = replace("\\}", "}")
}
