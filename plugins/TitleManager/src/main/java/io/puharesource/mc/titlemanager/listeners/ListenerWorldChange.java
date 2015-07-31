package io.puharesource.mc.titlemanager.listeners;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TabTitleCache;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import io.puharesource.mc.titlemanager.backend.updatechecker.UpdateManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    public void onChange(final PlayerChangedWorldEvent event) {
        final TitleManager manager = TitleManager.getInstance();

        final Config configManager = manager.getConfigManager();
        final ConfigMain config = manager.getConfigManager().getConfig();
        final BukkitScheduler scheduler = Bukkit.getScheduler();
        final Player player = event.getPlayer();

        if (!config.usingConfig) return;

        if (config.worldMessageEnabled) {
            Bukkit.getScheduler().runTaskLater(manager, new Runnable() {
                @Override
                public void run() {
                    configManager.getWorldObject().send(player);
                }
            }, 10l);
            Bukkit.getScheduler().runTaskLater(manager, new Runnable() {
                @Override
                public void run() {
                    configManager.getWorldActionbarObject().send(player);
                }
            }, 10l);
        }

    }
}
