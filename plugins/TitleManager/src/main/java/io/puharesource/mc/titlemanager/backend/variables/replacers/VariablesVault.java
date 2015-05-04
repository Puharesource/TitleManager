package io.puharesource.mc.titlemanager.backend.variables.replacers;

import io.puharesource.mc.titlemanager.backend.hooks.vault.VaultHook;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.api.variables.Variable;
import io.puharesource.mc.titlemanager.api.variables.VariableReplacer;
import org.bukkit.entity.Player;

public final class VariablesVault implements VariableReplacer {

    @Variable(hook = "VAULT", rule = "VAULT-GROUPS", vars = {"GROUP", "GROUP-NAME"})
    public String groupVar(Player player) { return VaultHook.getPermissions().getPrimaryGroup(player); }

    @Variable(hook = "VAULT", rule = "VAULT-ECONOMY", vars = {"BALANCE", "MONEY"})
    public String balanceVar(Player player) { return MiscellaneousUtils.formatNumber(VaultHook.getEconomy().getBalance(player)); }
}
