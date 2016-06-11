package io.puharesource.mc.titlemanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Optional;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;

import static io.puharesource.mc.titlemanager.backend.language.Messages.INVALID_RADIUS;
import static io.puharesource.mc.titlemanager.backend.language.Messages.INVALID_SERVER;
import static io.puharesource.mc.titlemanager.backend.language.Messages.INVALID_WORLD;

public final class CommandParameter {
    private final CommandParameterIdentifier identifier;
    private final String value;

    public CommandParameter(final CommandParameterIdentifier identifier, final String value) {
        this.identifier = identifier;
        this.value = value != null && value.isEmpty() ? null : value;
    }

    public CommandParameterIdentifier getIdentifier() {
        return identifier;
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
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            if (identifier == CommandParameterIdentifier.RADIUS) {
                throw new TMCommandException(INVALID_RADIUS, value);
            }

            return Optional.empty();
        }
    }

    public int getInt(final int defaultValue) {
        return getInt().orElse(defaultValue);
    }

    public Optional<Double> getDouble() {
        if (value == null) return Optional.empty();

        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            if (identifier == CommandParameterIdentifier.RADIUS) {
                throw new TMCommandException(INVALID_RADIUS, value);
            }

            return Optional.empty();
        }
    }

    public double getDouble(final double defaultValue) {
        return getDouble().orElse(defaultValue);
    }

    public Optional<World> getWorld() {
        if (value == null) return Optional.empty();

        final World world = Bukkit.getWorld(value);

        if (world == null) {
            throw new TMCommandException(INVALID_WORLD, value);
        }

        return Optional.of(world);
    }

    public World getWorld(final World defaultValue) {
        return getWorld().orElse(defaultValue);
    }

    public Optional<BungeeServerInfo> getServer() {
        if (value == null) return Optional.empty();

        final BungeeServerInfo server = TitleManager.getInstance().getBungeeManager().getServers().get(value);

        if (server == null) {
            throw new TMCommandException(INVALID_SERVER, value);
        }

        return Optional.of(server);
    }

    public BungeeServerInfo getServer(final BungeeServerInfo defaultValue) {
        return getServer().orElse(defaultValue);
    }
}
