package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.InternalsKt;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.scheduling.AsyncScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * This is the actionbar title animation.
 * It can send a sequence of actionbar messages to the player, making it look like an animation.
 */
@Deprecated
public class ActionbarTitleAnimation implements IAnimation, IActionbarObject {
    private FrameSequence title;

    @Deprecated
    public ActionbarTitleAnimation(FrameSequence title) {
        this.title = title;
    }

    @Override
    @Deprecated
    public void broadcast() {
        send(null);
    }

    @Override
    @Deprecated
    public void send(Player player) {
        int times = 0;
        for (AnimationFrame frame : title.getFrames()) {
            AsyncScheduler.INSTANCE.schedule(new Task(frame, player), times);
            times += frame.getTotalTime();
        }
    }

    @Deprecated
    private class Task implements Runnable {
        private AnimationFrame frame;
        private Player player;

        @Deprecated
        public Task(AnimationFrame frame, Player player) {
            this.frame = frame;
            this.player = player;
        }

        @Override
        @Deprecated
        public void run() {
            if (player == null) {
                for (Player p : Bukkit.getServer().getOnlinePlayers())
                    send(p, frame);
            } else send(player, frame);
        }

        @Deprecated
        private void send(final Player p, final AnimationFrame frame) {
            if (p != null) {
                InternalsKt.getPluginInstance().sendActionbarWithPlaceholders(player, frame.getText());
            }
        }
    }
}