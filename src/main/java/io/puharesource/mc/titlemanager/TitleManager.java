package io.puharesource.mc.titlemanager;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

public class TitleManager {

    private static Main plugin;
    private static ReflectionManager reflectionManager;

    public static FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public static void saveConfig() {
        plugin.saveConfig();
    }

    public static Main getPlugin() {
        return plugin;
    }

    public static void load(Main plugin) {
        TitleManager.plugin = plugin;
        try {
            reflectionManager = new ReflectionManager();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.SEVERE, "Failed to load NMS classes, please update to the latest version of Spigot! Disabling plugin...");
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    public static ReflectionManager getReflectionManager() {
        return reflectionManager;
    }
}
