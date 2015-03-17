package io.puharesource.mc.titlemanager.backend.variables

import org.bukkit.entity.Player

abstract class SpecialRule {
    abstract boolean rule(Player player);
    abstract String[] replace(Player player, String text);
}
