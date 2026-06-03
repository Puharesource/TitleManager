package dev.tarkan.titlemanager.animation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.Test

class TextDeleteAnimationTest {
    @Test
    fun `delete text`() = runTest {
        val textWriteAnimation = TextDeleteAnimation<Unit>("input")

        val result = textWriteAnimation.flow(Unit).toList()
        assertEquals(listOf("input", "inpu", "inp", "in", "i", ""), result)
    }

    @Test
    fun `delete text empty`() = runTest {
        val textWriteAnimation = TextWriteAnimation<Unit>("")

        val result = textWriteAnimation.flow(Unit).toList()
        assertEquals(emptyList<String>(), result)
    }
}