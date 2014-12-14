package io.puharesource.mc.titlemanager.listeners;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.*;
import io.puharesource.mc.titlemanager.api.animations.TabTitleAnimation;
import io.puharesource.mc.titlemanager.api.animations.TitleAnimation;
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
    public void onJoin(PlayerJoinEvent event) {
        if (Config.isUsingConfig()) return;
        final Player player = event.getPlayer();

        if (Config.isWelcomeMessageEnabled()) {
            final Object welcomeObject = Config.getWelcomeObject();

            if (welcomeObject instanceof TitleAnimation) {
                final TitleAnimation titleAnimation = (TitleAnimation) welcomeObject;
                Bukkit.getScheduler().runTaskLater(TitleManager.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        titleAnimation.send(player);
                    }
                }, 10l);
            } else {
                final TitleObject titleObject = (TitleObject) welcomeObject;
                Bukkit.getScheduler().runTaskLater(TitleManager.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        titleObject.send(player);
                    }
                }, 10l);
            }
        }
        if (Config.isTabmenuEnabled()) {
            final Object tabWelcomeObject = Config.getTabTitleObject();

            if (tabWelcomeObject instanceof TabTitleAnimation) {
                final TabTitleAnimation titleAnimation = (TabTitleAnimation) tabWelcomeObject;
                Bukkit.getScheduler().runTaskLater(TitleManager.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        titleAnimation.send(player);
                    }
                }, 10l);
            } else {
                Bukkit.getScheduler().runTaskLater(TitleManager.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        ((TabTitleObject) tabWelcomeObject).send(player);
                    }
                }, 10l);
            }
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
