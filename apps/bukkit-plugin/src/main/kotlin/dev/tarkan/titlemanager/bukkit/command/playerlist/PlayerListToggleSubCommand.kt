package dev.tarkan.titlemanager.bukkit.command.playerlist

import dev.tarkan.titlemanager.bukkit.extensions.sendTitleManagerMessage
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.extensions.toggleText
import dev.tarkan.titlemanager.bukkit.localization.PlayerListToggleSubCommandMessages
import dev.tarkan.titlemanager.bukkit.storage.PlayerStorage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PlayerListToggleSubCommand(private val playerContextManager: PlayerContextManager, private val playerStorage: PlayerStorage) : TitleManagerSubCommand(
    "toggle",
    description = PlayerListToggleSubCommandMessages.description,
    permission = "titlemanager.command.playerlist.toggle",
    playerOnly = true
) {
    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        val playerInfo = playerStorage.get(sender as Player)

        val enablePlayerList = !playerInfo.isPlayerListEnabled

        val playerContext = playerContextManager.getContext(sender)

        if (enablePlayerList) {
            playerContext.requirePlayerListCapability()
        }

        playerStorage.setPlayerListEnabled(sender.uniqueId, enablePlayerList)

        if (enablePlayerList) {
            playerContext.setConfigPlayerList()
        } else {
            playerContext.cancelPlayerListJob()
        }

        if (!parameters.isSilent) {
            sender.sendTitleManagerMessage(PlayerListToggleSubCommandMessages.toggle.toComponent(enablePlayerList.toggleText, context.locale))
        }
    }
}
