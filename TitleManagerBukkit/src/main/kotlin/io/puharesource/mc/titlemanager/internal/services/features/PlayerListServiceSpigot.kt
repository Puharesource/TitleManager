package io.puharesource.mc.titlemanager.internal.services.features

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.internal.config.TMConfigMain
import io.puharesource.mc.titlemanager.internal.extensions.getTitleManagerPlayer
import io.puharesource.mc.titlemanager.internal.model.animation.EasySendableAnimation
import io.puharesource.mc.titlemanager.internal.model.animation.PartBasedSendableAnimation
import io.puharesource.mc.titlemanager.internal.services.animation.AnimationsService
import io.puharesource.mc.titlemanager.internal.services.placeholder.PlaceholderService
import io.puharesource.mc.titlemanager.internal.services.task.SchedulerService
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import javax.inject.Inject

class PlayerListServiceSpigot @Inject constructor(
    private val plugin: TitleManagerPlugin,
    private val config: TMConfigMain,
    private val placeholderService: PlaceholderService,
    private val animationsService: AnimationsService,
    private val schedulerService: SchedulerService
) : PlayerListService {
    override fun startPlayerTasks() {
        plugin.server.onlinePlayers.forEach {
            setProcessedHeaderAndFooter(it, config.playerList.header, config.playerList.footer)
        }
    }

    override fun createHeaderSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(
            schedulerService,
            animation,
            player,
            {
                setHeader(player, it.text, withPlaceholders = withPlaceholders)
            },
            isContinuous = true,
            tickRate = config.bandwidth.playerListMsPerTick,
            fixedOnStop = {
                removeRunningHeaderAnimation(it)
            },
            fixedOnStart = { receiver, sendableAnimation ->
                setRunningHeaderAnimation(receiver, sendableAnimation)
            }
        )
    }

    override fun createFooterSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(
            schedulerService,
            animation,
            player,
            {
                setFooter(player, it.text, withPlaceholders = withPlaceholders)
            },
            isContinuous = true,
            tickRate = config.bandwidth.playerListMsPerTick,
            fixedOnStop = {
                removeRunningFooterAnimation(it)
            },
            fixedOnStart = { receiver, sendableAnimation ->
                setRunningFooterAnimation(receiver, sendableAnimation)
            }
        )
    }

    override fun createHeaderSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(
            schedulerService,
            parts,
            player,
            {
                setHeader(player, it.text, withPlaceholders = withPlaceholders)
            },
            isContinuous = true,
            tickRate = config.bandwidth.playerListMsPerTick,
            fixedOnStop = {
                removeRunningHeaderAnimation(it)
            },
            fixedOnStart = { receiver, animation ->
                setRunningHeaderAnimation(receiver, animation)
            }
        )
    }

    override fun createFooterSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(
            schedulerService,
            parts,
            player,
            {
                setFooter(player, it.text, withPlaceholders = withPlaceholders)
            },
            isContinuous = true,
            tickRate = config.bandwidth.playerListMsPerTick,
            fixedOnStop = {
                removeRunningFooterAnimation(it)
            },
            fixedOnStart = { receiver, animation ->
                setRunningFooterAnimation(receiver, animation)
            }
        )
    }

    override fun getHeader(player: Player): String {
        return player.getTitleManagerPlayer().playerListHeader
    }

    override fun setHeader(player: Player, header: String, withPlaceholders: Boolean) {
        var processedHeader = header

        if (withPlaceholders) {
            processedHeader = placeholderService.replaceText(player, header)
        }

        player.getTitleManagerPlayer().playerListHeader = processedHeader
    }

    override fun setProcessedHeader(player: Player, header: String) {
        val parts = animationsService.textToAnimationParts(header)

        createHeaderSendableAnimation(parts, player, withPlaceholders = true).start()
    }

    override fun getFooter(player: Player): String {
        return player.getTitleManagerPlayer().playerListFooter
    }

    override fun setFooter(player: Player, footer: String, withPlaceholders: Boolean) {
        var processedFooter = footer

        if (withPlaceholders) {
            processedFooter = placeholderService.replaceText(player, footer)
        }

        player.getTitleManagerPlayer().playerListHeader = processedFooter
    }

    override fun setProcessedFooter(player: Player, footer: String) {
        val parts = animationsService.textToAnimationParts(footer)

        createFooterSendableAnimation(parts, player, withPlaceholders = true).start()
    }

    override fun setHeaderAndFooter(player: Player, header: String, footer: String, withPlaceholders: Boolean) {
        player.getTitleManagerPlayer().let { titleManagerPlayer ->
            titleManagerPlayer.playerListHeader = header
            titleManagerPlayer.playerListFooter = footer
        }
    }

    override fun setProcessedHeaderAndFooter(player: Player, header: String, footer: String) {
        setProcessedHeader(player, header)
        setProcessedFooter(player, footer)
    }

    private fun setRunningAnimation(player: Player, path: String, animation: SendableAnimation) {
        player.setMetadata("running-$path-animation", FixedMetadataValue(plugin, animation))
    }

    private fun removeRunningAnimation(player: Player, path: String) {
        val fullPath = "running-$path-animation"

        if (player.hasMetadata(fullPath)) {
            val animation = player.getMetadata(fullPath).first().value() as SendableAnimation

            animation.stop()

            player.removeMetadata(fullPath, plugin)
        }
    }

    private fun setRunningHeaderAnimation(player: Player, animation: SendableAnimation) = setRunningAnimation(player, "header", animation)
    private fun removeRunningHeaderAnimation(player: Player) = removeRunningAnimation(player, "header")

    private fun setRunningFooterAnimation(player: Player, animation: SendableAnimation) = setRunningAnimation(player, "footer", animation)
    private fun removeRunningFooterAnimation(player: Player) = removeRunningAnimation(player, "footer")
}
