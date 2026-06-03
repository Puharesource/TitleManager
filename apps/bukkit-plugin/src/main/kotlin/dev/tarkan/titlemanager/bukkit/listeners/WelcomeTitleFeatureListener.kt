package dev.tarkan.titlemanager.bukkit.listeners

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.configuration.WelcomeTitleConfiguration
import dev.tarkan.titlemanager.bukkit.configuration.WelcomeTitleConfigurationPart
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.storage.PlayerStorage
import dev.tarkan.titlemanager.time.Timing
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.java.KoinJavaComponent.inject

class WelcomeTitleFeatureListener(plugin: TitleManagerPlugin, coroutineScopeManager: CoroutineScopeManager) : TitleManagerListener(plugin, coroutineScopeManager) {
    private val playerContextManager: PlayerContextManager by inject(PlayerContextManager::class.java)
    private val welcomeTitleConfiguration: WelcomeTitleConfiguration by inject(WelcomeTitleConfiguration::class.java)
    private val playerStorage: PlayerStorage by inject(PlayerStorage::class.java)

    init {
        registerEventExecutor<PlayerJoinEvent> { event ->
            val config = if (event.player.hasPlayedBefore()) {
                welcomeTitleConfiguration.worlds.getOrDefault(event.player.world.name, welcomeTitleConfiguration)
            } else {
                welcomeTitleConfiguration.firstJoin
            }


            sendWelcomeMessage(event.player, config)
        }

        registerEventExecutor<PlayerChangedWorldEvent>(priority = EventPriority.MONITOR) { event ->
            val config = welcomeTitleConfiguration.worlds[event.player.world.name] ?: return@registerEventExecutor

            sendWelcomeMessage(event.player, config)
        }
    }

    private fun sendWelcomeMessage(player: Player, config: WelcomeTitleConfigurationPart) {
        if (!config.enabled) {
            return
        }

        val context = playerContextManager.getContext(player)
        val playerInfo = playerStorage.get(player)
        if (!playerInfo.isWelcomeTitleEnabled) {
            return
        }

        val timing = Timing((config.fadeIn * 50).toUInt(), (config.stay * 50).toUInt(), (config.fadeOut * 50).toUInt())

        context.sendTitleAndSubtitle(config.title, config.subtitle, timing, delay = config.delayMilliseconds)
    }
}