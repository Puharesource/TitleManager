package io.puharesource.mc.titlemanager;

import com.google.common.collect.ImmutableList;
import io.puharesource.mc.titlemanager.api.variables.VariableManager;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeManager;
import io.puharesource.mc.titlemanager.backend.engine.Engine;
import io.puharesource.mc.titlemanager.backend.hooks.essentials.EssentialsHook;
import io.puharesource.mc.titlemanager.backend.hooks.ezrankslite.EZRanksLiteHook;
import io.puharesource.mc.titlemanager.backend.hooks.placeholderapi.PlaceholderAPIHook;
import io.puharesource.mc.titlemanager.backend.hooks.specialrules.BungeeRule;
import io.puharesource.mc.titlemanager.backend.hooks.specialrules.VanishRule;
import io.puharesource.mc.titlemanager.backend.hooks.supervanish.PremiumVanishHook;
import io.puharesource.mc.titlemanager.backend.hooks.supervanish.SuperVanishHook;
import io.puharesource.mc.titlemanager.backend.hooks.vanishnopacket.VanishNoPacketHook;
import io.puharesource.mc.titlemanager.backend.hooks.vault.VaultHook;
import io.puharesource.mc.titlemanager.backend.hooks.vault.VaultRuleEconomy;
import io.puharesource.mc.titlemanager.backend.hooks.vault.VaultRuleGroups;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import io.puharesource.mc.titlemanager.backend.updatechecker.UpdateManager;
import io.puharesource.mc.titlemanager.backend.variables.replacers.VariablesBungee;
import io.puharesource.mc.titlemanager.backend.variables.replacers.VariablesDefault;
import io.puharesource.mc.titlemanager.backend.variables.replacers.VariablesEZRanksLite;
import io.puharesource.mc.titlemanager.backend.variables.replacers.VariablesVault;
import io.puharesource.mc.titlemanager.commands.TMCommand;
import io.puharesource.mc.titlemanager.commands.sub.*;
import io.puharesource.mc.titlemanager.listeners.ListenerConnection;
import io.puharesource.mc.titlemanager.listeners.ListenerItemSlot;
import io.puharesource.mc.titlemanager.listeners.ListenerWorldChange;
import lombok.Getter;
import lombok.val;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TitleManager extends JavaPlugin {
    private @Getter static TitleManager instance;
    private @Getter Config configManager;
    private @Getter ReflectionManager reflectionManager;
    private @Getter VariableManager variableManager;
    private @Getter BungeeManager bungeeManager;
    private @Getter UpdateManager updateManager;
    private @Getter Engine engine;

    private static List<Integer> runningAnimations = Collections.synchronizedList(new ArrayList<Integer>());

    @Override
    public void onEnable() {
        instance = this;
        reflectionManager = ReflectionManager.createManager();
        variableManager = new VariableManager();
        updateManager = new UpdateManager();
        engine = new Engine();

        configManager = new Config();
        configManager.load();

        val pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new ListenerConnection(), this);
        pluginManager.registerEvents(new ListenerItemSlot(), this);
        pluginManager.registerEvents(new ListenerWorldChange(), this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", bungeeManager = new BungeeManager());

        val cmd = new TMCommand();
        cmd.addSubCommand(new SubBroadcast());
        cmd.addSubCommand(new SubMessage());
        cmd.addSubCommand(new SubReload());
        cmd.addSubCommand(new SubABroadcast());
        cmd.addSubCommand(new SubAMessage());
        cmd.addSubCommand(new SubAnimations());
        cmd.addSubCommand(new SubVersion());
        cmd.addSubCommand(new SubScripts());
        cmd.addSubCommand(new SubDebug());

        variableManager.registerHook("VAULT", new VaultHook());
        variableManager.registerHook("ESSENTIALS", new EssentialsHook());
        variableManager.registerHook("VANISHNOPACKET", new VanishNoPacketHook());
        variableManager.registerHook("SUPERVANISH", new SuperVanishHook());
        variableManager.registerHook("PREMIUMVANISH", new PremiumVanishHook());
        variableManager.registerHook("EZRANKSLITE", new EZRanksLiteHook());
        variableManager.registerHook("PLACEHOLDERAPI", new PlaceholderAPIHook());

        variableManager.registerRule("VANISH", new VanishRule());
        variableManager.registerRule("VAULT-ECONOMY", new VaultRuleEconomy());
        variableManager.registerRule("VAULT-GROUPS", new VaultRuleGroups());
        variableManager.registerRule("BUNGEECORD", new BungeeRule());

        variableManager.registerVariableReplacer(new VariablesDefault());
        variableManager.registerVariableReplacer(new VariablesVault());
        variableManager.registerVariableReplacer(new VariablesEZRanksLite());
        variableManager.registerVariableReplacer(new VariablesBungee());
    }

    @Override
    public void onDisable() {
        engine.cancelAll();
        runningAnimations.clear();
    }

    public static List<Integer> getRunningAnimations() {
        return ImmutableList.copyOf(runningAnimations);
    }

    public static void addRunningAnimationId(int id) {
        runningAnimations.add(id);
    }

    public static void removeRunningAnimationId(int id) {
        runningAnimations.remove((Integer) id);
    }
}
