package dev.tarkan.titlemanager.bukkit.command.subcommands

import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.diagnostics.DiagnosticsRenderer
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeDiagnosticsService
import dev.tarkan.titlemanager.bukkit.localization.DiagnosticsCommandMessages
import org.bukkit.command.CommandSender

class DiagnosticsSubCommand(
    private val runtimeDiagnosticsService: RuntimeDiagnosticsService
) : TitleManagerSubCommand("diagnostics", "diag", description = DiagnosticsCommandMessages.description, permission = "titlemanager.command.diagnostics") {
    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        DiagnosticsRenderer.render(runtimeDiagnosticsService.snapshot()).forEach(sender::sendMessage)
    }
}
