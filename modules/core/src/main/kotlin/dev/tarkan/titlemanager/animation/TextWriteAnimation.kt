package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.animation.TextWriteAnimation.Companion.DEFAULT_DELAY
import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing

/**
 * Represents an animation that writes a string character by character over time,
 * creating a "text write" effect.
 *
 * @param TContext The type of context required to execute the animation.
 * @property text The text to be gradually written.
 * @property delay The delay (in milliseconds) between each character being written. Defaults to [DEFAULT_DELAY].
 */
class TextWriteAnimation<TContext>(private val text: String, private val delay: Long = DEFAULT_DELAY) :
    TimelineAnimation<TContext, String> {
    private companion object {
        /**
         * Default delay time between writing each character, measured in milliseconds.
         */
        private const val DEFAULT_DELAY = 1000L
    }

    /**
     * Produces a flow that emits progressively written substrings of the original text.
     *
     * For each frame, the animation emits a substring of the text starting from an empty string
     * and progressively growing until the full text is displayed.
     *
     * @param context The context required to execute the animation.
     * @param isInfinite If `true`, the animation will loop infinitely.
     * @return A flow of [TimedItem]s where each item represents a partially written version of the text.
     */
    override fun singleIterationFramesWithTimings(context: TContext, isInfinite: Boolean): List<TimedItem<String>> {
        if (text.isEmpty()) {
            return emptyList()
        }

        return (0..text.length).map { i ->
            TimedItem(Timing.createStatic(delay.toUInt()), text.substring(0, i))
        }
    }
}