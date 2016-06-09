package io.puharesource.mc.titlemanager.api.animations;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import io.puharesource.mc.titlemanager.api.iface.AnimationIterable;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.packet.ActionbarTitlePacket;
import io.puharesource.mc.titlemanager.backend.player.TMPlayer;

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
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    @Override
    public void broadcast(final World world) {
        world.getPlayers().forEach(this::send);
    }

    @Override
    public void send(final Player player) {
        new EasyAnimation(title, player, frame -> new TMPlayer(player).sendPacket(new ActionbarTitlePacket(frame.getText()))).start();
    }
}