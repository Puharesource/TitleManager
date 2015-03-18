package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.titlemanager.api.animations.TabTitleAnimation;
import io.puharesource.mc.titlemanager.api.animations.TitleAnimation;
import io.puharesource.mc.titlemanager.api.iface.ITabObject;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.backend.config.ConfigFile;
import io.puharesource.mc.titlemanager.backend.config.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class __Config {

    private static boolean usingConfig;
    private static boolean tabmenuEnabled;
    private static boolean welcomeMessageEnabled;
    private static boolean numberFormatEnabled;

    private static ITitleObject welcomeObject;
    private static ITitleObject firstWelcomeObject;
    private static TabTitleAnimation tabTitleObject;
    
    private static String numberFormat;

    private static ConfigFile configFile;
    private static ConfigFile animationConfigFile;

    private static Map<String, FrameSequence> animations = new HashMap<>();

    public static void loadConfig() {
        Main plugin = TitleManager.getPlugin();

        configFile = new ConfigFile(plugin, plugin.getDataFolder(), "config", true);
        animationConfigFile = new ConfigFile(plugin, plugin.getDataFolder(), "animations", true);

        ConfigUpdater.update(plugin, configFile);

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
        numberFormatEnabled = getConfig().getBoolean("number-format.enabled");

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
        
        if (numberFormatEnabled)
            numberFormat = configFile.getConfig().getString("number-format.format");
    }

    public static void reloadConfig() {
        configFile.reload();
        animationConfigFile.reload();

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

    public static boolean isNumberFormatEnabled() {
        return numberFormatEnabled;
    }

    public static String getNumberFormat() {
        return numberFormat;
    }
}
