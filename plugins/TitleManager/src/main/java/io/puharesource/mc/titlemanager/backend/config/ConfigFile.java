package io.puharesource.mc.titlemanager.backend.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class ConfigFile {
    private JavaPlugin plugin;
    private File path;
    private @Getter File file;
    private String fileName;
    private @Getter FileConfiguration config;
    private boolean locatedInJar;

    private InputStream stream;

    public ConfigFile(final JavaPlugin plugin, final File path, final String fileName, final boolean locatedInJar) {
        this.plugin = plugin;
        this.path = path;
        this.fileName = fileName + ".yml";
        file = new File(path, this.fileName);
        this.locatedInJar = locatedInJar;

        reload();
    }

    public ConfigFile(final InputStream stream) {
        this.stream = stream;

        reload();
    }

    public void reload() {
        if (stream != null) {
            config = YamlConfiguration.loadConfiguration(stream);
            return;
        }

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

    public FileConfiguration getCopy() {
        return YamlConfiguration.loadConfiguration(file);
    }
}
