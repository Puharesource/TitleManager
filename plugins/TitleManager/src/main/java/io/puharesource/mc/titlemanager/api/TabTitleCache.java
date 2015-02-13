package io.puharesource.mc.titlemanager.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TabTitleCache {
    final private static Map<UUID, TabTitleCache> playerTabTitles = new HashMap<>();

    private String header;
    private String footer;

    public TabTitleCache(String header, String footer) {
        this.header = header;
        this.footer = footer;
    }

    public static TabTitleCache getTabTitle(UUID uuid) {
        return playerTabTitles.get(uuid);
    }

    public static void addTabTitle(UUID uuid, TabTitleCache titleCache) {
        playerTabTitles.put(uuid, titleCache);
    }

    public static void removeTabTitle(UUID uuid) {
        playerTabTitles.remove(uuid);
    }

    public String getHeader() {
        return header;
    }

    public String getFooter() {
        return footer;
    }
}
