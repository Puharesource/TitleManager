package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.commands.CommandMain;
import io.puharesource.mc.titlemanager.listeners.ListenerConnection;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        TitleManager.setPlugin(this);
        Config.loadConfig();

        getServer().getPluginManager().registerEvents(new ListenerConnection(), this);

        getCommand("tm").setExecutor(new CommandMain());
    }
}
