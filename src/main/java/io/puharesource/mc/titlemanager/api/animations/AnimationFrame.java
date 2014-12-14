package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.TitleManager;

public class AnimationFrame {

    private String rawText;
    private Object componentText;

    private int fadeIn = -1;
    private int stay = -1;
    private int fadeOut = -1;

    public AnimationFrame(String text, int fadeIn, int stay, int fadeOut) {
        setText(text);
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public String getText() {
        return rawText;
    }

    public void setText(String text) {
        rawText = text;
        componentText = TitleManager.getReflectionManager().getIChatBaseComponent(text);
    }

    public Object getComponentText() {
        return componentText;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public void setFadeIn(int fadeIn) {
        this.fadeIn = fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public void setStay(int stay) {
        this.stay = stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public void setFadeOut(int fadeOut) {
        this.fadeOut = fadeOut;
    }

    public int getTotalTime() {
        return fadeIn + stay + fadeOut;
    }
}
