package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.parser.IntermediaryParser
import dev.tarkan.titlemanager.parser.animation.AnimationParser
import dev.tarkan.titlemanager.parser.placeholder.animation.AnimationPlaceholderRegistry
import dev.tarkan.titlemanager.parser.placeholder.variable.VariablePlaceholderRegistry
import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.time.DurationUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnimationTimelineTest {
    @Test
    fun `timeline exposes deterministic frame boundaries`() {
        val timeline = StaticAnimation<Unit, String>(
            listOf(
                TimedItem(Timing.createStatic(100u), "first"),
                TimedItem(Timing.createStatic(200u), "second")
            )
        ).singleIterationTimeline(Unit)

        assertFalse(timeline.isInfinite)
        assertEquals(300L, timeline.totalMilliseconds)
        assertEquals(0L, timeline.frames[0].startMilliseconds)
        assertEquals(100L, timeline.frames[1].startMilliseconds)
        assertEquals("first", timeline.itemAt(0L))
        assertEquals("first", timeline.itemAt(99L))
        assertEquals("second", timeline.itemAt(100L))
        assertEquals("second", timeline.itemAt(299L))
        assertNull(timeline.itemAt(300L))
    }

    @Test
    fun `infinite timeline treats never timing as terminal hold`() {
        val timeline = StaticAnimation<Unit, String>(
            listOf(
                TimedItem(Timing.createStatic(100u), "first"),
                TimedItem(Timing.createStatic(100u), "second")
            )
        ).singleIterationTimeline(Unit, isInfinite = true)

        assertTrue(timeline.isInfinite)
        assertNull(timeline.totalMilliseconds)
        assertEquals(1, timeline.frames.size)
        assertEquals("first", timeline.itemAt(0L))
        assertEquals("first", timeline.itemAt(10_000_000L))
    }

    @Test
    fun `text write animation materializes frames without collecting a flow`() {
        val frames = TextWriteAnimation<Unit>("input").singleIterationFramesWithTimings(Unit)

        assertEquals(listOf("", "i", "in", "inp", "inpu", "input"), frames.map { it.item })
        assertEquals(List(6) { Timing.createStatic(1000u) }, frames.map { it.timing })
    }

    @Test
    fun `variable placeholder exposes finite and cache-timed frames`() {
        val placeholder = VariablePlaceholderRegistry.build<Unit> {
            addWithContextNoData("value", cacheTime = 50u, durationUnit = DurationUnit.MILLISECONDS) { "dynamic" }
        }["value"]!!
        val animation = VariablePlaceholderAnimation(placeholder, Timing.createStatic(200u), null)

        val finiteFrame = animation.singleIterationFramesWithTimings(Unit).single()
        val infiniteFrame = animation.singleIterationFramesWithTimings(Unit, isInfinite = true).single()
        val infiniteTimeline = animation.singleIterationTimeline(Unit, isInfinite = true)

        assertEquals(Timing.createStatic(200u), finiteFrame.timing)
        assertEquals(Timing.createStatic(50u), infiniteFrame.timing)
        assertEquals("dynamic", finiteFrame.item)
        assertTrue(infiniteTimeline.isInfinite)
        assertNull(infiniteTimeline.totalMilliseconds)
    }

    @Test
    fun `variable placeholder flow re-evaluates each cache interval`() = runTest {
        var value = 0
        val placeholder = VariablePlaceholderRegistry.build<Unit> {
            addWithContextNoData("value", cacheTime = 1u, durationUnit = DurationUnit.MILLISECONDS) {
                value += 1
                value.toString()
            }
        }["value"]!!
        val animation = VariablePlaceholderAnimation(placeholder, Timing.createStatic(200u), null)

        assertEquals(listOf("1", "2"), animation.flow(Unit, isInfinite = true).take(2).toList())
    }

    @Test
    fun `sequence timeline concatenates child timelines`() {
        val sequence = SequenceAnimation(
            listOf(
                StaticAnimation<Unit, String>(TimedItem(Timing.createStatic(100u), "first")),
                TextWriteAnimation("ab", delay = 50L)
            )
        )

        val timeline = sequence.singleIterationTimeline(Unit)

        assertEquals(listOf("first", "", "a", "ab"), timeline.frames.map { it.item })
        assertEquals(listOf(0L, 100L, 150L, 200L), timeline.frames.map { it.startMilliseconds })
        assertEquals(250L, timeline.totalMilliseconds)
    }

    @Test
    fun `sequence timeline stops after a never-ending child frame`() {
        val sequence = SequenceAnimation(
            listOf(
                StaticAnimation<Unit, String>(TimedItem(Timing.never, "hold")),
                StaticAnimation(TimedItem(Timing.createStatic(100u), "unreachable"))
            )
        )

        val timeline = sequence.singleIterationTimeline(Unit)

        assertTrue(timeline.isInfinite)
        assertEquals(listOf("hold"), timeline.frames.map { it.item })
        assertEquals("hold", timeline.itemAt(10_000L))
    }

    @Test
    fun `combined timeline uses union change points and holds shorter child timelines`() {
        val combined = CombinedAnimation(
            listOf(
                StaticAnimation<Unit, String>(
                    listOf(
                        TimedItem(Timing.createStatic(100u), "A"),
                        TimedItem(Timing.createStatic(100u), "B")
                    )
                ),
                StaticAnimation(TimedItem(Timing.createStatic(150u), "X"))
            )
        )

        val timeline = combined.singleIterationTimeline(Unit)

        assertEquals(listOf("AX", "BX"), timeline.frames.map { it.item })
        assertEquals(listOf(0L, 100L), timeline.frames.map { it.startMilliseconds })
        assertEquals(listOf(Timing.createStatic(100u), Timing.createStatic(100u)), timeline.frames.map { it.timing })
        assertEquals("BX", timeline.itemAt(175L))
    }

    @Test
    fun `combined timeline can be produced from parsed animation lines`() {
        val parser = IntermediaryParser()
        val variablePlaceholderRegistry = VariablePlaceholderRegistry.build<Unit> {
            addWithContextNoData("name") { "Player" }
        }
        val animationParser = AnimationParser(variablePlaceholderRegistry, AnimationPlaceholderRegistry.build { })
        val animation = animationParser.parseAnimation(parser.parseText("[100]Hello %{name}\n[100]Bye %{name}"))
        val timeline = (animation as TimelineAnimation<Unit, String>).singleIterationTimeline(Unit)

        assertEquals(listOf("Hello Player", "Bye Player"), timeline.frames.map { it.item })
        assertEquals(listOf(0L, 100L), timeline.frames.map { it.startMilliseconds })
        assertEquals(200L, timeline.totalMilliseconds)
    }
}
