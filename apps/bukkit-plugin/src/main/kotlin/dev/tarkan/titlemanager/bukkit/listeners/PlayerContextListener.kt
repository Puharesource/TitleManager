package dev.tarkan.titlemanager.bukkit.listeners

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import dev.tarkan.titlemanager.bukkit.concurrency.CoroutineScopeManager
import dev.tarkan.titlemanager.bukkit.context.PlayerContextManager
import dev.tarkan.titlemanager.bukkit.storage.PlayerStorage
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.java.KoinJavaComponent.inject

class PlayerContextListener(plugin: TitleManagerPlugin, coroutineScopeManager: CoroutineScopeManager) : TitleManagerListener(plugin, coroutineScopeManager) {
    private val playerContextManager: PlayerContextManager by inject(PlayerContextManager::class.java)
    private val playerStorage: PlayerStorage by inject(PlayerStorage::class.java)

    init {
        registerEventExecutor<AsyncPlayerPreLoginEvent>(priority = EventPriority.HIGHEST) { event ->
            playerStorage.load(event.uniqueId)
        }

        registerEventExecutor<PlayerConnectionCloseEvent> { event ->
            playerStorage.unload(event.playerUniqueId)
        }

        registerEventExecutor<PlayerQuitEvent> { event ->
            playerContextManager.removeContext(event.player)
        }
    }
}