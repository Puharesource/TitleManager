package dev.tarkan.titlemanager.bukkit.configuration

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.*

class LegacyConfigurationMigrator(
    private val resourceProvider: ConfigurationResourceProvider,
    private val dataFolder: Path
) {
    fun migrateIfNeeded(): LegacyConfigurationMigrationResult {
        try {
            return migrate()
        } catch (exception: ConfigurationException) {
            throw exception
        } catch (exception: Exception) {
            throw ConfigurationMigrationException(exception)
        }
    }

    private fun migrate(): LegacyConfigurationMigrationResult {
        val legacyConfigFile = dataFolder.resolve(ConfigurationFiles.LEGACY_CONFIGURATION_FILE_NAME)

        if (!legacyConfigFile.exists() || legacyConfigFile.fileSize() == 0L) {
            return LegacyConfigurationMigrationResult.NoMigrationNeeded
        }

        val missingSplitConfigFiles = ConfigurationManager.CONFIGURATION_FILE_NAMES.filter { fileName ->
            val targetFile = dataFolder.resolve(fileName)

            !targetFile.exists() || targetFile.fileSize() == 0L
        }

        if (missingSplitConfigFiles.isEmpty()) {
            return LegacyConfigurationMigrationResult.NoMigrationNeeded
        }

        val backupFile = createBackup(legacyConfigFile)
        val legacyRoot = legacyConfigFile.inputStream().use { inputStream ->
            loadYaml(inputStream.reader(Charsets.UTF_8).readText())
        }
        rejectEnabledUnsupportedFeatures(legacyRoot)

        val migratedFiles = buildMigratedFiles(legacyRoot).filterKeys { it in missingSplitConfigFiles }
        validateMigratedFiles(migratedFiles)

        for ((fileName, contents) in migratedFiles) {
            writeAtomically(dataFolder.resolve(fileName), contents)
        }

        return LegacyConfigurationMigrationResult.Migrated(backupFile)
    }

    private fun createBackup(legacyConfigFile: Path): Path {
        var backupFile = dataFolder.resolve(ConfigurationFiles.legacyBackupFileName())
        var index = 1

        while (backupFile.exists()) {
            backupFile = dataFolder.resolve(ConfigurationFiles.legacyBackupFileName(index))
            index++
        }

        Files.copy(legacyConfigFile, backupFile, StandardCopyOption.COPY_ATTRIBUTES)

        return backupFile
    }

    private fun loadYaml(contents: String): Map<String, Any?> {
        val loaded = Load(LoadSettings.builder().build()).loadFromString(contents)

        require(loaded is Map<*, *>) { "Legacy ${ConfigurationFiles.LEGACY_CONFIGURATION_FILE_NAME} must contain a YAML object at the root." }

        return loaded.entries.associate { (key, value) ->
            key.toString() to value
        }
    }

    private fun rejectEnabledUnsupportedFeatures(legacyRoot: Map<String, Any?>) {
        require(!legacyRoot.boolean("legacy-client-support", false)) {
            "Legacy 1.7 client actionbar support is not supported in TitleManager Next. Disable legacy-client-support before migrating."
        }
    }

    private fun buildMigratedFiles(legacyRoot: Map<String, Any?>): Map<String, String> = mapOf(
        ConfigurationManager.ADVANCED_CONFIGURATION_FILE_NAME to buildAdvancedConfiguration(legacyRoot),
        ConfigurationManager.PLAYER_LIST_CONFIGURATION_FILE_NAME to buildPlayerListConfiguration(legacyRoot),
        ConfigurationManager.PLACEHOLDER_CONFIGURATION_FILE_NAME to buildPlaceholderConfiguration(legacyRoot),
        ConfigurationManager.WELCOME_TITLE_CONFIGURATION_FILE_NAME to buildWelcomeTitleConfiguration(legacyRoot),
        ConfigurationManager.WELCOME_ACTIONBAR_CONFIGURATION_FILE_NAME to buildWelcomeActionbarConfiguration(legacyRoot),
        ConfigurationManager.SCOREBOARD_CONFIGURATION_FILE_NAME to buildScoreboardConfiguration(legacyRoot),
        ConfigurationManager.GRADIENTS_CONFIGURATION_FILE_NAME to readDefaultConfiguration(ConfigurationManager.GRADIENTS_CONFIGURATION_FILE_NAME),
        ConfigurationManager.ANNOUNCER_CONFIGURATION_FILE_NAME to buildAnnouncerConfiguration(legacyRoot),
        ConfigurationManager.HOOKS_CONFIGURATION_FILE_NAME to buildHooksConfiguration(legacyRoot)
    )

    private fun buildAdvancedConfiguration(legacyRoot: Map<String, Any?>): String {
        val updaterSection = legacyRoot.map("updater")
        val bandwidthSection = legacyRoot.map("bandwidth")

        return """
            configVersion: ${legacyRoot.int("config-version", 0)}
            threadPoolSize: 4
            debug: ${legacyRoot.boolean("debug", false)}
            usingConfig: ${legacyRoot.booleanAny(listOf("using-config", "usingConfig"), true)}
            usingBungeeCord: ${legacyRoot.boolean("using-bungeecord", false)}
            checkForUpdates: ${legacyRoot.boolean("check-for-updates", updaterSection.boolean("check-automatically", false))}
            preventDuplicatePackets: ${bandwidthSection.boolean("prevent-duplicate-packets", true)}
            databaseConnectionString: ""
        """.trimIndent()
    }

    private fun buildPlayerListConfiguration(legacyRoot: Map<String, Any?>): String {
        val section = legacyRoot.mapAny("player-list", "tabmenu")
        val bandwidthSection = legacyRoot.map("bandwidth")

        return """
            enabled: ${section.boolean("enabled", true)}
            updateIntervalMilliseconds: ${bandwidthSection.long("player-list-ms-per-tick", 50)}
            header: ${quoted(section.multilineString("header", ""))}
            footer: ${quoted(section.multilineString("footer", ""))}
            worlds: {}
        """.trimIndent()
    }

    private fun buildPlaceholderConfiguration(legacyRoot: Map<String, Any?>): String {
        val section = legacyRoot.map("placeholders")
        val numberFormatSection = section.mapAny("number-format").ifEmpty { legacyRoot.map("number-format") }
        val legacyDateFormatSection = legacyRoot.map("date-format")

        return """
            locale: ${quoted(section.string("locale", legacyRoot.string("locale", "en-US")))}
            numberFormat:
              enabled: ${numberFormatSection.boolean("enabled", true)}
              format: ${quoted(numberFormatSection.string("format", "#,###.##"))}
            dateFormat: ${quoted(section.string("date-format", legacyDateFormatSection.string("format", "EEE, dd MMM yyyy HH:mm:ss z")))}
            aliases: {}
        """.trimIndent()
    }

    private fun buildWelcomeTitleConfiguration(legacyRoot: Map<String, Any?>): String {
        val section = legacyRoot.mapAny("welcome-title", "welcome_message")
        val firstJoinSection = section.map("first-join")
        val fadeIn = section.intAny(listOf("fade-in", "fadeIn"), 20)
        val fadeOut = section.intAny(listOf("fade-out", "fadeOut"), 20)

        return """
            enabled: ${section.boolean("enabled", true)}
            delayMilliseconds: ${section.ticksAsMilliseconds("delay", 20)}
            title: ${quoted(section.string("title", "Welcome to My Server"))}
            subtitle: ${quoted(section.string("subtitle", "Hope you enjoy your stay"))}
            fadeIn: $fadeIn
            stay: ${section.int("stay", 40)}
            fadeOut: $fadeOut
            firstJoin:
              enabled: true
              delayMilliseconds: ${section.ticksAsMilliseconds("delay", 20)}
              title: ${quoted(firstJoinSection.string("title", section.string("title", "Welcome to My Server")))}
              subtitle: ${quoted(firstJoinSection.string("subtitle", section.string("subtitle", "Hope you enjoy your stay")))}
              fadeIn: $fadeIn
              stay: ${section.int("stay", 40)}
              fadeOut: $fadeOut
            worlds: {}
        """.trimIndent()
    }

    private fun buildWelcomeActionbarConfiguration(legacyRoot: Map<String, Any?>): String {
        val section = legacyRoot.mapAny("welcome-actionbar", "actionbar-welcome")
        val title = section.stringAny(listOf("title", "message"), "Welcome to My Server")
        val firstJoinTitle = when (val firstJoinValue = section["first-join"]) {
            is Map<*, *> -> firstJoinValue.entries
                .associate { (key, value) -> key.toString() to value }
                .string("message", title)
            null -> title
            else -> firstJoinValue.toString()
        }

        return """
            enabled: ${section.boolean("enabled", true)}
            delayMilliseconds: ${section.ticksAsMilliseconds("delay", 20)}
            title: ${quoted(title)}
            firstJoin:
              enabled: true
              delayMilliseconds: ${section.ticksAsMilliseconds("delay", 20)}
              title: ${quoted(firstJoinTitle)}
            worlds: {}
        """.trimIndent()
    }

    private fun buildScoreboardConfiguration(legacyRoot: Map<String, Any?>): String {
        val section = legacyRoot.map("scoreboard")
        val bandwidthSection = legacyRoot.map("bandwidth")
        val disabledWorlds = section.stringList("disabled-worlds")

        return buildString {
            appendLine("enabled: ${section.boolean("enabled", true)}")
            appendLine("updateIntervalMilliseconds: ${bandwidthSection.long("scoreboard-ms-per-tick", 50)}")
            appendLine("title: ${quoted(section.string("title", "\${shine:[0;2;0][0;25;0][0;25;0][&3;&b]My Server}"))}")
            appendLine("content: ${quoted(section.multilineString("lines", "").limitScoreboardLines())}")
            append("worlds: ${disabledWorlds.disabledWorldsMap()}")
        }
    }

    private fun buildAnnouncerConfiguration(legacyRoot: Map<String, Any?>): String {
        val section = legacyRoot.map("announcer")
        val announcements = section.map("announcements")

        return buildString {
            appendLine("enabled: ${section.boolean("enabled", false)}")
            if (announcements.isEmpty()) {
                append("announcements: {}")
                return@buildString
            }

            appendLine("announcements:")
            announcements.forEach { (name, rawAnnouncement) ->
                val announcement = rawAnnouncement.asStringMap()
                val timings = announcement.map("timings")
                appendLine("  ${quoted(name)}:")
                appendLine("    interval: ${announcement.int("interval", 60)}")
                appendLine("    timings:")
                appendLine("      fadeIn: ${timings.intAny(listOf("fade-in", "fadeIn"), 20)}")
                appendLine("      stay: ${timings.int("stay", 40)}")
                appendLine("      fadeOut: ${timings.intAny(listOf("fade-out", "fadeOut"), 20)}")
                appendStringList("    titles", announcement.stringList("titles").map { it.toTypedLineBreak() })
                appendStringList("    actionbar", announcement.stringList("actionbar"))
            }
        }.trimEnd()
    }

    private fun buildHooksConfiguration(legacyRoot: Map<String, Any?>): String {
        val section = legacyRoot.map("hooks")

        return """
            combatLogX: ${section.boolean("combatlogx", true)}
        """.trimIndent()
    }

    private fun readDefaultConfiguration(fileName: String): String {
        return resourceProvider.readDefaultConfiguration(fileName)
    }

    private fun validateMigratedFiles(files: Map<String, String>) {
        for ((fileName, contents) in files) {
            when (fileName) {
                ConfigurationManager.ADVANCED_CONFIGURATION_FILE_NAME -> Yaml.default.decodeFromString<AdvancedConfiguration>(contents)
                ConfigurationManager.PLAYER_LIST_CONFIGURATION_FILE_NAME -> Yaml.default.decodeFromString<PlayerListConfiguration>(contents)
                ConfigurationManager.PLACEHOLDER_CONFIGURATION_FILE_NAME -> Yaml.default.decodeFromString<PlaceholderConfiguration>(contents)
                ConfigurationManager.WELCOME_TITLE_CONFIGURATION_FILE_NAME -> Yaml.default.decodeFromString<WelcomeTitleConfiguration>(contents)
                ConfigurationManager.WELCOME_ACTIONBAR_CONFIGURATION_FILE_NAME -> Yaml.default.decodeFromString<WelcomeActionbarConfiguration>(contents)
                ConfigurationManager.SCOREBOARD_CONFIGURATION_FILE_NAME -> Yaml.default.decodeFromString<ScoreboardConfiguration>(contents)
                ConfigurationManager.GRADIENTS_CONFIGURATION_FILE_NAME -> Yaml.default.decodeFromString<GradientsConfiguration>(contents)
                ConfigurationManager.ANNOUNCER_CONFIGURATION_FILE_NAME -> Yaml.default.decodeFromString<AnnouncerConfiguration>(contents)
                ConfigurationManager.HOOKS_CONFIGURATION_FILE_NAME -> Yaml.default.decodeFromString<HooksConfiguration>(contents)
            }
        }
    }

    private fun writeAtomically(targetFile: Path, contents: String) {
        if (!dataFolder.exists()) {
            dataFolder.createDirectories()
        }

        val tempFile = Files.createTempFile(dataFolder, "${targetFile.name}.", ".tmp")

        try {
            tempFile.writeText(contents)

            try {
                Files.move(tempFile, targetFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.deleteExisting()
            }
        }
    }

    private fun Map<String, Any?>.map(key: String): Map<String, Any?> {
        val value = this[key]

        return value.asStringMap()
    }

    private fun Any?.asStringMap(): Map<String, Any?> {
        if (this !is Map<*, *>) {
            return emptyMap()
        }

        return entries.associate { (entryKey, entryValue) ->
            entryKey.toString() to entryValue
        }
    }

    private fun Map<String, Any?>.mapAny(vararg keys: String): Map<String, Any?> {
        for (key in keys) {
            val value = map(key)

            if (value.isNotEmpty()) {
                return value
            }
        }

        return emptyMap()
    }

    private fun Map<String, Any?>.string(key: String, default: String): String = this[key]?.toString() ?: default

    private fun Map<String, Any?>.stringAny(keys: List<String>, default: String): String {
        for (key in keys) {
            val value = this[key] ?: continue

            return value.toString()
        }

        return default
    }

    private fun Map<String, Any?>.boolean(key: String, default: Boolean): Boolean = when (val value = this[key]) {
        is Boolean -> value
        is String -> value.toBooleanStrictOrNull() ?: default
        else -> default
    }

    private fun Map<String, Any?>.booleanAny(keys: List<String>, default: Boolean): Boolean {
        for (key in keys) {
            if (key in this) {
                return boolean(key, default)
            }
        }

        return default
    }

    private fun Map<String, Any?>.int(key: String, default: Int): Int = when (val value = this[key]) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull() ?: default
        else -> default
    }

    private fun Map<String, Any?>.long(key: String, default: Long): Long = when (val value = this[key]) {
        is Number -> value.toLong()
        is String -> value.toLongOrNull() ?: default
        else -> default
    }

    private fun Map<String, Any?>.intAny(keys: List<String>, default: Int): Int {
        for (key in keys) {
            if (key in this) {
                return int(key, default)
            }
        }

        return default
    }

    private fun Map<String, Any?>.ticksAsMilliseconds(key: String, defaultTicks: Int): Long = int(key, defaultTicks) * 50L

    private fun Map<String, Any?>.multilineString(key: String, default: String): String = when (val value = this[key]) {
        is Iterable<*> -> value.joinToString("\n") { it?.toString().orEmpty() }
        null -> default
        else -> value.toString().replace("\\n", "\n")
    }

    private fun String.limitScoreboardLines(): String {
        return lineSequence().take(MAX_SCOREBOARD_LINES).joinToString("\n")
    }

    private fun Map<String, Any?>.stringList(key: String): List<String> = when (val value = this[key]) {
        is Iterable<*> -> value.mapNotNull { it?.toString() }.filter { it.isNotBlank() }
        is String -> listOf(value).filter { it.isNotBlank() }
        else -> emptyList()
    }

    private fun List<String>.disabledWorldsMap(): String {
        if (isEmpty()) {
            return "{}"
        }

        return buildString {
            appendLine()
            for (worldName in this@disabledWorldsMap) {
                append("  ")
                append(quoted(worldName))
                appendLine(":")
                appendLine("    enabled: false")
            }
        }.trimEnd()
    }

    private fun StringBuilder.appendStringList(label: String, values: List<String>) {
        if (values.isEmpty()) {
            appendLine("$label: []")
            return
        }

        appendLine("$label:")
        values.forEach { value ->
            appendLine("      - ${quoted(value)}")
        }
    }

    private fun String.toTypedLineBreak(): String = replace("\\n", "<nl>").replace("\n", "<nl>")

    private fun quoted(value: String): String = buildString {
        append('"')

        for (character in value) {
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(character)
            }
        }

        append('"')
    }

    private companion object {
        const val LEGACY_CONFIGURATION_FILE_NAME = ConfigurationFiles.LEGACY_CONFIGURATION_FILE_NAME
        const val MAX_SCOREBOARD_LINES = ConfigurationFiles.MAX_LEGACY_SCOREBOARD_LINES
    }
}

sealed class LegacyConfigurationMigrationResult {
    data object NoMigrationNeeded : LegacyConfigurationMigrationResult()

    data class Migrated(val backupFile: Path) : LegacyConfigurationMigrationResult()
}

class ConfigurationMigrationException(cause: Throwable) : ConfigurationException(
    "Failed to migrate legacy ${ConfigurationFiles.LEGACY_CONFIGURATION_FILE_NAME}: ${cause.message}",
    cause
)
