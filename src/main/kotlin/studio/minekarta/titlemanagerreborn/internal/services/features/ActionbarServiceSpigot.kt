package studio.minekarta.titlemanagerreborn.internal.services.features

import studio.minekarta.titlemanagerreborn.TitleManagerReborn
import studio.minekarta.titlemanagerreborn.api.v2.animation.Animation
import studio.minekarta.titlemanagerreborn.api.v2.animation.AnimationPart
import studio.minekarta.titlemanagerreborn.api.v2.animation.SendableAnimation
import studio.minekarta.titlemanagerreborn.internal.model.animation.EasySendableAnimation
import studio.minekarta.titlemanagerreborn.internal.model.animation.PartBasedSendableAnimation
import studio.minekarta.titlemanagerreborn.internal.reflections.NMSManager
import studio.minekarta.titlemanagerreborn.internal.reflections.NMSUtil
import studio.minekarta.titlemanagerreborn.internal.services.animation.AnimationsService
import studio.minekarta.titlemanagerreborn.internal.services.placeholder.PlaceholderService
import studio.minekarta.titlemanagerreborn.internal.services.task.SchedulerService
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import javax.inject.Inject

class ActionbarServiceSpigot @Inject constructor(
    private val plugin: TitleManagerReborn,
    private val placeholderService: PlaceholderService,
    private val animationsService: AnimationsService,
    private val schedulerService: SchedulerService
) : ActionbarService {
    override fun sendProcessedActionbar(player: Player, text: String) {
        val parts = animationsService.textToAnimationParts(placeholderService.replaceText(player, text))

        if (parts.size == 1 && parts.first().getPart() is String) {
            sendActionbar(player, text = parts.first().getPart() as String, withPlaceholders = false)
        } else if (parts.size == 1 && parts.first().getPart() is Animation) {
            createActionbarSendableAnimation(parts.first().getPart() as Animation, player, withPlaceholders = false).start()
        } else if (parts.isNotEmpty()) {
            createActionbarSendableAnimation(parts, player, withPlaceholders = false).start()
        }
    }

    override fun sendActionbar(player: Player, text: String, withPlaceholders: Boolean) {
        var processedText = text

        if (withPlaceholders) {
            processedText = placeholderService.replaceText(player, processedText)
        }

        if (NMSManager.versionIndex >= 5) {
            try {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(processedText))
            } catch (e: Exception) {
                error("To use Actionbar messages you need to run Spigot, not CraftBukkit!")
            }
        } else {
            NMSUtil.sendActionbar(player, processedText)
        }
    }

    override fun clearActionbar(player: Player) {
        sendActionbar(player, " ")
    }

    override fun createActionbarSendableAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return EasySendableAnimation(
            schedulerService,
            animation,
            player,
            {
                sendActionbar(player, it.text, withPlaceholders = withPlaceholders)
            },
            onStop = {
                clearActionbar(player)
            },
            fixedOnStop = {
                removeRunningActionbarAnimation(it)
            },
            fixedOnStart = { receiver, sendableAnimation ->
                setRunningActionbarAnimation(receiver, sendableAnimation)
            }
        )
    }

    override fun createActionbarSendableAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation {
        return PartBasedSendableAnimation(
            schedulerService,
            parts,
            player,
            {
                sendActionbar(player, it.text, withPlaceholders = withPlaceholders)
            },
            onStop = {
                clearActionbar(player)
            },
            fixedOnStop = {
                removeRunningActionbarAnimation(it)
            },
            fixedOnStart = { receiver, animation ->
                setRunningActionbarAnimation(receiver, animation)
            }
        )
    }

    private fun setRunningActionbarAnimation(player: Player, animation: SendableAnimation) {
        player.setMetadata("running-actionbar-animation", FixedMetadataValue(plugin, animation))
    }

    private fun removeRunningActionbarAnimation(player: Player) {
        val fullPath = "running-actionbar-animation"

        if (player.hasMetadata(fullPath)) {
            val animation = player.getMetadata(fullPath).first().value() as SendableAnimation

            animation.stop()

            player.removeMetadata(fullPath, plugin)
        }
    }
}
