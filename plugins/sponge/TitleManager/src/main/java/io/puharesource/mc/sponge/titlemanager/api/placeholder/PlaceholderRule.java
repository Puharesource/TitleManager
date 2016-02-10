package io.puharesource.mc.sponge.titlemanager.api.placeholder;

import org.spongepowered.api.entity.living.player.Player;

public abstract class PlaceholderRule {
    public abstract boolean rule(final Player player);

    public abstract String[] replace(final Player player, final String text);
}
