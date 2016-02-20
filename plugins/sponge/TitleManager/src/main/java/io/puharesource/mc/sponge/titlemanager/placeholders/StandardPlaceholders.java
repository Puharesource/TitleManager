package io.puharesource.mc.sponge.titlemanager.placeholders;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.placeholder.Placeholder;
import io.puharesource.mc.sponge.titlemanager.api.placeholder.PlaceholderReplacer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class StandardPlaceholders implements PlaceholderReplacer {
    @Inject private TitleManager plugin;

    @Placeholder(placeholders = {"PLAYER", "USERNAME", "NAME"})
    public String name(final Player player) { return player.getName(); }

    @Placeholder(placeholders = {"DISPLAYNAME", "DISPLAY-NAME", "NICKNAME", "NICK"})
    public String displayName(final Player player) { return player.getDisplayNameData().displayName().get().toPlain(); }

    @Placeholder(placeholders = {"STRIPPEDDISPLAYNAME", "STRIPPED-DISPLAYNAME", "STRIPPED-NICKNAME", "STRIPPED-NICK"})
    public String strippedDisplayName(final Player player) { return TextSerializers.formattingCode('&').stripCodes(displayName(player)); }

    @Placeholder(placeholders = {"WORLD", "WORLD-NAME"})
    public String worldName(final Player player) { return player.getWorld().getName(); }

    @Placeholder(placeholders = {"WEATHER"})
    public String weather(final Player player) { return String.valueOf(player.getWorld().getWeather().getName()); }

    @Placeholder(placeholders = {"ONLINE", "ONLINE-PLAYERS"})
    public String playerCount(final Player player) { return String.valueOf(Sponge.getServer().getOnlinePlayers().size()); }

    @Placeholder(placeholders = {"MAX-PLAYERS"})
    public String maxOnline(final Player player) { return String.valueOf(Sponge.getServer().getMaxPlayers()); }

    @Placeholder(placeholders = {"WORLD-PLAYERS", "WORLD-ONLINE"})
    public String worldPlayerCount(final Player player) {
        return String.valueOf(Sponge.getServer().getOnlinePlayers()
                .stream()
                .filter(p -> p.getWorld().equals(player.getWorld()))
                .count());
    }

    @Placeholder(placeholders = {"SERVER-TIME"})
    public String serverTime(final Player player) { return new SimpleDateFormat(plugin.getConfigHandler().getMainConfig().getConfig().dateFormat).format(new Date(System.currentTimeMillis())); }
}
