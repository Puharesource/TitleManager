package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.events.TabTitleChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TabTitleObject {

    private String rawHeader;
    private String rawFooter;

    private Object header;
    private Object footer;

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

        TabTitleCache.addTabTitle(player.getUniqueId(), new TabTitleCache(rawHeader, rawFooter));

        if (rawHeader == null && header == null)
            setHeader("");
        if (rawFooter == null && footer == null)
            setFooter("");

        TitleManager.getReflectionManager().sendPacket(TitleManager.getReflectionManager().constructHeaderAndFooterPacket(
                (rawHeader.contains("{") || rawHeader.contains("}")) ? TitleManager.getReflectionManager().getIChatBaseComponent(TextConverter.setVariables(player, rawHeader)) : header,
                (rawFooter.contains("{") || rawFooter.contains("}")) ? TitleManager.getReflectionManager().getIChatBaseComponent(TextConverter.setVariables(player, rawFooter)) : footer), player);
    }

    public String getHeader() {
        return rawHeader;
    }

    public TabTitleObject setHeader(String header) {
        rawHeader = header == null ? "" : header;
        this.header = TitleManager.getReflectionManager().getIChatBaseComponent(rawHeader);
        return this;
    }

    public String getFooter() {
        return rawFooter;
    }

    public TabTitleObject setFooter(String footer) {
        rawFooter = footer == null ? "" : footer;
        this.footer = TitleManager.getReflectionManager().getIChatBaseComponent(rawFooter);
        return this;
    }

    public enum Position {HEADER, FOOTER}
}
