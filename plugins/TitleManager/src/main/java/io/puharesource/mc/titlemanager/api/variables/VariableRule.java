package io.puharesource.mc.titlemanager.api.variables;

import org.bukkit.entity.Player;

public abstract class VariableRule {
    public abstract boolean rule(Player player);

    public abstract String[] replace(Player player, String text);
}
