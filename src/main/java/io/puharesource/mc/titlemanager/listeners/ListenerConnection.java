package io.puharesource.mc.titlemanager.listeners;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TabTitleCache;
import io.puharesource.mc.titlemanager.api.TabTitleObject;
import io.puharesource.mc.titlemanager.api.TextConverter;
import io.puharesource.mc.titlemanager.api.TitleObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ListenerConnection implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (Config.isUsingConfig()) {
            if (Config.isWelcomeMessageEnabled()) {
                final TitleObject titleObject = new TitleObject(Config.getWelcomeObject().getTitle(), Config.getWelcomeObject().getSubtitle())
                        .setFadeIn(Config.getWelcomeObject().getFadeIn()).setStay(Config.getWelcomeObject().getStay()).setFadeOut(Config.getWelcomeObject().getFadeOut());
                long delay = 0l;

                //if (titleObject.getTitle().toLowerCase().contains("{displayname}") || titleObject.getTitle().toLowerCase().contains("{strippeddisplayname}") ||
                        //titleObject.getSubtitle().toLowerCase().contains("{displayname}") || titleObject.getSubtitle().toLowerCase().contains("{strippeddisplayname}"))
                    //delay = 10l;
                for(TitleVariable tv : TitleVariable.values())
                {
                    if(titleObject.getTitle().toLowerCase().contains(tv.getTextRaw().toLowerCase()))
                    {
                        delay = 101
                        break;
                    }
                }
                Bukkit.getScheduler().runTaskLater(TitleManager.getPlugin(), new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (titleObject.getTitle() != null)
                            titleObject.setTitle(TextConverter.setVariables(player, titleObject.getTitle()));
                        if (titleObject.getSubtitle() != null)
                            titleObject.setSubtitle(TextConverter.setVariables(player, titleObject.getSubtitle()));
                        titleObject.send(player);
                    }
                }, delay);
            }
            if (Config.isTabmenuEnabled()) {
                final TabTitleObject tabTitleObject = new TabTitleObject(Config.getTabTitleObject().getHeader(), Config.getTabTitleObject().getFooter());
                long delay = 0l;

                if (tabTitleObject.getHeader() != null && tabTitleObject.getFooter() != null)
                {
                    for(TitleVariable tv : TitleVariable.values())
                    {
                        if(titleObject.getTitle().toLowerCase().contains(tv.getTextRaw().toLowerCase()))
                        {
                            delay = 101
                            break;
                        }
                    }
                }

                Bukkit.getScheduler().runTaskLater(TitleManager.getPlugin(), new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (tabTitleObject.getHeader() != null)
                            tabTitleObject.setHeader(TextConverter.setVariables(player, tabTitleObject.getHeader()));
                        if (tabTitleObject.getFooter() != null)
                            tabTitleObject.setFooter(TextConverter.setVariables(player, tabTitleObject.getFooter()));

                        tabTitleObject.send(player);
                    }
                }, delay);
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
