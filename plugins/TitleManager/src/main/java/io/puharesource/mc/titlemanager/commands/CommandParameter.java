package io.puharesource.mc.titlemanager.commands;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.bungee.BungeeServerInfo;
import org.bukkit.Bukkit;
import org.bukkit.World;

public final class CommandParameter {
    private final String param;
    private final String value;

    public CommandParameter(final String param, final String value) {
        this.param = param;
        this.value = value;
    }

    public String getParameter() {
        return param;
    }

    public String getValue() {
        return value;
    }

    public String getValue(final String defaultValue) {
        return value == null ? defaultValue : value;
    }

    public int getInt() throws NumberFormatException {
        return Integer.parseInt(value);
    }

    public int getInt(final int defaultValue) {
        try {
            return getInt();
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble() throws NumberFormatException {
        return Double.parseDouble(value);
    }

    public double getDouble(final double defaultValue) {
        try {
            return getDouble();
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public World getWorld() {
        return value == null ? null : Bukkit.getWorld(value);
    }

    public World getWorld(final World defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            final World world = Bukkit.getWorld(value);
            return world == null ? defaultValue : world;
        }
    }

    public BungeeServerInfo getServer() {
        return value == null ? null : TitleManager.getInstance().getBungeeManager().getServers().get(value);
    }

    public BungeeServerInfo getServer(final BungeeServerInfo defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            final BungeeServerInfo server = TitleManager.getInstance().getBungeeManager().getServers().get(value);
            return server == null ? defaultValue : server;
        }
    }
}
