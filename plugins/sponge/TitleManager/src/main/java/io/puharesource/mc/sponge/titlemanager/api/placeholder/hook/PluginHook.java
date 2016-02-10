package io.puharesource.mc.sponge.titlemanager.api.placeholder.hook;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

public abstract class PluginHook {
    private String pluginId;

    public PluginHook(final String pluginId) {
        this.pluginId = pluginId;
    }

    public Optional<PluginContainer> getPlugin() {
        return Sponge.getPluginManager().getPlugin(pluginId);
    }
}
