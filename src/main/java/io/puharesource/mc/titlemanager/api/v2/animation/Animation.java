package io.puharesource.mc.titlemanager.api.v2.animation;

import org.bukkit.entity.Player;

import java.util.Iterator;

/**
 * @since 2.0.0
 */
public interface Animation {
    Iterator<AnimationFrame> iterator(Player player);
}
