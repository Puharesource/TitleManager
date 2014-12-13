package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TabTitleObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class TabTitleAnimation {

    private Object header;
    private Object footer;

    public TabTitleAnimation(FrameSequence header, FrameSequence footer) {
        this(header, (Object) footer);
    }

    public TabTitleAnimation(FrameSequence header, String footer) {
        this(header, (Object) footer);
    }

    public TabTitleAnimation(String header, FrameSequence footer) {
        this(header, (Object) footer);
    }

    public TabTitleAnimation(Object header, Object footer) {
        this.header = header;
        this.footer = footer;
    }

    public void broadcast() {
        send(null);
    }

    public void send(Player player) {
        Plugin plugin = TitleManager.getPlugin();
        BukkitScheduler scheduler = Bukkit.getScheduler();

        long times = 0;
        if (header instanceof FrameSequence && footer instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) header).getFrames()) {
                Task task = new Task(frame.getText(), TabTitleObject.Position.HEADER, player);
                int id = scheduler.runTaskTimerAsynchronously(plugin, task, times, ((FrameSequence) header).getTotalTime()).getTaskId();
                task.setId(id);

                times += frame.getTotalTime();
            }
            times = 0;
            for (AnimationFrame frame : ((FrameSequence) footer).getFrames()) {
                scheduler.runTaskTimerAsynchronously(plugin, new Task(frame.getText(), TabTitleObject.Position.FOOTER, player), times, ((FrameSequence) footer).getTotalTime());
                times += frame.getTotalTime();
            }
        } else if (header instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) header).getFrames()) {
                scheduler.runTaskTimerAsynchronously(plugin, new Task(frame.getText(), (String) footer, player), times, ((FrameSequence) header).getTotalTime());
                times += frame.getTotalTime();
            }
        } else if (footer instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) footer).getFrames()) {
                scheduler.runTaskTimerAsynchronously(plugin, new Task((String) header, frame.getText(), player), times, ((FrameSequence) footer).getTotalTime());
                times += frame.getTotalTime();
            }
        }
    }

    private class Task implements Runnable {
        private TabTitleObject object;
        private Player player;
        private int id;

        public Task(String title, String footer, Player player) {
            object = new TabTitleObject(title, footer);
            this.player = player;
        }

        public Task(String title, TabTitleObject.Position position, Player player) {
            object = new TabTitleObject(title, position);
            this.player = player;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            if (!player.isOnline()) {
                Bukkit.getScheduler().cancelTask(id);
                TitleManager.removeRunningAnimationId(id);
                return;
            }
            object.send(player);
        }
    }
}
