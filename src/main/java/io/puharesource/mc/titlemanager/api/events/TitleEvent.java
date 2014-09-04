package io.puharesource.mc.titlemanager.api.events;

import io.puharesource.mc.titlemanager.api.TitleObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TitleEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private Player player;
    private TitleObject titleObject;

    public TitleEvent(Player player, TitleObject titleObject) {
        this.player = player;
        this.titleObject = titleObject;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean shouldCancel) {
        cancelled = shouldCancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public TitleObject getTitleObject() {
        return titleObject;
    }

    public void setTitleObject(TitleObject titleObject) {
        this.titleObject = titleObject;
    }
}
