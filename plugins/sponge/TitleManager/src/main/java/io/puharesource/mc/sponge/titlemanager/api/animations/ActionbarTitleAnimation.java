package io.puharesource.mc.sponge.titlemanager.api.animations;

import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationIterable;
import io.puharesource.mc.sponge.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.sponge.titlemanager.api.iface.IAnimation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.world.World;

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
        Sponge.getServer().getOnlinePlayers().forEach(this::send);
    }

    @Override
    public void broadcast(final World world) {
        Sponge.getServer().getOnlinePlayers()
                .stream()
                .filter(p -> p.getWorld().equals(world))
                .forEach(this::send);
    }

    @Override
    public void send(final Player player) {
        new EasyAnimation(title, player, frame -> player.sendMessage(ChatTypes.ACTION_BAR, Text.of(frame.getText()))).start();
    }
}