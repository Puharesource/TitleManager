package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.ReflectionManager;
import io.puharesource.mc.titlemanager.api.events.ActionbarEvent;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * This is the standard actionbar message object.
 * It is used whenever both in actionbar animations and simply for displaying a message above the actionbar.
 */
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
        
        ReflectionManager manager = ReflectionManager.getInstance();

        manager.sendPacket(manager.constructActionbarTitlePacket(TextConverter.containsVariable(rawTitle) ? manager.getIChatBaseComponent(TextConverter.setVariables(player, rawTitle)) : title), player);
    }

    public String getTitle() {
        return rawTitle;
    }

    public void setTitle(String title) {
        rawTitle = title;
        this.title = ReflectionManager.getInstance().getIChatBaseComponent(title);
    }
}
