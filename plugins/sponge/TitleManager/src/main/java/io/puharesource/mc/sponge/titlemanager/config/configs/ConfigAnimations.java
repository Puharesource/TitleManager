package io.puharesource.mc.sponge.titlemanager.config.configs;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.sponge.titlemanager.config.Config;
import io.puharesource.mc.sponge.titlemanager.config.ConfigField;
import org.spongepowered.api.config.ConfigDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public final class ConfigAnimations implements Config {
    @Inject @ConfigDir(sharedRoot = false) private Path configDir;
    @Override public Path getConfigPath() { return new File(configDir.toFile(), "animations.conf").toPath(); }

    @ConfigField(path = "animations")
    public Map<String, FrameSequence> animations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
}
