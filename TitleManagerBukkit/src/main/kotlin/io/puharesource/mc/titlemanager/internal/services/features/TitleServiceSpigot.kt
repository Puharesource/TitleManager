package io.puharesource.mc.titlemanager.internal.services.features

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.internal.extensions.getTitleManagerPlayer
import io.puharesource.mc.titlemanager.internal.model.animation.EasySendableAnimation
import io.puharesource.mc.titlemanager.internal.model.animation.PartBasedSendableAnimation
import io.puharesource.mc.titlemanager.internal.services.animation.AnimationsService
import io.puharesource.mc.titlemanager.internal.services.placeholder.PlaceholderService
import io.puharesource.mc.titlemanager.internal.services.task.SchedulerService
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import javax.inject.Inject

class TitleServiceSpigot @Inject constructor(
    private val plugin: io.puharesource.mc.titlemanager.TitleManagerPlugin,
    private val animationsService: AnimationsService,
    private val placeholderService: PlaceholderService,
    private val schedulerService: SchedulerService
) : TitleService {
    override fun sendTitle(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int, withPlaceholders: Boolean) {
        var processedTitle = title

        if (withPlaceholders) {
            processedTitle = placeholderService.replaceText(player, processedTitle)
        }

        player.getTitleManagerPlayer().sendTitle(processedTitle, fadeIn, stay, fadeOut)
    }

    override fun sendSubtitle(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int, withPlaceholders: Boolean) {
        var processedSubtitle = subtitle

        if (withPlaceholders) {
            processedSubtitle = placeholderService.replaceText(player, processedSubtitle)
        }

        player.getTitleManagerPlayer().sendSubtitle(processedSubtitle, fadeIn, stay, fadeOut)
    }

    override fun sendTitles(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int, withPlaceholders: Boolean) {
        var processedTitle = title
        var processedSubtitle = subtitle

        if (withPlaceholders) {
            processedTitle = placeholderService.replaceText(player, processedTitle)
            processedSubtitle = placeholderService.replaceText(player, processedSubtitle)
        }

        player.getTitleManagerPlayer().sendTitles(processedTitle, processedSubtitle, fadeIn, stay, fadeOut)
    }

    override fun sendTimings(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) {
        player.getTitleManagerPlayer().sendTitleTimings(fadeIn, stay, fadeOut)
    }

    override fun clearTitle(player: Player) {
        sendTitle(player, " ")
    }

    override fun clearSubtitle(player: Player) {
        sendSubtitle(player, " ")
    }

    override fun clearTitles(player: Player) {
        player.getTitleManagerPlayer().resetTitle()
    }

    override fun sendProcessedTitle(player: Player, text: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        val parts = animationsService.textToAnimationParts(placeholderService.replaceText(player, text))

        if (parts.size == 1 && parts.first().getPart() is String) {
            sendTitle(
                player,
                title = parts.first().getPart() as String,
                fadeIn = fadeIn,
                stay = stay,
                fadeOut = fadeOut,
                withPlaceholders = false
            )
        } else if (parts.size == 1 && parts.first().getPart() is Animation) {
            createTitleSendableAnimation(parts.first().getPart() as Animation, player, withPlaceholders = false).start()
        } else if (parts.isNotEmpty()) {
            createTitleSendableAnimation(parts, player, withPlaceholders = false).start()
        }
    }

    override fun sendProcessedSubtitle(player: Player, text: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        val parts = animationsService.textToAnimationParts(placeholderService.replaceText(player, text))

        if (parts.size == 1 && parts.first().getPart() is String) {
            sendSubtitle(
                player,
                subtitle = parts.first().getPart() as String,
                fadeIn = fadeIn,
                stay = stay,
                fadeOut = fadeOut,
                withPlaceholders = false
            )
        } else if (parts.size == 1 && parts.first().getPart() is Animation) {
            createSubtitleSendableAnimation(parts.first().getPart() as Animation, player, withPlaceholders = false).start()
        } else if (parts.isNotEmpty()) {
            createSubtitleSendableAnimation(parts, player, withPlaceholders = false).start()
        }
    }

    override fun createTitleSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(
            schedulerService,
            parts,
            player,
            {
                sendTitle(player, it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
            },
            onStop = {
                clearTitle(player)
            },
            fixedOnStop = {
                deleteRunningTitleAnimation(it)
            },
            fixedOnStart = { receiver, animation ->
                saveRunningTitleAnimation(receiver, animation)
            }
        )
    }

    override fun createSubtitleSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(
            schedulerService,
            parts,
            player,
            {
                sendSubtitle(player, it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
            },
            onStop = {
                clearSubtitle(player)
            },
            fixedOnStop = {
                deleteRunningSubtitleAnimation(it)
            },
            fixedOnStart = { receiver, animation ->
                saveRunningSubtitleAnimation(receiver, animation)
            }
        )
    }

    override fun createTitleSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(
            schedulerService,
            animation,
            player,
            {
                sendTitle(player, it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
            },
            onStop = {
                clearTitle(player)
            },
            fixedOnStop = {
                deleteRunningTitleAnimation(it)
            },
            fixedOnStart = { receiver, sendableAnimation ->
                saveRunningTitleAnimation(receiver, sendableAnimation)
            }
        )
    }

    override fun createSubtitleSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(
            schedulerService,
            animation,
            player,
            {
                sendSubtitle(player, it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
            },
            onStop = {
                clearSubtitle(player)
            },
            fixedOnStop = {
                deleteRunningSubtitleAnimation(it)
            },
            fixedOnStart = { receiver, sendableAnimation ->
                saveRunningSubtitleAnimation(receiver, sendableAnimation)
            }
        )
    }

    private fun saveRunningAnimation(player: Player, path: String, animation: SendableAnimation) {
        player.setMetadata("running-$path-animation", FixedMetadataValue(plugin, animation))
    }

    private fun deleteRunningAnimation(player: Player, path: String) {
        val fullPath = "running-$path-animation"

        if (player.hasMetadata(fullPath)) {
            val animation = player.getMetadata(fullPath).first().value() as SendableAnimation

            animation.stop()

            player.removeMetadata(fullPath, plugin)
        }
    }

    private fun saveRunningTitleAnimation(player: Player, animation: SendableAnimation) = saveRunningAnimation(player, "title", animation)
    private fun deleteRunningTitleAnimation(player: Player) = deleteRunningAnimation(player, "title")

    private fun saveRunningSubtitleAnimation(player: Player, animation: SendableAnimation) = saveRunningAnimation(player, "subtitle", animation)
    private fun deleteRunningSubtitleAnimation(player: Player) = deleteRunningAnimation(player, "subtitle")
}
