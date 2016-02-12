package io.puharesource.mc.sponge.titlemanager.api;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.animations.TabListPosition;
import io.puharesource.mc.sponge.titlemanager.api.iface.TabListSendable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * This object is being used in both tab list animations and simply when changing the header and/or footer of the tabmenu.
 */
public class TabTitleObject implements TabListSendable {
    @Inject private TitleManager plugin;

    private Optional<Text> header;
    private Optional<Text> footer;

    public TabTitleObject(final Text title, final TabListPosition position) {
        if (position == TabListPosition.HEADER)
            setHeader(title);
        else if (position == TabListPosition.FOOTER)
            setFooter(title);
    }

    public TabTitleObject(final Text header, final Text footer) {
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

        header.ifPresent(text -> tabList.setHeader(plugin.replacePlaceholders(player, text)));
        footer.ifPresent(text -> tabList.setFooter(plugin.replacePlaceholders(player, text)));
    }

    public Optional<Text> getHeader() {
        return header;
    }

    public TabTitleObject setHeader(final Text header) {
        this.header = Optional.ofNullable(header);
        return this;
    }

    public Optional<Text> getFooter() {
        return footer;
    }

    public TabTitleObject setFooter(final Text footer) {
        this.footer = Optional.ofNullable(footer);
        return this;
    }


}
