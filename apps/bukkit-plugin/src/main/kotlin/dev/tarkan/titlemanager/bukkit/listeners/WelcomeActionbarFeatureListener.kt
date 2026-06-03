package dev.tarkan.titlemanager.bukkit.listeners

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.configuration.WelcomeActionbarConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.WelcomeActionbarConfigurationPart
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.storage.PlayerStorage
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.java.KoinJavaComponent.inject

class WelcomeActionbarFeatureListener(plugin: TitleManagerPlugin, coroutineScopeManager: CoroutineScopeManager) : TitleManagerListener(plugin, coroutineScopeManager) {
    private val playerContextManager: PlayerContextManager by inject(PlayerContextManager::class.java)
    private val welcomeActionbarConfiguration: WelcomeActionbarConfiguration by inject(WelcomeActionbarConfiguration::class.java)
    private val playerStorage: PlayerStorage by inject(PlayerStorage::class.java)

    init {
        registerEventExecutor<PlayerJoinEvent> { event ->
            val config = if (event.player.hasPlayedBefore()) {
                welcomeActionbarConfiguration.worlds.getOrDefault(event.player.world.name, welcomeActionbarConfiguration)
            } else {
                welcomeActionbarConfiguration.firstJoin
            }

            sendWelcomeMessage(event.player, config)
        }

        registerEventExecutor<PlayerChangedWorldEvent>(priority = EventPriority.MONITOR) { event ->
            val config = welcomeActionbarConfiguration.worlds[event.player.world.name] ?: return@registerEventExecutor

            sendWelcomeMessage(event.player, config)
        }
    }

    private fun sendWelcomeMessage(player: Player, config: WelcomeActionbarConfigurationPart) {
        if (!config.enabled) {
            return
        }

        val context = playerContextManager.getContext(player)
        val playerInfo = playerStorage.get(player)
        if (!playerInfo.isWelcomeActionbarEnabled) {
            return
        }

        context.sendActionbar(config.title, delay = config.delayMilliseconds)
    }
}