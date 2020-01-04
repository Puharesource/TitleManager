package io.puharesource.mc.titlemanager.commands

import io.puharesource.mc.titlemanager.extensions.color
import io.puharesource.mc.titlemanager.internal.pluginInstance

object CommandAnimations : TMSubCommand("animations",
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            val animations = pluginInstance.getRegisteredAnimations().keys
            val separator = pluginInstance.config.getString("messages.command-animations.separator").orEmpty().color()

            sendConfigMessage("format",
                    "count" to animations.size.toString(),
                    "animations" to animations.joinToString(separator = separator))
        }})