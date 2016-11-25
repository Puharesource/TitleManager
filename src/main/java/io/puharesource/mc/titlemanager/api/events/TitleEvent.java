package io.puharesource.mc.titlemanager.api.events;

import io.puharesource.mc.titlemanager.api.TitleObject;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Deprecated
public class TitleEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private Player player;
    private TitleObject titleObject;

    @Deprecated
    public TitleEvent(Player player, TitleObject titleObject) {
        this.player = player;
        this.titleObject = titleObject;
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

    @Override
    @Deprecated
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * @return The player that is going to receive the title message.
     */
    @Deprecated
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The object being sent.
     */
    @Deprecated
    public TitleObject getTitleObject() {
        return titleObject;
    }

    /**
     * This sets the title object to something different.
     * @param titleObject The new object.
     */
    @Deprecated
    public void setTitleObject(TitleObject titleObject) {
        this.titleObject = titleObject;
    }
}