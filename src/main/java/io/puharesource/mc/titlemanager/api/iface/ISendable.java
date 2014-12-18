package io.puharesource.mc.titlemanager.api.iface;

import org.bukkit.entity.Player;

public interface ISendable{
    public void broadcast();
    public void send(Player player);
}
