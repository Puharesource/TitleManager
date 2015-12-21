package io.puharesource.mc.titlemanager.commands;

import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import lombok.Data;
import lombok.Getter;
import org.bukkit.World;

import java.util.Map;
import java.util.TreeMap;

public final class CommandParameters {
    private @Getter final Map<String, CommandParameter> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public CommandParameters(final Map<String, CommandParameter> params) {
        this.params.putAll(params);
    }

    public boolean contains(final String parameter) {
        return params.containsKey(parameter) && get(parameter).getValue() != null && !get(parameter).getValue().isEmpty();
    }

    public CommandParameter get(final String parameter) {
        return params.get(parameter);
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
        return contains(parameter) ? get(parameter).getWorld() : null;
    }

    public World getWorld(final String parameter, final World defaultValue) {
        return contains(parameter) ? get(parameter).getWorld(defaultValue) : defaultValue;
    }

    public BungeeServerInfo getServer(final String parameter) {
        return contains(parameter) ? get(parameter).getServer() : null;
    }

    public BungeeServerInfo getServer(final String parameter, final BungeeServerInfo defaultValue) {
        return contains(parameter) ? get(parameter).getServer(defaultValue) : defaultValue;
    }
}
