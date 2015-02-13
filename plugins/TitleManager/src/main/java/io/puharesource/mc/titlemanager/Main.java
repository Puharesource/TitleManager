package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.commands.TMCommand;
import io.puharesource.mc.titlemanager.commands.sub.*;
import io.puharesource.mc.titlemanager.listeners.ListenerConnection;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        ReflectionManager.getInstance();
        TitleManager.load(this);
        Config.loadConfig();

        getServer().getPluginManager().registerEvents(new ListenerConnection(), this);

        TMCommand cmd = new TMCommand();
        cmd.addSubCommand(new SubBroadcast());
        cmd.addSubCommand(new SubMessage());
        cmd.addSubCommand(new SubReload());
        cmd.addSubCommand(new SubABroadcast());
        cmd.addSubCommand(new SubAMessage());
        cmd.addSubCommand(new SubAnimations());
    }
}
