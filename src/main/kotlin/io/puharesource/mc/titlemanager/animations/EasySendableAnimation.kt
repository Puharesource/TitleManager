package io.puharesource.mc.titlemanager.animations

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import io.puharesource.mc.titlemanager.scheduling.AsyncScheduler
import org.bukkit.entity.Player

class EasySendableAnimation : SendableAnimation {
    private val animation: Animation
    private val player: Player
    private val onUpdate: (AnimationFrame) -> Unit

    private var iterator: Iterator<AnimationFrame>
    private var continuous: Boolean
    private var onStop: Runnable?

    private var running: Boolean = false

    constructor(animation: Animation, player: Player, onUpdate: (AnimationFrame) -> Unit, continuous: Boolean = false, onStop: Runnable? = null) {
        this.animation = animation
        this.player = player
        this.onUpdate = onUpdate

        this.iterator = animation.iterator(player)
        this.continuous = continuous
        this.onStop = onStop
    }

    override fun start() {
        if (!running) {
            running = true

            update(iterator.next())
        }
    }

    override fun stop() {
        if (running) {
            running = false

            if (onStop != null && player.isOnline) {
                onStop!!.run()
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

    override fun isContinuous(): Boolean {
        return continuous
    }
}