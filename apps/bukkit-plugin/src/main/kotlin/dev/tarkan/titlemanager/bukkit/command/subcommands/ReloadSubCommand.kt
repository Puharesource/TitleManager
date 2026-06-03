package dev.tarkan.titlemanager.bukkit.command.subcommands

import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.configuration.ConfigurationException
import dev.tarkan.titlemanager.bukkit.extensions.toErrorComponent
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.lifecycle.TitleManagerReloader
import dev.tarkan.titlemanager.bukkit.localization.ReloadCommandMessages
import org.bukkit.command.CommandSender
import kotlin.time.DurationUnit
import kotlin.time.measureTime

class ReloadSubCommand(private val reloader: TitleManagerReloader) : TitleManagerSubCommand("reload", description = ReloadCommandMessages.description, permission = "titlemanager.command.reload") {
    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        if (!parameters.isSilent) {
            sender.sendMessage(ReloadCommandMessages.before.toComponent(context.locale))
        }

        try {
            val elapsedTime = measureTime {
                reloader.reload()
            }

            if (!parameters.isSilent) {
                sender.sendMessage(ReloadCommandMessages.after.toComponent(elapsedTime.toString(DurationUnit.MILLISECONDS), context.locale))
            }
        } catch (exception: ConfigurationException) {
            sender.sendMessage(ReloadCommandMessages.failed.toErrorComponent(exception.message ?: "Unknown configuration error.", context.locale))
        }
    }
}