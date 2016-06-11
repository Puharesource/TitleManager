package io.puharesource.mc.titlemanager.commands;

import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import lombok.Getter;

public final class CommandParameters {
    @Getter private final Map<CommandParameterIdentifier, CommandParameter> params = new HashMap<>();

    public CommandParameters(final Map<CommandParameterIdentifier, CommandParameter> params) {
        this.params.putAll(params);
    }

    public boolean contains(final CommandParameterIdentifier parameter) {
        return params.containsKey(parameter);
    }

    public boolean containsValue(final CommandParameterIdentifier parameter) {
        return params.containsKey(parameter) && get(parameter).getValue().isPresent();
    }

    public CommandParameter get(final CommandParameterIdentifier parameter) {
        return params.get(parameter);
    }

    public Optional<Integer> getInt(final CommandParameterIdentifier parameter) {
        return get(parameter).getInt();
    }

    public int getInt(final CommandParameterIdentifier parameter, final int defaultValue) {
        return get(parameter).getInt(defaultValue);
    }

    public Optional<Double> getDouble(final CommandParameterIdentifier parameter) {
        return get(parameter).getDouble();
    }

    public double getDouble(final CommandParameterIdentifier parameter, final double defaultValue) {
        return get(parameter).getDouble(defaultValue);
    }

    public Optional<World> getWorld(final CommandParameterIdentifier parameter) {
        return get(parameter).getWorld();
    }

    public World getWorld(final CommandParameterIdentifier parameter, final World defaultValue) {
        return get(parameter).getWorld(defaultValue);
    }

    public Optional<BungeeServerInfo> getServer(final CommandParameterIdentifier parameter) {
        return get(parameter).getServer();
    }

    public BungeeServerInfo getServer(final CommandParameterIdentifier parameter, final BungeeServerInfo defaultValue) {
        return get(parameter).getServer(defaultValue);
    }
}
