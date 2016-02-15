package io.puharesource.mc.sponge.titlemanager.config.configs;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.config.Config;
import org.spongepowered.api.config.ConfigDir;

import java.io.File;
import java.nio.file.Path;

public final class ConfigMessages implements Config {
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    @Override public Path getConfigPath() { return new File(configDir.toFile(), "messages.conf").toPath(); }
}
