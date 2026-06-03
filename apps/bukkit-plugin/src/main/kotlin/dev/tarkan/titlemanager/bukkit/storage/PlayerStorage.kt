package dev.tarkan.titlemanager.bukkit.storage

import dev.tarkan.titlemanager.bukkit.storage.schema.PlayerInfo
import org.bukkit.entity.Player
import java.io.Closeable
import java.util.*

interface PlayerStorage : Closeable {
    fun get(player: Player): PlayerInfo

    fun get(uuid: UUID): PlayerInfo

    suspend fun load(uuid: UUID): PlayerInfo

    fun unload(uuid: UUID)

    suspend fun setSidebarEnabled(uuid: UUID, enabled: Boolean)

    suspend fun setPlayerListEnabled(uuid: UUID, enabled: Boolean)

    suspend fun setWelcomeTitleEnabled(uuid: UUID, enabled: Boolean)

    suspend fun setWelcomeActionbarEnabled(uuid: UUID, enabled: Boolean)
}