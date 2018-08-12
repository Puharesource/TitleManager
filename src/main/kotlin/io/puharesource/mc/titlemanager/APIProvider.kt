package io.puharesource.mc.titlemanager

import io.puharesource.mc.titlemanager.animations.EasySendableAnimation
import io.puharesource.mc.titlemanager.animations.PartBasedSendableAnimation
import io.puharesource.mc.titlemanager.animations.StandardAnimationFrame
import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.extensions.clearActionbar
import io.puharesource.mc.titlemanager.extensions.clearSubtitle
import io.puharesource.mc.titlemanager.extensions.clearTitle
import io.puharesource.mc.titlemanager.extensions.color
import io.puharesource.mc.titlemanager.extensions.getTitleManagerMetadata
import io.puharesource.mc.titlemanager.extensions.modify
import io.puharesource.mc.titlemanager.extensions.sendActionbar
import io.puharesource.mc.titlemanager.extensions.sendSubtitle
import io.puharesource.mc.titlemanager.extensions.sendTitle
import io.puharesource.mc.titlemanager.extensions.setPlayerListFooter
import io.puharesource.mc.titlemanager.extensions.setPlayerListHeader
import io.puharesource.mc.titlemanager.extensions.setScoreboardTitle
import io.puharesource.mc.titlemanager.extensions.setScoreboardValue
import io.puharesource.mc.titlemanager.extensions.setTitleManagerMetadata
import io.puharesource.mc.titlemanager.placeholder.MvdwPlaceholderAPIHook
import io.puharesource.mc.titlemanager.placeholder.PlaceholderAPIHook
import io.puharesource.mc.titlemanager.reflections.NMSManager
import io.puharesource.mc.titlemanager.reflections.PacketPlayOutChat
import io.puharesource.mc.titlemanager.reflections.PacketTabHeader
import io.puharesource.mc.titlemanager.reflections.PacketTitle
import io.puharesource.mc.titlemanager.reflections.TitleTypeMapper
import io.puharesource.mc.titlemanager.reflections.sendNMSPacket
import io.puharesource.mc.titlemanager.scoreboard.ScoreboardManager
import io.puharesource.mc.titlemanager.scoreboard.ScoreboardRepresentation
import io.puharesource.mc.titlemanager.script.ScriptManager
import me.clip.placeholderapi.PlaceholderAPI
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.io.File
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentSkipListMap

object APIProvider : TitleManagerAPI {
    private const val HEADER_METADATA_KEY = "TM-HEADER"
    private const val FOOTER_METADATA_KEY = "TM-FOOTER"

    private val classPacketTabHeader by lazy { PacketTabHeader() }
    private val classPacketTitle by lazy { PacketTitle() }
    private val classPacketPlayOutChat by lazy { PacketPlayOutChat() }

    internal val registeredAnimations : MutableMap<String, Animation> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)

    private val placeholderReplacers : MutableMap<String, (Player) -> String> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)
    private val placeholderReplacersWithValues : MutableMap<String, (Player, String) -> String> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)

    internal val textAnimationFramePattern = "^\\[([-]?\\d+);([-]?\\d+);([-]?\\d+)](.+)$".toRegex()
    internal val variablePattern = """[%][{](([^}:]+\b)(?:[:]((?:(?>[^}\\]+)|\\.)+))?)[}]""".toRegex()
    internal val animationPattern = """[$][{](([^}:]+\b)(?:[:]((?:(?>[^}\\]+)|\\.)+))?)[}]""".toRegex()
    internal val commandSplitPattern = """([<]nl[>])|(\\n)""".toRegex()

    // Running animations

    private fun setRunningAnimation(player: Player, path: String, animation: SendableAnimation) {
        player.setMetadata("running-$path-animation", FixedMetadataValue(pluginInstance, animation))
    }

    private fun removeRunningAnimation(player: Player, path: String) {
        val fullPath = "running-$path-animation"

        if (player.hasMetadata(fullPath)) {
            val animation = player.getMetadata(fullPath).first().value() as SendableAnimation

            animation.stop()

            player.removeMetadata(fullPath, pluginInstance)
        }
    }

    fun removeAllRunningAnimations(player: Player) {
        removeRunningTitleAnimation(player)
        removeRunningSubtitleAnimation(player)

        removeRunningActionbarAnimation(player)

        removeRunningHeaderAnimation(player)
        removeRunningFooterAnimation(player)

        removeRunningScoreboardTitleAnimation(player)
        (1..15).forEach { removeRunningScoreboardValueAnimation(player, it) }
    }

    fun setRunningHeaderAnimation(player: Player, animation: SendableAnimation) = setRunningAnimation(player, "header", animation)
    fun removeRunningHeaderAnimation(player: Player) = removeRunningAnimation(player, "header")

    fun setRunningFooterAnimation(player: Player, animation: SendableAnimation) = setRunningAnimation(player, "footer", animation)
    fun removeRunningFooterAnimation(player: Player) = removeRunningAnimation(player, "footer")

    fun setRunningTitleAnimation(player: Player, animation: SendableAnimation) = setRunningAnimation(player, "title", animation)
    fun removeRunningTitleAnimation(player: Player) = removeRunningAnimation(player, "title")

    fun setRunningSubtitleAnimation(player: Player, animation: SendableAnimation) = setRunningAnimation(player, "subtitle", animation)
    fun removeRunningSubtitleAnimation(player: Player) = removeRunningAnimation(player, "subtitle")

    fun setRunningActionbarAnimation(player: Player, animation: SendableAnimation) = setRunningAnimation(player, "actionbar", animation)
    fun removeRunningActionbarAnimation(player: Player) = removeRunningAnimation(player, "actionbar")

    fun setRunningScoreboardTitleAnimation(player: Player, animation: SendableAnimation) = setRunningAnimation(player, "scoreboardtitle", animation)
    fun removeRunningScoreboardTitleAnimation(player: Player) = removeRunningAnimation(player, "scoreboardtitle")

    fun setRunningScoreboardValueAnimation(player: Player, index: Int, animation: SendableAnimation) = setRunningAnimation(player, "scoreboardvalue$index", animation)
    fun removeRunningScoreboardValueAnimation(player: Player, index: Int) = removeRunningAnimation(player, "scoreboardvalue$index")

    // Placeholder

    fun addPlaceholderReplacer(name: String, body: (Player) -> String, vararg aliases: String) {
        placeholderReplacers[name] = body
        aliases.forEach { placeholderReplacers[it] = body }
    }

    fun addPlaceholderReplacer(name: String, instance: Any, method: Method, vararg aliases: String) {
        addPlaceholderReplacer(name, { method.invoke(instance, it) as String }, *aliases)
    }

    fun addPlaceholderReplacerWithValue(name: String, body: (Player, String) -> String, vararg aliases: String) {
        placeholderReplacersWithValues[name] = body
        aliases.forEach { placeholderReplacersWithValues[it] = body }
    }

    override fun replaceText(player: Player, text: String): String {
        val placeholderAPIEnabled = !isTesting && PlaceholderAPIHook.isEnabled()
        val mvdwAPIEnabled = !isTesting && MvdwPlaceholderAPIHook.isEnabled()
        val mvdwReplace : (Player, String) -> String = { player, text -> be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(player, text) }

        if (!containsPlaceholders(text)) {
            var replacedText = text

            if (placeholderAPIEnabled) {
                replacedText = PlaceholderAPI.setPlaceholders(player, replacedText)
            }

            if (mvdwAPIEnabled) {
                replacedText = mvdwReplace(player, replacedText)
            }

            return replacedText
        }

        val matcher = variablePattern.toPattern().matcher(text)
        var replacedText = text

        while (matcher.find()) {
            val placeholder = matcher.group(2)
            val parameter : String? = if (matcher.groupCount() == 3) matcher.group(3)?.replace("\\}", "}") else null

            if (parameter != null) {
                placeholderReplacersWithValues[placeholder]?.let { replacer ->
                    replacedText = replacedText.replace(matcher.group(), replacer.invoke(player, parameter))
                }
            } else {
                placeholderReplacers[matcher.group(1)]?.let { replacer ->
                    replacedText = replacedText.replace(matcher.group(), replacer.invoke(player))
                }
            }
        }

        if (placeholderAPIEnabled) {
            replacedText = PlaceholderAPI.setPlaceholders(player, replacedText)
        }

        if (mvdwAPIEnabled) {
            replacedText = mvdwReplace(player, replacedText)
        }

        return replacedText
    }

    override fun containsPlaceholders(text: String) = text.contains(variablePattern)

    override fun containsPlaceholder(text: String, placeholder: String) = text.contains("%{$placeholder}", ignoreCase = true)

    override fun containsAnimations(text: String) = text.contains(animationPattern)

    override fun containsAnimation(text: String, animation: String) = text.contains("\${$animation}", ignoreCase = true)

    // Animations and scripts

    override fun getRegisteredAnimations(): Map<String, Animation> = registeredAnimations

    override fun getRegisteredScripts(): Set<String> = ScriptManager.registeredScripts

    override fun addAnimation(id: String, animation: Animation) {
        registeredAnimations[id] = animation
    }

    override fun removeAnimation(id: String) {
        registeredAnimations.remove(id)
    }

    override fun toTitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.sendTitle(it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearTitle()
        }, fixedOnStop = { removeRunningTitleAnimation(it) }, fixedOnStart = { player, animation -> setRunningTitleAnimation(player, animation) })
    }

    override fun toSubtitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.sendSubtitle(it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearSubtitle()
        }, fixedOnStop = { removeRunningSubtitleAnimation(it) }, fixedOnStart = { player, animation -> setRunningSubtitleAnimation(player, animation) })
    }

    override fun toActionbarAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.sendActionbar(it.text, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearActionbar()
        }, fixedOnStop = { removeRunningActionbarAnimation(it) }, fixedOnStart = { player, animation -> setRunningActionbarAnimation(player, animation) })
    }

    override fun toHeaderAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.setPlayerListHeader(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginInstance.config.getLong("bandwidth.player-list-ms-per-tick"), fixedOnStop = { removeRunningHeaderAnimation(it) }, fixedOnStart = { player, animation -> setRunningHeaderAnimation(player, animation) })
    }

    override fun toFooterAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.setPlayerListFooter(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginInstance.config.getLong("bandwidth.player-list-ms-per-tick"), fixedOnStop = { removeRunningFooterAnimation(it) }, fixedOnStart = { player, animation -> setRunningFooterAnimation(player, animation) })
    }

    override fun toScoreboardTitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.setScoreboardTitle(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginInstance.config.getLong("bandwidth.scoreboard-ms-per-tick"), fixedOnStop = { removeRunningScoreboardTitleAnimation(player) }, fixedOnStart = { player, animation -> setRunningScoreboardTitleAnimation(player, animation) })
    }

    override fun toScoreboardValueAnimation(animation: Animation, player: Player, index: Int, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.setScoreboardValue(index, it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginInstance.config.getLong("bandwidth.scoreboard-ms-per-tick"), fixedOnStop = { removeRunningScoreboardTitleAnimation(player) }, fixedOnStart = { player, animation -> setRunningScoreboardValueAnimation(player, index, animation) })
    }

    override fun toAnimationPart(text: String): AnimationPart<String> {
        return AnimationPart { text }
    }

    override fun toAnimationPart(animation: Animation): AnimationPart<Animation> {
        return AnimationPart { animation }
    }

    override fun toAnimationParts(text: String): List<AnimationPart<*>> {
        if (text.matches(animationPattern)) {
            val result = animationPattern.matchEntire(text)!!
            val animationName = result.groups[2]!!.value
            val hasParameter = result.groups.size == 3

            if (hasParameter && ScriptManager.registeredScripts.contains(animationName)) {
                val animationValue = result.groups[3]!!.value.replace("\\}", "}")
                return listOf(AnimationPart { ScriptManager.getJavaScriptAnimation(animationName, animationValue, withPlaceholders = true) })
            } else if (registeredAnimations.containsKey(animationName)) {
                return listOf(AnimationPart { registeredAnimations[animationName] })
            } else {
                listOf(AnimationPart { text })
            }
        }

        if (text.contains(animationPattern)) {
            val list : MutableList<AnimationPart<*>> = mutableListOf()
            val matcher = animationPattern.toPattern().matcher(text)

            var lastEnd = 0

            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                val fullAnimation = matcher.group()
                val animation = matcher.group(2)
                val hasParameter = matcher.groupCount() == 3

                val part : String = text.substring(lastEnd, start)

                if (part.isNotEmpty()) {
                    list.add(AnimationPart { part })
                }

                if (hasParameter && ScriptManager.registeredScripts.contains(animation)) {
                    val animationValue = matcher.group(3).replace("\\}", "}")
                    list.add(AnimationPart { ScriptManager.getJavaScriptAnimation(animation, animationValue, withPlaceholders = true) })
                } else if (registeredAnimations.containsKey(animation)) {
                    list.add(AnimationPart { registeredAnimations[animation] })
                } else {
                    list.add(AnimationPart { fullAnimation })
                }

                lastEnd = end
            }

            if (lastEnd != text.length) {
                val part : String = text.substring(lastEnd, text.length)

                if (part.isNotEmpty()) {
                    list.add(AnimationPart { part })
                }
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
        }, fixedOnStop = { removeRunningTitleAnimation(it) }, fixedOnStart = { player, animation -> setRunningTitleAnimation(player, animation) })
    }

    override fun toSubtitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.sendSubtitle(it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearSubtitle()
        }, fixedOnStop = { removeRunningSubtitleAnimation(it) }, fixedOnStart = { player, animation -> setRunningSubtitleAnimation(player, animation) })
    }

    override fun toActionbarAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.sendActionbar(it.text, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearActionbar()
        }, fixedOnStop = { removeRunningActionbarAnimation(it) }, fixedOnStart = { player, animation -> setRunningActionbarAnimation(player, animation) })
    }

    override fun toHeaderAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.setPlayerListHeader(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginInstance.config.getLong("bandwidth.player-list-ms-per-tick"), fixedOnStop = { removeRunningHeaderAnimation(it) }, fixedOnStart = { player, animation -> setRunningHeaderAnimation(player, animation) })
    }

    override fun toFooterAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.setPlayerListFooter(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginInstance.config.getLong("bandwidth.player-list-ms-per-tick"), fixedOnStop = { removeRunningFooterAnimation(it) }, fixedOnStart = { player, animation -> setRunningFooterAnimation(player, animation) })
    }

    override fun toScoreboardTitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.setScoreboardTitle(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginInstance.config.getLong("bandwidth.scoreboard-ms-per-tick"), fixedOnStop = { removeRunningScoreboardTitleAnimation(player) }, fixedOnStart = { player, animation -> setRunningScoreboardTitleAnimation(player, animation) })
    }

    override fun toScoreboardValueAnimation(parts: List<AnimationPart<*>>, player: Player, index: Int, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.setScoreboardValue(index, it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginInstance.config.getLong("bandwidth.scoreboard-ms-per-tick"), fixedOnStop = { removeRunningScoreboardTitleAnimation(player) }, fixedOnStart = { player, animation -> setRunningScoreboardValueAnimation(player, index, animation) })
    }

    override fun fromText(vararg frames: String): Animation {
        val animationFrames = frames
                .map {
                    val matcher = textAnimationFramePattern.toPattern().matcher(it)

                    if (matcher.matches()) {
                        val fadeIn = matcher.group(1).toInt()
                        val stay = matcher.group(2).toInt()
                        val fadeOut = matcher.group(3).toInt()
                        val text = matcher.group(4).color().replace("\\n", "\n")

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

    // Titles

    override fun sendTitle(player: Player, title: String) {
        sendTitle(player, title, -1, -1, -1)
    }

    override fun sendTitle(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        val provider = NMSManager.getClassProvider()
        val packetConstructor : Constructor<*> = classPacketTitle.constructor

        sendTimings(player, fadeIn, stay, fadeOut)

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
        val packetConstructor = classPacketTitle.constructor

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
        val packet = if (NMSManager.versionIndex == 0) {
            classPacketTitle.timingsConstructor.newInstance(TitleTypeMapper.TIMES.handle, fadeIn, stay, fadeOut)
        } else {
            classPacketTitle.constructor.newInstance(TitleTypeMapper.TIMES.handle, null, fadeIn, stay, fadeOut)
        }

        player.sendNMSPacket(packet)
    }

    override fun clearTitles(player: Player) {
        sendTitles(player, " ", " ") // TODO: Make this actually use the "Clear" enum.
    }

    override fun clearTitle(player: Player) {
        sendTitle(player, " ")
    }

    override fun clearSubtitle(player: Player) {
        sendSubtitle(player, " ")
    }

    // Actionbar

    override fun sendActionbar(player: Player, text: String) {
        val provider = NMSManager.getClassProvider()

        when {
            NMSManager.versionIndex == 0 -> try {
                val packet = classPacketPlayOutChat.constructor.newInstance(provider.getIChatComponent(text), 2)

                player.sendNMSPacket(packet)
            } catch (e: NoSuchMethodException) {
                error("(If you're using Spigot #1649) Your version of Spigot #1649 doesn't support actionbar messages. Please find that spigot version from another source!")
            }
            NMSManager.versionIndex >= 5 -> try {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(text))
            } catch (e: Exception) {
                error("To use Actionbar messages you need to run Spigot, not CraftBukkit!")
            }
            else -> {
                val packet = classPacketPlayOutChat.constructor.newInstance(provider.getIChatComponent(text), 2.toByte())

                player.sendNMSPacket(packet)
            }
        }
    }

    override fun sendActionbarWithPlaceholders(player: Player, text: String) {
        sendActionbar(player, replaceText(player, text))
    }

    override fun clearActionbar(player: Player) {
        sendActionbar(player, " ")
    }

    // Player list

    override fun getHeader(player: Player) = player.getTitleManagerMetadata(HEADER_METADATA_KEY)?.asString().orEmpty()

    override fun setHeader(player: Player, header: String) {
        player.setTitleManagerMetadata(HEADER_METADATA_KEY, header)
        setHeaderAndFooter(player, header, getFooter(player))
    }

    override fun setHeaderWithPlaceholders(player: Player, header: String) {
        setHeaderAndFooter(player, replaceText(player, header), getFooter(player))
    }

    override fun getFooter(player: Player) = player.getTitleManagerMetadata(FOOTER_METADATA_KEY)?.asString().orEmpty()

    override fun setFooter(player: Player, footer: String) {
        player.setTitleManagerMetadata(FOOTER_METADATA_KEY, footer)
        setHeaderAndFooter(player, getHeader(player), footer)
    }

    override fun setFooterWithPlaceholders(player: Player, footer: String) {
        setHeaderAndFooter(player, getHeader(player), replaceText(player, footer))
    }

    override fun setHeaderAndFooter(player: Player, header: String, footer: String) {
        if (pluginInstance.config.getBoolean("bandwidth.prevent-duplicate-packets")) {
            val cachedHeader = getHeader(player)
            val cachedFooter = getFooter(player)

            if (header == cachedHeader && footer == cachedFooter) {
                return
            }
        }

        player.setTitleManagerMetadata(HEADER_METADATA_KEY, header)
        player.setTitleManagerMetadata(FOOTER_METADATA_KEY, footer)

        val provider = NMSManager.getClassProvider()
        val packet : Any

        if (NMSManager.versionIndex == 0) {
            packet = classPacketTabHeader.legacyConstructor.newInstance(provider.getIChatComponent(header), provider.getIChatComponent(footer))
        } else {
            packet = classPacketTabHeader.createInstance()

            classPacketTabHeader.headerField.modify { set(packet, provider.getIChatComponent(header)) }
            classPacketTabHeader.footerField.modify { set(packet, provider.getIChatComponent(footer)) }
        }

        player.sendNMSPacket(packet)
    }

    override fun setHeaderAndFooterWithPlaceholders(player: Player, header: String, footer: String) {
        setHeaderAndFooter(player, replaceText(player, header), replaceText(player, footer))
    }

    // Scoreboard

    override fun giveScoreboard(player: Player) {
        if (!hasScoreboard(player)) {
            ScoreboardManager.playerScoreboards[player] = ScoreboardRepresentation()
            ScoreboardManager.startUpdateTask(player)
        }
    }

    override fun removeScoreboard(player: Player) {
        ScoreboardManager.removeScoreboard(player)
    }

    override fun hasScoreboard(player: Player) = ScoreboardManager.playerScoreboards.containsKey(player)

    override fun setScoreboardTitle(player: Player, title: String) {
        ScoreboardManager.playerScoreboards[player]?.title = title
    }

    override fun setScoreboardTitleWithPlaceholders(player: Player, title: String) = setScoreboardTitle(player, replaceText(player, title))

    override fun getScoreboardTitle(player: Player) = ScoreboardManager.playerScoreboards[player]?.title

    override fun setScoreboardValue(player: Player, index: Int, value: String) {
        if (index < 1 || index > 15) throw IllegalArgumentException("Index needs to be in the range of 1 to 15 (1 and 15 inclusive). Index provided: $index")

        ScoreboardManager.playerScoreboards[player]?.set(index, value)
    }

    override fun setScoreboardValueWithPlaceholders(player: Player, index: Int, value: String) = setScoreboardValue(player, index, replaceText(player, value))

    override fun getScoreboardValue(player: Player, index: Int): String? {
        if (index < 1 || index > 15) throw IllegalArgumentException("Index needs to be in the range of 1 to 15 (1 and 15 inclusive). Index provided: $index")

        return ScoreboardManager.playerScoreboards[player]?.get(index)
    }

    override fun removeScoreboardValue(player: Player, index: Int) {
        if (index < 1 || index > 15) throw IllegalArgumentException("Index needs to be in the range of 1 to 15 (1 and 15 inclusive). Index provided: $index")

        ScoreboardManager.playerScoreboards[player]?.remove(index)
    }
}