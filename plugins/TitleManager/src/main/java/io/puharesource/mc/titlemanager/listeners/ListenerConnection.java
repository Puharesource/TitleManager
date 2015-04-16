package io.puharesource.mc.titlemanager.listeners;

import io.puharesource.mc.titlemanager.Config;
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
        final Config config = TitleManager.getInstance().getConfigManager();

        if (!config.isUsingConfig()) return;


        if (config.isWelcomeMessageEnabled()) {
            Bukkit.getScheduler().runTaskLater(TitleManager.getInstance(), new Runnable() {
                @Override
                public void run() {
                    (event.getPlayer().hasPlayedBefore() ? config.getWelcomeObject() : config.getFirstWelcomeObject()).send(event.getPlayer());
                }
            }, 10l);
        }


        if (config.isTabmenuEnabled() && !(config.getTabTitleObject() instanceof IAnimation)) {
            Bukkit.getScheduler().runTaskLater(TitleManager.getInstance(), new Runnable() {
                @Override
                public void run() {
                    config.getTabTitleObject().send(event.getPlayer());
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
