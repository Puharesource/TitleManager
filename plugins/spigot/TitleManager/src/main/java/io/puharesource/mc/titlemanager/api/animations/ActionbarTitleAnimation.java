package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.api.iface.AnimationIterable;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.packet.ActionbarTitlePacket;
import io.puharesource.mc.titlemanager.backend.player.TMPlayer;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * This is the actionbar title animation.
 * It can send a sequence of actionbar messages to the player, making it look like an animation.
 */
public class ActionbarTitleAnimation implements IAnimation, IActionbarObject {
    private AnimationIterable title;

    public ActionbarTitleAnimation(final AnimationIterable title) {
        this.title = title;
    }

    public AnimationIterable getTitle() {
        return title;
    }

    public void setTitle(final AnimationIterable title) {
        this.title = title;
    }

    @Override
    public void broadcast() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            send(player);
        }
    }

    @Override
    public void broadcast(final World world) {
        for (val player : world.getPlayers()) {
            send(player);
        }
    }

    @Override
    public void send(final Player player) {
        new EasyAnimation(title, player, new EasyAnimation.Updatable() {
            @Override
            public void run(AnimationFrame frame) {
                new TMPlayer(player).sendPacket(new ActionbarTitlePacket(frame.getText()));
            }
        }).start();
    }
}