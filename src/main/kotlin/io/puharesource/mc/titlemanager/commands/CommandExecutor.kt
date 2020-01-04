package io.puharesource.mc.titlemanager.commands

import io.puharesource.mc.titlemanager.extensions.color
import io.puharesource.mc.titlemanager.extensions.sendActionbar
import io.puharesource.mc.titlemanager.extensions.sendConfigMessage
import io.puharesource.mc.titlemanager.extensions.sendSubtitle
import io.puharesource.mc.titlemanager.extensions.sendTitle
import io.puharesource.mc.titlemanager.extensions.sendTitles
import io.puharesource.mc.titlemanager.internal.pluginInstance
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommandExecutor(val cmd: TMSubCommand, val sender: CommandSender, val args: Array<out String>, val parameters: Map<String, CommandParameter>) {
    var silent: Boolean = false

    var fadeIn: Int = -1
    var stay: Int = -1
    var fadeOut: Int = -1

    var world: World? = null
    var radius: Double? = null

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
            sender.sendTitle(title, fadeIn, stay, fadeOut, withPlaceholders = true)
        }
    }

    fun sendSubtitle(subtitle: String) {
        if (!silent && sender is Player) {
            sender.sendSubtitle(subtitle, fadeIn, stay, fadeOut, withPlaceholders = true)
        }
    }

    fun sendTitleAndSubtitle(title: String, subtitle: String) {
        if (!silent && sender is Player) {
            sender.sendTitles(title, subtitle, fadeIn, stay, fadeOut, withPlaceholders = true)
        }
    }

    fun sendActionbar(text: String) {
        if (!silent && sender is Player) {
            sender.sendActionbar(text, withPlaceholders = true)
        }
    }

    fun Player.sendTitle(title: String, checkForAnimations: Boolean = false) {
        if (checkForAnimations && pluginInstance.containsAnimations(title)) {
            val parts = pluginInstance.toAnimationParts(title)
            this.sendTitle(parts, withPlaceholders = true)
        } else {
            this.sendTitle(title, fadeIn, stay, fadeOut, withPlaceholders = true)
        }
    }

    fun Player.sendSubtitle(subtitle: String, checkForAnimations: Boolean = false) {
        if (checkForAnimations && pluginInstance.containsAnimations(subtitle)) {
            val parts = pluginInstance.toAnimationParts(subtitle)
            this.sendSubtitle(parts, withPlaceholders = true)
        } else {
            this.sendSubtitle(subtitle, fadeIn, stay, fadeOut, withPlaceholders = true)
        }
    }

    fun Player.sendTitleAndSubtitle(title: String, subtitle: String, checkForAnimations: Boolean = false) {
        if (checkForAnimations && (pluginInstance.containsAnimations(title) || pluginInstance.containsAnimations(subtitle))) {
            if (pluginInstance.containsAnimations(title)) {
                val parts = pluginInstance.toAnimationParts(title)
                this.sendTitle(parts, withPlaceholders = true)
            } else {
                this.sendTitle(title, fadeIn, stay, fadeOut, withPlaceholders = true)
            }

            if (pluginInstance.containsAnimations(subtitle)) {
                val parts = pluginInstance.toAnimationParts(subtitle)
                this.sendTitle(parts, withPlaceholders = true)
            } else {
                this.sendSubtitle(subtitle, fadeIn, stay, fadeOut, withPlaceholders = true)
            }
        } else {
            this.sendTitles(title, subtitle, fadeIn, stay, fadeOut)
        }
    }

    fun Player.sendActionbar(text: String, checkForAnimations: Boolean = false) {
        if (checkForAnimations && pluginInstance.containsAnimations(text)) {
            val parts = pluginInstance.toAnimationParts(text)
            this.sendActionbar(parts, withPlaceholders = true)
        } else {
            this.sendActionbar(text, withPlaceholders = true)
        }
    }

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

    val message: String get() = args.joinToString(separator = " ").color()

    fun getMessageFrom(from: Int) : String = args.copyOfRange(from, args.size).joinToString(separator = " ").color()

    fun getPlayerAt(index: Int) : Player? = Bukkit.getPlayer(args[index])

    fun broadcastTitle(title: String) {
        if (pluginInstance.containsAnimations(title)) {
            val parts = pluginInstance.toAnimationParts(title)
            recipients.forEach { it.sendTitle(parts, withPlaceholders = true) }
        } else {
            recipients.forEach { it.sendTitle(title, checkForAnimations = false) }
        }
    }

    fun broadcastSubtitle(subtitle: String) {
        if (pluginInstance.containsAnimations(subtitle)) {
            val parts = pluginInstance.toAnimationParts(subtitle)
            recipients.forEach { it.sendSubtitle(parts, withPlaceholders = true) }
        } else {
            recipients.forEach { it.sendSubtitle(subtitle, checkForAnimations = false) }
        }
    }

    fun broadcastTitles(title: String, subtitle: String) {
        recipients.forEach { it.sendTitleAndSubtitle(title, subtitle) }
    }

    fun broadcastActionbar(text: String) {
        if (pluginInstance.containsAnimations(text)) {
            val parts = pluginInstance.toAnimationParts(text)
            recipients.forEach { it.sendActionbar(parts, withPlaceholders = true) }
        } else {
            recipients.forEach { it.sendActionbar(text, checkForAnimations = false) }
        }
    }
}

internal fun commandExecutor(cmd: TMSubCommand, sender: CommandSender, args: Array<out String>, parameters: Map<String, CommandParameter>, init: CommandExecutor.() -> Unit): CommandExecutor {
    val executor = CommandExecutor(cmd, sender, args, parameters)

    executor.silent = parameters.containsKey("silent")

    parameters["fadein"]?.let { executor.fadeIn = it.getIntOr(-1) }
    parameters["stay"]?.let { executor.stay = it.getIntOr(-1) }
    parameters["fadeout"]?.let { executor.fadeOut = it.getIntOr(-1) }
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