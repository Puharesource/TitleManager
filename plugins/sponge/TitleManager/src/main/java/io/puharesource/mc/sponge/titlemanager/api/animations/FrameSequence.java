package io.puharesource.mc.sponge.titlemanager.api.animations;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationIterable;
import io.puharesource.mc.sponge.titlemanager.api.iface.Script;
import lombok.val;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Iterator;
import java.util.List;

/**
 * This class is similar to a list, in which it stores a sequence of AnimationFrame's.
 * This class is used in all types of animations.
 */
public class FrameSequence implements AnimationIterable {
    @Inject private TitleManager plugin;

    private final ImmutableList<AnimationFrame> frames;
    private final Script script;
    private final String originalString;

    public FrameSequence(final List<AnimationFrame> frames) {
        this.frames = ImmutableList.copyOf(frames);
        script = null;
        originalString = null;
    }

    public FrameSequence(final Script script, final String originalString) {
        frames = null;
        this.script = script;
        this.originalString = originalString;
    }

    @Override
    public Iterator<AnimationFrame> iterator(final Player player) {
        return frames != null ?
                new AnimationFrameIterator(frames.iterator(), player) :
                script.getIterator(originalString, player);
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
            val frame = iterator.next();
            frame.setText(plugin.replacePlaceholders(player, frame.getText()));
            return frame;
        }
    }
}
