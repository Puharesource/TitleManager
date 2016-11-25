package io.puharesource.mc.titlemanager.api.animations;

/**
 * This is a frame used in every type of animation.
 */
@Deprecated
public class AnimationFrame {
    private String text;

    private int fadeIn = -1;
    private int stay = -1;
    private int fadeOut = -1;

    @Deprecated
    public AnimationFrame(String text, int fadeIn, int stay, int fadeOut) {
        setText(text);
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    @Deprecated
    public String getText() {
        return text;
    }

    @Deprecated
    public void setText(String text) {
        this.text = text;
    }

    @Deprecated
    public int getFadeIn() {
        return fadeIn;
    }

    @Deprecated
    public void setFadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
    }

    @Deprecated
    public int getStay() {
        return stay;
    }

    @Deprecated
    public void setStay(int stay) {
        this.stay = stay;
    }

    @Deprecated
    public int getFadeOut() {
        return fadeOut;
    }

    @Deprecated
    public void setFadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
    }

    @Deprecated
    public int getTotalTime() {
        return fadeIn + stay + fadeOut;
    }
}