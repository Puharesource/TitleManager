package io.puharesource.mc.titlemanager.api.events;

import io.puharesource.mc.titlemanager.api.TabTitleObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Deprecated
public class TabTitleChangeEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private Player player;
    private TabTitleObject titleObject;

    @Deprecated
    public TabTitleChangeEvent(Player player, TabTitleObject titleObject) {
        this.player = player;
        this.titleObject = titleObject;
    }

    @Deprecated
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    @Deprecated
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    @Deprecated
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    @Deprecated
    public void setCancelled(boolean shouldCancel) {
        cancelled = shouldCancel;
    }

    /**
     * @return The player that is going to receive the tabtitle change.
     */
    @Deprecated
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The object being sent.
     */
    @Deprecated
    public TabTitleObject getTitleObject() {
        return titleObject;
    }

    /**
     * This sets the tabtitle object to something different.
     * @param titleObject The new object.
     */
    @Deprecated
    public void setTitleObject(TabTitleObject titleObject) {
        this.titleObject = titleObject;
    }
}