package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.commands.CommandMain;
import io.puharesource.mc.titlemanager.listeners.ListenerConnection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        TitleManager.setPlugin(this);
        try {
            Config.loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getServer().getPluginManager().registerEvents(new ListenerConnection(), this);

        getCommand("tm").setExecutor(new CommandMain());
    }
}
