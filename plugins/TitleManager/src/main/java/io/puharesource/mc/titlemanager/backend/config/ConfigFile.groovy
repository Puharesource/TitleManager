package io.puharesource.mc.titlemanager.backend.config

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin

import java.nio.file.Files

class ConfigFile {
    private JavaPlugin plugin
    private File path
    private File file
    private String fileName
    private FileConfiguration config
    private boolean locatedInJar

    ConfigFile(JavaPlugin plugin, File path, String fileName, boolean locatedInJar) {
        this.plugin = plugin
        this.path = path
        this.fileName = fileName
        file = new File(path, this.fileName)
        this.locatedInJar = locatedInJar

        reload()
    }

    void reload() {
        if (!path.exists()) path.mkdirs();

        if (locatedInJar) {
            if (!file.exists())
                Files.copy(plugin.getResource(fileName), file.toPath());
        } else if (!file.exists())
            file.createNewFile();

        config = YamlConfiguration.loadConfiguration(file);
    }

    void save() { config.save(file) }

    void backupToFile(File folder, String fileName) { Files.copy(file.toPath(), new File(folder, fileName).toPath()) }

    void regenConfig() {
        if (!path.exists()) path.mkdir()
        if (file.exists()) file.delete()

        if (locatedInJar) {
            if (!file.exists())
                Files.copy(plugin.getResource(fileName), file.toPath())
        } else if (!file.exists()) {
            file.createNewFile()
        }

        config = YamlConfiguration.loadConfiguration(file)
    }

    FileConfiguration getConfig() { config }

    File getFile() { file }

    FileConfiguration getCopy() { YamlConfiguration.loadConfiguration(file) }
}