package dev.tarkan.titlemanager.bukkit.command.title

import dev.tarkan.titlemanager.bukkit.extensions.sendTitleManagerMessage
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.extensions.toggleText
import dev.tarkan.titlemanager.bukkit.localization.TitleWelcomeToggleSubCommandMessages
import dev.tarkan.titlemanager.bukkit.storage.PlayerStorage
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WelcomeTitleToggleSubCommand(private val playerStorage: PlayerStorage) : TitleManagerSubCommand(
    "toggle",
    description = TitleWelcomeToggleSubCommandMessages.description,
    permission = "titlemanager.command.title.toggle",
    playerOnly = true
) {
    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        val playerInfo = playerStorage.get(sender as Player)

        val enableSidebar = !playerInfo.isWelcomeTitleEnabled

        playerStorage.setWelcomeTitleEnabled(sender.uniqueId, enableSidebar)

        if (!parameters.isSilent) {
            sender.sendTitleManagerMessage(TitleWelcomeToggleSubCommandMessages.toggle.toComponent(enableSidebar.toggleText, context.locale))
        }
    }
}
