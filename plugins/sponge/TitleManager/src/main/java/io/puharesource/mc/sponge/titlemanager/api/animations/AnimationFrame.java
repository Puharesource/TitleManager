package io.puharesource.mc.sponge.titlemanager.api.animations;

import lombok.Getter;
import lombok.Setter;

/**
 * This is a frame used in every type of animation.
 */
public class AnimationFrame {
    @Getter @Setter private String text;

    @Getter @Setter private int fadeIn = -1;
    @Getter @Setter private int stay = -1;
    @Getter @Setter private int fadeOut = -1;

    public AnimationFrame(final String text, int fadeIn, int stay, int fadeOut) {
        setText(text);
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public int getTotalTime() {
        return fadeIn + stay + fadeOut;
    }
}
