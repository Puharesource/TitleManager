package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.commands.TMCommand;
import io.puharesource.mc.titlemanager.commands.sub.*;
import io.puharesource.mc.titlemanager.listeners.ListenerConnection;
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

        TMCommand cmd = new TMCommand();
        cmd.addSubCommand(new SubBroadcast());
        cmd.addSubCommand(new SubMessage());
        cmd.addSubCommand(new SubReload());
        cmd.addSubCommand(new SubABroadcast());
        cmd.addSubCommand(new SubAMessage());
    }
}
