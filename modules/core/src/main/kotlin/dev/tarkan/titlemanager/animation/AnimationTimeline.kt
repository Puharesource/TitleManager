package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A materialized, scheduler-free view of animation output over time.
 */
class AnimationTimeline<TItem> private constructor(
    val frames: List<TimelineFrame<TItem>>,
    val isInfinite: Boolean
) {
    val totalMilliseconds: Long? = if (isInfinite) {
        null
    } else {
        frames.lastOrNull()?.endMilliseconds ?: 0L
    }

    fun frameAt(milliseconds: Long): TimelineFrame<TItem>? {
        require(milliseconds >= 0) { "milliseconds must be greater than or equal to 0" }

        return frames.firstOrNull { it.contains(milliseconds) }
            ?: frames.lastOrNull()?.takeIf { isInfinite && milliseconds >= it.startMilliseconds }
    }

    fun itemAt(milliseconds: Long): TItem? = frameAt(milliseconds)?.item

    fun latestFrameAtOrBefore(milliseconds: Long): TimelineFrame<TItem>? {
        require(milliseconds >= 0) { "milliseconds must be greater than or equal to 0" }

        return frames.lastOrNull { it.startMilliseconds <= milliseconds }
    }

    fun toTimedItems(): List<TimedItem<TItem>> = frames.map { TimedItem(it.timing, it.item) }

    companion object {
        fun <TItem> fromTimedItems(
            items: List<TimedItem<TItem>>,
            isInfinite: Boolean = false
        ): AnimationTimeline<TItem> {
            val frames = mutableListOf<TimelineFrame<TItem>>()
            var startMilliseconds = 0L
            var hasNeverEndingFrame = false

            for (item in items) {
                frames.add(TimelineFrame(startMilliseconds, item.timing, item.item))

                if (item.timing == Timing.never) {
                    hasNeverEndingFrame = true
                    break
                }

                startMilliseconds += item.timing.totalMilliseconds.toLong()
            }

            return AnimationTimeline(frames, isInfinite || hasNeverEndingFrame)
        }
    }
}

data class TimelineFrame<TItem>(
    val startMilliseconds: Long,
    val timing: Timing,
    val item: TItem
) {
    val endMilliseconds: Long? = if (timing == Timing.never) {
        null
    } else {
        startMilliseconds + timing.totalMilliseconds.toLong()
    }

    fun contains(milliseconds: Long): Boolean {
        require(milliseconds >= 0) { "milliseconds must be greater than or equal to 0" }

        if (milliseconds < startMilliseconds) {
            return false
        }

        val end = endMilliseconds ?: return true
        return if (end == startMilliseconds) {
            milliseconds == startMilliseconds
        } else {
            milliseconds < end
        }
    }
}

interface TimelineAnimation<TContext, TItem> : Animation<TContext, TItem> {
    fun singleIterationFramesWithTimings(context: TContext, isInfinite: Boolean = false): List<TimedItem<TItem>>

    fun singleIterationTimeline(context: TContext, isInfinite: Boolean = false): AnimationTimeline<TItem> =
        AnimationTimeline.fromTimedItems(singleIterationFramesWithTimings(context, isInfinite))

    override fun singleIterationFlowWithTimings(context: TContext, isInfinite: Boolean): Flow<TimedItem<TItem>> = flow {
        for (item in singleIterationFramesWithTimings(context, isInfinite)) {
            emit(item)
            delay(item.timing.flowDelayMilliseconds())
        }
    }
}

private fun Timing.flowDelayMilliseconds(): Long = totalMilliseconds.toLong()
