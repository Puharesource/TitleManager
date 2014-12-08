package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.TabTitleObject;
import io.puharesource.mc.titlemanager.api.TextConverter;
import io.puharesource.mc.titlemanager.api.TitleObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private static boolean usingConfig;
    private static boolean tabmenuEnabled;
    private static boolean welcomeMessageEnabled;

    private static TitleObject welcomeObject;
    private static TabTitleObject tabTitleObject;

    private static ConfigFile configFile;
    private static ConfigFile animationConfigFile;

    private static List<String> animations = new ArrayList<>();

    public static void loadConfig() throws IOException {
        Main plugin = TitleManager.getPlugin();

        configFile = new ConfigFile(plugin, plugin.getDataFolder(), "config", true);
        animationConfigFile = new ConfigFile(plugin, plugin.getDataFolder(), "animations", true);

        configFile.load();
        animationConfigFile.load();

        FileConfiguration config = configFile.getConfig();

        //Updates the config from v1.0.1 to v1.0.2.
        if (getConfig().contains("header")) {

            configFile.backupToFile(plugin.getDataFolder(), "1.0.1-old-config.yml");
            configFile.regenConfig();

            FileConfiguration newConfig = configFile.getCopy();

            newConfig.set("tabmenu.header", config.getString("header"));
            newConfig.set("tabmenu.footer", config.getString("footer"));

            newConfig.set("welcome_message.title", config.getString("title"));
            newConfig.set("welcome_message.subtitle", config.getString("subtitle"));

            configFile.save();
            config = configFile.getConfig();
            reloadConfig();
        }

        //Updates the config from v1.0.6 to v1.0.7
        if (!getConfig().contains("tabmenu.enabled") || !getConfig().contains("welcome_message.enabled")) {
            configFile.backupToFile(plugin.getDataFolder(), "1.0.6-old-config.yml");
            configFile.regenConfig();

            FileConfiguration oldConfig = configFile.getCopy();

            config.set("tabmenu.header", oldConfig.getString("tabmenu.header"));
            config.set("tabmenu.footer", oldConfig.getString("tabmenu.footer"));

            config.set("welcome_message.title", oldConfig.getString("welcome_message.title"));
            config.set("welcome_message.subtitle", oldConfig.getString("welcome_message.subtitle"));
            config.set("welcome_message.fadeIn", oldConfig.getInt("welcome_message.fadeIn"));
            config.set("welcome_message.stay", oldConfig.getInt("welcome_message.stay"));
            config.set("welcome_message.fadeOut", oldConfig.getInt("welcome_message.fadeOut"));

            configFile.save();
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
