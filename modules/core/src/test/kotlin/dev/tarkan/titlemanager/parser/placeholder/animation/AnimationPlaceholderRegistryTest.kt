package dev.tarkan.titlemanager.parser.placeholder.animation

import dev.tarkan.titlemanager.animation.CountdownAnimation
import dev.tarkan.titlemanager.parser.IntermediaryParser
import dev.tarkan.titlemanager.parser.animation.AnimationParser
import dev.tarkan.titlemanager.parser.animation.serialization.IntAnimationDataSerializer
import dev.tarkan.titlemanager.parser.placeholder.variable.VariablePlaceholderRegistry
import dev.tarkan.titlemanager.test.utils.TestFixtures
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.Test

class AnimationPlaceholderRegistryTest {
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
        val preParse = intermediaryParser.parseText(TestFixtures.animationFile("animation_file_simple_with_animations.txt"))
        val animationParser = AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)

        val combinedAnimation = animationParser.parseAnimation(preParse)

        runTest {
            val expected = listOf("line 1 5", "line 1 4", "line 1 3", "line 1 2", "line 1 1", "line 2 3", "line 2 2", "line 2 1")
            val result = combinedAnimation.flow(Unit).toList()

            assertEquals(expected, result)
        }
    }

    @Test
    fun `core animation placeholders exposes shared built ins`() {
        val registry = AnimationPlaceholderRegistry.build<Unit> {
            addCoreAnimationPlaceholders()
        }

        assertEquals(
            setOf("countdown", "text_write", "text_delete", "marquee", "shine"),
            registry.keys
        )
    }
}