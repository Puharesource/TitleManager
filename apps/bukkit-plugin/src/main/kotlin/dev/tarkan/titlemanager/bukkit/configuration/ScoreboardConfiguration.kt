package dev.tarkan.titlemanager.bukkit.configuration

import kotlinx.serialization.Serializable

@Serializable
abstract class ScoreboardConfigurationPart(
    val enabled: Boolean = true,
    val updateIntervalMilliseconds: Long = 50,
    val title: String = "\${shine:[0;2;0][0;25;0][0;25;0][&3;&b]My Server}",
    val content: String = """
        &b&m----------------------------------
        &b> &3&lPlayer Name:
        &b%{name}
        &r
        &b> &3&lOnline:
        &b%{online} players
        &r&r
        &b> &3&lServer Time:
        &b%{server-time}
        &b&m----------------------------------&r
    """.trimIndent()
) {
    init {
        require(updateIntervalMilliseconds > 0) { "Scoreboard update interval must be greater than zero milliseconds" }
    }
}

@Serializable
class ScoreboardConfiguration(
    val worlds: Map<String, WorldScoreboardConfiguration> = mapOf("world_nether" to WorldScoreboardConfiguration())
) : ScoreboardConfigurationPart()

@Serializable
class WorldScoreboardConfiguration : ScoreboardConfigurationPart()