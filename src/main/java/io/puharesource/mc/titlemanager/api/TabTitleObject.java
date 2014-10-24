package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.events.TabTitleChangeEvent;
import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.spigotmc.ProtocolInjector;

public class TabTitleObject {

    private String rawHeader;
    private String rawFooter;

    private IChatBaseComponent header;
    private IChatBaseComponent footer;

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

    public void send(Player p) {
        final TabTitleChangeEvent event = new TabTitleChangeEvent(p, this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        CraftPlayer player = (CraftPlayer) p;
        if (player.getHandle().playerConnection.networkManager.getVersion() != TitleManager.PROTOCOL_VERSION) return;

        if (header == null || footer == null) {
            TabTitleCache titleCache = TabTitleCache.getTabTitle(p.getUniqueId());

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

        TabTitleCache.addTabTitle(p.getUniqueId(), new TabTitleCache(rawHeader, rawFooter));
        ProtocolInjector.PacketTabHeader packet = new ProtocolInjector.PacketTabHeader(header, footer);
        player.getHandle().playerConnection.sendPacket(packet);
    }

    public String getHeader() {
        return rawHeader;
    }

    public TabTitleObject setHeader(String header) {
        rawHeader = header;
        this.header = ChatSerializer.a(TextConverter.convert(header));
        return this;
    }

    public String getFooter() {
        return rawFooter;
    }

    public TabTitleObject setFooter(String footer) {
        rawFooter = footer;
        this.footer = ChatSerializer.a(TextConverter.convert(footer));
        return this;
    }

    public enum Position {HEADER, FOOTER}
}
