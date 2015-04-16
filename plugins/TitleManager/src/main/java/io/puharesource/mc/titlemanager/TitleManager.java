package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.backend.hooks.PluginHook;
import io.puharesource.mc.titlemanager.backend.hooks.essentials.EssentialsHook;
import io.puharesource.mc.titlemanager.backend.hooks.ezrankslite.EZRanksLiteHook;
import io.puharesource.mc.titlemanager.backend.hooks.vanishnopacket.VanishNoPacketHook;
import io.puharesource.mc.titlemanager.backend.hooks.vault.VaultHook;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import io.puharesource.mc.titlemanager.backend.variables.VariableReplacer;
import io.puharesource.mc.titlemanager.backend.variables.replacers.VariablesDefault;
import io.puharesource.mc.titlemanager.backend.variables.replacers.VariablesEZRanksLite;
import io.puharesource.mc.titlemanager.backend.variables.replacers.VariablesVault;
import io.puharesource.mc.titlemanager.commands.TMCommand;
import io.puharesource.mc.titlemanager.commands.sub.*;
import io.puharesource.mc.titlemanager.listeners.ListenerConnection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class TitleManager extends JavaPlugin {

    private static TitleManager instance;
    private Config config;
    private ReflectionManager reflectionManager;

    private static List<Integer> runningAnimations = Collections.synchronizedList(new ArrayList<Integer>());
    private static List<VariableReplacer> variableReplacers = Collections.synchronizedList(new ArrayList<VariableReplacer>());

    private Map<String, PluginHook> hooks = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        reflectionManager = ReflectionManager.createManager();

        getServer().getPluginManager().registerEvents(new ListenerConnection(), this);

        TMCommand cmd = new TMCommand();
        cmd.addSubCommand(new SubBroadcast());
        cmd.addSubCommand(new SubMessage());
        cmd.addSubCommand(new SubReload());
        cmd.addSubCommand(new SubABroadcast());
        cmd.addSubCommand(new SubAMessage());
        cmd.addSubCommand(new SubAnimations());

        addHook("VAULT", new VaultHook());
        addHook("ESSENTIALS", new EssentialsHook());
        addHook("VANISHNOPACKET", new VanishNoPacketHook());
        addHook("EZRANKSLITE", new EZRanksLiteHook());

        addVariableReplacer(new VariablesDefault());
        addVariableReplacer(new VariablesVault());
        addVariableReplacer(new VariablesEZRanksLite());

        config = new Config();
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

    public void addHook(String pluginName, PluginHook hook) {
        hooks.put(pluginName.toUpperCase().trim(), hook);
    }

    public PluginHook getHook(String pluginName) {
        return hooks.get(pluginName.toUpperCase().trim());
    }

    public void addVariableReplacer(VariableReplacer replacer) {
        variableReplacers.add(replacer);
    }
}
