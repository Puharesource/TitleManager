package io.puharesource.mc.titlemanager.internal.functionality.commands

import io.puharesource.mc.titlemanager.internal.extensions.color
import io.puharesource.mc.titlemanager.internal.pluginInstance

object CommandScripts : TMSubCommand("scripts",
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            val scripts = pluginInstance.getRegisteredScripts()

            sendConfigMessage("format",
                    "count" to scripts.size.toString(),
                    "scripts" to scripts.joinToString(separator = pluginInstance.config.getString("messages.command-scripts.separator").orEmpty().color()))
        }})