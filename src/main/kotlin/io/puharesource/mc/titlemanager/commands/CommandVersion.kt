package io.puharesource.mc.titlemanager.commands

import io.puharesource.mc.titlemanager.pluginInstance

object CommandVersion : TMSubCommand("version",
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            sendConfigMessage("version", Pair("version", pluginInstance.description.version))
        }})