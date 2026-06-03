package dev.tarkan.titlemanager.parser

import dev.tarkan.titlemanager.test.utils.TestFixtures
import dev.tarkan.titlemanager.time.Timing
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.Test

class IntermediaryParserTest {
    private lateinit var intermediaryParser: IntermediaryParser

    @BeforeTest
    fun setUp() {
        intermediaryParser = IntermediaryParser()
    }


    @Test
    fun `parseLine with no timings expects previous timing and provided text`() {
        val input = "Test line with no provided timings"
        val parsedLine = intermediaryParser.parseLine(input, previousTiming = Timing.instant)

        assertEquals(Timing.instant, parsedLine.timing)
        assertEquals(input, parsedLine.text)
    }

    @Test
    fun `parseLine with simple timings expects timing with only stay set`() {
        for (expectedStay in listOf(1, 20, 300, 4_000, 50_000, 700_000, 8_000_000)) {
            val inputText = "Test line"
            val input = "[$expectedStay]$inputText"
            val expectedTiming = Timing.createStatic(expectedStay.toUInt())
            val parsedLine = intermediaryParser.parseLine(input, previousTiming = Timing.instant)

            assertEquals(expectedTiming, parsedLine.timing)
            assertEquals(inputText, parsedLine.text)
        }
    }

    @Test
    fun `parseLine with full timings expects provided timing`() {
        for ((expectedFadeIn, expectedStay, expectedFadeOut) in listOf(
            Triple(1u, 2u, 3u),
            Triple(10u, 20u, 30u),
            Triple(100u, 2000u, 30000u),
            Triple(100000u, 2000000u, 30000000u),
        )) {
            val inputText = "Test line"
            val input = "[$expectedFadeIn;$expectedStay;$expectedFadeOut]$inputText"
            val expectedTiming = Timing(expectedFadeIn, expectedStay, expectedFadeOut)
            val parsedLine = intermediaryParser.parseLine(input, previousTiming = Timing.instant)

            assertEquals(expectedTiming, parsedLine.timing)
            assertEquals(inputText, parsedLine.text)
        }
    }

    @Test
    fun `parseFile test file animation_file_all expects correct output`() {
        val parsedFileLines = intermediaryParser.parseText(TestFixtures.animationFile("animation_file_all.txt"))
        val expectedTimings = listOf(
            Timing.instant,
            Timing.instant,
            Timing.createStatic(1u),
            Timing.createStatic(1u),
            Timing.createStatic(1u),
            Timing(1u, 2u, 3u),
            Timing(4u, 5u, 6u),
            Timing(4u, 5u, 6u)
        )

        assertEquals(expectedTimings.size, parsedFileLines.size)

        for ((index, actualLine) in parsedFileLines.withIndex()) {
            assertEquals(expectedTimings[index], actualLine.timing, "timing failed on line ${index + 1}")
            assertEquals("line ${index + 1}", actualLine.text, "text failed on line ${index + 1}")
        }
    }

    @Test
    fun `parseText with timing scale preserves scaled timing for inherited lines`() {
        val parsedFileLines = IntermediaryParser(timingScale = 50u).parseText(
            """
            [1]first
            second
            [1;2;3]third
            fourth
            """.trimIndent()
        )

        val expectedTimings = listOf(
            Timing.createStatic(50u),
            Timing.createStatic(50u),
            Timing(50u, 100u, 150u),
            Timing(50u, 100u, 150u)
        )

        assertEquals(expectedTimings.size, parsedFileLines.size)

        for ((index, actualLine) in parsedFileLines.withIndex()) {
            assertEquals(expectedTimings[index], actualLine.timing, "timing failed on line ${index + 1}")
        }
    }
}