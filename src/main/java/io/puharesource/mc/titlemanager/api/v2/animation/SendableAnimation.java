package io.puharesource.mc.titlemanager.api.v2.animation;

/**
 * An {@link Animation} associated with a {@link org.bukkit.entity.Player} that can be sent to the associated {@link org.bukkit.entity.Player}.
 *
 * @see #start() to start sending the animation.
 * @see #stop() to stop sending the animation.
 *
 * @since 2.0.0
 */
public interface SendableAnimation {
    /**
     * Starts sending the frames of the Animation to the associated {@link org.bukkit.entity.Player}.
     *
     * @see #stop()
     *
     * @since 2.0.0
     */
    void start();

    /**
     * Stops sending the frames of the Animation to the associated {@link org.bukkit.entity.Player}.
     *
     * @see #start()
     *
     * @since 2.0.0
     */
    void stop();

    /**
     * Sends the given {@link AnimationFrame} to the associated {@link org.bukkit.entity.Player}
     * and tells the frame to schedule the next frame to be sent.
     *
     * @param frame The frame to be sent.
     *
     * @since 2.0.0
     */
    void update(AnimationFrame frame);

    /**
     * Runs the {@link Runnable} when the animation is done sending or {@link #stop()} is called.
     *
     * @param runnable The runnable to be run, when the animation is done sending.
     *
     * @see #stop()
     *
     * @since 2.0.0
     */
    void onStop(Runnable runnable);

    /**
     * If continuous is <code>true</code>, then the Animation will loop when it hits the last frame,
     * or until {@link #stop()} is called.
     *
     * @param continuous Whether or not the animation should loop.
     *
     * @see #isContinuous()
     * @see #stop()
     *
     * @since 2.0.0
     */
    void setContinuous(boolean continuous);

    /**
     * Checks whether or not the animation will loop when it reaches the end.
     *
     * @return If <code>true</code> then the animation will loop when it reaches the end.
     *         If <code>false</code> then the animation will call {@link #stop()} when it reaches the end.
     *
     * @see #setContinuous(boolean)
     * @see #stop()
     *
     * @since 2.0.0
     */
    boolean isContinuous();

    /**
     * Checks whether or not the animation is currently running.
     *
     * @return If <code>true</code> the animation is running and scheduling sending of the frames.
     *         If <code>false</code> the animation isn't running.
     *
     * @see #start()
     * @see #stop()
     *
     * @since 2.0.0
     */
    boolean isRunning();
}
