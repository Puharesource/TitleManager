package io.puharesource.mc.titlemanager.commands;

import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import lombok.Data;
import org.bukkit.World;

import java.util.Map;

@Data
public final class CommandParameters {
    private final Map<String, CommandParameter> params;

    public boolean contains(final String parameter) {
        return params.containsKey(parameter.toUpperCase().trim()) && get(parameter).getValue() != null && !get(parameter).getValue().isEmpty();
    }

    public CommandParameter get(final String parameter) {
        return params.get(parameter.toUpperCase().trim());
    }

    public int getInt(final String parameter) throws NumberFormatException {
        return get(parameter).getInt();
    }

    public int getInt(final String parameter, final int defaultValue) {
        return contains(parameter) ? get(parameter).getInt(defaultValue) : defaultValue;
    }

    public double getDouble(final String parameter) throws NumberFormatException {
        return get(parameter).getDouble();
    }

    public double getDouble(final String parameter, final double defaultValue) {
        return contains(parameter) ? get(parameter).getDouble(defaultValue) : defaultValue;
    }

    public boolean getBoolean(final String parameter) {
        return contains(parameter);
    }

    public World getWorld(final String parameter) {
        return get(parameter).getWorld();
    }

    public World getWorld(final String parameter, final World defaultValue) {
        return contains(parameter) ? get(parameter).getWorld(defaultValue) : defaultValue;
    }

    public BungeeServerInfo getServer(final String parameter) {
        return get(parameter).getServer();
    }

    public BungeeServerInfo getServer(final String parameter, final BungeeServerInfo defaultValue) {
        return contains(parameter) ? get(parameter).getServer(defaultValue) : defaultValue;
    }
}
