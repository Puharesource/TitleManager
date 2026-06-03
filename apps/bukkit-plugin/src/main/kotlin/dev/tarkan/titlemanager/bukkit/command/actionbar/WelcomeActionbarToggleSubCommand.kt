package dev.tarkan.titlemanager.bukkit.command.actionbar

import dev.tarkan.titlemanager.bukkit.extensions.sendTitleManagerMessage
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.extensions.toggleText
import dev.tarkan.titlemanager.bukkit.localization.ActionbarWelcomeToggleSubCommandMessages
import dev.tarkan.titlemanager.bukkit.storage.PlayerStorage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WelcomeActionbarToggleSubCommand(private val playerStorage: PlayerStorage) : TitleManagerSubCommand(
    "toggle",
    description = ActionbarWelcomeToggleSubCommandMessages.description,
    permission = "titlemanager.command.actionbar.toggle",
    playerOnly = true
) {
    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        val playerInfo = playerStorage.get(sender as Player)

        val enableSidebar = !playerInfo.isWelcomeActionbarEnabled

        playerStorage.setWelcomeActionbarEnabled(sender.uniqueId, enableSidebar)

        if (!parameters.isSilent) {
            sender.sendTitleManagerMessage(ActionbarWelcomeToggleSubCommandMessages.toggle.toComponent(enableSidebar.toggleText, context.locale))
        }
    }
}
