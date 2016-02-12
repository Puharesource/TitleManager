package io.puharesource.mc.sponge.titlemanager.api.animations;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.text.Text;

/**
 * This is a frame used in every type of animation.
 */
public class AnimationFrame {
    @Getter private Text text;

    @Getter @Setter private int fadeIn = -1;
    @Getter @Setter private int stay = -1;
    @Getter @Setter private int fadeOut = -1;

    public AnimationFrame(final Text text, int fadeIn, int stay, int fadeOut) {
        setText(text);
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public void setText(final Text text) {
        Validate.notNull(text);

        this.text = text;
    }

    public int getTotalTime() {
        return fadeIn + stay + fadeOut;
    }
}
