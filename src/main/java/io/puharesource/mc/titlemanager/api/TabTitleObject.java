package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.InternalsKt;
import io.puharesource.mc.titlemanager.TitleManagerPlugin;
import io.puharesource.mc.titlemanager.api.events.TabTitleChangeEvent;
import io.puharesource.mc.titlemanager.api.iface.ITabObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * This object is being used in both tabmenu animations and simply when changing the header and/or footer of the tabmenu.
 */
@Deprecated
public class TabTitleObject implements ITabObject {
    private String header;
    private String footer;

    @Deprecated
    public TabTitleObject(String title, Position position) {
        if (position == Position.HEADER)
            setHeader(title);
        else if (position == Position.FOOTER)
            setFooter(title);
    }

    @Deprecated
    public TabTitleObject(String header, String footer) {
        setHeader(header);
        setFooter(footer);
    }

    @Override
    @Deprecated
    public void broadcast() {
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    @Deprecated
    public void send(Player player) {
        final TabTitleChangeEvent event = new TabTitleChangeEvent(player, this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        final TitleManagerPlugin plugin = InternalsKt.getPluginInstance();

        if (header != null && footer != null) {
            plugin.setHeaderAndFooterWithPlaceholders(player, header, footer);
        } else {
            if (header != null) {
                plugin.setHeaderWithPlaceholders(player, header);
            }

            if (footer != null) {
                plugin.setFooterWithPlaceholders(player, footer);
            }
        }
    }

    @Deprecated
    public String getHeader() {
        return header;
    }

    @Deprecated
    public TabTitleObject setHeader(String header) {
        this.header = header == null ? "" : header.replace("\\n", "\n");
        return this;
    }

    @Deprecated
    public String getFooter() {
        return footer;
    }

    @Deprecated
    public TabTitleObject setFooter(String footer) {
        this.footer = footer == null ? "" : footer.replace("\\n", "\n");
        return this;
    }

    @Deprecated
    public enum Position {HEADER, FOOTER}
}