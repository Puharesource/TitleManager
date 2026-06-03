package dev.tarkan.titlemanager.bukkit.command.actionbar

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.command.BroadcastSubCommand
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.localization.ActionbarBroadcastCommandMessages
import org.bukkit.command.CommandSender

class ActionbarBroadcastSubCommand(
    plugin: TitleManagerPlugin,
    private val playerContextManager: PlayerContextManager
) : BroadcastSubCommand(plugin, "bc", "broadcast", description = ActionbarBroadcastCommandMessages.description, permission = "titlemanager.command.actionbar.broadcast") {

    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        if (args.isEmpty()) {
            sendInvalidUsage(sender, ActionbarBroadcastCommandMessages.usage, context)
            return
        }

        val message = args.joinedMessage()

        for (player in getRecipients(sender, parameters)) {
            val playerContext = playerContextManager.getContext(player)

            playerContext.sendActionbar(message)
        }

        sender.sendIfNotSilent(parameters, ActionbarBroadcastCommandMessages.sent.toComponent(message, locale = context.locale))
    }
}