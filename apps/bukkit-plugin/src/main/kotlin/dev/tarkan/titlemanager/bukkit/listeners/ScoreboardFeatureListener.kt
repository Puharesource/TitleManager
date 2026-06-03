package dev.tarkan.titlemanager.bukkit.listeners

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.java.KoinJavaComponent.inject

class ScoreboardFeatureListener(plugin: TitleManagerPlugin, coroutineScopeManager: CoroutineScopeManager) : TitleManagerListener(plugin, coroutineScopeManager) {
    private val playerContextManager: PlayerContextManager by inject(PlayerContextManager::class.java)

    init {
        registerEventExecutor<PlayerJoinEvent> { event ->
            playerContextManager.getContext(event.player).setConfigScoreboard()
        }

        registerEventExecutor<PlayerChangedWorldEvent>(priority = EventPriority.MONITOR) { event ->
            playerContextManager.getContext(event.player).setConfigScoreboard()
        }
    }
}