package io.puharesource.mc.titlemanager.animations

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.scheduling.AsyncScheduler
import org.bukkit.entity.Player

class EasySendableAnimation(private val animation: Animation,
                            private val player: Player,
                            private val onUpdate: (AnimationFrame) -> Unit,
                            private var continuous: Boolean = false,
                            private var onStop: Runnable? = null,
                            private val fixedOnStop: ((Player) -> Unit)? = null,
                            private val fixedOnStart: ((Player, SendableAnimation) -> Unit)? = null) : SendableAnimation {
    private var iterator = animation.iterator(player)
    private var running: Boolean = false

    override fun start() {
        if (!running && player.isOnline) {
            running = true

            fixedOnStart?.invoke(player, this)

            update(iterator.next())
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
        if (!player.isOnline) stop()
        if (!running) return

        onUpdate(frame)

        if (!iterator.hasNext() && continuous) {
            iterator = animation.iterator(player)
        }

        if (iterator.hasNext()) {
            AsyncScheduler.schedule({ update(iterator.next()) }, frame.totalTime)
        } else {
            AsyncScheduler.schedule({ stop() }, frame.totalTime)
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