package io.puharesource.mc.sponge.titlemanager.api.iface;

import io.puharesource.mc.sponge.titlemanager.api.animations.AnimationFrame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.Iterator;

public interface Script {
    String getName();
    String getVersion();
    String getAuthor();

    Iterator<AnimationFrame> getIterator(final Text originalText, final Player player);
}
