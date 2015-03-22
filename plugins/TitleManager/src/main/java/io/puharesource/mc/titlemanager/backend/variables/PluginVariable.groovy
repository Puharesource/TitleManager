package io.puharesource.mc.titlemanager.backend.variables

import io.puharesource.mc.titlemanager.TitleManager
import io.puharesource.mc.titlemanager.backend.hooks.ezrankslite.EZRanksLiteHook
import io.puharesource.mc.titlemanager.backend.hooks.vault.VaultHook
import io.puharesource.mc.titlemanager.backend.hooks.vault.VaultRuleEconomy
import io.puharesource.mc.titlemanager.backend.hooks.vault.VaultRuleGroups
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils
import io.puharesource.mc.titlemanager.backend.variables.specialrule.VanishRule
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

import java.text.SimpleDateFormat
import java.util.regex.Pattern

enum PluginVariable {
    //TitleManager Variables
    PLAYER({Player p -> p.getName()}, "PLAYER", "USERNAME", "NAME"),
    DISPLAYNAME({Player p -> p.getDisplayName()}, "DISPLAYNAME", "DISPLAY-NAME", "NICKNAME", "NICK"),
    STRIPPEDDISPLAYNAME({Player p -> ChatColor.stripColor(p.getDisplayName())}, "STRIPPEDDISPLAYNAME", "STRIPPED-DISPLAYNAME", "STRIPPED-NICKNAME", "STRIPPED-NICK"),
    WORLD({Player p -> p.getWorld().getName()}, "WORLD", "WORLD-NAME"),
    WORLD_TIME({Player p -> String.valueOf(p.getWorld().getTime())}, "WORLD-TIME"),
    ONLINE({Player p -> String.valueOf(Bukkit.getOnlinePlayers().size())}, "ONLINE", "ONLINE-PLAYERS"),
    MAX_PLAYERS({Player p -> String.valueOf(Bukkit.getMaxPlayers())}, "MAX-PLAYERS"),
    WORLD_PLAYERS({Player p -> String.valueOf(p.getWorld().getPlayers().size())}, "WORLD-PLAYERS", "WORLD-ONLINE"),
    SERVER_TIME({Player p -> new SimpleDateFormat(TitleManager.getInstance().getConfig().getString("date-format.format")).format(new Date(System.currentTimeMillis()))}, "SERVER-TIME"),

    //Vault Variables
    VAULT_GROUP({Player p -> VaultHook.permissions.getPrimaryGroup(p)}, "VAULT", VaultRuleGroups.class, "GROUP", "GROUP-NAME"),
    VAULT_BALANCE({Player p -> MiscellaneousUtils.formatNumber(VaultHook.economy.getBalance(p))}, "VAULT", VaultRuleEconomy.class, "BALANCE", "MONEY"),

    //EZRanksLite Variables
    EZRL_RANK_PREFIX({Player p -> EZRanksLiteHook.getRankPrefix(p)}, "EZRanksLite", (Class<VariableRule>) null, "EZRL.RANKPREFIX"),
    EZRL_RANKUP_PREFIX({Player p -> EZRanksLiteHook.getRankupPrefix(p)}, "EZRanksLite", (Class<VariableRule>) null, "EZRL.RANKUPPREFIX"),
    EZRL_CURRENT_RANK({Player p -> EZRanksLiteHook.getCurrentRank(p)}, "EZRanksLite", (Class<VariableRule>) null, "EZRL.CURRENTRANK", "EZRL.RANKFROM"),
    EZRL_RANKUP_RANK({Player p -> EZRanksLiteHook.getRankup(p)}, "EZRanksLite", (Class<VariableRule>) null, "EZRL.RANKTO", "EZRL.RANKUP"),
    EZRL_COST({Player p -> EZRanksLiteHook.getRankupCost(p)}, "EZRanksLite", (Class<VariableRule>) null, "EZRL.COST", "EZRL.RANKUPCOST"),
    EZRL_PROGRESS({Player p -> EZRanksLiteHook.getProgress(p)}, "EZRanksLite", (Class<VariableRule>) null, "EZRL.PROGRESS"),
    EZRL_PROGRESSBAR({Player p -> EZRanksLiteHook.getProgressBar(p)}, "EZRanksLite", (Class<VariableRule>), null, "EZRL.PROGRESSBAR"),
    EZRL_NEEDED({Player p -> EZRanksLiteHook.getNeeded(p)}, "EZRanksLite", (Class<VariableRule>), null, "EZRL.NEEDED", "EZRL.DIFFERENCE"),

    //Special Rules
    SAFE_ONLINE({Player p -> String.valueOf(VanishRule.getOnlinePlayers())}, VanishRule.class, "SAFE-ONLINE", "SAFE-ONLINE-PLAYERS");

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
}