package dev.tarkan.titlemanager.bukkit.configuration

import kotlinx.serialization.Serializable

@Serializable
data class AnnouncerConfiguration(
    val enabled: Boolean = false,
    val announcements: Map<String, AnnouncerEntryConfiguration> = emptyMap()
)

@Serializable
data class AnnouncerEntryConfiguration(
    val interval: Long = 60,
    val timings: AnnouncerTimingConfiguration = AnnouncerTimingConfiguration(),
    val titles: List<String> = emptyList(),
    val actionbar: List<String> = emptyList()
) {
    init {
        require(interval > 0) { "Announcer interval must be greater than zero seconds" }
    }
}

@Serializable
data class AnnouncerTimingConfiguration(
    val fadeIn: Int = 20,
    val stay: Int = 40,
    val fadeOut: Int = 20
) {
    init {
        require(fadeIn >= 0) { "Announcer fade-in must not be negative" }
        require(stay >= 0) { "Announcer stay must not be negative" }
        require(fadeOut >= 0) { "Announcer fade-out must not be negative" }
    }
}
