package dev.tarkan.titlemanager.bukkit.configuration

import kotlinx.serialization.Serializable

@Serializable
data class HooksConfiguration(
    val combatLogX: Boolean = true
)
