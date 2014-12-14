package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.ReflectionManager;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TextConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class ActionbarTitleAnimation {

    private ReflectionManager manager;
    private FrameSequence title;

    public ActionbarTitleAnimation(FrameSequence title) {
        manager = TitleManager.getReflectionManager();
        this.title = title;
    }

    public void broadcast() {
        send(null);
    }

    public void send(Player player) {
        Plugin plugin = TitleManager.getPlugin();
        BukkitScheduler scheduler = Bukkit.getScheduler();

        long times = 0;
        for (AnimationFrame frame : title.getFrames()) {
            scheduler.runTaskLaterAsynchronously(plugin, new Task(frame, player), times);
            times += frame.getTotalTime();
        }
    }

    private class Task implements Runnable {
        private AnimationFrame frame;
        private Player player;

        public Task(AnimationFrame frame, Player player) {
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
            if (p != null)
                manager.sendPacket(manager.constructActionbarTitlePacket((frame.getText().contains("{") && frame.getText().contains("}")) ? manager.getIChatBaseComponent(TextConverter.setVariables(player, frame.getText())) : frame.getComponentText()), p);
        }
    }
}