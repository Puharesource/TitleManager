package dev.tarkan.titlemanager.bukkit.api

import java.util.UUID

interface TitleManagerSession : AutoCloseable {
    val playerUniqueId: UUID
    val type: TitleManagerSessionType
    val isClosed: Boolean

    override fun close()
}

enum class TitleManagerSessionType {
    TITLE,
    ACTIONBAR,
    PLAYER_LIST,
    SIDEBAR
}
