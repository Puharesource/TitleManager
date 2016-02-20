package io.puharesource.mc.sponge.titlemanager.api.animations;

import com.google.common.collect.ImmutableList;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationIterable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MultiFrameSequence implements AnimationIterable {
    private final ImmutableList<AnimationToken> tokens;

    public MultiFrameSequence(final List<AnimationToken> parts) {
        Validate.notEmpty(parts);

        this.tokens = ImmutableList.copyOf(parts);
    }

    @Override
    public Iterator<AnimationFrame> iterator(final Player player) {
        return new MultiAnimationFrameIterator(tokens, player);
    }

    private interface FramePart {
        Text tick(Player player, int updateTicks);
        boolean hasFinishedOnce();
        int getTimings();
        int nextUpdate();
    }

    private final class TextPart implements FramePart {
        private final Text text;

        public TextPart(final Text text) {
            this.text = text;
        }

        @Override
        public Text tick(final Player player, final int updateTicks) {
            return text;
        }

        @Override
        public boolean hasFinishedOnce() {
            return true;
        }

        @Override
        public int getTimings() {
            return 1;
        }

        @Override
        public int nextUpdate() {
            return 1;
        }
    }

    private final class AnimationPart implements FramePart {
        private final AnimationIterable iterable;
        private Optional<Iterator<AnimationFrame>> iterator = Optional.empty();
        private Optional<Text> text = Optional.empty();
        private Optional<AnimationFrame> current = Optional.empty();
        private int ticks;
        private boolean hasFinishedOnce;

        public AnimationPart(final AnimationIterable iterable) {
            this.iterable = iterable;
        }

        @Override
        public Text tick(final Player player, final int updateTicks) {
            if (current.isPresent()) {
                if (current.get().getTotalTime() <= ticks) {
                    ticks = 0;

                    if (iterator.get().hasNext()) {
                        final AnimationFrame frame = iterator.get().next();
                        current = Optional.of(frame);
                        text = Optional.of(frame.getText());
                    } else {
                        hasFinishedOnce = true;
                        reset(player);
                    }
                } else {
                    ticks += updateTicks;
                }
            } else {
                reset(player);
            }

            return text.orElse(Text.EMPTY);
        }

        private void reset(final Player player) {
            final Iterator<AnimationFrame> it = iterable.iterator(player);
            iterator = Optional.of(it);
            final AnimationFrame frame = it.next();
            current = Optional.of(frame);
            text = Optional.of(frame.getText());
        }

        @Override
        public boolean hasFinishedOnce() {
            return hasFinishedOnce;
        }

        @Override
        public int getTimings() {
            return current.isPresent() ? current.get().getTotalTime() : 1;
        }

        @Override
        public int nextUpdate() {
            return current.isPresent() ? current.get().getTotalTime() - ticks : 1;
        }
    }

    @EqualsAndHashCode
    private final class FrameValue {
        @Getter private final FramePart part;

        public FrameValue(final AnimationToken value) {
            if (value.isText()) {
                part = new TextPart(value.getText().get());
            } else {
                part = new AnimationPart(value.getIterable().get());
            }
        }
    }

    private class MultiAnimationFrameIterator implements Iterator<AnimationFrame> {
        private final Player player;
        private final List<FrameValue> parts;
        private int lastUpdateTime = 0;

        public MultiAnimationFrameIterator(final List<AnimationToken> parts, final Player player) {
            this.player = player;
            this.parts = ImmutableList.copyOf(parts.stream().map(FrameValue::new).collect(Collectors.toList()));
        }

        @Override
        public boolean hasNext() {
            for (final FrameValue part : parts) {
                if (part.getPart().hasFinishedOnce()) return true;
            }

            return false;
        }

        @Override
        public AnimationFrame next() {
            final Text[] tickedParts = new Text[parts.size()];
            int nextUpdate = 0;

            for (int i = 0; parts.size() > i; i++) {
                final FrameValue value = parts.get(0);

                final int timings = value.getPart().getTimings();
                if (nextUpdate == 0 || timings > nextUpdate) {
                    nextUpdate = timings;
                }

                tickedParts[i] = value.getPart().tick(player, lastUpdateTime);
            }

            if (nextUpdate <= 0) {
                nextUpdate = 1;
            }

            final Text animationText = Text.of(tickedParts);
            lastUpdateTime = nextUpdate;
            return new AnimationFrame(animationText, 0, nextUpdate, 0);
        }
    }
}
