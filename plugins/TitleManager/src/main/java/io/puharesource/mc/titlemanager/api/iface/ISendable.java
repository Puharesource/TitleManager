package io.puharesource.mc.titlemanager.api.iface;

import org.bukkit.entity.Player;

/**
 * This interface is used for all types of sendable objects.
 */
public interface ISendable {
    /**
     * This broadcasts the object to all players on the server.
     */
    public void broadcast();

    /**
     * This sends the object to a specific player.
     * @param player The player receiving the object.
     */
    public void send(Player player);
}
