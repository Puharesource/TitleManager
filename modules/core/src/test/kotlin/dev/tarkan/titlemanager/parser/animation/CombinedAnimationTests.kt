package dev.tarkan.titlemanager.parser.animation

import dev.tarkan.titlemanager.animation.CombinedAnimation
import dev.tarkan.titlemanager.animation.CountdownAnimation
import dev.tarkan.titlemanager.animation.StaticAnimation
import dev.tarkan.titlemanager.parser.IntermediaryParser
import dev.tarkan.titlemanager.parser.animation.serialization.IntAnimationDataSerializer
import dev.tarkan.titlemanager.parser.placeholder.animation.AnimationPlaceholderRegistry
import dev.tarkan.titlemanager.parser.placeholder.variable.VariablePlaceholderRegistry
import dev.tarkan.titlemanager.test.utils.TestFixtures
import dev.tarkan.titlemanager.time.TimedItem
import dev.tarkan.titlemanager.time.Timing
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.Test

class CombinedAnimationTests {
    private lateinit var variablePlaceholderRegistry: VariablePlaceholderRegistry<Unit>
    private lateinit var animationPlaceholderRegistry: AnimationPlaceholderRegistry<Unit>
    private lateinit var intermediaryParser: IntermediaryParser

    @BeforeTest
    fun beforeEach() {
        variablePlaceholderRegistry = VariablePlaceholderRegistry.build {
            addSimple("var1") { "Variable #1" }
            addSimple("var2") { "Variable #2" }
            addSimple("var3") { "Variable #3" }
        }

        animationPlaceholderRegistry = AnimationPlaceholderRegistry.build {
            addSimple("countdown", dataSerializer = IntAnimationDataSerializer) { n ->
                CountdownAnimation(n ?: 0)
            }
        }

        intermediaryParser = IntermediaryParser()
    }

    @Test
    fun `combined animations`() {
        val preParse = intermediaryParser.parseText(TestFixtures.animationFile("animation_file_simple.txt"))
        val animationParser = AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)

        val combinedAnimation = CombinedAnimation(listOf(
            StaticAnimation(TimedItem(Timing.instant, "Countdown: ")),
            CountdownAnimation(5),
            StaticAnimation(TimedItem(Timing.instant, " ")),
            animationParser.parseAnimation(preParse)
        ))

        runTest {
            val expected = listOf("Countdown: 5 line 1", "Countdown: 5 line 2", "Countdown: 4 line 2", "Countdown: 4 line 3", "Countdown: 3 line 3", "Countdown: 3 line 4", "Countdown: 2 line 4", "Countdown: 1 line 4")
            val result = combinedAnimation.flow(Unit).toList()

            assertEquals(expected, result)
        }
    }

    @Test
    fun `combined with variables`() {
        val preParse = intermediaryParser.parseText(TestFixtures.animationFile("animation_file_simple_with_variables.txt"))
        val animationParser = AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)

        val combinedAnimation = CombinedAnimation(listOf(
            CountdownAnimation(5),
            StaticAnimation(TimedItem(Timing.instant, ": ")),
            animationParser.parseAnimation(preParse)
        ))

        runTest {
            val expected = listOf("5: line 1 Variable #1", "5: line 2 Variable #2", "4: line 2 Variable #2", "4: line 3 Variable #3 Variable #1", "3: line 3 Variable #3 Variable #1", "3: line 4 Variable #1 Variable #2", "2: line 4 Variable #1 Variable #2", "1: line 4 Variable #1 Variable #2")
            val result = combinedAnimation.flow(Unit).toList()

            assertEquals(expected, result)
        }
    }

    @Test
    fun `combined with animations`() {
        val preParse = intermediaryParser.parseText(TestFixtures.animationFile("animation_file_combined_with_animations.txt"))
        val animationParser = AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)

        val animation = animationParser.parseAnimation(preParse)

        runTest {
            val expected = listOf("line 1: 3", "line 1: 2", "line 1: 1", "line 2: 3 3", "line 2: 2 2", "line 2: 1 1")
            val result = animation.flow(Unit).toList()

            assertEquals(expected, result)
        }
    }
}