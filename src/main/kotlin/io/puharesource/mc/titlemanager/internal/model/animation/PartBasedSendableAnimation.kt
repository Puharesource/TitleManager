package io.puharesource.mc.titlemanager.internal.model.animation

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.internal.services.task.SchedulerService
import org.bukkit.entity.Player

class PartBasedSendableAnimation(
    private val schedulerService: SchedulerService,
    parts: List<AnimationPart<*>>,
    private val player: Player,
    private val onUpdate: (AnimationFrame) -> Unit,
    private var continuous: Boolean = false,
    private var onStop: Runnable? = null,
    private val tickRate: Long = 50,
    private val fixedOnStop: ((Player) -> Unit)? = null,
    private val fixedOnStart: ((Player, SendableAnimation) -> Unit)? = null
) : SendableAnimation {
    private var sendableParts: List<SendablePart>

    private var running: Boolean = false
    private var ticksRun: Int = 0

    init {
        this.sendableParts = parts.mapNotNull { animationPartToSendablePart(it) }
    }

    private fun animationPartToSendablePart(animationPart: AnimationPart<*>): SendablePart? {
        return when (animationPart.part) {
            is Animation -> AnimationSendablePart(player = player, part = animationPart as AnimationPart<Animation>, isContinuous = isContinuous)
            is String -> StringSendablePart(animationPart as AnimationPart<String>, isContinuous = isContinuous)
            else -> null
        }
    }

    private fun getCurrentFrameText(): AnimationFrame {
        val textParts = sendableParts
                .map {
                    if (it is AnimationSendablePart && it.isDone()) {
                        ""
                    } else {
                        it.updateText(ticksRun)
                        it.getCurrentText()
                    }
                }.toTypedArray()

        return StandardAnimationFrame(textParts.joinToString(separator = ""), 0, 2, 0)
    }

    override fun start() {
        if (!running && player.isOnline) {
            running = true

            fixedOnStart?.invoke(player, this)

            update(getCurrentFrameText())
        }
    }

    override fun stop() {
        if (running) {
            running = false

            if (player.isOnline) {
                onStop?.run()
                fixedOnStop?.invoke(player)
            }
        }
    }

    override fun update(frame: AnimationFrame) {
        if (!player.isOnline) {
            stop()
            return
        }

        if (!running) return

        ticksRun++

        onUpdate(frame)

        val isDone = !isContinuous && sendableParts.filter { it.isDone() }.size == sendableParts.size

        if (isDone) {
            schedulerService.scheduleRaw({ stop() }, 1 * tickRate)
        } else {
            schedulerService.scheduleRaw({ update(getCurrentFrameText()) }, 1 * tickRate)
        }
    }

    override fun onStop(onStop: Runnable) {
        this.onStop = onStop
    }

    override fun setContinuous(continuous: Boolean) {
        this.continuous = continuous
    }

    override fun isContinuous() = continuous

    override fun isRunning() = running
}
