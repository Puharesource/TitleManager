package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.TabTitleObject;
import io.puharesource.mc.titlemanager.api.TitleObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

        //Updates the config from v1.0.1 to v1.0.2.
        if(getConfig().contains("header")) {
            try {
                FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(config);

                String header = fileConfig.getString("header");
                String footer = fileConfig.getString("footer");

                String title = fileConfig.getString("title");
                String subtitle = fileConfig.getString("subtitle");

                Files.delete(config.toPath());
                Files.copy(plugin.getResource("config.yml"), config.toPath());
                fileConfig = YamlConfiguration.loadConfiguration(config);

                fileConfig.set("tabmenu.header", header);
                fileConfig.set("tabmenu.footer", footer);

                fileConfig.set("welcome_message.title", title);
                fileConfig.set("welcome_message.subtitle", subtitle);

                fileConfig.save(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        plugin.getConfig();
        plugin.saveConfig();
        loadSettings();
    }

    static void loadSettings() {
        usingConfig = getConfig().getBoolean("usingConfig");

        tabTitleObject = new TabTitleObject(ChatColor.translateAlternateColorCodes('&', getConfig().getString("tabmenu.header")), ChatColor.translateAlternateColorCodes('&', getConfig().getString("tabmenu.footer")));
        welcomeObject = new TitleObject(ChatColor.translateAlternateColorCodes('&', getConfig().getString("welcome_message.title")), ChatColor.translateAlternateColorCodes('&', getConfig().getString("welcome_message.subtitle")))
                .setFadeIn(getConfig().getInt("welcome_message.fadeIn")).setStay(getConfig().getInt("welcome_message.stay")).setFadeOut(getConfig().getInt("welcome_message.fadeOut"));

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
