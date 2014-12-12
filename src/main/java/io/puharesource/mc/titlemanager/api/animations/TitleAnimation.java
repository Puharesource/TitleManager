package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.ReflectionManager;
import io.puharesource.mc.titlemanager.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class TitleAnimation {

    private ReflectionManager manager = TitleManager.getReflectionManager();
    private Object title;
    private Object subtitle;
    private Player player;

    public TitleAnimation(Player player, FrameSequence title, FrameSequence subtitle) {
        this(player, title, (Object) subtitle);
    }

    public TitleAnimation(Player player, FrameSequence title, String subtitle) {
        this(player, title, (Object) subtitle);
    }

    public TitleAnimation(Player player, String title, FrameSequence subtitle) {
        this(player, title, (Object) subtitle);
    }

    public TitleAnimation(Player player, Object title, Object subtitle) {
        manager = TitleManager.getReflectionManager();
        this.player = player;
        this.title = title;
        this.subtitle = subtitle;
    }

    public void run() {
        Plugin plugin = TitleManager.getPlugin();
        BukkitScheduler scheduler = Bukkit.getScheduler();

        long times = 0;
        if (title instanceof FrameSequence && subtitle instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) title).getFrames()) {
                scheduler.runTaskLaterAsynchronously(plugin, new Task(false, frame), times);
                times += frame.getTotalTime();
            }
            times = 0;
            for (AnimationFrame frame : ((FrameSequence) subtitle).getFrames()) {
                scheduler.runTaskLaterAsynchronously(plugin, new Task(true, frame), times);
                times += frame.getTotalTime();
            }
        } else if (title instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) title).getFrames()) {
                scheduler.runTaskLaterAsynchronously(plugin, new Task(false, frame), times);
                times += frame.getTotalTime();
            }
            FrameSequence sequence = (FrameSequence) title;
            scheduler.runTaskAsynchronously(plugin, new Task(true, new AnimationFrame((String) subtitle, sequence.getFadeIn(), sequence.getStay(), sequence.getFadeOut())));
        } else if (subtitle instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) subtitle).getFrames()) {
                scheduler.runTaskLaterAsynchronously(plugin, new Task(true, frame), times);
                times += frame.getTotalTime();
            }
            FrameSequence sequence = (FrameSequence) subtitle;
            scheduler.runTaskAsynchronously(plugin, new Task(false, new AnimationFrame((String) title, sequence.getFadeIn(), sequence.getStay(), sequence.getFadeOut())));
        }
    }

    private class Task implements Runnable {

        private boolean isSubtitle;
        private AnimationFrame frame;

        public Task(boolean isSubtitle, AnimationFrame frame) {
            this.isSubtitle = isSubtitle;
            this.frame = frame;
        }

        @Override
        public void run() {
            if (player == null) {
                for (Player p : Bukkit.getServer().getOnlinePlayers())
                    send(p, frame);
            } else send(player, frame);
        }

        private void send(Player p, AnimationFrame frame) {
            manager.sendPacket(manager.constructTitleTimingsPacket(frame.getFadeIn(), frame.getStay() + 1, frame.getFadeOut()), p);
            manager.sendPacket(manager.constructTitlePacket(isSubtitle, frame.getComponentText()), p);
        }
    }
}