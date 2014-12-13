package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.events.ActionbarEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ActionbarTitleObject {
    private String rawTitle;
    private Object title;

    public ActionbarTitleObject(String title) {
        setTitle(title);
    }

    public void send(Player player) {
        final ActionbarEvent event = new ActionbarEvent(player, this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        TitleManager.getReflectionManager().sendPacket(TitleManager.getReflectionManager().constructActionbarTitlePacket((rawTitle.contains("{") && rawTitle.contains("}")) ? TitleManager.getReflectionManager().getIChatBaseComponent(TextConverter.setVariables(player, rawTitle)) : title), player);
    }

    public String getTitle() {
        return rawTitle;
    }

    public void setTitle(String title) {
        rawTitle = title;
        this.title = TitleManager.getReflectionManager().getIChatBaseComponent(title);
    }
}
