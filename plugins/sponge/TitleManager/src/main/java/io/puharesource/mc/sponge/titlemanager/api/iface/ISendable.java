package io.puharesource.mc.sponge.titlemanager.api.iface;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

/**
 * This interface is used for all types of sendable objects.
 */
public interface ISendable {
    /**
     * This broadcasts the object to all players on the server.
     */
    void broadcast();

    /**
     * This broadcasts the object to all players in the given world.
     */
    void broadcast(final World world);

    /**
     * This sends the object to a specific player.
     * @param player The player receiving the object.
     */
    void send(final Player player);
}
