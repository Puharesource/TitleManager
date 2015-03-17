package io.puharesource.mc.titlemanager.backend.variables.supportedplugins.vault.specialrule

import io.puharesource.mc.titlemanager.TitleManager
import io.puharesource.mc.titlemanager.backend.variables.SpecialRule
import org.bukkit.entity.Player

class VaultRuleEconomy extends SpecialRule {
    @Override
    boolean rule(Player player) {
        return TitleManager.isEconomySupported()
    }

    @Override
    String[] replace(Player player, String text) {
        return new String[0]
    }
}
