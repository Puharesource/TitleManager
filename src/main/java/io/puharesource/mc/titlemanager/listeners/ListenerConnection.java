package io.puharesource.mc.titlemanager.listeners;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TabTitleChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ListenerConnection implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        TitleManager.setHeaderAndFooter(event.getPlayer(), TitleManager.getHeader(), TitleManager.getFooter(), TabTitleChangeEvent.ChangeReason.DEFAULT);
        TitleManager.sendTitle(event.getPlayer(), TitleManager.getTitle(), TitleManager.getSubstring());
    }
}
