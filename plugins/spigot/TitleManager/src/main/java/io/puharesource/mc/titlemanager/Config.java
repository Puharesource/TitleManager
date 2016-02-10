package io.puharesource.mc.titlemanager;

import com.google.common.collect.ImmutableMap;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.animations.ActionbarTitleAnimation;
import io.puharesource.mc.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.titlemanager.api.animations.TitleAnimation;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.ITabObject;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.api.iface.Script;
import io.puharesource.mc.titlemanager.api.scripts.LuaScript;
import io.puharesource.mc.titlemanager.backend.config.ConfigFile;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import io.puharesource.mc.titlemanager.backend.config.ConfigSerializer;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import lombok.Getter;
import lombok.val;
import org.bukkit.configuration.ConfigurationSection;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class Config {

    private ConfigFile configFile, animationConfigFile;
    private @Getter ConfigFile messagesConfigFile;
    private Map<String, FrameSequence> animations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, Script> scripts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private File scriptDir;

    private @Getter ITabObject tabTitleObject;
    private Object welcomeObject;
    private Object firstWelcomeObject;
    private Object actionbarWelcomeObject;
    private Object actionbarFirstWelcomeObject;
    private Object worldObject;
    private Object worldActionbarObject;

    private @Getter ConfigMain config;

    public void load() {
        val plugin = TitleManager.getInstance();

        configFile = new ConfigFile(plugin, plugin.getDataFolder(), "config", false);
        animationConfigFile = new ConfigFile(plugin, plugin.getDataFolder(), "animations", true);

        scriptDir = new File(plugin.getDataFolder(), "scripts");
        scriptDir.mkdir();

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
        val plugin = TitleManager.getInstance();

        for (int i = 0; TitleManager.getRunningAnimations().size() > i; i++) {
            int id = TitleManager.getRunningAnimations().get(i);
            TitleManager.removeRunningAnimationId(id);
        }

        plugin.getEngine().cancelAll();

        configFile.reload();

        try {
            ConfigSerializer.saveDefaults(ConfigMain.class, configFile.getFile(), false);
            config = ConfigSerializer.deserialize(ConfigMain.class, configFile.getFile());
        } catch (IllegalAccessException | InvocationTargetException | IOException | InstantiationException e) {
            e.printStackTrace();
        }


        final File locale = new File(plugin.getDataFolder(), config.locale + ".yml");
        final InputStream stream = plugin.getResource(config.locale + ".yml");
        if (locale.exists()) {
            messagesConfigFile = new ConfigFile(plugin, plugin.getDataFolder(), config.locale, true);
        } else if (stream != null) {
            messagesConfigFile = new ConfigFile(stream);
        } else {
            messagesConfigFile = new ConfigFile(plugin, plugin.getDataFolder(), "en_US", true);
        }

        animationConfigFile.reload();
        animations.clear();

        for (String animationName : animationConfigFile.getConfig().getKeys(false)) {
            ConfigurationSection section = animationConfigFile.getConfig().getConfigurationSection(animationName);
            List<AnimationFrame> frames = new ArrayList<>();
            for (String frame : section.getStringList("frames")) {
                frames.add(MiscellaneousUtils.getFrameFromString(frame));
            }

            animations.put(animationName, new FrameSequence(frames));
        }

        for (val file : scriptDir.listFiles()) {
            if (!file.isDirectory() && file.getName().matches("(.*)(?i).lua")) {
                try {
                    val globals = JsePlatform.standardGlobals();
                    globals.get("dofile").call(LuaValue.valueOf(file.getPath()));
                    globals.get("tm_load").invoke();

                    val script = new LuaScript(globals);
                    TitleManager.getInstance().getLogger().info("Loaded script: " + script.getName() + " v" + script.getVersion() + " by: " + script.getAuthor());
                    scripts.put(script.getName(), script);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to load " + file, e);
                }
            }
        }

        if (!config.usingConfig) return;

        if (config.tabmenuEnabled) {
            tabTitleObject = MiscellaneousUtils.generateTabObject(config.tabmenuHeader, config.tabmenuFooter);
            tabTitleObject.broadcast();
        }

        if (config.welcomeMessageEnabled) {
            if (config.welcomeMessageTitle instanceof String) {
                welcomeObject = MiscellaneousUtils.generateTitleObject((String) config.welcomeMessageTitle, config.welcomeMessageSubtitle,
                        config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);
            } else if (config.welcomeMessageTitle instanceof List) {
                if (config.welcomeMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();
                    final List<AnimationFrame> subtitleFrames = new ArrayList<>();

                    for (String title : (List<String>) config.welcomeMessageTitle) {
                        val titles = MiscellaneousUtils.splitString(title);

                        titleFrames.add(new AnimationFrame(titles[0], config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));
                        subtitleFrames.add(new AnimationFrame(titles[1], config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));

                        welcomeObject = new TitleAnimation(new FrameSequence(titleFrames), new FrameSequence(subtitleFrames));
                    }
                } else {
                    welcomeObject = new ArrayList<ITitleObject>();

                    for (String title : (List<String>) config.welcomeMessageTitle) {
                        val titles = MiscellaneousUtils.splitString(title);
                        ((List<ITitleObject>) welcomeObject).add(new TitleObject(titles[0], titles[1]).setFadeIn(config.welcomeMessageFadeIn).setStay(config.welcomeMessageStay).setFadeOut(config.welcomeMessageFadeOut));
                    }
                }
            }

            if (config.firstJoinTitle instanceof String) {
                firstWelcomeObject = MiscellaneousUtils.generateTitleObject((String) config.firstJoinTitle, config.firstJoinSubtitle,
                        config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);
            } else if (config.firstJoinTitle instanceof List) {
                if (config.welcomeMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();
                    final List<AnimationFrame> subtitleFrames = new ArrayList<>();

                    for (String title : (List<String>) config.firstJoinTitle) {
                        val titles = MiscellaneousUtils.splitString(title);

                        titleFrames.add(new AnimationFrame(titles[0], config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));
                        subtitleFrames.add(new AnimationFrame(titles[1], config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));

                        firstWelcomeObject = new TitleAnimation(new FrameSequence(titleFrames), new FrameSequence(subtitleFrames));
                    }
                } else {
                    firstWelcomeObject = new ArrayList<ITitleObject>();

                    for (String title : (List<String>) config.firstJoinTitle) {
                        val titles = MiscellaneousUtils.splitString(title);
                        ((List<ITitleObject>) firstWelcomeObject).add(new TitleObject(titles[0], titles[1]).setFadeIn(config.welcomeMessageFadeIn).setStay(config.welcomeMessageStay).setFadeOut(config.welcomeMessageFadeOut));
                    }
                }
            }
        }

        if (config.actionbarWelcomeEnabled) {
            if (config.actionbarWelcomeMessage instanceof String) {
                actionbarWelcomeObject = MiscellaneousUtils.generateActionbarObject((String) config.actionbarWelcomeMessage);
            } else if (config.welcomeMessageTitle instanceof List) {
                if (config.welcomeMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();

                    for (String title : (List<String>) config.actionbarWelcomeMessage) {
                        titleFrames.add(new AnimationFrame(title, config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));

                        actionbarWelcomeObject = new ActionbarTitleAnimation(new FrameSequence(titleFrames));
                    }
                } else {
                    actionbarWelcomeObject = new ArrayList<ITitleObject>();

                    for (String title : (List<String>) config.actionbarWelcomeMessage) {
                        val titles = MiscellaneousUtils.splitString(title);
                        ((List<ITitleObject>) welcomeObject).add(new TitleObject(titles[0], titles[1]).setFadeIn(config.welcomeMessageFadeIn).setStay(config.welcomeMessageStay).setFadeOut(config.welcomeMessageFadeOut));
                    }
                }
            }

            if (config.actionbarFirstWelcomeMessage instanceof String) {
                firstWelcomeObject = MiscellaneousUtils.generateTitleObject((String) config.actionbarFirstWelcomeMessage, config.firstJoinSubtitle,
                        config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);
            } else if (config.actionbarFirstWelcomeMessage instanceof List) {
                if (config.welcomeMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();
                    final List<AnimationFrame> subtitleFrames = new ArrayList<>();

                    for (String title : (List<String>) config.actionbarFirstWelcomeMessage) {
                        val titles = MiscellaneousUtils.splitString(title);

                        titleFrames.add(new AnimationFrame(titles[0], config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));
                        subtitleFrames.add(new AnimationFrame(titles[1], config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));

                        actionbarFirstWelcomeObject = new TitleAnimation(new FrameSequence(titleFrames), new FrameSequence(subtitleFrames));
                    }
                } else {
                    actionbarFirstWelcomeObject = new ArrayList<ITitleObject>();

                    for (String title : (List<String>) config.actionbarFirstWelcomeMessage) {
                        val titles = MiscellaneousUtils.splitString(title);
                        ((List<ITitleObject>) actionbarFirstWelcomeObject).add(new TitleObject(titles[0], titles[1]).setFadeIn(config.welcomeMessageFadeIn).setStay(config.welcomeMessageStay).setFadeOut(config.welcomeMessageFadeOut));
                    }
                }
            }
        }



        for (int i = 0; config.disabledVariables.size() > i; i++) {
            config.disabledVariables.set(i, config.disabledVariables.get(i).toLowerCase());
        }

        if (config.worldMessageEnabled) {
            if (config.worldMessageTitle instanceof String) {
                worldObject = MiscellaneousUtils.generateTitleObject((String) config.worldMessageTitle, config.worldMessageSubtitle,
                        config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut);
            } else if (config.welcomeMessageTitle instanceof List) {
                if (config.worldMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();
                    final List<AnimationFrame> subtitleFrames = new ArrayList<>();

                    for (String title : (List<String>) config.worldMessageTitle) {
                        val titles = MiscellaneousUtils.splitString(title);

                        titleFrames.add(new AnimationFrame(titles[0], config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut));
                        subtitleFrames.add(new AnimationFrame(titles[1], config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut));

                        welcomeObject = new TitleAnimation(new FrameSequence(titleFrames), new FrameSequence(subtitleFrames));
                    }
                } else {
                    welcomeObject = new ArrayList<ITitleObject>();

                    for (String title : (List<String>) config.worldMessageTitle) {
                        val titles = MiscellaneousUtils.splitString(title);
                        ((List<ITitleObject>) worldObject).add(new TitleObject(titles[0], titles[1]).setFadeIn(config.worldMessageFadeIn).setStay(config.worldMessageStay).setFadeOut(config.worldMessageFadeOut));
                    }
                }
            }

            if (config.worldMessageActionBar instanceof String) {
                worldActionbarObject = MiscellaneousUtils.generateActionbarObject((String) config.worldMessageActionBar);
            } else if (config.worldMessageActionBar instanceof List) {
                if (config.worldMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();

                    for (String title : (List<String>) config.worldMessageActionBar) {
                        titleFrames.add(new AnimationFrame(title, config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut));

                        welcomeObject = new ActionbarTitleAnimation(new FrameSequence(titleFrames));
                    }
                } else {
                    welcomeObject = new ArrayList<ITitleObject>();

                    for (String title : (List<String>) config.worldMessageActionBar) {
                        val titles = MiscellaneousUtils.splitString(title);
                        ((List<ITitleObject>) worldActionbarObject).add(new TitleObject(titles[0], titles[1]).setFadeIn(config.worldMessageFadeIn).setStay(config.welcomeMessageStay).setFadeOut(config.worldMessageFadeOut));
                    }
                }
            }
        }
    }

    public ITitleObject getTitleWelcomeMessage(final boolean isFirstLogin) {
        if (isFirstLogin) {
            if (firstWelcomeObject instanceof ITitleObject) {
                return (ITitleObject) firstWelcomeObject;
            } else {
                final List<ITitleObject> titles = (List<ITitleObject>) firstWelcomeObject;
                return titles.get(new Random().nextInt(titles.size()));
            }
        } else {
            if (welcomeObject instanceof ITitleObject) {
                return (ITitleObject) welcomeObject;
            } else {
                final List<ITitleObject> titles = (List<ITitleObject>) welcomeObject;
                return titles.get(new Random().nextInt(titles.size()));
            }
        }
    }

    public IActionbarObject getActionbarWelcomeMessage(final boolean isFirstLogin) {
        if (isFirstLogin) {
            if (actionbarFirstWelcomeObject instanceof IActionbarObject) {
                return (IActionbarObject) actionbarFirstWelcomeObject;
            } else {
                final List<IActionbarObject> titles = (List<IActionbarObject>) actionbarFirstWelcomeObject;
                return titles.get(new Random().nextInt(titles.size()));
            }
        } else {
            if (actionbarWelcomeObject instanceof IActionbarObject) {
                return (IActionbarObject) actionbarWelcomeObject;
            } else {
                final List<IActionbarObject> titles = (List<IActionbarObject>) actionbarWelcomeObject;
                return titles.get(new Random().nextInt(titles.size()));
            }
        }
    }

    public ITitleObject getWorldTitleMessage() {
        if (worldObject instanceof ITitleObject) {
            return (ITitleObject) worldObject;
        } else {
            final List<ITitleObject> titles = (List<ITitleObject>) worldObject;
            return titles.get(new Random().nextInt(titles.size()));
        }
    }

    public IActionbarObject getWorldActionbarTitleMessage() {
        if (worldActionbarObject instanceof IActionbarObject) {
            return (IActionbarObject) worldActionbarObject;
        } else {
            final List<IActionbarObject> titles = (List<IActionbarObject>) worldActionbarObject;
            return titles.get(new Random().nextInt(titles.size()));
        }
    }

    public Script getScript(final String script) {
        return script == null ? null : scripts.get(script);
    }

    public Map<String, Script> getScripts() {
        return ImmutableMap.copyOf(scripts);
    }

    public static FrameSequence getAnimation(final String animation) {
        return animation == null ? null : TitleManager.getInstance().getConfigManager().animations.get(animation);
    }

    public static Map<String, FrameSequence> getAnimations() {
        return ImmutableMap.copyOf(TitleManager.getInstance().getConfigManager().animations);
    }
}
