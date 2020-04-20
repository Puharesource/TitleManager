package io.puharesource.mc.titlemanager.internal.config

import io.puharesource.mc.titlemanager.internal.extensions.color
import io.puharesource.mc.titlemanager.internal.pluginInstance
import org.bukkit.configuration.ConfigurationSection
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale

interface TMConfig {
    val section: ConfigurationSection

    fun <T> value(path: String) = section[path] as T
    fun <F, T> value(path: String, transform: (F) -> T) = transform(section[path] as F)

    fun stringList(path: String): List<String> {
        return section.getStringList(path)
    }

    fun multilineString(path: String): String {
        val value = section[path]

        if (value is String) {
            return value
        } else if (value is List<*>) {
            return stringList(path).joinToString(separator = "\n") { if (it.isEmpty()) " " else it }
        }

        return ""
    }
}

data class TMConfigMain(override val section: ConfigurationSection) : TMConfig {
    val configVersion: Int = value("config-version")
    val debug: Boolean = value("debug")
    val usingConfig: Boolean = value("using-config")
    val usingBungeecord: Boolean = value("using-bungeecord")
    val legacyClientSupport: Boolean = value("legacy-client-support")
    val checkForUpdates: Boolean = value("check-for-updates")
    val locale: Locale = value<String, Locale>("locale") { Locale.forLanguageTag(it) }

    val playerList by lazy { TMConfigPlayerList(section.getConfigurationSection("player-list")!!) }
    val welcomeTitle by lazy { TMConfigWelcomeTitle(section.getConfigurationSection("welcome-title")!!) }
    val welcomeActionbar by lazy { TMConfigWelcomeActionbar(section.getConfigurationSection("welcome-actionbar")!!) }
    val placeholders by lazy { TMConfigPlaceholders(section.getConfigurationSection("placeholders")!!) }
    val scoreboard by lazy { TMConfigScoreboard(section.getConfigurationSection("scoreboard")!!) }
    val announcer by lazy { TMConfigAnnouncer(section.getConfigurationSection("announcer")!!) }
    val bandwidth by lazy { TMConfigBandwidth(section.getConfigurationSection("bandwidth")!!) }
    val messages by lazy { TMConfigMessages(section.getConfigurationSection("messages")!!) }
}

data class TMConfigPlayerList(override val section: ConfigurationSection) : TMConfig {
    val enabled: Boolean = value("enabled")

    val header: String = multilineString("header").color()
    val footer: String = multilineString("footer").color()
}

data class TMConfigWelcomeTitle(override val section: ConfigurationSection) : TMConfig {
    val enabled: Boolean = value("enabled")

    val title: String = value<String>("title").color()
    val subtitle: String = value<String>("subtitle").color()
    val fadeIn: Int = value("fade-in")
    val stay: Int = value("stay")
    val fadeOut: Int = value("fade-out")

    val firstJoin by lazy { TMConfigWelcomeTitleFirstJoin(section.getConfigurationSection("first-join")!!) }
}

data class TMConfigWelcomeTitleFirstJoin(override val section: ConfigurationSection) : TMConfig {
    val title: String = value<String>("title").color()
    val subtitle: String = value<String>("subtitle").color()
}

data class TMConfigWelcomeActionbar(override val section: ConfigurationSection) : TMConfig {
    val enabled: Boolean = value("enabled")

    val title: String = value<String>("title").color()
    val firstJoin: String = value<String>("first-join").color()
}

data class TMConfigPlaceholders(override val section: ConfigurationSection) : TMConfig {
    val numberFormat = TMConfigPlaceholdersNumberFormat(section.getConfigurationSection("number-format")!!)
    val dateFormat: SimpleDateFormat = value<String, SimpleDateFormat>("date-format") { SimpleDateFormat(it, pluginInstance.tmConfig.locale) }
}

data class TMConfigPlaceholdersNumberFormat(override val section: ConfigurationSection) : TMConfig {
    val enabled: Boolean = value("enabled")
    val format: DecimalFormat = value<String, DecimalFormat>("format") { format ->
        val symbols = DecimalFormatSymbols(pluginInstance.tmConfig.locale)

        DecimalFormat(format, symbols)
    }
}

data class TMConfigScoreboard(override val section: ConfigurationSection) : TMConfig {
    val enabled: Boolean = value("enabled")
    val title: String = value<String>("title").color()
    val lines: List<String> = stringList("lines").take(15).map { it.color() }
}

data class TMConfigAnnouncer(override val section: ConfigurationSection) : TMConfig {
    val enabled: Boolean = value("enabled")
    val announcements: List<ConfigurationSection> = section.getConfigurationSection("announcements")!!.getKeys(false).map { section.getConfigurationSection("announcements.$it")!! }
}

data class TMConfigBandwidth(override val section: ConfigurationSection) : TMConfig {
    val preventDuplicatePackets: Boolean = value("prevent-duplicate-packets")
    val playerListMsPerTick: Long = value("player-list-ms-per-tick")
    val scoreboardMsPerTick: Long = value("scoreboard-ms-per-tick")
}

class TMConfigMessages(override val section: ConfigurationSection) : TMConfig
