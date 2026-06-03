package dev.tarkan.titlemanager.bukkit.command

import dev.tarkan.titlemanager.bukkit.extensions.sendTitleManagerMessage
import dev.tarkan.titlemanager.bukkit.extensions.MessageColors
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.extensions.toErrorComponent
import dev.tarkan.titlemanager.bukkit.localization.GlobalCommandMessages
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

internal const val DEFAULT_UNSUPPORTED_RUNTIME_MESSAGE = "This command is not supported on the selected runtime."

internal fun Collection<TitleManagerSubCommand>.findByArgument(argument: String?): TitleManagerSubCommand? {
    if (argument == null) {
        return null
    }

    return firstOrNull { it.matches(argument) }
}

internal fun TitleManagerSubCommand.matches(argument: String): Boolean {
    return name.equals(argument, ignoreCase = true) || aliases.any { it.equals(argument, ignoreCase = true) }
}

internal fun Collection<TitleManagerSubCommand>.visibleSuggestions(
    sender: CommandSender,
    argument: String?,
    context: CommandContext
): List<CommandSuggestion> {
    return filter { it.isVisibleTo(sender) }
        .flatMap { subCommand -> subCommand.suggestionsFor(argument, context) }
        .sortedBy { it.text }
}

internal fun TitleManagerSubCommand.suggestionsFor(argument: String?, context: CommandContext): List<CommandSuggestion> {
    return (aliases.toList() + name)
        .filter { argument == null || it.startsWith(argument, ignoreCase = true) }
        .map { CommandSuggestion(it, description.toComponent(context.locale)) }
}

internal fun CommandSender.sendCommandHelp(
    baseCommand: String,
    subCommands: Collection<TitleManagerSubCommand>,
    context: CommandContext
) {
    sendTitleManagerMessage(GlobalCommandMessages.helpHeader.toErrorComponent(baseCommand, context.locale))

    for (subCommand in subCommands.filter { it.isVisibleTo(this) }) {
        sendTitleManagerMessage(
            GlobalCommandMessages.helpLine.toErrorComponent(
                "$baseCommand ${subCommand.name}",
                subCommand.description.toErrorComponent(context.locale),
                context.locale
            )
        )
    }
}

internal suspend fun CommandSender.executeSafely(body: suspend () -> Unit) {
    try {
        body()
    } catch (exception: CommandError) {
        sendTitleManagerMessage(exception.message.orEmpty().toRawErrorComponent())
    } catch (exception: UnsupportedOperationException) {
        sendTitleManagerMessage((exception.message ?: DEFAULT_UNSUPPORTED_RUNTIME_MESSAGE).toRawErrorComponent())
    }
}

internal fun String.toRawErrorComponent(): Component = Component.text(this).color(MessageColors.ERROR)
