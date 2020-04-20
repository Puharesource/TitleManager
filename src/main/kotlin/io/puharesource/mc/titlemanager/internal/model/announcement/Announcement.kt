package io.puharesource.mc.titlemanager.internal.model.announcement

import org.bukkit.configuration.ConfigurationSection

data class Announcement(
    val interval: Int,
    val fadeIn: Int,
    val stay: Int,
    val fadeOut: Int,
    val titles: List<String>,
    val actionbarTitles: List<String>
) {
    companion object {
        fun fromConfig(configurationSection: ConfigurationSection): Announcement {
            return Announcement(
                    interval = configurationSection.getInt("interval", 60),
                    fadeIn = configurationSection.getInt("timings.fade-in", 20),
                    stay = configurationSection.getInt("timings.stay", 40),
                    fadeOut = configurationSection.getInt("timings.fade-out", 20),
                    titles = configurationSection.getStringList("titles"),
                    actionbarTitles = configurationSection.getStringList("actionbar")
            )
        }
    }

    val size: Int
        get() = titles.size.coerceAtLeast(actionbarTitles.size)

    val isEmpty: Boolean
        get() = size == 0
}
