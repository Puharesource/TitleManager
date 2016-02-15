package io.puharesource.mc.sponge.titlemanager.listeners;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.ConfigHandler;
import io.puharesource.mc.sponge.titlemanager.Engine;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.config.configs.ConfigMain;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
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

        final ConfigMain config = handler.getMainConfig().getConfig();

        if (!config.usingConfig) return;

        if (config.welcomeMessageEnabled) {
            engine.schedule(() -> handler.getTitleWelcomeMessage(!player.get(JoinData.class).isPresent()).send(player), 10);
        }

        if (config.tablistEnabled) {
            engine.schedule(() -> handler.getTabTitleObject().send(player), 10);
        }

        if (config.actionbarWelcomeEnabled) {
            engine.schedule(() -> handler.getActionbarWelcomeMessage(!player.get(JoinData.class).isPresent()).send(player), 10);
        }
    }
}
