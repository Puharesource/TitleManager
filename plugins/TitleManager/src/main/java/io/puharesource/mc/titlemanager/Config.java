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
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class Config {

    private ConfigFile configFile;
    private ConfigFile animationConfigFile;
    private Map<String, FrameSequence> animations = new HashMap<>();
    private ITabObject tabmenu;
    private ITitleObject welcomeTitle;
    private ITitleObject firstWelcomeTitle;

    public Config() {
        TitleManager plugin = TitleManager.getInstance();

        configFile = new ConfigFile(plugin, plugin.getDataFolder(), "config", true);
        animationConfigFile = new ConfigFile(plugin, plugin.getDataFolder(), "animations", true);

        ConfigUpdater.update(plugin, configFile);

        plugin.reloadConfig();
        reload();
    }

    public void reload() {
        configFile.reload();
        animationConfigFile.reload();

        animations.clear();

        for (String str : animationConfigFile.getConfig().getKeys(false)) {
            ConfigurationSection section = animationConfigFile.getConfig().getConfigurationSection(str);
            List<AnimationFrame> frames = new ArrayList<>();
            for (String frame : section.getStringList("frames")) {
                int fadeIn = -1;
                int stay = -1;
                int fadeOut = -1;

                frame = MiscellaneousUtils.format(frame);
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


        if (!isUsingConfig()) return;


        if (isTabmenuEnabled()) {
            Object headerString = getConfig().getString("tabmenu.header");
            Object footerString = getConfig().getString("tabmenu.footer");

            final FrameSequence string = MiscellaneousUtils.isValidAnimationString(headerString);
            Object header = string == null ? null : MiscellaneousUtils.format(headerString).replace("\\n", "\n");
            final FrameSequence string1 = MiscellaneousUtils.isValidAnimationString(footerString);
            Object footer = string1 == null ? null : MiscellaneousUtils.format(footerString).replace("\\n", "\n");

            if (header instanceof FrameSequence || footer instanceof FrameSequence) {
                tabmenu = new TabTitleAnimation(header, footer);
            } else if (header instanceof String && footer instanceof String) {
                tabmenu = new TabTitleAnimation(new FrameSequence(Arrays.asList(new AnimationFrame((String) header, 0, 5, 0))), new FrameSequence(Arrays.asList(new AnimationFrame((String) footer, 0, 5, 0))));
            }

            tabmenu.broadcast();
        }


        if (isWelcomeMessageEnabled()) {
            ConfigurationSection section = getConfig().getConfigurationSection("welcome_message");

            String titleString = section.getString("title");
            String subtitleString = section.getString("subtitle");

            int fadeIn = section.getInt("fadeIn");
            int stay = section.getInt("stay");
            int fadeOut = section.getInt("fadeOut");

            final FrameSequence string = MiscellaneousUtils.isValidAnimationString(titleString);
            Object title = string != null ? string : MiscellaneousUtils.format(titleString);
            final FrameSequence string1 = MiscellaneousUtils.isValidAnimationString(subtitleString);
            Object subtitle = string1 != null ? string1 : MiscellaneousUtils.format(subtitleString);

            if (title instanceof FrameSequence || subtitle instanceof FrameSequence) {
                welcomeTitle = new TitleAnimation(title, subtitle);
            } else if (title instanceof String && subtitle instanceof String) {
                welcomeTitle = new TitleObject((String) title, (String) subtitle).setFadeIn(fadeIn).setStay(stay).setFadeOut(fadeOut);
            }


            titleString = section.getString("first-join.title");
            subtitleString = section.getString("first-join.subtitle");

            final FrameSequence string2 = MiscellaneousUtils.isValidAnimationString(titleString);
            title = DefaultGroovyMethods.asBoolean(string2) ? string2 : MiscellaneousUtils.format(titleString);
            final FrameSequence string3 = MiscellaneousUtils.isValidAnimationString(subtitleString);
            subtitle = DefaultGroovyMethods.asBoolean(string3) ? string3 : MiscellaneousUtils.format(subtitleString);

            if (title instanceof FrameSequence || subtitle instanceof FrameSequence) {
                firstWelcomeTitle = new TitleAnimation(title, subtitle);
            } else if (title instanceof String && subtitle instanceof String) {
                firstWelcomeTitle = new TitleObject((String) title, (String) subtitle).setFadeIn(fadeIn).setStay(stay).setFadeOut(fadeOut);
            }

        }

    }

    public FileConfiguration getConfig() {
        return configFile.getConfig();
    }

    public static FrameSequence getAnimation(String animation) {
        return TitleManager.getInstance().getConfigManager().animations.get(animation.toUpperCase().trim());
    }

    public static Map<String, FrameSequence> getAnimations() {
        return TitleManager.getInstance().getConfigManager().animations;
    }

    public boolean isUsingConfig() {
        return getConfig().getBoolean("usingConfig");
    }

    public boolean isTabmenuEnabled() {
        return getConfig().getBoolean("tabmenu.enabled");
    }

    public boolean isWelcomeMessageEnabled() {
        return getConfig().getBoolean("welcome_message.enabled");
    }

    public boolean isNumberFormatEnabled() {
        return getConfig().getBoolean("number-format.enabled");
    }

    public ITitleObject getWelcomeObject() {
        return welcomeTitle;
    }

    public ITitleObject getFirstWelcomeObject() {
        return firstWelcomeTitle;
    }

    public ITabObject getTabTitleObject() {
        return tabmenu;
    }

    public String getNumberFormat() {
        return configFile.getConfig().getString("number-format.format");
    }
}
