package io.puharesource.mc.sponge.titlemanager;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.api.Sendables;
import io.puharesource.mc.sponge.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.sponge.titlemanager.api.animations.AnimationToken;
import io.puharesource.mc.sponge.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.sponge.titlemanager.api.animations.TitleAnimation;
import io.puharesource.mc.sponge.titlemanager.api.iface.*;
import io.puharesource.mc.sponge.titlemanager.api.scripts.LuaScript;
import io.puharesource.mc.sponge.titlemanager.config.Config;
import io.puharesource.mc.sponge.titlemanager.config.ConfigFile;
import io.puharesource.mc.sponge.titlemanager.config.configs.ConfigAnimations;
import io.puharesource.mc.sponge.titlemanager.config.configs.ConfigMain;
import io.puharesource.mc.sponge.titlemanager.config.configs.ConfigMessages;
import lombok.Getter;
import ninja.leaping.configurate.ConfigurationNode;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static io.puharesource.mc.sponge.titlemanager.utils.MiscellaneousUtils.*;

public final class ConfigHandler {
    @Inject private TitleManager plugin;
    @Inject private Logger logger;
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;

    @Getter private ConfigFile<ConfigMain> mainConfig;
    @Getter private ConfigFile<ConfigMessages> messagesConfig;
    @Getter private ConfigFile<ConfigAnimations> animationsConfig;

    private Map<String, Script> scripts = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private File scriptDir;

    @Getter private TabListSendable tabTitleObject;
    private Object welcomeObject;
    private Object firstWelcomeObject;
    private Object actionbarWelcomeObject;
    private Object actionbarFirstWelcomeObject;
    private Object worldObject;
    private Object worldActionbarObject;

    public <T extends Config> ConfigFile<T> createConfig(final Class<T> clazz) {
        return createConfig(clazz, null);
    }

    public <T extends Config> ConfigFile<T> createConfig(final Class<T> clazz, final String embeddedResourceName) {
        final ConfigFile<T> config = new ConfigFile<>(clazz, embeddedResourceName);
        plugin.getInjector().injectMembers(config);

        return config;
    }

    public void load() {
        // Construct configs
        this.mainConfig = createConfig(ConfigMain.class);
        this.messagesConfig = createConfig(ConfigMessages.class);
        this.animationsConfig = createConfig(ConfigAnimations.class);

        // Loading configs
        mainConfig.load();
        messagesConfig.load();
        animationsConfig.load();

        // Create scripts dir
        scriptDir = new File(configDir.toFile(), "scripts");
        if (!scriptDir.exists()) scriptDir.mkdirs();

        // Load the scripts
        logger.debug("Loading scripts");
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
        logger.debug("Finished loading scripts.");

        final ConfigMain config = mainConfig.getConfig();

        // Don't load more values if the configuration file is not in use.

        if (!config.usingConfig) return;

        // If the tab list is enabled, then generate the tab list sendable and broadcast it to all online players.
        if (config.tablistEnabled) {
            tabTitleObject = createTabListSendable(format(config.tablistHeader), format(config.tablistFooter));
            tabTitleObject.broadcast();
        }

        // If welcome messages is enabled, generate it.
        if (config.welcomeMessageEnabled) {
            if (config.welcomeMessageTitle instanceof String) {
                welcomeObject = createTitleSendable(format((String) config.welcomeMessageTitle), format(config.welcomeMessageSubtitle),
                        config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);
            } else if (config.welcomeMessageTitle instanceof List) {
                if (config.welcomeMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();
                    final List<AnimationFrame> subtitleFrames = new ArrayList<>();

                    for (final String title : (List<String>) config.welcomeMessageTitle) {
                        final String[] titles = splitString(title);

                        titleFrames.add(new AnimationFrame(format(titles[0]), config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));
                        subtitleFrames.add(new AnimationFrame(format(titles[1]), config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));

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

            // If the first join title is enabled, generate it.
            if (config.firstJoinTitle instanceof String) {
                firstWelcomeObject = createTitleSendable(format((String) config.firstJoinTitle), format(config.firstJoinSubtitle),
                        config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);
            } else if (config.firstJoinTitle instanceof List) {
                if (config.welcomeMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();
                    final List<AnimationFrame> subtitleFrames = new ArrayList<>();

                    for (final String title : (List<String>) config.firstJoinTitle) {
                        final String[] titles = splitString(title);

                        titleFrames.add(new AnimationFrame(format(titles[0]), config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));
                        subtitleFrames.add(new AnimationFrame(format(titles[1]), config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));

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
                actionbarWelcomeObject = createActionbarSendable(format((String) config.actionbarWelcomeMessage));
            } else if (config.welcomeMessageTitle instanceof List) {
                if (config.welcomeMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();

                    for (final String title : (List<String>) config.actionbarWelcomeMessage) {
                        titleFrames.add(new AnimationFrame(format(title), config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));

                        actionbarWelcomeObject = Sendables.actionbar(new FrameSequence(titleFrames));
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
                firstWelcomeObject = createTitleSendable(format((String) config.actionbarFirstWelcomeMessage), format(config.firstJoinSubtitle),
                        config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut);
            } else if (config.actionbarFirstWelcomeMessage instanceof List) {
                if (config.welcomeMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();
                    final List<AnimationFrame> subtitleFrames = new ArrayList<>();

                    for (final String title : (List<String>) config.actionbarFirstWelcomeMessage) {
                        final String[] titles = splitString(title);

                        titleFrames.add(new AnimationFrame(format(titles[0]), config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));
                        subtitleFrames.add(new AnimationFrame(format(titles[0]), config.welcomeMessageFadeIn, config.welcomeMessageStay, config.welcomeMessageFadeOut));

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

        if (config.worldMessageEnabled) {
            if (config.worldMessageTitle instanceof String) {
                worldObject = createTitleSendable(format((String) config.worldMessageTitle), format(config.worldMessageSubtitle),
                        config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut);
            } else if (config.welcomeMessageTitle instanceof List) {
                if (config.worldMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();
                    final List<AnimationFrame> subtitleFrames = new ArrayList<>();

                    for (final String title : (List<String>) config.worldMessageTitle) {
                        final String[] titles = splitString(title);

                        titleFrames.add(new AnimationFrame(format(titles[0]), config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut));
                        subtitleFrames.add(new AnimationFrame(format(titles[1]), config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut));

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
                worldActionbarObject = createActionbarSendable(format((String) config.worldMessageActionBar));
            } else if (config.worldMessageActionBar instanceof List) {
                if (config.worldMessageMode.equalsIgnoreCase("SEQUENTIAL")) {
                    final List<AnimationFrame> titleFrames = new ArrayList<>();

                    for (final String title : (List<String>) config.worldMessageActionBar) {
                        titleFrames.add(new AnimationFrame(format(title), config.worldMessageFadeIn, config.worldMessageStay, config.worldMessageFadeOut));

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

    public void unload() {
        // Stop all running animations.
        logger.debug("Clearing old config data.");
        plugin.getRunningAnimations().forEach(i -> plugin.removeRunningAnimationId(i));
        plugin.getEngine().cancelAll();
        logger.debug("Finished clearing of old config data.");
    }

    public void reload() {
        unload();

        // Reload the configuration files.
        logger.debug("Loading main config.");
        mainConfig.reload();
        logger.debug("Finished loading main config.");

        logger.debug("Loading messages config.");
        messagesConfig.reload();
        logger.debug("Finished loading messages config.");

        logger.debug("Loading animations config.");
        animationsConfig.reload();
        logger.debug("Finished loading animations config.");

        load();
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

    public Optional<AnimationIterable> getAnimation(final String animationName) {
        return Optional.ofNullable(animationsConfig.getConfig().animations.get(animationName));
    }

    public Map<String, AnimationIterable> getAnimations() {
        return ImmutableMap.copyOf(animationsConfig.getConfig().animations);
    }

    public String getMessage(final String path, final String... args) {
        final ConfigurationNode node = messagesConfig
                .getRootNode()
                .getNode(path.split("\\."));
        return node.isVirtual() ? "" : String.format(node.getString(), args);
    }
}
