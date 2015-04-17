package io.puharesource.mc.titlemanager.backend.hooks.vault;

import io.puharesource.mc.titlemanager.api.variables.VariableRule;
import org.bukkit.entity.Player;

public final class VaultRuleGroups extends VariableRule {
    @Override
    public boolean rule(Player player) {
        return VaultHook.isPermissionsSupported() && VaultHook.hasGroupSupport();
    }

    @Override
    public String[] replace(Player player, String text) {
        return new String[0];
    }

}
