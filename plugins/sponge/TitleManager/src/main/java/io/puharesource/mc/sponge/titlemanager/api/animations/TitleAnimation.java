package io.puharesource.mc.sponge.titlemanager.api.animations;

import io.puharesource.mc.sponge.titlemanager.api.Sendables;
import io.puharesource.mc.sponge.titlemanager.api.TitleObject;
import io.puharesource.mc.sponge.titlemanager.api.TitlePosition;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationSendable;
import io.puharesource.mc.sponge.titlemanager.api.iface.TitleSendable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

/**
 * This is the title animation.
 * It can send a sequence of actionbar messages to the player, making it look like an animation.
 */
public class TitleAnimation implements AnimationSendable, TitleSendable {
    @Getter @Setter private AnimationToken title;
    @Getter @Setter private AnimationToken subtitle;

    public TitleAnimation(final AnimationToken title, final AnimationToken subtitle) {
        Validate.notNull(title);
        Validate.notNull(subtitle);

        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    public void broadcast() {
        Sponge.getServer().getOnlinePlayers().forEach(this::send);
    }

    @Override
    public void broadcast(final World world) {
        Validate.notNull(world);

        Sponge.getServer().getOnlinePlayers()
                .stream()
                .filter(p -> p.getWorld().equals(world))
                .forEach(this::send);
    }

    @Override
    public void send(final Player player) {
        if (title.isIterable()) {
            final EasyAnimation animation = new EasyAnimation(title.getIterable().get(), player, frame -> Sendables.title(frame.getText(), TitlePosition.TITLE).setFadeIn(frame.getFadeIn()).setStay(frame.getStay() + 1).setFadeOut(frame.getFadeOut()).send(player));

            if (subtitle.isText()) {
                animation.onStop(player::resetTitle);
            }

            animation.start();
        } else {
            new TitleObject(title.getText().get(), TitlePosition.TITLE).setFadeIn(0).setStay(Integer.MAX_VALUE).setFadeOut(0).send(player);
        }

        if (subtitle.isIterable()) {
            final EasyAnimation animation = new EasyAnimation(subtitle.getIterable().get(), player, frame -> Sendables.title(frame.getText(), TitlePosition.SUBTITLE).setFadeIn(frame.getFadeIn()).setStay(frame.getStay() + 1).setFadeOut(frame.getFadeOut()).send(player));

            if (title.isText()) {
                animation.onStop(() -> Sendables.title(Text.EMPTY, TitlePosition.TITLE).setFadeIn(20).setStay(40).setFadeOut(20).send(player));
            }

            animation.start();
        } else {
            Sendables.title(subtitle.getText().get(), TitlePosition.SUBTITLE).setFadeIn(0).setStay(Integer.MAX_VALUE).setFadeOut(0).send(player);
        }
    }
}