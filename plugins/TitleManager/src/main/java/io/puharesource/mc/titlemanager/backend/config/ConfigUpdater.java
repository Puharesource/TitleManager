package io.puharesource.mc.titlemanager.backend.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigUpdater {
    public static void update(JavaPlugin plugin, ConfigFile configFile) {
        FileConfiguration config = configFile.getConfig();

        //Updates the config from v1.0.1 to v1.0.2.
        if (config.contains("header")) {
            FileConfiguration newConfig = configFile.getCopy();
            configFile.backupToFile(plugin.getDataFolder(), "1.0.1-old-config.yml");
            configFile.regenConfig();

            newConfig.set("tabmenu.header", config.getString("header"));
            newConfig.set("tabmenu.footer", config.getString("footer"));

            newConfig.set("welcome_message.title", config.getString("title"));
            newConfig.set("welcome_message.subtitle", config.getString("subtitle"));

            configFile.save();
        }


        //Updates the config from v1.0.6 to v1.0.7
        if (!config.contains("tabmenu.enabled") || !config.contains("welcome_message.enabled")) {
            FileConfiguration oldConfig = configFile.getCopy();
            configFile.backupToFile(plugin.getDataFolder(), "1.0.6-old-config.yml");
            configFile.regenConfig();

            config.set("tabmenu.header", oldConfig.getString("tabmenu.header"));
            config.set("tabmenu.footer", oldConfig.getString("tabmenu.footer"));

            config.set("welcome_message.title", oldConfig.getString("welcome_message.title"));
            config.set("welcome_message.subtitle", oldConfig.getString("welcome_message.subtitle"));
            config.set("welcome_message.fadeIn", oldConfig.getInt("welcome_message.fadeIn"));
            config.set("welcome_message.stay", oldConfig.getInt("welcome_message.stay"));
            config.set("welcome_message.fadeOut", oldConfig.getInt("welcome_message.fadeOut"));

            configFile.save();
        }


        //Updates the config from v1.2.1 to v1.3.0
        if (config.get("config-version") == null) {
            config.set("config-version", 1);
            ConfigurationSection section = config.createSection("welcome_message.first-join");
            section.set("title", config.getString("welcome_message.title"));
            section.set("subtitle", config.getString("welcome_message.subtitle"));
            config.set("welcome_message.first-join", section);

            configFile.save();
        }


        //Updates the config from config-version 1 to 2
        if (config.getInt("config-version") == 1) {
            config.set("config-version", 2);
            ConfigurationSection section = config.createSection("number-format");
            section.set("enabled", true);
            section.set("format", "#,###.##");
            config.set("number-format", section);

            configFile.save();
        }


        //Updates the config from config-version 2 to 3
        if (config.getInt("config-version") == 2) {
            config.set("config-version", 3);
            ConfigurationSection section = config.createSection("date-format");
            section.set("format", "EEE, dd MMM yyyy HH:mm:ss z");
            config.set("date-format", section);

            configFile.save();
        }

    }

}
