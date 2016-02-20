package io.puharesource.mc.sponge.titlemanager.commands;

import lombok.Getter;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class CommandParameters {
    @Getter private final Map<String, CommandParameter> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public CommandParameters(final Map<String, CommandParameter> params) {
        this.params.putAll(params);
    }

    public boolean contains(final String parameter) {
        return params.containsKey(parameter) && get(parameter).getValue().isPresent();
    }

    public CommandParameter get(final String parameter) {
        return params.get(parameter);
    }

    public Optional<Integer> getInt(final String parameter) {
        try {
            return get(parameter).getInt();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public int getInt(final String parameter, final int defaultValue) {
        return get(parameter).getInt(defaultValue);
    }

    public Optional<Double> getDouble(final String parameter) {
        return get(parameter).getDouble();
    }

    public double getDouble(final String parameter, final double defaultValue) {
        return get(parameter).getDouble(defaultValue);
    }

    public boolean getBoolean(final String parameter) {
        return contains(parameter);
    }

    public Optional<World> getWorld(final String parameter) {
        return get(parameter).getWorld();
    }

    public World getWorld(final String parameter, final World defaultValue) {
        return get(parameter).getWorld(defaultValue);
    }
}
