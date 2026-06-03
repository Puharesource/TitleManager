package dev.tarkan.titlemanager.parser.animation

import dev.tarkan.titlemanager.parser.IntermediaryParser
import dev.tarkan.titlemanager.parser.placeholder.animation.AnimationPlaceholderRegistry
import dev.tarkan.titlemanager.parser.placeholder.variable.VariablePlaceholderRegistry
import dev.tarkan.titlemanager.test.utils.TestFixtures
import dev.tarkan.titlemanager.time.Timing
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.Test

class AnimationParserTest {
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

        }

        intermediaryParser = IntermediaryParser()
    }

    @Test
    fun `parseAnimation expects correct output`() {
        val preParse = intermediaryParser.parseText(TestFixtures.animationFile("animation_file_all.txt"))
        val animationParser = AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)

        runTest {
            val result = animationParser.parseAnimation(preParse).flow(Unit).toList()
            val expected = listOf("line 1", "line 2", "line 3", "line 4", "line 5", "line 6", "line 7", "line 8")

            assertEquals(expected, result)
        }
    }

    @Test
    fun `parseAnimation with no variables or animations expects correct output`() {
        val input = "Welcome to my server!"
        val preParse = intermediaryParser.parseLine(input)
        val animationParser = AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)

        runTest {
            val result = animationParser.parseAnimation(preParse).flowWithTimings(Unit).toList()

            assertEquals(1, result.size)
            assertEquals(input, result.first().item)
            assertEquals(Timing.default, result.first().timing)
        }
    }

    @Test
    fun `parseAnimation with multiple variables in one line expects correct output`() {
        val input = "Welcome %{var1} and %{var2} to %{var3}!"
        val preParse = intermediaryParser.parseLine(input)
        val animationParser = AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)

        runTest {
            val result = animationParser.parseAnimation(preParse).flowWithTimings(Unit).toList()

            assertEquals(1, result.size)
            assertEquals("Welcome Variable #1 and Variable #2 to Variable #3!", result.first().item)
            assertEquals(Timing.default, result.first().timing)
        }
    }

    @Test
    fun `parseAnimation with empty input expects no output`() {
        val input = ""
        val preParse = intermediaryParser.parseLine(input)
        val animationParser = AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)

        runTest {
            val result = animationParser.parseAnimation(preParse).flowWithTimings(Unit).toList()

            assertEquals(1, result.size)
        }
    }

    @Test
    fun `parseAnimation with custom timings expects correct output`() {
        val input = "[100;200;300]Timed line"
        val preParse = intermediaryParser.parseLine(input)
        val animationParser = AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)

        runTest {
            val result = animationParser.parseAnimation(preParse).flowWithTimings(Unit).toList()

            assertEquals(1, result.size)
            assertEquals("Timed line", result.first().item)
            assertEquals(Timing(100u, 200u, 300u), result.first().timing)
        }
    }

    @Test
    fun `parseAnimation with invalid placeholders expects correct output`() {
        val input = "Invalid \${unknown} and %{unknown}"
        val preParse = intermediaryParser.parseLine(input)
        val animationParser = AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)

        runTest {
            val result = animationParser.parseAnimation(preParse).flowWithTimings(Unit).toList()

            assertEquals(1, result.size)
            assertEquals("Invalid Unknown-Animation-Placeholder and Unknown-Variable-Placeholder", result.first().item)
            assertEquals(Timing.default, result.first().timing)
        }
    }
}