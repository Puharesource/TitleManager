package io.puharesource.mc.sponge.titlemanager.api.animations;

import io.puharesource.mc.sponge.titlemanager.TitlePosition;
import io.puharesource.mc.sponge.titlemanager.api.TitleObject;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationIterable;
import io.puharesource.mc.sponge.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.sponge.titlemanager.api.iface.ITitleObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

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
        Sponge.getServer().getOnlinePlayers().forEach(this::send);
    }

    @Override
    public void broadcast(World world) {
        Sponge.getServer().getOnlinePlayers()
                .stream()
                .filter(p -> p.getWorld().equals(world))
                .forEach(this::send);
    }

    @Override
    public void send(final Player player) {
        if (title instanceof AnimationIterable) {
            final EasyAnimation animation = new EasyAnimation((AnimationIterable) title, player, frame -> new TitleObject(frame.getText(), TitlePosition.TITLE).setFadeIn(frame.getFadeIn()).setStay(frame.getStay() + 1).setFadeOut(frame.getFadeOut()).send(player));

            if (!(subtitle instanceof AnimationIterable)) {
                animation.onStop(() -> new TitleObject(" ", TitlePosition.SUBTITLE).setFadeIn(20).setStay(40).setFadeOut(20).send(player));
            }

            animation.start();
        } else {
            new TitleObject((String) title, TitlePosition.TITLE).setFadeIn(0).setStay(Integer.MAX_VALUE).setFadeOut(0).send(player);
        }

        if (subtitle instanceof AnimationIterable) {
            final EasyAnimation animation = new EasyAnimation((AnimationIterable) subtitle, player, frame -> new TitleObject(frame.getText(), TitlePosition.SUBTITLE).setFadeIn(frame.getFadeIn()).setStay(frame.getStay() + 1).setFadeOut(frame.getFadeOut()).send(player));

            if (!(title instanceof AnimationIterable)) {
                animation.onStop(() -> new TitleObject(" ", TitlePosition.TITLE).setFadeIn(20).setStay(40).setFadeOut(20).send(player));
            }

            animation.start();
        } else {
            new TitleObject((String) subtitle, TitlePosition.SUBTITLE).setFadeIn(0).setStay(Integer.MAX_VALUE).setFadeOut(0).send(player);
        }
    }
}