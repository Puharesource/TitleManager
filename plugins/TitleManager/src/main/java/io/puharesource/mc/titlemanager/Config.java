package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.ITabObject;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.backend.config.ConfigFile;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import io.puharesource.mc.titlemanager.backend.config.ConfigSerializer;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Config {

    private ConfigFile configFile, animationConfigFile;
    private Map<String, FrameSequence> animations = new HashMap<>();
    private @Getter ITabObject tabTitleObject;
    private @Getter ITitleObject welcomeObject;
    private @Getter ITitleObject firstWelcomeObject;
    private @Getter IActionbarObject actionbarWelcomeObject;
    private @Getter IActionbarObject actionbarFirstWelcomeObject;
    private @Getter ITitleObject worldObject;
    private @Getter IActionbarObject worldActionbarObject;

    private @Getter ConfigMain config;

    public void load() {
        TitleManager plugin = TitleManager.getInstance();

        configFile = new ConfigFile(plugin, plugin.getDataFolder(), "config", false);
        animationConfigFile = new ConfigFile(plugin, plugin.getDataFolder(), "animations", true);

        configFile.reload();
        try {
            ConfigSerializer.saveDefaults(ConfigMain.class, configFile.getFile(), false);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | IOException e) {
            e.printStackTrace();
        }

        plugin.reloadConfig();
        reload();
    }

    public void reload() {
        for (int i = 0; TitleManager.getRunningAnimations().size() > i; i++) {
            int id = TitleManager.getRunningAnimations().get(i);
            TitleManager.getInstance().getEngine().cancelAll();
            TitleManager.removeRunningAnimationId(id);
        }

        configFile.reload();

        try {
            ConfigSerializer.saveDefaults(ConfigMain.class, configFile.getFile(), false);
            config = ConfigSerializer.deserialize(ConfigMain.class, configFile.getFile());
        } catch (IllegalAccessException | InvocationTargetException | IOException | InstantiationException e) {
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
            tabTitleObject = MiscellaneousUtils.generateTabObject(config.tabmenuHeader, config.tabmenuFooter);
            tabTitleObject.broadcast();
        }

        if (config.welcomeMessageEnabled) {
            welcomeObject = MiscellaneousUtils.generateTitleObject(config.welcomeMessageTitle, config.welcomeMessageSubtitle,
                    config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);


            firstWelcomeObject = MiscellaneousUtils.generateTitleObject(config.firstJoinTitle, config.firstJoinSubtitle,
                    config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);
        }

        if (config.actionbarWelcomeEnabled) {
            actionbarWelcomeObject = MiscellaneousUtils.generateActionbarObject(config.actionbarWelcomeMessage);
            actionbarFirstWelcomeObject = MiscellaneousUtils.generateActionbarObject(config.actionbarFirstWelcomeMessage);
        }

        for (int i = 0; config.disabledVariables.size() > i; i++) {
            config.disabledVariables.set(i, config.disabledVariables.get(i).trim().toLowerCase());
        }
        
        
        if (config.worldMessageEnabled) {
            worldObject = MiscellaneousUtils.generateTitleObject(config.worldMessageTitle, config.worldMessageSubtitle,
                    config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut);

            worldActionbarObject = MiscellaneousUtils.generateActionbarObject(config.worldMessageActionBar);
        }
    }

    public static FrameSequence getAnimation(String animation) {
        return animation == null ? null : getAnimations().get(animation.toUpperCase().trim());
    }

    public static Map<String, FrameSequence> getAnimations() {
        return TitleManager.getInstance().getConfigManager().animations;
    }
}
