package io.puharesource.mc.titlemanager.internal.services.features

import io.puharesource.mc.titlemanager.TitleManagerPlugin
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.internal.model.animation.EasySendableAnimation
import io.puharesource.mc.titlemanager.internal.model.animation.PartBasedSendableAnimation
import io.puharesource.mc.titlemanager.internal.reflections.NMSManager
import io.puharesource.mc.titlemanager.internal.reflections.NMSUtil
import io.puharesource.mc.titlemanager.internal.services.animation.AnimationsService
import io.puharesource.mc.titlemanager.internal.services.placeholder.PlaceholderService
import io.puharesource.mc.titlemanager.internal.services.task.SchedulerService
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import javax.inject.Inject

class TitleServiceSpigot @Inject constructor(
    private val plugin: TitleManagerPlugin,
    private val animationsService: AnimationsService,
    private val placeholderService: PlaceholderService,
    private val schedulerService: SchedulerService
) : TitleService {
    override fun sendTitle(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int, withPlaceholders: Boolean) {
        var processedTitle = title

        if (withPlaceholders) {
            processedTitle = placeholderService.replaceText(player, processedTitle)
        }

        if (NMSManager.versionIndex >= 9) {
            player.sendTitle(processedTitle, null, fadeIn, stay, fadeOut)
        } else {
            NMSUtil.sendTitle(player, processedTitle, fadeIn, stay, fadeOut)
        }
    }

    override fun sendSubtitle(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int, withPlaceholders: Boolean) {
        var processedSubtitle = subtitle

        if (withPlaceholders) {
            processedSubtitle = placeholderService.replaceText(player, processedSubtitle)
        }

        if (NMSManager.versionIndex >= 9) {
            player.sendTitle("", processedSubtitle, fadeIn, stay, fadeOut)
        } else {
            NMSUtil.sendSubtitle(player, processedSubtitle, fadeIn, stay, fadeOut)
        }
    }

    override fun sendTitles(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int, withPlaceholders: Boolean) {
        var processedTitle = title
        var processedSubtitle = title

        if (withPlaceholders) {
            processedTitle = placeholderService.replaceText(player, processedTitle)
            processedSubtitle = placeholderService.replaceText(player, processedSubtitle)
        }

        if (NMSManager.versionIndex >= 9) {
            player.sendTitle(processedTitle, processedSubtitle, fadeIn, stay, fadeOut)

            return
        }

        NMSUtil.sendTitle(player, processedTitle, fadeIn, stay, fadeOut)
        NMSUtil.sendSubtitle(player, processedSubtitle, fadeIn, stay, fadeOut)
    }

    override fun sendTimings(player: Player, fadeIn: Int, stay: Int, fadeOut: Int) {
        if (NMSManager.versionIndex >= 9) {
            player.sendTitle(null, null, fadeIn, stay, fadeOut)

            return
        }

        NMSUtil.sendTimings(player, fadeIn, stay, fadeOut)
    }

    override fun clearTitle(player: Player) {
        sendTitle(player, " ")
    }

    override fun clearSubtitle(player: Player) {
        sendSubtitle(player, " ")
    }

    override fun clearTitles(player: Player) {
        if (NMSManager.versionIndex >= 9) {
            player.resetTitle()

            return
        }

        sendTitles(player, " ", " ") // TODO: Make this actually use the "Clear" enum.
    }

    override fun sendProcessedTitle(player: Player, text: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        val parts = animationsService.textToAnimationParts(text)

        if (parts.size == 1 && parts.first().part is String) {
            sendTitle(player,
                    title = parts.first().part as String,
                    fadeIn = fadeIn,
                    stay = stay,
                    fadeOut = fadeOut,
                    withPlaceholders = true)
        } else if (parts.size == 1 && parts.first().part is Animation) {
            createTitleSendableAnimation(parts.first().part as Animation, player, withPlaceholders = true).start()
        } else if (parts.isNotEmpty()) {
            createTitleSendableAnimation(parts, player, withPlaceholders = true).start()
        }
    }

    override fun sendProcessedSubtitle(player: Player, text: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        val parts = animationsService.textToAnimationParts(text)

        if (parts.size == 1 && parts.first().part is String) {
            sendSubtitle(player,
                    subtitle = parts.first().part as String,
                    fadeIn = fadeIn,
                    stay = stay,
                    fadeOut = fadeOut,
                    withPlaceholders = true)
        } else if (parts.size == 1 && parts.first().part is Animation) {
            createSubtitleSendableAnimation(parts.first().part as Animation, player, withPlaceholders = true).start()
        } else if (parts.isNotEmpty()) {
            createSubtitleSendableAnimation(parts, player, withPlaceholders = true).start()
        }
    }

    override fun createTitleSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(schedulerService, parts, player, {
            sendTitle(player, it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            clearTitle(player)
        }, fixedOnStop = { deleteRunningTitleAnimation(it) }, fixedOnStart = { receiver, animation -> saveRunningTitleAnimation(receiver, animation) })
    }

    override fun createSubtitleSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(schedulerService, parts, player, {
            sendSubtitle(player, it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            clearSubtitle(player)
        }, fixedOnStop = { deleteRunningSubtitleAnimation(it) }, fixedOnStart = { receiver, animation -> saveRunningSubtitleAnimation(receiver, animation) })
    }

    override fun createTitleSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(schedulerService, animation, player, {
            sendTitle(player, it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            clearTitle(player)
        }, fixedOnStop = { deleteRunningTitleAnimation(it) }, fixedOnStart = { receiver, sendableAnimation -> saveRunningTitleAnimation(receiver, sendableAnimation) })
    }

    override fun createSubtitleSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(schedulerService, animation, player, {
            sendSubtitle(player, it.text, fadeIn = it.fadeIn, stay = it.stay + 1, fadeOut = it.fadeOut, withPlaceholders = withPlaceholders)
        }, onStop = Runnable {
            clearSubtitle(player)
        }, fixedOnStop = { deleteRunningSubtitleAnimation(it) }, fixedOnStart = { receiver, sendableAnimation -> saveRunningSubtitleAnimation(receiver, sendableAnimation) })
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
