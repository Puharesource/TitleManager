package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.backend.hooks.PluginHook;
import io.puharesource.mc.titlemanager.backend.hooks.VaultHook;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import io.puharesource.mc.titlemanager.commands.TMCommand;
import io.puharesource.mc.titlemanager.commands.sub.*;
import io.puharesource.mc.titlemanager.listeners.ListenerConnection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TitleManager extends JavaPlugin {

    private static TitleManager instance;
    private Config config;
    private ReflectionManager reflectionManager;
    private static List<Integer> runningAnimations = Collections.synchronizedList(new ArrayList<Integer>());

    private Map<String, PluginHook> hooks = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        config = new Config();
        reflectionManager = ReflectionManager.createManager();

        getServer().getPluginManager().registerEvents(new ListenerConnection(), this);

        TMCommand cmd = new TMCommand();
        cmd.addSubCommand(new SubBroadcast());
        cmd.addSubCommand(new SubMessage());
        cmd.addSubCommand(new SubReload());
        cmd.addSubCommand(new SubABroadcast());
        cmd.addSubCommand(new SubAMessage());
        cmd.addSubCommand(new SubAnimations());

        hooks.put("VAULT", VaultHook.getInstance());
    }

    public static TitleManager getInstance() {
        return instance;
    }

    public Config getConfigManager() {
        return config;
    }

    public ReflectionManager getReflectionManager() {
        return reflectionManager;
    }

    public static void addRunningAnimationId(int id) {
        runningAnimations.add(id);
    }

    public static void removeRunningAnimationId(int id) {
        runningAnimations.remove((Integer) id);
    }

    public static List<Integer> getRunningAnimations() {
        return runningAnimations;
    }

    public PluginHook getHook(String pluginName) {
        return hooks.get(pluginName.toUpperCase().trim());
    }
}
