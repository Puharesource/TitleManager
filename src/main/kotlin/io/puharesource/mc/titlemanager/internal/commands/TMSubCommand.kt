package io.puharesource.mc.titlemanager.internal.commands

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.internal.extensions.color
import io.puharesource.mc.titlemanager.internal.extensions.stripColor
import io.puharesource.mc.titlemanager.internal.model.command.AllowedCommandSender
import io.puharesource.mc.titlemanager.internal.model.command.CommandExecutor
import io.puharesource.mc.titlemanager.internal.model.command.CommandParameter
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.CommandSender
import java.util.concurrent.Executor

class TMSubCommand constructor(
    val plugin: TitleManagerPlugin,
    val name: String,
    val allowedSender: AllowedCommandSender = AllowedCommandSender.ALL,
    private val executor: Executor? = null,
    val aliases: Set<String> = setOf(),
    val cmdExecutor: (TMSubCommand, CommandSender, Array<out String>, Map<String, CommandParameter>) -> CommandExecutor
) {
    fun runCommand(sender: CommandSender, args: Array<out String>, parameters: Map<String, CommandParameter>) {
        if (executor == null) {
            cmdExecutor(this, sender, args, parameters)
        } else {
            executor.execute { cmdExecutor(this, sender, args, parameters) }
        }
    }

    fun syntaxError(sender: CommandSender) {
        sender.sendMessage("${ChatColor.RED}Wrong usage! Correct usage:")

        sender.sendMessage(
            buildString {
                append("${ChatColor.RED}").append("    /tm ").append(name.lowercase())

                if (usage.isNotBlank()) {
                    append(" ").append(usage)
                }

                // If a description is specified, add the description to the message.
                if (description.isNotBlank()) {
                    append("${ChatColor.GRAY}").append(" - ").append(description)
                }
            }
        )
    }

    val permission: String
        get() = "titlemanager.command.$name"

    val usage: String
        get() = plugin.config.getString("messages.command-$name.usage").orEmpty().color().stripColor()

    val description: String
        get() = plugin.config.getString("messages.command-$name.description").orEmpty().color().stripColor()
}
