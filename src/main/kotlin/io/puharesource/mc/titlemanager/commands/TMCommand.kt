package io.puharesource.mc.titlemanager.commands

import io.puharesource.mc.titlemanager.extensions.sendConfigMessage
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.TreeMap
import java.util.TreeSet

object TMCommand : CommandExecutor, TabCompleter {
    private val subCommands : MutableMap<String, TMSubCommand> = TreeMap(String.CASE_INSENSITIVE_ORDER)
    private val parameterPattern = """(?i)^[-](silent|world|fadein|stay|fadeout|radius)$""".toRegex()
    private val parameterPatternWithValue = """(?i)^[-](silent|world|fadein|stay|fadeout|radius)[=]([^ ]+)$""".toRegex()

    init {
        addSubCommand(CommandBroadcast)
        addSubCommand(CommandABroadcast)
        addSubCommand(CommandMessage)
        addSubCommand(CommandAMessage)
        addSubCommand(CommandScoreboard)
        addSubCommand(CommandAnimations)
        addSubCommand(CommandScripts)
        addSubCommand(CommandReload)
        addSubCommand(CommandVersion)
    }

    fun addSubCommand(cmd: TMSubCommand) {
        subCommands[cmd.name] = cmd
        cmd.aliases.forEach { subCommands[it] = cmd }
    }

    /**
     * Sends a list of commands, usages and descriptions to a command sender.

     * @param sender The command sender to send the list to.
     * *
     * @param primaryColor The primary color of the list.
     * *
     * @param secondaryColor The secondary color of the list.
     */
    private fun commandList(sender: CommandSender, primaryColor: ChatColor, secondaryColor: ChatColor) {
        // A set of commands aliases that have already been formatted.
        val alreadyShown = TreeSet(String.CASE_INSENSITIVE_ORDER)

        // Iterate over all aliases and send the formatted message.
        for (cmd in subCommands.values) {
            // Continue if the command has already been shown.
            if (alreadyShown.contains(cmd.name)) continue

            // Continue if the player doesn't have permission to use the command.
            if (cmd.permission.isNotBlank() && !sender.hasPermission(cmd.permission)) continue

            // Add the command's name to the list of shown commands, to avoid showing the same command multiple times.
            alreadyShown.add(cmd.name)

            // Send the formatted message.
            sender.sendMessage(formatCommandListString(cmd, primaryColor, secondaryColor))
        }
    }

    /**
     * Gets a formatted command, usage and description string, from the specified command.
     *
     * @param subCommand If the command to be described is a sub command, then specify an empty optional.
     * @param primaryColor The primary color to be used.
     * @param secondaryColor The secondary color to be used.
     * @return The line
     */
    private fun formatCommandListString(subCommand: TMSubCommand, primaryColor: ChatColor, secondaryColor: ChatColor): String {
        // Create the string builder, to build the message.
        val sb = StringBuilder("$primaryColor").append("    /tm")

        sb.append(" ").append(subCommand.name.toLowerCase())

        // If a usage is specified, add the usage to the message.
        if (subCommand.usage.isNotBlank()) {
            sb.append(" ").append(subCommand.usage)
        }

        // If a description is specified, add the description to the message.
        if (subCommand.description.isNotBlank()) {
            sb.append("$secondaryColor").append(" - ").append(subCommand.description)
        }

        return sb.toString()
    }

    fun syntaxError(sender: CommandSender) {
        sender.sendMessage("${ChatColor.RED}Wrong usage! Correct usage" + if (subCommands.size > 1) "s:" else ":")
        commandList(sender, ChatColor.RED, ChatColor.GRAY)
    }

    fun helpMessage(sender: CommandSender) {
        sender.sendMessage(String.format("${ChatColor.YELLOW}Command \"/tm\" help:"))
        commandList(sender, ChatColor.YELLOW, ChatColor.GRAY)
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (!cmd.name.equals("tm", ignoreCase = true)) return false

        if (args.isEmpty()) {
            syntaxError(sender)
            return true
        }

        if (args.first().equals("help", ignoreCase = true) || args.first().equals("?", ignoreCase = true)) {
            helpMessage(sender)
            return true
        }

        val subCommand = subCommands[args.first()]

        if (subCommand == null) {
            syntaxError(sender)
            return true
        }

        if (subCommand.permission.isNotBlank() && !sender.hasPermission(subCommand.permission)) {
            sender.sendConfigMessage("no-permission")
            return true
        }

        if (!subCommand.allowedSender.isAllowed(sender)) {
            sender.sendMessage("${ChatColor.RED}${subCommand.allowedSender.disallowMessage}")
            return true
        }

        val parameters : MutableMap<String, CommandParameter> = TreeMap(String.CASE_INSENSITIVE_ORDER)

        var i = 0
        for (arg in args) {
            if (i == 0) {
                i++
                continue
            }

            if (arg.matches(parameterPattern)) {
                val parameter = parameterPattern.matchEntire(arg)!!.groups[1]!!.value

                parameters[parameter] = CommandParameter(parameter)
            } else if (arg.matches(parameterPatternWithValue)) {
                val result = parameterPatternWithValue.matchEntire(arg)!!.groups
                val parameter = result[1]!!.value
                val value = result[2]!!.value

                parameters[parameter] = CommandParameter(parameter, value)
            } else {
                break
            }

            i++
        }

        subCommand.runCommand(sender, args.copyOfRange(i, args.size), parameters)
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String>? {
        if (args.isNotEmpty()) {
            return subCommands.keys
                    .asSequence()
                    .filter { it.startsWith(args[args.size - 1], ignoreCase = true) }
                    .toMutableList()
        }

        return null
    }
}