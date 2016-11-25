package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.InternalsKt;
import io.puharesource.mc.titlemanager.api.events.ActionbarEvent;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * This is the standard actionbar message object.
 * It is used whenever both in actionbar animations and simply for displaying a message above the actionbar.
 */
@Deprecated
public class ActionbarTitleObject implements IActionbarObject {
    private String title;

    @Deprecated
    public ActionbarTitleObject(String title) {
        setTitle(title);
    }

    @Override
    @Deprecated
    public void broadcast() {
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    @Override
    @Deprecated
    public void send(Player player) {
        final ActionbarEvent event = new ActionbarEvent(player, this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        InternalsKt.getPluginInstance().sendActionbarWithPlaceholders(player, title);
    }

    @Deprecated
    public String getTitle() {
        return title;
    }

    @Deprecated
    public void setTitle(String title) {
        this.title = title;
    }
}