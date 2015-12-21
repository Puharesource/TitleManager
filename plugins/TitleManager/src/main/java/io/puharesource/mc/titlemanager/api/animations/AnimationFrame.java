package io.puharesource.mc.titlemanager.api.animations;

/**
 * This is a frame used in every type of animation.
 */
public class AnimationFrame {
    private String text;

    private int fadeIn = -1;
    private int stay = -1;
    private int fadeOut = -1;

    public AnimationFrame(final String text, int fadeIn, int stay, int fadeOut) {
        setText(text);
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public void setFadeIn(final int fadeIn) {
        this.fadeIn = fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public void setStay(final int stay) {
        this.stay = stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public void setFadeOut(final int fadeOut) {
        this.fadeOut = fadeOut;
    }

    public int getTotalTime() {
        return fadeIn + stay + fadeOut;
    }
}
