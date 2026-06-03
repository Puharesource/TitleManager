package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing

/**
 * An animation that displays a series of numbers within a defined range, each for a specified duration.
 *
 * @param TContext The type of context required to execute the animation.
 */
class CountAnimation<TContext> : TimelineAnimation<TContext, String> {
    private companion object {
        /** The default delay (in milliseconds) between each number in the count animation. */
        private const val DEFAULT_DELAY = 1000L
    }

    /** The range of numbers to be displayed in the animation. */
    private val range: IntRange

    /** The delay (in milliseconds) between displaying each number. */
    private val delay: Long

    /**
     * Creates a `CountAnimation` with a custom range and delay.
     *
     * @param range The range of numbers to display.
     * @param delay The delay (in milliseconds) between each number. Defaults to [DEFAULT_DELAY].
     */
    constructor(range: IntRange, delay: Long = DEFAULT_DELAY) {
        this.range = range
        this.delay = delay
    }

    /**
     * Creates a `CountAnimation` with numbers from 1 to the specified end value and a custom delay.
     *
     * @param end The last number to display in the range.
     * @param delay The delay (in milliseconds) between each number. Defaults to [DEFAULT_DELAY].
     */
    constructor(end: Int, delay: Long = DEFAULT_DELAY) {
        this.range = 1..end
        this.delay = delay
    }

    /**
     * Produces a flow that emits timed items for each number in the range.
     *
     * @param context The context required to execute the animation.
     * @param isInfinite If `true`, the animation will loop indefinitely; otherwise, it runs once. (Ignored in this implementation).
     * @return A flow of [TimedItem] where each item represents a number in the range with its corresponding timing.
     */
    override fun singleIterationFramesWithTimings(context: TContext, isInfinite: Boolean): List<TimedItem<String>> =
        range.map { i ->
            TimedItem(Timing.createStatic(delay.toUInt()), i.toString())
        }
}