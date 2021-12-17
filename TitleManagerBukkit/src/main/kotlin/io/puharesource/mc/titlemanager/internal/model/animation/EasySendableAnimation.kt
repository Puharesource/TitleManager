package io.puharesource.mc.titlemanager.internal.model.animation

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.internal.services.task.SchedulerService
import org.bukkit.entity.Player

class EasySendableAnimation(
    private val schedulerService: SchedulerService,
    private val animation: Animation,
    private val player: Player,
    private val onUpdate: (AnimationFrame) -> Unit,
    override var isContinuous: Boolean = false,
    private var onStop: Runnable? = null,
    private val tickRate: Long = 50,
    private val fixedOnStop: ((Player) -> Unit)? = null,
    private val fixedOnStart: ((Player, SendableAnimation) -> Unit)? = null
) : SendableAnimation {
    private var iterator = animation.iterator(player)
    override var isRunning = false
        private set

    override fun start() {
        if (!isRunning && player.isOnline) {
            isRunning = true

            fixedOnStart?.invoke(player, this)

            update(iterator.next())
        }
    }

    override fun stop() {
        if (isRunning) {
            isRunning = false

            if (player.isOnline) {
                onStop?.run()
                fixedOnStop?.invoke(player)
            }
        }
    }

    override fun update(frame: AnimationFrame?) {
        if (!isRunning) return

        if (frame == null) {
            stop()
            return
        }

        if (!player.isOnline) {
            stop()
            return
        }

        onUpdate(frame)

        if (!iterator.hasNext() && isContinuous) {
            iterator = animation.iterator(player)
        }

        if (iterator.hasNext()) {
            schedulerService.scheduleRaw({ update(iterator.next()) }, frame.totalTime * tickRate)
        } else {
            schedulerService.scheduleRaw({ stop() }, frame.totalTime * tickRate)
        }
    }

    override fun onStop(runnable: Runnable?) {
        this.onStop = runnable
    }
}
