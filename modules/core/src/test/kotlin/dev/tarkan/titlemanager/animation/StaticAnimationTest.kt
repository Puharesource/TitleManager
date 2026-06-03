package dev.tarkan.titlemanager.animation

import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.Test

class StaticAnimationTest {
    @Test
    fun `flow get all items expects correct items`() = runTest {
        val items: List<TimedItem<String>> = listOf(
            TimedItem(Timing.createStatic(1000u), "hello"),
            TimedItem(Timing.createStatic(1000u), "world"),
            TimedItem(Timing.createStatic(1000u), "!!!"),
        )

        val staticAnimation = StaticAnimation<Unit, String>(items)

        staticAnimation.flow(Unit).collectIndexed { index, item ->
            val actualTimedItem = items[index]

            assertEquals(actualTimedItem.item, item)
        }
    }

    @Test
    fun `flow combined items expects correct items`() = runTest {
        val firstItems: List<TimedItem<String>> = listOf(
            TimedItem(Timing.createStatic(3000u), "hello"),
            TimedItem(Timing.createStatic(1000u), "bye")
        )

        val secondItems: List<TimedItem<String>> = listOf(
            TimedItem(Timing.createStatic(2000u), "world"),
            TimedItem(Timing.createStatic(2000u), "foo"),
            TimedItem(Timing.createStatic(2000u), "bar"),
        )

        val firstAnimation = StaticAnimation<Unit, String>(firstItems)
        val secondAnimation = StaticAnimation<Unit, String>(secondItems)

        val expectedResults = listOf("hello world", "hello foo", "bye foo", "bye bar")

        firstAnimation.flow(Unit).combine(secondAnimation.flow(Unit)) { first, second -> "$first $second" }.collectIndexed { index, item ->
            assertEquals(expectedResults[index], item)
        }
    }
}