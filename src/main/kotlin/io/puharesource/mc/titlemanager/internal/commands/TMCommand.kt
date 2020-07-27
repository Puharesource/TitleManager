package io.puharesource.mc.titlemanager.internal.commands

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.internal.extensions.color
import io.puharesource.mc.titlemanager.internal.extensions.sendConfigMessage
import io.puharesource.mc.titlemanager.internal.model.command.AllowedCommandSender
import io.puharesource.mc.titlemanager.internal.model.command.CommandParameter
import io.puharesource.mc.titlemanager.internal.model.command.commandExecutor
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.TreeMap
import java.util.TreeSet

class TMCommand constructor(private val plugin: TitleManagerPlugin) : CommandExecutor, TabCompleter {
    private val subCommands: MutableMap<String, TMSubCommand> = TreeMap(String.CASE_INSENSITIVE_ORDER)
    private val parameterPattern = """(?i)^[-](silent|world|fadein|stay|fadeout|radius)$""".toRegex()
    private val parameterPatternWithValue = """(?i)^[-](silent|world|fadein|stay|fadeout|radius)[=]([^ ]+)$""".toRegex()
    private val commandSplitPattern = """([<]nl[>])|(\\n)""".toRegex()

    init {
        addSubCommand(createSubCommand(aliases = *arrayOf("abroadcast", "abc"), executor = ::executorActionbarBroadcast))
        addSubCommand(createSubCommand(aliases = *arrayOf("amessage", "amsg"), executor = ::executorActionbarMessage))
        addSubCommand(createSubCommand(aliases = *arrayOf("animations"), executor = ::executorAnimations))
        addSubCommand(createSubCommand(aliases = *arrayOf("broadcast", "bc"), executor = ::executorBroadcast))
        addSubCommand(createSubCommand(aliases = *arrayOf("message", "msg"), executor = ::executorMessage))
        addSubCommand(createSubCommand(aliases = *arrayOf("reload"), executor = ::executorReload))
        addSubCommand(createSubCommand(aliases = *arrayOf("scoreboard", "sb"), allowedSender = AllowedCommandSender.PLAYER, executor = ::executorScoreboard))
        addSubCommand(createSubCommand(aliases = *arrayOf("scripts"), executor = ::executorScripts))
        addSubCommand(createSubCommand(aliases = *arrayOf("version"), executor = ::executorVersion))
    }

    private fun addSubCommand(cmd: TMSubCommand) {
        subCommands[cmd.name] = cmd
        cmd.aliases.forEach { subCommands[it] = cmd }
    }

    private fun createSubCommand(vararg aliases: String, allowedSender: AllowedCommandSender = AllowedCommandSender.ALL, executor: (io.puharesource.mc.titlemanager.internal.model.command.CommandExecutor) -> Unit): TMSubCommand {
        val name = aliases.first()
        val otherAliases = aliases.filterIndexed { index, _ -> index != 0 }.toSet()
        val cmdExecutor: (TMSubCommand, CommandSender, Array<out String>, Map<String, CommandParameter>) -> io.puharesource.mc.titlemanager.internal.model.command.CommandExecutor = { cmd, sender, args, parameters ->
            commandExecutor(plugin, cmd, sender, args, parameters, executor)
        }

        return TMSubCommand(plugin = plugin, name = name, aliases = otherAliases, allowedSender = allowedSender, cmdExecutor = cmdExecutor)
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

    private fun syntaxError(sender: CommandSender) {
        sender.sendMessage("${ChatColor.RED}Wrong usage! Correct usage" + if (subCommands.size > 1) "s:" else ":")
        commandList(sender, ChatColor.RED, ChatColor.GRAY)
    }

    private fun helpMessage(sender: CommandSender) {
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

        val parameters: MutableMap<String, CommandParameter> = TreeMap(String.CASE_INSENSITIVE_ORDER)

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
        if (args.size == 1) {
            return subCommands.keys
                .asSequence()
                .filter { it.startsWith(args[0], ignoreCase = true) }
                .toMutableList()
        }

        if (args.size == 2) {
            if (args[1].startsWith("-")) {
                return arrayOf("silent", "world", "fadein", "stay", "fadeout", "radius")
                    .asSequence()
                    .map { "-$it" }
                    .filter { it.startsWith(args[1], ignoreCase = true) }
                    .toMutableList()
            }
        }

        return null
    }

    private fun executorActionbarBroadcast(commandExecutor: io.puharesource.mc.titlemanager.internal.model.command.CommandExecutor) {
        if (commandExecutor.args.isEmpty()) {
            commandExecutor.syntaxError()
            return
        }

        commandExecutor.sendConfigMessage("sent", "title" to commandExecutor.message)
        commandExecutor.broadcastActionbar(commandExecutor.message)
    }

    private fun executorActionbarMessage(commandExecutor: io.puharesource.mc.titlemanager.internal.model.command.CommandExecutor) {
        if (commandExecutor.args.size <= 1) {
            commandExecutor.syntaxError()
            return
        }

        val player = commandExecutor.getPlayerAt(0)

        if (player == null) {
            commandExecutor.sendConfigMessage("invalid-player", "player" to commandExecutor.args.first())
            return
        }

        val message = commandExecutor.getMessageFrom(1)

        commandExecutor.sendConfigMessage("sent", "player" to player.name, "title" to message)
        plugin.titleManagerComponent.actionbarService().sendProcessedActionbar(player, message)
    }

    private fun executorAnimations(commandExecutor: io.puharesource.mc.titlemanager.internal.model.command.CommandExecutor) {
        val animations = plugin.titleManagerComponent.animationsService().animations.keys
        val separator = plugin.config.getString("messages.command-animations.separator").orEmpty().color()

        commandExecutor.sendConfigMessage("format",
                "count" to animations.size.toString(),
                "animations" to animations.joinToString(separator = separator))
    }

    private fun executorBroadcast(commandExecutor: io.puharesource.mc.titlemanager.internal.model.command.CommandExecutor) {
        if (commandExecutor.args.isEmpty()) {
            commandExecutor.syntaxError()
            return
        }

        if (commandExecutor.message.contains(commandSplitPattern)) {
            val parts = commandExecutor.message.split(commandSplitPattern, limit = 2)

            val title = parts[0]
            val subtitle = parts[1]

            if (subtitle.isNotBlank()) {
                if (title.isBlank()) {
                    commandExecutor.sendConfigMessage("subtitle-sent", "subtitle" to subtitle)
                    commandExecutor.broadcastSubtitle(subtitle)
                } else {
                    commandExecutor.sendConfigMessage("both-sent", "title" to title, "subtitle" to subtitle)
                    commandExecutor.broadcastTitles(title, subtitle)
                }

                return
            }
        }

        commandExecutor.sendConfigMessage("title-sent", "title" to commandExecutor.message)
        commandExecutor.broadcastTitle(commandExecutor.message)
    }

    private fun executorMessage(commandExecutor: io.puharesource.mc.titlemanager.internal.model.command.CommandExecutor) {
        if (commandExecutor.args.size <= 1) {
            commandExecutor.syntaxError()
            return
        }

        val player = commandExecutor.getPlayerAt(0)

        if (player == null) {
            commandExecutor.sendConfigMessage("invalid-player", "player" to commandExecutor.args.first())
            return
        }

        val message = commandExecutor.getMessageFrom(1)
        val titleService = plugin.titleManagerComponent.titleService()

        if (message.contains(commandSplitPattern)) {
            val parts = message.split(commandSplitPattern, limit = 2)

            if (parts[1].isNotBlank()) {
                if (parts[0].isBlank()) {
                    commandExecutor.sendConfigMessage("subtitle-sent", "player" to player.name, "subtitle" to parts[1])
                    titleService.sendProcessedSubtitle(player, parts[1], fadeIn = commandExecutor.fadeIn, stay = commandExecutor.stay, fadeOut = commandExecutor.fadeOut)
                } else {
                    commandExecutor.sendConfigMessage("both-sent", "player" to player.name, "title" to parts[0], "subtitle" to parts[1])
                    titleService.sendProcessedTitle(player, parts[0], fadeIn = commandExecutor.fadeIn, stay = commandExecutor.stay, fadeOut = commandExecutor.fadeOut)
                    titleService.sendProcessedSubtitle(player, parts[1], fadeIn = commandExecutor.fadeIn, stay = commandExecutor.stay, fadeOut = commandExecutor.fadeOut)
                }

                return
            }
        }

        commandExecutor.sendConfigMessage("title-sent", "player" to player.name, "title" to message)
        titleService.sendProcessedTitle(player, message, fadeIn = commandExecutor.fadeIn, stay = commandExecutor.stay, fadeOut = commandExecutor.fadeOut)
    }

    private fun executorReload(commandExecutor: io.puharesource.mc.titlemanager.internal.model.command.CommandExecutor) {
        plugin.reloadPlugin()

        commandExecutor.fadeIn = 20
        commandExecutor.stay = 40
        commandExecutor.fadeOut = 20

        commandExecutor.sendConfigMessage("reloaded")
        commandExecutor.sendTitle(plugin.config.getString("messages.command-reload.reloaded")!!.color())
    }

    private fun executorScoreboard(commandExecutor: io.puharesource.mc.titlemanager.internal.model.command.CommandExecutor) {
        if (commandExecutor.args.isEmpty() || !commandExecutor.args.first().equals("toggle", ignoreCase = true)) {
            commandExecutor.syntaxError()
            return
        }

        val player = commandExecutor.sender as Player
        val playerInfoService = plugin.titleManagerComponent.playerInfoService()
        val scoreboardService = plugin.titleManagerComponent.scoreboardService()

        playerInfoService.showScoreboard(player, !playerInfoService.isScoreboardEnabled(player))

        if (playerInfoService.isScoreboardEnabled(player)) {
            scoreboardService.toggleScoreboardInWorld(player, player.world)

            commandExecutor.sendConfigMessage("toggled-on")
        } else {
            scoreboardService.removeScoreboard(player)

            commandExecutor.sendConfigMessage("toggled-off")
        }
    }

    private fun executorScripts(commandExecutor: io.puharesource.mc.titlemanager.internal.model.command.CommandExecutor) {
        val scripts = plugin.getRegisteredScripts()

        commandExecutor.sendConfigMessage("format",
                "count" to scripts.size.toString(),
                "scripts" to scripts.joinToString(separator = plugin.config.getString("messages.command-scripts.separator").orEmpty().color()))
    }

    private fun executorVersion(commandExecutor: io.puharesource.mc.titlemanager.internal.model.command.CommandExecutor) {
        commandExecutor.sendConfigMessage("version", "version" to plugin.description.version)

        val updateService = plugin.titleManagerComponent.updateService()

        if (updateService.isUpdateAvailable) {
            commandExecutor.sendMessage("${ChatColor.YELLOW}An update is available version: ${updateService.latestVersion}")
        }
    }
}
