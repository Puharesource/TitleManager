package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TabTitleObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.UUID;

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
            FrameSequence headerSequence = (FrameSequence) header;
            FrameSequence footerSequence = (FrameSequence) footer;
            int speed = (headerSequence.getTotalTime() / headerSequence.size() + footerSequence.getTotalTime() / footerSequence.size()) / 2;
            MultiTask task = new MultiTask(headerSequence, footerSequence, player);
            int id = scheduler.runTaskTimerAsynchronously(plugin, task, 0, speed).getTaskId();
            task.setId(id);
        } else if (header instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) header).getFrames()) {
                Task task = new Task(frame.getText(), (String) footer, player);
                int id = scheduler.runTaskTimerAsynchronously(plugin, task, times, ((FrameSequence) header).getTotalTime()).getTaskId();
                task.setId(id);
                times += frame.getTotalTime();
            }
        } else if (footer instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) footer).getFrames()) {
                Task task = new Task((String) header, frame.getText(), player);
                int id = scheduler.runTaskTimerAsynchronously(plugin, task, times, ((FrameSequence) footer).getTotalTime()).getTaskId();
                task.setId(id);
                times += frame.getTotalTime();
            }
        }
    }

    private class Task implements Runnable {
        private String header;
        private String footer;

        private UUID uuid;
        private int id;

        public Task(String header, String footer, UUID uuid) {
            this.header = header;
            this.footer = footer;
            this.uuid = uuid;
        }

        public Task(String header, String footer, Player player) {
            this(header, footer, player == null ? null : player.getUniqueId());
        }

        public Task(String title, TabTitleObject.Position position, UUID uuid) {
            if (position == TabTitleObject.Position.HEADER)
                header = title;
            else footer = title;
            this.uuid = uuid;
        }

        public Task(String title, TabTitleObject.Position position, Player player) {
            this(title, position, player == null ? null : player.getUniqueId());
        }

        public void setId(int id) {
            this.id = id;
            TitleManager.addRunningAnimationId(id);
        }

        @Override
        public void run() {
            if (uuid == null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (header != null && footer == null)
                        new TabTitleObject(header, TabTitleObject.Position.HEADER).send(player);
                    else if (header == null && footer != null)
                        new TabTitleObject(footer, TabTitleObject.Position.FOOTER).send(player);
                    else new TabTitleObject(header, footer).send(player);
                }
            } else {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    if (header != null && footer == null)
                        new TabTitleObject(header, TabTitleObject.Position.HEADER).send(player);
                    else if (header == null && footer != null)
                        new TabTitleObject(footer, TabTitleObject.Position.FOOTER).send(player);
                    else new TabTitleObject(header, footer).send(player);
                } else {
                    Bukkit.getScheduler().cancelTask(id);
                    TitleManager.removeRunningAnimationId(id);
                }
            }
        }
    }

    private class MultiTask implements Runnable {
        private int i;
        private int j;

        private int id;

        private FrameSequence header;
        private FrameSequence footer;
        private UUID uuid;

        public MultiTask(FrameSequence header, FrameSequence footer, UUID uuid) {
            this.header = header;
            this.footer = footer;
            this.uuid = uuid;
        }

        public MultiTask(FrameSequence header, FrameSequence footer, Player player) {
            this(header, footer, player == null ? null : player.getUniqueId());
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public void run() {

            if (uuid == null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    new TabTitleObject(header.getFrames().get(i).getText(), footer.getFrames().get(j).getText()).send(player);
                }
            } else {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    new TabTitleObject(header.getFrames().get(i).getText(), footer.getFrames().get(j).getText()).send(player);
                } else {
                    Bukkit.getScheduler().cancelTask(id);
                    TitleManager.removeRunningAnimationId(id);
                }
            }

            if (i == header.size() - 1)
                i = 0;
            else i++;

            if (j == footer.size() - 1)
                j = 0;
            else j++;
        }
    }
}
