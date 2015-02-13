package io.puharesource.mc.titlemanager.api.events;

import io.puharesource.mc.titlemanager.api.TabTitleObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TabTitleChangeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private Player player;
    private TabTitleObject titleObject;

    public TabTitleChangeEvent(Player player, TabTitleObject titleObject) {
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

    /**
     * @return The player that is going to receive the tabtitle change.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The object being sent.
     */
    public TabTitleObject getTitleObject() {
        return titleObject;
    }

    /**
     * This sets the tabtitle object to something different.
     * @param titleObject The new object.
     */
    public void setTitleObject(TabTitleObject titleObject) {
        this.titleObject = titleObject;
    }
}
