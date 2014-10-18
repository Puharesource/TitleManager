package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.TabTitleObject;
import io.puharesource.mc.titlemanager.api.TextConverter;
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
    private static boolean tabmenuEnabled;
    private static boolean welcomeMessageEnabled;

    private static TitleObject welcomeObject;
    private static TabTitleObject tabTitleObject;

    public static void loadConfig() throws IOException {
        Main plugin = TitleManager.getPlugin();

        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();
        File config = new File(plugin.getDataFolder(), "config.yml");

        if (!config.exists()) Files.copy(plugin.getResource("config.yml"), config.toPath());


        //Updates the config from v1.0.1 to v1.0.2.
        if (getConfig().contains("header")) {
            FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(config);

            Files.copy(config.toPath(), new File(plugin.getDataFolder(), "1.0.1-old-config.yml").toPath());
            Files.delete(config.toPath());
            if (!config.exists())
                Files.copy(plugin.getResource("config.yml"), config.toPath());
            FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));

            fileConfig.set("tabmenu.header", oldConfig.getString("header"));
            fileConfig.set("tabmenu.footer", oldConfig.getString("footer"));

            fileConfig.set("welcome_message.title", oldConfig.getString("title"));
            fileConfig.set("welcome_message.subtitle", oldConfig.getString("subtitle"));

            fileConfig.save(config);
            reloadConfig();
        }

        //Updates the config from v1.0.6 to v1.0.7
        if (!getConfig().contains("tabmenu.enabled") || !getConfig().contains("welcome_message.enabled")) {
            FileConfiguration oldConfig = YamlConfiguration.loadConfiguration(config);

            Files.copy(config.toPath(), new File(plugin.getDataFolder(), "1.0.6-old-config.yml").toPath());
            Files.delete(config.toPath());
            if (!config.exists())
                Files.copy(plugin.getResource("config.yml"), config.toPath());
            FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));

            fileConfig.set("tabmenu.header", oldConfig.getString("tabmenu.header"));
            fileConfig.set("tabmenu.footer", oldConfig.getString("tabmenu.footer"));

            fileConfig.set("welcome_message.title", oldConfig.getString("welcome_message.title"));
            fileConfig.set("welcome_message.subtitle", oldConfig.getString("welcome_message.subtitle"));
            fileConfig.set("welcome_message.fadeIn", oldConfig.getInt("welcome_message.fadeIn"));
            fileConfig.set("welcome_message.stay", oldConfig.getInt("welcome_message.stay"));
            fileConfig.set("welcome_message.fadeOut", oldConfig.getInt("welcome_message.fadeOut"));

            fileConfig.save(config);
            reloadConfig();
        }

        loadSettings();
    }

    static void loadSettings() {
        usingConfig = getConfig().getBoolean("usingConfig");
        tabmenuEnabled = getConfig().getBoolean("tabmenu.enabled");
        welcomeMessageEnabled = getConfig().getBoolean("welcome_message.enabled");

        if (tabmenuEnabled) {
            tabTitleObject = new TabTitleObject(ChatColor.translateAlternateColorCodes('&', getConfig().getString("tabmenu.header").replace("\\n", "\n")), ChatColor.translateAlternateColorCodes('&', getConfig().getString("tabmenu.footer").replace("\\n", "\n")));
            for (Player player : Bukkit.getOnlinePlayers()) {
                TabTitleObject tabObject = tabTitleObject;
                if (tabObject.getHeader() != null)
                    tabObject.setHeader(TextConverter.setPlayerName(player, tabObject.getHeader()));
                if (tabObject.getFooter() != null)
                    tabObject.setFooter(TextConverter.setPlayerName(player, tabObject.getFooter()));
                tabTitleObject.send(player);
            }
        }
        if (welcomeMessageEnabled)
            welcomeObject = new TitleObject(ChatColor.translateAlternateColorCodes('&', getConfig().getString("welcome_message.title")), ChatColor.translateAlternateColorCodes('&', getConfig().getString("welcome_message.subtitle")))
                    .setFadeIn(getConfig().getInt("welcome_message.fadeIn")).setStay(getConfig().getInt("welcome_message.stay")).setFadeOut(getConfig().getInt("welcome_message.fadeOut"));
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

    public static boolean isTabmenuEnabled() {
        return tabmenuEnabled;
    }

    public static boolean isWelcomeMessageEnabled() {
        return welcomeMessageEnabled;
    }
}
