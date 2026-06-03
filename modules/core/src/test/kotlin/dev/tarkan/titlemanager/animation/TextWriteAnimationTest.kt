package dev.tarkan.titlemanager.animation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.Test

class TextWriteAnimationTest {
    @Test
    fun `write text`() = runTest {
        val textWriteAnimation = TextWriteAnimation<Unit>("input")

        val result = textWriteAnimation.flow(Unit).toList()
        assertEquals(listOf("", "i", "in", "inp", "inpu", "input"), result)
    }

    @Test
    fun `write text empty`() = runTest {
        val textWriteAnimation = TextWriteAnimation<Unit>("")

        val result = textWriteAnimation.flow(Unit).toList()
        assertEquals(emptyList<String>(), result)
    }
}