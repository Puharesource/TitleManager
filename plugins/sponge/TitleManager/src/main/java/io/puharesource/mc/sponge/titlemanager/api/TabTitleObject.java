package io.puharesource.mc.sponge.titlemanager.api;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.iface.ITabObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * This object is being used in both tabmenu animations and simply when changing the header and/or footer of the tabmenu.
 */
public class TabTitleObject implements ITabObject {
    @Inject private TitleManager plugin;

    private Optional<String> header;
    private Optional<String> footer;

    public TabTitleObject(final String title, final Position position) {
        if (position == Position.HEADER)
            setHeader(title);
        else if (position == Position.FOOTER)
            setFooter(title);
    }

    public TabTitleObject(final String header, final String footer) {
        setHeader(header);
        setFooter(footer);
    }

    @Override
    public void broadcast() {
        Sponge.getServer().getOnlinePlayers().forEach(this::send);
    }

    @Override
    public void broadcast(final World world) {
        Sponge.getServer().getOnlinePlayers()
                .stream()
                .filter(p -> p.getWorld().equals(world))
                .forEach(this::send);
    }

    public void send(final Player player) {
        final TabList tabList = player.getTabList();

        header.ifPresent(str -> tabList.setHeader(Text.of(plugin.replacePlaceholders(player, str))));
        footer.ifPresent(str -> tabList.setFooter(Text.of(plugin.replacePlaceholders(player, str))));
    }

    public Optional<String> getHeader() {
        return header;
    }

    public TabTitleObject setHeader(final String header) {
        this.header = Optional.ofNullable(header == null ? null : header.replace("\\n", "\n"));
        return this;
    }

    public Optional<String> getFooter() {
        return footer;
    }

    public TabTitleObject setFooter(final String footer) {
        this.footer = Optional.ofNullable(footer == null ? null : footer.replace("\\n", "\n"));
        return this;
    }

    public enum Position {HEADER, FOOTER}
}
