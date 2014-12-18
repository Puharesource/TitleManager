package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.ReflectionManager;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TextConverter;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class TitleAnimation implements IAnimation, ITitleObject {

    private ReflectionManager manager = TitleManager.getReflectionManager();
    private Object title;
    private Object subtitle;

    public TitleAnimation(FrameSequence title, FrameSequence subtitle) {
        this(title, (Object) subtitle);
    }

    public TitleAnimation(FrameSequence title, String subtitle) {
        this(title, (Object) subtitle);
    }

    public TitleAnimation(String title, FrameSequence subtitle) {
        this(title, (Object) subtitle);
    }

    public TitleAnimation(Object title, Object subtitle) {
        manager = TitleManager.getReflectionManager();
        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    public void broadcast() {
        send(null);
    }

    @Override
    public void send(Player player) {
        Plugin plugin = TitleManager.getPlugin();
        BukkitScheduler scheduler = Bukkit.getScheduler();

        long times = 0;
        if (title instanceof FrameSequence && subtitle instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) title).getFrames()) {
                scheduler.runTaskLaterAsynchronously(plugin, new Task(false, frame, player), times);
                times += frame.getTotalTime();
            }
            times = 0;
            for (AnimationFrame frame : ((FrameSequence) subtitle).getFrames()) {
                scheduler.runTaskLaterAsynchronously(plugin, new Task(true, frame, player), times);
                times += frame.getTotalTime();
            }
        } else if (title instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) title).getFrames()) {
                scheduler.runTaskLaterAsynchronously(plugin, new Task(false, frame, player), times);
                times += frame.getTotalTime();
            }
            FrameSequence sequence = (FrameSequence) title;
            scheduler.runTaskAsynchronously(plugin, new Task(true, new AnimationFrame((String) subtitle, sequence.getFadeIn(), sequence.getStay(), sequence.getFadeOut()), player));
        } else if (subtitle instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) subtitle).getFrames()) {
                scheduler.runTaskLaterAsynchronously(plugin, new Task(true, frame, player), times);
                times += frame.getTotalTime();
            }
            FrameSequence sequence = (FrameSequence) subtitle;
            scheduler.runTaskAsynchronously(plugin, new Task(false, new AnimationFrame((String) title, sequence.getFadeIn(), sequence.getStay(), sequence.getFadeOut()), player));
        }
    }

    private class Task implements Runnable {

        private boolean isSubtitle;
        private AnimationFrame frame;
        private Player player;

        public Task(boolean isSubtitle, AnimationFrame frame, Player player) {
            this.isSubtitle = isSubtitle;
            this.frame = frame;
            this.player = player;
        }

        @Override
        public void run() {
            if (player == null) {
                for (Player p : Bukkit.getServer().getOnlinePlayers())
                    send(p, frame);
            } else send(player, frame);
        }

        private void send(Player p, AnimationFrame frame) {
            if (p != null) {
                manager.sendPacket(manager.constructTitleTimingsPacket(frame.getFadeIn(), frame.getStay() + 1, frame.getFadeOut()), p);
                manager.sendPacket(manager.constructTitlePacket(isSubtitle, (frame.getText().contains("{") && frame.getText().contains("}")) ? manager.getIChatBaseComponent(TextConverter.setVariables(player, frame.getText())) : frame.getComponentText()), p);
            }
        }
    }
}