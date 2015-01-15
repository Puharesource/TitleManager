package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.titlemanager.api.animations.TabTitleAnimation;
import io.puharesource.mc.titlemanager.api.animations.TitleAnimation;
import io.puharesource.mc.titlemanager.api.iface.ITabObject;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Config {

    private static boolean usingConfig;
    private static boolean tabmenuEnabled;
    private static boolean welcomeMessageEnabled;

    private static ITitleObject welcomeObject;
    private static ITitleObject firstWelcomeObject;
    private static TabTitleAnimation tabTitleObject;

    private static ConfigFile configFile;
    private static ConfigFile animationConfigFile;

    private static Map<String, FrameSequence> animations = new HashMap<>();

    public static void loadConfig() {
        Main plugin = TitleManager.getPlugin();

        configFile = new ConfigFile(plugin, plugin.getDataFolder(), "config", true);
        animationConfigFile = new ConfigFile(plugin, plugin.getDataFolder(), "animations", true);

        configFile.load();
        animationConfigFile.load();

        FileConfiguration config = getConfig();

        //Updates the config from v1.0.1 to v1.0.2.
        if (config.contains("header")) {
            FileConfiguration newConfig = configFile.getCopy();
            configFile.backupToFile(plugin.getDataFolder(), "1.0.1-old-config.yml");
            configFile.regenConfig();

            newConfig.set("tabmenu.header", config.getString("header"));
            newConfig.set("tabmenu.footer", config.getString("footer"));

            newConfig.set("welcome_message.title", config.getString("title"));
            newConfig.set("welcome_message.subtitle", config.getString("subtitle"));

            saveConfig();
            config = configFile.getConfig();
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

            saveConfig();
        }

        //Updates the config from v1.2.1 to 1.3.0
        if (config.get("config-version") == null) {
            config.set("config-version", 1);
            ConfigurationSection section = config.createSection("welcome_message.first-join");
            section.set("title", config.getString("welcome_message.title"));
            section.set("subtitle", config.getString("welcome_message.subtitle"));
            config.set("welcome_message.first-join", section);

            saveConfig();
        }

        plugin.reloadConfig();
        loadSettings();
    }

    static void loadSettings() {
        for (String str : animationConfigFile.getConfig().getKeys(false)) {
            ConfigurationSection section = animationConfigFile.getConfig().getConfigurationSection(str);
            List<AnimationFrame> frames = new ArrayList<>();
            for (String frame : section.getStringList("frames")) {
                int fadeIn = -1;
                int stay = -1;
                int fadeOut = -1;
                frame = ChatColor.translateAlternateColorCodes('&', frame);
                if (frame.startsWith("[") && frame.length() > 1) {
                    char[] chars = frame.toCharArray();
                    String timesString = "";
                    for (int i = 1; frame.length() > i; i++) {
                        char c = chars[i];
                        if (c == ']') {
                            frame = frame.substring(i + 1);
                            break;
                        }
                        timesString += chars[i];
                    }

                    try {
                        String[] times = timesString.split(";", 3);
                        fadeIn = Integer.valueOf(times[0]);
                        stay = Integer.valueOf(times[1]);
                        fadeOut = Integer.parseInt(times[2]);
                    } catch (NumberFormatException ignored) {
                    }

                    frames.add(new AnimationFrame(frame, fadeIn, stay, fadeOut));
                }
            }
            animations.put(str.toUpperCase().trim(), new FrameSequence(frames));
        }

        usingConfig = getConfig().getBoolean("usingConfig");

        if (!usingConfig) return;

        tabmenuEnabled = getConfig().getBoolean("tabmenu.enabled");
        welcomeMessageEnabled = getConfig().getBoolean("welcome_message.enabled");

        if (tabmenuEnabled) {
            String headerString = getConfig().getString("tabmenu.header");
            String footerString = getConfig().getString("tabmenu.footer");
            if (headerString.toLowerCase().startsWith("animation:") || footerString.toLowerCase().startsWith("animation:")) {
                Object header;
                Object footer;

                if (headerString.toLowerCase().startsWith("animation:"))
                    header = getAnimation(headerString.substring("animation:".length()));
                else header = ChatColor.translateAlternateColorCodes('&', headerString);
                if (footerString.toLowerCase().startsWith("animation:"))
                    footer = getAnimation(footerString.substring("animation:".length()));
                else footer = ChatColor.translateAlternateColorCodes('&', footerString);

                tabTitleObject = new TabTitleAnimation(header == null ? "" : header, footer == null ? "" : footer);
            } else tabTitleObject = new TabTitleAnimation(new FrameSequence(Arrays.asList(new AnimationFrame(ChatColor.translateAlternateColorCodes('&', headerString), 0, 5, 0))), new FrameSequence(Arrays.asList(new AnimationFrame(ChatColor.translateAlternateColorCodes('&', footerString), 0, 5, 0))));
            tabTitleObject.broadcast();
        }
        if (welcomeMessageEnabled) {
            String titleString = getConfig().getString("welcome_message.title");
            String subtitleString = getConfig().getString("welcome_message.subtitle");
            if (titleString.toLowerCase().startsWith("animation:") || subtitleString.toLowerCase().startsWith("animation:")) {
                Object title;
                Object subtitle;

                if (titleString.toLowerCase().startsWith("animation:"))
                    title = getAnimation(titleString.substring("animation:".length()));
                else title = ChatColor.translateAlternateColorCodes('&', titleString.replace("\\n", "\n"));
                if (subtitleString.toLowerCase().startsWith("animation:"))
                    subtitle = getAnimation(subtitleString.substring("animation:".length()));
                else subtitle = ChatColor.translateAlternateColorCodes('&', subtitleString.replace("\\n", "\n"));

                welcomeObject = new TitleAnimation(title, subtitle);
            } else {
                welcomeObject = new TitleObject(ChatColor.translateAlternateColorCodes('&', getConfig().getString("welcome_message.title")), ChatColor.translateAlternateColorCodes('&', getConfig().getString("welcome_message.subtitle")))
                        .setFadeIn(getConfig().getInt("welcome_message.fadeIn")).setStay(getConfig().getInt("welcome_message.stay")).setFadeOut(getConfig().getInt("welcome_message.fadeOut"));
            }

            titleString = getConfig().getString("welcome_message.first-join.title");
            subtitleString = getConfig().getString("welcome_message.first-join.subtitle");
            if (titleString.toLowerCase().startsWith("animation:") || subtitleString.toLowerCase().startsWith("animation:")) {
                Object title;
                Object subtitle;

                if (titleString.toLowerCase().startsWith("animation:"))
                    title = getAnimation(titleString.substring("animation:".length()));
                else title = ChatColor.translateAlternateColorCodes('&', titleString.replace("\\n", "\n"));
                if (subtitleString.toLowerCase().startsWith("animation:"))
                    subtitle = getAnimation(subtitleString.substring("animation:".length()));
                else subtitle = ChatColor.translateAlternateColorCodes('&', subtitleString.replace("\\n", "\n"));

                firstWelcomeObject = new TitleAnimation(title, subtitle);
            } else {
                firstWelcomeObject = new TitleObject(ChatColor.translateAlternateColorCodes('&', titleString), ChatColor.translateAlternateColorCodes('&', subtitleString))
                        .setFadeIn(getConfig().getInt("welcome_message.fadeIn")).setStay(getConfig().getInt("welcome_message.stay")).setFadeOut(getConfig().getInt("welcome_message.fadeOut"));
            }
        }
    }

    public static void reloadConfig() {
        configFile.load();
        animationConfigFile.load();

        animations.clear();

        for (int id : TitleManager.getRunningAnimations())
            Bukkit.getScheduler().cancelTask(id);

        TitleManager.getRunningAnimations().clear();

        loadSettings();
    }

    public static FrameSequence getAnimation(String animation) {
        return animations.get(animation.toUpperCase().trim());
    }

    public static Map<String, FrameSequence> getAnimations() {
        return animations;
    }

    public static FileConfiguration getConfig() {
        return configFile.getConfig();
    }

    public static void saveConfig() {
        configFile.save();
    }

    public static boolean isUsingConfig() {
        return usingConfig;
    }

    public static ITitleObject getWelcomeObject() {
        return welcomeObject;
    }

    public static ITitleObject getFirstWelcomeObject() {
        return firstWelcomeObject;
    }

    public static ITabObject getTabTitleObject() {
        return tabTitleObject;
    }

    public static boolean isTabmenuEnabled() {
        return tabmenuEnabled;
    }

    public static boolean isWelcomeMessageEnabled() {
        return welcomeMessageEnabled;
    }
}
