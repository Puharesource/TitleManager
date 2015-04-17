package io.puharesource.mc.titlemanager.backend.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ConfigFile {

    private JavaPlugin plugin;
    private File path;
    private File file;
    private String fileName;
    private FileConfiguration config;
    private boolean locatedInJar;

    public ConfigFile(JavaPlugin plugin, File path, String fileName, boolean locatedInJar) {
        this.plugin = plugin;
        this.path = path;
        this.fileName = fileName + ".yml";
        file = new File(path, this.fileName);
        this.locatedInJar = locatedInJar;

        reload();
    }

    public void reload() {
        if (!path.exists()) path.mkdirs();

        try {
            if (locatedInJar) {
                if (!file.exists())
                    Files.copy(plugin.getResource(fileName), file.toPath());
            } else if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void backupToFile(File folder, String fileName) {
        try {
            Files.copy(file.toPath(), new File(folder, fileName).toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void regenConfig() {
        if (!path.exists()) path.mkdir();
        if (file.exists()) file.delete();

        try {
            if (locatedInJar) {
                if (!file.exists())
                    Files.copy(plugin.getResource(fileName), file.toPath());
            } else if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public File getFile() {
        return file;
    }

    public FileConfiguration getCopy() {
        return YamlConfiguration.loadConfiguration(file);
    }
}
