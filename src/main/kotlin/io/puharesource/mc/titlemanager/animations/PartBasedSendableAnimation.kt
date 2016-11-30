package io.puharesource.mc.titlemanager.animations

import com.google.common.base.Joiner
import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.scheduling.AsyncScheduler
import org.bukkit.entity.Player

class PartBasedSendableAnimation(parts: List<AnimationPart<*>>,
                                 private val player: Player,
                                 private val onUpdate: (AnimationFrame) -> Unit,
                                 private var continuous: Boolean = false,
                                 private var onStop: Runnable? = null,
                                 private val fixedOnStop: ((Player) -> Unit)? = null,
                                 private val fixedOnStart: ((Player, SendableAnimation) -> Unit)? = null) : SendableAnimation {
    private var sendableParts: List<SendablePart>

    private var running: Boolean = false
    private var ticksRun: Int = 0

    private val joiner = Joiner.on("")

    init {
        this.sendableParts = parts.mapNotNull {
            if (it.part is Animation) {
                AnimationSendablePart(player = player, part = it as AnimationPart<Animation>, isContinuous = isContinuous)
            } else if (it.part is String) {
                StringSendablePart(it as AnimationPart<String>, isContinuous = isContinuous)
            } else {
                null
            }
        }
    }

    private fun getCurrentFrameText() : AnimationFrame {
        val textParts = sendableParts
                .map {
                    if (it is AnimationSendablePart && it.isDone()) {
                        ""
                    } else {
                        it.updateText(ticksRun)
                        it.getCurrentText()
                    }
                }.toTypedArray()

        return StandardAnimationFrame(joiner.join(textParts), 0, 2, 0)
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
            AsyncScheduler.schedule({ stop() }, 1)
        } else {
            AsyncScheduler.schedule({ update(getCurrentFrameText()) }, 1)
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

interface SendablePart {
    fun getCurrentText() : String
    fun updateText(currentTick: Int)
    fun isDone() : Boolean
}

data class AnimationSendablePart(private val player: Player,
                                 private val part: AnimationPart<Animation>,
                                 private var done: Boolean = false,
                                 private val isContinuous: Boolean) : SendablePart {
    private var currentText = ""
    private var iterator = part.part.iterator(player)
    private var nextUpdateTick = 0

    override fun getCurrentText() = currentText

    override fun updateText(currentTick: Int) {
        if (nextUpdateTick > currentTick) return

        if (!iterator.hasNext() && isContinuous) {
            iterator = part.part.iterator(player)
        } else if (!iterator.hasNext()) {
            return
        }

        val frame = iterator.next()

        currentText = frame.text
        nextUpdateTick = (if (frame.totalTime <= 0) 1 else frame.totalTime) + currentTick

        if (!iterator.hasNext() && !isContinuous) {
            done = true
        }
    }

    override fun isDone() = done
}

data class StringSendablePart(val part: AnimationPart<String>, private val isContinuous: Boolean) : SendablePart {
    override fun getCurrentText() : String = part.part
    override fun updateText(currentTick: Int) {}
    override fun isDone() = !isContinuous
}