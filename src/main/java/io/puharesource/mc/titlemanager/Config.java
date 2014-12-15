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

    private static Object welcomeObject;
    private static Object firstWelcomeObject;
    private static Object tabTitleObject;

    private static ConfigFile configFile;
    private static ConfigFile animationConfigFile;
    private static ConfigFile commandsFile;
    
    private static Map<String, Object> commands = new Map<>();
    private static Map<String, FrameSequence> animations = new HashMap<>();

    public static void loadConfig() {
        Main plugin = TitleManager.getPlugin();

        configFile = new ConfigFile(plugin, plugin.getDataFolder(), "config", true);
        animationConfigFile = new ConfigFile(plugin, plugin.getDataFolder(), "animations", true);

        configFile.load();
        animationConfigFile.load();
        commandsFile.load();

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

            configFile.save();
        }

        //Updates the config from v1.2.1 to 1.3.0
        if (config.get("config-version") == null) {
            config.set("config-version", 1);
            ConfigurationSection section = config.createSection("welcome_message.first-join");
            section.set("title", config.getString("welcome_message.title"));
            section.set("subtitle", config.getString("welcome_message.subtitle"));
            config.set("welcome_message.first-join", section);

            configFile.save();
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
                    } catch (NumberFormatException ignored) {}

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
                else header = ChatColor.translateAlternateColorCodes('&', headerString.replace("\\n", "\n"));
                if (footerString.toLowerCase().startsWith("animation:"))
                    footer = getAnimation(footerString.substring("animation:".length()));
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
        
        if(commandsFile.getConfig().getBoolean("enabled"))
        {
            ConfigurationSection section = commandsFile.getConfig().getConfigurationSection("commands");
            ConfigurationSection sect;
            commands = section.getValues(false);
            for(String str : commands.keySet())
            {
                Object message;
                Object amessage;
                sect = section.getConfigurationSection(str);
                if(sect.getBoolean("title.enabled")
                {
                    if(sect.getString("title.title").contains("animation:"))
                    {
                        message = new TitleAnimation(getAnimation(sect.getString("title.title")),sect.getString("title.subtitle").contains("animation:")?getAnimation(sect.getString("title.subtitle")):sect.getString("title.subtitle"));
                    }
                    else message = new TitleObject(sect.getString("title.title"),sect.getString("title.subtitle"));
                }
                if(sect.getBoolean("actionbar.enabled"))
                {
                    if(sect.getString("actionbar.text").contains("animation:"))
                    {
                        amessage = new ActionbarTitleAnimation(getAnimation(sect.getString("actionbar.text")));
                    }
                    else amessage = new ActionbarTitleObject(sect.getString("actionbar.text"));
                }
                List<Object> msgs = new ArrayList<>();
                if(message!=null) msgs.add(message);
                if(amessage!=null) msgs.add(amessage);
                commands.put(str, msgs);
            }
        }
    }

    public static void reloadConfig() {
        configFile.load();
        animationConfigFile.load();
        commandsFile.load();
        
        animations.clear();

        for (int id : TitleManager.getRunningAnimations())
            Bukkit.getScheduler().cancelTask(id);

        TitleManager.getRunningAnimations().clear();

        loadSettings();
    }

    public static Map<String, Object> getCommandTitles() {
        return commands;
    }
    
    @SuppressWarnings("unchecked")
    public static List<Object> getCommandTitle(String command)
    {
        String cmd = command.replace("/","").trim();
        if(commands.containsKey(cmd)) return (ArrayList<Object>) commands.get(cmd);
        return null;
    }

    public static FrameSequence getAnimation(String animation) {
        return animations.get(animation.toUpperCase().trim());
    }

    public static Map<String, FrameSequence> getAnimations() {
        return animations;
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

    public static Object getFirstWelcomeObject() {
        return firstWelcomeObject;
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
}
