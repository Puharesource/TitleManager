package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.ITabObject;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.backend.config.ConfigFile;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import io.puharesource.mc.titlemanager.backend.config.ConfigSerializer;
import io.puharesource.mc.titlemanager.backend.config.ConfigUpdater;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private ConfigFile configFile, animationConfigFile;
    private Map<String, FrameSequence> animations = new HashMap<>();
    private ITabObject tabmenu;
    private ITitleObject welcomeTitle, firstWelcomeTitle;
    private IActionbarObject actionbar, firstActionbar;

    private ConfigMain config;

    public void load() {
        TitleManager plugin = TitleManager.getInstance();

        configFile = new ConfigFile(plugin, plugin.getDataFolder(), "config", false);
        animationConfigFile = new ConfigFile(plugin, plugin.getDataFolder(), "animations", true);

        configFile.reload();
        try {
            ConfigSerializer.saveDefaults(ConfigMain.class, configFile.getFile(), false);
        } catch (IllegalAccessException | InvocationTargetException | IOException | InstantiationException e) {
            e.printStackTrace();
        }
        ConfigUpdater.update(plugin, configFile);

        plugin.reloadConfig();
        reload();
    }

    public void reload() {
        for (int i = 0; TitleManager.getRunningAnimations().size() > i; i++) {
            int id = TitleManager.getRunningAnimations().get(i);
            Bukkit.getScheduler().cancelTask(id);
            TitleManager.removeRunningAnimationId(id);
        }

        configFile.reload();

        try {
            ConfigSerializer.saveDefaults(ConfigMain.class, configFile.getFile(), false);
            config = ConfigSerializer.deserialize(ConfigMain.class, configFile.getFile());
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | IOException e) {
            e.printStackTrace();
        }

        animationConfigFile.reload();

        animations.clear();

        for (String str : animationConfigFile.getConfig().getKeys(false)) {
            ConfigurationSection section = animationConfigFile.getConfig().getConfigurationSection(str);
            List<AnimationFrame> frames = new ArrayList<>();
            for (String frame : section.getStringList("frames")) {
                frames.add(MiscellaneousUtils.getFrameFromString(frame));
            }

            animations.put(str.toUpperCase().trim(), new FrameSequence(frames));
        }

        if (!config.usingConfig) return;

        if (config.tabmenuEnabled) {
            tabmenu = MiscellaneousUtils.generateTabObject(config.tabmenuHeader, config.tabmenuFooter);
            tabmenu.broadcast();
        }

        if (config.welcomeMessageEnabled) {
            welcomeTitle = MiscellaneousUtils.generateTitleObject(config.welcomeMessageTitle, config.welcomeMessageSubtitle,
                    config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);


            firstWelcomeTitle = MiscellaneousUtils.generateTitleObject(config.firstJoinTitle, config.firstJoinSubtitle,
                    config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);
        }

        if (config.actionbarWelcomeEnabled) {
            actionbar = MiscellaneousUtils.generateActionbarObject(config.actionbarWelcomeMessage);
            firstActionbar = MiscellaneousUtils.generateActionbarObject(config.actionbarFirstWelcomeMessage);
        }
    }

    public ConfigMain getConfig() {
        return config;
    }

    public static FrameSequence getAnimation(String animation) {
        return animation == null ? null : getAnimations().get(animation.toUpperCase().trim());
    }

    public static Map<String, FrameSequence> getAnimations() {
        return TitleManager.getInstance().getConfigManager().animations;
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

    public IActionbarObject getActionbarWelcomeObject() {
        return actionbar;
    }

    public IActionbarObject getActionbarFirstWelcomeObject() {
        return firstActionbar;
    }
}
