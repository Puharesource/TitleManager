package dev.tarkan.titlemanager.bukkit.command

import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsStatus
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilityStatus
import org.bukkit.Server

class CommandPlatformCapabilities private constructor(
    val brigadierLifecycleCommands: Boolean,
    val asyncTabCompletion: Boolean,
    val richSuggestionTooltips: Boolean,
    val coloredSuggestionTooltips: Boolean
) {
    val diagnostics: List<DiagnosticsStatus> = listOf(
        DiagnosticsStatus(
            RuntimeCapability.COMMANDS,
            RuntimeCapabilityStatus.AVAILABLE,
            buildList {
                add("bukkit-executor")
                add("legacy-tab-complete")
                if (brigadierLifecycleCommands) {
                    add("paper-brigadier-lifecycle")
                }
            }.joinToString()
        ),
        DiagnosticsStatus(
            RuntimeCapability.COMMAND_SUGGESTIONS,
            RuntimeCapabilityStatus.AVAILABLE,
            buildList {
                add("text")
                if (asyncTabCompletion) {
                    add("async-tab-complete")
                }
                if (richSuggestionTooltips) {
                    add("tooltips")
                }
                if (coloredSuggestionTooltips) {
                    add("colored-tooltips")
                }
            }.joinToString()
        )
    )

    companion object {
        fun detect(server: Server): CommandPlatformCapabilities {
            val serverClassLoader = server::class.java.classLoader
            val pluginClassLoader = CommandPlatformCapabilities::class.java.classLoader

            return detect { className ->
                classExists(className, serverClassLoader) || classExists(className, pluginClassLoader)
            }
        }

        internal fun detect(classExists: (String) -> Boolean): CommandPlatformCapabilities {
            val brigadierLifecycleCommands = classExists("io.papermc.paper.command.brigadier.Commands") &&
                classExists("io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents")
            val richSuggestionTooltips = brigadierLifecycleCommands &&
                classExists("com.mojang.brigadier.Message") &&
                classExists("com.mojang.brigadier.suggestion.SuggestionsBuilder")
            val coloredSuggestionTooltips = richSuggestionTooltips &&
                classExists("net.kyori.adventure.text.Component") &&
                classExists("io.papermc.paper.command.brigadier.MessageComponentSerializer")

            return CommandPlatformCapabilities(
                brigadierLifecycleCommands = brigadierLifecycleCommands,
                asyncTabCompletion = classExists("com.destroystokyo.paper.event.server.AsyncTabCompleteEvent"),
                richSuggestionTooltips = richSuggestionTooltips,
                coloredSuggestionTooltips = coloredSuggestionTooltips
            )
        }

        private fun classExists(className: String, classLoader: ClassLoader?): Boolean {
            return runCatching { Class.forName(className, false, classLoader) }.isSuccess
        }
    }
}
