package io.puharesource.mc.titlemanager.api.v2.animation;

/**
 * A class used to create animations from strings and animations.
 *
 * @param <T> Should be a {@link String} or {@link Animation}.
 *
 * @since 2.0.0
 */
public interface AnimationPart<T> {
    /**
     * @return The part that should be displayed.
     */
    T getPart();
}
