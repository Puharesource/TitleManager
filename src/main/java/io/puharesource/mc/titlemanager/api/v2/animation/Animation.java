package io.puharesource.mc.titlemanager.api.v2.animation;

import java.util.Iterator;
import org.bukkit.entity.Player;

/**
 * @since 2.0.0
 */
public interface Animation {
    Iterator<AnimationFrame> iterator(Player player);
}
