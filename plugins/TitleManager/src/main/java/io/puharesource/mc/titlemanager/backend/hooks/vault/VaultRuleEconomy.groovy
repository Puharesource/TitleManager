package io.puharesource.mc.titlemanager.backend.hooks.vault
import io.puharesource.mc.titlemanager.TitleManager
import io.puharesource.mc.titlemanager.backend.variables.VariableRule
import org.bukkit.entity.Player

final class VaultRuleEconomy extends VariableRule {
    @Override
    boolean rule(Player player) {
        return TitleManager.getInstance().getHook("VAULT").isEnabled() && VaultHook.isEconomySupported()
    }

    @Override
    String[] replace(Player player, String text) {
        return new String[0]
    }
}
