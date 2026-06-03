package dev.tarkan.titlemanager.bukkit.command

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.tarkan.titlemanager.bukkit.concurrency.ConcurrencyType
import dev.tarkan.titlemanager.bukkit.extensions.toErrorComponent
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.localization.GlobalCommandMessages
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

abstract class TitleManagerSubCommand(
    val name: String,
    vararg val aliases: String,
    val description: MessageBundleLocalizedString,
    val concurrencyType: ConcurrencyType = ConcurrencyType.UNDEFINED,
    val permission: String? = null,
    val playerOnly: Boolean = false
) {
    abstract suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext)

    open fun tabComplete(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext): List<String> {
        return emptyList()
    }

    open fun suggest(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext): List<CommandSuggestion> {
        return tabComplete(sender, args, parameters, context).map(CommandSuggestion::text)
    }

    fun canExecute(sender: CommandSender, context: CommandContext): Boolean {
        if (permission != null && !sender.hasPermission(permission)) {
            sender.sendMessage(GlobalCommandMessages.noPermission.toErrorComponent(permission, context.locale))
            return false
        }

        if (playerOnly && sender !is Player) {
            sender.sendMessage(GlobalCommandMessages.playerOnly.toErrorComponent(context.locale))
            return false
        }

        return true
    }

    protected fun sendInvalidUsage(sender: CommandSender, usage: MessageBundleLocalizedString, context: CommandContext) {
        sender.sendMessage(GlobalCommandMessages.invalidUsage.toErrorComponent(usage.toComponent(context.locale), context.locale))
    }

    protected fun CommandSender.sendIfNotSilent(parameters: CommandParameters, message: Component) {
        if (!parameters.isSilent) {
            sendMessage(message)
        }
    }

    protected fun Array<out String>.joinedMessage(startIndex: Int = 0): String {
        return drop(startIndex).joinToString(separator = " ")
    }

    protected fun Server.completePlayerNames(args: Array<out String>): List<String> {
        if (args.size > 1) {
            return emptyList()
        }

        val prefix = args.firstOrNull().orEmpty()

        return onlinePlayers
            .map { it.name }
            .filter { it.startsWith(prefix, ignoreCase = true) }
            .sorted()
    }

    fun isVisibleTo(sender: CommandSender): Boolean {
        return permission == null || sender.hasPermission(permission)
    }
}
