package io.puharesource.mc.titlemanager.internal.config

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.internal.debug
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class ConfigMigration(private val plugin: TitleManagerPlugin) {
    private val animationsFolder = File(plugin.dataFolder, "animations")
    private val oldAnimationPattern = "(?i)^animation[:](.+)$".toRegex()
    private val oldPlaceholderPattern = "\\{(.+)}".toRegex()

    private val config
        get() = plugin.config

    private val dataFolder
        get() = plugin.dataFolder

    private val oldConfig by lazy {
        YamlConfiguration.loadConfiguration(oldConfigFile.reader())
    }

    private val configFile by lazy {
        File(dataFolder, "config.yml")
    }

    private val oldConfigFile by lazy {
        File(dataFolder, "config-old-3.yml")
    }

    private val oldAnimationFile by lazy {
        File(dataFolder, "animations.yml")
    }

    fun updateConfig() {
        val config = plugin.config

        var version = config.getInt("config-version")

        if (version < 4) {
            updateTo4()
            plugin.reloadConfig()
        }

        version = config.getInt("config-version")

        if (version < 5) {
            updateTo5()
            plugin.reloadConfig()
        }

        version = config.getInt("config-version")

        if (version < 6) {
            updateTo6()
            plugin.reloadConfig()
        }

        version = config.getInt("config-version")

        if (version < 7) {
            updateTo7()
            plugin.reloadConfig()
        }
    }

    private fun move(path: String, newPath: String? = null, transformer: ((Any) -> Any)? = null) {
        val correctNewPath = newPath ?: path

        if (oldConfig.contains(path)) {
            var oldValue = oldConfig.get(path)!!

            if (oldValue is String) {
                if (oldValue.matches(oldAnimationPattern)) {
                    oldValue = "\${${oldValue.substring(10)}}"
                } else if (oldValue.contains(oldPlaceholderPattern)) {
                    oldValue.replace(oldPlaceholderPattern, transform = { "%{${it.groups[1]!!.value}}" })
                }
            }

            if (transformer != null) {
                config.set(correctNewPath, transformer(oldValue))
            } else {
                config.set(correctNewPath, oldValue)
            }
        }
    }

    private fun updateTo4() {
        debug("Upgrading config to the 2.0 format.")

        configFile.renameTo(oldConfigFile)
        plugin.saveDefaultConfig()

        plugin.conf = PrettyConfig(configFile)

        move("usingConfig", "using-config")
        move("using-bungeecord")
        move("legacy-client-support")
        move("updater.check-automatically", "check-for-updates")

        move("tabmenu.enabled", "player-list.enabled")
        move(
            "tabmenu.header",
            "player-list.header",
            transformer = {
                val header = it as String

                if (header.contains("\\n")) {
                    header.split("\\n")
                } else {
                    header
                }
            }
        )
        move(
            "tabmenu.footer",
            "player-list.footer",
            transformer = {
                val footer = it as String

                if (footer.contains("\\n")) {
                    footer.split("\\n")
                } else {
                    footer
                }
            }
        )

        move("welcome_message.enabled", "welcome-title.enabled")
        move("welcome_message.title", "welcome-title.title")
        move("welcome_message.subtitle", "welcome-title.subtitle")
        move("welcome_message.fadeIn", "welcome-title.fade-in")
        move("welcome_message.stay", "welcome-title.stay")
        move("welcome_message.fadeOut", "welcome-title.fade-out")
        move("welcome_message.first-join.title", "welcome-title.first-join.title")
        move("welcome_message.first-join.subtitle", "welcome-title.first-join.subtitle")

        move("actionbar-welcome.enabled", "welcome-actionbar.enabled")
        move("actionbar-welcome.message", "welcome-actionbar.title")
        move("actionbar-welcome.first-join.message", "welcome-actionbar.first-join")

        move("number-format.enabled", "placeholders.number-format.enabled")
        move("number-format.format", "placeholders.number-format.format")
        move("date-format.format", "placeholders.date-format")

        config.save(configFile)

        if (oldAnimationFile.exists()) {
            val oldAnimationsConfig = YamlConfiguration.loadConfiguration(oldAnimationFile)

            oldAnimationsConfig.getKeys(false)
                .map { it to oldAnimationsConfig.getStringList("$it.frames") }
                .forEach { entry ->
                    val name = entry.first
                    val frames = entry.second
                        .joinToString(separator = "\n")
                        .replace(oldPlaceholderPattern, transform = { "%{${it.groups[1]!!.value}}" })

                    animationsFolder.mkdirs()
                    val file = File(animationsFolder, "$name.txt")

                    if (!file.exists()) {
                        file.createNewFile()
                        file.writeText(frames)
                    }
                }

            oldAnimationFile.renameTo(File(dataFolder, "animations-old.yml"))
        }
    }

    private fun updateTo5() {
        debug("Upgrading config from version 4 to version 5")

        val configFile = File(dataFolder, "config.yml")
        val oldFile = File(dataFolder, "config-old-4.yml")

        configFile.renameTo(oldFile)
        plugin.saveDefaultConfig()

        val conf = PrettyConfig(configFile)
        plugin.conf = conf
        val oldConfig = YamlConfiguration.loadConfiguration(oldFile.reader())

        conf.getKeys(false)
            .asSequence()
            .filter { it != "config-version" && it != "messages" }
            .filter { oldConfig.contains(it) }
            .map { it to oldConfig[it] }
            .forEach { conf.set(it.first, it.second) }

        conf.getConfigurationSection("messages")!!.getKeys(false)
            .asSequence()
            .filter { oldConfig.contains(it) }
            .map { it to oldConfig[it] }
            .forEach { conf.getConfigurationSection("messages")!!.set(it.first, it.second) }

        config.set("config-version", 5)
        config.save(configFile)
    }

    private fun updateTo6() {
        debug("Upgrading config from version 5 to version 6")

        val configFile = File(dataFolder, "config.yml")
        val oldFile = File(dataFolder, "config-old-5.yml")

        configFile.renameTo(oldFile)
        plugin.saveDefaultConfig()

        val conf = PrettyConfig(configFile)
        plugin.conf = conf

        val disabledWorlds = conf.get("scoreboard.disabled-worlds")
        val oldConfig = YamlConfiguration.loadConfiguration(oldFile.reader())

        conf.getKeys(false)
            .asSequence()
            .filter { it != "config-version" && it != "messages" }
            .filter { oldConfig.contains(it) }
            .map { it to oldConfig[it] }
            .forEach { conf.set(it.first, it.second) }

        conf.getConfigurationSection("messages")!!.getKeys(false)
            .asSequence()
            .filter { oldConfig.contains(it) }
            .map { it to oldConfig[it] }
            .forEach { conf.getConfigurationSection("messages")!!.set(it.first, it.second) }

        conf.set("scoreboard.disabled-worlds", disabledWorlds)

        config.set("config-version", 6)
        config.save(configFile)
    }

    private fun updateTo7() {
        debug("Upgrading config from version 6 to version 7")

        val configFile = File(dataFolder, "config.yml")
        val oldFile = File(dataFolder, "config-old-6.yml")

        configFile.renameTo(oldFile)
        plugin.saveDefaultConfig()

        val conf = PrettyConfig(configFile)
        plugin.conf = conf

        val titleDelay = conf.getLong("welcome-title.delay")
        val actionbarDelay = conf.getLong("welcome-actionbar.delay")
        val oldConfig = YamlConfiguration.loadConfiguration(oldFile.reader())

        val hooks = oldConfig.get("hooks") ?: conf.get("hooks")

        conf.getKeys(false)
            .asSequence()
            .filter { it != "config-version" && it != "messages" }
            .filter { oldConfig.contains(it) }
            .map { it to oldConfig[it] }
            .forEach { conf.set(it.first, it.second) }

        conf.getConfigurationSection("messages")!!.getKeys(false)
            .asSequence()
            .filter { oldConfig.contains(it) }
            .map { it to oldConfig[it] }
            .forEach { conf.getConfigurationSection("messages")!!.set(it.first, it.second) }

        conf.set("welcome-title.delay", titleDelay)
        conf.set("welcome-actionbar.delay", actionbarDelay)
        conf.set("hooks", hooks)

        config.set("config-version", 7)
        config.save(configFile)
    }
}
