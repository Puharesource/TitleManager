package io.puharesource.mc.titlemanager.internal.model.command

import org.bukkit.block.CommandBlock
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

enum class AllowedCommandSender(val disallowMessage: String) {
    ALL(""),
    PLAYER("This command can only be run as a player."),
    CONSOLE("This command can only be run from the console."),
    COMMAND_BLOCK("This command can only be run from a command block.");

    fun isAllowed(sender: CommandSender): Boolean {
        if (this == ALL) return true
        if (this == PLAYER && sender is Player) return true
        if (this == CONSOLE && sender is ConsoleCommandSender) return true
        if (this == COMMAND_BLOCK && sender is CommandBlock) return true

        return false
    }
}
