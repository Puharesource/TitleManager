package io.puharesource.mc.titlemanager.commands

import io.puharesource.mc.titlemanager.extensions.color
import io.puharesource.mc.titlemanager.extensions.stripColor
import io.puharesource.mc.titlemanager.pluginInstance
import org.bukkit.ChatColor
import org.bukkit.block.CommandBlock
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.util.concurrent.Executor

abstract class TMSubCommand constructor(val name: String,
                                    val allowedSender: AllowedCommandSender = AllowedCommandSender.ALL,
                                    private val executor: Executor? = null,
                                    val aliases: Set<String> = setOf(),
                                    val cmdExecutor: (TMSubCommand, CommandSender, Array<out String>, Map<String, CommandParameter>) -> CommandExecutor) {
    fun runCommand(sender: CommandSender, args: Array<out String>, parameters: Map<String, CommandParameter>) {
        if (executor == null) {
            cmdExecutor(this, sender, args, parameters)
        } else {
            executor.execute { cmdExecutor(this, sender, args, parameters) }
        }
    }

    fun syntaxError(sender: CommandSender) {
        sender.sendMessage("${ChatColor.RED}Wrong usage! Correct usage:")

        val sb = StringBuilder("${ChatColor.RED}").append("    /tm ").append(name.toLowerCase())

        // If a usage is specified, add the usage to the message.
        if (usage.isNotBlank()) {
            sb.append(" ").append(usage)
        }

        // If a description is specified, add the description to the message.
        if (description.isNotBlank()) {
            sb.append("${ChatColor.GRAY}").append(" - ").append(description)
        }

        sender.sendMessage(sb.toString())
    }

    val permission: String
        get() = "titlemanager.command.$name"

    val usage: String
        get() = pluginInstance.config.getString("messages.command-$name.usage").orEmpty().color().stripColor()

    val description: String
        get() = pluginInstance.config.getString("messages.command-$name.description").orEmpty().color().stripColor()
}

enum class AllowedCommandSender {
    ALL(""),
    PLAYER("This command can only be run as a player."),
    CONSOLE("This command can only be run from the console."),
    COMMAND_BLOCK("This command can only be run from a command block.");

    val disallowMessage : String

    constructor(disallowMessage: String) {
        this.disallowMessage = disallowMessage
    }

    fun isAllowed(sender: CommandSender) : Boolean {
        if (this == ALL) return true
        if (this == PLAYER && sender is Player) return true
        if (this == CONSOLE && sender is ConsoleCommandSender) return true
        if (this == COMMAND_BLOCK && sender is CommandBlock) return true

        return false
    }
}