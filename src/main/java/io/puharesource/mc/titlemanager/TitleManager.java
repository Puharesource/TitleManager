package io.puharesource.mc.titlemanager;

import org.bukkit.configuration.file.FileConfiguration;

public class TitleManager {

    public static final int PROTOCOL_VERSION = 47;
    private static Main plugin;

    public static FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public static void saveConfig() {
        plugin.saveConfig();
    }

    public static Main getPlugin() {
        return plugin;
    }

    public static void setPlugin(Main plugin) {
        TitleManager.plugin = plugin;
    }
}
