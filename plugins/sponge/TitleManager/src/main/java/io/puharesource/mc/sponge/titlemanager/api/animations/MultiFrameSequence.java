package io.puharesource.mc.sponge.titlemanager.api.animations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationIterable;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.*;

public class MultiFrameSequence implements AnimationIterable {
    @Inject private TitleManager plugin;

    private final ImmutableList<AnimationToken> tokens;

    public MultiFrameSequence(final List<AnimationToken> parts) {
        Validate.notEmpty(parts);

        this.tokens = ImmutableList.copyOf(parts);
    }

    @Override
    public Iterator<AnimationFrame> iterator(final Player player) {
        return new MultiAnimationFrameIterator(tokens, player);
    }

    @EqualsAndHashCode
    private final class FrameValue {
        private AnimationToken value;
        private Optional<Iterator<AnimationFrame>> iterator = Optional.empty();
        private int timeLeft;

        public FrameValue(final Player player, final AnimationToken value) {
            this.value = value;

            if (value.isIterable()) {
                this.iterator = Optional.of(value.getIterable().get().iterator(player));
                timeLeft = 0;
            } else {
                this.timeLeft = -1;
            }
        }

        public void reset(final Player player) {
            if (value.isIterable()) {
                this.iterator = Optional.of(value.getIterable().get().iterator(player));
            }
        }
    }

    private class MultiAnimationFrameIterator implements Iterator<AnimationFrame> {
        private final ImmutableMap<Integer, FrameValue> parts;
        private final Text[] renderedParts;
        private final boolean[] doneParts;
        private final Player player;

        public MultiAnimationFrameIterator(final List<AnimationToken> parts, final Player player) {
            final Map<Integer, FrameValue> values = new HashMap<>();

            for (int i = 0; parts.size() > i; i++) {
                values.put(i, new FrameValue(player, parts.get(i)));
            }

            this.parts = ImmutableMap.copyOf(values);
            this.renderedParts = new Text[parts.size()];
            this.doneParts = new boolean[parts.size()];
            this.player = player;
        }

        @Override
        public boolean hasNext() {
            for (final boolean done : doneParts) {
                if (!done) return true;
            }

            return false;
        }

        @Override
        public AnimationFrame next() {
            int lowestTimings = 0;
            boolean hasWrittenTimings = false;

            for (int i = 0; parts.size() > i; i++) {
                final FrameValue frameValue = parts.get(i);

                if (i == 0 || renderedParts[i] == null) {
                    if (frameValue.value.isText()) {
                        renderedParts[i] = frameValue.value.getText().get();
                        doneParts[i] = true;
                    } else  {
                        final Iterator<AnimationFrame> iterator = frameValue.iterator.get();

                        if (!iterator.hasNext()) {
                            doneParts[i] = true;

                            if (renderedParts[i] == null) {
                                renderedParts[i] = Text.EMPTY;
                                continue;
                            } else {
                                frameValue.reset(player);
                            }
                        }

                        final AnimationFrame frame = iterator.next();
                        renderedParts[i] = frame.getText();
                        final int timings = frame.getTotalTime();
                        frameValue.timeLeft = timings;

                        if (!hasWrittenTimings || lowestTimings > timings) {
                            lowestTimings = timings;
                            hasWrittenTimings = true;
                        }
                    }
                }
            }

            if (lowestTimings == 0) {
                lowestTimings++;
            }

            final Text animationText = Text.of(renderedParts);

            for (int i = 0; parts.size() > i; i++) {
                final FrameValue frameValue = parts.get(i);

                if (frameValue.timeLeft > 0) {
                    frameValue.timeLeft -= lowestTimings;
                }
            }

            return new AnimationFrame(animationText, 0, lowestTimings, 0);
        }
    }
}
