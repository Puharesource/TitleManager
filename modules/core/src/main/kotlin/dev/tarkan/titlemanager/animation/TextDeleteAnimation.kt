package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.animation.TextDeleteAnimation.Companion.DEFAULT_DELAY
import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing

/**
 * Represents an animation that deletes characters from a given string one by one in reverse order,
 * creating a "text delete" effect.
 *
 * @param TContext The type of context required to execute the animation.
 * @property text The text to be gradually deleted.
 * @property delay The delay (in milliseconds) between each character deletion. Defaults to [DEFAULT_DELAY].
 */
class TextDeleteAnimation<TContext>(private val text: String, private val delay: Long = DEFAULT_DELAY) :
    TimelineAnimation<TContext, String> {
    private companion object {
        /**
         * Default delay time between each character deletion, measured in milliseconds.
         */
        private const val DEFAULT_DELAY = 1000L
    }

    /**
     * Produces a flow that emits the gradually deleted substrings of the original text.
     *
     * For each frame, the animation emits a substring of the text starting from its full length
     * and progressively reducing until it's empty.
     *
     * @param context The context required to execute the animation.
     * @param isInfinite If `true`, the animation will loop infinitely.
     * @return A flow of [TimedItem]s where each item represents a partially deleted version of the text.
     */
    override fun singleIterationFramesWithTimings(context: TContext, isInfinite: Boolean): List<TimedItem<String>> {
        if (text.isEmpty()) {
            return emptyList()
        }

        return (text.length downTo 0).map { i ->
            TimedItem(Timing.createStatic(delay.toUInt()), text.substring(0, i))
        }
    }
}