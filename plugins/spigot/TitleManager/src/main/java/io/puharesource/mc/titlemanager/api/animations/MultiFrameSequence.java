package io.puharesource.mc.titlemanager.api.animations;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.puharesource.mc.titlemanager.api.TextConverter;
import io.puharesource.mc.titlemanager.api.iface.AnimationIterable;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;

import java.util.*;

public class MultiFrameSequence implements AnimationIterable {
    private final ImmutableList<Object> parts;

    public MultiFrameSequence(final List<Object> parts) {
        this.parts = ImmutableList.copyOf(parts);
    }

    @Override
    public Iterator<AnimationFrame> iterator(final Player player) {
        return new MultiAnimationFrameIterator(parts, player);
    }

    @EqualsAndHashCode
    private class FrameValue {
        private AnimationIterable iterable;
        private Object value;
        private int timeLeft;

        public FrameValue(final Player player, final Object value) {
            if (value instanceof AnimationIterable) {
                this.iterable = (AnimationIterable) value;
                this.value = this.iterable.iterator(player);
                timeLeft = 0;
            } else if (value instanceof String) {
                this.value = value;
                this.timeLeft = -1;
            } else throw new IllegalArgumentException("List must contain Strings and/or AnimationIterables!");
        }

        public void reset(final Player player) {
            if (iterable != null) {
                this.value = iterable.iterator(player);
            }
        }
    }

    private class MultiAnimationFrameIterator implements Iterator<AnimationFrame> {
        private final ImmutableMap<Integer, FrameValue> parts;
        private final String[] renderedParts;
        private final boolean[] doneParts;
        private final Player player;

        public MultiAnimationFrameIterator(final List<Object> parts, final Player player) {
            final Map<Integer, FrameValue> values = new HashMap<>();

            for (int i = 0; parts.size() > i; i++) {
                values.put(i, new FrameValue(player, parts.get(i)));
            }

            this.parts = ImmutableMap.copyOf(values);
            renderedParts = new String[parts.size()];
            doneParts = new boolean[parts.size()];
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

                if (renderedParts[i] == null || i == 0) {
                    if (frameValue.value instanceof String) {
                        renderedParts[i] = (String) frameValue.value;
                        doneParts[i] = true;
                    } else if (frameValue.value instanceof Iterator) {
                        final Iterator<AnimationFrame> iterator = (Iterator<AnimationFrame>) frameValue.value;
                        if (!iterator.hasNext()) {
                            doneParts[i] = true;

                            if (renderedParts[i] == null) {
                                renderedParts[i] = "";
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

            final String animationString = TextConverter.setVariables(player, Joiner.on("").skipNulls().join(renderedParts));

            for (int i = 0; parts.size() > i; i++) {
                final FrameValue frameValue = parts.get(i);

                frameValue.timeLeft -= lowestTimings;
                if (frameValue.timeLeft < 0) frameValue.timeLeft = 0;
            }

            return new AnimationFrame(animationString, 0, lowestTimings, 0);
        }
    }
}
