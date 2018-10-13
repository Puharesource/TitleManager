package io.puharesource.mc.titlemanager

import com.google.common.base.Joiner
import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.bungeecord.BungeeCordManager
import io.puharesource.mc.titlemanager.commands.TMCommand
import io.puharesource.mc.titlemanager.config.PrettyConfig
import io.puharesource.mc.titlemanager.event.observeEvent
import io.puharesource.mc.titlemanager.event.observeEventRaw
import io.puharesource.mc.titlemanager.extensions.color
import io.puharesource.mc.titlemanager.extensions.format
import io.puharesource.mc.titlemanager.extensions.getFormattedTime
import io.puharesource.mc.titlemanager.extensions.getStringWithMultilines
import io.puharesource.mc.titlemanager.extensions.giveScoreboard
import io.puharesource.mc.titlemanager.extensions.isInt
import io.puharesource.mc.titlemanager.extensions.removeScoreboard
import io.puharesource.mc.titlemanager.extensions.sendActionbar
import io.puharesource.mc.titlemanager.extensions.sendSubtitle
import io.puharesource.mc.titlemanager.extensions.sendTitle
import io.puharesource.mc.titlemanager.extensions.stripColor
import io.puharesource.mc.titlemanager.placeholder.PlaceholderTps
import io.puharesource.mc.titlemanager.placeholder.VanishHookReplacer
import io.puharesource.mc.titlemanager.placeholder.VaultHook
import io.puharesource.mc.titlemanager.playerinfo.PlayerInfoDB
import io.puharesource.mc.titlemanager.reflections.NMSManager
import io.puharesource.mc.titlemanager.reflections.getPing
import io.puharesource.mc.titlemanager.scheduling.AsyncScheduler
import io.puharesource.mc.titlemanager.scoreboard.ScoreboardManager
import io.puharesource.mc.titlemanager.script.GraalScriptManager
import io.puharesource.mc.titlemanager.script.ScriptManager
import io.puharesource.mc.titlemanager.web.UpdateChecker
import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class TitleManagerPlugin : JavaPlugin(), TitleManagerAPI by APIProvider {
    private val animationsFolder = File(dataFolder, "animations")
    private var conf : PrettyConfig? = null
    var playerInfoDB: PlayerInfoDB? = null

    override fun onEnable() {
        debug("Save default config")
        saveDefaultConfig()

        debug("Adding script files")
        addFiles()

        debug("Updating config from 1.5.13 to 2.0.0")
        updateConfig()

        debug("Setting up player info database")
        setupDB()

        debug("Loading animations & scripts")
        loadAnimations()

        debug("Registering listeners")
        registerListeners()

        debug("Registering commands")
        registerCommands()

        debug("Registering placeholders")
        registerPlaceholders()

        debug("Registering BungeeCord messengers")
        registerBungeeCord()

        debug("Registering Announcers")
        registerAnnouncers()

        debug("Using MC version: ${NMSManager.serverVersion} | NMS Index: ${NMSManager.versionIndex}")

        startPlayerTasks()
    }

    override fun onDisable() {
        AsyncScheduler.cancelAll()

        server.onlinePlayers.forEach {
            APIProvider.removeAllRunningAnimations(it)
            it.removeScoreboard()
        }

        ScoreboardManager.playerScoreboards.clear()
        ScoreboardManager.playerScoreboardUpdateTasks.clear()
    }

    // Override default config methods.
    override fun getConfig() : FileConfiguration {
        if (conf == null) {
            conf = PrettyConfig(File(dataFolder, "config.yml"))
        }

        return conf!!
    }
    override fun saveConfig() = config.save(File(dataFolder, "config.yml"))
    override fun reloadConfig() {
        conf = PrettyConfig(File(dataFolder, "config.yml"))
    }

    fun reloadPlugin() {
        UpdateChecker.stop()

        onDisable()

        saveDefaultConfig()
        reloadConfig()

        APIProvider.registeredAnimations.clear()
        APIProvider.scriptManager = ScriptManager.create()

        addFiles()
        loadAnimations()
        registerAnnouncers()

        if (config.getBoolean("check-for-updates")) {
            UpdateChecker.start()
        }

        startPlayerTasks()
    }

    private fun startPlayerTasks() {
        val playerListSection = config.getConfigurationSection("player-list")
        val header = toAnimationParts(playerListSection.getStringWithMultilines("header").color())
        val footer = toAnimationParts(playerListSection.getStringWithMultilines("footer").color())

        val scoreboardSection = config.getConfigurationSection("scoreboard")
        val title = toAnimationParts(scoreboardSection.getString("title").color())
        val lines = scoreboardSection.getStringList("lines").take(15).map { toAnimationParts(it.color()) }

        server.onlinePlayers.forEach {
            if (playerListSection.getBoolean("enabled")) {
                toHeaderAnimation(header, it, withPlaceholders = true).start()
                toFooterAnimation(footer, it, withPlaceholders = true).start()
            }

            if (scoreboardSection.getBoolean("enabled")) {
                if (playerInfoDB!!.isScoreboardToggled(it)) {
                    it.giveScoreboard()

                    toScoreboardTitleAnimation(title, it, true).start()

                    lines.forEachIndexed { index, parts ->
                        toScoreboardValueAnimation(parts, it, index + 1, true).start()
                    }
                }
            }
        }
    }

    private fun updateConfig() {
        val version = config.getInt("config-version")

        if (version <= 3) {
            debug("Upgrading config to the 2.0 format.")

            val configFile = File(dataFolder, "config.yml")
            val oldFile = File(dataFolder, "config-old-3.yml")
            val oldAnimationFile = File(dataFolder, "animations.yml")

            configFile.renameTo(oldFile)
            saveDefaultConfig()

            conf = PrettyConfig(configFile)
            val oldConfig = YamlConfiguration.loadConfiguration(oldFile.reader())

            val oldAnimationPattern = "(?i)^animation[:](.+)$".toRegex()
            val oldPlaceholderPattern = "\\{(.+)}".toRegex()

            fun move(path: String, newPath: String? = null, transformer: ((Any) -> Any)? = null) {
                val correctNewPath = newPath ?: path

                if (oldConfig.contains(path)) {
                    var oldValue = oldConfig.get(path)

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

            move("usingConfig", "using-config")
            move("using-bungeecord")
            move("legacy-client-support")
            move("updater.check-automatically", "check-for-updates")

            move("tabmenu.enabled", "player-list.enabled")
            move("tabmenu.header", "player-list.header", transformer = {
                val header = it as String

                if (header.contains("\\n")) {
                    header.split("\\n")
                } else {
                    header
                }
            })
            move("tabmenu.footer", "player-list.footer", transformer = {
                val footer = it as String

                if (footer.contains("\\n")) {
                    footer.split("\\n")
                } else {
                    footer
                }
            })

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
                        .forEach {
                            val name = it.first
                            val frames = Joiner.on("\n")
                                    .join(it.second)
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
        } else if (version == 4) {
            debug("Upgrading config from version 4 to version 5")

            val configFile = File(dataFolder, "config.yml")
            val oldFile = File(dataFolder, "config-old-4.yml")

            configFile.renameTo(oldFile)
            saveDefaultConfig()

            conf = PrettyConfig(configFile)
            val oldConfig = YamlConfiguration.loadConfiguration(oldFile.reader())

            conf!!.getKeys(false)
                    .asSequence()
                    .filter { it != "config-version" && it != "messages" }
                    .filter { oldConfig.contains(it) }
                    .map { it to oldConfig[it] }
                    .forEach { conf!!.set(it.first, it.second) }

            conf!!.getConfigurationSection("messages").getKeys(false)
                    .asSequence()
                    .filter { oldConfig.contains(it) }
                    .map { it to oldConfig[it] }
                    .forEach { conf!!.getConfigurationSection("messages").set(it.first, it.second) }

            config.save(configFile)
        }
    }

    private fun setupDB() {
        playerInfoDB = PlayerInfoDB(File(dataFolder, "playerinfo.sqlite"), createStatement = getTextResource("playerinfo.sql").readText())
    }

    private fun registerAnnouncers() {
        var section : ConfigurationSection = config.getConfigurationSection("announcer")

        if (!config.getBoolean("using-config")) return
        if (!section.getBoolean("enabled")) return

        section = section.getConfigurationSection("announcements")

        section.getKeys(false)
                .forEach {
                    val announcement = section.getConfigurationSection(it)

                    val interval = announcement.getInt("interval", 60)

                    val fadeIn = announcement.getInt("timings.fade-in", 20)
                    val stay = announcement.getInt("timings.stay", 40)
                    val fadeOut = announcement.getInt("timings.fade-out", 20)

                    val titles : List<String> = announcement.getStringList("titles") ?: listOf()
                    val actionbarTitles : List<String> = announcement.getStringList("actionbar") ?: listOf()

                    val size = if (titles.size > actionbarTitles.size) titles.size else actionbarTitles.size
                    val index = AtomicInteger(0)

                    debug("Registering announcement: $it")
                    debug("Announcement Info:")
                    debug("Interval: $interval")
                    debug("Fade in: $fadeIn")
                    debug("Stay: $stay")
                    debug("Fade out: $fadeOut")
                    debug("Titles: ${titles.size}")
                    debug("Actionbar Titles: ${actionbarTitles.size}")
                    debug("Size: $size")

                    if (size != 0) {
                        AsyncScheduler.scheduleRaw({
                            debug("Sending announcement: $it")

                            val i = index.andIncrement % size

                            server.onlinePlayers.forEach {
                                if (i < titles.size) {
                                    val title = titles[i].color().split("\\n", limit = 2)

                                    if (title.first().isNotEmpty() && title[1].isEmpty()) {
                                        it.sendTitleFromText(title.first(), fadeIn, stay, fadeOut)
                                    } else if (title.first().isEmpty() && title[1].isNotEmpty()) {
                                        it.sendSubtitleFromText(title[1], fadeIn, stay, fadeOut)
                                    } else {
                                        it.sendTitleFromText(title.first(), fadeIn, stay, fadeOut)
                                        it.sendSubtitleFromText(title[1], fadeIn, stay, fadeOut)
                                    }
                                }

                                if (i < actionbarTitles.size) {
                                    it.sendActionbarFromText(actionbarTitles[i].color())
                                }
                            }
                        }, interval, interval, TimeUnit.SECONDS)
                    }
                }
    }

    private fun registerBungeeCord() {
        server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")
    }

    private fun addFiles() {
        if (!animationsFolder.exists()) {
            animationsFolder.mkdir()

            fun saveAnimationFiles(fileName: String) {
                File(animationsFolder, fileName).writeBytes(getResource("animations/$fileName").readBytes())
            }

            // Text based animations
            saveAnimationFiles("left-to-right.txt")
            saveAnimationFiles("right-to-left.txt")
        }
    }

    private fun loadAnimations() {
        // Load text based animations
        animationsFolder.listFiles()
                .asSequence()
                .filter { it.isFile }
                .filter { it.extension.equals("txt", ignoreCase = true) }
                .forEach { APIProvider.registeredAnimations[it.nameWithoutExtension] = fromTextFile(it) }

        APIProvider.scriptManager = ScriptManager.create() ?: return

        // Load JavaScript based animations
        animationsFolder.listFiles()
                .asSequence()
                .filter { it.isFile }
                .filter { it.extension.equals("js", ignoreCase = true) }
                .forEach {
                    val name = it.nameWithoutExtension

                    APIProvider.scriptManager!!.addScript(name, it)
                }
    }

    private fun registerListeners() {
        // Notify administrators joining the server of the update.
        observeEvent<PlayerJoinEvent>()
                .filter { config.getBoolean("check-for-updates") }
                .filter { UpdateChecker.isUpdateAvailable() }
                .map { it.player }
                .filter { it.hasPermission("titlemanager.update.notify") }
                .subscribe {
                    it.sendMessage("${ChatColor.WHITE}[${ChatColor.GOLD}TitleManager${ChatColor.WHITE}] ${ChatColor.YELLOW}An update was found!")
                    it.sendMessage("${ChatColor.YELLOW}You're currently on version ${UpdateChecker.getCurrentVersion()} while ${UpdateChecker.getLatestVersion()} is available.")
                    it.sendMessage("${ChatColor.YELLOW}Download it here:${ChatColor.GOLD}${ChatColor.UNDERLINE} http://www.spigotmc.org/resources/titlemanager.1049")
                }

        // Welcome title message
        observeEvent<PlayerJoinEvent>()
                .observeOn(asyncScheduler)
                .subscribeOn(asyncScheduler)
                .filter { config.getBoolean("using-config") }
                .filter { config.getBoolean("welcome-title.enabled") }
                .map { it.player }
                .delay(1, TimeUnit.SECONDS)
                .filter { it.isOnline }
                .subscribe {
                    val section = config.getConfigurationSection("welcome-title")

                    if (it.hasPlayedBefore()) {
                        it.sendTitleFromText(
                                section.getString("title").color(),
                                section.getInt("fade-in"),
                                section.getInt("stay"),
                                section.getInt("fade-out"))

                        it.sendSubtitleFromText(
                                section.getString("subtitle").color(),
                                section.getInt("fade-in"),
                                section.getInt("stay"),
                                section.getInt("fade-out"))
                    } else {
                        it.sendTitleFromText(
                                section.getString("first-join.title").color(),
                                section.getInt("fade-in"),
                                section.getInt("stay"),
                                section.getInt("fade-out"))

                        it.sendSubtitleFromText(
                                section.getString("first-join.subtitle").color(),
                                section.getInt("fade-in"),
                                section.getInt("stay"),
                                section.getInt("fade-out"))
                    }
                }

        // Welcome actionbar message
        observeEventRaw<PlayerJoinEvent>()
                .observeOn(asyncScheduler)
                .subscribeOn(asyncScheduler)
                .filter { config.getBoolean("using-config") }
                .filter { config.getBoolean("welcome-actionbar.enabled") }
                .map { it.player }
                .delay(1, TimeUnit.SECONDS)
                .filter { it.isOnline }
                .subscribe {
                    val section = config.getConfigurationSection("welcome-actionbar")

                    if (it.hasPlayedBefore()) {
                        it.sendActionbarFromText(section.getString("title").color())
                    } else {
                        it.sendActionbarFromText(section.getString("first-join").color())
                    }
                }

        // Set header and footer
        observeEvent<PlayerJoinEvent>()
                .filter { config.getBoolean("using-config") }
                .filter { config.getBoolean("player-list.enabled") }
                .map { it.player }
                .subscribe {
                    val section = config.getConfigurationSection("player-list")

                    it.setHeaderFromText(section.getStringWithMultilines("header").color())
                    it.setFooterFromText(section.getStringWithMultilines("footer").color())
                }

        // Set scoreboard
        observeEvent<PlayerJoinEvent>()
                .filter { config.getBoolean("using-config") }
                .filter { config.getBoolean("scoreboard.enabled") }
                .map { it.player }
                .filter { playerInfoDB!!.isScoreboardToggled(it) }
                .subscribe {
                    val section = config.getConfigurationSection("scoreboard")

                    val title = toAnimationParts(section.getString("title").color())
                    val lines = section.getStringList("lines").take(15).map { toAnimationParts(it.color()) }

                    it.giveScoreboard()
                    toScoreboardTitleAnimation(title, it, true).start()

                    lines.forEachIndexed { index, parts ->
                        toScoreboardValueAnimation(parts, it, index + 1, true).start()
                    }
                }

        // Delete players from the scoreboard cache when they quit the server
        observeEvent<PlayerQuitEvent>()
                .map { it.player }
                .filter { APIProvider.hasScoreboard(it) }
                .subscribe {
                    ScoreboardManager.playerScoreboards.remove(it)
                    ScoreboardManager.stopUpdateTask(it)
                }

        // End all running animations when they quit the server
        observeEvent<PlayerQuitEvent>()
                .map { it.player }
                .subscribe { APIProvider.removeAllRunningAnimations(it) }
    }

    private fun registerCommands() {
        val cmd = getCommand("tm")

        cmd.executor = TMCommand
        cmd.tabCompleter = TMCommand
    }

    private fun registerPlaceholders() {
        APIProvider.addPlaceholderReplacer("player", { it.name }, "username", "name")
        APIProvider.addPlaceholderReplacer("displayname", { it.displayName }, "display-name", "nickname", "nick")
        APIProvider.addPlaceholderReplacer("strippeddisplayname", { it.displayName.stripColor() }, "strippeddisplayname", "stripped-displayname", "stripped-nickname", "stripped-nick")
        APIProvider.addPlaceholderReplacer("world", { it.world.name }, "world-name")
        APIProvider.addPlaceholderReplacer("world-time", { it.world.time.toString() })
        APIProvider.addPlaceholderReplacer("24h-world-time", { it.world.getFormattedTime(true) })
        APIProvider.addPlaceholderReplacer("12h-world-time", { it.world.getFormattedTime(false) })
        APIProvider.addPlaceholderReplacer("online", { server.onlinePlayers.size.toString() }, "online-players")
        APIProvider.addPlaceholderReplacer("max", { server.maxPlayers.toString() }, "max-players")
        APIProvider.addPlaceholderReplacer("world-players", { it.world.players.size.toString() }, "world-online")
        APIProvider.addPlaceholderReplacer("ping", { it.getPing().toString() })
        APIProvider.addPlaceholderReplacer("tps", { PlaceholderTps.getTps(1) })

        APIProvider.addPlaceholderReplacer("server-time", {
            val format = config.getString("placeholders.date-format")
            val date = Date(System.currentTimeMillis())
            val locale = Locale.forLanguageTag(config.getString("locale"))

            return@addPlaceholderReplacer SimpleDateFormat(format, locale).format(date)
        })

        APIProvider.addPlaceholderReplacerWithValue("tps", replacer@ { _, value ->
            if (value.isInt()) {
                return@replacer PlaceholderTps.getTps(value.toInt())
            }

            return@replacer PlaceholderTps.getTps(value)
        })

        if (config.getBoolean("using-bungeecord")) {
            APIProvider.addPlaceholderReplacer("bungeecord-online", { BungeeCordManager.onlinePlayers.toString() }, "bungeecord-online-players")
            APIProvider.addPlaceholderReplacer("server", { BungeeCordManager.getCurrentServer().orEmpty() }, "server-name")

            APIProvider.addPlaceholderReplacerWithValue("online", replacer@ { _, value ->
                if (value.contains(",")) {
                    return@replacer value.split(",").mapNotNull { BungeeCordManager.getServers()[value]?.playerCount }.sum().toString()
                }

                return@replacer BungeeCordManager.getServers()[value]?.playerCount?.toString() ?: ""
            }, "online-players")
        }

        if (VanishHookReplacer.isValid()) {
            APIProvider.addPlaceholderReplacer("safe-online", { VanishHookReplacer.value(it) }, "safe-online-players")
        }

        if (VaultHook.isEnabled()) {
            if (VaultHook.economySupported) {
                APIProvider.addPlaceholderReplacer("balance", { VaultHook.economy!!.getBalance(it).format() }, "money")
            }

            if (VaultHook.hasGroupSupport()) {
                APIProvider.addPlaceholderReplacer("group", { VaultHook.permissions!!.getPrimaryGroup(it) }, "group-name")
            }
        }
    }

    private fun Player.sendTitleFromText(text: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        val parts = toAnimationParts(text)

        if (parts.size == 1 && parts.first().part is String) {
            sendTitle(
                    title = parts.first().part as String,
                    fadeIn = fadeIn,
                    stay = stay,
                    fadeOut = fadeOut,
                    withPlaceholders = true)
        } else if (parts.size == 1 && parts.first().part is Animation) {
            toTitleAnimation(parts.first().part as Animation, this, withPlaceholders = true).start()
        } else if (parts.isNotEmpty()) {
            toTitleAnimation(parts, this, withPlaceholders = true).start()
        }
    }

    private fun Player.sendSubtitleFromText(text: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        val parts = toAnimationParts(text)

        if (parts.size == 1 && parts.first().part is String) {
            sendSubtitle(
                    subtitle = parts.first().part as String,
                    fadeIn = fadeIn,
                    stay = stay,
                    fadeOut = fadeOut,
                    withPlaceholders = true)
        } else if (parts.size == 1 && parts.first().part is Animation) {
            toSubtitleAnimation(parts.first().part as Animation, this, withPlaceholders = true).start()
        } else if (parts.isNotEmpty()) {
            toSubtitleAnimation(parts, this, withPlaceholders = true).start()
        }
    }

    private fun Player.sendActionbarFromText(text: String) {
        val parts = toAnimationParts(text)

        if (parts.size == 1 && parts.first().part is String) {
            sendActionbar(
                    text = parts.first().part as String,
                    withPlaceholders = true)
        } else if (parts.size == 1 && parts.first().part is Animation) {
            toActionbarAnimation(parts.first().part as Animation, this, withPlaceholders = true).start()
        } else if (parts.isNotEmpty()) {
            toActionbarAnimation(parts, this, withPlaceholders = true).start()
        }
    }

    private fun Player.setHeaderFromText(text: String) {
        val parts = toAnimationParts(text)

        toHeaderAnimation(parts, this, withPlaceholders = true).start()
    }

    private fun Player.setFooterFromText(text: String) {
        val parts = toAnimationParts(text)

        toFooterAnimation(parts, this, withPlaceholders = true).start()
    }
}