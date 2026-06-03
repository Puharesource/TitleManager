package dev.tarkan.titlemanager.bukkit.command.sidebar

import dev.tarkan.titlemanager.bukkit.command.RootSubCommand
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.localization.SidebarCommandMessages

class SidebarSubCommand(
    coroutineScopeManager: CoroutineScopeManager,
    toggleCommand: SidebarToggleSubCommand
) : RootSubCommand(
    "sidebar",
    "sb",
    "scoreboard",
    description = SidebarCommandMessages.description,
    runCommand = { concurrencyType, body -> coroutineScopeManager.run(concurrencyType, body) },
    subCommands = listOf(toggleCommand)
)