package dev.tarkan.titlemanager.parser

import dev.tarkan.titlemanager.time.Timing
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.Test

class IntermediaryParsedAnimationLineTest {
    @Test
    fun `parse with empty input expects correct output`() {
        val expectedText = ""
        val line = IntermediaryParsedAnimationLine(Timing.instant, expectedText)
        val parsedNodes = line.parse()

        assertEquals(1, parsedNodes.size)

        assertEquals(parsedNodes.first(), IntermediaryTextNode(expectedText))
    }

    @Test
    fun `parse with only text input expects correct output`() {
        val expectedText = "hello world"
        val line = IntermediaryParsedAnimationLine(Timing.instant, expectedText)
        val parsedNodes = line.parse()

        assertEquals(1, parsedNodes.size)

        assertEquals(parsedNodes.first(), IntermediaryTextNode(expectedText))
    }

    @Test
    fun `parse with animation in middle expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "before animation \${hello:world} after animation")
        val parsedNodes = line.parse()

        assertEquals(3, parsedNodes.size)

        assertIs<IntermediaryTextNode>(parsedNodes[0])
        assertEquals("before animation ", (parsedNodes[0] as IntermediaryTextNode).text)

        assertIs<IntermediaryAnimationPlaceholderNode>(parsedNodes[1])
        assertEquals("hello", (parsedNodes[1] as IntermediaryAnimationPlaceholderNode).id)
        assertEquals("world", (parsedNodes[1] as IntermediaryAnimationPlaceholderNode).data)

        assertIs<IntermediaryTextNode>(parsedNodes[2])
        assertEquals(" after animation", (parsedNodes[2] as IntermediaryTextNode).text)
    }

    @Test
    fun `parse with animation at start expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "\${hello:world} after animation")
        val parsedNodes = line.parse()

        assertEquals(2, parsedNodes.size)

        assertIs<IntermediaryAnimationPlaceholderNode>(parsedNodes[0])
        assertEquals("hello", (parsedNodes[0] as IntermediaryAnimationPlaceholderNode).id)
        assertEquals("world", (parsedNodes[0] as IntermediaryAnimationPlaceholderNode).data)

        assertIs<IntermediaryTextNode>(parsedNodes[1])
        assertEquals(" after animation", (parsedNodes[1] as IntermediaryTextNode).text)
    }

    @Test
    fun `parse with animation at end expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "before animation \${hello:world}")
        val parsedNodes = line.parse()

        assertEquals(2, parsedNodes.size)

        assertIs<IntermediaryTextNode>(parsedNodes[0])
        assertEquals("before animation ", (parsedNodes[0] as IntermediaryTextNode).text)

        assertIs<IntermediaryAnimationPlaceholderNode>(parsedNodes[1])
        assertEquals("hello", (parsedNodes[1] as IntermediaryAnimationPlaceholderNode).id)
        assertEquals("world", (parsedNodes[1] as IntermediaryAnimationPlaceholderNode).data)
    }

    @Test
    fun `parse with animation alone expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "\${hello:world}")
        val parsedNodes = line.parse()

        assertEquals(1, parsedNodes.size)

        assertIs<IntermediaryAnimationPlaceholderNode>(parsedNodes.first())
        assertEquals("hello", (parsedNodes.first() as IntermediaryAnimationPlaceholderNode).id)
        assertEquals("world", (parsedNodes.first() as IntermediaryAnimationPlaceholderNode).data)
    }

    @Test
    fun `parse with animation alone with no data expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "\${hello}")
        val parsedNodes = line.parse()

        assertEquals(1, parsedNodes.size)

        assertIs<IntermediaryAnimationPlaceholderNode>(parsedNodes.first())
        assertEquals("hello", (parsedNodes.first() as IntermediaryAnimationPlaceholderNode).id)
        assertNull((parsedNodes.first() as IntermediaryAnimationPlaceholderNode).data)
    }

    @Test
    fun `parse with animation alone with empty data expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "\${hello:}")
        val parsedNodes = line.parse()

        assertEquals(1, parsedNodes.size)

        assertIs<IntermediaryAnimationPlaceholderNode>(parsedNodes.first())
        assertEquals("hello", (parsedNodes.first() as IntermediaryAnimationPlaceholderNode).id)
        assertEquals("", (parsedNodes.first() as IntermediaryAnimationPlaceholderNode).data)
    }

    @Test
    fun `parse with animation escape } expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "\${hello:escaped \\}}")
        val parsedNodes = line.parse()

        assertEquals(1, parsedNodes.size)

        assertIs<IntermediaryAnimationPlaceholderNode>(parsedNodes.first())
        assertEquals("hello", (parsedNodes.first() as IntermediaryAnimationPlaceholderNode).id)
        assertEquals("escaped }", (parsedNodes.first() as IntermediaryAnimationPlaceholderNode).data)
    }

    @Test
    fun `parse with variable in middle expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "before animation %{hello:world} after animation")
        val parsedNodes = line.parse()

        assertEquals(3, parsedNodes.size)

        assertIs<IntermediaryTextNode>(parsedNodes[0])
        assertEquals("before animation ", (parsedNodes[0] as IntermediaryTextNode).text)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes[1])
        assertEquals("hello", (parsedNodes[1] as IntermediaryVariablePlaceholderNode).id)
        assertEquals("world", (parsedNodes[1] as IntermediaryVariablePlaceholderNode).data)

        assertIs<IntermediaryTextNode>(parsedNodes[2])
        assertEquals(" after animation", (parsedNodes[2] as IntermediaryTextNode).text)
    }

    @Test
    fun `parse with variable at start expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "%{hello:world} after animation")
        val parsedNodes = line.parse()

        assertEquals(2, parsedNodes.size)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes[0])
        assertEquals("hello", (parsedNodes[0] as IntermediaryVariablePlaceholderNode).id)
        assertEquals("world", (parsedNodes[0] as IntermediaryVariablePlaceholderNode).data)

        assertIs<IntermediaryTextNode>(parsedNodes[1])
        assertEquals(" after animation", (parsedNodes[1] as IntermediaryTextNode).text)
    }

    @Test
    fun `parse with variable at end expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "before animation %{hello:world}")
        val parsedNodes = line.parse()

        assertEquals(2, parsedNodes.size)

        assertIs<IntermediaryTextNode>(parsedNodes[0])
        assertEquals("before animation ", (parsedNodes[0] as IntermediaryTextNode).text)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes[1])
        assertEquals("hello", (parsedNodes[1] as IntermediaryVariablePlaceholderNode).id)
        assertEquals("world", (parsedNodes[1] as IntermediaryVariablePlaceholderNode).data)
    }

    @Test
    fun `parse with variable alone expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "%{hello:world}")
        val parsedNodes = line.parse()

        assertEquals(1, parsedNodes.size)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes.first())
        assertEquals("hello", (parsedNodes.first() as IntermediaryVariablePlaceholderNode).id)
        assertEquals("world", (parsedNodes.first() as IntermediaryVariablePlaceholderNode).data)
    }

    @Test
    fun `parse with variable alone with no data expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "%{hello}")
        val parsedNodes = line.parse()

        assertEquals(1, parsedNodes.size)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes.first())
        assertEquals("hello", (parsedNodes.first() as IntermediaryVariablePlaceholderNode).id)
        assertNull((parsedNodes.first() as IntermediaryVariablePlaceholderNode).data)
    }

    @Test
    fun `parse with variable alone with empty data expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "%{hello:}")
        val parsedNodes = line.parse()

        assertEquals(1, parsedNodes.size)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes.first())
        assertEquals("hello", (parsedNodes.first() as IntermediaryVariablePlaceholderNode).id)
        assertEquals("", (parsedNodes.first() as IntermediaryVariablePlaceholderNode).data)
    }

    @Test
    fun `parse with variable escape } expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "%{hello:escaped \\}}")
        val parsedNodes = line.parse()

        assertEquals(1, parsedNodes.size)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes.first())
        assertEquals("hello", (parsedNodes.first() as IntermediaryVariablePlaceholderNode).id)
        assertEquals("escaped }", (parsedNodes.first() as IntermediaryVariablePlaceholderNode).data)
    }

    @Test
    fun `parse with variable and animation expects correct output`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "before %{myvariable:my variable data} between \${myanimation:my animation data} after")
        val parsedNodes = line.parse()

        assertEquals(5, parsedNodes.size)

        assertIs<IntermediaryTextNode>(parsedNodes[0])
        assertEquals("before ", (parsedNodes[0] as IntermediaryTextNode).text)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes[1])
        assertEquals("myvariable", (parsedNodes[1] as IntermediaryVariablePlaceholderNode).id)
        assertEquals("my variable data", (parsedNodes[1] as IntermediaryVariablePlaceholderNode).data)

        assertIs<IntermediaryTextNode>(parsedNodes[2])
        assertEquals(" between ", (parsedNodes[2] as IntermediaryTextNode).text)

        assertIs<IntermediaryAnimationPlaceholderNode>(parsedNodes[3])
        assertEquals("myanimation", (parsedNodes[3] as IntermediaryAnimationPlaceholderNode).id)
        assertEquals("my animation data", (parsedNodes[3] as IntermediaryAnimationPlaceholderNode).data)

        assertIs<IntermediaryTextNode>(parsedNodes[4])
        assertEquals(" after", (parsedNodes[4] as IntermediaryTextNode).text)
    }

    @Test
    fun `parse with two variables with no data`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "before %{var1} between %{var2} after")
        val parsedNodes = line.parse()

        assertEquals(5, parsedNodes.size)

        assertIs<IntermediaryTextNode>(parsedNodes[0])
        assertEquals("before ", (parsedNodes[0] as IntermediaryTextNode).text)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes[1])
        assertEquals("var1", (parsedNodes[1] as IntermediaryVariablePlaceholderNode).id)
        assertNull((parsedNodes[1] as IntermediaryVariablePlaceholderNode).data)

        assertIs<IntermediaryTextNode>(parsedNodes[2])
        assertEquals(" between ", (parsedNodes[2] as IntermediaryTextNode).text)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes[3])
        assertEquals("var2", (parsedNodes[3] as IntermediaryVariablePlaceholderNode).id)
        assertNull((parsedNodes[3] as IntermediaryVariablePlaceholderNode).data)

        assertIs<IntermediaryTextNode>(parsedNodes[4])
        assertEquals(" after", (parsedNodes[4] as IntermediaryTextNode).text)
    }

    @Test
    fun `parse with two variables with data`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "before %{var1:var 1 data} between %{var2:var 2 data} after")
        val parsedNodes = line.parse()

        assertEquals(5, parsedNodes.size)

        assertIs<IntermediaryTextNode>(parsedNodes[0])
        assertEquals("before ", (parsedNodes[0] as IntermediaryTextNode).text)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes[1])
        assertEquals("var1", (parsedNodes[1] as IntermediaryVariablePlaceholderNode).id)
        assertEquals("var 1 data", (parsedNodes[1] as IntermediaryVariablePlaceholderNode).data)

        assertIs<IntermediaryTextNode>(parsedNodes[2])
        assertEquals(" between ", (parsedNodes[2] as IntermediaryTextNode).text)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes[3])
        assertEquals("var2", (parsedNodes[3] as IntermediaryVariablePlaceholderNode).id)
        assertEquals("var 2 data", (parsedNodes[3] as IntermediaryVariablePlaceholderNode).data)

        assertIs<IntermediaryTextNode>(parsedNodes[4])
        assertEquals(" after", (parsedNodes[4] as IntermediaryTextNode).text)
    }

    @Test
    fun `parse with two variables with no data touching`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "before %{var1}%{var2} after")
        val parsedNodes = line.parse()

        assertEquals(4, parsedNodes.size)

        assertIs<IntermediaryTextNode>(parsedNodes[0])
        assertEquals("before ", (parsedNodes[0] as IntermediaryTextNode).text)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes[1])
        assertEquals("var1", (parsedNodes[1] as IntermediaryVariablePlaceholderNode).id)
        assertNull((parsedNodes[1] as IntermediaryVariablePlaceholderNode).data)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes[2])
        assertEquals("var2", (parsedNodes[2] as IntermediaryVariablePlaceholderNode).id)
        assertNull((parsedNodes[2] as IntermediaryVariablePlaceholderNode).data)

        assertIs<IntermediaryTextNode>(parsedNodes[3])
        assertEquals(" after", (parsedNodes[3] as IntermediaryTextNode).text)
    }

    @Test
    fun `parse with two variables with data touching`() {
        val line = IntermediaryParsedAnimationLine(Timing.instant, "before %{var1:var 1 data}%{var2:var 2 data} after")
        val parsedNodes = line.parse()

        assertEquals(4, parsedNodes.size)

        assertIs<IntermediaryTextNode>(parsedNodes[0])
        assertEquals("before ", (parsedNodes[0] as IntermediaryTextNode).text)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes[1])
        assertEquals("var1", (parsedNodes[1] as IntermediaryVariablePlaceholderNode).id)
        assertEquals("var 1 data", (parsedNodes[1] as IntermediaryVariablePlaceholderNode).data)

        assertIs<IntermediaryVariablePlaceholderNode>(parsedNodes[2])
        assertEquals("var2", (parsedNodes[2] as IntermediaryVariablePlaceholderNode).id)
        assertEquals("var 2 data", (parsedNodes[2] as IntermediaryVariablePlaceholderNode).data)

        assertIs<IntermediaryTextNode>(parsedNodes[3])
        assertEquals(" after", (parsedNodes[3] as IntermediaryTextNode).text)
    }
}