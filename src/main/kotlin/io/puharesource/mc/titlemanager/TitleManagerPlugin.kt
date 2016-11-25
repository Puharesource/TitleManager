package io.puharesource.mc.titlemanager

import io.puharesource.mc.titlemanager.animations.EasySendableAnimation
import io.puharesource.mc.titlemanager.animations.PartBasedSendableAnimation
import io.puharesource.mc.titlemanager.animations.StandardAnimationFrame
import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.bungeecord.BungeeCordManager
import io.puharesource.mc.titlemanager.commands.TMCommand
import io.puharesource.mc.titlemanager.event.observeEvent
import io.puharesource.mc.titlemanager.event.observeEventRaw
import io.puharesource.mc.titlemanager.extensions.clearActionbar
import io.puharesource.mc.titlemanager.extensions.clearSubtitle
import io.puharesource.mc.titlemanager.extensions.clearTitle
import io.puharesource.mc.titlemanager.extensions.color
import io.puharesource.mc.titlemanager.extensions.format
import io.puharesource.mc.titlemanager.extensions.getStringWithMultilines
import io.puharesource.mc.titlemanager.extensions.sendActionbar
import io.puharesource.mc.titlemanager.extensions.sendSubtitle
import io.puharesource.mc.titlemanager.extensions.sendTitle
import io.puharesource.mc.titlemanager.extensions.setPlayerListFooter
import io.puharesource.mc.titlemanager.extensions.setPlayerListHeader
import io.puharesource.mc.titlemanager.extensions.stripColor
import io.puharesource.mc.titlemanager.placeholder.PlaceholderAPIHook
import io.puharesource.mc.titlemanager.placeholder.VanishHookReplacer
import io.puharesource.mc.titlemanager.placeholder.VaultHook
import io.puharesource.mc.titlemanager.reflections.NMSManager
import io.puharesource.mc.titlemanager.reflections.TitleTypeMapper
import io.puharesource.mc.titlemanager.reflections.getPing
import io.puharesource.mc.titlemanager.reflections.sendNMSPacket
import io.puharesource.mc.titlemanager.script.ScriptManager
import io.puharesource.mc.titlemanager.web.UpdateChecker
import me.clip.placeholderapi.PlaceholderAPI
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TreeMap
import java.util.TreeSet
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.TimeUnit

class TitleManagerPlugin : JavaPlugin(), TitleManagerAPI {
    private val playerListCache = ConcurrentHashMap<Player, Pair<String?, String?>>()

    private val registeredAnimations : MutableMap<String, Animation> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)
    private val registeredScripts : MutableSet<String> = ConcurrentSkipListSet(String.CASE_INSENSITIVE_ORDER)

    private val textAnimationFramePattern = "^\\[([-]?\\d+);([-]?\\d+);([-]?\\d+)\\](.+)$".toRegex()
    private val variablePattern = """[%][{]([^}]+\b)[}]""".toRegex()
    private val animationPattern = """[$][{]([^}]+\b)[}]""".toRegex()
    private val variablePatternWithParameter = """[%][{](([^}:]+\b)[:]((?:(?>[^}\\]+)|\\.)+))[}]""".toRegex()
    private val animationPatternWithParameter = """[$][{](([^}:]+\b)[:]((?:(?>[^}\\]+)|\\.)+))[}]""".toRegex()

    private val placeholderReplacers : MutableMap<String, (Player) -> String> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)
    private val placeholderReplacersWithValues : MutableMap<String, (Player, String) -> String> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)

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

    fun reloadPlugin() {
        UpdateChecker.stop()

        reloadConfig()
        saveDefaultConfig()

        registeredAnimations.clear()
        registeredScripts.clear()

        addFiles()

        if (config.getBoolean("check-for-updates")) {
            UpdateChecker.start()
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

                    registeredScripts.add(name)
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
        observeEvent(events = PlayerJoinEvent::class.java)
                .map { it.player }
                .filter { playerListCache.contains(it) }
                .subscribe { playerListCache.remove(it) }
    }

    private fun registerCommands() {
        val cmd = getCommand("tm")

        cmd.executor = TMCommand
        cmd.tabCompleter = TMCommand
    }

    private fun registerPlaceholders() {
        addPlaceholderReplacer("player", { it.name }, "username", "name")
        addPlaceholderReplacer("displayname", { it.displayName }, "display-name", "nickname", "nick")
        addPlaceholderReplacer("strippeddisplayname", { it.displayName.stripColor() }, "strippeddisplayname", "stripped-displayname", "stripped-nickname", "stripped-nick")
        addPlaceholderReplacer("world", { it.world.name }, "world-name")
        addPlaceholderReplacer("world-time", { it.world.time.toString() })
        addPlaceholderReplacer("online", { server.onlinePlayers.size.toString() }, "online-players")
        addPlaceholderReplacer("max", { server.maxPlayers.toString() }, "max-players")
        addPlaceholderReplacer("world-players", { it.world.players.size.toString() }, "world-online")
        addPlaceholderReplacer("server-time", { SimpleDateFormat(config.getString("placeholders.date-format")).format(Date(System.currentTimeMillis())) })
        addPlaceholderReplacer("ping", { it.getPing().toString() })

        if (config.getBoolean("using-bungeecord")) {
            addPlaceholderReplacer("bungeecord-online", { BungeeCordManager.onlinePlayers.toString() }, "bungeecord-online-players")
            addPlaceholderReplacer("server", { BungeeCordManager.getCurrentServer().orEmpty() }, "server-name")

            addPlaceholderReplacerWithValue("online", { player, value -> BungeeCordManager.getServers()[value]?.playerCount?.toString() ?: "" }, "online-players")
        }

        if (VanishHookReplacer.isValid()) {
            addPlaceholderReplacer("safe-online", { VanishHookReplacer.value(it) }, "safe-online-players")
        }

        if (VaultHook.isEnabled()) {
            if (VaultHook.economySupported) {
                addPlaceholderReplacer("balance", { VaultHook.economy!!.getBalance(it).format() }, "money")
            }

            if (VaultHook.hasGroupSupport()) {
                addPlaceholderReplacer("group", { VaultHook.permissions!!.getPrimaryGroup(it) }, "group-name")
            }
        }
    }

    fun addPlaceholderReplacer(name: String, body: (Player) -> String, vararg aliases: String) {
        placeholderReplacers.put(name, body)
        aliases.forEach { placeholderReplacers.put(it, body) }
    }

    fun addPlaceholderReplacer(name: String, instance: Any, method: Method, vararg aliases: String) {
        addPlaceholderReplacer(name, { method.invoke(instance, it) as String }, *aliases)
    }

    fun addPlaceholderReplacerWithValue(name: String, body: (Player, String) -> String, vararg aliases: String) {
        placeholderReplacersWithValues.put(name, body)
        aliases.forEach { placeholderReplacersWithValues.put(it, body) }
    }

    override fun replaceText(player: Player, text: String): String {
        var replacedText : String

        if (PlaceholderAPIHook.isEnabled()) {
            replacedText = PlaceholderAPI.setPlaceholders(player, text)
        } else {
            replacedText = text
        }

        if (!containsPlaceholders(text)) return replacedText

        val placeholdersInText = TreeSet(String.CASE_INSENSITIVE_ORDER)
        val parameterPlaceholdersInText = TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)

        val parameterMatcher = variablePatternWithParameter.toPattern().matcher(text)
        while (parameterMatcher.find()) {
            val placeholder = parameterMatcher.group(2)
            val parameter = parameterMatcher.group(3).replace("\\}", "}")

            parameterPlaceholdersInText.put(placeholder, parameter)
        }

        val regularMatcher = variablePattern.toPattern().matcher(text)
        while (regularMatcher.find()) {
            placeholdersInText.add(regularMatcher.group(1))
        }

        parameterPlaceholdersInText
                .filter { placeholderReplacersWithValues.containsKey(it.key) }
                .forEach { replacedText = replacedText.replace("%{${it.key}:${it.value}}", placeholderReplacersWithValues[it.key]!!.invoke(player, it.value), ignoreCase = true) }

        placeholdersInText
                .filter { placeholderReplacers.containsKey(it) }
                .map { Pair(it, placeholderReplacers[it]!!) }
                .forEach { replacedText = replacedText.replace("%{${it.first}}", it.second(player), ignoreCase = true) }

        return replacedText
    }

    override fun containsPlaceholders(text: String) = text.contains(variablePattern) || text.contains(variablePatternWithParameter)

    override fun containsPlaceholder(text: String, placeholder: String) = text.contains("%{$placeholder}", ignoreCase = true)

    override fun containsAnimations(text: String) = text.contains(animationPattern)

    override fun containsAnimation(text: String, animation: String) = text.contains("\${$animation}", ignoreCase = true)

    override fun getRegisteredAnimations(): Map<String, Animation> = registeredAnimations

    override fun getRegisteredScripts(): Set<String> = registeredScripts

    override fun addAnimation(id: String, animation: Animation) {
        registeredAnimations.put(id, animation)
    }

    override fun removeAnimation(id: String) {
        registeredAnimations.remove(id)
    }

    override fun toTitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.sendTitle(it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearTitle()
        })
    }

    override fun toSubtitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.sendTitle(it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearSubtitle()
        })
    }

    override fun toActionbarAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.sendActionbar(it.text, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearActionbar()
        })
    }

    override fun toHeaderAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.setPlayerListHeader(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true)
    }

    override fun toFooterAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.setPlayerListFooter(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true)
    }

    override fun toAnimationPart(text: String): AnimationPart<String> {
        return AnimationPart { text }
    }

    override fun toAnimationPart(animation: Animation): AnimationPart<Animation> {
        return AnimationPart { animation }
    }

    override fun toAnimationParts(text: String): List<AnimationPart<*>> {
        if (text.matches(animationPatternWithParameter)) {
            val result = animationPatternWithParameter.matchEntire(text)!!
            val animationName = result.groups[2]!!.value
            val animationValue = result.groups[3]!!.value.replace("\\}", "}")

            if (registeredScripts.contains(animationName)) {
                return listOf(AnimationPart { ScriptManager.getJavaScriptAnimation(animationName, animationValue, withPlaceholders = true) })
            }
        } else if (text.matches(animationPattern)) {
            val animationName = animationPattern.matchEntire(text)!!.groups[1]!!.value

            if (registeredAnimations.containsKey(animationName)) {
                return listOf(AnimationPart { registeredAnimations[animationName] })
            }

            return listOf(AnimationPart { text })
        }

        if (text.contains(animationPatternWithParameter)) {
            val list : MutableList<AnimationPart<*>> = mutableListOf()
            val matcher = animationPatternWithParameter.toPattern().matcher(text)

            var lastEnd = 0
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                val fullAnimation = matcher.group()
                val animation = matcher.group(2)
                val animationValue = matcher.group(3).replace("\\}", "}")
                val part : String = text.substring(lastEnd, start)

                if (part.isNotEmpty()) {
                    list.add(AnimationPart { part })
                }

                if (registeredScripts.contains(animation)) {
                    list.add(AnimationPart { ScriptManager.getJavaScriptAnimation(animation, animationValue, withPlaceholders = true) })
                } else {
                    list.add(AnimationPart { fullAnimation })
                }

                lastEnd = end
            }

            return list
        } else if (text.contains(animationPattern)) {
            val list : MutableList<AnimationPart<*>> = mutableListOf()
            val matcher = animationPattern.toPattern().matcher(text)

            var lastEnd = 0
            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                val fullAnimation = matcher.group()
                val animation = matcher.group(1)
                val part : String = text.substring(lastEnd, start)

                if (part.isNotEmpty()) {
                    list.add(AnimationPart { part })
                }

                if (registeredAnimations.containsKey(animation)) {
                    list.add(AnimationPart { registeredAnimations[animation] })
                } else {
                    list.add(AnimationPart { fullAnimation })
                }

                lastEnd = end
            }

            return list
        }

        return listOf(AnimationPart { text })
    }

    override fun createAnimationFrame(text: String, fadeIn: Int, stay: Int, fadeOut: Int): AnimationFrame {
        return StandardAnimationFrame(text, fadeIn, stay, fadeOut)
    }

    override fun toTitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.sendTitle(it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearTitle()
        })
    }

    override fun toSubtitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.sendSubtitle(it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearSubtitle()
        })
    }

    override fun toActionbarAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.sendActionbar(it.text, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearActionbar()
        })
    }

    override fun toHeaderAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.setPlayerListHeader(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true)
    }

    override fun toFooterAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.setPlayerListFooter(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true)
    }

    override fun fromText(vararg frames: String): Animation {
        val animationFrames = frames
                .map {
                    val matcher = textAnimationFramePattern.toPattern().matcher(it)

                    if (matcher.matches()) {
                        val fadeIn = matcher.group(1).toInt()
                        val stay = matcher.group(2).toInt()
                        val fadeOut = matcher.group(3).toInt()
                        val text = matcher.group(4).color()

                        StandardAnimationFrame(text, fadeIn, stay, fadeOut)
                    } else {
                        StandardAnimationFrame(it.color())
                    }
                }

        return Animation { animationFrames.iterator() }
    }

    override fun fromTextFile(file: File): Animation {
        return fromText(*file.readLines().toTypedArray())
    }

    override fun fromJavaScript(name: String, input: String): Animation {
        return ScriptManager.getJavaScriptAnimation(name, input)
    }

    override fun sendTitle(player: Player, title: String) {
        sendTitle(player, title, -1, -1, -1)
    }

    override fun sendTitle(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        val provider = NMSManager.getClassProvider()
        val packetConstructor : Constructor<*>

        sendTimings(player, fadeIn, stay, fadeOut)

        if (NMSManager.versionIndex == 0) {
            val packetTitle = provider.get("PacketTitle")

            packetConstructor = packetTitle.getConstructor(
                    provider.get("Action").handle,
                    provider.get("IChatBaseComponent").handle,
                    Integer.TYPE, Integer.TYPE, Integer.TYPE)
        } else {
            packetConstructor = provider.get("PacketPlayOutTitle")
                    .getConstructor(
                            provider.get("EnumTitleAction").handle,
                            provider.get("IChatBaseComponent").handle,
                            Integer.TYPE, Integer.TYPE, Integer.TYPE)

        }

        val packet = packetConstructor
                .newInstance(
                        TitleTypeMapper.TITLE.handle,
                        provider.getIChatComponent(title),
                        fadeIn, stay, fadeOut)

        player.sendNMSPacket(packet)
    }

    override fun sendTitleWithPlaceholders(player: Player, title: String) {
        sendTitle(player, replaceText(player, title))
    }

    override fun sendTitleWithPlaceholders(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        sendTitle(player, replaceText(player, title), fadeIn, stay, fadeOut)
    }

    override fun sendSubtitle(player: Player, subtitle: String) {
        sendSubtitle(player, subtitle, -1, -1, -1)
    }

    override fun sendSubtitle(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        val provider = NMSManager.getClassProvider()
        val packetConstructor : Constructor<*>

        if (NMSManager.versionIndex == 0) {
            val packetTitle = provider.get("PacketTitle")

            packetConstructor = packetTitle.getConstructor(
                    provider.get("Action").handle,
                    provider.get("IChatBaseComponent").handle,
                    Integer.TYPE, Integer.TYPE, Integer.TYPE)
        } else {
            packetConstructor = provider.get("PacketPlayOutTitle")
                    .getConstructor(
                            provider.get("EnumTitleAction").handle,
                            provider.get("IChatBaseComponent").handle,
                            Integer.TYPE, Integer.TYPE, Integer.TYPE)

        }

        val packet = packetConstructor
                .newInstance(
                        TitleTypeMapper.SUBTITLE.handle,
                        provider.getIChatComponent(subtitle),
                        fadeIn, stay, fadeOut)

        player.sendNMSPacket(packet)
    }

    override fun sendSubtitleWithPlaceholders(player: Player, subtitle: String) {
        sendSubtitleWithPlaceholders(player, subtitle, -1, -1, -1)
    }

    override fun sendSubtitleWithPlaceholders(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        sendSubtitle(player, replaceText(player, subtitle), fadeIn, stay, fadeOut)
    }

    override fun sendTitles(player: Player, title: String, subtitle: String) {
        sendTitles(player, title, subtitle, -1, -1, -1)
    }

    override fun sendTitles(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        sendTitle(player, title, fadeIn, stay, fadeOut)
        sendSubtitle(player, subtitle, fadeIn, stay, fadeOut)
    }

    override fun sendTitlesWithPlaceholders(player: Player, title: String, subtitle: String) {
        sendTitles(player, replaceText(player, title), replaceText(player, subtitle))
    }

    override fun sendTitlesWithPlaceholders(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        sendTitles(player, replaceText(player, title), replaceText(player, subtitle), fadeIn, stay, fadeOut)
    }

    override fun sendTimings(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) {
        val provider = NMSManager.getClassProvider()
        val packet : Any

        if (NMSManager.versionIndex == 0) {
            val packetTitle = provider.get("PacketTitle")

            packet = packetTitle.getConstructor(
                    provider.get("Action").handle, Integer.TYPE, Integer.TYPE, Integer.TYPE)
                    .newInstance(TitleTypeMapper.TIMES.handle, fadeIn, stay, fadeOut)
        } else {
            packet = provider.get("PacketPlayOutTitle")
                    .getConstructor(
                            provider.get("EnumTitleAction").handle,
                            provider.get("IChatBaseComponent").handle,
                            Integer.TYPE, Integer.TYPE, Integer.TYPE)
                    .newInstance(TitleTypeMapper.TIMES.handle, null, fadeIn, stay, fadeOut)
        }

        player.sendNMSPacket(packet)
    }

    override fun clearTitles(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) {
        sendTitles(player, " ", " ") // TODO: Make this actually use the "Clear" enum.
    }

    override fun clearTitle(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) {
        sendTitle(player, " ", fadeIn, stay, fadeOut)
    }

    override fun clearSubtitle(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) {
        sendSubtitle(player, " ", fadeIn, stay, fadeOut)
    }

    override fun sendActionbar(player: Player, text: String) {
        val provider = NMSManager.getClassProvider()

        if (NMSManager.versionIndex >= 4) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(text))
        } else if (NMSManager.versionIndex == 0) {
            try {
                val packet = provider.get("PacketPlayOutChat")
                        .getConstructor(provider.get("IChatBaseComponent").handle, Integer.TYPE)
                        .newInstance(provider.getIChatComponent(text), 2)

                player.sendNMSPacket(packet)
            } catch (e: NoSuchMethodException) {
                error("(If you're using Spigot #1649) Your version of Spigot #1649 doesn't support actionbar messages. Please find that spigot version from another source!")
            }
        } else {
            val packet = provider.get("PacketPlayOutChat")
                    .getConstructor(provider.get("IChatBaseComponent").handle, Byte::class.java)
                    .newInstance(provider.getIChatComponent(text), 2.toByte())

            player.sendNMSPacket(packet)
        }
    }

    override fun sendActionbarWithPlaceholders(player: Player, text: String) {
        sendActionbar(player, replaceText(player, text))
    }

    override fun clearActionbar(player: Player) {
        sendActionbar(player, " ")
    }

    override fun getHeader(player: Player) = playerListCache[player]?.first.orEmpty()

    override fun setHeader(player: Player, header: String) {
        playerListCache.put(player, Pair(header, getFooter(player)))
        setHeaderAndFooter(player, header, getFooter(player))
    }

    override fun setHeaderWithPlaceholders(player: Player, header: String) {
        setHeaderAndFooter(player, replaceText(player, header), getFooter(player))
    }

    override fun getFooter(player: Player) = playerListCache[player]?.second.orEmpty()

    override fun setFooter(player: Player, footer: String) {
        playerListCache.put(player, Pair(getFooter(player), footer))
        setHeaderAndFooter(player, getHeader(player), footer)
    }

    override fun setFooterWithPlaceholders(player: Player, footer: String) {
        setHeaderAndFooter(player, getHeader(player), replaceText(player, footer))
    }

    override fun setHeaderAndFooter(player: Player, header: String, footer: String) {
        playerListCache.put(player, Pair(header, footer))
        val provider = NMSManager.getClassProvider()
        val packet : Any

        if (NMSManager.versionIndex == 0) {
            packet = provider.get("PacketTabHeader")
                    .getConstructor(provider.get("IChatBaseComponent").handle, provider.get("IChatBaseComponent").handle)
                    .newInstance(provider.getIChatComponent(header), provider.getIChatComponent(footer))
        } else {
            val packetClass = provider.get("PacketPlayOutPlayerListHeaderFooter")
            packet = packetClass.handle.newInstance()

            val headerField = packetClass.handle.getDeclaredField("a")
            headerField.isAccessible = true
            headerField.set(packet, provider.getIChatComponent(header))
            headerField.isAccessible = false

            val footerField = packetClass.handle.getDeclaredField("b")
            footerField.isAccessible = true
            footerField.set(packet, provider.getIChatComponent(footer))
            footerField.isAccessible = false
        }

        player.sendNMSPacket(packet)
    }

    override fun setHeaderAndFooterWithPlaceholders(player: Player, header: String, footer: String) {
        setHeaderAndFooter(player, replaceText(player, header), replaceText(player, footer))
    }
}