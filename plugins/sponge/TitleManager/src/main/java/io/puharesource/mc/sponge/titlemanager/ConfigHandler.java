package io.puharesource.mc.sponge.titlemanager;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.api.Sendables;
import io.puharesource.mc.sponge.titlemanager.api.TitleObject;
import io.puharesource.mc.sponge.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.sponge.titlemanager.api.animations.AnimationToken;
import io.puharesource.mc.sponge.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.sponge.titlemanager.api.animations.TitleAnimation;
import io.puharesource.mc.sponge.titlemanager.api.iface.ActionbarSendable;
import io.puharesource.mc.sponge.titlemanager.api.iface.Script;
import io.puharesource.mc.sponge.titlemanager.api.iface.TabListSendable;
import io.puharesource.mc.sponge.titlemanager.api.iface.TitleSendable;
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
import java.util.concurrent.ThreadLocalRandom;

import static io.puharesource.mc.sponge.titlemanager.MiscellaneousUtils.*;

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

    @Getter private TabListSendable tabTitleObject;
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

        for (final String animationName : animationConfigFile.getConfig().getKeys(false)) {
            ConfigurationSection section = animationConfigFile.getConfig().getConfigurationSection(animationName);
            List<AnimationFrame> frames = new ArrayList<>();
            for (String frame : section.getStringList("frames")) {
                frames.add(getFrameFromString(frame));
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
            tabTitleObject = generateTabObject(config.tabmenuHeader, config.tabmenuFooter);
            tabTitleObject.broadcast();
        }

        if (config.welcomeMessageEnabled) {
            if (config.welcomeMessageTitle instanceof String) {
                welcomeObject = generateTitleObject((String) config.welcomeMessageTitle, config.welcomeMessageSubtitle,
                        config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);
            } else if (config.welcomeMessageTitle instanceof List) {
                if (config.welcomeMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();
                    final List<AnimationFrame> subtitleFrames = new ArrayList<>();

                    for (final String title : (List<String>) config.welcomeMessageTitle) {
                        final String[] titles = splitString(title);

                        titleFrames.add(new AnimationFrame(titles[0], config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));
                        subtitleFrames.add(new AnimationFrame(titles[1], config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));

                        welcomeObject = Sendables.title(AnimationToken.of(new FrameSequence(titleFrames)), AnimationToken.of(new FrameSequence(subtitleFrames)));
                    }
                } else {
                    welcomeObject = new ArrayList<TitleSendable>();

                    for (final String title : (List<String>) config.welcomeMessageTitle) {
                        final String[] titles = splitString(title);
                        ((List<TitleSendable>) welcomeObject).add(Sendables.title(format(titles[0]), format(titles[1])).setFadeIn(config.welcomeMessageFadeIn).setStay(config.welcomeMessageStay).setFadeOut(config.welcomeMessageFadeOut));
                    }
                }
            }

            if (config.firstJoinTitle instanceof String) {
                firstWelcomeObject = generateTitleObject((String) config.firstJoinTitle, config.firstJoinSubtitle,
                        config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);
            } else if (config.firstJoinTitle instanceof List) {
                if (config.welcomeMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();
                    final List<AnimationFrame> subtitleFrames = new ArrayList<>();

                    for (final String title : (List<String>) config.firstJoinTitle) {
                        final String[] titles = splitString(title);

                        titleFrames.add(new AnimationFrame(titles[0], config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));
                        subtitleFrames.add(new AnimationFrame(titles[1], config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));

                        firstWelcomeObject = Sendables.title(AnimationToken.of(new FrameSequence(titleFrames)), AnimationToken.of(new FrameSequence(subtitleFrames)));
                    }
                } else {
                    firstWelcomeObject = new ArrayList<TitleSendable>();

                    for (final String title : (List<String>) config.firstJoinTitle) {
                        final String[] titles = splitString(title);
                        ((List<TitleSendable>) firstWelcomeObject).add(Sendables.title(format(titles[0]), format(titles[1])).setFadeIn(config.welcomeMessageFadeIn).setStay(config.welcomeMessageStay).setFadeOut(config.welcomeMessageFadeOut));
                    }
                }
            }
        }

        if (config.actionbarWelcomeEnabled) {
            if (config.actionbarWelcomeMessage instanceof String) {
                actionbarWelcomeObject = generateActionbarObject((String) config.actionbarWelcomeMessage);
            } else if (config.welcomeMessageTitle instanceof List) {
                if (config.welcomeMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();

                    for (final String title : (List<String>) config.actionbarWelcomeMessage) {
                        titleFrames.add(new AnimationFrame(title, config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));

                        actionbarWelcomeObject = new ActionbarTitleAnimation(new FrameSequence(titleFrames));
                    }
                } else {
                    actionbarWelcomeObject = new ArrayList<TitleSendable>();

                    for (final String title : (List<String>) config.actionbarWelcomeMessage) {
                        final String[] titles = splitString(title);
                        ((List<TitleSendable>) welcomeObject).add(Sendables.title(format(titles[0]), format(titles[1])).setFadeIn(config.welcomeMessageFadeIn).setStay(config.welcomeMessageStay).setFadeOut(config.welcomeMessageFadeOut));
                    }
                }
            }

            if (config.actionbarFirstWelcomeMessage instanceof String) {
                firstWelcomeObject = generateTitleObject((String) config.actionbarFirstWelcomeMessage, config.firstJoinSubtitle,
                        config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);
            } else if (config.actionbarFirstWelcomeMessage instanceof List) {
                if (config.welcomeMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();
                    final List<AnimationFrame> subtitleFrames = new ArrayList<>();

                    for (final String title : (List<String>) config.actionbarFirstWelcomeMessage) {
                        final String[] titles = splitString(title);

                        titleFrames.add(new AnimationFrame(titles[0], config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));
                        subtitleFrames.add(new AnimationFrame(titles[1], config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));

                        actionbarFirstWelcomeObject = new TitleAnimation(AnimationToken.of(new FrameSequence(titleFrames)), AnimationToken.of(new FrameSequence(subtitleFrames)));
                    }
                } else {
                    actionbarFirstWelcomeObject = new ArrayList<TitleSendable>();

                    for (String title : (List<String>) config.actionbarFirstWelcomeMessage) {
                        final String[] titles = splitString(title);
                        ((List<TitleSendable>) actionbarFirstWelcomeObject).add(Sendables.title(format(titles[0]), format(titles[1])).setFadeIn(config.welcomeMessageFadeIn).setStay(config.welcomeMessageStay).setFadeOut(config.welcomeMessageFadeOut));
                    }
                }
            }
        }



        for (int i = 0; config.disabledVariables.size() > i; i++) {
            config.disabledVariables.set(i, config.disabledVariables.get(i).toLowerCase());
        }

        if (config.worldMessageEnabled) {
            if (config.worldMessageTitle instanceof String) {
                worldObject = generateTitleObject((String) config.worldMessageTitle, config.worldMessageSubtitle,
                        config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut);
            } else if (config.welcomeMessageTitle instanceof List) {
                if (config.worldMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();
                    final List<AnimationFrame> subtitleFrames = new ArrayList<>();

                    for (final String title : (List<String>) config.worldMessageTitle) {
                        final String[] titles = splitString(title);

                        titleFrames.add(new AnimationFrame(titles[0], config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut));
                        subtitleFrames.add(new AnimationFrame(titles[1], config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut));

                        welcomeObject = Sendables.title(AnimationToken.of(new FrameSequence(titleFrames)), AnimationToken.of(new FrameSequence(subtitleFrames)));
                    }
                } else {
                    welcomeObject = new ArrayList<TitleSendable>();

                    for (final String title : (List<String>) config.worldMessageTitle) {
                        final String[] titles = splitString(title);
                        ((List<TitleSendable>) worldObject).add(Sendables.title(format(titles[0]), format(titles[1])).setFadeIn(config.worldMessageFadeIn).setStay(config.worldMessageStay).setFadeOut(config.worldMessageFadeOut));
                    }
                }
            }

            if (config.worldMessageActionBar instanceof String) {
                worldActionbarObject = generateActionbarObject((String) config.worldMessageActionBar);
            } else if (config.worldMessageActionBar instanceof List) {
                if (config.worldMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();

                    for (final String title : (List<String>) config.worldMessageActionBar) {
                        titleFrames.add(new AnimationFrame(title, config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut));

                        welcomeObject = Sendables.actionbar(new FrameSequence(titleFrames));
                    }
                } else {
                    welcomeObject = new ArrayList<TitleSendable>();

                    for (final String title : (List<String>) config.worldMessageActionBar) {
                        final String[] titles = splitString(title);
                        ((List<TitleSendable>) worldActionbarObject).add(Sendables.title(format(titles[0]), format(titles[1])).setFadeIn(config.worldMessageFadeIn).setStay(config.welcomeMessageStay).setFadeOut(config.worldMessageFadeOut));
                    }
                }
            }
        }
    }

    public TitleSendable getTitleWelcomeMessage(final boolean isFirstLogin) {
        if (isFirstLogin) {
            if (firstWelcomeObject instanceof TitleSendable) {
                return (TitleSendable) firstWelcomeObject;
            } else {
                final List<TitleSendable> titles = (List<TitleSendable>) firstWelcomeObject;
                return titles.get(ThreadLocalRandom.current().nextInt(titles.size()));
            }
        } else {
            if (welcomeObject instanceof TitleSendable) {
                return (TitleSendable) welcomeObject;
            } else {
                final List<TitleSendable> titles = (List<TitleSendable>) welcomeObject;
                return titles.get(ThreadLocalRandom.current().nextInt(titles.size()));
            }
        }
    }

    public ActionbarSendable getActionbarWelcomeMessage(final boolean isFirstLogin) {
        if (isFirstLogin) {
            if (actionbarFirstWelcomeObject instanceof ActionbarSendable) {
                return (ActionbarSendable) actionbarFirstWelcomeObject;
            } else {
                final List<ActionbarSendable> titles = (List<ActionbarSendable>) actionbarFirstWelcomeObject;
                return titles.get(ThreadLocalRandom.current().nextInt(titles.size()));
            }
        } else {
            if (actionbarWelcomeObject instanceof ActionbarSendable) {
                return (ActionbarSendable) actionbarWelcomeObject;
            } else {
                final List<ActionbarSendable> titles = (List<ActionbarSendable>) actionbarWelcomeObject;
                return titles.get(ThreadLocalRandom.current().nextInt(titles.size()));
            }
        }
    }

    public TitleSendable getWorldTitleMessage() {
        if (worldObject instanceof TitleSendable) {
            return (TitleSendable) worldObject;
        } else {
            final List<TitleSendable> titles = (List<TitleSendable>) worldObject;
            return titles.get(ThreadLocalRandom.current().nextInt(titles.size()));
        }
    }

    public ActionbarSendable getWorldActionbarTitleMessage() {
        if (worldActionbarObject instanceof ActionbarSendable) {
            return (ActionbarSendable) worldActionbarObject;
        } else {
            final List<ActionbarSendable> titles = (List<ActionbarSendable>) worldActionbarObject;
            return titles.get(ThreadLocalRandom.current().nextInt(titles.size()));
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
