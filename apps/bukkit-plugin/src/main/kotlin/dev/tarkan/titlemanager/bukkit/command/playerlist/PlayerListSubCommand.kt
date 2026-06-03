package dev.tarkan.titlemanager.bukkit.command.playerlist

import dev.tarkan.titlemanager.bukkit.command.RootSubCommand
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.localization.PlayerListSubCommandMessages

class PlayerListSubCommand(
    coroutineScopeManager: CoroutineScopeManager,
    toggleCommand: PlayerListToggleSubCommand
) : RootSubCommand(
    "playerlist",
    "pl",
    description = PlayerListSubCommandMessages.description,
    runCommand = { concurrencyType, body -> coroutineScopeManager.run(concurrencyType, body) },
    subCommands = listOf(toggleCommand)
)