package dev.tarkan.titlemanager.time

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.Test
import kotlin.time.DurationUnit

class TimingTest {
    @Test
    fun `createStatic expects only stay being set`() {
        val expectedStay = 1u
        val expectedTiming = Timing(0u, expectedStay, 0u)
        val actualTiming = Timing.createStatic(expectedStay)

        assertEquals(expectedTiming, actualTiming)
    }

    @Test
    fun `total expects to be sum of all timings`() {
        val timing = Timing(1u, 1u, 1u)

        assertEquals(3u, timing.totalMilliseconds)
    }

    @Test
    fun `createWithDurationUnit expects correct timings`() {
        val expectedTiming = Timing(1000u, 1000u, 1000u)
        val actualTiming = Timing.createWithDurationUnit(1u, 1u, 1u, DurationUnit.SECONDS)

        assertEquals(expectedTiming, actualTiming)
    }

    @Test
    fun `createWithDurationUnit with scale expects correct timings`() {
        val expectedTiming = Timing(50u, 50u, 50u)
        val actualTiming = Timing.createWithDurationUnit(1u, 1u, 1u, DurationUnit.MILLISECONDS, 50u)

        assertEquals(expectedTiming, actualTiming)
    }

    @Test
    fun `times with UInt expects correct timings`() {
        val timing = Timing(5u, 5u, 5u)
        val multiplier = 2u

        val expectedTiming = Timing(10u, 10u, 10u)
        val actualTiming = timing * multiplier

        assertEquals(expectedTiming, actualTiming)
    }

    @Test
    fun `times with Timing expects correct timings`() {
        val timing = Timing(5u, 5u, 5u)
        val multiplier = Timing(2u, 3u, 4u)

        val expectedTiming = Timing(10u, 15u, 20u)
        val actualTiming = timing * multiplier

        assertEquals(expectedTiming, actualTiming)
    }

    @Test
    fun `compareTo expects correct comparison`() {
        val smallerTiming = Timing(1u, 1u, 1u)
        val largerTiming = Timing(2u, 2u, 2u)

        assertTrue(smallerTiming < largerTiming)
        assertTrue(largerTiming > smallerTiming)
    }

    @Test
    fun `equals expects timings with same values to be equal`() {
        val timing1 = Timing(1u, 1u, 1u)
        val timing2 = Timing(1u, 1u, 1u)

        assertEquals(timing1, timing2)
    }

    @Test
    fun `maxOf expects to return the maximum of two timings`() {
        val timing1 = Timing(1u, 5u, 10u)
        val timing2 = Timing(2u, 4u, 8u)

        val expectedMax = Timing(2u, 5u, 10u)
        assertEquals(expectedMax, timing1.maxOf(timing2))
    }

    @Test
    fun `minOf expects to return the minimum of two timings`() {
        val timing1 = Timing(1u, 5u, 10u)
        val timing2 = Timing(2u, 4u, 8u)

        val expectedMin = Timing(1u, 4u, 8u)
        assertEquals(expectedMin, timing1.minOf(timing2))
    }

    @Test
    fun `createStatic with UInt_MAX_VALUE expects correct timing`() {
        val expectedStay = UInt.MAX_VALUE
        val expectedTiming = Timing(0u, expectedStay, 0u)

        assertEquals(expectedTiming, Timing.createStatic(expectedStay))
    }

    @Test
    fun `zero timings expects correct totalMilliseconds`() {
        val timing = Timing(0u, 0u, 0u)

        assertEquals(0u, timing.totalMilliseconds)
    }

    @Test
    fun `totalMilliseconds saturates instead of wrapping`() {
        val timing = Timing(UInt.MAX_VALUE, 1u, 1u)

        assertEquals(UInt.MAX_VALUE, timing.totalMilliseconds)
        assertEquals(UInt.MAX_VALUE, Timing.never.totalMilliseconds)
    }

    @Test
    fun `scale with zero expects zero timings`() {
        val timing = Timing(5u, 5u, 5u)
        val scaledTiming = timing * 0u

        assertEquals(Timing(0u, 0u, 0u), scaledTiming)
    }

    @Test
    fun `scale with one expects unchanged timings`() {
        val timing = Timing(5u, 5u, 5u)
        val scaledTiming = timing * 1u

        assertEquals(timing, scaledTiming)
    }

    @Test
    fun `splitTimingsAndText rejects timing values outside unsigned integer range`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Timing.splitTimingsAndText("[999999999999999999999;1;0]Too large")
        }

        assertEquals("Timing values must be unsigned 32-bit integers", exception.message)
    }

    @Test
    fun `multiplication with large scale expects no overflow`() {
        val timing = Timing(1u, 1u, 1u)
        val scale = UInt.MAX_VALUE

        val expectedTiming = Timing(UInt.MAX_VALUE, UInt.MAX_VALUE, UInt.MAX_VALUE)
        assertEquals(expectedTiming, timing * scale)
    }

    @Test
    fun `multiplication exceeding unsigned integer range fails loudly`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            Timing(2u, 0u, 0u) * UInt.MAX_VALUE
        }

        assertEquals("Timing multiplication exceeds unsigned 32-bit range", exception.message)
    }
}