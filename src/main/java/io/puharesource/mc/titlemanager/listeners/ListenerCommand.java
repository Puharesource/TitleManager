package io.puharesource.mc.titlemanager.listeners;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TabTitleCache;
import io.puharesource.mc.titlemanager.api.TabTitleObject;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.animations.TabTitleAnimation;
import io.puharesource.mc.titlemanager.api.animations.TitleAnimation;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ListenerCommand implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommandSend(final PlayerCommandPreprocessEvent event) {
        if(event.isCancelled()) return;
        if(Config.getCommandTitle(event.getMessage())!=null)
        {
            List<Object> messages = Config.getCommandTitle(event.getMessage);
            for(Object obj : messages)
            {
                if(obj instanceof TitleObject)
                {
                    ((TitleObject) obj).send(event.getPlayer());
                }
                if(obj instanceof ActionbarTitleObject)
                {
                    ((ActionbarTitleObject) obj).send(event.getPlayer());
                }
                if(obj instanceof ActionbarTitleAnimation)
                {
                    ((ActionbarTitleAnimation) obj).send(event.getPlayer());
                }
                if(obj instanceof TitleAnimation)
                {
                    ((TitleAnimation) obj).send(event.getPlayer());
                }
            }
        }
    }
}
