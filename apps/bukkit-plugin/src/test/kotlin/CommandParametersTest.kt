import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommandParametersTest {
    @Test
    fun `parses silent and hyphenated legacy timing parameters`() {
        val parameters = CommandParameters.fromArguments(listOf("-silent", "-fade-in=2", "-stay=3", "-fade-out=4", "message"))

        assertEquals(4, parameters.size)
        assertEquals(4, parameters.consumedArgumentCount)
        assertTrue(parameters.isSilent)
        assertEquals("2", parameters.getValue("fade-in"))
    }

    @Test
    fun `tracks consumed arguments separately from unique parameter names`() {
        val parameters = CommandParameters.fromArguments(listOf("-silent", "-silent", "-world=spawn", "message"))

        assertEquals(2, parameters.size)
        assertEquals(3, parameters.consumedArgumentCount)
        assertTrue(parameters.isSilent)
        assertEquals("spawn", parameters.getValue("world"))
    }

    @Test
    fun `stops parsing at unknown dash-prefixed message arguments`() {
        val parameters = CommandParameters.fromArguments(listOf("-silent", "-not-a-parameter", "message"))

        assertEquals(1, parameters.size)
        assertEquals(1, parameters.consumedArgumentCount)
        assertTrue(parameters.isSilent)
    }

    @Test
    fun `converts old title command tick timing to milliseconds`() {
        val timing = CommandParameters.fromArguments(listOf("-fadein=2", "-stay=3", "-fadeout=4"))
            .toLegacyTitleTiming()

        assertEquals(100u, timing.fadeInMilliseconds)
        assertEquals(150u, timing.stayMilliseconds)
        assertEquals(200u, timing.fadeOutMilliseconds)
    }

    @Test
    fun `uses old command defaults and invalid value fallbacks for title timing`() {
        val timing = CommandParameters.fromArguments(listOf("-fadein=bad", "-stay=bad", "-fadeout=bad"))
            .toLegacyTitleTiming()

        assertEquals(500u, timing.fadeInMilliseconds)
        assertEquals(2000u, timing.stayMilliseconds)
        assertEquals(500u, timing.fadeOutMilliseconds)

        val defaultTiming = CommandParameters.fromArguments(emptyList()).toLegacyTitleTiming()
        assertEquals(1000u, defaultTiming.fadeInMilliseconds)
        assertEquals(1000u, defaultTiming.stayMilliseconds)
        assertEquals(1000u, defaultTiming.fadeOutMilliseconds)
    }

    @Test
    fun `uses invalid value fallbacks when legacy title timing ticks would overflow milliseconds`() {
        val timing = CommandParameters.fromArguments(
            listOf("-fadein=${UInt.MAX_VALUE}", "-stay=${UInt.MAX_VALUE}", "-fadeout=${UInt.MAX_VALUE}")
        ).toLegacyTitleTiming()

        assertEquals(500u, timing.fadeInMilliseconds)
        assertEquals(2000u, timing.stayMilliseconds)
        assertEquals(500u, timing.fadeOutMilliseconds)
    }

    @Test
    fun `suggests legacy command parameters by prefix`() {
        assertContentEquals(
            listOf("-fadein", "-fadeout"),
            CommandParameters.completeParameter("-f")
        )
        assertContentEquals(
            listOf("-silent", "-world", "-fadein", "-stay", "-fadeout", "-radius"),
            CommandParameters.completeParameter("-")
        )
        assertTrue(CommandParameters.completeParameter("f").isEmpty())
    }

    @Test
    fun `suggests legacy command parameters with metadata`() {
        val suggestions = CommandParameters.completeParameterSuggestions("-f")

        assertContentEquals(listOf("-fadein", "-fadeout"), suggestions.map { it.text })
        assertTrue(suggestions.all { it.tooltip != null })
    }
}
