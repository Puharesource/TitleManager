package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.InternalsKt;
import io.puharesource.mc.titlemanager.TitleManagerPlugin;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.api.v2.animation.Animation;
import io.puharesource.mc.titlemanager.scheduling.AsyncScheduler;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * This is the title animation.
 * It can send a sequence of actionbar messages to the player, making it look like an animation.
 *
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#toAnimationPart(String)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#addAnimation(String, Animation)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#removeAnimation(String)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#containsAnimation(String, String)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#createAnimationFrame(String, int, int, int)
 *
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#toTitleAnimation(Animation, Player, boolean)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#toTitleAnimation(List, Player, boolean)
 *
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#toSubtitleAnimation(Animation, Player, boolean)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#toSubtitleAnimation(List, Player, boolean)
 *
 * @since 1.3.0
 */
@Deprecated
public class TitleAnimation implements IAnimation, ITitleObject {
    private Object title;
    private Object subtitle;

    @Deprecated
    public TitleAnimation(FrameSequence title, FrameSequence subtitle) {
        this((Object) title, (Object) subtitle);
    }

    @Deprecated
    public TitleAnimation(FrameSequence title, String subtitle) {
        this((Object) title, (Object) subtitle);
    }

    @Deprecated
    public TitleAnimation(String title, FrameSequence subtitle) {
        this((Object) title, (Object) subtitle);
    }

    @Deprecated
    public TitleAnimation(Object title, Object subtitle) {
        if (title != null && !(title instanceof FrameSequence) && !(title instanceof String)) throw new IllegalArgumentException("The title must be a String or a FrameSequence!");
        if (subtitle != null && !(subtitle instanceof FrameSequence) && !(subtitle instanceof String)) throw new IllegalArgumentException("The subtitle must be a String or a FrameSequence!");
        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    @Deprecated
    public void broadcast() {
        send(null);
    }

    @Override
    @Deprecated
    public void send(Player player) {
        final AsyncScheduler engine = AsyncScheduler.INSTANCE;

        int times = 0;
        if (title instanceof FrameSequence && subtitle instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) title).getFrames()) {
                engine.schedule(new Task(false, frame, player), times);
                times += frame.getTotalTime();
            }
            times = 0;
            for (AnimationFrame frame : ((FrameSequence) subtitle).getFrames()) {
                engine.schedule(new Task(true, frame, player), times);
                times += frame.getTotalTime();
            }
        } else if (title instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) title).getFrames()) {
                engine.schedule(new Task(false, frame, player), times);
                times += frame.getTotalTime();
            }
            FrameSequence sequence = (FrameSequence) title;
            engine.schedule(new Task(true, new AnimationFrame((String) subtitle, sequence.getFadeIn(), sequence.getStay(), sequence.getFadeOut()), player), 0);
        } else if (subtitle instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) subtitle).getFrames()) {
                engine.schedule(new Task(true, frame, player), times);
                times += frame.getTotalTime();
            }
            FrameSequence sequence = (FrameSequence) subtitle;
            engine.schedule(new Task(false, new AnimationFrame((String) title, sequence.getFadeIn(), sequence.getStay(), sequence.getFadeOut()), player), 0);
        }
    }

    @Deprecated
    private class Task implements Runnable {
        private boolean isSubtitle;
        private AnimationFrame frame;
        private Player player;

        @Deprecated
        public Task(boolean isSubtitle, AnimationFrame frame, Player player) {
            this.isSubtitle = isSubtitle;
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
        private void send(Player p, AnimationFrame frame) {
            if (p != null) {
                final TitleManagerPlugin plugin = InternalsKt.getPluginInstance();

                if (isSubtitle) {
                    plugin.sendSubtitleWithPlaceholders(p, frame.getText(), frame.getFadeIn(), frame.getStay() + 1, frame.getFadeOut());
                } else {
                    plugin.sendTitleWithPlaceholders(p, frame.getText(), frame.getFadeIn(), frame.getStay() + 1, frame.getFadeOut());
                }
            }
        }
    }
}