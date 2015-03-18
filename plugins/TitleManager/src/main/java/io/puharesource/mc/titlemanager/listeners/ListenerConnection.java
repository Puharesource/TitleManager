package io.puharesource.mc.titlemanager.listeners;

import io.puharesource.mc.titlemanager.__Config;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TabTitleCache;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ListenerConnection implements Listener {
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        if (!__Config.isUsingConfig()) return;

        if (__Config.isWelcomeMessageEnabled()) {
            Bukkit.getScheduler().runTaskLater(TitleManager.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    (event.getPlayer().hasPlayedBefore() ? __Config.getWelcomeObject() : __Config.getFirstWelcomeObject()).send(event.getPlayer());
                }
            }, 10l);
        }

        if (__Config.isTabmenuEnabled() && !(__Config.getTabTitleObject() instanceof IAnimation)) {
            Bukkit.getScheduler().runTaskLater(TitleManager.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    __Config.getTabTitleObject().send(event.getPlayer());
                }
            }, 10l);
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