package dev.tarkan.titlemanager.bukkit.command.subcommands

import dev.tarkan.titlemanager.bukkit.extensions.sendTitleManagerMessage
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.context.PlayerContext
import dev.tarkan.titlemanager.bukkit.extensions.joinToComponent
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.localization.ListAnimationsCommandMessages
import dev.tarkan.titlemanager.parser.placeholder.animation.AnimationPlaceholderRegistry
import org.bukkit.command.CommandSender

class ListAnimationsSubCommand(private val animationPlaceholderRegistry: AnimationPlaceholderRegistry<PlayerContext>) : TitleManagerSubCommand("animations", description = ListAnimationsCommandMessages.description, permission = "titlemanager.command.animations") {
    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        val keys = animationPlaceholderRegistry.keys
        val list = keys.joinToComponent(separator = ", ")

        if (!parameters.isSilent) {
            sender.sendTitleManagerMessage(ListAnimationsCommandMessages.list.toComponent(keys.size, list, context.locale))
        }
    }
}
