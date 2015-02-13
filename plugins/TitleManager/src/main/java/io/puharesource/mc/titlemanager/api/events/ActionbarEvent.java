package io.puharesource.mc.titlemanager.api.events;

import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ActionbarEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private Player player;
    private ActionbarTitleObject titleObject;

    public ActionbarEvent(Player player, ActionbarTitleObject titleObject) {
        this.player = player;
        this.titleObject = titleObject;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean shouldCancel) {
        cancelled = shouldCancel;
    }

    public Player getPlayer() {
        return player;
    }

    public ActionbarTitleObject getTitleObject() {
        return titleObject;
    }

    public void setTitleObject(ActionbarTitleObject titleObject) {
        this.titleObject = titleObject;
    }
}
