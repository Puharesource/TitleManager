package io.puharesource.mc.titlemanager.backend.variables.replacers;

import io.puharesource.mc.titlemanager.backend.hooks.ezrankslite.EZRanksLiteHook;
import io.puharesource.mc.titlemanager.api.variables.Variable;
import io.puharesource.mc.titlemanager.api.variables.VariableReplacer;
import org.bukkit.entity.Player;

public final class VariablesEZRanksLite implements VariableReplacer {
    @Variable(hook = "EZRANKSLITE", vars = {"EZRL.RANKPREFIX"})
    public String rankPrefixVar(Player player) { return EZRanksLiteHook.getRankPrefix(player); }

    @Variable(hook = "EZRANKSLITE", vars = {"EZRL.RANKUPPREFIX"})
    public String rankupPrefixVar(Player player) { return EZRanksLiteHook.getRankupPrefix(player); }

    @Variable(hook = "EZRANKSLITE", vars = {"EZRL.CURRENTRANK", "EZRL.RANKFROM"})
    public String currentRankVar(Player player) { return EZRanksLiteHook.getCurrentRank(player); }

    @Variable(hook = "EZRANKSLITE", vars = {"EZRL.RANKTO", "EZRL.RANKUP"})
    public String rankupVar(Player player) { return EZRanksLiteHook.getRankup(player); }

    @Variable(hook = "EZRANKSLITE", vars = {"EZRL.COST", "EZRL.RANKUPCOST"})
    public String rankupCostVar(Player player) { return EZRanksLiteHook.getRankupCost(player); }

    @Variable(hook = "EZRANKSLITE", vars = {"EZRL.PROGRESS"})
    public String progressVar(Player player) { return EZRanksLiteHook.getProgress(player); }

    @Variable(hook = "EZRANKSLITE", vars = {"EZRL.PROGRESSBAR"})
    public String progressBarVar(Player player) { return EZRanksLiteHook.getProgressBar(player); }

    @Variable(hook = "EZRANKSLITE", vars = {"EZRL.NEEDED", "EZRL.DIFFERENCE"})
    public String neededVar(Player player) { return EZRanksLiteHook.getNeeded(player); }
}
