package io.puharesource.mc.titlemanager

import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.internal.commands.TMCommand
import io.puharesource.mc.titlemanager.internal.components.DaggerTitleManagerComponent
import io.puharesource.mc.titlemanager.internal.components.TitleManagerComponent
import io.puharesource.mc.titlemanager.internal.config.ConfigMigration
import io.puharesource.mc.titlemanager.internal.config.PrettyConfig
import io.puharesource.mc.titlemanager.internal.config.TMConfigMain
import io.puharesource.mc.titlemanager.internal.debug
import io.puharesource.mc.titlemanager.internal.info
import io.puharesource.mc.titlemanager.internal.model.animation.StandardAnimationFrame
import io.puharesource.mc.titlemanager.internal.placeholder.CombatLogXHook
import io.puharesource.mc.titlemanager.internal.reflections.NMSManager
import io.puharesource.mc.titlemanager.internal.services.TitleManagerService
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TitleManagerPlugin : JavaPlugin(), TitleManagerAPI {
    internal var conf: PrettyConfig? = null
    internal lateinit var tmConfig: TMConfigMain

    lateinit var titleManagerComponent: TitleManagerComponent
    private lateinit var titleManagerService: TitleManagerService

    override fun onEnable() {
        saveDefaultConfig()
        updateConfig()

        titleManagerComponent = DaggerTitleManagerComponent.create()
        titleManagerService = titleManagerComponent.titleManagerService()

        titleManagerService.start()

        if (CombatLogXHook.isEnabled() && !CombatLogXHook.isCorrectVersion()) {
            info("Invalid version of CombatLogX minimum required version is version 10.X.X.X")
        }

        debug("Registering commands")
        registerCommands()

        debug("Registering BungeeCord messengers")
        registerBungeeCord()

        debug("Using MC version: ${NMSManager.serverVersion} | NMS Index: ${NMSManager.versionIndex}")
    }

    override fun onDisable() {
        titleManagerService.stop()
    }

    // Override default config methods.
    override fun getConfig(): FileConfiguration {
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
        onDisable()

        saveDefaultConfig()
        reloadConfig()

        titleManagerComponent = DaggerTitleManagerComponent.create()
        titleManagerService = titleManagerComponent.titleManagerService()

        titleManagerService.start()
    }

    private fun updateConfig() {
        val migration = ConfigMigration(this)

        migration.updateConfig()
    }

    private fun registerBungeeCord() {
        server.messenger.registerOutgoingPluginChannel(this, "BungeeCord")
    }

    private fun registerCommands() {
        getCommand("tm")?.let {
            val tmCommand = TMCommand(this)

            it.setExecutor(tmCommand)
            it.tabCompleter = tmCommand
        }
    }

    override fun replaceText(player: Player, text: String) = titleManagerComponent.placeholderService().replaceText(player, text)

    override fun containsPlaceholders(text: String) = titleManagerComponent.placeholderService().containsPlaceholders(text)
    override fun containsPlaceholder(text: String, placeholder: String) = titleManagerComponent.placeholderService().containsPlaceholder(text, placeholder)

    override fun containsAnimations(text: String) = titleManagerComponent.animationsService().containsAnimations(text)
    override fun containsAnimation(text: String, animation: String) = titleManagerComponent.animationsService().containsAnimation(text, animation)
    override fun getRegisteredAnimations() = titleManagerComponent.animationsService().animations
    override fun getRegisteredScripts() = titleManagerComponent.scriptService().scripts
    override fun addAnimation(id: String, animation: Animation) = titleManagerComponent.animationsService().addAnimation(name, animation)
    override fun removeAnimation(id: String) = titleManagerComponent.animationsService().removeAnimation(name)

    override fun toTitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean) = titleManagerComponent.titleService().createTitleSendableAnimation(animation, player, withPlaceholders)
    override fun toTitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean) = titleManagerComponent.titleService().createTitleSendableAnimation(parts, player, withPlaceholders)
    override fun toSubtitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean) = titleManagerComponent.titleService().createSubtitleSendableAnimation(animation, player, withPlaceholders)
    override fun toSubtitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean) = titleManagerComponent.titleService().createSubtitleSendableAnimation(parts, player, withPlaceholders)

    override fun toActionbarAnimation(animation: Animation, player: Player, withPlaceholders: Boolean) = titleManagerComponent.actionbarService().createActionbarSendableAnimation(animation, player, withPlaceholders)
    override fun toActionbarAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean) = titleManagerComponent.actionbarService().createActionbarSendableAnimation(parts, player, withPlaceholders)

    override fun toHeaderAnimation(animation: Animation, player: Player, withPlaceholders: Boolean) = titleManagerComponent.playerListService().createHeaderSendableAnimation(animation, player, withPlaceholders)
    override fun toHeaderAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean) = titleManagerComponent.playerListService().createHeaderSendableAnimation(parts, player, withPlaceholders)
    override fun toFooterAnimation(animation: Animation, player: Player, withPlaceholders: Boolean) = titleManagerComponent.playerListService().createFooterSendableAnimation(animation, player, withPlaceholders)
    override fun toFooterAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean) = titleManagerComponent.playerListService().createFooterSendableAnimation(parts, player, withPlaceholders)

    override fun toScoreboardTitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean) = titleManagerComponent.scoreboardService().createScoreboardTitleSendableAnimation(animation, player, withPlaceholders)
    override fun toScoreboardTitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean) = titleManagerComponent.scoreboardService().createScoreboardTitleSendableAnimation(parts, player, withPlaceholders)

    override fun toScoreboardValueAnimation(animation: Animation, player: Player, index: Int, withPlaceholders: Boolean) = titleManagerComponent.scoreboardService().createScoreboardValueSendableAnimation(animation, player, index, withPlaceholders)
    override fun toScoreboardValueAnimation(parts: List<AnimationPart<*>>, player: Player, index: Int, withPlaceholders: Boolean) = titleManagerComponent.scoreboardService().createScoreboardValueSendableAnimation(parts, player, index, withPlaceholders)

    override fun toAnimationPart(text: String) = AnimationPart { text }
    override fun toAnimationPart(animation: Animation) = AnimationPart { animation }
    override fun toAnimationParts(text: String) = titleManagerComponent.animationsService().textToAnimationParts(text)

    override fun createAnimationFrame(text: String, fadeIn: Int, stay: Int, fadeOut: Int) = StandardAnimationFrame(text, fadeIn, stay, fadeOut)

    override fun fromText(vararg frames: String) = titleManagerComponent.animationsService().createAnimationFromTextLines(*frames)
    override fun fromTextFile(file: File) = titleManagerComponent.animationsService().createAnimationFromTextFile(file)
    override fun fromJavaScript(name: String, input: String) = titleManagerComponent.scriptService().getScriptAnimation(name, input, true)

    override fun sendTitle(player: Player, title: String) = titleManagerComponent.titleService().sendTitle(player, title, withPlaceholders = false)
    override fun sendTitle(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int) = titleManagerComponent.titleService().sendTitle(player, title, fadeIn, stay, fadeOut, withPlaceholders = false)
    override fun sendTitleWithPlaceholders(player: Player, title: String) = titleManagerComponent.titleService().sendTitle(player, title, withPlaceholders = true)
    override fun sendTitleWithPlaceholders(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int) = titleManagerComponent.titleService().sendTitle(player, title, fadeIn, stay, fadeOut, withPlaceholders = true)
    override fun sendProcessedTitle(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int) = titleManagerComponent.titleService().sendProcessedTitle(player, title, fadeIn, stay, fadeOut)

    override fun sendSubtitle(player: Player, subtitle: String) = titleManagerComponent.titleService().sendSubtitle(player, subtitle, withPlaceholders = false)
    override fun sendSubtitle(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) = titleManagerComponent.titleService().sendSubtitle(player, subtitle, fadeIn, stay, fadeOut, withPlaceholders = false)
    override fun sendSubtitleWithPlaceholders(player: Player, subtitle: String) = titleManagerComponent.titleService().sendSubtitle(player, subtitle, withPlaceholders = true)
    override fun sendSubtitleWithPlaceholders(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) = titleManagerComponent.titleService().sendSubtitle(player, subtitle, fadeIn, stay, fadeOut, withPlaceholders = true)
    override fun sendProcessedSubtitle(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) = titleManagerComponent.titleService().sendProcessedSubtitle(player, subtitle, fadeIn, stay, fadeOut)

    override fun sendTitles(player: Player, title: String, subtitle: String) = titleManagerComponent.titleService().sendTitles(player, title, subtitle, withPlaceholders = false)
    override fun sendTitles(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) = titleManagerComponent.titleService().sendTitles(player, title, subtitle, fadeIn, stay, fadeOut, withPlaceholders = false)
    override fun sendTitlesWithPlaceholders(player: Player, title: String, subtitle: String) = titleManagerComponent.titleService().sendTitles(player, title, subtitle, withPlaceholders = true)
    override fun sendTitlesWithPlaceholders(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) = titleManagerComponent.titleService().sendTitles(player, title, subtitle, fadeIn, stay, fadeOut, withPlaceholders = true)
    override fun sendProcessedTitles(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        titleManagerComponent.titleService().sendProcessedTitle(player, title, fadeIn, stay, fadeOut)
        titleManagerComponent.titleService().sendProcessedSubtitle(player, subtitle, fadeIn, stay, fadeOut)
    }

    override fun sendTimings(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) = titleManagerComponent.titleService().sendTimings(player, fadeIn, stay, fadeOut)

    override fun clearTitle(player: Player) = titleManagerComponent.titleService().clearTitle(player)
    override fun clearSubtitle(player: Player) = titleManagerComponent.titleService().clearSubtitle(player)
    override fun clearTitles(player: Player) = titleManagerComponent.titleService().clearTitles(player)

    override fun sendActionbar(player: Player, text: String) = titleManagerComponent.actionbarService().sendActionbar(player, text, withPlaceholders = false)
    override fun sendActionbarWithPlaceholders(player: Player, text: String) = titleManagerComponent.actionbarService().sendActionbar(player, text, withPlaceholders = true)
    override fun sendProcessedActionbar(player: Player, text: String) = titleManagerComponent.actionbarService().sendProcessedActionbar(player, text)
    override fun clearActionbar(player: Player) = titleManagerComponent.actionbarService().clearActionbar(player)

    override fun setHeader(player: Player, header: String) = titleManagerComponent.playerListService().setHeader(player, header, withPlaceholders = false)
    override fun setHeaderWithPlaceholders(player: Player, header: String) = titleManagerComponent.playerListService().setHeader(player, header, withPlaceholders = true)
    override fun setProcessedHeader(player: Player, header: String) = titleManagerComponent.playerListService().setProcessedHeader(player, header)
    override fun getHeader(player: Player) = titleManagerComponent.playerListService().getHeader(player)

    override fun setFooter(player: Player, footer: String) = titleManagerComponent.playerListService().setFooter(player, footer, withPlaceholders = false)
    override fun setFooterWithPlaceholders(player: Player, footer: String) = titleManagerComponent.playerListService().setFooter(player, footer, withPlaceholders = true)
    override fun setProcessedFooter(player: Player, footer: String) = titleManagerComponent.playerListService().setProcessedFooter(player, footer)
    override fun getFooter(player: Player) = titleManagerComponent.playerListService().getFooter(player)

    override fun setHeaderAndFooter(player: Player, header: String, footer: String) = titleManagerComponent.playerListService().setHeaderAndFooter(player, header, footer, withPlaceholders = false)
    override fun setHeaderAndFooterWithPlaceholders(player: Player, header: String, footer: String) = titleManagerComponent.playerListService().setHeaderAndFooter(player, header, footer)
    override fun setProcessedHeaderAndFooter(player: Player, header: String, footer: String) = titleManagerComponent.playerListService().setHeaderAndFooter(player, header, footer)

    override fun giveScoreboard(player: Player) = titleManagerComponent.scoreboardService().giveScoreboard(player)
    override fun giveDefaultScoreboard(player: Player) = titleManagerComponent.scoreboardService().giveDefaultScoreboard(player)
    override fun removeScoreboard(player: Player) = titleManagerComponent.scoreboardService().removeScoreboard(player)
    override fun hasScoreboard(player: Player) = titleManagerComponent.scoreboardService().hasScoreboard(player)

    override fun setScoreboardTitle(player: Player, title: String) = titleManagerComponent.scoreboardService().setScoreboardTitle(player, title, withPlaceholders = false)
    override fun setScoreboardTitleWithPlaceholders(player: Player, title: String) = titleManagerComponent.scoreboardService().setScoreboardTitle(player, title, withPlaceholders = true)
    override fun setProcessedScoreboardTitle(player: Player, title: String) = titleManagerComponent.scoreboardService().setProcessedScoreboardTitle(player, title)
    override fun getScoreboardTitle(player: Player) = titleManagerComponent.scoreboardService().getScoreboardTitle(player)

    override fun setScoreboardValue(player: Player, index: Int, value: String) = titleManagerComponent.scoreboardService().setScoreboardValue(player, index, value, withPlaceholders = false)
    override fun setScoreboardValueWithPlaceholders(player: Player, index: Int, value: String) = titleManagerComponent.scoreboardService().setScoreboardValue(player, index, value, withPlaceholders = false)
    override fun setProcessedScoreboardValue(player: Player, index: Int, value: String) = titleManagerComponent.scoreboardService().setProcessedScoreboardValue(player, index, value)
    override fun getScoreboardValue(player: Player, index: Int) = titleManagerComponent.scoreboardService().getScoreboardValue(player, index)
    override fun removeScoreboardValue(player: Player, index: Int) = titleManagerComponent.scoreboardService().removeScoreboardValue(player, index)
}
