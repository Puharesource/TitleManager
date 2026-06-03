package dev.tarkan.titlemanager.bukkit.command.title

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.extensions.splitTypedLineBreak
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.extensions.toErrorComponent
import dev.tarkan.titlemanager.bukkit.localization.TitleMessageCommandMessages
import org.bukkit.command.CommandSender

class TitleMessageSubCommand(
    private val plugin: TitleManagerPlugin,
    private val playerContextManager: PlayerContextManager
) : TitleManagerSubCommand("msg", "message", description = TitleMessageCommandMessages.description, permission = "titlemanager.command.title.message") {

    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        if (args.size <= 1) {
            sendInvalidUsage(sender, TitleMessageCommandMessages.usage, context)
            return
        }

        val playerName = args.first()
        val player = plugin.server.getPlayer(playerName)

        if (player == null) {
            sender.sendMessage(TitleMessageCommandMessages.invalidPlayer.toErrorComponent(playerName, context.locale))
            return
        }

        val messages = args.joinedMessage(startIndex = 1).splitTypedLineBreak(2)
        val title = messages.first()
        val subtitle = messages.getOrNull(1)
        val playerContext = playerContextManager.getContext(player)
        val timing = parameters.toLegacyTitleTiming()

        if (subtitle == null) {
            playerContext.sendTitle(title, timing)
            sender.sendIfNotSilent(parameters, TitleMessageCommandMessages.titleSent.toComponent(title, player.name, context.locale))
            return
        }

        if (title.isEmpty()) {
            playerContext.sendSubtitle(subtitle, timing)
            sender.sendIfNotSilent(parameters, TitleMessageCommandMessages.subtitleSent.toComponent(subtitle, player.name, context.locale))
            return
        }

        playerContext.sendTitleAndSubtitle(title, subtitle, timing)
        sender.sendIfNotSilent(parameters, TitleMessageCommandMessages.bothSent.toComponent(title, subtitle, player.name, context.locale))
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext): List<String> {
        return plugin.server.completePlayerNames(args)
    }
}