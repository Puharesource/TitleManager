package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.TabTitleObject;
import io.puharesource.mc.titlemanager.api.TitleObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Config {

    private static boolean usingConfig;

    private static TitleObject welcomeObject;
    private static TabTitleObject tabTitleObject;

    public static void loadConfig() {
        Main plugin = TitleManager.getPlugin();

        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();
        File config = new File(plugin.getDataFolder(), "config.yml");

        try {
            if (!config.exists()) Files.copy(plugin.getResource("config.yml"), config.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        plugin.getConfig();
        plugin.saveConfig();
        loadSettings();
    }

    static void loadSettings() {
        usingConfig = getConfig().getBoolean("usingConfig");

        tabTitleObject = new TabTitleObject(ChatColor.translateAlternateColorCodes('&', getConfig().getString("header")), ChatColor.translateAlternateColorCodes('&', getConfig().getString("footer")));
        welcomeObject = new TitleObject(ChatColor.translateAlternateColorCodes('&', getConfig().getString("title")), ChatColor.translateAlternateColorCodes('&', getConfig().getString("subtitle")));

        for(Player player : Bukkit.getOnlinePlayers())
            tabTitleObject.send(player);
    }

    public static void reloadConfig() {
        TitleManager.getPlugin().reloadConfig();
        loadSettings();
    }

    public static FileConfiguration getConfig() {
        return TitleManager.getPlugin().getConfig();
    }

    public static void saveConfig() {
        TitleManager.getPlugin().saveConfig();
    }

    public static boolean isUsingConfig() {
        return usingConfig;
    }

    public static TitleObject getWelcomeObject() {
        return welcomeObject;
    }

    public static TabTitleObject getTabTitleObject() {
        return tabTitleObject;
    }
}
