package dev.tarkan.titlemanager.bukkit.configuration

import kotlinx.serialization.Serializable

@Serializable
data class AdvancedConfiguration(
    val configVersion: Int = 0,
    val threadPoolSize: Int = 4,
    val debug: Boolean = false,
    val usingConfig: Boolean = true,
    val usingBungeeCord: Boolean = false,
    val checkForUpdates: Boolean = false,
    val preventDuplicatePackets: Boolean = true,
    val databaseConnectionString: String = ""
)
