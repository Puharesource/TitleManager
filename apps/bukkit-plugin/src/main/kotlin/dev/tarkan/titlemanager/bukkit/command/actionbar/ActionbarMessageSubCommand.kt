package dev.tarkan.titlemanager.bukkit.command.actionbar

import dev.tarkan.titlemanager.bukkit.extensions.sendTitleManagerMessage
import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.extensions.toErrorComponent
import dev.tarkan.titlemanager.bukkit.localization.ActionbarMessageCommandMessages
import org.bukkit.command.CommandSender

class ActionbarMessageSubCommand(
    private val plugin: TitleManagerPlugin,
    private val playerContextManager: PlayerContextManager
) : TitleManagerSubCommand("msg", "message", description = ActionbarMessageCommandMessages.description, permission = "titlemanager.command.actionbar.message") {

    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        if (args.size <= 1) {
            sendInvalidUsage(sender, ActionbarMessageCommandMessages.usage, context)
            return
        }

        val playerName = args.first()
        val player = plugin.server.getPlayer(playerName)

        if (player == null) {
            sender.sendTitleManagerMessage(ActionbarMessageCommandMessages.invalidPlayer.toErrorComponent(playerName, context.locale))
            return
        }

        val message = args.joinedMessage(startIndex = 1)
        val playerContext = playerContextManager.getContext(player)

        playerContext.sendActionbar(message)
        sender.sendIfNotSilent(parameters, ActionbarMessageCommandMessages.sent.toComponent(message, player.name, context.locale))
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext): List<String> {
        return plugin.server.completePlayerNames(args)
    }
}
