package dev.tarkan.titlemanager.parser

import dev.tarkan.titlemanager.api.TitleManagerCoreApi
import dev.tarkan.titlemanager.animation.ShineAnimation
import dev.tarkan.titlemanager.test.utils.TestFixtures
import dev.tarkan.titlemanager.time.Timing
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OldTitleManagerCompatibilityTest {
    private val parser = IntermediaryParser()

    @Test
    fun `parse old bundled left-to-right animation`() {
        val parsedLines = parser.parseText(TestFixtures.animationFile("old-titlemanager-left-to-right.txt"))

        assertEquals(18, parsedLines.size)
        assertEquals(Timing(0u, 5u, 0u), parsedLines.first().timing)
        assertEquals("&7&b-&7---------", parsedLines.first().text)
        assertEquals(Timing(0u, 2u, 0u), parsedLines[1].timing)
        assertEquals("&7-&b-&7--------", parsedLines[1].text)
        assertEquals("&7-&b-&7--------", parsedLines.last().text)
    }

    @Test
    fun `parse old bundled right-to-left animation`() {
        val parsedLines = parser.parseText(TestFixtures.animationFile("old-titlemanager-right-to-left.txt"))

        assertEquals(18, parsedLines.size)
        assertEquals(Timing(0u, 5u, 0u), parsedLines.first().timing)
        assertEquals("&7---------&b-&7", parsedLines.first().text)
        assertEquals(Timing(0u, 2u, 0u), parsedLines[1].timing)
        assertEquals("&7--------&b-&7-", parsedLines[1].text)
        assertEquals("&7--------&b-&7-", parsedLines.last().text)
    }

    @Test
    fun `parse old default config placeholder expression`() {
        val serializedShineData = "[0;2;0][0;25;0][0;25;0][&3;&b]My Server"
        val line = IntermediaryParsedAnimationLine(
            Timing.instant,
            "${'$'}{shine:$serializedShineData}"
        )

        val nodes = line.parse()

        val animationNode = assertIs<IntermediaryAnimationPlaceholderNode>(nodes.single())
        assertEquals("shine", animationNode.id)
        assertEquals(serializedShineData, animationNode.data)

        val shineData = ShineAnimation.DataSerializer.deserialize(animationNode.data)
        assertEquals(Timing(0u, 100u, 0u), shineData.intermediaryTiming)
        assertEquals(Timing(0u, 1250u, 0u), shineData.startTiming)
        assertEquals(Timing(0u, 1250u, 0u), shineData.endTiming)
        assertEquals("&3", shineData.primaryColor)
        assertEquals("&b", shineData.secondaryColor)
        assertEquals("My Server", shineData.text)
    }

    @Test
    fun `parse old default player list line with animations and variables`() {
        val line = IntermediaryParsedAnimationLine(
            Timing.instant,
            "${'$'}{right-to-left} &b%{online}&7/&b%{max} &7Online Players ${'$'}{left-to-right}"
        )

        val nodes = line.parse()

        assertEquals("right-to-left", assertIs<IntermediaryAnimationPlaceholderNode>(nodes[0]).id)
        assertEquals(" &b", assertIs<IntermediaryTextNode>(nodes[1]).text)
        assertEquals("online", assertIs<IntermediaryVariablePlaceholderNode>(nodes[2]).id)
        assertEquals("&7/&b", assertIs<IntermediaryTextNode>(nodes[3]).text)
        assertEquals("max", assertIs<IntermediaryVariablePlaceholderNode>(nodes[4]).id)
        assertEquals(" &7Online Players ", assertIs<IntermediaryTextNode>(nodes[5]).text)
        assertEquals("left-to-right", assertIs<IntermediaryAnimationPlaceholderNode>(nodes[6]).id)
    }

    @Test
    fun `parse old default config rendered text entries`() {
        val entries = TestFixtures.oldTitleManagerDefaultConfigTextEntries()

        for (entry in entries) {
            val parsedLine = parser.parseLine(entry.text)
            val reconstructedText = parsedLine.parse().joinToString(separator = "") { node ->
                when (node) {
                    is IntermediaryTextNode -> node.text
                    is IntermediaryVariablePlaceholderNode -> "%{${node.id}${node.data?.let { ":$it" } ?: ""}}"
                    is IntermediaryAnimationPlaceholderNode -> "${'$'}{${node.id}${node.data?.let { ":$it" } ?: ""}}"
                    else -> error("Unknown node in ${entry.path}")
                }
            }

            assertEquals(entry.text, reconstructedText, "Failed to round-trip ${entry.path}")
        }
    }

    @Test
    fun `old default config placeholder inventory remains recognized`() {
        val nodes = TestFixtures.oldTitleManagerDefaultConfigTextEntries()
            .flatMap { parser.parseLine(it.text).parse() }

        val animationIds = nodes
            .filterIsInstance<IntermediaryAnimationPlaceholderNode>()
            .map { it.id }
            .toSet()
        val variableIds = nodes
            .filterIsInstance<IntermediaryVariablePlaceholderNode>()
            .map { it.id }
            .toSet()

        assertEquals(setOf("shine", "right-to-left", "left-to-right"), animationIds)
        assertEquals(setOf("12h-world-time", "server-time", "online", "max", "name", "ping"), variableIds)
    }

    @Test
    fun `old default config shine entries produce scheduler-free preview timelines`() {
        val shineEntries = TestFixtures.oldTitleManagerDefaultConfigTextEntries()
            .filter { it.text.startsWith("${'$'}{shine:") }

        assertEquals(2, shineEntries.size)

        for (entry in shineEntries) {
            val timeline = TitleManagerCoreApi.createAnimationTimeline(entry.text)

            assertTrue(timeline.isSuccess, "Failed to create timeline for ${entry.path}")
            assertEquals(13, timeline.frames.size, "Unexpected frame count for ${entry.path}")
            assertTrue(timeline.totalMilliseconds!! > 0.0, "Timeline must have duration for ${entry.path}")
            assertTrue(timeline.frames.first().text.contains("My Server"), "First frame should render text for ${entry.path}")
        }
    }
}
