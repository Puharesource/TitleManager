package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.InternalsKt;
import io.puharesource.mc.titlemanager.TitleManagerPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Due to the fact that you can't simply change the header or footer by themselves, this class acts as a cache for and easy way to do so.
 * Whenever a player's tabmenu has been changed using the {@link TabTitleObject} the header and footer will be stored in this cache.
 *
 * @deprecated In favor of the methods seen under the "see also" section.
 *
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#getHeader(Player)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#getFooter(Player)
 *
 * @since 1.0.1
 */
@Deprecated
public class TabTitleCache {
    private final String header;
    private final String footer;

    @Deprecated
    public TabTitleCache(final String header, final String footer) {
        this.header = header;
        this.footer = footer;
    }

    @Deprecated
    public static TabTitleCache getTabTitle(final UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            return null;
        }

        final TitleManagerPlugin plugin = InternalsKt.getPluginInstance();

        final String header = plugin.getHeader(player);
        final String footer = plugin.getFooter(player);

        return new TabTitleCache(header, footer);
    }

    @Deprecated
    public static void addTabTitle(final UUID uuid, final TabTitleCache titleCache) {
        final Player player = Bukkit.getPlayer(uuid);

        if (titleCache != null && player != null) {
            final TitleManagerPlugin plugin = InternalsKt.getPluginInstance();

            final String header = titleCache.getHeader();
            final String footer = titleCache.getFooter();

            if (header != null) {
                plugin.setHeader(player, header);
            }

            if (footer != null) {
                plugin.setFooter(player, footer);
            }
        }
    }

    @Deprecated
    public static void removeTabTitle(UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            final TitleManagerPlugin plugin = InternalsKt.getPluginInstance();

            plugin.setHeader(player, "");
            plugin.setFooter(player, "");
        }
    }

    @Deprecated
    public String getHeader() {
        return header;
    }

    @Deprecated
    public String getFooter() {
        return footer;
    }
}