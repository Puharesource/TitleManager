package io.puharesource.mc.titlemanager

import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.internal.APIProvider
import io.puharesource.mc.titlemanager.internal.functionality.bungeecord.BungeeCordManager
import io.puharesource.mc.titlemanager.internal.functionality.commands.TMCommand
import io.puharesource.mc.titlemanager.internal.config.ConfigMigration
import io.puharesource.mc.titlemanager.internal.config.PrettyConfig
import io.puharesource.mc.titlemanager.internal.config.TMConfigMain
import io.puharesource.mc.titlemanager.internal.functionality.event.listenEventSync
import io.puharesource.mc.titlemanager.internal.extensions.color
import io.puharesource.mc.titlemanager.internal.extensions.getFormattedTime
import io.puharesource.mc.titlemanager.internal.extensions.giveScoreboard
import io.puharesource.mc.titlemanager.internal.extensions.isInt
import io.puharesource.mc.titlemanager.internal.extensions.removeScoreboard
import io.puharesource.mc.titlemanager.internal.extensions.sendActionbar
import io.puharesource.mc.titlemanager.internal.extensions.sendSubtitle
import io.puharesource.mc.titlemanager.internal.extensions.sendTitle
import io.puharesource.mc.titlemanager.internal.extensions.sendTitles
import io.puharesource.mc.titlemanager.internal.extensions.stripColor
import io.puharesource.mc.titlemanager.internal.debug
import io.puharesource.mc.titlemanager.internal.extensions.format
import io.puharesource.mc.titlemanager.internal.functionality.event.TMEventListener
import io.puharesource.mc.titlemanager.internal.functionality.event.listenEventAsync
import io.puharesource.mc.titlemanager.internal.functionality.placeholder.PlaceholderTps
import io.puharesource.mc.titlemanager.internal.functionality.placeholder.VanishHookReplacer
import io.puharesource.mc.titlemanager.internal.functionality.placeholder.VaultHook
import io.puharesource.mc.titlemanager.internal.functionality.placeholder.createPlaceholder
import io.puharesource.mc.titlemanager.internal.playerinfo.PlayerInfoDB
import io.puharesource.mc.titlemanager.internal.reflections.NMSManager
import io.puharesource.mc.titlemanager.internal.reflections.getPing
import io.puharesource.mc.titlemanager.internal.scheduling.AsyncScheduler
import io.puharesource.mc.titlemanager.internal.functionality.scoreboard.ScoreboardManager
import io.puharesource.mc.titlemanager.internal.script.ScriptManager
import io.puharesource.mc.titlemanager.internal.web.UpdateChecker
import org.bukkit.ChatColor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.Date
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class TitleManagerPlugin : JavaPlugin(), TitleManagerAPI by APIProvider {
    internal val animationsFolder = File(dataFolder, "animations")
    internal var conf : PrettyConfig? = null
    var playerInfoDB: PlayerInfoDB? = null
    private var bungeeCordManager: BungeeCordManager? = null
    internal lateinit var tmConfig: TMConfigMain
    private val listeners: MutableSet<TMEventListener<*>> = mutableSetOf()

    override fun onEnable() {
        saveDefaultConfig()
        updateConfig()

        debug("Adding script files")
        addFiles()

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

        if (tmConfig.usingBungeecord) {
            debug("Creating BungeeCord manager")
            bungeeCordManager = BungeeCordManager()
        }

        startPlayerTasks()
    }

    override fun onDisable() {
        AsyncScheduler.cancelAll()

        server.onlinePlayers.forEach {
            APIProvider.removeAllRunningAnimations(it)
            it.removeScoreboard()
            APIProvider.clearHeaderAndFooterCache(it)
        }

        ScoreboardManager.playerScoreboards.clear()
        ScoreboardManager.playerScoreboardUpdateTasks.clear()
    }

    // Override default config methods.
    override fun getConfig() : FileConfiguration {
        if (conf == null) {
            reloadConfig()
        }

        return conf!!
    }
    override fun saveConfig() = config.save(File(dataFolder, "config.yml"))
    override fun reloadConfig() {
        conf = PrettyConfig(File(dataFolder, "config.yml"))
        tmConfig = TMConfigMain(conf!!)
    }

    fun reloadPlugin() {
        UpdateChecker.stop()

        onDisable()
        unregisterListeners()

        if (bungeeCordManager != null) {
            bungeeCordManager!!.invalidate()
            bungeeCordManager = null
        }

        APIProvider.registeredAnimations.clear()

        saveDefaultConfig()
        reloadConfig()

        APIProvider.scriptManager = ScriptManager.create()

        addFiles()
        loadAnimations()
        registerListeners()
        registerAnnouncers()

        if (tmConfig.checkForUpdates) {
            UpdateChecker.start()
        }

        if (tmConfig.usingBungeecord) {
            bungeeCordManager = BungeeCordManager()
        }

        startPlayerTasks()
    }

    private fun startPlayerTasks() {
        val header = toAnimationParts(tmConfig.playerList.header.color())
        val footer = toAnimationParts(tmConfig.playerList.footer.color())

        val title = toAnimationParts(tmConfig.scoreboard.title.color())
        val lines = tmConfig.scoreboard.lines.asSequence().take(15).map { toAnimationParts(it.color()) }.toList()

        server.onlinePlayers.forEach {
            if (tmConfig.playerList.enabled) {
                toHeaderAnimation(header, it, withPlaceholders = true).start()
                toFooterAnimation(footer, it, withPlaceholders = true).start()
            }

            if (tmConfig.scoreboard.enabled) {
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
        val migration = ConfigMigration(this)

        migration.updateConfig()
    }

    private fun setupDB() {
        playerInfoDB = PlayerInfoDB(File(dataFolder, "playerinfo.sqlite"), createStatement = getTextResource("playerinfo.sql")!!.readText())
    }

    private fun registerAnnouncers() {
        if (!tmConfig.usingConfig) return
        if (!tmConfig.announcer.enabled) return

        tmConfig.announcer.announcements
                .forEach { announcement ->
                    val interval = announcement.getInt("interval", 60)

                    val fadeIn = announcement.getInt("timings.fade-in", 20)
                    val stay = announcement.getInt("timings.stay", 40)
                    val fadeOut = announcement.getInt("timings.fade-out", 20)

                    val titles : List<String> = announcement.getStringList("titles")
                    val actionbarTitles : List<String> = announcement.getStringList("actionbar")

                    val size = if (titles.size > actionbarTitles.size) titles.size else actionbarTitles.size
                    val index = AtomicInteger(0)

                    debug("Registering announcement: ${announcement.name}")
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
                            debug("Sending announcement: ${announcement.name}")

                            val i = index.andIncrement % size

                            server.onlinePlayers.forEach {
                                if (i < titles.size) {
                                    val title = titles[i].color().split("\\n", limit = 2)

                                    if (title.first().isNotEmpty() && title[1].isEmpty()) {
                                        it.sendTitleFromText(title.first(), fadeIn, stay, fadeOut)
                                    } else if (title.first().isEmpty() && title[1].isNotEmpty()) {
                                        it.sendSubtitleFromText(title[1], fadeIn, stay, fadeOut)
                                    } else {
                                        it.sendTitles(title.first(), title[1], fadeIn, stay, fadeOut)
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
                File(animationsFolder, fileName).writeBytes(getResource("animations/$fileName")!!.readBytes())
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
        if (tmConfig.checkForUpdates) {
            listenEventSync<PlayerJoinEvent> {
                if (!UpdateChecker.isUpdateAvailable()) return@listenEventSync

                val player = it.player

                if (!player.hasPermission("titlemanager.update.notify")) return@listenEventSync

                player.sendMessage("${ChatColor.WHITE}[${ChatColor.GOLD}TitleManager${ChatColor.WHITE}] ${ChatColor.YELLOW}An update was found!")
                player.sendMessage("${ChatColor.YELLOW}You're currently on version ${UpdateChecker.getCurrentVersion()} while ${UpdateChecker.getLatestVersion()} is available.")
                player.sendMessage("${ChatColor.YELLOW}Download it here:${ChatColor.GOLD}${ChatColor.UNDERLINE} http://www.spigotmc.org/resources/titlemanager.1049")
            }.addTo(listeners)
        }

        // Delete players from player list cache when they quit the server
        listenEventSync<PlayerQuitEvent> { APIProvider.clearHeaderAndFooterCache(it.player) }.addTo(listeners)

        // Delete players from the scoreboard cache when they quit the server
        listenEventSync<PlayerQuitEvent> {
            val player = it.player

            if (hasScoreboard(player)) {
                ScoreboardManager.playerScoreboards.remove(player)
                ScoreboardManager.stopUpdateTask(player)
            }
        }.addTo(listeners)

        // End all running animations when they quit the server
        listenEventSync<PlayerQuitEvent> { APIProvider.removeAllRunningAnimations(it.player) }.addTo(listeners)

        if (tmConfig.usingConfig) {
            // Welcome title message
            if (tmConfig.welcomeTitle.enabled) {
                listenEventAsync<PlayerJoinEvent> {
                    val player = it.player

                    if (!player.isOnline) return@listenEventAsync

                    val welcomeTitle = tmConfig.welcomeTitle

                    if (player.hasPlayedBefore()) {
                        player.sendTitles(welcomeTitle.title.color(), welcomeTitle.subtitle.color(), welcomeTitle.fadeIn, welcomeTitle.stay, welcomeTitle.fadeOut)
                    } else {
                        player.sendTitles(welcomeTitle.firstJoin.title.color(), welcomeTitle.firstJoin.subtitle.color(), welcomeTitle.fadeIn, welcomeTitle.stay, welcomeTitle.fadeOut)
                    }
                }.delay(20).addTo(listeners)
            }

            // Welcome actionbar message
            if (tmConfig.welcomeActionbar.enabled) {
                listenEventAsync<PlayerJoinEvent> {
                    val player = it.player

                    if (!player.isOnline) return@listenEventAsync

                    if (player.hasPlayedBefore()) {
                        player.sendActionbarFromText(tmConfig.welcomeActionbar.title.color())
                    } else {
                        player.sendActionbarFromText(tmConfig.welcomeActionbar.firstJoin.color())
                    }
                }.delay(20).addTo(listeners)
            }

            // Set header and footer
            if (tmConfig.playerList.enabled) {
                listenEventSync<PlayerJoinEvent> {
                    val player = it.player

                    player.setHeaderFromText(tmConfig.playerList.header.color())
                    player.setFooterFromText(tmConfig.playerList.footer.color())
                }.addTo(listeners)
            }

            // Set scoreboard
            if (tmConfig.scoreboard.enabled) {
                listenEventSync<PlayerJoinEvent> {
                    val player = it.player

                    if (!playerInfoDB!!.isScoreboardToggled(player)) return@listenEventSync

                    val title = toAnimationParts(tmConfig.scoreboard.title.color())
                    val lines = tmConfig.scoreboard.lines.take(15).map { toAnimationParts(it.color()) }

                    player.giveScoreboard()
                    toScoreboardTitleAnimation(title, player, true).start()

                    lines.forEachIndexed { index, parts ->
                        toScoreboardValueAnimation(parts, player, index + 1, true).start()
                    }
                }.addTo(listeners)
            }
        }
    }

    private fun unregisterListeners() {
        this.listeners.forEach { it.invalidate() }
        this.listeners.clear()
    }

    private fun registerCommands() {
        val cmd = getCommand("tm")!!

        cmd.setExecutor(TMCommand)
        cmd.tabCompleter = TMCommand
    }

    private fun registerPlaceholders() {
        APIProvider.addPlaceholder(createPlaceholder("player", "username", "name") { player -> player.name })
        APIProvider.addPlaceholder(createPlaceholder("displayname", "display-name", "nickname", "nick") { player -> player.displayName })
        APIProvider.addPlaceholder(createPlaceholder("strippeddisplayname", "stripped-displayname", "stripped-nickname", "stripped-nick") { player -> player.displayName.stripColor() })
        APIProvider.addPlaceholder(createPlaceholder("world", "world-name") { player -> player.world.name })
        APIProvider.addPlaceholder(createPlaceholder("world-time") { player -> player.world.time })
        APIProvider.addPlaceholder(createPlaceholder("24h-world-time") { player -> player.world.getFormattedTime(true) })
        APIProvider.addPlaceholder(createPlaceholder("12h-world-time") { player -> player.world.getFormattedTime(false) })
        APIProvider.addPlaceholder(createPlaceholder("online", "online-players") { _, value ->
            if (value == null || !tmConfig.usingBungeecord || bungeeCordManager == null) {
                return@createPlaceholder server.onlinePlayers.size
            }

            if (value.contains(",")) {
                return@createPlaceholder value.split(",").asSequence().mapNotNull { bungeeCordManager!!.getServers()[value]?.playerCount }.sum().toString()
            }

            return@createPlaceholder bungeeCordManager!!.getServers()[value]?.playerCount?.toString() ?: ""
        })
        APIProvider.addPlaceholder(createPlaceholder("max", "max-players") { _ -> server.maxPlayers })
        APIProvider.addPlaceholder(createPlaceholder("world-players", "world-online") { player -> player.world.players.size })
        APIProvider.addPlaceholder(createPlaceholder("ping") { player -> player.getPing() })
        APIProvider.addPlaceholder(createPlaceholder("tps") { _, value ->
            if (value == null) {
                return@createPlaceholder PlaceholderTps.getTps(1)
            }

            if (value.isInt()) {
                return@createPlaceholder PlaceholderTps.getTps(value.toInt())
            }

            return@createPlaceholder PlaceholderTps.getTps(value)
        }.cached(30))
        APIProvider.addPlaceholder(createPlaceholder("server-time") { _ -> tmConfig.placeholders.dateFormat.format(Date(System.currentTimeMillis())) })
        APIProvider.addPlaceholder(createPlaceholder("bungeecord-online", "bungeecord-online-players", enabled = { tmConfig.usingBungeecord && bungeeCordManager != null }) { _ -> bungeeCordManager!!.onlinePlayers }.cached(5))
        APIProvider.addPlaceholder(createPlaceholder("server", "server-name", enabled = { tmConfig.usingBungeecord && bungeeCordManager != null }) { _ -> bungeeCordManager!!.getCurrentServer().orEmpty() })
        APIProvider.addPlaceholder(createPlaceholder("safe-online", "safe-online-players", enabled = { VanishHookReplacer.isValid() }) { player -> VanishHookReplacer.value(player) })
        APIProvider.addPlaceholder(createPlaceholder("balance", "money", enabled = { VaultHook.isEnabled() && VaultHook.isEconomySupported }) { player -> VaultHook.economy!!.getBalance(player).format() })
        APIProvider.addPlaceholder(createPlaceholder("group", "group-name", enabled = { VaultHook.isEnabled() && VaultHook.hasGroupSupport }) { player -> VaultHook.permissions!!.getPrimaryGroup(player) })
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