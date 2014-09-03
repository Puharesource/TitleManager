package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.listeners.ListenerConnection;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        TitleManager.setPlugin(this);
        TitleManager.loadConfig();

        getServer().getPluginManager().registerEvents(new ListenerConnection(), this);
    }
}
