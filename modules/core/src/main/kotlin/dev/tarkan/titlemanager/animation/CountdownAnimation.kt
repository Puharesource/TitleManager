package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing

/**
 * An animation that counts down from a specified range or value, displaying each number for a defined duration.
 *
 * @param TContext The type of context required to execute the animation.
 */
class CountdownAnimation<TContext> : TimelineAnimation<TContext, String> {
    private companion object {
        /** The default delay (in milliseconds) between each number in the countdown animation. */
        private const val DEFAULT_DELAY = 1000L
    }

    /** The range of numbers to be displayed in the countdown animation. */
    private val range: IntRange

    /** The delay (in milliseconds) between displaying each number. */
    private val delay: Long

    /**
     * Creates a `CountdownAnimation` with a custom range and delay.
     *
     * @param range The range of numbers to count down from.
     * @param delay The delay (in milliseconds) between each number. Defaults to [DEFAULT_DELAY].
     */
    constructor(range: IntRange, delay: Long = DEFAULT_DELAY) {
        this.range = range
        this.delay = delay
    }

    /**
     * Creates a `CountdownAnimation` starting from the specified value down to 1 with a custom delay.
     *
     * @param start The starting number of the countdown.
     * @param delay The delay (in milliseconds) between each number. Defaults to [DEFAULT_DELAY].
     */
    constructor(start: Int, delay: Long = DEFAULT_DELAY) {
        this.range = 1..start
        this.delay = delay
    }

    /**
     * Produces a flow that emits timed items for each number in the countdown.
     *
     * @param context The context required to execute the animation.
     * @param isInfinite If `true`, the animation will loop indefinitely; otherwise, it runs once. (Ignored in this implementation).
     * @return A flow of [TimedItem] where each item represents a number in the countdown with its corresponding timing.
     */
    override fun singleIterationFramesWithTimings(context: TContext, isInfinite: Boolean): List<TimedItem<String>> =
        range.reversed().map { i ->
            TimedItem(Timing.createStatic(delay.toUInt()), i.toString())
        }
}