package dev.tarkan.titlemanager.bukkit.command.subcommands

import dev.tarkan.titlemanager.bukkit.extensions.sendTitleManagerMessage
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.localization.ListScriptsCommandMessages
import org.bukkit.command.CommandSender

class ListScriptsSubCommand : TitleManagerSubCommand("scripts", description = ListScriptsCommandMessages.description, permission = "titlemanager.command.scripts") {
    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        if (!parameters.isSilent) {
            sender.sendTitleManagerMessage(ListScriptsCommandMessages.unsupported.toComponent(context.locale))
        }
    }
}
