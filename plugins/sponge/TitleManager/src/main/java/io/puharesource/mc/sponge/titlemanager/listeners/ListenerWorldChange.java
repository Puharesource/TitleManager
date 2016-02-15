package io.puharesource.mc.sponge.titlemanager.listeners;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.ConfigHandler;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.config.configs.ConfigMain;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;

public final class ListenerWorldChange {
    @Inject private TitleManager plugin;

    @Listener
    public void onChange(final DisplaceEntityEvent.Teleport event) {
        final ConfigHandler configHandler = plugin.getConfigHandler();
        final ConfigMain config = configHandler.getMainConfig().getConfig();
        final Entity entity = event.getTargetEntity();

        if (!(entity instanceof Player)) return;
        if (!config.usingConfig) return;

        final Player player = (Player) event.getTargetEntity();

        if (config.worldMessageEnabled) {
            plugin.getEngine().schedule(() -> {
                configHandler.getWorldTitleMessage().send(player);
                configHandler.getWorldActionbarTitleMessage().send(player);
            }, 10);
        }
    }
}
