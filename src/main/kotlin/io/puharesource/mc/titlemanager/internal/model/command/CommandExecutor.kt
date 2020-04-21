package io.puharesource.mc.titlemanager.internal.model.command

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.internal.commands.TMSubCommand
import io.puharesource.mc.titlemanager.internal.extensions.color
import io.puharesource.mc.titlemanager.internal.extensions.sendConfigMessage
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandExecutor(private val plugin: TitleManagerPlugin, val cmd: TMSubCommand, val sender: CommandSender, val args: Array<out String>, val parameters: Map<String, CommandParameter>) {
    var silent: Boolean = false

    var fadeIn: Int = -1
    var stay: Int = -1
    var fadeOut: Int = -1

    var world: World? = null
    var radius: Double? = null

    val message: String
        get() = args.joinToString(separator = " ").color()

    private val recipients: Set<Player>
        get() {
            if (sender is Player && radius != null) {
                val players = sender.getNearbyEntities(radius!!, radius!!, radius!!)
                        .asSequence()
                        .filter { it is Player }
                        .map { it as Player }
                        .toMutableSet()

                players.add(sender)
                return players
            }

            if (world != null) {
                return world!!.players.toSet()
            }

            return Bukkit.getOnlinePlayers().toSet()
        }

    fun getMessageFrom(from: Int): String = args.copyOfRange(from, args.size).joinToString(separator = " ").color()

    fun getPlayerAt(index: Int): Player? = Bukkit.getPlayer(args[index])

    fun syntaxError() = cmd.syntaxError(sender)

    fun sendMessage(message: String) {
        if (!silent) {
            sender.sendMessage(message)
        }
    }

    fun sendConfigMessage(path: String, vararg replace: Pair<String, String>) {
        if (!silent) {
            sender.sendConfigMessage("command-${cmd.name}.$path", *replace)
        }
    }

    fun sendTitle(title: String) {
        if (!silent && sender is Player) {
            plugin.titleManagerComponent.titleService().sendProcessedTitle(sender, title, fadeIn, stay, fadeOut)
        }
    }

    fun sendSubtitle(subtitle: String) {
        if (!silent && sender is Player) {
            plugin.titleManagerComponent.titleService().sendProcessedSubtitle(sender, subtitle, fadeIn, stay, fadeOut)
        }
    }

    fun sendTitleAndSubtitle(title: String, subtitle: String) {
        if (!silent && sender is Player) {
            plugin.titleManagerComponent.titleService().sendProcessedTitle(sender, title, fadeIn, stay, fadeOut)
            plugin.titleManagerComponent.titleService().sendProcessedSubtitle(sender, title, fadeIn, stay, fadeOut)
        }
    }

    fun sendActionbar(text: String) {
        if (!silent && sender is Player) {
            plugin.titleManagerComponent.actionbarService().sendProcessedActionbar(sender, text)
        }
    }

    fun broadcastTitle(title: String) {
        recipients.forEach {
            plugin.titleManagerComponent.titleService().sendProcessedTitle(it, title, fadeIn, stay, fadeOut)
        }
    }

    fun broadcastSubtitle(subtitle: String) {
        recipients.forEach {
            plugin.titleManagerComponent.titleService().sendProcessedSubtitle(it, subtitle, fadeIn, stay, fadeOut)
        }
    }

    fun broadcastTitles(title: String, subtitle: String) {
        recipients.forEach {
            plugin.titleManagerComponent.titleService().sendProcessedTitle(it, title, fadeIn, stay, fadeOut)
            plugin.titleManagerComponent.titleService().sendProcessedSubtitle(it, subtitle, fadeIn, stay, fadeOut)
        }
    }

    fun broadcastActionbar(text: String) {
        recipients.forEach {
            plugin.titleManagerComponent.actionbarService().sendProcessedActionbar(it, text)
        }
    }
}

internal fun commandExecutor(plugin: TitleManagerPlugin, cmd: TMSubCommand, sender: CommandSender, args: Array<out String>, parameters: Map<String, CommandParameter>, init: CommandExecutor.() -> Unit): CommandExecutor {
    val executor = CommandExecutor(plugin, cmd, sender, args, parameters)

    executor.silent = parameters.containsKey("silent")

    parameters["fadein"]?.let { executor.fadeIn = it.getIntOr(10) }
    parameters["stay"]?.let { executor.stay = it.getIntOr(40) }
    parameters["fadeout"]?.let { executor.fadeOut = it.getIntOr(10) }
    parameters["radius"]?.let { executor.radius = it.getDoubleOrNull() }

    parameters["world"]?.let { param ->
        if (param.hasValue()) {
            executor.world = Bukkit.getWorld(param.value!!)
        } else if (sender is Player) {
            executor.world = sender.world
        }
    }

    executor.init()
    return executor
}
