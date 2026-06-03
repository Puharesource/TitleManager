package dev.tarkan.titlemanager.bukkit.command.subcommands

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.command.CommandContext
import dev.tarkan.titlemanager.bukkit.command.CommandParameters
import dev.tarkan.titlemanager.bukkit.command.TitleManagerSubCommand
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.localization.VersionCommandMessages
import dev.tarkan.titlemanager.bukkit.update.UpdateService
import org.bukkit.command.CommandSender

class VersionSubCommand(
    private val plugin: TitleManagerPlugin,
    private val updateService: UpdateService
) : TitleManagerSubCommand("version", "v", description = VersionCommandMessages.description, permission = "titlemanager.command.version") {

    override suspend fun executeCommand(sender: CommandSender, args: Array<out String>, parameters: CommandParameters, context: CommandContext) {
        val versionString = "v${plugin.pluginVersion}"

        sender.sendIfNotSilent(parameters, VersionCommandMessages.version.toComponent(versionString, context.locale))
        if (!parameters.isSilent && updateService.isUpdateAvailable) {
            sender.sendMessage(VersionCommandMessages.updateAvailable.toComponent(updateService.latestVersion.orEmpty(), context.locale))
        }
    }
}