package io.puharesource.mc.titlemanager.backend.hooks.vault

import io.puharesource.mc.titlemanager.backend.variables.VariableRule
import org.bukkit.entity.Player

final class VaultRuleGroups extends VariableRule {
    @Override
    boolean rule(Player player) { VaultHook.isPermissionsSupported() && VaultHook.hasGroupSupport() }

    @Override
    String[] replace(Player player, String text) {
        return new String[0]
    }
}
