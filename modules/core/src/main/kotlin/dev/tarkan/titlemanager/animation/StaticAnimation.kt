package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing

/**
 * Represents a static animation where a predefined list of items is emitted in sequence, with each item
 * displayed for its associated timing.
 *
 * @param TContext The type of context required to execute the animation.
 * @param TItem The type of item being animated.
 * @property items A list of [TimedItem]s representing the items and their associated timings.
 */
class StaticAnimation<TContext, TItem>(private val items: List<TimedItem<TItem>>) : TimelineAnimation<TContext, TItem> {
    /**
     * Constructs a [StaticAnimation] with a single item.
     *
     * @param item The single [TimedItem] to be emitted.
     */
    constructor(item: TimedItem<TItem>) : this(listOf(item))

    /**
     * Produces a flow that emits the items in the animation, along with their associated timings.
     *
     * If the animation is infinite, each item is emitted with a [Timing.never] value to indicate no expiration.
     *
     * @param context The context required to execute the animation.
     * @param isInfinite If `true`, the animation will loop infinitely.
     * @return A flow of [TimedItem]s representing each item in the animation.
     */
    override fun singleIterationFramesWithTimings(context: TContext, isInfinite: Boolean): List<TimedItem<TItem>> =
        items.map { timedItem ->
            val timing = if (isInfinite) {
                Timing.never
            } else {
                timedItem.timing
            }

            TimedItem(timing, timedItem.item)
        }
}