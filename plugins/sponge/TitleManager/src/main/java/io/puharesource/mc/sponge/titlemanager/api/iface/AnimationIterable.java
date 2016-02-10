package io.puharesource.mc.sponge.titlemanager.api.iface;

import io.puharesource.mc.sponge.titlemanager.api.animations.AnimationFrame;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Iterator;

public interface AnimationIterable {
    Iterator<AnimationFrame> iterator(final Player player);
}
