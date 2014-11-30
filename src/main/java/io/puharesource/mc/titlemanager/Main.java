package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.commands.CommandMain;
import io.puharesource.mc.titlemanager.listeners.ListenerConnection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        TitleManager.load(this);
        try {
            Config.loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        getServer().getPluginManager().registerEvents(new ListenerConnection(), this);

        getCommand("tm").setExecutor(new CommandMain());

        for (Player player : Bukkit.getOnlinePlayers()) {

        }
    }
}
