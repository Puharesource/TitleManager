package io.puharesource.mc.titlemanager.backend.variables.replacers;

import io.puharesource.mc.titlemanager.backend.hooks.vault.VaultHook;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import io.puharesource.mc.titlemanager.backend.variables.Variable;
import io.puharesource.mc.titlemanager.backend.variables.VariableReplacer;
import org.bukkit.entity.Player;

/**
 * Created by Tarkan on 16-04-2015.
 * This class is under the GPLv3 license.
 */
public final class VariablesVault implements VariableReplacer {

    @Variable(hook = "VAULT", vars = {"GROUP", "GROUP-NAME"})
    public String groupVar(Player player) {
        return VaultHook.isPermissionsSupported() ? VaultHook.getPermissions().getPrimaryGroup(player) : null;
    }

    @Variable(hook = "VAULT", vars = {"BALANCE", "MONEY"})
    public String balanceVar(Player player) { return VaultHook.isEconomySupported() ? MiscellaneousUtils.formatNumber(VaultHook.getEconomy().getBalance(player)) : null; }
}
