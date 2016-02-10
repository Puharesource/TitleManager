package io.puharesource.mc.sponge.titlemanager;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.sponge.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.sponge.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.sponge.titlemanager.api.iface.ITabObject;
import io.puharesource.mc.sponge.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.sponge.titlemanager.api.iface.Script;
import io.puharesource.mc.sponge.titlemanager.api.scripts.LuaScript;
import lombok.Getter;
import lombok.val;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

public final class ConfigHandler {
    @Inject private TitleManager plugin;
    @Inject private Logger logger;
    @Inject @DefaultConfig(sharedRoot = false) private Path mainConfigPath;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;

    private final Path animationsConfigPath = new File(configDir.toFile(), "animations").toPath();


    private final ConfigurationLoader<CommentedConfigurationNode> mainLoader = HoconConfigurationLoader.builder().setPath(mainConfigPath).build();
    private final ConfigurationLoader<CommentedConfigurationNode> animationsLoader = HoconConfigurationLoader.builder().setPath(animationsConfigPath).build();

    private final ConfigurationNode mainRootNode = mainLoader.createEmptyNode(ConfigurationOptions.defaults());
    private final ConfigurationNode animationsConfig = animationsLoader.createEmptyNode(ConfigurationOptions.defaults());

    private Map<String, FrameSequence> animations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, Script> scripts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private File scriptDir;

    @Getter private ITabObject tabTitleObject;
    private Object welcomeObject;
    private Object firstWelcomeObject;
    private Object actionbarWelcomeObject;
    private Object actionbarFirstWelcomeObject;
    private Object worldObject;
    private Object worldActionbarObject;

    public void reload() {
        logger.debug("Clearing old config data.");
        plugin.getRunningAnimations().forEach(i -> plugin.removeRunningAnimationId(i));
        plugin.getEngine().cancelAll();
        logger.debug("Finished clearing of old config data.");

        logger.debug("Loading main configuration file.");
        try {
            mainLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load main config!", e);
        }
        logger.debug("Finished loading main configuration file.");

        logger.debug("Loading animations configuration file.");
        try {
            animationsLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load animation config!", e);
        }
        logger.debug("Finished loading animations configuration file.");

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

        Arrays.stream(scriptDir.listFiles())
                .filter(File::isFile)
                .filter(file -> file.getName().matches("(.*)(?i).lua"))
                .forEach(file -> {
                    try {
                        final Globals globals = JsePlatform.standardGlobals();
                        globals.get("dofile").call(LuaValue.valueOf(file.getPath()));
                        globals.get("tm_load").invoke();

                        final LuaScript script = new LuaScript(globals);
                        logger.info("Loaded script: " + script.getName() + " v" + script.getVersion() + " by: " + script.getAuthor());
                        scripts.put(script.getName(), script);
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to load " + file, e);
                    }
                });

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

    public Optional<Script> getScript(final String script) {
        return Optional.ofNullable(scripts.get(script));
    }

    public Map<String, Script> getScripts() {
        return ImmutableMap.copyOf(scripts);
    }

    public Optional<FrameSequence> getAnimation(final String animationName) {
        return Optional.ofNullable(animations.get(animationName));
    }

    public Map<String, FrameSequence> getAnimations() {
        return ImmutableMap.copyOf(animations);
    }
}
