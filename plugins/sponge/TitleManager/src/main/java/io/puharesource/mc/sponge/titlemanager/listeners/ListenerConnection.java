package io.puharesource.mc.sponge.titlemanager.listeners;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.ConfigHandler;
import io.puharesource.mc.sponge.titlemanager.Engine;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public final class ListenerConnection {
    @Inject private TitleManager plugin;

    @Listener
    public void onJoin(final ClientConnectionEvent.Join event) {
        final Player player = event.getTargetEntity();
        final Engine engine = plugin.getEngine();
        final ConfigHandler handler = plugin.getConfigHandler();

        if (!handler.usingConfig) return;

        if (handler.welcomeMessageEnabled) {
            engine.schedule(() -> configManager.getTitleWelcomeMessage(!player.hasPlayedBefore()).send(player), 10);
        }

        if (handler.tabmenuEnabled) {
            engine.schedule(() -> configManager.getTabTitleObject().send(player), 10L);
        }

        if (handler.actionbarWelcomeEnabled) {
            engine.schedule(() -> configManager.getActionbarWelcomeMessage(!player.hasPlayedBefore()).send(player), 10L);
        }
    }
}
