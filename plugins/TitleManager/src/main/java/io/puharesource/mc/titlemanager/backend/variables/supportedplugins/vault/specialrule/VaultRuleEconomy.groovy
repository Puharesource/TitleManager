package io.puharesource.mc.titlemanager.backend.variables.supportedplugins.vault.specialrule

import io.puharesource.mc.titlemanager.TitleManager
import io.puharesource.mc.titlemanager.backend.hooks.VaultHook
import io.puharesource.mc.titlemanager.backend.variables.VariableRule
import org.bukkit.entity.Player

class VaultRuleEconomy extends VariableRule {
    @Override
    boolean rule(Player player) {
        return TitleManager.getInstance().getHook("VAULT").isEnabled() && VaultHook.isEconomySupported()
    }

    @Override
    String[] replace(Player player, String text) {
        return new String[0]
    }
}
