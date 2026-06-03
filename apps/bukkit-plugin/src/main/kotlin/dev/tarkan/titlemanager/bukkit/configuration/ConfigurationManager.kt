package dev.tarkan.titlemanager.bukkit.configuration

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import dev.tarkan.titlemanager.bukkit.plugin.TitleManagerPlugin
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

open class ConfigurationException(message: String, cause: Throwable? = null) : IllegalStateException(message, cause)

class ConfigurationLoadException(
    val fileName: String,
    cause: Throwable
) : ConfigurationException("Failed to load configuration file '$fileName': ${cause.message}", cause)

class UnsupportedLegacyFeatureException(message: String) : ConfigurationException(message)

fun interface ConfigurationResourceProvider {
    fun openResource(path: String): InputStream?
}

class PluginConfigurationResourceProvider(private val plugin: TitleManagerPlugin) : ConfigurationResourceProvider {
    override fun openResource(path: String): InputStream? = plugin.getResource(path)
}

class ConfigurationManager(private val resourceProvider: ConfigurationResourceProvider, private val dataFolder: Path) {
    constructor(plugin: TitleManagerPlugin, dataFolder: Path) : this(PluginConfigurationResourceProvider(plugin), dataFolder)

    internal companion object {
        const val ADVANCED_CONFIGURATION_FILE_NAME = ConfigurationFiles.ADVANCED_CONFIGURATION_FILE_NAME
        const val PLAYER_LIST_CONFIGURATION_FILE_NAME = ConfigurationFiles.PLAYER_LIST_CONFIGURATION_FILE_NAME
        const val PLACEHOLDER_CONFIGURATION_FILE_NAME = ConfigurationFiles.PLACEHOLDER_CONFIGURATION_FILE_NAME
        const val WELCOME_TITLE_CONFIGURATION_FILE_NAME = ConfigurationFiles.WELCOME_TITLE_CONFIGURATION_FILE_NAME
        const val WELCOME_ACTIONBAR_CONFIGURATION_FILE_NAME = ConfigurationFiles.WELCOME_ACTIONBAR_CONFIGURATION_FILE_NAME
        const val SCOREBOARD_CONFIGURATION_FILE_NAME = ConfigurationFiles.SCOREBOARD_CONFIGURATION_FILE_NAME
        const val GRADIENTS_CONFIGURATION_FILE_NAME = ConfigurationFiles.GRADIENTS_CONFIGURATION_FILE_NAME
        const val ANNOUNCER_CONFIGURATION_FILE_NAME = ConfigurationFiles.ANNOUNCER_CONFIGURATION_FILE_NAME
        const val HOOKS_CONFIGURATION_FILE_NAME = ConfigurationFiles.HOOKS_CONFIGURATION_FILE_NAME

        val CONFIGURATION_FILE_NAMES = ConfigurationFiles.SPLIT_CONFIGURATION_FILE_NAMES
    }

    val advancedConfiguration: AdvancedConfiguration
    val playerListConfiguration: PlayerListConfiguration
    val placeholderConfiguration: PlaceholderConfiguration
    val welcomeTitleConfiguration: WelcomeTitleConfiguration
    val welcomeActionbarConfiguration: WelcomeActionbarConfiguration
    val scoreboardConfiguration: ScoreboardConfiguration
    val gradientsConfiguration: GradientsConfiguration
    val announcerConfiguration: AnnouncerConfiguration
    val hooksConfiguration: HooksConfiguration

    init {
        LegacyConfigurationMigrator(resourceProvider, dataFolder).migrateIfNeeded()
        rejectLegacyScriptsDirectory()

        advancedConfiguration = readConfiguration(ADVANCED_CONFIGURATION_FILE_NAME)
        playerListConfiguration = readConfiguration(PLAYER_LIST_CONFIGURATION_FILE_NAME)
        placeholderConfiguration = readConfiguration(PLACEHOLDER_CONFIGURATION_FILE_NAME)
        welcomeTitleConfiguration = readConfiguration(WELCOME_TITLE_CONFIGURATION_FILE_NAME)
        welcomeActionbarConfiguration = readConfiguration(WELCOME_ACTIONBAR_CONFIGURATION_FILE_NAME)
        scoreboardConfiguration = readConfiguration(SCOREBOARD_CONFIGURATION_FILE_NAME)
        gradientsConfiguration = readConfiguration(GRADIENTS_CONFIGURATION_FILE_NAME)
        announcerConfiguration = readConfiguration(ANNOUNCER_CONFIGURATION_FILE_NAME)
        hooksConfiguration = readConfiguration(HOOKS_CONFIGURATION_FILE_NAME)
    }

    private fun ensureDataFolderExists() {
        if (!dataFolder.exists()) {
            dataFolder.createDirectories()
        }
    }

    private fun rejectLegacyScriptsDirectory() {
        val scriptsDirectory = dataFolder.resolve(ConfigurationFiles.SCRIPTS_DIRECTORY_NAME)

        if (!scriptsDirectory.exists() || !scriptsDirectory.isDirectory()) {
            return
        }

        val hasScriptFiles = Files.walk(scriptsDirectory).use { paths ->
            paths.anyMatch { path -> Files.isRegularFile(path) }
        }

        if (hasScriptFiles) {
            throw UnsupportedLegacyFeatureException("Legacy script files in the scripts folder are not supported in TitleManager Next because arbitrary script execution is disabled.")
        }
    }

    private inline fun <reified T> readConfiguration(path: String): T {
        try {
            ensureDataFolderExists()

            val configurationFile = dataFolder.resolve(path)

            if (!configurationFile.exists()) {
                configurationFile.createFile()
            }

            if (configurationFile.fileSize() == 0L) {
                resourceProvider.copyDefaultConfiguration(path, configurationFile)
            }

            return Yaml.default.decodeFromStream<T>(configurationFile.inputStream())
        } catch (exception: ConfigurationException) {
            throw exception
        } catch (exception: Exception) {
            throw ConfigurationLoadException(path, exception)
        }
    }
}