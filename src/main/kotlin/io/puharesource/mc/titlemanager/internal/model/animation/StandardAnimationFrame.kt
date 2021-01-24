package io.puharesource.mc.titlemanager.internal.model.animation

import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import java.util.regex.Pattern

data class StandardAnimationFrame(
    override var text: String,
    override var fadeIn: Int = -1,
    override var stay: Int = -1,
    override var fadeOut: Int = -1
) : AnimationFrame {
    companion object {
        private val timingsPattern: Pattern =
            """^\[(?<fadeIn>[-]?\d+);(?<stay>[-]?\d+);(?<fadeOut>[-]?\d+)](?<text>.+)$""".toRegex().toPattern()

        fun createFrame(text: String): AnimationFrame? {
            val matcher = timingsPattern.matcher(text)

            if (!matcher.find()) {
                return null
            }

            return StandardAnimationFrame(
                text = matcher.group("text"),
                fadeIn = matcher.group("fadeIn").toInt(),
                stay = matcher.group("stay").toInt(),
                fadeOut = matcher.group("fadeOut").toInt()
            )
        }
    }

    override val totalTime: Int
        get() = fadeIn + stay + fadeOut
}
