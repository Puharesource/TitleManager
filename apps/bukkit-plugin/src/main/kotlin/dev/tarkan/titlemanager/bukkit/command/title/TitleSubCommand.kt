package dev.tarkan.titlemanager.bukkit.command.title

import dev.tarkan.titlemanager.bukkit.command.RootSubCommand
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.localization.TitleCommandMessages

class TitleSubCommand(
    coroutineScopeManager: CoroutineScopeManager,
    titleBroadcastSubCommand: TitleBroadcastSubCommand,
    titleMessageSubCommand: TitleMessageSubCommand,
    titleWelcomeToggleSubCommand: WelcomeTitleToggleSubCommand
) : RootSubCommand(
    "title",
    "t",
    description = TitleCommandMessages.description,
    runCommand = { concurrencyType, body -> coroutineScopeManager.run(concurrencyType, body) },
    subCommands = listOf(titleBroadcastSubCommand, titleMessageSubCommand, titleWelcomeToggleSubCommand)
)