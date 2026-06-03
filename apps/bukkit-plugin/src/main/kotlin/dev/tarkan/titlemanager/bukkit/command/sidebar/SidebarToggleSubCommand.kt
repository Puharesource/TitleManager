package dev.tarkan.titlemanager.bukkit.command.sidebar

import dev.tarkan.titlemanager.bukkit.extensions.sendTitleManagerMessage
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.extensions.toggleText
import dev.tarkan.titlemanager.bukkit.localization.SidebarCommandMessages
import dev.tarkan.titlemanager.bukkit.localization.SidebarToggleCommandMessages
import dev.tarkan.titlemanager.bukkit.storage.PlayerStorage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SidebarToggleSubCommand(private val playerContextManager: PlayerContextManager, private val playerStorage: PlayerStorage) : TitleManagerSubCommand(
    "toggle",
    description = SidebarCommandMessages.description,
    permission = "titlemanager.command.sidebar.toggle",
    playerOnly = true
) {
    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        val playerInfo = playerStorage.get(sender as Player)

        val enableSidebar = !playerInfo.isSidebarEnabled

        val playerContext = playerContextManager.getContext(sender)

        if (enableSidebar) {
            playerContext.requireSidebarCapability()
        }

        playerStorage.setSidebarEnabled(sender.uniqueId, enableSidebar)

        if (enableSidebar) {
            playerContext.setConfigScoreboard()
        } else {
            playerContext.removeScoreboard()
        }

        if (!parameters.isSilent) {
            sender.sendTitleManagerMessage(SidebarToggleCommandMessages.toggle.toComponent(enableSidebar.toggleText, context.locale))
        }
    }
}
