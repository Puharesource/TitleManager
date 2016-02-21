package io.puharesource.mc.sponge.titlemanager.api.animations;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationIterable;
import io.puharesource.mc.sponge.titlemanager.api.iface.Script;
import io.puharesource.mc.sponge.titlemanager.utils.MiscellaneousUtils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * This class is similar to a list, in which it stores a sequence of AnimationFrame's.
 * This class is used in all types of animations.
 */
public class FrameSequence implements AnimationIterable {
    @Inject private TitleManager plugin;

    private Optional<ImmutableList<AnimationFrame>> frames;
    private Optional<Script> script;
    private Optional<Text> originalText;

    public FrameSequence(final List<AnimationFrame> frames) {
        Validate.notEmpty(frames);

        this.frames = Optional.of(ImmutableList.copyOf(frames));
        this.script = Optional.empty();
        this.originalText = Optional.empty();
    }

    public FrameSequence(final Script script, final Text originalText) {
        this.frames = Optional.empty();
        this.script = Optional.of(script);
        this.originalText = Optional.of(originalText);
    }

    @Override
    public Iterator<AnimationFrame> iterator(final Player player) {
        Validate.notNull(player);

        return frames.isPresent() ?
                new AnimationFrameIterator(frames.get().iterator(), player) :
                script.get().getIterator(originalText.get(), player);
    }

    private class AnimationFrameIterator implements Iterator<AnimationFrame> {
        private final Iterator<AnimationFrame> iterator;
        private final Player player;

        public AnimationFrameIterator(final Iterator<AnimationFrame> iterator, final Player player) {
            this.iterator = iterator;
            this.player = player;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public AnimationFrame next() {
            final AnimationFrame frame = iterator.next();
            frame.setText(plugin.replacePlaceholders(player, frame.getText()));
            return frame;
        }
    }

    public static class Serializer implements TypeSerializer<FrameSequence> {
        @Inject private TitleManager plugin;

        @Override
        public FrameSequence deserialize(final TypeToken<?> type, final ConfigurationNode value) throws ObjectMappingException {
            final ConfigurationNode nodeScript = value.getNode("script");
            final ConfigurationNode nodeText = value.getNode("text");
            final ConfigurationNode nodeFrames = value.getNode("frames");

            if (!nodeScript.isVirtual()) {
                final Optional<Script> script = plugin.getConfigHandler().getScript(value.getString());

                if (!script.isPresent()) return null;

                return new FrameSequence(script.get(), nodeText.isVirtual() ? Text.EMPTY : MiscellaneousUtils.format(nodeText.getString()));
            }

            return nodeFrames.isVirtual() ? null : new FrameSequence(nodeFrames.getList(TypeToken.of(AnimationFrame.class)));
        }

        @Override
        public void serialize(final TypeToken<?> type, final FrameSequence sequence, final ConfigurationNode value) throws ObjectMappingException {
            if (sequence.script.isPresent()) {
                value.getNode("script").setValue(sequence.script.get().getName());
                value.getNode("text").setValue(sequence.originalText.get());
            } else {
                value.getNode("frames").setValue(sequence.frames);
            }
        }
    }
}
