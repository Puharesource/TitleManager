package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.TabTitleObject;
import io.puharesource.mc.titlemanager.api.TextConverter;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.titlemanager.api.animations.TabTitleAnimation;
import io.puharesource.mc.titlemanager.api.animations.TitleAnimation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private static boolean usingConfig;
    private static boolean tabmenuEnabled;
    private static boolean welcomeMessageEnabled;
    private static boolean returnMessageEnabled;

    private static Object welcomeObject;
    private static Object returnMsgObject;
    private static Object tabTitleObject;

    private static ConfigFile configFile;
    private static ConfigFile animationConfigFile;
    private static ConfigFile usersList;

    private static Map<String, FrameSequence> animations = new HashMap<>();

    public static void loadConfig() {
        Main plugin = TitleManager.getPlugin();

        configFile = new ConfigFile(plugin, plugin.getDataFolder(), "config", true);
        animationConfigFile = new ConfigFile(plugin, plugin.getDataFolder(), "animations", true);
        usersList = new ConfigFile(plugin, plugin.getDataFolder(), "users", true);
        
        configFile.load();
        animationConfigFile.load();
        usersList.load();

        FileConfiguration config = configFile.getConfig();

        //Updates the config from v1.0.1 to v1.0.2.
        if (getConfig().contains("header")) {

            configFile.backupToFile(plugin.getDataFolder(), "1.0.1-old-config.yml");
            configFile.regenConfig();

            FileConfiguration newConfig = configFile.getCopy();

            newConfig.set("tabmenu.header", config.getString("header"));
            newConfig.set("tabmenu.footer", config.getString("footer"));

            newConfig.set("welcome_message.title", config.getString("welcome_message.title"));
            newConfig.set("welcome_message.subtitle", config.getString("welcome_message.subtitle"));
            
            newConfig.set("return_message.title", config.getString("return_message.title"));
            newConfig.set("return_message.subtitle", config.getString("return_message.subtitle"));

            configFile.save();
            config = configFile.getConfig();
            reloadConfig();
        }

        //Updates the config from v1.0.6 to v1.0.7
        if (!getConfig().contains("tabmenu.enabled") || !getConfig().contains("welcome_message.enabled") || !getConfig().contains("return_message.enabled") {
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

            config.set("return_message.title", oldConfig.getString("return_message.title"));
            config.set("return_message.subtitle", oldConfig.getString("return_message.subtitle"));
            config.set("return_message.fadeIn", oldConfig.getInt("return_message.fadeIn"));
            config.set("return_message.stay", oldConfig.getInt("return_message.stay"));
            config.set("return_message.fadeOut", oldConfig.getInt("return_message.fadeOut"));

            configFile.save();
            reloadConfig();
        }

        loadSettings();
    }

    static void loadSettings() {
        animations.clear();

        for (int id : TitleManager.getRunningAnimations())
            Bukkit.getScheduler().cancelTask(id);

        TitleManager.getRunningAnimations().clear();

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
                    } catch (NumberFormatException ignored) {}

                    frames.add(new AnimationFrame(frame, fadeIn, stay, fadeOut));
                }
            }
            animations.put(str.toUpperCase().trim(), new FrameSequence(frames));
        }

        usingConfig = getConfig().getBoolean("usingConfig");
        tabmenuEnabled = getConfig().getBoolean("tabmenu.enabled");
        welcomeMessageEnabled = getConfig().getBoolean("welcome_message.enabled");
        returnMessageEnabled = getConfig().getBoolean("return_message.enabled");

        if (tabmenuEnabled) {
            String headerString = getConfig().getString("tabmenu.header");
            String footerString = getConfig().getString("tabmenu.footer");
            if (headerString.toLowerCase().startsWith("animation:") || footerString.toLowerCase().startsWith("animation:")) {
                Object header;
                Object footer;

                if (headerString.toLowerCase().startsWith("animation:"))
                    header = getAnimation(headerString.substring("animation:".length()));
                else header = ChatColor.translateAlternateColorCodes('&', headerString.replace("\\n", "\n"));
                if (footerString.toLowerCase().startsWith("animation:"))
                    footer = getAnimation(headerString.substring("animation:".length()));
                else footer = ChatColor.translateAlternateColorCodes('&', footerString.replace("\\n", "\n"));

                tabTitleObject = new TabTitleAnimation(header == null ? "" : header, footer == null ? "" : footer);
                ((TabTitleAnimation) tabTitleObject).broadcast();
            } else {
                tabTitleObject = new TabTitleObject(ChatColor.translateAlternateColorCodes('&', headerString.replace("\\n", "\n")), ChatColor.translateAlternateColorCodes('&', footerString.replace("\\n", "\n")));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    TabTitleObject tempObject = (TabTitleObject) tabTitleObject;
                    tempObject.setHeader(TextConverter.setVariables(player, tempObject.getHeader()));
                    tempObject.setFooter(TextConverter.setVariables(player, tempObject.getFooter()));
                    tempObject.send(player);
                }
            }
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
                ((TitleAnimation) welcomeObject).broadcast();//Wondering if this should broadcast or not...
            } else {
                welcomeObject = new TitleObject(ChatColor.translateAlternateColorCodes('&', getConfig().getString("welcome_message.title")), ChatColor.translateAlternateColorCodes('&', getConfig().getString("welcome_message.subtitle")))
                        .setFadeIn(getConfig().getInt("welcome_message.fadeIn")).setStay(getConfig().getInt("welcome_message.stay")).setFadeOut(getConfig().getInt("welcome_message.fadeOut"));
            }
        }
        if (returnMessageEnabled) {
            String titleString = getConfig().getString("return_message.title");
            String subtitleString = getConfig().getString("return_message.subtitle");
            if (titleString.toLowerCase().startsWith("animation:") || subtitleString.toLowerCase().startsWith("animation:")) {
                Object title;
                Object subtitle;

                if (titleString.toLowerCase().startsWith("animation:"))
                    title = getAnimation(titleString.substring("animation:".length()));
                else title = ChatColor.translateAlternateColorCodes('&', titleString.replace("\\n", "\n"));
                if (subtitleString.toLowerCase().startsWith("animation:"))
                    subtitle = getAnimation(subtitleString.substring("animation:".length()));
                else subtitle = ChatColor.translateAlternateColorCodes('&', subtitleString.replace("\\n", "\n"));

                returnMsgObject = new TitleAnimation(title, subtitle);
                ((TitleAnimation) welcomeObject).broadcast();//Again, broadcast?
            } else {
                returnMsgObject = new TitleObject(ChatColor.translateAlternateColorCodes('&', getConfig().getString("return_message.title")), ChatColor.translateAlternateColorCodes('&', getConfig().getString("return_message.subtitle")))
                        .setFadeIn(getConfig().getInt("return_message.fadeIn")).setStay(getConfig().getInt("return_message.stay")).setFadeOut(getConfig().getInt("return_message.fadeOut"));
            }
        }
    }

    public static void reloadConfig() {
        configFile.load();
        animationConfigFile.load();
        usersList.load();
        loadSettings();
    }

    public static FrameSequence getAnimation(String animation) {
        return animations.get(animation.toUpperCase().trim());
    }
    
    public static ConfigFile getUsersList() {
        return usersList;
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

    public static Object getWelcomeObject() {
        return welcomeObject;
    }
    
    public static Object getReturnMsgObject() {
        return returnMsgObject;
    }

    public static Object getTabTitleObject() {
        return tabTitleObject;
    }

    public static boolean isTabmenuEnabled() {
        return tabmenuEnabled;
    }

    public static boolean isWelcomeMessageEnabled() {
        return welcomeMessageEnabled;
    }
    
    public static boolean isReturnMessageEnabled() {
        return returnMessageEnabled;
    }
}
