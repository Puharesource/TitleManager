package io.puharesource.mc.titlemanager.api.v2.animation

/**
 * A class used to create animations from strings and animations.
 *
 * @param <T> Should be a [String] or [Animation].
 *
 * @since 2.0.0
</T> */
fun interface AnimationPart<T> {
    /**
     * The part that should be displayed.
     */
     fun getPart(): T
}
