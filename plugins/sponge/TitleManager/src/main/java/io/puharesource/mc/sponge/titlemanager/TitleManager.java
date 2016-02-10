package io.puharesource.mc.sponge.titlemanager;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.api.placeholder.PlaceholderManager;
import io.puharesource.mc.sponge.titlemanager.commands.TMCommand;
import lombok.Getter;
import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

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

    public String setVariables(final Player player, final String text) {
        if (!containsVariable(text)) return text;

        return placeholderManager.replaceText(player, text);
    }

    public boolean containsVariable(final String str, final String... strings) {
        if (str != null && ((str.contains("{") && str.contains("}")) || str.contains("%"))) return true;

        for (final String str0 : strings)
            if (str0 != null && ((str0.contains("{") && str0.contains("}")) || str0.contains("%"))) return true;

        return false;
    }

    private String replaceVariable(final String text, final String variable, final String replacement) {
        try {
            if (text.toLowerCase().contains("{" + variable.toLowerCase() + "}"))
                return text.replaceAll("(?i)\\{" + variable + "\\}", replacement);
            else return text;
        } catch (Exception e) {
            return text;
        }
    }
}
