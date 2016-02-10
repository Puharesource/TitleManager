package io.puharesource.mc.sponge.titlemanager.api.animations;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationIterable;
import lombok.EqualsAndHashCode;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Iterator;

@EqualsAndHashCode
public class EasyAnimation {
    @Inject private TitleManager plugin;

    private final AnimationIterable animationIterable;
    private final Player player;
    private final Updatable onUpdate;

    private Iterator<AnimationFrame> iterator;
    private boolean continuous;
    private boolean running;
    private Runnable onStop;

    public EasyAnimation(final AnimationIterable animationIterable, final Player player, final Updatable onUpdate) {
        this.animationIterable = animationIterable;
        this.player = player;
        this.onUpdate = onUpdate;

        iterator = animationIterable.iterator(player);
    }

    public void start() {
        if (!running) {
            running = true;

            update(iterator.next());
        }
    }

    public void stop() {
        if (running) {
            running = false;

            if (onStop != null && player != null && player.isOnline()) {
                onStop.run();
            }
        }
    }

    public void update(final AnimationFrame frame) {
        if (player == null || !player.isOnline()) stop();
        if (!running) return;

        onUpdate.run(frame);

        if (!iterator.hasNext() && continuous) {
            iterator = animationIterable.iterator(player);
        }

        if (iterator.hasNext()) {
            plugin.getEngine().schedule(() -> update(iterator.next()), frame.getTotalTime());
        } else {
            stop();
        }
    }

    public void onStop(final Runnable onStop) {
        this.onStop = onStop;
    }

    public interface Updatable {
        void run(final AnimationFrame frame);
    }

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    public boolean isContinuous() {
        return continuous;
    }
}
