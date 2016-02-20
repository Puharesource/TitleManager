package io.puharesource.mc.sponge.titlemanager.config;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import lombok.Getter;
import lombok.SneakyThrows;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.config.ConfigDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class ConfigFile<T extends Config> {
    @Inject @ConfigDir(sharedRoot = false) private Path path;
    @Inject private TitleManager plugin;
    @Inject private Logger logger;

    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private Optional<String> embeddedResourceName;

    @Getter private File file;
    @Getter private Class<T> clazz;
    @Getter private T config;
    @Getter private ConfigurationNode rootNode;

    public ConfigFile(final Class<T> clazz) {
        this(clazz, null);
    }

    public ConfigFile(final Class<T> clazz, final String embeddedResourceName) {
        Validate.notNull(clazz);

        this.clazz = clazz;
        this.embeddedResourceName = Optional.ofNullable(embeddedResourceName);
    }

    @SneakyThrows
    public void load() {
        this.config = clazz.newInstance();
        plugin.getInjector().injectMembers(config);
        this.file = config.getConfigPath().toFile();
        this.loader = HoconConfigurationLoader.builder().setPath(config.getConfigPath()).build();

        if (!path.toFile().exists() && embeddedResourceName.isPresent()) {
            final URL embeddedResource = plugin.getResourceURL("messages.conf");
            final ConfigurationLoader<CommentedConfigurationNode> embeddedLoader = HoconConfigurationLoader.builder().setURL(embeddedResource).build();

            this.loader.save(embeddedLoader.load());
        }

        createFileIfNotExists();
        this.rootNode = loader.load();
        applyDefaults(true);

        System.out.println(rootNode);
    }

    @SneakyThrows(IOException.class)
    public void reload() {
        createFileIfNotExists();

        this.rootNode = loader.load();

        this.config = deserialize();
        applyDefaults(false);
    }

    @SneakyThrows(IOException.class)
    public void save() {
        loader.save(rootNode);
    }

    @SneakyThrows(IOException.class)
    public void backupToFile(final File path, final String name) {
        Validate.notNull(path);
        Validate.notNull(name);

        Files.copy(file.toPath(), new File(path, name).toPath());
    }

    @SneakyThrows(IOException.class)
    private void createFileIfNotExists() {
        if (!path.toFile().exists()) path.toFile().mkdirs();
        if (!file.exists()) file.createNewFile();
    }

    /* Serialization */

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public void applyDefaults(final boolean override) {
        createFileIfNotExists();

        if (embeddedResourceName.isPresent()) {
            final ConfigurationLoader<CommentedConfigurationNode> embeddedLoader = HoconConfigurationLoader.builder().setURL(plugin.getResourceURL(embeddedResourceName.get())).build();
            final CommentedConfigurationNode embeddedNode = embeddedLoader.load(ConfigurationOptions.defaults());
            rootNode.mergeValuesFrom(embeddedNode);
        } else {
            final T configObject = (T) clazz.getConstructors()[0].newInstance();
            plugin.getInjector().injectMembers(configObject);

            findFields().entrySet().forEach(e -> {
                final String[] nodePath = e.getValue().path().split("\\.");
                final ConfigurationNode fieldPath = rootNode.getNode(nodePath);

                if (override || fieldPath.getValue() != null) {
                    setValueInFile(e.getKey(), e.getValue());
                }
            });
        }

        save();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public T deserialize() {
        createFileIfNotExists();

        final T configObject = (T) clazz.getConstructors()[0].newInstance();

        findFields().entrySet().forEach(e -> {
            final Field field = e.getKey();
            final ConfigField configField = e.getValue();

            final String[] nodePath = configField.path().split("\\.");
            final ConfigurationNode node = rootNode.getNode(nodePath);

            try {
                if (field.get(configObject) instanceof Collection && node.getValue() instanceof Collection) {
                    final Collection collection = (Collection) field.get(configObject);
                    collection.clear();

                    collection.addAll((Collection) node.getValue());
                } else if (field.get(configObject) instanceof Map && node.getValue() instanceof Map) {
                    final Map map = (Map) field.get(configObject);
                    map.clear();

                    map.putAll((Map) node.getValue());
                } else {
                    setValueInObject(field, configField);
                }
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        });

        return configObject;
    }

    @SneakyThrows
    public void backupToFile(final Path newPath) {
        createFileIfNotExists();
        if (!newPath.toFile().exists()) newPath.toFile().createNewFile();

        final ConfigurationLoader<CommentedConfigurationNode> newLoader = HoconConfigurationLoader.builder().setPath(newPath).build();
        final ConfigurationNode newRootNode = newLoader.createEmptyNode();

        findFields().entrySet().forEach(e -> {
            final String[] nodePath = e.getValue().path().split("\\.");

            try {
                newRootNode.getNode(nodePath).setValue(e.getKey().get(config));
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            }
        });

        newLoader.save(newRootNode);
    }

    private Map<Field, ConfigField> findFields() {
        final Map<Field, ConfigField> map = new HashMap<>();

        for (final Field field : clazz.getDeclaredFields()) {
            final Optional<ConfigField> oConfigField = findConfigField(field);

            oConfigField.ifPresent(configField -> map.put(field, configField));
        }

        return ImmutableMap.copyOf(map);
    }

    private Optional<ConfigField> findConfigField(final Field field) {
        if (!field.isAnnotationPresent(ConfigField.class)) return Optional.empty();

        return Arrays.stream(field.getDeclaredAnnotations())
                .filter(annotation -> annotation instanceof ConfigFile)
                .limit(1)
                .map(annotation -> (ConfigField) annotation)
                .findAny();
    }

    @SneakyThrows(IllegalAccessException.class)
    private void setValueInObject(final Field field, ConfigField configField) {
        field.set(config, rootNode.getNode(configField.path().split("\\.")).getValue());
    }

    @SneakyThrows(IllegalAccessException.class)
    private void setValueInFile(final Field field, ConfigField configField) {
        rootNode.getNode(configField.path().split("\\.")).setValue(field.get(config));
    }
}
