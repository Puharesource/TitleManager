package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.parser.placeholder.variable.VariablePlaceholder
import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing

/**
 * Represents an animation that uses a variable placeholder to generate dynamic content
 * based on the provided context.
 *
 * @param TContext The type of context required for evaluating the variable placeholder.
 * @property placeholder The placeholder used to dynamically generate content.
 * @property timing The timing configuration for the animation when not running infinitely.
 * @property unprocessedData Optional data to be processed and passed to the placeholder.
 */
class VariablePlaceholderAnimation<TContext>(
    private val placeholder: VariablePlaceholder<TContext, String>,
    private val timing: Timing,
    private val unprocessedData: String?
) : TimelineAnimation<TContext, String> {
    /**
     * Generates a flow that emits a dynamically generated string based on the placeholder
     * and context, along with the timing configuration.
     *
     * @param context The context required to evaluate the placeholder.
     * @param isInfinite If `true`, the animation will continue emitting dynamically generated
     *                   content indefinitely, using the placeholder's cache time for timing.
     * @return A flow of [TimedItem]s where each item contains the dynamically generated string
     *         and its associated timing configuration.
     */
    override fun singleIterationFramesWithTimings(context: TContext, isInfinite: Boolean): List<TimedItem<String>> {
        val compiledVariablePlaceholder = placeholder.compile(unprocessedData)
        val currentTiming: Timing = if (isInfinite) {
            Timing.createWithDurationUnit(0u, placeholder.cacheTime, 0u, placeholder.durationUnit)
        } else {
            timing
        }


        return listOf(TimedItem(currentTiming, compiledVariablePlaceholder.replace(context)))
    }

    override fun singleIterationTimeline(context: TContext, isInfinite: Boolean): AnimationTimeline<String> =
        AnimationTimeline.fromTimedItems(singleIterationFramesWithTimings(context, isInfinite), isInfinite)
}