package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing
import kotlinx.coroutines.flow.emitAll

/**
 * Represents an animation that plays a sequence of other animations in order.
 *
 * @param TContext The type of context required to execute the animations.
 * @property animations A list of animations to be played sequentially.
 */
class SequenceAnimation<TContext>(private val animations: List<Animation<TContext, String>>) :
    TimelineAnimation<TContext, String> {
    /**
     * Produces a flow that emits items from each animation in the sequence, one after the other.
     *
     * @param context The context required to execute the animations.
     * @param isInfinite If `true`, the animations will repeat infinitely; otherwise, they will play once in sequence.
     * @return A flow of [dev.tarkan.titlemanager.time.TimedItem] containing the items and their timing information for the entire sequence.
     */
    override fun singleIterationFlowWithTimings(context: TContext, isInfinite: Boolean) = kotlinx.coroutines.flow.flow {
        for (animation in animations) {
            val flow = animation.flowWithTimings(context)

            emitAll(flow)
        }

    }

    override fun singleIterationFramesWithTimings(context: TContext, isInfinite: Boolean): List<TimedItem<String>> {
        val items = mutableListOf<TimedItem<String>>()

        for (animation in animations) {
            val timelineAnimation = animation.asTimelineAnimation()

            for (item in timelineAnimation.singleIterationFramesWithTimings(context)) {
                items.add(item)

                if (item.timing == Timing.never) {
                    return items
                }
            }
        }

        return items
    }

    @Suppress("UNCHECKED_CAST")
    private fun Animation<TContext, String>.asTimelineAnimation(): TimelineAnimation<TContext, String> =
        this as? TimelineAnimation<TContext, String>
            ?: error("Animation does not expose deterministic timeline frames")
}