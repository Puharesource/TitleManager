package io.puharesource.mc.titlemanager.api.iface;

import io.puharesource.mc.titlemanager.api.animations.AnimationFrame;
import org.bukkit.entity.Player;

import java.util.Iterator;

public interface Script {
    String getName();
    String getVersion();
    String getAuthor();

    Iterator<AnimationFrame> getIterator(final String originalString, final Player player);
}
