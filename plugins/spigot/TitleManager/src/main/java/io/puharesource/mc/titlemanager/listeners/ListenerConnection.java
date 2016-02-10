package io.puharesource.mc.titlemanager.listeners;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TabTitleCache;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import io.puharesource.mc.titlemanager.backend.language.Messages;
import io.puharesource.mc.titlemanager.backend.updatechecker.UpdateManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

public final class ListenerConnection implements Listener {
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final TitleManager manager = TitleManager.getInstance();

        final Config configManager = manager.getConfigManager();
        final UpdateManager updateManager = manager.getUpdateManager();
        final ConfigMain config = manager.getConfigManager().getConfig();
        final BukkitScheduler scheduler = Bukkit.getScheduler();
        final Player player = event.getPlayer();

        if (updateManager.isUpdateAvailable() && player.hasPermission("titlemanager.update.notify")) {
            scheduler.runTaskLater(manager, new Runnable() {
                @Override
                public void run() {
                    player.sendMessage(String.format(Messages.UPDATE_MESSAGE.getMessage(),
                            updateManager.getCurrentVersion(),
                            updateManager.getLatestVersion(),
                            "http://www.spigotmc.org/resources/titlemanager.1049"));
                }
            }, 30L);
        }

        if (!config.usingConfig) return;

        if (config.welcomeMessageEnabled) {
            Bukkit.getScheduler().runTaskLater(manager, new Runnable() {
                @Override
                public void run() {
                    configManager.getTitleWelcomeMessage(!player.hasPlayedBefore()).send(player);
                }
            }, 10);
        }

        if (config.tabmenuEnabled) {
            Bukkit.getScheduler().runTaskLater(manager, new Runnable() {
                @Override
                public void run() {
                    configManager.getTabTitleObject().send(player);
                }
            }, 10L);
        }

        if (config.actionbarWelcomeEnabled) {
            Bukkit.getScheduler().runTaskLater(manager, new Runnable() {
                @Override
                public void run() {
                    configManager.getActionbarWelcomeMessage(!player.hasPlayedBefore()).send(player);
                }
            }, 10L);
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
