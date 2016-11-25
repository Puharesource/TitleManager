package io.puharesource.mc.titlemanager.api.v2.animation;

public interface AnimationFrame {
    String getText();

    void setText(final String text);

    int getFadeIn();

    void setFadeIn(final int fadeIn);

    int getStay();

    void setStay(final int stay);

    int getFadeOut();

    void setFadeOut(final int fadeOut);

    int getTotalTime();
}
