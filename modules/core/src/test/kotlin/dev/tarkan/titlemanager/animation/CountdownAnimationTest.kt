package dev.tarkan.titlemanager.animation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.Test

class CountdownAnimationTest {
    @Test
    fun `ctor with start number`() = runTest {
        val countdownAnimation = CountdownAnimation<Unit>(5)

        val result = countdownAnimation.flow(Unit).toList()
        assertEquals(listOf("5", "4", "3", "2", "1"), result)
    }

    @Test
    fun `ctor with range`() = runTest {
        val countdownAnimation = CountdownAnimation<Unit>(5..10)

        val result = countdownAnimation.flow(Unit).toList()
        assertEquals(listOf("10", "9", "8", "7", "6", "5"), result)
    }
}