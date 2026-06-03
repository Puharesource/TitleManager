package dev.tarkan.titlemanager.bukkit.storage.schema

import java.util.*

data class PlayerInfo(
    val uuid: UUID,
    val isSidebarEnabled: Boolean = true,
    val isPlayerListEnabled: Boolean = true,
    val isWelcomeTitleEnabled: Boolean = true,
    val isWelcomeActionbarEnabled: Boolean = true
)