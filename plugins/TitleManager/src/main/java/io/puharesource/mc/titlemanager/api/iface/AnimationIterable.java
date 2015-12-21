package io.puharesource.mc.titlemanager.api.iface;

import io.puharesource.mc.titlemanager.api.animations.AnimationFrame;
import org.bukkit.entity.Player;

import java.util.Iterator;

public interface AnimationIterable {
    Iterator<AnimationFrame> iterator(final Player player);
}
