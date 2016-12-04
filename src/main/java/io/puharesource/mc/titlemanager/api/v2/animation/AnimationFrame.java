package io.puharesource.mc.titlemanager.api.v2.animation;

/**
 * An interface to contain the information needed for Animations.
 *
 * @since 2.0.0
 */
public interface AnimationFrame {
    /**
     * @return The text that should be replaced.
     *
     * @see #setText(String)
     *
     * @since 2.0.0
     */
    String getText();

    /**
     * Sets the text to be displayed.
     *
     * @param text The text to be displayed.
     *
     * @see #getText()
     *
     * @since 2.0.0
     */
    void setText(final String text);

    /**
     * If the frame is used in a title or subtitle,
     * then this will be the time it takes for the title to fade onto the screen.
     *
     * @return The time it takes for a title to fade onto the screen.
     *
     * @see #setFadeIn(int)
     *
     * @since 2.0.0
     */
    int getFadeIn();

    /**
     * Sets the time it takes for a title to fade onto the screen.
     *
     * @param fadeIn The time it takes for a title or subtitle to fade onto the screen.
     *
     * @see #getFadeIn()
     *
     * @since 2.0.0
     */
    void setFadeIn(final int fadeIn);

    /**
     * @return The time the frame should be shown.
     *
     * @see #setStay(int)
     *
     * @since 2.0.0
     */
    int getStay();

    /**
     * Sets the time the frame should be shown.
     *
     * @param stay The time the frame should be shown.
     *
     * @see #getStay()
     *
     * @since 2.0.0
     */
    void setStay(final int stay);

    /**
     * If the frame is used in a title or subtitle,
     * then this will be the time it takes for the title to fade off of the screen.
     *
     * @return The time it takes for a title to fade off of the screen.
     *
     * @see #setFadeOut(int)
     *
     * @since 2.0.0
     */
    int getFadeOut();

    /**
     * Sets the time it takes for a title or subtitle, to fade off of the screen.
     *
     * @param fadeOut The time it takes for a title or subtitle to fade off of the screen.
     *
     * @see #getFadeOut()
     *
     * @since 2.0.0
     */
    void setFadeOut(final int fadeOut);

    /**
     * The combined time of {@link #getFadeIn()}, {@link #getStay()}, {@link #getFadeOut()}.
     *
     * @return The combined time the frame should stay on the screen.
     *
     * @see #getFadeIn()
     * @see #getStay()
     * @see #getFadeOut()
     *
     * @see #setFadeIn(int)
     * @see #setStay(int)
     * @see #setFadeOut(int)
     *
     * @since 2.0.0
     */
    int getTotalTime();
}
