package io.puharesource.mc.titlemanager.api.variables;

import org.bukkit.entity.Player;

@Deprecated
public abstract class VariableRule {
    @Deprecated
    public abstract boolean rule(Player player);

    @Deprecated
    public abstract String[] replace(Player player, String text);
}