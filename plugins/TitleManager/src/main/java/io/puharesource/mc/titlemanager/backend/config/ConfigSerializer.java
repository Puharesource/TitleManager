package io.puharesource.mc.titlemanager.backend.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public final class ConfigSerializer {

    public static void saveDefaults(Class<?> clazz, File file, boolean override) throws IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        try {
            if (!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        Object object = clazz.getConstructors()[0].newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class)) continue;

            ConfigField configField = null;
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (annotation instanceof ConfigField) {
                    configField = (ConfigField) annotation;
                    break;
                }
            }

            if (configField == null) continue;

            try {
                if (override || !config.contains(configField.path()))
                    config.set(configField.path(), field.get(object));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        config.save(file);
    }

    @SuppressWarnings("unchecked")
    public static <T>T deserialize(Class<T> clazz, File file) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        T object = (T) clazz.getConstructors()[0].newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class)) continue;

            ConfigField configField = null;
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (annotation instanceof ConfigField) {
                    configField = (ConfigField) annotation;
                    break;
                }
            }

            if (configField != null) {
                field.set(object, config.get(configField.path()));
            }
        }

        return object;
    }

    public static FileConfiguration serialize(Object object, File file) throws IllegalAccessException {
        try {
            if (!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (Field field : object.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(ConfigField.class)) continue;

            ConfigField configField = null;
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (annotation instanceof ConfigField) {
                    configField = (ConfigField) annotation;
                    break;
                }
            }

            if (configField != null)
                if (!config.contains(configField.path()))
                    config.set(configField.path(), field.get(object));
        }

        return config;
    }
}
