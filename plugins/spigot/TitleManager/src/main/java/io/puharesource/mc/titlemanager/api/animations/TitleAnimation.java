package io.puharesource.mc.titlemanager.api.animations;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.iface.AnimationIterable;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.backend.packet.TitlePacket;
import io.puharesource.mc.titlemanager.backend.player.TMPlayer;

/**
 * This is the title animation.
 * It can send a sequence of actionbar messages to the player, making it look like an animation.
 */
public class TitleAnimation implements IAnimation, ITitleObject {

    private Object title;
    private Object subtitle;

    public TitleAnimation(AnimationIterable title, AnimationIterable subtitle) {
        this((Object) title, (Object) subtitle);
    }

    public TitleAnimation(AnimationIterable title, String subtitle) {
        this((Object) title, (Object) subtitle);
    }

    public TitleAnimation(String title, AnimationIterable subtitle) {
        this((Object) title, (Object) subtitle);
    }

    public TitleAnimation(Object title, Object subtitle) {
        if (title != null && !(title instanceof AnimationIterable) && !(title instanceof String)) throw new IllegalArgumentException("The title must be a String or implement AnimationIterable!");
        if (subtitle != null && !(subtitle instanceof AnimationIterable) && !(subtitle instanceof String)) throw new IllegalArgumentException("The subtitle must be a String or implement AnimationIterable!");
        this.title = title;
        this.subtitle = subtitle;
    }

    public Object getTitle() {
        return title;
    }

    public Object getSubtitle() {
        return subtitle;
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
        if (title instanceof AnimationIterable) {
            final EasyAnimation animation = new EasyAnimation((AnimationIterable) title, player, frame -> new TitleObject(frame.getText(), TitleObject.TitleType.TITLE).setFadeIn(frame.getFadeIn()).setStay(frame.getStay() + 1).setFadeOut(frame.getFadeOut()).send(player));

            if (!(subtitle instanceof AnimationIterable)) {
                animation.onStop(new Runnable() {
                    @Override
                    public void run() {
                        new TitleObject(" ", TitleObject.TitleType.SUBTITLE).setFadeIn(20).setStay(40).setFadeOut(20).send(player);
                    }
                });
            }

            animation.start();
        } else {
            new TMPlayer(player).sendPacket(new TitlePacket(TitleObject.TitleType.TITLE, (String) title, 0, Integer.MAX_VALUE, 0));
        }

        if (subtitle instanceof AnimationIterable) {
            final EasyAnimation animation = new EasyAnimation((AnimationIterable) subtitle, player, frame -> new TitleObject(frame.getText(), TitleObject.TitleType.SUBTITLE).setFadeIn(frame.getFadeIn()).setStay(frame.getStay() + 1).setFadeOut(frame.getFadeOut()).send(player));

            if (!(title instanceof AnimationIterable)) {
                animation.onStop(() -> new TitleObject(" ", TitleObject.TitleType.TITLE).setFadeIn(20).setStay(40).setFadeOut(20).send(player));
            }

            animation.start();
        } else {
            new TMPlayer(player).sendPacket(new TitlePacket(TitleObject.TitleType.SUBTITLE, (String) subtitle, 0, Integer.MAX_VALUE, 0));
        }
    }
}