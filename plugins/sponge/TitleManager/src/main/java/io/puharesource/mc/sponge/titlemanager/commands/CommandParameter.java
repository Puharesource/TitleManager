package io.puharesource.mc.sponge.titlemanager.commands;


import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;

import java.util.Optional;

public final class CommandParameter {
    private final String param;
    private final String value;

    public CommandParameter(final String param, final String value) {
        this.param = param;
        this.value = value != null && value.isEmpty() ? null : value;
    }

    public String getParameter() {
        return param;
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }

    public String getValue(final String defaultValue) {
        return getValue().orElse(defaultValue);
    }

    public Optional<Integer> getInt() {
        if (value == null) return Optional.empty();

        try {
            return Optional.ofNullable(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public int getInt(final int defaultValue) {
        return getInt().orElse(defaultValue);
    }

    public Optional<Double> getDouble() {
        if (value == null) return Optional.empty();

        try {
            return Optional.ofNullable(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public double getDouble(final double defaultValue) {
        return getDouble().orElse(defaultValue);
    }

    public Optional<World> getWorld() {
        return Sponge.getServer().getWorld(value);
    }

    public World getWorld(final World defaultValue) {
        return getWorld().orElse(defaultValue);
    }
}
