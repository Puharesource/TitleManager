import dev.tarkan.titlemanager.animation.TimelineAnimation
import dev.tarkan.titlemanager.bukkit.animation.ColorCycleAnimation
import dev.tarkan.titlemanager.bukkit.animation.DefaultAnimationFiles
import dev.tarkan.titlemanager.bukkit.animation.addAnimationFilePlaceholders
import dev.tarkan.titlemanager.bukkit.configuration.ConfigurationManager
import dev.tarkan.titlemanager.bukkit.configuration.ConfigurationLoadException
import dev.tarkan.titlemanager.bukkit.configuration.ConfigurationMigrationException
import dev.tarkan.titlemanager.bukkit.configuration.ConfigurationResourceProvider
import dev.tarkan.titlemanager.bukkit.configuration.UnsupportedLegacyFeatureException
import dev.tarkan.titlemanager.parser.IntermediaryParser
import dev.tarkan.titlemanager.parser.animation.AnimationParser
import dev.tarkan.titlemanager.parser.placeholder.animation.AnimationPlaceholderRegistry
import dev.tarkan.titlemanager.parser.placeholder.animation.addCoreAnimationPlaceholders
import dev.tarkan.titlemanager.parser.placeholder.variable.VariablePlaceholderRegistry
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfigurationManagerTest {
    @Test
    fun `copies and decodes bundled default configs without a running Bukkit plugin`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            val configurationManager = ConfigurationManager(classpathResourceProvider(), tempDirectory)

            assertEquals(4, configurationManager.advancedConfiguration.threadPoolSize)
            assertEquals(false, configurationManager.advancedConfiguration.debug)
            assertEquals(true, configurationManager.advancedConfiguration.usingConfig)
            assertEquals(false, configurationManager.advancedConfiguration.usingBungeeCord)
            assertEquals(true, configurationManager.advancedConfiguration.preventDuplicatePackets)
            assertEquals("en-US", configurationManager.placeholderConfiguration.locale)
            assertEquals(2, configurationManager.welcomeTitleConfiguration.worlds.size)
            assertEquals("&5Welcome to the End!", configurationManager.welcomeTitleConfiguration.worlds["world_the_end"]?.title)
            assertEquals("Welcome to My Server", configurationManager.welcomeActionbarConfiguration.firstJoin.title)
            assertEquals(50L, configurationManager.playerListConfiguration.updateIntervalMilliseconds)
            assertEquals(50L, configurationManager.scoreboardConfiguration.updateIntervalMilliseconds)
            assertTrue(configurationManager.playerListConfiguration.footer.contains("%{server-time}"))
            assertTrue(configurationManager.scoreboardConfiguration.content.contains("\${cycle:sunset}"))
            assertTrue(configurationManager.gradientsConfiguration.findGradient("RAINBOW") != null)
            assertFalse(configurationManager.announcerConfiguration.enabled)
            assertTrue(configurationManager.announcerConfiguration.announcements.containsKey("example"))
            assertTrue(configurationManager.hooksConfiguration.combatLogX)

            for (fileName in DEFAULT_CONFIG_FILES) {
                val configFile = tempDirectory.resolve(fileName)
                assertTrue(configFile.exists(), "$fileName should be copied to the data folder")
                assertTrue(configFile.fileSize() > 0L, "$fileName should not be empty")
            }
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `default scoreboard animations compile against default gradients`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            val configurationManager = ConfigurationManager(classpathResourceProvider(), tempDirectory)
            val variablePlaceholderRegistry = VariablePlaceholderRegistry.build<Unit> {}
            val animationPlaceholderRegistry = AnimationPlaceholderRegistry.build<Unit> {
                addCoreAnimationPlaceholders()
                addSimple("cycle", dataSerializer = ColorCycleAnimation.DataSerializer(configurationManager.gradientsConfiguration)) { data ->
                    val cycleData = requireNotNull(data) { "No cycle data found" }

                    ColorCycleAnimation(cycleData.timing, cycleData.gradient)
                }
            }
            val parser = AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)
            val intermediaryParser = IntermediaryParser()

            fun compile(text: String) {
                parser.parseAnimation(intermediaryParser.parseText(text))
            }

            compile(configurationManager.scoreboardConfiguration.title)
            compile(configurationManager.scoreboardConfiguration.content)
            configurationManager.scoreboardConfiguration.worlds.values.forEach { worldConfig ->
                compile(worldConfig.title)
                compile(worldConfig.content)
            }
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `default player list animations render without missing animation placeholders`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            val resourceProvider = classpathResourceProvider()
            val configurationManager = ConfigurationManager(resourceProvider, tempDirectory)
            val intermediaryParser = IntermediaryParser()
            val animationsFolder = DefaultAnimationFiles.copyMissingTo(tempDirectory, resourceProvider::openResource)
            val variablePlaceholderRegistry = VariablePlaceholderRegistry.build<Unit> {}
            lateinit var parser: AnimationParser<Unit>
            val animationPlaceholderRegistry = AnimationPlaceholderRegistry.build<Unit> {
                addCoreAnimationPlaceholders()
                addAnimationFilePlaceholders(DefaultAnimationFiles.FILE_NAMES.map { animationsFolder.resolve(it) }, intermediaryParser) {
                    parser
                }
            }
            parser = AnimationParser(variablePlaceholderRegistry, animationPlaceholderRegistry)

            fun renderedFrames(text: String): List<String> {
                val animation = parser.parseAnimation(intermediaryParser.parseText(text))
                val timeline = (animation as TimelineAnimation<Unit, String>).singleIterationTimeline(Unit)

                return timeline.frames.map { it.item }
            }

            fun assertNoMissingAnimationPlaceholder(text: String) {
                assertFalse(
                    renderedFrames(text).any { it.contains("Unknown-Animation-Placeholder") },
                    "Default player-list text should not render missing animation placeholders"
                )
            }

            assertNoMissingAnimationPlaceholder(configurationManager.playerListConfiguration.header)
            assertNoMissingAnimationPlaceholder(configurationManager.playerListConfiguration.footer)
            configurationManager.playerListConfiguration.worlds.values.forEach { worldConfig ->
                assertNoMissingAnimationPlaceholder(worldConfig.header)
                assertNoMissingAnimationPlaceholder(worldConfig.footer)
            }
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `keeps non-empty existing configs instead of overwriting them`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("placeholder.yml").writeText(
                """
                    locale: "da-DK"
                    numberFormat:
                      enabled: true
                      format: "#,###.##"
                    dateFormat: "HH:mm:ss"
                    aliases: {}
                """.trimIndent()
            )

            val configurationManager = ConfigurationManager(classpathResourceProvider(), tempDirectory)

            assertEquals("da-DK", configurationManager.placeholderConfiguration.locale)
            assertEquals("HH:mm:ss", configurationManager.placeholderConfiguration.dateFormat)
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects negative welcome timing values while loading configuration`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("welcome-title.yml").writeText(
                """
                    enabled: true
                    delayMilliseconds: -1
                    title: "Bad delay"
                    subtitle: ""
                    fadeIn: 20
                    stay: 40
                    fadeOut: 20
                    firstJoin:
                      enabled: false
                    worlds: {}
                """.trimIndent()
            )

            val exception = assertFailsWith<ConfigurationLoadException> {
                ConfigurationManager(classpathResourceProvider(), tempDirectory)
            }

            assertEquals("welcome-title.yml", exception.fileName)
            assertTrue(exception.message!!.contains("Welcome title delay must not be negative"))
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects negative announcer title timing values while loading configuration`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("announcer.yml").writeText(
                """
                    enabled: true
                    announcements:
                      invalid:
                        interval: 60
                        timings:
                          fadeIn: -1
                          stay: 40
                          fadeOut: 20
                        titles:
                          - "Bad timing"
                        actionbar: []
                """.trimIndent()
            )

            val exception = assertFailsWith<ConfigurationLoadException> {
                ConfigurationManager(classpathResourceProvider(), tempDirectory)
            }

            assertEquals("announcer.yml", exception.fileName)
            assertTrue(exception.message!!.contains("Announcer fade-in must not be negative"))
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects invalid gradient definitions while loading configuration`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("gradients.yml").writeText(
                """
                    gradients:
                      invalid:
                        colorSpace: RGB
                        precision: 100
                        colors:
                          - '#000000'
                          - '#ffffff'
                """.trimIndent()
            )

            val exception = assertFailsWith<ConfigurationLoadException> {
                ConfigurationManager(classpathResourceProvider(), tempDirectory)
            }

            assertEquals("gradients.yml", exception.fileName)
            assertTrue(exception.message!!.contains("Gradient color space must be either HSL or HSL Long"))
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `rejects invalid number format while loading configuration`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("placeholder.yml").writeText(
                """
                    locale: en-US
                    numberFormat:
                      enabled: true
                      format: "0.00E"
                    dateFormat: "EEE, dd MMM yyyy HH:mm:ss z"
                    aliases: {}
                """.trimIndent()
            )

            val exception = assertFailsWith<ConfigurationLoadException> {
                ConfigurationManager(classpathResourceProvider(), tempDirectory)
            }

            assertEquals("placeholder.yml", exception.fileName)
            assertTrue(exception.message!!.contains("Malformed exponential pattern"))
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `migrates legacy monolithic config into split config files`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("config.yml").writeText(LEGACY_CONFIG)

            val configurationManager = ConfigurationManager(classpathResourceProvider(), tempDirectory)

            assertTrue(tempDirectory.resolve("config.yml.legacy-backup").exists())
            assertEquals(7, configurationManager.advancedConfiguration.configVersion)
            assertEquals(true, configurationManager.advancedConfiguration.debug)
            assertEquals(false, configurationManager.advancedConfiguration.usingConfig)
            assertEquals(true, configurationManager.advancedConfiguration.usingBungeeCord)
            assertEquals(false, configurationManager.advancedConfiguration.checkForUpdates)
            assertEquals(false, configurationManager.advancedConfiguration.preventDuplicatePackets)
            assertEquals("\n${'$'}{shine:[0;2;0][&3;&b]Legacy Server}\n", configurationManager.playerListConfiguration.header)
            assertEquals("&7Online: &b%{online}", configurationManager.playerListConfiguration.footer)
            assertEquals(125L, configurationManager.playerListConfiguration.updateIntervalMilliseconds)
            assertEquals("da-DK", configurationManager.placeholderConfiguration.locale)
            assertEquals(false, configurationManager.placeholderConfiguration.numberFormat.enabled)
            assertEquals("HH:mm:ss", configurationManager.placeholderConfiguration.dateFormat)
            assertEquals(false, configurationManager.welcomeTitleConfiguration.enabled)
            assertEquals(400L, configurationManager.welcomeTitleConfiguration.delayMilliseconds)
            assertEquals("Legacy first title", configurationManager.welcomeTitleConfiguration.firstJoin.title)
            assertEquals("Legacy actionbar", configurationManager.welcomeActionbarConfiguration.title)
            assertEquals("First legacy actionbar", configurationManager.welcomeActionbarConfiguration.firstJoin.title)
            assertEquals("Legacy Board", configurationManager.scoreboardConfiguration.title)
            assertEquals("&aLine 1\n&bLine 2", configurationManager.scoreboardConfiguration.content)
            assertEquals(250L, configurationManager.scoreboardConfiguration.updateIntervalMilliseconds)
            assertEquals(false, configurationManager.scoreboardConfiguration.worlds["legacy-disabled-world"]?.enabled)
            assertEquals(false, configurationManager.scoreboardConfiguration.worlds["legacy-disabled-world-nether"]?.enabled)
            assertFalse(configurationManager.announcerConfiguration.enabled)
            assertFalse(configurationManager.hooksConfiguration.combatLogX)

            for (fileName in DEFAULT_CONFIG_FILES) {
                assertTrue(tempDirectory.resolve(fileName).exists(), "$fileName should be created by migration")
            }
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `migrates older legacy config keys that old TitleManager auto-migrated`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("config.yml").writeText(OLDER_LEGACY_CONFIG)

            val configurationManager = ConfigurationManager(classpathResourceProvider(), tempDirectory)

            assertEquals(false, configurationManager.advancedConfiguration.usingConfig)
            assertEquals(true, configurationManager.advancedConfiguration.checkForUpdates)
            assertEquals(true, configurationManager.advancedConfiguration.preventDuplicatePackets)
            assertEquals(false, configurationManager.playerListConfiguration.enabled)
            assertEquals("Old Header\nSecond Header", configurationManager.playerListConfiguration.header)
            assertEquals("Old Footer", configurationManager.playerListConfiguration.footer)
            assertEquals(50L, configurationManager.playerListConfiguration.updateIntervalMilliseconds)
            assertEquals(false, configurationManager.placeholderConfiguration.numberFormat.enabled)
            assertEquals("0.000", configurationManager.placeholderConfiguration.numberFormat.format)
            assertEquals("HH:mm", configurationManager.placeholderConfiguration.dateFormat)
            assertEquals("Old Welcome Title", configurationManager.welcomeTitleConfiguration.title)
            assertEquals("Old First Title", configurationManager.welcomeTitleConfiguration.firstJoin.title)
            assertEquals(3, configurationManager.welcomeTitleConfiguration.fadeIn)
            assertEquals(5, configurationManager.welcomeTitleConfiguration.fadeOut)
            assertEquals("Old Actionbar", configurationManager.welcomeActionbarConfiguration.title)
            assertEquals("Old First Actionbar", configurationManager.welcomeActionbarConfiguration.firstJoin.title)
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `legacy migration limits scoreboard content to legacy sidebar line count`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("config.yml").writeText(
                buildString {
                    appendLine("config-version: 7")
                    appendLine("scoreboard:")
                    appendLine("  enabled: true")
                    appendLine("  title: \"Legacy Board\"")
                    appendLine("  lines:")
                    (1..16).forEach { lineNumber ->
                        appendLine("  - \"Line $lineNumber\"")
                    }
                }
            )

            val configurationManager = ConfigurationManager(classpathResourceProvider(), tempDirectory)
            val migratedLines = configurationManager.scoreboardConfiguration.content.lines()

            assertEquals(15, migratedLines.size)
            assertEquals("Line 15", migratedLines.last())
            assertFalse(configurationManager.scoreboardConfiguration.content.contains("Line 16"))
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `legacy migration does not overwrite existing split config files`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("config.yml").writeText(LEGACY_CONFIG)
            tempDirectory.resolve("placeholder.yml").writeText(
                """
                    locale: "sv-SE"
                    numberFormat:
                      enabled: true
                      format: "#"
                    dateFormat: "yyyy"
                    aliases: {}
                """.trimIndent()
            )

            val configurationManager = ConfigurationManager(classpathResourceProvider(), tempDirectory)

            assertTrue(tempDirectory.resolve("config.yml.legacy-backup").exists())
            assertEquals("sv-SE", configurationManager.placeholderConfiguration.locale)
            assertEquals("yyyy", configurationManager.placeholderConfiguration.dateFormat)
            assertEquals(true, configurationManager.placeholderConfiguration.numberFormat.enabled)
            assertTrue(tempDirectory.resolve("player-list.yml").exists())
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `fails loudly when a missing default config must be copied`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            val exception = assertFailsWith<ConfigurationLoadException> {
                ConfigurationManager(ConfigurationResourceProvider { null }, tempDirectory)
            }

            assertEquals("advanced.yml", exception.fileName)
            assertTrue(exception.message!!.contains("Missing default configuration resource: DefaultConfigs/advanced.yml"))
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports invalid edited split config file with file name`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("placeholder.yml").writeText(
                """
                    locale: []
                    numberFormat:
                      enabled: true
                      format: "#"
                    dateFormat: "yyyy"
                    aliases: {}
                """.trimIndent()
            )

            val exception = assertFailsWith<ConfigurationLoadException> {
                ConfigurationManager(classpathResourceProvider(), tempDirectory)
            }

            assertEquals("placeholder.yml", exception.fileName)
            assertTrue(exception.message!!.contains("placeholder.yml"))
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports invalid legacy config migration without writing split files`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("config.yml").writeText("- this-is-not-a-root-map")

            val exception = assertFailsWith<ConfigurationMigrationException> {
                ConfigurationManager(classpathResourceProvider(), tempDirectory)
            }

            assertTrue(exception.message!!.contains("Failed to migrate legacy config.yml"))
            assertTrue(tempDirectory.resolve("config.yml.legacy-backup").exists())

            for (fileName in DEFAULT_CONFIG_FILES) {
                assertTrue(!tempDirectory.resolve(fileName).exists(), "$fileName should not be written after migration failure")
            }
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `migrates enabled legacy announcer into split announcer configuration`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("config.yml").writeText(
                """
                    config-version: 7
                    announcer:
                      enabled: true
                      announcements:
                        legacy:
                          interval: 5
                          timings:
                            fade-in: 1
                            stay: 2
                            fade-out: 3
                          titles:
                          - "Legacy title\nLegacy subtitle"
                          actionbar:
                          - "Legacy actionbar"
                """.trimIndent()
            )

            val configurationManager = ConfigurationManager(classpathResourceProvider(), tempDirectory)

            assertTrue(tempDirectory.resolve("config.yml.legacy-backup").exists())
            assertTrue(configurationManager.announcerConfiguration.enabled)
            val announcement = configurationManager.announcerConfiguration.announcements.getValue("legacy")
            assertEquals(5, announcement.interval)
            assertEquals(1, announcement.timings.fadeIn)
            assertEquals(2, announcement.timings.stay)
            assertEquals(3, announcement.timings.fadeOut)
            assertEquals(listOf("Legacy title<nl>Legacy subtitle"), announcement.titles)
            assertEquals(listOf("Legacy actionbar"), announcement.actionbar)
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports enabled unsupported legacy client support without writing split files`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            tempDirectory.resolve("config.yml").writeText(
                """
                    config-version: 7
                    legacy-client-support: true
                    announcer:
                      enabled: false
                """.trimIndent()
            )

            val exception = assertFailsWith<ConfigurationMigrationException> {
                ConfigurationManager(classpathResourceProvider(), tempDirectory)
            }

            assertTrue(exception.message!!.contains("Legacy 1.7 client actionbar support is not supported"))
            assertFalse(tempDirectory.resolve("advanced.yml").exists())
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `reports legacy script files before loading split configuration`() {
        val tempDirectory = Files.createTempDirectory("titlemanager-config")

        try {
            val scriptsDirectory = tempDirectory.resolve("scripts")
            scriptsDirectory.createDirectories()
            scriptsDirectory.resolve("legacy-script.js").writeText("print('legacy')")

            val exception = assertFailsWith<UnsupportedLegacyFeatureException> {
                ConfigurationManager(classpathResourceProvider(), tempDirectory)
            }

            assertTrue(exception.message!!.contains("Legacy script files in the scripts folder are not supported"))
        } finally {
            tempDirectory.toFile().deleteRecursively()
        }
    }

    private fun classpathResourceProvider(): ConfigurationResourceProvider {
        val classLoader = Thread.currentThread().contextClassLoader

        return ConfigurationResourceProvider { path ->
            classLoader.getResourceAsStream(path)
        }
    }

    private companion object {
        val DEFAULT_CONFIG_FILES = listOf(
            "advanced.yml",
            "player-list.yml",
            "placeholder.yml",
            "welcome-title.yml",
            "welcome-actionbar.yml",
            "scoreboard.yml",
            "gradients.yml",
            "announcer.yml",
            "hooks.yml"
        )

        const val LEGACY_CONFIG = """
            config-version: 7
            debug: true
            using-config: false
            using-bungeecord: true
            check-for-updates: false
            locale: "da-DK"
            bandwidth:
              prevent-duplicate-packets: false
              player-list-ms-per-tick: 125
              scoreboard-ms-per-tick: 250
            player-list:
              enabled: true
              header:
              - ''
              - '${'$'}{shine:[0;2;0][&3;&b]Legacy Server}'
              - ''
              footer: '&7Online: &b%{online}'
            welcome-title:
              enabled: false
              title: "Legacy title"
              subtitle: "Legacy subtitle"
              fade-in: 5
              stay: 6
              fade-out: 7
              delay: 8
              first-join:
                title: "Legacy first title"
                subtitle: "Legacy first subtitle"
            welcome-actionbar:
              enabled: true
              title: "Legacy actionbar"
              delay: 3
              first-join: "First legacy actionbar"
            placeholders:
              number-format:
                enabled: false
                format: "0.00"
              date-format: "HH:mm:ss"
            scoreboard:
              enabled: true
              title: "Legacy Board"
              lines:
              - "&aLine 1"
              - "&bLine 2"
              disabled-worlds:
              - "legacy-disabled-world"
              - "legacy-disabled-world-nether"
            hooks:
              combatlogx: false
        """

        const val OLDER_LEGACY_CONFIG = """
            config-version: 5
            usingConfig: false
            updater:
              check-automatically: true
            tabmenu:
              enabled: false
              header: 'Old Header\nSecond Header'
              footer: "Old Footer"
            number-format:
              enabled: false
              format: "0.000"
            date-format:
              format: "HH:mm"
            welcome_message:
              enabled: true
              title: "Old Welcome Title"
              subtitle: "Old Welcome Subtitle"
              fadeIn: 3
              stay: 4
              fadeOut: 5
              first-join:
                title: "Old First Title"
                subtitle: "Old First Subtitle"
            actionbar-welcome:
              enabled: true
              message: "Old Actionbar"
              first-join:
                message: "Old First Actionbar"
            scoreboard:
              enabled: false
              title: "Old Board"
              lines: []
            announcer:
              enabled: false
            hooks:
              combatlogx: false
        """
    }
}
