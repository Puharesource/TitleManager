package io.puharesource.mc.sponge.titlemanager.listeners;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.ConfigHandler;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.SpawnEntityEvent;

public final class ListenerWorldChange {
    @Inject private TitleManager plugin;

    @Listener
    public void onChange(final SpawnEntityEvent event) {
        final ConfigHandler configHandler = plugin.getConfigHandler();
        final ConfigMain config = configHandler.getConfig();
        final Player player = event.getPlayer();

        if (!config.usingConfig) return;

        if (config.worldMessageEnabled) {
            plugin.getEngine().schedule(() -> {
                configHandler.getWorldTitleMessage().send(player);
                configHandler.getWorldActionbarTitleMessage().send(player);
            }, 10);
        }
    }
}
