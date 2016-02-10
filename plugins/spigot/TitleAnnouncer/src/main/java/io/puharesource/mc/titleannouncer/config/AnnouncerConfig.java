package io.puharesource.mc.titleannouncer.config;

import io.puharesource.mc.titlemanager.backend.config.ConfigField;

import java.util.Arrays;
import java.util.List;

public final class AnnouncerConfig {
    @ConfigField(path = "config-version")
    public int configVersion = 1;

    @ConfigField(path = "settings.hovering.interval")
    public int hoverInterval = 120;

    @ConfigField(path = "settings.hovering.fade-in")
    public int hoverFadeIn = 10;

    @ConfigField(path = "settings.hovering.stay")
    public int hoverStay = 40;

    @ConfigField(path = "settings.hovering.fade-out")
    public int hoverFadeOut = 10;

    @ConfigField(path = "settings.actionbar.interval")
    public int actionbarInterval = 45;

    @ConfigField(path = "messages.hovering")
    public List<String> hoverMessages = Arrays.asList("&2Some title\\n&aSome subtitle", "&2Another title\\n&aAnother subtitle");

    @ConfigField(path = "messages.actionbar")
    public List<String> actionbarMessages = Arrays.asList("&a&lSome actionbar title", "&a&lAnother actionbar title");
}
