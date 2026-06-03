package dev.tarkan.titlemanager.bukkit.command

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.tarkan.titlemanager.bukkit.concurrency.ConcurrencyType
import kotlinx.coroutines.CoroutineScope
import org.bukkit.command.CommandSender

abstract class RootSubCommand(
    name: String,
    vararg aliases: String,
    description: MessageBundleLocalizedString,
    private val runCommand: (ConcurrencyType, suspend CoroutineScope.() -> Unit) -> Unit,
    val subCommands: List<TitleManagerSubCommand>
) : TitleManagerSubCommand(name, *aliases, description = description) {

    final override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        val argument = args.firstOrNull()
        val subCommand = subCommands.findByArgument(argument)

        if (subCommand == null) {
            sendHelp(sender, context)
            return
        }
        val subCommandArgs = args.drop(1).toTypedArray()

        if (!subCommand.canExecute(sender, context)) {
            return
        }

        val childParameters = CommandParameters.fromArguments(subCommandArgs.toList())
        val effectiveParameters = parameters + childParameters
        val effectiveSubCommandArgs = subCommandArgs.drop(childParameters.consumedArgumentCount).toTypedArray()

        if (concurrencyType != subCommand.concurrencyType) {
            runCommand(subCommand.concurrencyType) {
                sender.executeSafely {
                    subCommand.executeCommand(sender, effectiveSubCommandArgs, effectiveParameters, context)
                }
            }
        } else {
            sender.executeSafely {
                subCommand.executeCommand(sender, effectiveSubCommandArgs, effectiveParameters, context)
            }
        }
    }

    final override fun tabComplete(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext): List<String> {
        return suggest(sender, args, parameters, context).map { it.text }
    }

    final override fun suggest(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext): List<CommandSuggestion> {
        val argument = args.firstOrNull()

        if (argument == null || args.size == 1 && subCommands.findByArgument(argument) == null) {
            return subCommands.visibleSuggestions(sender, argument, context)
        }

        val subCommand = subCommands.findByArgument(argument) ?: return emptyList()

        if (!subCommand.isVisibleTo(sender)) {
            return emptyList()
        }

        val subCommandArgs = args.drop(1).toTypedArray()
        val childParameters = CommandParameters.fromArguments(subCommandArgs.toList())
        val effectiveParameters = parameters + childParameters
        val effectiveSubCommandArgs = subCommandArgs.drop(childParameters.consumedArgumentCount).toTypedArray()

        return subCommand.suggest(sender, effectiveSubCommandArgs, effectiveParameters, context)
    }

    private fun sendHelp(sender: CommandSender, context: CommandContext) {
        sender.sendCommandHelp("/tm $name", subCommands, context)
    }
}