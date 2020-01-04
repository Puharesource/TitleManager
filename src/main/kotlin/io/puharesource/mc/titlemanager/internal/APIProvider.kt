package io.puharesource.mc.titlemanager.internal

import io.puharesource.mc.titlemanager.internal.animations.EasySendableAnimation
import io.puharesource.mc.titlemanager.internal.animations.PartBasedSendableAnimation
import io.puharesource.mc.titlemanager.internal.animations.StandardAnimationFrame
import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.internal.extensions.clearActionbar
import io.puharesource.mc.titlemanager.internal.extensions.clearSubtitle
import io.puharesource.mc.titlemanager.internal.extensions.clearTitle
import io.puharesource.mc.titlemanager.internal.extensions.color
import io.puharesource.mc.titlemanager.internal.extensions.getTitleManagerMetadata
import io.puharesource.mc.titlemanager.internal.extensions.removeTitleManagerMetadata
import io.puharesource.mc.titlemanager.internal.extensions.sendActionbar
import io.puharesource.mc.titlemanager.internal.extensions.sendSubtitle
import io.puharesource.mc.titlemanager.internal.extensions.sendTitle
import io.puharesource.mc.titlemanager.internal.extensions.setPlayerListFooter
import io.puharesource.mc.titlemanager.internal.extensions.setPlayerListHeader
import io.puharesource.mc.titlemanager.internal.extensions.setScoreboardTitle
import io.puharesource.mc.titlemanager.internal.extensions.setScoreboardValue
import io.puharesource.mc.titlemanager.internal.extensions.setTitleManagerMetadata
import io.puharesource.mc.titlemanager.internal.functionality.placeholder.MvdwPlaceholderAPIHook
import io.puharesource.mc.titlemanager.internal.functionality.placeholder.PlaceholderAPIHook
import io.puharesource.mc.titlemanager.internal.reflections.NMSManager
import io.puharesource.mc.titlemanager.internal.reflections.NMSUtil
import io.puharesource.mc.titlemanager.internal.functionality.scoreboard.ScoreboardManager
import io.puharesource.mc.titlemanager.internal.functionality.scoreboard.ScoreboardRepresentation
import io.puharesource.mc.titlemanager.internal.script.ScriptManager
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.io.File
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentSkipListMap

object APIProvider : TitleManagerAPI {
    private const val HEADER_METADATA_KEY = "TM-HEADER"
    private const val FOOTER_METADATA_KEY = "TM-FOOTER"

    internal val registeredAnimations : MutableMap<String, Animation> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)

    private val placeholderReplacers : MutableMap<String, (Player) -> String> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)
    private val placeholderReplacersWithValues : MutableMap<String, (Player, String) -> String> = ConcurrentSkipListMap(String.CASE_INSENSITIVE_ORDER)

    internal val textAnimationFramePattern = "^\\[([-]?\\d+);([-]?\\d+);([-]?\\d+)](.+)$".toRegex()
    internal val variablePattern = """[%][{](([^}:]+\b)(?:[:]((?:(?>[^}\\]+)|\\.)+))?)[}]""".toRegex()
    internal val animationPattern = """[$][{](([^}:]+\b)(?:[:]((?:(?>[^}\\]+)|\\.)+))?)[}]""".toRegex()
    internal val commandSplitPattern = """([<]nl[>])|(\\n)""".toRegex()

    var scriptManager: ScriptManager? = null

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
        var replacedText = text

        if (containsPlaceholders(text)) {
            val matcher = variablePattern.toPattern().matcher(text)

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
        }

        if (!isTesting) {
            replacedText = replaceTextFromHooks(player, replacedText)
        }

        return replacedText
    }

    private fun replaceTextFromHooks(player: Player, text: String): String {
        var replacedText = text

        if (PlaceholderAPIHook.isEnabled()) {
            replacedText = PlaceholderAPIHook.replacePlaceholders(player, replacedText)
        }

        if (MvdwPlaceholderAPIHook.canReplace()) {
            replacedText = MvdwPlaceholderAPIHook.replacePlaceholders(player, replacedText)
        }

        return replacedText
    }

    override fun containsPlaceholders(text: String) = text.contains(variablePattern)

    override fun containsPlaceholder(text: String, placeholder: String) = text.contains("%{$placeholder}", ignoreCase = true)

    override fun containsAnimations(text: String) = text.contains(animationPattern)

    override fun containsAnimation(text: String, animation: String) = text.contains("\${$animation}", ignoreCase = true)

    // Animations and scripts

    override fun getRegisteredAnimations(): Map<String, Animation> = registeredAnimations

    override fun getRegisteredScripts(): Set<String> = scriptManager?.registeredScripts ?: emptySet()

    private fun doesScriptExist(name: String) = getRegisteredScripts().contains(name)

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
        }, fixedOnStop = { removeRunningTitleAnimation(it) }, fixedOnStart = { receiver, sendableAnimation -> setRunningTitleAnimation(receiver, sendableAnimation) })
    }

    override fun toSubtitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.sendSubtitle(it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearSubtitle()
        }, fixedOnStop = { removeRunningSubtitleAnimation(it) }, fixedOnStart = { receiver, sendableAnimation -> setRunningSubtitleAnimation(receiver, sendableAnimation) })
    }

    override fun toActionbarAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.sendActionbar(it.text, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearActionbar()
        }, fixedOnStop = { removeRunningActionbarAnimation(it) }, fixedOnStart = { receiver, sendableAnimation -> setRunningActionbarAnimation(receiver, sendableAnimation) })
    }

    override fun toHeaderAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.setPlayerListHeader(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginConfig.bandwidth.playerListMsPerTick, fixedOnStop = { removeRunningHeaderAnimation(it) }, fixedOnStart = { receiver, sendableAnimation -> setRunningHeaderAnimation(receiver, sendableAnimation) })
    }

    override fun toFooterAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.setPlayerListFooter(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginConfig.bandwidth.playerListMsPerTick, fixedOnStop = { removeRunningFooterAnimation(it) }, fixedOnStart = { receiver, sendableAnimation -> setRunningFooterAnimation(receiver, sendableAnimation) })
    }

    override fun toScoreboardTitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.setScoreboardTitle(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginConfig.bandwidth.scoreboardMsPerTick, fixedOnStop = { removeRunningScoreboardTitleAnimation(player) }, fixedOnStart = { receiver, sendableAnimation -> setRunningScoreboardTitleAnimation(receiver, sendableAnimation) })
    }

    override fun toScoreboardValueAnimation(animation: Animation, player: Player, index: Int, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(animation, player, {
            player.setScoreboardValue(index, it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginConfig.bandwidth.scoreboardMsPerTick, fixedOnStop = { removeRunningScoreboardTitleAnimation(player) }, fixedOnStart = { receiver, sendableAnimation -> setRunningScoreboardValueAnimation(receiver, index, sendableAnimation) })
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

            if (hasParameter && doesScriptExist(animationName)) {
                val animationValue = result.groups[3]!!.value.replace("\\}", "}")
                return listOf(AnimationPart { scriptManager!!.getScriptAnimation(animationName, animationValue, withPlaceholders = true) })
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

                if (hasParameter && doesScriptExist(animation)) {
                    val animationValue = matcher.group(3).replace("\\}", "}")
                    list.add(AnimationPart { scriptManager!!.getScriptAnimation(animation, animationValue, withPlaceholders = true) })
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
        }, fixedOnStop = { removeRunningTitleAnimation(it) }, fixedOnStart = { receiver, animation -> setRunningTitleAnimation(receiver, animation) })
    }

    override fun toSubtitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.sendSubtitle(it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearSubtitle()
        }, fixedOnStop = { removeRunningSubtitleAnimation(it) }, fixedOnStart = { receiver, animation -> setRunningSubtitleAnimation(receiver, animation) })
    }

    override fun toActionbarAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.sendActionbar(it.text, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            player.clearActionbar()
        }, fixedOnStop = { removeRunningActionbarAnimation(it) }, fixedOnStart = { receiver, animation -> setRunningActionbarAnimation(receiver, animation) })
    }

    override fun toHeaderAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.setPlayerListHeader(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginConfig.bandwidth.playerListMsPerTick, fixedOnStop = { removeRunningHeaderAnimation(it) }, fixedOnStart = { receiver, animation -> setRunningHeaderAnimation(receiver, animation) })
    }

    override fun toFooterAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.setPlayerListFooter(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginConfig.bandwidth.playerListMsPerTick, fixedOnStop = { removeRunningFooterAnimation(it) }, fixedOnStart = { receiver, animation -> setRunningFooterAnimation(receiver, animation) })
    }

    override fun toScoreboardTitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.setScoreboardTitle(it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginConfig.bandwidth.scoreboardMsPerTick, fixedOnStop = { removeRunningScoreboardTitleAnimation(player) }, fixedOnStart = { receiver, animation -> setRunningScoreboardTitleAnimation(receiver, animation) })
    }

    override fun toScoreboardValueAnimation(parts: List<AnimationPart<*>>, player: Player, index: Int, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(parts, player, {
            player.setScoreboardValue(index, it.text, withPlaceholders = withPlaceholders)
        }, continuous = true, tickRate = pluginConfig.bandwidth.scoreboardMsPerTick, fixedOnStop = { removeRunningScoreboardTitleAnimation(player) }, fixedOnStart = { receiver, animation -> setRunningScoreboardValueAnimation(receiver, index, animation) })
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
        return scriptManager?.getScriptAnimation(name, input) ?: throw RuntimeException("Script engine does not exist")
    }

    // Titles

    override fun sendTitle(player: Player, title: String) = sendTitle(player, title, -1, -1, -1)

    override fun sendTitle(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        if (NMSManager.versionIndex >= 9) {
            player.sendTitle(title, null, fadeIn, stay, fadeOut)
        } else {
            NMSUtil.sendTitle(player, title, fadeIn, stay, fadeOut)
        }
    }

    override fun sendTitleWithPlaceholders(player: Player, title: String) = sendTitle(player, replaceText(player, title))

    override fun sendTitleWithPlaceholders(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int) = sendTitle(player, replaceText(player, title), fadeIn, stay, fadeOut)

    override fun sendSubtitle(player: Player, subtitle: String) = sendSubtitle(player, subtitle, -1, -1, -1)

    override fun sendSubtitle(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        if (NMSManager.versionIndex >= 9) {
            player.sendTitle("", subtitle, fadeIn, stay, fadeOut)
        } else {
            NMSUtil.sendSubtitle(player, subtitle, fadeIn, stay, fadeOut)
        }
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
        if (NMSManager.versionIndex >= 9) {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut)

            return
        }

        NMSUtil.sendTitle(player, title, fadeIn, stay, fadeOut)
        NMSUtil.sendSubtitle(player, subtitle, fadeIn, stay, fadeOut)
    }

    override fun sendTitlesWithPlaceholders(player: Player, title: String, subtitle: String) {
        sendTitles(player, replaceText(player, title), replaceText(player, subtitle))
    }

    override fun sendTitlesWithPlaceholders(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        sendTitles(player, replaceText(player, title), replaceText(player, subtitle), fadeIn, stay, fadeOut)
    }

    override fun sendTimings(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) {
        if (NMSManager.versionIndex >= 9) {
            player.sendTitle(null, null, fadeIn, stay, fadeOut)

            return
        }

        NMSUtil.sendTimings(player, fadeIn, stay, fadeOut)
    }

    override fun clearTitles(player: Player) {
        if (NMSManager.versionIndex >= 9) {
            player.resetTitle()

            return
        }

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
        if (NMSManager.versionIndex >= 5) {
            try {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(text))
            } catch (e: Exception) {
                error("To use Actionbar messages you need to run Spigot, not CraftBukkit!")
            }
        } else {
            NMSUtil.sendActionbar(player, text)
        }
    }

    override fun sendActionbarWithPlaceholders(player: Player, text: String) {
        sendActionbar(player, replaceText(player, text))
    }

    override fun clearActionbar(player: Player) {
        sendActionbar(player, " ")
    }

    // Player list

    override fun getHeader(player: Player): String {
        if (NMSManager.versionIndex >= 9) {
            return player.playerListHeader.orEmpty()
        }

        return player.getTitleManagerMetadata(HEADER_METADATA_KEY)?.asString().orEmpty()
    }

    override fun setHeader(player: Player, header: String) {
        if (NMSManager.versionIndex >= 9) {
            player.playerListHeader = header

            return
        }

        setHeaderAndFooter(player, header, getFooter(player))
    }

    override fun setHeaderWithPlaceholders(player: Player, header: String) = setHeader(player, replaceText(player, header))

    override fun getFooter(player: Player): String {
        if (NMSManager.versionIndex >= 9) {
            return player.playerListFooter.orEmpty()
        }

        return player.getTitleManagerMetadata(FOOTER_METADATA_KEY)?.asString().orEmpty()
    }

    override fun setFooter(player: Player, footer: String) {
        if (NMSManager.versionIndex >= 9) {
            player.playerListFooter = footer

            return
        }

        setHeaderAndFooter(player, getHeader(player), footer)
    }

    override fun setFooterWithPlaceholders(player: Player, footer: String) = setFooter(player, replaceText(player, footer))

    override fun setHeaderAndFooter(player: Player, header: String, footer: String) {
        if (pluginConfig.bandwidth.preventDuplicatePackets) {
            val cachedHeader = getHeader(player)
            val cachedFooter = getFooter(player)

            if (header == cachedHeader && footer == cachedFooter) {
                return
            }

            if (NMSManager.versionIndex < 9) {
                player.setTitleManagerMetadata(HEADER_METADATA_KEY, header)
                player.setTitleManagerMetadata(FOOTER_METADATA_KEY, footer)
            }
        }

        if (NMSManager.versionIndex >= 9) {
            player.setPlayerListHeaderFooter(header, footer)
        } else {
            NMSUtil.setHeaderAndFooter(player, header, footer)
        }
    }

    override fun setHeaderAndFooterWithPlaceholders(player: Player, header: String, footer: String) {
        setHeaderAndFooter(player, replaceText(player, header), replaceText(player, footer))
    }

    fun clearHeaderAndFooterCache(player: Player) {
        player.removeTitleManagerMetadata(HEADER_METADATA_KEY)
        player.removeTitleManagerMetadata(FOOTER_METADATA_KEY)
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
        require(index in 1..15) { "Index needs to be in the range of 1 to 15 (1 and 15 inclusive). Index provided: $index" }

        ScoreboardManager.playerScoreboards[player]?.set(index, value)
    }

    override fun setScoreboardValueWithPlaceholders(player: Player, index: Int, value: String) = setScoreboardValue(player, index, replaceText(player, value))

    override fun getScoreboardValue(player: Player, index: Int): String? {
        require(index in 1..15) { "Index needs to be in the range of 1 to 15 (1 and 15 inclusive). Index provided: $index" }

        return ScoreboardManager.playerScoreboards[player]?.get(index)
    }

    override fun removeScoreboardValue(player: Player, index: Int) {
        require(index in 1..15) { "Index needs to be in the range of 1 to 15 (1 and 15 inclusive). Index provided: $index" }

        ScoreboardManager.playerScoreboards[player]?.remove(index)
    }
}