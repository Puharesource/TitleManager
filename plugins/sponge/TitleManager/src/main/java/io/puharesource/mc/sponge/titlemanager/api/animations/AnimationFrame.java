package io.puharesource.mc.sponge.titlemanager.api.animations;

import com.google.common.reflect.TypeToken;
import io.puharesource.mc.sponge.titlemanager.utils.MiscellaneousUtils;
import lombok.Getter;
import lombok.Setter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
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

    public AnimationFrame(final Text text, final int fadeIn, final int stay, final int fadeOut) {
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

    public static class Serializer implements TypeSerializer<AnimationFrame> {
        @Override
        public AnimationFrame deserialize(final TypeToken<?> type, final ConfigurationNode value) throws ObjectMappingException {
            final ConfigurationNode nodeFadeIn = value.getNode("fade-in");
            final ConfigurationNode nodeStay = value.getNode("stay");
            final ConfigurationNode nodeFadeOut = value.getNode("fade-out");
            final ConfigurationNode nodeText = value.getNode("text");

            if (nodeText.isVirtual()) return null;

            final Text text = MiscellaneousUtils.format(nodeText.getString());
            final int fadeIn = nodeFadeIn.isVirtual() ? -1 : nodeFadeIn.getInt();
            final int stay = nodeStay.isVirtual() ? -1 : nodeStay.getInt();
            final int fadeOut = nodeFadeOut.isVirtual() ? -1 : nodeFadeOut.getInt();

            return new AnimationFrame(text, fadeIn, stay, fadeOut);
        }

        @Override
        public void serialize(final TypeToken<?> type, final AnimationFrame frame, final ConfigurationNode node) throws ObjectMappingException {
            node.getNode("fade-in").setValue(frame.getFadeIn());
            node.getNode("stay").setValue(frame.getStay());
            node.getNode("fade-out").setValue(frame.getFadeOut());
            node.getNode("text").setValue(frame.getText());
        }
    }
}
