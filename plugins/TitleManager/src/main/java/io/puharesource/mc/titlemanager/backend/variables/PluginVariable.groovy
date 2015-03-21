package io.puharesource.mc.titlemanager.backend.variables
import io.puharesource.mc.titlemanager.Config
import io.puharesource.mc.titlemanager.TitleManager
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
    GROUP({Player p -> VaultHook.permissions.getPrimaryGroup(p)}, "VAULT", VaultRuleGroups.class, "GROUP", "GROUP-NAME"),
    BALANCE({Player p -> formatNumber(VaultHook.economy.getBalance(p))}, "VAULT", VaultRuleEconomy.class, "BALANCE", "MONEY");

    Closure<String> closure
    String hook
    VariableRule rule
    String aliases

    PluginVariable(Closure<String> closure, String... aliases) {
        this.closure = closure
        this.aliases = aliases
    }

    PluginVariable(Closure<String> closure, Class<VariableRule> rule, String... aliases) {
        this(closure, aliases)
        this.rule = rule.newInstance()
    }

    PluginVariable(Closure<String> closure, String hook, Class<VariableRule> rule, String... aliases) {
        this(closure, rule, aliases)
        this.hook = hook
    }

    static String replace(Player player, String str) {
        for (PluginVariable var : values()) {
            if (var.hook != null && !TitleManager.getInstance().getHook(var.hook).isEnabled()) continue
            if (var.rule != null && !var.rule.rule(player)) continue

            for (String alias : var.aliases)
                str = str.replaceAll(getPattern(alias), var.closure(player))
        }
        return str
    }

    private static Pattern getPattern(String alias) {
        return Pattern.compile("\\{" + alias + "\\}")
    }

    private static String formatNumber(BigDecimal number) {
        Config config = TitleManager.getInstance().getConfigManager()

        if (config.isNumberFormatEnabled()) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US)
            return new DecimalFormat(config.getNumberFormat(), symbols).format(number)
        }
        String.valueOf(number.toDouble())
    }
}