package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.api.events.ActionbarEvent;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.backend.packet.ActionbarTitlePacket;
import io.puharesource.mc.titlemanager.backend.player.TMPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * This is the standard actionbar message object.
 * It is used whenever both in actionbar animations and simply for displaying a message above the actionbar.
 */
public class ActionbarTitleObject implements IActionbarObject {
    private String title;

    public ActionbarTitleObject(String title) {
        setTitle(title);
    }

    @Override
    public void broadcast() {
        for (Player player : Bukkit.getOnlinePlayers())
            send(player);
    }

    @Override
    public void send(Player player) {
        final ActionbarEvent event = new ActionbarEvent(player, this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        TMPlayer tmPlayer = new TMPlayer(player);

        tmPlayer.sendPacket(new ActionbarTitlePacket(TextConverter.setVariables(player, title)));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
