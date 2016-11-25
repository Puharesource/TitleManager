package io.puharesource.mc.titlemanager.commands

import com.google.common.base.Joiner
import io.puharesource.mc.titlemanager.extensions.color
import io.puharesource.mc.titlemanager.pluginInstance

object CommandScripts : TMSubCommand("scripts",
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            val scripts = pluginInstance.registeredScripts
            val joiner = Joiner.on(pluginInstance.config.getString("messages.command-scripts.separator").orEmpty().color())

            sendConfigMessage("format",
                    Pair("count", scripts.size.toString()),
                    Pair("scripts", joiner.join(scripts)))
        }})