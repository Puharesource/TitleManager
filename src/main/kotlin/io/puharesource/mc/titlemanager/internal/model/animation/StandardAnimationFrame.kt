package io.puharesource.mc.titlemanager.internal.model.animation

import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import java.util.regex.Pattern

data class StandardAnimationFrame(
    private var text: String,
    private var fadeIn: Int = -1,
    private var stay: Int = -1,
    private var fadeOut: Int = -1
) : AnimationFrame {
    companion object {
        private val timingsPattern: Pattern = """^\[(?<fadeIn>[-]?\d+);(?<stay>[-]?\d+);(?<fadeOut>[-]?\d+)](?<text>.+)$""".toRegex().toPattern()

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

    override fun getText() = text

    override fun setText(text: String) {
        this.text = text
    }

    override fun getFadeIn() = fadeIn

    override fun setFadeIn(fadeIn: Int) {
        this.fadeIn = fadeIn
    }

    override fun getStay() = stay

    override fun setStay(stay: Int) {
        this.stay = stay
    }

    override fun getFadeOut() = fadeOut

    override fun setFadeOut(fadeOut: Int) {
        this.fadeOut = fadeOut
    }

    override fun getTotalTime() = fadeIn + stay + fadeOut
}
