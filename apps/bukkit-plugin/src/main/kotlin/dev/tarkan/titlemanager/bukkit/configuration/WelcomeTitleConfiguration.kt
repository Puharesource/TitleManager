package dev.tarkan.titlemanager.bukkit.configuration

import kotlinx.serialization.Serializable

@Serializable
abstract class WelcomeTitleConfigurationPart(
    val enabled: Boolean = true,

    val delayMilliseconds: Long = 1000,

    val title: String = "Welcome to My Server",
    val subtitle: String = "Hope you enjoy your stay",

    val fadeIn: Int = 20,
    val stay: Int = 40,
    val fadeOut: Int = 20,
) {
    init {
        require(delayMilliseconds >= 0) { "Welcome title delay must not be negative" }
        require(fadeIn >= 0) { "Welcome title fade-in must not be negative" }
        require(stay >= 0) { "Welcome title stay must not be negative" }
        require(fadeOut >= 0) { "Welcome title fade-out must not be negative" }
    }
}

@Serializable
data class WelcomeTitleConfiguration(
    val firstJoin: FirstJoinTitleConfiguration = FirstJoinTitleConfiguration(),
    val worlds: Map<String, WorldWelcomeTitleConfiguration> = mapOf("nether-world" to WorldWelcomeTitleConfiguration())
) : WelcomeTitleConfigurationPart()

@Serializable
class FirstJoinTitleConfiguration : WelcomeTitleConfigurationPart()

@Serializable
class WorldWelcomeTitleConfiguration : WelcomeTitleConfigurationPart()
