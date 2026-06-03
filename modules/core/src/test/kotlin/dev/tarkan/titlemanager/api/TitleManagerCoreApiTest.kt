package dev.tarkan.titlemanager.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TitleManagerCoreApiTest {
    @Test
    fun `parseAnimationText exposes JS-friendly line and node data`() {
        val result = TitleManagerCoreApi.parseAnimationText("[0;5;0]Hello ${'$'}{shine:World} %{player}")

        assertTrue(result.isSuccess)
        assertEquals(1, result.lines.size)
        assertEquals(1, result.lines[0].lineNumber)
        assertEquals(0.0, result.lines[0].fadeInMilliseconds)
        assertEquals(5.0, result.lines[0].stayMilliseconds)
        assertEquals(0.0, result.lines[0].fadeOutMilliseconds)
        assertEquals(5.0, result.lines[0].totalMilliseconds)
        assertEquals("Hello ${'$'}{shine:World} %{player}", result.lines[0].text)

        assertEquals("text", result.lines[0].nodes[0].type)
        assertEquals("Hello ", result.lines[0].nodes[0].text)
        assertEquals("", result.lines[0].nodes[0].id)
        assertNull(result.lines[0].nodes[0].data)

        assertEquals("animation", result.lines[0].nodes[1].type)
        assertEquals("", result.lines[0].nodes[1].text)
        assertEquals("shine", result.lines[0].nodes[1].id)
        assertEquals("World", result.lines[0].nodes[1].data)

        assertEquals("text", result.lines[0].nodes[2].type)
        assertEquals(" ", result.lines[0].nodes[2].text)

        assertEquals("variable", result.lines[0].nodes[3].type)
        assertEquals("player", result.lines[0].nodes[3].id)
    }

    @Test
    fun `parseAnimationText reports invalid timing without throwing`() {
        val result = TitleManagerCoreApi.parseAnimationText("[999999999999999999999]Too large")

        assertFalse(result.isSuccess)
        assertEquals(0, result.lines.size)
        assertEquals(1, result.errors.size)
        assertEquals(1, result.errors[0].lineNumber)
        assertEquals("[999999999999999999999]Too large", result.errors[0].line)
    }

    @Test
    fun `parseAnimationTextWithTimingScale validates timing scale`() {
        val result = TitleManagerCoreApi.parseAnimationTextWithTimingScale("[5]Hello", 0)

        assertFalse(result.isSuccess)
        assertEquals(0, result.lines.size)
        assertEquals(1, result.errors.size)
        assertEquals("timingScale must be at least 1", result.errors[0].message)
    }

    @Test
    fun `parseAnimationTextWithTimingScale keeps scaled timings for inherited lines`() {
        val result = TitleManagerCoreApi.parseAnimationTextWithTimingScale("[1]first\nsecond", 50)

        assertTrue(result.isSuccess)
        assertEquals(2, result.lines.size)
        assertEquals(50.0, result.lines[0].stayMilliseconds)
        assertEquals(50.0, result.lines[1].stayMilliseconds)
        assertEquals("second", result.lines[1].text)
    }

    @Test
    fun `parseAnimationTextWithTimingScale reports timing overflow without throwing`() {
        val result = TitleManagerCoreApi.parseAnimationTextWithTimingScale("[100000000]Too large", 50)

        assertFalse(result.isSuccess)
        assertEquals(0, result.lines.size)
        assertEquals(1, result.errors.size)
        assertEquals("Timing multiplication exceeds unsigned 32-bit range", result.errors.single().message)
    }

    @Test
    fun `createAnimationTimeline exposes scheduler-free frames`() {
        val result = TitleManagerCoreApi.createAnimationTimeline("[100]Hello\n[200]Bye")

        assertTrue(result.isSuccess)
        assertFalse(result.isInfinite)
        assertEquals(300.0, result.totalMilliseconds)
        assertEquals(2, result.frames.size)
        assertEquals(1, result.frames[0].frameNumber)
        assertEquals(0.0, result.frames[0].startMilliseconds)
        assertEquals(100.0, result.frames[0].endMilliseconds)
        assertEquals(100.0, result.frames[0].stayMilliseconds)
        assertEquals("Hello", result.frames[0].text)
        assertEquals(100.0, result.frames[1].startMilliseconds)
        assertEquals(300.0, result.frames[1].endMilliseconds)
        assertEquals("Bye", result.frames[1].text)
    }

    @Test
    fun `createAnimationTimeline expands core animation placeholders`() {
        val result = TitleManagerCoreApi.createAnimationTimeline("${'$'}{text_write:Hi}")

        assertTrue(result.isSuccess)
        assertEquals(listOf("", "H", "Hi"), result.frames.map { it.text })
        assertEquals(listOf(0.0, 1000.0, 2000.0), result.frames.map { it.startMilliseconds })
        assertEquals(3000.0, result.totalMilliseconds)
    }

    @Test
    fun `createAnimationTimeline expands marquee animation placeholders`() {
        val result = TitleManagerCoreApi.createAnimationTimeline("${'$'}{marquee:[0;1;0][2]ABC}")

        assertTrue(result.isSuccess)
        assertEquals(listOf("AB", "BC", "CA"), result.frames.map { it.text })
        assertEquals(listOf(0.0, 50.0, 100.0), result.frames.map { it.startMilliseconds })
        assertEquals(150.0, result.totalMilliseconds)
    }

    @Test
    fun `createAnimationTimeline expands shine animation placeholders`() {
        val result = TitleManagerCoreApi.createAnimationTimeline("${'$'}{shine:[0;1;0][&3;&b]Hi}")

        assertTrue(result.isSuccess)
        assertEquals("&3Hi", result.frames.first().text)
        assertEquals("&3Hi", result.frames.last().text)
        assertEquals(6, result.frames.size)
        assertEquals(300.0, result.totalMilliseconds)
    }

    @Test
    fun `createAnimationTimelineWithTimingScale applies inherited timing scale`() {
        val result = TitleManagerCoreApi.createAnimationTimelineWithTimingScale("[1]first\nsecond", 50)

        assertTrue(result.isSuccess)
        assertEquals(2, result.frames.size)
        assertEquals(listOf(0.0, 50.0), result.frames.map { it.startMilliseconds })
        assertEquals(50.0, result.frames[0].stayMilliseconds)
        assertEquals(50.0, result.frames[1].stayMilliseconds)
        assertEquals(100.0, result.totalMilliseconds)
    }

    @Test
    fun `createAnimationTimeline preserves unresolved variable placeholders for preview`() {
        val result = TitleManagerCoreApi.createAnimationTimeline("[100]Hello %{player}")

        assertTrue(result.isSuccess)
        assertEquals(1, result.frames.size)
        assertEquals("Hello %{player}", result.frames[0].text)
        assertEquals(100.0, result.totalMilliseconds)
    }

    @Test
    fun `createAnimationTimeline reports parse errors without throwing`() {
        val result = TitleManagerCoreApi.createAnimationTimeline("[999999999999999999999]Too large")

        assertFalse(result.isSuccess)
        assertEquals(0, result.frames.size)
        assertEquals(1, result.errors.size)
        assertEquals(0.0, result.totalMilliseconds)
    }

    @Test
    fun `createAnimationTimeline reports nested animation timing errors without throwing`() {
        val result = TitleManagerCoreApi.createAnimationTimeline("${'$'}{marquee:[999999999999999999999;1;0][2]ABC}")

        assertFalse(result.isSuccess)
        assertEquals(0, result.frames.size)
        assertEquals(1, result.errors.size)
        assertEquals("Timing values must be unsigned 32-bit integers", result.errors.single().message)
    }

    @Test
    fun `createAnimationTimelineWithSafetyLimits reports input limits before parsing`() {
        val result = TitleManagerCoreApi.createAnimationTimelineWithSafetyLimits(
            text = "[100]Hello\n[100]Bye",
            timingScale = 1,
            maxInputCharacters = 100,
            maxLines = 1,
            maxFrames = 10,
            maxOutputCharacters = 100
        )

        assertFalse(result.isSuccess)
        assertEquals(0, result.frames.size)
        assertEquals("Input has 2 lines, exceeding maxLines 1", result.errors.single().message)
    }

    @Test
    fun `createAnimationTimelineWithSafetyLimits reports generated frame limits`() {
        val result = TitleManagerCoreApi.createAnimationTimelineWithSafetyLimits(
            text = "${'$'}{text_write:Hi}",
            timingScale = 1,
            maxInputCharacters = 100,
            maxLines = 10,
            maxFrames = 2,
            maxOutputCharacters = 100
        )

        assertFalse(result.isSuccess)
        assertEquals(0, result.frames.size)
        assertEquals("Generated timeline has 3 frames, exceeding maxFrames 2", result.errors.single().message)
    }

    @Test
    fun `createAnimationTimelineWithSafetyLimits reports generated output character limits`() {
        val result = TitleManagerCoreApi.createAnimationTimelineWithSafetyLimits(
            text = "${'$'}{text_write:Hello}",
            timingScale = 1,
            maxInputCharacters = 100,
            maxLines = 10,
            maxFrames = 10,
            maxOutputCharacters = 5
        )

        assertFalse(result.isSuccess)
        assertEquals(0, result.frames.size)
        assertEquals("Generated timeline has 15 output characters, exceeding maxOutputCharacters 5", result.errors.single().message)
    }

    @Test
    fun `createAnimationTimelineWithSafetyLimits validates limit values`() {
        val result = TitleManagerCoreApi.createAnimationTimelineWithSafetyLimits(
            text = "[100]Hello",
            timingScale = 1,
            maxInputCharacters = 0,
            maxLines = 10,
            maxFrames = 10,
            maxOutputCharacters = 100
        )

        assertFalse(result.isSuccess)
        assertEquals("maxInputCharacters must be at least 1", result.errors.single().message)
    }

    @Test
    fun `renderLegacyText exposes colored segments for web previews`() {
        val result = TitleManagerCoreApi.renderLegacyText("&aGreen &lbold &rplain")

        assertEquals(3, result.size)
        assertEquals("Green ", result[0].text)
        assertEquals("#55ff55", result[0].color)
        assertFalse(result[0].bold)

        assertEquals("bold ", result[1].text)
        assertEquals("#55ff55", result[1].color)
        assertTrue(result[1].bold)

        assertEquals("plain", result[2].text)
        assertNull(result[2].color)
        assertFalse(result[2].bold)
    }

    @Test
    fun `renderLegacyText supports modern hex color sequences`() {
        val result = TitleManagerCoreApi.renderLegacyText("§x§1§2§3§a§b§cHex")

        assertEquals(1, result.size)
        assertEquals("Hex", result[0].text)
        assertEquals("#123abc", result[0].color)
    }

    @Test
    fun `renderLegacyText resets formats when a new color starts`() {
        val result = TitleManagerCoreApi.renderLegacyText("&lBold &aGreen")

        assertEquals(2, result.size)
        assertEquals("Bold ", result[0].text)
        assertTrue(result[0].bold)
        assertNull(result[0].color)
        assertEquals("Green", result[1].text)
        assertEquals("#55ff55", result[1].color)
        assertFalse(result[1].bold)
    }

    @Test
    fun `renderLegacyText leaves invalid color codes literal`() {
        val result = TitleManagerCoreApi.renderLegacyText("&zNot a color")

        assertEquals(1, result.size)
        assertEquals("&zNot a color", result[0].text)
        assertNull(result[0].color)
    }

    @Test
    fun `translateLegacyColorCodes converts ampersand color and format codes`() {
        val result = TitleManagerCoreApi.translateLegacyColorCodes("&aGreen &lbold &zliteral")

        assertEquals("§aGreen §lbold &zliteral", result)
    }

    @Test
    fun `translateLegacyColorCodes converts ampersand hex colors`() {
        val result = TitleManagerCoreApi.translateLegacyColorCodes("&x&1&2&3&a&b&cHex")

        assertEquals("§x§1§2§3§a§b§cHex", result)
    }

    @Test
    fun `legacyColorCode encodes hex colors without Bukkit dependencies`() {
        val result = TitleManagerCoreApi.legacyColorCode("#ff00aa")

        assertEquals("§x§f§f§0§0§a§a", result)
    }

    @Test
    fun `legacy RGB helpers encode colors without Bukkit dependencies`() {
        assertEquals(0xff00aa, TitleManagerCoreApi.parseLegacyHexColorRgb("#ff00aa"))
        assertEquals("§x§f§f§0§0§a§a", TitleManagerCoreApi.legacyRgbColorCode(255, 0, 170))
        assertEquals("§x§f§f§0§0§a§a", TitleManagerCoreApi.legacyRgbIntColorCode(0xff00aa))
    }

    @Test
    fun `legacyColorCode rejects invalid hex colors`() {
        assertFailsWith<IllegalArgumentException> {
            TitleManagerCoreApi.legacyColorCode("not-a-color")
        }
    }

    @Test
    fun `formatLegacyGradient encodes old RGB gradient placeholders`() {
        val result = TitleManagerCoreApi.formatLegacyGradient("[#ff0000,#00ff00,bold]Go")

        assertEquals("§x§f§f§0§0§0§0§lG§x§7§f§7§f§0§0§lo", result)
    }

    @Test
    fun `formatLegacyGradient defaults missing gradient data to red green`() {
        val result = TitleManagerCoreApi.formatLegacyGradient("Go")

        assertEquals("§x§f§f§0§0§0§0G§x§7§f§7§f§0§0o", result)
    }

    @Test
    fun `splitTypedLineBreak drops trailing empty segments without an explicit limit`() {
        val result = TitleManagerCoreApi.splitTypedLineBreak("Title<nl>", 0)

        assertEquals(listOf("Title"), result.toList())
    }

    @Test
    fun `splitTypedLineBreak supports all legacy title separators`() {
        val result = TitleManagerCoreApi.splitTypedLineBreak("Title<nl>Subtitle{nl}Ignored%nl%Tail", 2)

        assertEquals(listOf("Title", "Subtitle{nl}Ignored%nl%Tail"), result.toList())
    }

    @Test
    fun `splitTypedLineBreak without limit returns every segment`() {
        val result = TitleManagerCoreApi.splitTypedLineBreak("Title<nl>Subtitle{nl}Footer", 0)

        assertEquals(listOf("Title", "Subtitle", "Footer"), result.toList())
    }

    @Test
    fun `splitTypedLineBreak supports old literal newline separators`() {
        assertEquals(listOf("Title", "Subtitle"), TitleManagerCoreApi.splitTypedLineBreak("Title\\nSubtitle", 2).toList())
        assertEquals(listOf("Title", "Subtitle"), TitleManagerCoreApi.splitTypedLineBreak("Title\nSubtitle", 2).toList())
    }
}
