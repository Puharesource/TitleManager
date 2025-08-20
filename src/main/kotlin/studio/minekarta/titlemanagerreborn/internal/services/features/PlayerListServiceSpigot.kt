package studio.minekarta.titlemanagerreborn.internal.services.features

import studio.minekarta.titlemanagerreborn.TitleManagerReborn
import studio.minekarta.titlemanagerreborn.api.v2.animation.Animation
import studio.minekarta.titlemanagerreborn.api.v2.animation.AnimationPart
import studio.minekarta.titlemanagerreborn.api.v2.animation.SendableAnimation
import studio.minekarta.titlemanagerreborn.internal.config.TMConfigMain
import studio.minekarta.titlemanagerreborn.internal.extensions.getTitleManagerMetadata
import studio.minekarta.titlemanagerreborn.internal.extensions.removeTitleManagerMetadata
import studio.minekarta.titlemanagerreborn.internal.extensions.setTitleManagerMetadata
import studio.minekarta.titlemanagerreborn.internal.model.animation.EasySendableAnimation
import studio.minekarta.titlemanagerreborn.internal.model.animation.PartBasedSendableAnimation
import studio.minekarta.titlemanagerreborn.internal.reflections.NMSManager
import studio.minekarta.titlemanagerreborn.internal.reflections.NMSUtil
import studio.minekarta.titlemanagerreborn.internal.services.animation.AnimationsService
import studio.minekarta.titlemanagerreborn.internal.services.placeholder.PlaceholderService
import studio.minekarta.titlemanagerreborn.internal.services.task.SchedulerService
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import javax.inject.Inject

class PlayerListServiceSpigot @Inject constructor(
    private val plugin: TitleManagerReborn,
    private val config: TMConfigMain,
    private val placeholderService: PlaceholderService,
    private val animationsService: AnimationsService,
    private val schedulerService: SchedulerService
) : PlayerListService {
    private val headerMetadataKey = "TM-HEADER"
    private val footerMetadataKey = "TM-FOOTER"

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
        if (NMSManager.versionIndex >= 9) {
            return player.playerListHeader.orEmpty()
        }

        return player.getTitleManagerMetadata(headerMetadataKey)?.asString().orEmpty()
    }

    override fun setHeader(player: Player, header: String, withPlaceholders: Boolean) {
        var processedHeader = header

        if (withPlaceholders) {
            processedHeader = placeholderService.replaceText(player, header)
        }

        if (NMSManager.versionIndex >= 9) {
            player.playerListHeader = processedHeader

            return
        }

        setHeaderAndFooter(player, processedHeader, getFooter(player))
    }

    override fun setProcessedHeader(player: Player, header: String) {
        val parts = animationsService.textToAnimationParts(header)

        createHeaderSendableAnimation(parts, player, withPlaceholders = true).start()
    }

    override fun getFooter(player: Player): String {
        if (NMSManager.versionIndex >= 9) {
            return player.playerListFooter.orEmpty()
        }

        return player.getTitleManagerMetadata(footerMetadataKey)?.asString().orEmpty()
    }

    override fun setFooter(player: Player, footer: String, withPlaceholders: Boolean) {
        var processedFooter = footer

        if (withPlaceholders) {
            processedFooter = placeholderService.replaceText(player, footer)
        }

        if (NMSManager.versionIndex >= 9) {
            player.playerListFooter = processedFooter

            return
        }

        setHeaderAndFooter(player, getHeader(player), processedFooter)
    }

    override fun setProcessedFooter(player: Player, footer: String) {
        val parts = animationsService.textToAnimationParts(footer)

        createFooterSendableAnimation(parts, player, withPlaceholders = true).start()
    }

    override fun setHeaderAndFooter(player: Player, header: String, footer: String, withPlaceholders: Boolean) {
        if (config.bandwidth.preventDuplicatePackets) {
            val cachedHeader = getHeader(player)
            val cachedFooter = getFooter(player)

            if (header == cachedHeader && footer == cachedFooter) {
                return
            }

            if (NMSManager.versionIndex < 9) {
                player.setTitleManagerMetadata(headerMetadataKey, header)
                player.setTitleManagerMetadata(footerMetadataKey, footer)
            }
        }

        if (NMSManager.versionIndex >= 9) {
            player.setPlayerListHeaderFooter(header, footer)
        } else {
            NMSUtil.setHeaderAndFooter(player, header, footer)
        }
    }

    override fun setProcessedHeaderAndFooter(player: Player, header: String, footer: String) {
        setProcessedHeader(player, header)
        setProcessedFooter(player, footer)
    }

    override fun clearHeaderAndFooterCache(player: Player) {
        player.removeTitleManagerMetadata(headerMetadataKey)
        player.removeTitleManagerMetadata(footerMetadataKey)
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
