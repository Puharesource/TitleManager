package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.time.TimedItem
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive

/**
 * Interface representing an animation, providing methods to generate animated flows of items with or without timing.
 *
 * @param TContext The type of the context required to execute the animation.
 * @param TItem The type of item being animated.
 */
interface Animation<TContext, TItem> {
    /**
     * Creates a flow of animated items, without including timing information.
     *
     * @param context The context required to execute the animation.
     * @param isInfinite If `true`, the animation will loop indefinitely; otherwise, it runs once.
     * @return A [Flow] of animated items.
     */
    fun flow(context: TContext, isInfinite: Boolean = false) = flowWithTimings(context, isInfinite).map { it.item }

    /**
     * Creates a flow of animated items, including timing information.
     *
     * @param context The context required to execute the animation.
     * @param isInfinite If `true`, the animation will loop indefinitely; otherwise, it runs once.
     * @return A [Flow] of [TimedItem] containing animated items with their associated timings.
     */
    fun flowWithTimings(context: TContext, isInfinite: Boolean = false): Flow<TimedItem<TItem>> {
        if (isInfinite) {
            return kotlinx.coroutines.flow.flow {
                while (currentCoroutineContext().isActive) {
                    emitAll(singleIterationFlowWithTimings(context, true))
                }
            }
        }

        return singleIterationFlowWithTimings(context)
    }

    /**
     * Creates a single iteration flow of animated items, without including timing information.
     *
     * @param context The context required to execute the animation.
     * @param isInfinite If `true`, the animation will loop within a single iteration; otherwise, it runs once.
     * @return A [Flow] of animated items for a single iteration.
     */
    fun singleIterationFlow(context: TContext, isInfinite: Boolean = false) =
        singleIterationFlowWithTimings(context, isInfinite).map { it.item }

    /**
     * Creates a single iteration flow of animated items, including timing information.
     *
     * @param context The context required to execute the animation.
     * @param isInfinite If `true`, the animation will loop within a single iteration; otherwise, it runs once.
     * @return A [Flow] of [TimedItem] containing animated items with their associated timings for a single iteration.
     */
    fun singleIterationFlowWithTimings(context: TContext, isInfinite: Boolean = false): Flow<TimedItem<TItem>>
}