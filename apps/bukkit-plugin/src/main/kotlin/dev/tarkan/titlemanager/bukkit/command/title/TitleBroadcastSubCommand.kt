package dev.tarkan.titlemanager.bukkit.command.title

import dev.tarkan.titlemanager.bukkit.extensions.sendTitleManagerMessage
import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin

import dev.tarkan.titlemanager.bukkit.command.BroadcastSubCommand
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.extensions.splitTypedLineBreak
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.localization.TitleBroadcastCommandMessages
import org.bukkit.command.CommandSender

class TitleBroadcastSubCommand(
    plugin: TitleManagerPlugin,
    private val playerContextManager: PlayerContextManager
) : BroadcastSubCommand(plugin, "broadcast", "bc", description = TitleBroadcastCommandMessages.description, permission = "titlemanager.command.title.broadcast") {

    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        if (args.isEmpty()) {
            sendInvalidUsage(sender, TitleBroadcastCommandMessages.usage, context)
            return
        }

        val messages = args.joinedMessage().splitTypedLineBreak(2)
        val title = messages.first()
        val subtitle = messages.getOrNull(1)
        val timing = parameters.toLegacyTitleTiming()

        for (player in getRecipients(sender, parameters)) {
            val playerContext = playerContextManager.getContext(player)

            when {
                subtitle == null -> playerContext.sendTitle(title, timing)
                title.isEmpty() -> playerContext.sendSubtitle(subtitle, timing)
                else -> playerContext.sendTitleAndSubtitle(title, subtitle, timing)
            }
        }

        if (parameters.isSilent) {
            return
        }

        if (subtitle == null) {
            sender.sendTitleManagerMessage(TitleBroadcastCommandMessages.titleSent.toComponent(title, context.locale))
            return
        }

        if (title.isEmpty()) {
            sender.sendTitleManagerMessage(TitleBroadcastCommandMessages.subtitleSent.toComponent(subtitle, context.locale))
            return
        }

        sender.sendTitleManagerMessage(TitleBroadcastCommandMessages.bothSent.toComponent(title, subtitle, context.locale))
    }
}
