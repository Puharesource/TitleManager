package io.puharesource.mc.titlemanager.backend.hooks.specialrules;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.variables.VariableRule;
import org.bukkit.entity.Player;

public final class BungeeRule extends VariableRule {
    @Override
    public boolean rule(Player player) {
        return TitleManager.getInstance().getConfigManager().getConfig().usingBungeecord;
    }

    @Override
    public String[] replace(Player player, String text) {
        return new String[0];
    }
}
