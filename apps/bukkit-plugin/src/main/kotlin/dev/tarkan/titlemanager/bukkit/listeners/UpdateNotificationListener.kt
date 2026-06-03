package dev.tarkan.titlemanager.bukkit.listeners

import dev.tarkan.titlemanager.bukkit.extensions.sendTitleManagerMessage
import de.comahe.i18n4k.Locale
import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.extensions.toComponent
import dev.tarkan.titlemanager.bukkit.localization.VersionCommandMessages
import dev.tarkan.titlemanager.bukkit.update.UpdateService
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.java.KoinJavaComponent.inject

class UpdateNotificationListener(plugin: TitleManagerPlugin, coroutineScopeManager: CoroutineScopeManager) : TitleManagerListener(plugin, coroutineScopeManager) {
    private val updateService: UpdateService by inject(UpdateService::class.java)

    init {
        registerEventExecutor<PlayerJoinEvent> { event ->
            if (!updateService.isUpdateAvailable || !event.player.hasPermission("titlemanager.update.notify")) {
                return@registerEventExecutor
            }

            val latestVersion = updateService.latestVersion ?: return@registerEventExecutor
            event.player.sendTitleManagerMessage(
                VersionCommandMessages.updateAvailable.toComponent(
                    latestVersion,
                    Locale.forLanguageTag(event.player.locale().toLanguageTag())
                )
            )
        }
    }
}
