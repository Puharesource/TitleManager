package io.puharesource.mc.sponge.titlemanager;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.api.placeholder.PlaceholderManager;
import io.puharesource.mc.sponge.titlemanager.commands.TMCommand;
import lombok.Getter;
import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.util.Set;

@Plugin(id = "titlemanager", name = "Title Manager", version = "1.0.0-Sponge")
public final class TitleManager {
    @Inject private Logger logger;

    @Getter private Engine engine = new Engine();
    @Getter private ConfigHandler configHandler = new ConfigHandler();
    @Getter private PlaceholderManager placeholderManager = new PlaceholderManager();

    private TMCommand mainCommand;

    @Listener
    public void onStart(final GameStartedServerEvent event) {
        mainCommand = new TMCommand();
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
        runningAnimations.remove((Integer) id);
    }
}
