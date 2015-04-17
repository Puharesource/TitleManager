package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.api.events.TabTitleChangeEvent;
import io.puharesource.mc.titlemanager.api.iface.ITabObject;
import io.puharesource.mc.titlemanager.backend.packet.TabmenuPacket;
import io.puharesource.mc.titlemanager.backend.player.TMPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * This object is being used in both tabmenu animations and simply when changing the header and/or footer of the tabmenu.
 */
public class TabTitleObject implements ITabObject {

    private String header;
    private String footer;

    public TabTitleObject(String title, Position position) {
        if (position == Position.HEADER)
            setHeader(title);
        else if (position == Position.FOOTER)
            setFooter(title);
    }

    public TabTitleObject(String header, String footer) {
        setHeader(header);
        setFooter(footer);
    }

    @Override
    public void broadcast() {
        for (Player player : Bukkit.getOnlinePlayers())
            send(player);
    }

    public void send(Player player) {
        final TabTitleChangeEvent event = new TabTitleChangeEvent(player, this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        if (header == null || footer == null) {
            TabTitleCache titleCache = TabTitleCache.getTabTitle(player.getUniqueId());

            if (titleCache != null) {
                if (header == null) {
                    String headerString = titleCache.getHeader();
                    if (headerString != null)
                        setHeader(headerString);
                }
                if (footer == null) {
                    String footerString = titleCache.getFooter();
                    if (footerString != null)
                        setFooter(footerString);
                }
            }
        }

        TabTitleCache.addTabTitle(player.getUniqueId(), new TabTitleCache(header, footer));

        if (header == null)
            setHeader("");
        if (footer == null)
            setFooter("");

        TMPlayer tmPlayer = new TMPlayer(player);

        tmPlayer.sendPacket(new TabmenuPacket(
                TextConverter.setVariables(player, header),
                TextConverter.setVariables(player, footer)));
    }

    public String getHeader() {
        return header;
    }

    public TabTitleObject setHeader(String header) {
        this.header = header == null ? "" : header.replace("\\n", "\n");
        return this;
    }

    public String getFooter() {
        return footer;
    }

    public TabTitleObject setFooter(String footer) {
        this.footer = footer == null ? "" : footer.replace("\\n", "\n");
        return this;
    }

    public enum Position {HEADER, FOOTER}
}
