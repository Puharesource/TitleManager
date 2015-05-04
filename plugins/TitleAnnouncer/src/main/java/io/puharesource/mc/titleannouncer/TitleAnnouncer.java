package io.puharesource.mc.titleannouncer;

import io.puharesource.mc.titleannouncer.commands.CommandTa;
import io.puharesource.mc.titleannouncer.config.AnnouncerConfig;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.backend.config.ConfigFile;
import io.puharesource.mc.titlemanager.backend.config.ConfigSerializer;
import io.puharesource.mc.titlemanager.backend.utils.MiscellaneousUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public final class TitleAnnouncer extends JavaPlugin {
    private ConfigFile configFile;
    private AnnouncerConfig config;

    private final List<ITitleObject> hoveringMessages = new ArrayList<>();
    private final List<IActionbarObject> actionbarMessages = new ArrayList<>();

    private int hoveringId = -1;
    private int actionbarId = -1;

    public void onEnable() {
        configFile = new ConfigFile(this, getDataFolder(), "config", false);
        getCommand("ta").setExecutor(new CommandTa(this));

        reload();
    }

    public void reload() {
        if (!hoveringMessages.isEmpty())
            hoveringMessages.clear();
        if (!actionbarMessages.isEmpty())
            actionbarMessages.clear();

        if (hoveringId != -1)
            Bukkit.getScheduler().cancelTask(hoveringId);
        if (actionbarId != -1)
            Bukkit.getScheduler().cancelTask(actionbarId);

        hoveringId = -1;
        actionbarId = -1;

        configFile.reload();

        try {
            ConfigSerializer.saveDefaults(AnnouncerConfig.class, configFile.getFile(), false);
            config = ConfigSerializer.deserialize(AnnouncerConfig.class, configFile.getFile());
        } catch (IllegalAccessException | InvocationTargetException | IOException | InstantiationException e) {
            e.printStackTrace();
        }

        int hoveringInterval = config.hoverInterval * 20;
        int actionbarInterval = config.actionbarInterval * 20;

        for (String message : config.hoverMessages) {
            message = MiscellaneousUtils.format(message);
            ITitleObject object;
            if (message.contains("\\n")) {
                String[] messages = message.replace("\\n", "\n").split("\n", 2);

                object = MiscellaneousUtils.generateTitleObject(messages[0], messages[1], config.hoverFadeIn, config.hoverStay, config.hoverFadeOut);
            } else object = MiscellaneousUtils.generateTitleObject(message, "", config.hoverFadeIn, config.hoverStay, config.hoverFadeOut);

            hoveringMessages.add(object);
        }

        for (String message : config.actionbarMessages) {
            actionbarMessages.add(MiscellaneousUtils.generateActionbarObject(message));
        }

        if (!hoveringMessages.isEmpty()) {
            hoveringId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
                int i = 0;

                @Override
                public void run() {
                    hoveringMessages.get(i).broadcast();
                    i = (hoveringMessages.size() - 1 > i) ? i++ : 0;
                }
            }, hoveringInterval, hoveringInterval).getTaskId();
        }

        if (!actionbarMessages.isEmpty()) {
            actionbarId = Bukkit.getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
                int i = 0;

                @Override
                public void run() {
                    actionbarMessages.get(i).broadcast();
                    i = (hoveringMessages.size() - 1 > i) ? i++ : 0;
                }
            }, actionbarInterval, actionbarInterval).getTaskId();
        }
    }
}