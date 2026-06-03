package dev.tarkan.titlemanager.bukkit.command

import de.comahe.i18n4k.Locale
import dev.tarkan.titlemanager.bukkit.concurrency.ConcurrencyType
import dev.tarkan.titlemanager.bukkit.configuration.PlaceholderConfiguration
import kotlinx.coroutines.CoroutineScope
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TitleManagerCommandDispatcher(
    private val placeholderConfiguration: PlaceholderConfiguration,
    private val runCommand: (ConcurrencyType, suspend CoroutineScope.() -> Unit) -> Unit,
    private val subCommands: List<TitleManagerSubCommand>
) {
    fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val context = CommandContext(
            locale = getLocale(sender)
        )

        val subCommand = subCommands.findByArgument(args.firstOrNull())

        if (subCommand == null) {
            sender.sendCommandHelp("/tm", subCommands, context)

            return true
        }

        if (!subCommand.canExecute(sender, context)) {
            return true
        }

        runCommand(subCommand.concurrencyType) {
            sender.executeSafely {
                val parameters = CommandParameters.fromArguments(args.drop(1))

                subCommand.executeCommand(sender, args.drop(parameters.consumedArgumentCount + 1).toTypedArray(), parameters, context)
            }
        }

        return true
    }

    fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        return onSuggestions(sender, alias, args).map { it.text }
    }

    fun onSuggestions(sender: CommandSender, alias: String, args: Array<out String>): List<CommandSuggestion> {
        val context = CommandContext(
            locale = getLocale(sender)
        )
        val firstArgument = args.firstOrNull()

        if (firstArgument == null || args.size == 1) {
            return subCommands.visibleSuggestions(sender, firstArgument, context)
        }

        val subCommand = subCommands.findByArgument(firstArgument) ?: return emptyList()

        if (!subCommand.isVisibleTo(sender)) {
            return emptyList()
        }

        val subCommandArgs = args.drop(1).toTypedArray()
        val parameterCompletions = completeParameterSuggestions(subCommandArgs)
        if (parameterCompletions.isNotEmpty()) {
            return parameterCompletions
        }

        val parameters = CommandParameters.fromArguments(subCommandArgs.toList())

        return subCommand.suggest(sender, subCommandArgs.drop(parameters.consumedArgumentCount).toTypedArray(), parameters, context)
    }

    private fun getLocale(sender: CommandSender): Locale {
        if (sender is Player) {
            return Locale.forLanguageTag(sender.locale().toLanguageTag())
        }

        return Locale.forLanguageTag(placeholderConfiguration.locale)
    }


    private fun completeParameterSuggestions(args: Array<out String>): List<CommandSuggestion> {
        val currentArgument = args.lastOrNull() ?: return emptyList()
        if (args.dropLast(1).any { !CommandParameters.isParameterArgument(it) }) {
            return emptyList()
        }

        return CommandParameters.completeParameterSuggestions(currentArgument)
    }

}
