package io.puharesource.mc.titlemanager.commands

import io.puharesource.mc.titlemanager.extensions.color
import io.puharesource.mc.titlemanager.internal.pluginInstance

object CommandReload : TMSubCommand("reload",
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            pluginInstance.reloadPlugin()

            fadeIn = 20
            stay = 40
            fadeOut = 20

            sendConfigMessage("reloaded")
            sendTitle(pluginInstance.config.getString("messages.command-reload.reloaded")!!.color())
        }})
