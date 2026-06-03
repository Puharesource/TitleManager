package dev.tarkan.titlemanager.bukkit.context

import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import org.bukkit.entity.Player
import org.koin.java.KoinJavaComponent.inject
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap

class PlayerContextManager : Closeable {
    private val plugin: TitleManagerPlugin by inject(TitleManagerPlugin::class.java)

    private val contexts = ConcurrentHashMap<Player, PlayerContext>()
    internal val activeContextCount: Int
        get() = contexts.size

    fun getContext(player: Player): PlayerContext = contexts.getOrPut(player) {
        PlayerContext(player, plugin)
    }

    fun removeContext(player: Player) {
        contexts.remove(player)?.close()
    }

    override fun close() {
        contexts.values.forEach(PlayerContext::close)
        contexts.clear()
    }
}