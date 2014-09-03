package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.TitleObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TabTitleChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private Player player;
    private TitleObject titleObject;
    private ChangeReason reason;

    public TabTitleChangeEvent(Player player, TitleObject titleObject, ChangeReason reason) {
        this.player = player;
        this.titleObject = titleObject;
        this.reason = reason;
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

    public TitleObject getTitleObject() {
        return titleObject;
    }

    public void setTitleObject(TitleObject titleObject) {
        this.titleObject = titleObject;
    }

    public ChangeReason getReason() {
        return reason;
    }

    public enum ChangeReason {
        DEFAULT, PLUGIN, OTHERS
    }
}
