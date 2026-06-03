package dev.tarkan.titlemanager.bukkit.configuration

import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapabilities
import dev.tarkan.titlemanager.bukkit.diagnostics.RuntimeCapability

class RuntimeConfigurationValidator(
    private val vaultEconomyAvailable: Boolean = false,
    private val vaultPermissionGroupsAvailable: Boolean = false
) {
    private companion object {
        private val VARIABLE_PLACEHOLDER_REGEX = """%\{([^}:]+)(?::((?:[^}\\]|\\.)*))?\}""".toRegex()
        private val VAULT_ECONOMY_PLACEHOLDERS = setOf("balance", "money")
        private val VAULT_PERMISSION_PLACEHOLDERS = setOf("group", "group-name")
        private val BUNGEE_CORD_PLACEHOLDERS = setOf("bungeecord-online", "bungeecord-online-players", "server", "server-name")
        private const val MAX_SCOREBOARD_LINES = 15
    }

    fun validate(configurationManager: ConfigurationManager, runtimeCapabilities: RuntimeCapabilities) {
        val errors = buildList {
            requireCapability(
                runtimeCapabilities,
                RuntimeCapability.TITLES,
                configurationManager.advancedConfiguration.usingConfig && isWelcomeTitleConfigured(configurationManager.welcomeTitleConfiguration),
                "welcome titles"
            )
            requireCapability(
                runtimeCapabilities,
                RuntimeCapability.ACTIONBAR,
                configurationManager.advancedConfiguration.usingConfig && isWelcomeActionbarConfigured(configurationManager.welcomeActionbarConfiguration),
                "welcome actionbars"
            )
            requireCapability(
                runtimeCapabilities,
                RuntimeCapability.PLAYER_LIST,
                configurationManager.advancedConfiguration.usingConfig && isPlayerListConfigured(configurationManager.playerListConfiguration),
                "player-list headers and footers"
            )
            requireCapability(
                runtimeCapabilities,
                RuntimeCapability.SIDEBAR,
                configurationManager.advancedConfiguration.usingConfig && isScoreboardConfigured(configurationManager.scoreboardConfiguration),
                "scoreboards"
            )
            requireCapability(
                runtimeCapabilities,
                RuntimeCapability.TITLES,
                configurationManager.advancedConfiguration.usingConfig && isAnnouncerTitleConfigured(configurationManager.announcerConfiguration),
                "announcer titles"
            )
            requireCapability(
                runtimeCapabilities,
                RuntimeCapability.ACTIONBAR,
                configurationManager.advancedConfiguration.usingConfig && isAnnouncerActionbarConfigured(configurationManager.announcerConfiguration),
                "announcer actionbars"
            )

            addUnsupportedLegacyPlaceholderErrors(configurationManager)
            addScoreboardLineLimitErrors(configurationManager)
        }

        if (errors.isNotEmpty()) {
            throw ConfigurationException(
                "Runtime capabilities do not support enabled configuration:\n${errors.joinToString("\n")}"
            )
        }
    }

    private fun MutableList<String>.requireCapability(
        runtimeCapabilities: RuntimeCapabilities,
        capability: String,
        enabled: Boolean,
        featureName: String
    ) {
        if (!enabled || runtimeCapabilities.isAvailable(capability)) {
            return
        }

        val status = runtimeCapabilities.status(capability)
        val statusDescription = status?.let { "${it.status} (${it.detail})" } ?: "missing"
        add("$featureName requires capability '$capability', but it is $statusDescription.")
    }

    private fun MutableList<String>.addUnsupportedLegacyPlaceholderErrors(configurationManager: ConfigurationManager) {
        if (!configurationManager.advancedConfiguration.usingConfig) {
            return
        }

        val unsupportedPlaceholders = buildSet {
            if (!vaultEconomyAvailable) {
                addAll(VAULT_ECONOMY_PLACEHOLDERS)
            }
            if (!vaultPermissionGroupsAvailable) {
                addAll(VAULT_PERMISSION_PLACEHOLDERS)
            }
            if (!configurationManager.advancedConfiguration.usingBungeeCord) {
                addAll(BUNGEE_CORD_PLACEHOLDERS)
            }
        }

        val unsupportedUsages = configuredTextValues(configurationManager)
            .flatMap { (label, text) ->
                VARIABLE_PLACEHOLDER_REGEX.findAll(text).mapNotNull { match ->
                    val name = match.groups[1]?.value?.lowercase() ?: return@mapNotNull null
                    val data = match.groups[2]?.value
                    when {
                        name in unsupportedPlaceholders -> label to "%{$name}"
                        else -> null
                    }
                }
            }
            .distinct()

        unsupportedUsages.forEach { (label, placeholder) ->
            add("Configured $label uses unsupported legacy placeholder '$placeholder'. Remove it or replace it with a supported placeholder before starting.")
        }
    }

    private fun configuredTextValues(configurationManager: ConfigurationManager): List<Pair<String, String>> {
        val values = mutableListOf<Pair<String, String>>()

        fun addWelcomeTitle(label: String, configuration: WelcomeTitleConfigurationPart) {
            if (configuration.enabled) {
                values += "$label title" to configuration.title
                values += "$label subtitle" to configuration.subtitle
            }
        }

        fun addWelcomeActionbar(label: String, configuration: WelcomeActionbarConfigurationPart) {
            if (configuration.enabled) {
                values += "$label title" to configuration.title
            }
        }

        fun addPlayerList(label: String, configuration: PlayerListConfigurationPart) {
            if (configuration.enabled) {
                values += "$label header" to configuration.header
                values += "$label footer" to configuration.footer
            }
        }

        fun addScoreboard(label: String, configuration: ScoreboardConfigurationPart) {
            if (configuration.enabled) {
                values += "$label title" to configuration.title
                values += "$label content" to configuration.content
            }
        }

        val welcomeTitle = configurationManager.welcomeTitleConfiguration
        addWelcomeTitle("welcome-title", welcomeTitle)
        addWelcomeTitle("welcome-title firstJoin", welcomeTitle.firstJoin)
        welcomeTitle.worlds.forEach { (world, worldConfiguration) ->
            addWelcomeTitle("welcome-title world '$world'", worldConfiguration)
        }

        val welcomeActionbar = configurationManager.welcomeActionbarConfiguration
        addWelcomeActionbar("welcome-actionbar", welcomeActionbar)
        addWelcomeActionbar("welcome-actionbar firstJoin", welcomeActionbar.firstJoin)
        welcomeActionbar.worlds.forEach { (world, worldConfiguration) ->
            addWelcomeActionbar("welcome-actionbar world '$world'", worldConfiguration)
        }

        val playerList = configurationManager.playerListConfiguration
        addPlayerList("player-list", playerList)
        playerList.worlds.forEach { (world, worldConfiguration) ->
            addPlayerList("player-list world '$world'", worldConfiguration)
        }

        val scoreboard = configurationManager.scoreboardConfiguration
        addScoreboard("scoreboard", scoreboard)
        scoreboard.worlds.forEach { (world, worldConfiguration) ->
            addScoreboard("scoreboard world '$world'", worldConfiguration)
        }

        val announcer = configurationManager.announcerConfiguration
        if (announcer.enabled) {
            announcer.announcements.forEach { (name, announcement) ->
                announcement.titles.forEachIndexed { index, title ->
                    values += "announcer '$name' title ${index + 1}" to title
                }
                announcement.actionbar.forEachIndexed { index, actionbar ->
                    values += "announcer '$name' actionbar ${index + 1}" to actionbar
                }
            }
        }

        return values
    }

    private fun MutableList<String>.addScoreboardLineLimitErrors(configurationManager: ConfigurationManager) {
        if (!configurationManager.advancedConfiguration.usingConfig) {
            return
        }

        fun addScoreboard(label: String, configuration: ScoreboardConfigurationPart) {
            if (!configuration.enabled) {
                return
            }

            val lines = configuration.content.lines().size
            if (lines > MAX_SCOREBOARD_LINES) {
                add("Configured $label has $lines lines, but Bukkit sidebars support at most $MAX_SCOREBOARD_LINES lines.")
            }
        }

        val scoreboard = configurationManager.scoreboardConfiguration
        addScoreboard("scoreboard", scoreboard)
        scoreboard.worlds.forEach { (world, worldConfiguration) ->
            addScoreboard("scoreboard world '$world'", worldConfiguration)
        }
    }

    private fun isWelcomeTitleConfigured(configuration: WelcomeTitleConfiguration): Boolean {
        return configuration.enabled || configuration.firstJoin.enabled || configuration.worlds.values.any { it.enabled }
    }

    private fun isWelcomeActionbarConfigured(configuration: WelcomeActionbarConfiguration): Boolean {
        return configuration.enabled || configuration.firstJoin.enabled || configuration.worlds.values.any { it.enabled }
    }

    private fun isPlayerListConfigured(configuration: PlayerListConfiguration): Boolean {
        return configuration.enabled || configuration.worlds.values.any { it.enabled }
    }

    private fun isScoreboardConfigured(configuration: ScoreboardConfiguration): Boolean {
        return configuration.enabled || configuration.worlds.values.any { it.enabled }
    }

    private fun isAnnouncerTitleConfigured(configuration: AnnouncerConfiguration): Boolean {
        return configuration.enabled && configuration.announcements.values.any { it.titles.isNotEmpty() }
    }

    private fun isAnnouncerActionbarConfigured(configuration: AnnouncerConfiguration): Boolean {
        return configuration.enabled && configuration.announcements.values.any { it.actionbar.isNotEmpty() }
    }
}
