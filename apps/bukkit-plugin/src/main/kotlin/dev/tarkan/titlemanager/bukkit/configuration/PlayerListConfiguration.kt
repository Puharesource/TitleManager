package dev.tarkan.titlemanager.bukkit.configuration

import kotlinx.serialization.Serializable

@Serializable
abstract class PlayerListConfigurationPart(
    val enabled: Boolean = true,
    val updateIntervalMilliseconds: Long = 50,
    val header: String = """
        
        ${"\${shine:[0;2;0][0;25;0][0;25;0][&3;&b]My Server}"}
        
    """.trimIndent(),
    val footer: String = """
        
        &7World time: &b%{12h-world-time}
        &7Server time: &b%{server-time}
        
        ${"\${right-to-left}"} &b%{online}&7/&b%{max} &7Online Players ${"\${left-to-right}"}
    """.trimIndent()
) {
    init {
        require(updateIntervalMilliseconds > 0) { "Player-list update interval must be greater than zero milliseconds" }
    }
}

@Serializable
class PlayerListConfiguration(
    val worlds: Map<String, WorldPlayerListConfiguration> = mapOf("world_nether" to WorldPlayerListConfiguration())
) : PlayerListConfigurationPart()

@Serializable
class WorldPlayerListConfiguration : PlayerListConfigurationPart()