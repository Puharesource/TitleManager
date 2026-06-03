package dev.tarkan.titlemanager.parser.animation

import dev.tarkan.titlemanager.animation.*
import dev.tarkan.titlemanager.parser.*
import dev.tarkan.titlemanager.parser.placeholder.animation.AnimationPlaceholderRegistry
import dev.tarkan.titlemanager.parser.placeholder.variable.VariablePlaceholderRegistry
import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing

class AnimationParser<TContext>(private val variablePlaceholderRegistry: VariablePlaceholderRegistry<TContext>, private val animationPlaceholderRegistry: AnimationPlaceholderRegistry<TContext>) {
    private companion object {
        private const val UNKNOWN_VARIABLE_PLACEHOLDER = "Unknown-Variable-Placeholder"
        private const val UNKNOWN_ANIMATION_PLACEHOLDER = "Unknown-Animation-Placeholder"
    }

    fun parseAnimation(line: IntermediaryParsedAnimationLine): Animation<TContext, String> {
        val parsedLines = line.parse().map { it.toParsedString(line.timing) }

        return CombinedAnimation(parsedLines)
    }

    fun parseAnimation(lines: List<IntermediaryParsedAnimationLine>): Animation<TContext, String> {
        val parsedLines = lines.map { parseAnimation(it) }

        return SequenceAnimation(parsedLines)
    }

    private fun IntermediaryLineNode.toParsedString(timing: Timing = Timing.default): Animation<TContext, String> {
        return when(this) {
            is IntermediaryTextNode -> StaticAnimation(TimedItem(timing, text))
            is IntermediaryVariablePlaceholderNode -> variablePlaceholderRegistry[id]?.let { VariablePlaceholderAnimation(it, timing, data) } ?: StaticAnimation(TimedItem(Timing.never, UNKNOWN_VARIABLE_PLACEHOLDER))
            is IntermediaryAnimationPlaceholderNode -> animationPlaceholderRegistry[id]?.compile(data) ?: StaticAnimation(TimedItem(Timing.never, UNKNOWN_ANIMATION_PLACEHOLDER))
            else -> throw IllegalArgumentException("Unknown IntermediaryLineNode")
        }
    }
}