package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TextConverter;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.packet.ActionbarTitlePacket;
import io.puharesource.mc.titlemanager.backend.player.TMPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * This is the actionbar title animation.
 * It can send a sequence of actionbar messages to the player, making it look like an animation.
 */
public class ActionbarTitleAnimation implements IAnimation, IActionbarObject {
    private FrameSequence title;

    public ActionbarTitleAnimation(FrameSequence title) {
        this.title = title;
    }

    @Override
    public void broadcast() {
        send(null);
    }

    @Override
    public void send(Player player) {
        Plugin plugin = TitleManager.getInstance();
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
            if (p != null) {
                new TMPlayer(p).sendPacket(new ActionbarTitlePacket(TextConverter.setVariables(p, frame.getText())));
            }
        }
    }
}