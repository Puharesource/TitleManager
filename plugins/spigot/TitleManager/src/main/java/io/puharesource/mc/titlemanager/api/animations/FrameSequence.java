package io.puharesource.mc.titlemanager.api.animations;

import com.google.common.collect.ImmutableList;
import io.puharesource.mc.titlemanager.api.TextConverter;
import io.puharesource.mc.titlemanager.api.iface.AnimationIterable;
import io.puharesource.mc.titlemanager.api.iface.Script;
import lombok.val;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;

/**
 * This class is similar to a list, in which it stores a sequence of AnimationFrame's.
 * This class is used in all types of animations.
 */
public class FrameSequence implements AnimationIterable {
    private @Deprecated int fadeIn;
    private @Deprecated int stay;
    private @Deprecated int fadeOut;
    private @Deprecated int totalTime;

    private final ImmutableList<AnimationFrame> frames;
    private final Script script;
    private final String originalString;

    public FrameSequence(final List<AnimationFrame> frames) {
        this.frames = ImmutableList.copyOf(frames);
        script = null;
        originalString = null;

        for (int i = 0; frames.size() > i; i++) {
            AnimationFrame frame = frames.get(i);
            if (i == 0) {
                fadeIn = (frame.getFadeIn() == -1 ? 0 : frame.getFadeIn());
                stay += (frame.getStay() == -1 ? 0 : frame.getStay());
                stay += (frame.getFadeOut() == -1 ? 0 : frame.getFadeOut());
            } else if (i + 1 == frames.size()) {
                stay += (frame.getFadeIn() == -1 ? 0 : frame.getFadeIn());
                stay += (frame.getStay() == -1 ? 0 : frame.getStay());
                fadeOut = (frame.getFadeOut() == -1 ? 0 : frame.getFadeOut());
            } else {
                stay += (frame.getFadeIn() == -1 ? 0 : frame.getFadeIn());
                stay += (frame.getStay() == -1 ? 0 : frame.getStay());
                stay += (frame.getFadeOut() == -1 ? 0 : frame.getFadeOut());
            }
            totalTime += frame.getTotalTime();
        }
    }

    public FrameSequence(final Script script, final String originalString) {
        frames = null;
        this.script = script;
        this.originalString = originalString;
    }

    @Deprecated
    public List<AnimationFrame> getFrames() {
        return frames;
    }

    @Deprecated
    public int size() {
        return frames.size();
    }

    @Deprecated
    public int getFadeIn() {
        return fadeIn;
    }

    @Deprecated
    public int getStay() {
        return stay;
    }

    @Deprecated
    public int getFadeOut() {
        return fadeOut;
    }

    @Deprecated
    public int getTotalTime() {
        return totalTime;
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
            frame.setText(TextConverter.setVariables(player, frame.getText()));
            return frame;
        }
    }
}
