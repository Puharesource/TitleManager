package io.puharesource.mc.titlemanager.listeners;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.api.TabTitleCache;
import io.puharesource.mc.titlemanager.api.TabTitleObject;
import io.puharesource.mc.titlemanager.api.TitleObject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ListenerConnection implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (Config.isUsingConfig()) {
            TitleObject titleObject = Config.getWelcomeObject();
            titleObject.setTitle(titleObject.getTitle().replaceAll("(?i)\\{PLAYER\\}", event.getPlayer().getName()));
            TabTitleObject tabTitleObject = Config.getTabTitleObject();
            tabTitleObject.setFooter(tabTitleObject.getFooter().replaceAll("(?i)\\{PLAYER\\}", event.getPlayer().getName()));
            tabTitleObject.setHeader(tabTitleObject.getHeader().replaceAll("(?i)\\{PLAYER\\}", event.getPlayer().getName()));
            titleObject.send(event.getPlayer());
            tabTitleObject.send(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        TabTitleCache.removeTabTitle(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKick(PlayerKickEvent event) {
        TabTitleCache.removeTabTitle(event.getPlayer().getUniqueId());
    }
}
