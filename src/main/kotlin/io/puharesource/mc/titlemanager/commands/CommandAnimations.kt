package io.puharesource.mc.titlemanager.commands

import com.google.common.base.Joiner
import io.puharesource.mc.titlemanager.extensions.color
import io.puharesource.mc.titlemanager.pluginInstance

object CommandAnimations : TMSubCommand("animations",
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            val animations = pluginInstance.registeredAnimations.keys
            val joiner = Joiner.on(pluginInstance.config.getString("messages.command-animations.separator").orEmpty().color())

            sendConfigMessage("format",
                    Pair("count", animations.size.toString()),
                    Pair("animations", joiner.join(animations)))
        }})