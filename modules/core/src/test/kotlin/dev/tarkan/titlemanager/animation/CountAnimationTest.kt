package dev.tarkan.titlemanager.animation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.Test

class CountAnimationTest {
    @Test
    fun `ctor with start number`() = runTest {
        val countdownAnimation = CountAnimation<Unit>(5)

        val result = countdownAnimation.flow(Unit).toList()
        assertEquals(listOf("1", "2", "3", "4", "5"), result)
    }

    @Test
    fun `ctor with range`() = runTest {
        val countdownAnimation = CountAnimation<Unit>(5..10)

        val result = countdownAnimation.flow(Unit).toList()
        assertEquals(listOf("5", "6", "7", "8", "9", "10"), result)
    }
}