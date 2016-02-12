package io.puharesource.mc.sponge.titlemanager.api.animations;

import io.puharesource.mc.sponge.titlemanager.api.Sendables;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationSendable;
import io.puharesource.mc.sponge.titlemanager.api.iface.TabListSendable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

/**
 * This is the actionbar title animation.
 * It can send a sequence of actionbar messages to the player, making it look like an animation.
 */
public class TabTitleAnimation implements AnimationSendable, TabListSendable {
    @Getter @Setter private AnimationToken header;
    @Getter @Setter private AnimationToken footer;

    public TabTitleAnimation(final AnimationToken header, final AnimationToken footer) {
        Validate.notNull(header);
        Validate.notNull(footer);

        this.header = header;
        this.footer = footer;
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
        Validate.notNull(player);

        if (header.isIterable()) {
            final EasyAnimation animation = new EasyAnimation(header.getIterable().get(), player, frame -> Sendables.tabList(frame.getText(), TabListPosition.HEADER).send(player));

            animation.setContinuous(true);
            animation.start();
        } else {
            Sendables.tabList(header.getText().get(), TabListPosition.HEADER).send(player);
        }

        if (footer.isIterable()) {
            final EasyAnimation animation = new EasyAnimation(footer.getIterable().get(), player, frame -> Sendables.tabList(frame.getText(), TabListPosition.FOOTER).send(player));

            animation.setContinuous(true);
            animation.start();
        } else {
            Sendables.tabList(footer.getText().get(), TabListPosition.FOOTER).send(player);
        }
    }
}
