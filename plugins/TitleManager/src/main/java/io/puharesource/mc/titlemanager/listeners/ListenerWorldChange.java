package io.puharesource.mc.titlemanager.listeners;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public final class ListenerWorldChange implements Listener {
    @EventHandler
    public void onChange(final PlayerChangedWorldEvent event) {
        final TitleManager manager = TitleManager.getInstance();

        final Config configManager = manager.getConfigManager();
        final ConfigMain config = manager.getConfigManager().getConfig();
        final Player player = event.getPlayer();

        if (!config.usingConfig) return;

        if (config.worldMessageEnabled) {
            Bukkit.getScheduler().runTaskLater(manager, new Runnable() {
                @Override
                public void run() {
                    configManager.getWorldTitleMessage().send(player);
                    configManager.getWorldActionbarTitleMessage().send(player);
                }
            }, 10l);
        }

    }
}
