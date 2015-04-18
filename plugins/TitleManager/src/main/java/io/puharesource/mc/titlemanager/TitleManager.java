package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.variables.VariableManager;
import io.puharesource.mc.titlemanager.backend.hooks.essentials.EssentialsHook;
import io.puharesource.mc.titlemanager.backend.hooks.ezrankslite.EZRanksLiteHook;
import io.puharesource.mc.titlemanager.backend.hooks.vanishnopacket.VanishNoPacketHook;
import io.puharesource.mc.titlemanager.backend.hooks.vault.VaultHook;
import io.puharesource.mc.titlemanager.backend.hooks.vault.VaultRuleEconomy;
import io.puharesource.mc.titlemanager.backend.hooks.vault.VaultRuleGroups;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import io.puharesource.mc.titlemanager.backend.variables.replacers.VariablesDefault;
import io.puharesource.mc.titlemanager.backend.variables.replacers.VariablesEZRanksLite;
import io.puharesource.mc.titlemanager.backend.variables.replacers.VariablesVault;
import io.puharesource.mc.titlemanager.backend.hooks.specialrules.VanishRule;
import io.puharesource.mc.titlemanager.commands.TMCommand;
import io.puharesource.mc.titlemanager.commands.sub.*;
import io.puharesource.mc.titlemanager.listeners.ListenerConnection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TitleManager extends JavaPlugin {

    private static TitleManager instance;
    private Config config;
    private ReflectionManager reflectionManager;
    private VariableManager variableManager;

    private static List<Integer> runningAnimations = Collections.synchronizedList(new ArrayList<Integer>());

    @Override
    public void onEnable() {
        instance = this;
        reflectionManager = ReflectionManager.createManager();
        variableManager = new VariableManager();

        getServer().getPluginManager().registerEvents(new ListenerConnection(), this);

        TMCommand cmd = new TMCommand();
        cmd.addSubCommand(new SubBroadcast());
        cmd.addSubCommand(new SubMessage());
        cmd.addSubCommand(new SubReload());
        cmd.addSubCommand(new SubABroadcast());
        cmd.addSubCommand(new SubAMessage());
        cmd.addSubCommand(new SubAnimations());

        variableManager.registerHook("VAULT", new VaultHook());
        variableManager.registerHook("ESSENTIALS", new EssentialsHook());
        variableManager.registerHook("VANISHNOPACKET", new VanishNoPacketHook());
        variableManager.registerHook("EZRANKSLITE", new EZRanksLiteHook());

        variableManager.registerRule("VANISH", new VanishRule());
        variableManager.registerRule("VAULT-ECONOMY", new VaultRuleEconomy());
        variableManager.registerRule("VAULT-GROUPS", new VaultRuleGroups());

        variableManager.registerVariableReplacer(new VariablesDefault());
        variableManager.registerVariableReplacer(new VariablesVault());
        variableManager.registerVariableReplacer(new VariablesEZRanksLite());

        config = new Config();
        config.load();
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

    public VariableManager getVariableManager() {
        return variableManager;
    }
}
