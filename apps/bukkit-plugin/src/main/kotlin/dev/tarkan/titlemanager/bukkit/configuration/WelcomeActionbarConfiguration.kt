package dev.tarkan.titlemanager.bukkit.configuration

import kotlinx.serialization.Serializable

@Serializable
abstract class WelcomeActionbarConfigurationPart(
    val enabled: Boolean = true,

    val delayMilliseconds: Long = 1000,

    val title: String = "Welcome to My Server"
) {
    init {
        require(delayMilliseconds >= 0) { "Welcome actionbar delay must not be negative" }
    }
}

@Serializable
data class WelcomeActionbarConfiguration(
    val firstJoin: FirstJoinActionbarConfiguration = FirstJoinActionbarConfiguration(),
    val worlds: Map<String, WorldWelcomeActionbarConfiguration> = mapOf("nether-world" to WorldWelcomeActionbarConfiguration())
) : WelcomeActionbarConfigurationPart()

@Serializable
class FirstJoinActionbarConfiguration : WelcomeActionbarConfigurationPart()

@Serializable
class WorldWelcomeActionbarConfiguration : WelcomeActionbarConfigurationPart()
