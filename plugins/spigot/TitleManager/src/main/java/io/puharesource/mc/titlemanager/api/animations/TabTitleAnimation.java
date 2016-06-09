package io.puharesource.mc.titlemanager.api.animations;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import io.puharesource.mc.titlemanager.api.TabTitleObject;
import io.puharesource.mc.titlemanager.api.iface.AnimationIterable;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.api.iface.ITabObject;
import lombok.val;

/**
 * This is the actionbar title animation.
 * It can send a sequence of actionbar messages to the player, making it look like an animation.
 */
public class TabTitleAnimation implements IAnimation, ITabObject {

    private Object header;
    private Object footer;

    public TabTitleAnimation(AnimationIterable header, AnimationIterable footer) {
        this((Object) header, (Object) footer);
    }

    public TabTitleAnimation(AnimationIterable header, String footer) {
        this((Object) header, (Object) footer);
    }

    public TabTitleAnimation(String header, AnimationIterable footer) {
        this((Object) header, (Object) footer);
    }

    public TabTitleAnimation(Object header, Object footer) {
        if (header != null && !(header instanceof AnimationIterable) && !(header instanceof String)) throw new IllegalArgumentException("The header must be a String or implement AnimationIterable!");
        if (footer != null && !(footer instanceof AnimationIterable) && !(footer instanceof String)) throw new IllegalArgumentException("The footer must be a String or implement AnimationIterable!");
        this.header = header;
        this.footer = footer;
    }

    public Object getHeader() {
        return header;
    }

    public Object getFooter() {
        return footer;
    }

    @Override
    public void broadcast() {
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    @Override
    public void broadcast(World world) {
        world.getPlayers().forEach(this::send);
    }



    @Override
    public void send(final Player player) {
        if (header instanceof AnimationIterable) {
            val animation = new EasyAnimation((AnimationIterable) header, player, frame -> new TabTitleObject(frame.getText(), TabTitleObject.Position.HEADER).send(player));

            animation.setContinuous(true);
            animation.start();
        } else {
            new TabTitleObject((String) header, TabTitleObject.Position.HEADER).send(player);
        }

        if (footer instanceof AnimationIterable) {
            val animation = new EasyAnimation((AnimationIterable) footer, player, frame -> new TabTitleObject(frame.getText(), TabTitleObject.Position.FOOTER).send(player));

            animation.setContinuous(true);
            animation.start();
        } else {
            new TabTitleObject((String) footer, TabTitleObject.Position.HEADER).send(player);
        }
    }
}
