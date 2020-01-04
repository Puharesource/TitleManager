package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.internal.InternalsKt;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.api.v2.animation.Animation;
import io.puharesource.mc.titlemanager.internal.scheduling.AsyncScheduler;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * This is the actionbar title animation.
 * It can send a sequence of actionbar messages to the player, making it look like an animation.
 *
 * @deprecated In favor of the methods seen under the "see also" section.
 *
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#toAnimationPart(String)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#addAnimation(String, Animation)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#removeAnimation(String)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#containsAnimation(String, String)
 *
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#toActionbarAnimation(Animation, Player, boolean)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#toActionbarAnimation(List, Player, boolean)
 *
 * @since 1.3.0
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