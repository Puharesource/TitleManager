package io.puharesource.mc.titlemanager.backend.variables
import io.puharesource.mc.titlemanager.TitleManager
import io.puharesource.mc.titlemanager.__Config
import io.puharesource.mc.titlemanager.backend.hooks.PluginHook
import io.puharesource.mc.titlemanager.backend.hooks.VaultHook
import io.puharesource.mc.titlemanager.backend.variables.supportedplugins.vault.specialrule.VaultRuleEconomy
import io.puharesource.mc.titlemanager.backend.variables.supportedplugins.vault.specialrule.VaultRuleGroups
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.regex.Pattern

enum PluginVariable {
    //TitleManager Variables:
    PLAYER({Player p -> p.getName()}, "PLAYER", "USERNAME", "NAME"),
    DISPLAYNAME({Player p -> p.getDisplayName()}, "DISPLAYNAME", "DISPLAY-NAME", "NICKNAME", "NICK"),
    STRIPPEDDISPLAYNAME({Player p -> ChatColor.stripColor(p.getDisplayName())}, "STRIPPEDDISPLAYNAME", "STRIPPED-DISPLAYNAME", "STRIPPED-NICKNAME", "STRIPPED-NICK"),
    WORLD({Player p -> p.getWorld().getName()}, "WORLD", "WORLD-NAME"),
    WORLD_TIME({Player p -> String.valueOf(p.getWorld().getTime())}, "WORLD-TIME"),
    ONLINE({Player p -> String.valueOf(Bukkit.getOnlinePlayers().size())}, "ONLINE", "ONLINE-PLAYERS"),
    MAX_PLAYERS({Player p -> String.valueOf(Bukkit.getMaxPlayers())}, "MAX-PLAYERS"),
    WORLD_PLAYERS({Player p -> String.valueOf(p.getWorld().getPlayers().size())}, "WORLD-PLAYERS", "WORLD-ONLINE"),

    //Vault Variables:
    GROUP({Player p -> TitleManager.permissions.getPrimaryGroup(p)}, VaultHook.getInstance(), VaultRuleGroups.class, "GROUP", "GROUP-NAME"),
    BALANCE({Player p -> formatNumber(TitleManager.economy.getBalance(p))}, VaultHook.getInstance(), VaultRuleEconomy.class, "BALANCE", "MONEY");

    Closure<String> closure
    PluginHook hook
    VariableRule rule
    String aliases

    PluginVariable(Closure<String> closure, String... aliases) {
        this.closure = closure
        this.aliases = aliases
    }

    PluginVariable(Closure<String> closure, PluginHook hook, String... aliases) {
        this(closure, aliases)
        this.hook = hook
    }

    PluginVariable(Closure<String> closure, Class<VariableRule> rule, String... aliases) {
        this(closure, aliases)
        this.rule = rule.newInstance()
    }

    PluginVariable(Closure<String> closure, PluginHook hook, Class<VariableRule> rule, String... aliases) {
        this(closure, hook, aliases)
        this.rule = rule.newInstance()
    }

    static String replace(Player player, String str) {
        for (PluginVariable var : values()) {
            if (var.hook != null && Bukkit.getPluginManager().getPlugin(var.hook.pluginName) == null) continue
            if (var.rule != null && !var.rule.rule(player)) continue

            for (String alias : var.aliases)
                str = str.replaceAll(getPattern(alias), var.closure(player))
        }

        str
    }

    private static Pattern getPattern(String alias) {
        return Pattern.compile("\\{" + alias + "\\}")
    }

    private static String formatNumber(BigDecimal number) {
        if (__Config.numberFormatEnabled) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US)
            return new DecimalFormat(__Config.getNumberFormat(), symbols).format(number)
        }
        String.valueOf(number.toDouble())
    }
}