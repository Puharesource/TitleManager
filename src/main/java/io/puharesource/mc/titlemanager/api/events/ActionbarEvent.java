package io.puharesource.mc.titlemanager.api.events;

import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event fires whenever a player is sent an actionbar message.
 */
@Deprecated
public class ActionbarEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private Player player;
    private ActionbarTitleObject titleObject;

    public ActionbarEvent(Player player, ActionbarTitleObject titleObject) {
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
     * @return The player that is going to receive the actionbar message.
     */
    @Deprecated
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The object being sent.
     */
    @Deprecated
    public ActionbarTitleObject getTitleObject() {
        return titleObject;
    }

    /**
     * This sets the actionbar object to something different.
     * @param titleObject The new object.
     */
    @Deprecated
    public void setTitleObject(ActionbarTitleObject titleObject) {
        this.titleObject = titleObject;
    }
}