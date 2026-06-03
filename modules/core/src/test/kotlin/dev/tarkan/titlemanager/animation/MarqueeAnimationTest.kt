package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.time.Timing
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.Test

class MarqueeAnimationTest {
    @Test
    fun `1 width marquee`() = runTest {
        val textWriteAnimation = MarqueeAnimation<Unit>("input", Timing.default, 1)

        val result = textWriteAnimation.flow(Unit).toList()
        assertEquals(listOf("i", "n", "p", "u", "t"), result)
    }

    @Test
    fun `2 width marquee`() = runTest {
        val textWriteAnimation = MarqueeAnimation<Unit>("input", Timing.default, 2)

        val result = textWriteAnimation.flow(Unit).toList()
        assertEquals(listOf("in", "np", "pu", "ut", "ti"), result)
    }

    @Test
    fun `3 width marquee`() = runTest {
        val textWriteAnimation = MarqueeAnimation<Unit>("input", Timing.default, 3)

        val result = textWriteAnimation.flow(Unit).toList()
        assertEquals(listOf("inp", "npu", "put", "uti", "tin"), result)
    }

    @Test
    fun `full width marquee`() = runTest {
        val textWriteAnimation = MarqueeAnimation<Unit>("input", Timing.default)

        val result = textWriteAnimation.flow(Unit).toList()
        assertEquals(listOf("input", "nputi", "putin", "utinp", "tinpu"), result)
    }
}