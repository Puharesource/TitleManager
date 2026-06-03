package dev.tarkan.titlemanager.bukkit.command.actionbar

import dev.tarkan.titlemanager.bukkit.command.RootSubCommand
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.localization.ActionbarCommandMessages

class ActionbarSubCommand(
    coroutineScopeManager: CoroutineScopeManager,
    broadcastCommand: ActionbarBroadcastSubCommand,
    messageCommand: ActionbarMessageSubCommand,
    welcomeToggleCommand: WelcomeActionbarToggleSubCommand
) : RootSubCommand(
    "actionbar",
    "a",
    description = ActionbarCommandMessages.description,
    runCommand = { concurrencyType, body -> coroutineScopeManager.run(concurrencyType, body) },
    subCommands = listOf(broadcastCommand, messageCommand, welcomeToggleCommand)
)