package io.puharesource.mc.titlemanager.commands

import io.puharesource.mc.titlemanager.extensions.color
import io.puharesource.mc.titlemanager.pluginInstance

object CommandScripts : TMSubCommand("scripts",
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            val scripts = pluginInstance.getRegisteredScripts()

            sendConfigMessage("format",
                    "count" to scripts.size.toString(),
                    "scripts" to scripts.joinToString(separator = pluginInstance.config.getString("messages.command-scripts.separator").orEmpty().color()))
        }})