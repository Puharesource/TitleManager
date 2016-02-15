package io.puharesource.mc.sponge.titlemanager;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.api.placeholder.PlaceholderManager;
import io.puharesource.mc.sponge.titlemanager.commands.TMCommand;
import io.puharesource.mc.sponge.titlemanager.listeners.ListenerConnection;
import io.puharesource.mc.sponge.titlemanager.listeners.ListenerWorldChange;
import lombok.Getter;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.InputStream;
import java.net.URL;
import java.util.Set;

@Plugin(id = "titlemanager", name = "Title Manager", version = "1.0.0-Sponge")
public final class TitleManager {
    @Inject private Logger logger;

    @Getter private Engine engine = new Engine();
    @Getter private ConfigHandler configHandler = new ConfigHandler();
    @Getter private PlaceholderManager placeholderManager = new PlaceholderManager();

    private TMCommand mainCommand;

    private final Set<Integer> runningAnimations = Sets.newSetFromMap(new MapMaker().concurrencyLevel(4).makeMap());

    @Listener
    public void onStart(final GameStartedServerEvent event) {
        mainCommand = new TMCommand();

        logger.debug("Registering listeners.");
        Sponge.getEventManager().registerListeners(this, new ListenerConnection());
        Sponge.getEventManager().registerListeners(this, new ListenerWorldChange());
        logger.debug("Done registering listeners.");
    }

    public Text replacePlaceholders(final Player player, final Text text) {
        if (!containsVariable(text)) return text;

        return placeholderManager.replaceText(player, text);
    }

    public boolean containsVariable(final Text... texts) {
        for (final Text text : texts) {
            final String str = text.toPlain();

            if ((str.contains("{") && str.contains("}")) || str.contains("%")) return true;
        }

        return false;
    }

    public Set<Integer> getRunningAnimations() {
        return ImmutableSet.copyOf(runningAnimations);
    }

    public void addRunningAnimationId(final int id) {
        runningAnimations.add(id);
    }

    public void removeRunningAnimationId(final int id) {
        runningAnimations.remove(id);
    }

    public InputStream getResourceStream(final String fileName) {
        return this.getClass().getResourceAsStream(fileName);
    }

    public URL getResourceURL(final String fileName) {
        return this.getClass().getResource(fileName);
    }
}
