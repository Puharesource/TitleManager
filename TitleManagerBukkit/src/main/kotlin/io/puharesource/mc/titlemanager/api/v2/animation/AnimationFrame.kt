package io.puharesource.mc.titlemanager.api.v2.animation

/**
 * An interface to contain the information needed for Animations.
 *
 * @since 2.0.0
 */
interface AnimationFrame {
    /**
     * The text.
     *
     * @since 2.0.0
     */
    var text: String

    /**
     * If the frame is used in a title or subtitle,
     * then this will be the time it takes for the title to fade onto the screen.
     *
     * @since 2.0.0
     */
    var fadeIn: Int

    /**
     * The time the frame should be shown.
     *
     * @since 2.0.0
     */
    var stay: Int

    /**
     * If the frame is used in a title or subtitle,
     * then this will be the time it takes for the title to fade off of the screen.
     *
     * @see .setFadeOut
     * @since 2.0.0
     */
    var fadeOut: Int

    /**
     * The combined time the frame should stay on the screen. (A combination of [.fadeIn], [.stay], [.fadeOut])
     *
     * @return The combined time the frame should stay on the screen.
     *
     * @since 2.0.0
     */
    val totalTime: Int
}
