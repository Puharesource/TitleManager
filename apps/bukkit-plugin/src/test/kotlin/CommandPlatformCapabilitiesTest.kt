import dev.tarkan.titlemanager.bukkit.command.CommandPlatformCapabilities
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommandPlatformCapabilitiesTest {
    @Test
    fun `legacy Bukkit command path is always available`() {
        val capabilities = CommandPlatformCapabilities.detect { false }

        assertFalse(capabilities.brigadierLifecycleCommands)
        assertFalse(capabilities.asyncTabCompletion)
        assertFalse(capabilities.richSuggestionTooltips)
        assertFalse(capabilities.coloredSuggestionTooltips)
        assertEquals("bukkit-executor, legacy-tab-complete", capabilities.diagnostics.single { it.name == RuntimeCapability.COMMANDS }.detail)
        assertEquals("text", capabilities.diagnostics.single { it.name == RuntimeCapability.COMMAND_SUGGESTIONS }.detail)
    }

    @Test
    fun `modern Paper command path reports Brigadier and colored tooltip support`() {
        val modernClasses = setOf(
            "io.papermc.paper.command.brigadier.Commands",
            "io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents",
            "com.destroystokyo.paper.event.server.AsyncTabCompleteEvent",
            "com.mojang.brigadier.Message",
            "com.mojang.brigadier.suggestion.SuggestionsBuilder",
            "net.kyori.adventure.text.Component",
            "io.papermc.paper.command.brigadier.MessageComponentSerializer"
        )

        val capabilities = CommandPlatformCapabilities.detect(modernClasses::contains)

        assertTrue(capabilities.brigadierLifecycleCommands)
        assertTrue(capabilities.asyncTabCompletion)
        assertTrue(capabilities.richSuggestionTooltips)
        assertTrue(capabilities.coloredSuggestionTooltips)
        assertEquals(
            "bukkit-executor, legacy-tab-complete, paper-brigadier-lifecycle",
            capabilities.diagnostics.single { it.name == RuntimeCapability.COMMANDS }.detail
        )
        assertEquals(
            "text, async-tab-complete, tooltips, colored-tooltips",
            capabilities.diagnostics.single { it.name == RuntimeCapability.COMMAND_SUGGESTIONS }.detail
        )
    }

    @Test
    fun `tooltip support is not reported unless Brigadier command registration is present`() {
        val classes = setOf(
            "com.mojang.brigadier.Message",
            "com.mojang.brigadier.suggestion.SuggestionsBuilder",
            "net.kyori.adventure.text.Component",
            "io.papermc.paper.command.brigadier.MessageComponentSerializer"
        )

        val capabilities = CommandPlatformCapabilities.detect(classes::contains)

        assertFalse(capabilities.brigadierLifecycleCommands)
        assertFalse(capabilities.richSuggestionTooltips)
        assertFalse(capabilities.coloredSuggestionTooltips)
    }
}
