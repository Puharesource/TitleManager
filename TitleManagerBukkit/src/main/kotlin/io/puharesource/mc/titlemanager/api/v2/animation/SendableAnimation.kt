package io.puharesource.mc.titlemanager.api.v2.animation

/**
 * An [Animation] associated with a [org.bukkit.entity.Player] that can be sent to the associated [org.bukkit.entity.Player].
 *
 * @see start
 * @see stop
 *
 * @since 2.0.0
 */
interface SendableAnimation {
    /**
     * Starts sending the frames of the Animation to the associated [org.bukkit.entity.Player].
     *
     * @see stop
     *
     * @since 2.0.0
     */
    fun start()

    /**
     * Stops sending the frames of the Animation to the associated [org.bukkit.entity.Player].
     *
     * @see start
     *
     * @since 2.0.0
     */
    fun stop()

    /**
     * Sends the given [AnimationFrame] to the associated [org.bukkit.entity.Player]
     * and tells the frame to schedule the next frame to be sent.
     *
     * @param frame The frame to be sent.
     *
     * @since 2.0.0
     */
    fun update(frame: AnimationFrame?)

    /**
     * Runs the [Runnable] when the animation is done sending or [.stop] is called.
     *
     * @param runnable The runnable to be run, when the animation is done sending.
     *
     * @see stop
     *
     * @since 2.0.0
     */
    fun onStop(runnable: Runnable?)

    /**
     * If `true`, then the Animation will loop when it hits the last frame,
     * or until [stop] is called.
     *
     * @see stop
     *
     * @since 2.0.0
     */
    var isContinuous: Boolean

    /**
     * Checks whether or not the animation is currently running.
     *
     * If `true` the animation is running and scheduling sending of the frames.
     * If `false` the animation isn't running.
     *
     * @see start
     * @see stop
     *
     * @since 2.0.0
     */
    val isRunning: Boolean
}
