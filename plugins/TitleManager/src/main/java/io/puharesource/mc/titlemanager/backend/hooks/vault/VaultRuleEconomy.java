package io.puharesource.mc.titlemanager.backend.hooks.vault;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.variables.VariableRule;
import org.bukkit.entity.Player;

public final class VaultRuleEconomy extends VariableRule {
    @Override
    public boolean rule(Player player) {
        return TitleManager.getInstance().getVariableManager().getHook("VAULT").isEnabled() && VaultHook.isEconomySupported();
    }

    @Override
    public String[] replace(Player player, String text) {
        return new String[0];
    }

}
