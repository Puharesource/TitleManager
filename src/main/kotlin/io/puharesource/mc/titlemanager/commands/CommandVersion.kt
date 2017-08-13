package io.puharesource.mc.titlemanager.commands

import io.puharesource.mc.titlemanager.pluginInstance
import io.puharesource.mc.titlemanager.web.UpdateChecker
import org.bukkit.ChatColor

object CommandVersion : TMSubCommand("version",
        cmdExecutor = { cmd, sender, args, parameters -> commandExecutor(cmd, sender, args, parameters) {
            sendConfigMessage("version", "version" to pluginInstance.description.version)

            if (UpdateChecker.isUpdateAvailable()) {
                sendMessage("${ChatColor.YELLOW}An update is available version: ${UpdateChecker.getLatestVersion()}")
            }
        }})