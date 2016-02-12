package io.puharesource.mc.sponge.titlemanager.api.animations;

import com.google.common.base.Optional;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationIterable;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.text.Text;

public final class AnimationToken {
    private final Object object;

    private AnimationToken(final Text text) {
        this.object = text;
    }

    private AnimationToken(final AnimationIterable iterable) {
        this.object = iterable;
    }

    public static AnimationToken of(final Text text) {
        Validate.notNull(text);

        return new AnimationToken(text);
    }

    public static AnimationToken of(final AnimationIterable iterable) {
        Validate.notNull(iterable);

        return new AnimationToken(iterable);
    }

    public boolean isText() {
        return getText().isPresent();
    }

    public Optional<Text> getText() {
        return Optional.fromNullable(object instanceof Text ? (Text) object : null);
    }

    public boolean isIterable() {
        return getIterable().isPresent();
    }

    public Optional<AnimationIterable> getIterable() {
        return Optional.fromNullable(object instanceof AnimationIterable ? (AnimationIterable) object : null);
    }
}
