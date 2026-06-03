package dev.tarkan.titlemanager.bukkit.command

import dev.tarkan.titlemanager.bukkit.configuration.ConfigurationException
import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsRenderer
import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsSnapshot
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class SafeModeTitleManagerCommand(
    private val pluginVersion: String,
    private val failure: ConfigurationException,
    private val reload: () -> SafeModeReloadResult,
    private val diagnosticsSnapshot: () -> DiagnosticsSnapshot
) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        when (args.firstOrNull()?.lowercase()) {
            "version", "v" -> sender.sendMessage(SafeModeCommandMessages.version(pluginVersion))
            "diagnostics", "diag" -> DiagnosticsRenderer.render(diagnosticsSnapshot()).forEach(sender::sendMessage)
            "reload" -> {
                sender.sendMessage(SafeModeCommandMessages.RELOAD_STARTED)
                val result = reload()

                if (result.recovered) {
                    sender.sendMessage(SafeModeCommandMessages.RELOAD_RECOVERED)
                } else {
                    sender.sendMessage(SafeModeCommandMessages.RELOAD_FAILED)
                    sender.sendMessage(SafeModeCommandMessages.failureMessage(result, failure))
                }
            }
            else -> {
                sender.sendMessage(SafeModeCommandMessages.CONFIGURATION_FAILED)
                sender.sendMessage(SafeModeCommandMessages.failureMessage(failure))
                sender.sendMessage(SafeModeCommandMessages.HELP)
            }
        }

        return true
    }
}

data class SafeModeReloadResult(
    val recovered: Boolean,
    val failureMessage: String? = null
)
