package io.puharesource.mc.titlemanager.animations

import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame

data class StandardAnimationFrame(private var text: String, private var fadeIn: Int = -1, private var stay: Int = -1, private var fadeOut: Int = -1) : AnimationFrame {
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
