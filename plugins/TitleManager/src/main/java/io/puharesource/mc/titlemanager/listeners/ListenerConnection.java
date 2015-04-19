package io.puharesource.mc.titlemanager.listeners;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TabTitleCache;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ListenerConnection implements Listener {
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final Config configManager = TitleManager.getInstance().getConfigManager();
        final ConfigMain config = TitleManager.getInstance().getConfigManager().getConfig();

        if (!config.usingConfig) return;
        final Player player = event.getPlayer();

        if (config.welcomeMessageEnabled) {
            Bukkit.getScheduler().runTaskLater(TitleManager.getInstance(), new Runnable() {
                @Override
                public void run() {
                    (player.hasPlayedBefore() ? configManager.getWelcomeObject() : configManager.getFirstWelcomeObject()).send(player);
                }
            }, 10l);
        }


        if (config.tabmenuEnabled && !(configManager.getTabTitleObject() instanceof IAnimation)) {
            Bukkit.getScheduler().runTaskLater(TitleManager.getInstance(), new Runnable() {
                @Override
                public void run() {
                    configManager.getTabTitleObject().send(player);
                }
            }, 10l);
        }

        if (config.actionbarWelcomeEnabled) {
            Bukkit.getScheduler().runTaskLater(TitleManager.getInstance(), new Runnable() {
                @Override
                public void run() {
                    (player.hasPlayedBefore() ? configManager.getActionbarWelcomeObject() : configManager.getActionbarFirstWelcomeObject()).send(player);
                }
            }, 10l);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        TabTitleCache.removeTabTitle(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerKickEvent event) {
        TabTitleCache.removeTabTitle(event.getPlayer().getUniqueId());
    }

}
