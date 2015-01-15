package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.events.ActionbarEvent;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionbarTitleObject implements IActionbarObject {
    private String rawTitle;
    private Object title;

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

        TitleManager.getReflectionManager().sendPacket(TitleManager.getReflectionManager().constructActionbarTitlePacket(TextConverter.containsVariable(rawTitle) ? TitleManager.getReflectionManager().getIChatBaseComponent(TextConverter.setVariables(player, rawTitle)) : title), player);
    }

    public String getTitle() {
        return rawTitle;
    }

    public void setTitle(String title) {
        rawTitle = title;
        this.title = TitleManager.getReflectionManager().getIChatBaseComponent(title);
    }
}
