package dev.tarkan.titlemanager.bukkit.configuration

import java.nio.file.Path
import kotlin.io.path.outputStream

internal object ConfigurationFiles {
    const val DEFAULT_CONFIGURATION_DIRECTORY = "DefaultConfigs"
    const val LEGACY_CONFIGURATION_FILE_NAME = "config.yml"
    const val LEGACY_BACKUP_SUFFIX = ".legacy-backup"
    const val SCRIPTS_DIRECTORY_NAME = "scripts"
    const val MAX_LEGACY_SCOREBOARD_LINES = 15

    const val ADVANCED_CONFIGURATION_FILE_NAME = "advanced.yml"
    const val PLAYER_LIST_CONFIGURATION_FILE_NAME = "player-list.yml"
    const val PLACEHOLDER_CONFIGURATION_FILE_NAME = "placeholder.yml"
    const val WELCOME_TITLE_CONFIGURATION_FILE_NAME = "welcome-title.yml"
    const val WELCOME_ACTIONBAR_CONFIGURATION_FILE_NAME = "welcome-actionbar.yml"
    const val SCOREBOARD_CONFIGURATION_FILE_NAME = "scoreboard.yml"
    const val GRADIENTS_CONFIGURATION_FILE_NAME = "gradients.yml"
    const val ANNOUNCER_CONFIGURATION_FILE_NAME = "announcer.yml"
    const val HOOKS_CONFIGURATION_FILE_NAME = "hooks.yml"

    val SPLIT_CONFIGURATION_FILE_NAMES = listOf(
        ADVANCED_CONFIGURATION_FILE_NAME,
        PLAYER_LIST_CONFIGURATION_FILE_NAME,
        PLACEHOLDER_CONFIGURATION_FILE_NAME,
        WELCOME_TITLE_CONFIGURATION_FILE_NAME,
        WELCOME_ACTIONBAR_CONFIGURATION_FILE_NAME,
        SCOREBOARD_CONFIGURATION_FILE_NAME,
        GRADIENTS_CONFIGURATION_FILE_NAME,
        ANNOUNCER_CONFIGURATION_FILE_NAME,
        HOOKS_CONFIGURATION_FILE_NAME
    )

    fun defaultResourcePath(fileName: String): String = "$DEFAULT_CONFIGURATION_DIRECTORY/$fileName"

    fun legacyBackupFileName(index: Int? = null): String {
        return buildString {
            append(LEGACY_CONFIGURATION_FILE_NAME)
            append(LEGACY_BACKUP_SUFFIX)
            if (index != null) {
                append('-')
                append(index)
            }
        }
    }
}

internal fun ConfigurationResourceProvider.copyDefaultConfiguration(fileName: String, target: Path) {
    val resourcePath = ConfigurationFiles.defaultResourcePath(fileName)

    openResource(resourcePath)?.use { defaultConfigStream ->
        target.outputStream().use(defaultConfigStream::transferTo)
    } ?: error("Missing default configuration resource: $resourcePath")
}

internal fun ConfigurationResourceProvider.readDefaultConfiguration(fileName: String): String {
    val resourcePath = ConfigurationFiles.defaultResourcePath(fileName)

    return openResource(resourcePath)?.use { inputStream ->
        inputStream.reader(Charsets.UTF_8).readText()
    } ?: error("Missing default configuration resource: $resourcePath")
}
