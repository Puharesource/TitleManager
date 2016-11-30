package io.puharesource.mc.titlemanager

import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.bungeecord.BungeeCordManager
import io.puharesource.mc.titlemanager.commands.TMCommand
import io.puharesource.mc.titlemanager.event.observeEvent
import io.puharesource.mc.titlemanager.event.observeEventRaw
import io.puharesource.mc.titlemanager.extensions.color
import io.puharesource.mc.titlemanager.extensions.format
import io.puharesource.mc.titlemanager.extensions.getFormattedTime
import io.puharesource.mc.titlemanager.extensions.getStringWithMultilines
import io.puharesource.mc.titlemanager.extensions.sendActionbar
import io.puharesource.mc.titlemanager.extensions.sendSubtitle
import io.puharesource.mc.titlemanager.extensions.sendTitle
import io.puharesource.mc.titlemanager.extensions.stripColor
import io.puharesource.mc.titlemanager.placeholder.VanishHookReplacer
import io.puharesource.mc.titlemanager.placeholder.VaultHook
import io.puharesource.mc.titlemanager.reflections.NMSManager
import io.puharesource.mc.titlemanager.reflections.getPing
import io.puharesource.mc.titlemanager.script.ScriptManager
import io.puharesource.mc.titlemanager.web.UpdateChecker
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class TitleManagerPlugin : JavaPlugin(), TitleManagerAPI {
    private val animationsFolder = File(dataFolder, "animations")

    override fun onEnable() {
        debug("Saving default config")
        saveDefaultConfig()

        debug("Adding script files")
        addFiles()

        debug("Registering listeners")
        registerListeners()

        debug("Registering commands")
        registerCommands()

        debug("Registering placeholders")
        registerPlaceholders()

        debug("Registering BungeeCord messengers")
        registerBungeeCord()

        debug("Using MC version: ${NMSManager.serverVersion} | NMS Index: ${NMSManager.versionIndex}")
    }

    override fun onDisable() {
        server.onlinePlayers.forEach { APIProvider.removeAllRunningAnimations(it) }
    }

    fun reloadPlugin() {
        UpdateChecker.stop()
        server.onlinePlayers.forEach { APIProvider.removeAllRunningAnimations(it) }

        reloadConfig()
        saveDefaultConfig()

        registeredAnimations.clear()
        ScriptManager.registeredScripts.clear()

        addFiles()

        if (config.getBoolean("check-for-updates")) {
            UpdateChecker.start()
        }

        val section = config.getConfigurationSection("player-list")
        val header = toAnimationParts(section.getStringWithMultilines("header").color())
        val footer = toAnimationParts(section.getStringWithMultilines("footer").color())

        server.onlinePlayers.forEach {
            toHeaderAnimation(header, it, withPlaceholders = true).start()
            toFooterAnimation(footer, it, withPlaceholders = true).start()
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

            // JavaScript based animations
            saveAnimationFiles("text_delete.js")
            saveAnimationFiles("text_write.js")
            saveAnimationFiles("count_down.js")
            saveAnimationFiles("count_up.js")
            saveAnimationFiles("shine.js")
        }

        // Load text based animations
        animationsFolder.listFiles()
                .filter { it.isFile }
                .filter { it.extension.equals("txt", ignoreCase = true) }
                .forEach { registeredAnimations.put(it.nameWithoutExtension, fromTextFile(it)) }

        // Load JavaScript based animations
        animationsFolder.listFiles()
                .filter { it.isFile }
                .filter { it.extension.equals("js", ignoreCase = true) }
                .forEach {
                    val name = it.nameWithoutExtension
                    ScriptManager.addJavaScript(it)

                    ScriptManager.registeredScripts.add(name)
                }
    }

    private fun registerListeners() {
        fun Player.sendTitleFromText(text: String, fadeIn: Int, stay: Int, fadeOut: Int) {
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

        fun Player.sendSubtitleFromText(text: String, fadeIn: Int, stay: Int, fadeOut: Int) {
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

        fun Player.sendActionbarFromText(text: String) {
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

        fun Player.setHeaderFromText(text: String) {
            val parts = toAnimationParts(text)

            toHeaderAnimation(parts, this, withPlaceholders = true).start()
        }

        fun Player.setFooterFromText(text: String) {
            val parts = toAnimationParts(text)

            toFooterAnimation(parts, this, withPlaceholders = true).start()
        }

        // Notify administrators joining the server of the update.
        observeEvent(events = PlayerJoinEvent::class.java)
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
        observeEvent(events = PlayerJoinEvent::class.java)
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
        observeEventRaw(events = PlayerJoinEvent::class.java)
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
                        it.sendActionbarFromText(section.getString("title"))
                    } else {
                        it.sendActionbarFromText(section.getString("first-join"))
                    }
                }

        // Set header and footer
        observeEvent(events = PlayerJoinEvent::class.java)
                .filter { config.getBoolean("using-config") }
                .filter { config.getBoolean("player-list.enabled") }
                .map { it.player }
                .subscribe {
                    val section = config.getConfigurationSection("player-list")

                    it.setHeaderFromText(section.getStringWithMultilines("header").color())
                    it.setFooterFromText(section.getStringWithMultilines("footer").color())
                }

        // Delete players from tab list cache when they quit the server
        observeEvent(events = PlayerQuitEvent::class.java)
                .map { it.player }
                .filter { APIProvider.playerListCache.contains(it) }
                .subscribe { APIProvider.playerListCache.remove(it) }

        // End all running animations when they quit the server
        observeEvent(events = PlayerQuitEvent::class.java)
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
        APIProvider.addPlaceholderReplacer("server-time", { SimpleDateFormat(config.getString("placeholders.date-format")).format(Date(System.currentTimeMillis())) })
        APIProvider.addPlaceholderReplacer("ping", { it.getPing().toString() })

        if (config.getBoolean("using-bungeecord")) {
            APIProvider.addPlaceholderReplacer("bungeecord-online", { BungeeCordManager.onlinePlayers.toString() }, "bungeecord-online-players")
            APIProvider.addPlaceholderReplacer("server", { BungeeCordManager.getCurrentServer().orEmpty() }, "server-name")

            APIProvider.addPlaceholderReplacerWithValue("online", { player, value -> BungeeCordManager.getServers()[value]?.playerCount?.toString() ?: "" }, "online-players")
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

    override fun replaceText(player: Player, text: String) = APIProvider.replaceText(player, text)

    override fun containsPlaceholders(text: String) = APIProvider.containsPlaceholders(text)

    override fun containsPlaceholder(text: String, placeholder: String) = APIProvider.containsPlaceholder(text, placeholder)

    override fun containsAnimations(text: String) = APIProvider.containsAnimations(text)

    override fun containsAnimation(text: String, animation: String) = APIProvider.containsAnimation(text, animation)

    override fun getRegisteredAnimations() = APIProvider.registeredAnimations

    override fun getRegisteredScripts() = APIProvider.registeredScripts

    override fun addAnimation(id: String, animation: Animation) = APIProvider.addAnimation(id, animation)

    override fun removeAnimation(id: String) = APIProvider.removeAnimation(id)

    override fun toTitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean) = APIProvider.toTitleAnimation(animation, player, withPlaceholders)

    override fun toSubtitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean) = APIProvider.toSubtitleAnimation(animation, player, withPlaceholders)

    override fun toActionbarAnimation(animation: Animation, player: Player, withPlaceholders: Boolean) = APIProvider.toActionbarAnimation(animation, player, withPlaceholders)

    override fun toHeaderAnimation(animation: Animation, player: Player, withPlaceholders: Boolean) = APIProvider.toHeaderAnimation(animation, player, withPlaceholders)

    override fun toFooterAnimation(animation: Animation, player: Player, withPlaceholders: Boolean) = APIProvider.toFooterAnimation(animation, player, withPlaceholders)

    override fun toAnimationPart(text: String) = APIProvider.toAnimationPart(text)

    override fun toAnimationPart(animation: Animation) = APIProvider.toAnimationPart(animation)

    override fun toAnimationParts(text: String) = APIProvider.toAnimationParts(text)

    override fun createAnimationFrame(text: String, fadeIn: Int, stay: Int, fadeOut: Int) = APIProvider.createAnimationFrame(text, fadeIn, stay, fadeOut)

    override fun toTitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean) = APIProvider.toTitleAnimation(parts, player, withPlaceholders)

    override fun toSubtitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean) = APIProvider.toSubtitleAnimation(parts, player, withPlaceholders)

    override fun toActionbarAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean) = APIProvider.toActionbarAnimation(parts, player, withPlaceholders)

    override fun toHeaderAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean) = APIProvider.toHeaderAnimation(parts, player, withPlaceholders)

    override fun toFooterAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean) = APIProvider.toFooterAnimation(parts, player, withPlaceholders)

    override fun fromText(vararg frames: String) = APIProvider.fromText(*frames)

    override fun fromTextFile(file: File) = APIProvider.fromTextFile(file)

    override fun fromJavaScript(name: String, input: String) = APIProvider.fromJavaScript(name, input)

    override fun sendTitle(player: Player, title: String) = APIProvider.sendTitle(player, title)

    override fun sendTitle(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int) = APIProvider.sendTitle(player, title, fadeIn, stay, fadeOut)

    override fun sendTitleWithPlaceholders(player: Player, title: String) = APIProvider.sendTitleWithPlaceholders(player, title)

    override fun sendTitleWithPlaceholders(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int) = APIProvider.sendTitleWithPlaceholders(player, title, fadeIn, stay, fadeOut)

    override fun sendSubtitle(player: Player, subtitle: String) = APIProvider.sendSubtitle(player, subtitle)

    override fun sendSubtitle(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) = APIProvider.sendSubtitle(player, subtitle, fadeIn, stay, fadeOut)

    override fun sendSubtitleWithPlaceholders(player: Player, subtitle: String) = APIProvider.sendSubtitleWithPlaceholders(player, subtitle)

    override fun sendSubtitleWithPlaceholders(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) = APIProvider.sendSubtitleWithPlaceholders(player, subtitle, fadeIn, stay, fadeOut)

    override fun sendTitles(player: Player, title: String, subtitle: String) = APIProvider.sendTitles(player, title, subtitle)

    override fun sendTitles(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) = APIProvider.sendTitles(player, title, subtitle, fadeIn, stay, fadeOut)

    override fun sendTitlesWithPlaceholders(player: Player, title: String, subtitle: String) = APIProvider.sendTitlesWithPlaceholders(player, title, subtitle)

    override fun sendTitlesWithPlaceholders(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) = APIProvider.sendTitlesWithPlaceholders(player, title, subtitle, fadeIn, stay, fadeOut)

    override fun sendTimings(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) = APIProvider.sendTimings(player, fadeIn, stay, fadeOut)

    override fun clearTitles(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) = APIProvider.clearTitles(player, fadeIn, stay, fadeOut)

    override fun clearTitle(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) = APIProvider.clearTitle(player, fadeIn, stay, fadeOut)

    override fun clearSubtitle(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) = APIProvider.clearSubtitle(player, fadeIn, stay, fadeOut)

    override fun sendActionbar(player: Player, text: String) = APIProvider.sendActionbar(player, text)

    override fun sendActionbarWithPlaceholders(player: Player, text: String) = APIProvider.sendActionbarWithPlaceholders(player, text)

    override fun clearActionbar(player: Player) = APIProvider.clearActionbar(player)

    override fun getHeader(player: Player) = APIProvider.getHeader(player)

    override fun setHeader(player: Player, header: String) = APIProvider.setHeader(player, header)

    override fun setHeaderWithPlaceholders(player: Player, header: String) = APIProvider.setHeaderWithPlaceholders(player, header)

    override fun getFooter(player: Player) = APIProvider.getFooter(player)

    override fun setFooter(player: Player, footer: String) = APIProvider.setFooter(player, footer)

    override fun setFooterWithPlaceholders(player: Player, footer: String) = APIProvider.setFooterWithPlaceholders(player, footer)

    override fun setHeaderAndFooter(player: Player, header: String, footer: String) = APIProvider.setHeaderAndFooter(player, header, footer)

    override fun setHeaderAndFooterWithPlaceholders(player: Player, header: String, footer: String) = APIProvider.setHeaderAndFooterWithPlaceholders(player, header, footer)
}