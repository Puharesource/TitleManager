package io.puharesource.mc.titlemanager.api.v2.animation;

import org.bukkit.entity.Player;

import java.util.Iterator;

public interface Animation {
    Iterator<AnimationFrame> iterator(Player player);
}
