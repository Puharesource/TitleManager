package dev.tarkan.titlemanager.bukkit.command

import dev.tarkan.titlemanager.bukkit.command.actionbar.ActionbarSubCommand
import dev.tarkan.titlemanager.bukkit.command.actionbar.ActionbarBroadcastSubCommand
import dev.tarkan.titlemanager.bukkit.command.actionbar.ActionbarMessageSubCommand
import dev.tarkan.titlemanager.bukkit.command.playerlist.PlayerListSubCommand
import dev.tarkan.titlemanager.bukkit.command.sidebar.SidebarSubCommand
import dev.tarkan.titlemanager.bukkit.command.subcommands.DiagnosticsSubCommand
import dev.tarkan.titlemanager.bukkit.command.subcommands.ListAnimationsSubCommand
import dev.tarkan.titlemanager.bukkit.command.subcommands.ListScriptsSubCommand
import dev.tarkan.titlemanager.bukkit.command.subcommands.ReloadSubCommand
import dev.tarkan.titlemanager.bukkit.command.subcommands.VersionSubCommand
import dev.tarkan.titlemanager.bukkit.command.title.TitleSubCommand
import dev.tarkan.titlemanager.bukkit.command.title.TitleBroadcastSubCommand
import dev.tarkan.titlemanager.bukkit.command.title.TitleMessageSubCommand
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.configuration.PlaceholderConfiguration
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class TitleManagerCommand(
    placeholderConfiguration: PlaceholderConfiguration,
    titleSubCommand: TitleSubCommand,
    actionbarSubCommand: ActionbarSubCommand,
    sidebarSubCommand: SidebarSubCommand,
    playerListSubCommand: PlayerListSubCommand,
    titleBroadcastSubCommand: TitleBroadcastSubCommand,
    titleMessageSubCommand: TitleMessageSubCommand,
    actionbarBroadcastSubCommand: ActionbarBroadcastSubCommand,
    actionbarMessageSubCommand: ActionbarMessageSubCommand,
    listAnimationsSubCommand: ListAnimationsSubCommand,
    listScriptsSubCommand: ListScriptsSubCommand,
    diagnosticsSubCommand: DiagnosticsSubCommand,
    reloadSubCommand: ReloadSubCommand,
    versionSubCommand: VersionSubCommand,
    coroutineScopeManager: CoroutineScopeManager
) : TabExecutor {
    private val dispatcher = TitleManagerCommandDispatcher(
        placeholderConfiguration,
        runCommand = { concurrencyType, body ->
            coroutineScopeManager.run(concurrencyType, body)
        },
        subCommands = listOf(
            titleSubCommand,
            actionbarSubCommand,
            sidebarSubCommand,
            playerListSubCommand,
            listAnimationsSubCommand,
            listScriptsSubCommand,
            AliasedSubCommand("broadcast", "bc", delegate = titleBroadcastSubCommand),
            AliasedSubCommand("message", "msg", delegate = titleMessageSubCommand),
            AliasedSubCommand("abroadcast", "abc", delegate = actionbarBroadcastSubCommand),
            AliasedSubCommand("amessage", "amsg", delegate = actionbarMessageSubCommand),
            diagnosticsSubCommand,
            reloadSubCommand,
            versionSubCommand
        )
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return dispatcher.onCommand(sender, command, label, args)
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        return dispatcher.onTabComplete(sender, command, alias, args).toMutableList()
    }

    fun suggestions(sender: CommandSender, alias: String, args: Array<out String>): List<CommandSuggestion> {
        return dispatcher.onSuggestions(sender, alias, args)
    }
}

private class AliasedSubCommand(
    name: String,
    vararg aliases: String,
    private val delegate: TitleManagerSubCommand
) : TitleManagerSubCommand(
    name,
    *aliases,
    description = delegate.description,
    concurrencyType = delegate.concurrencyType,
    permission = delegate.permission,
    playerOnly = delegate.playerOnly
) {
    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        delegate.executeCommand(sender, args, parameters, context)
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext): List<String> {
        return delegate.tabComplete(sender, args, parameters, context)
    }
}