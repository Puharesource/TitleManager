package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce

/**
 * A class that combines multiple animations into a single animation, merging their outputs.
 *
 * @param TContext The type of context required to execute the animations.
 * @property animations The list of animations to combine.
 */
class CombinedAnimation<TContext>(private val animations: List<Animation<TContext, String>>) :
    TimelineAnimation<TContext, String> {
    @OptIn(FlowPreview::class)
    override fun singleIterationFlowWithTimings(context: TContext, isInfinite: Boolean): Flow<TimedItem<String>> {
        var firstValueShowed = false

        return animations
            .map { it.flowWithTimings(context, isInfinite) }
            .reduce { accumulatedFlow, otherFlow ->
                accumulatedFlow.combine(otherFlow) { item1, item2 ->
                    TimedItem(item1.timing.minOf(item2.timing), "${item1.item}${item2.item}")
                }
            }
            .debounce {
                if (firstValueShowed) {
                    10L
                } else {
                    firstValueShowed = true
                    0L
                }
            }
    }

    override fun singleIterationFramesWithTimings(context: TContext, isInfinite: Boolean): List<TimedItem<String>> {
        if (animations.isEmpty()) {
            return emptyList()
        }

        val timelines = animations.map { it.asTimelineAnimation().singleIterationTimeline(context, isInfinite) }

        if (timelines.any { it.frames.isEmpty() }) {
            return emptyList()
        }

        val finiteEndMilliseconds = timelines.mapNotNull { it.totalMilliseconds }.maxOrNull() ?: 0L
        val isCombinedInfinite = isInfinite || timelines.any { it.isInfinite }
        val changeTimes = timelines
            .flatMap { timeline -> timeline.frames.map { it.startMilliseconds } }
            .filter { isCombinedInfinite || it <= finiteEndMilliseconds }
            .toSet()
            .sorted()

        if (changeTimes.isEmpty()) {
            return emptyList()
        }

        return changeTimes.mapIndexed { index, startMilliseconds ->
            val childFrames = timelines.map { timeline ->
                timeline.latestFrameAtOrBefore(startMilliseconds)
                    ?: error("Timeline has no frame at or before $startMilliseconds ms")
            }
            val nextStartMilliseconds = changeTimes.getOrNull(index + 1)
                ?: if (isCombinedInfinite) null else finiteEndMilliseconds
            val timing = nextStartMilliseconds?.let {
                Timing.createStatic((it - startMilliseconds).toUIntMilliseconds())
            } ?: Timing.never

            TimedItem(timing, childFrames.joinToString(separator = "") { it.item })
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Animation<TContext, String>.asTimelineAnimation(): TimelineAnimation<TContext, String> =
        this as? TimelineAnimation<TContext, String>
            ?: error("Animation does not expose deterministic timeline frames")

    private fun Long.toUIntMilliseconds(): UInt {
        require(this >= 0) { "Timeline duration cannot be negative" }
        require(this <= UInt.MAX_VALUE.toLong()) { "Timeline duration exceeds UInt millisecond range" }

        return toUInt()
    }
}
