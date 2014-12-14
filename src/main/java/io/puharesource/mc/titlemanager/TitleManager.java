package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.TitleObject;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class TitleManager {

    private static Main plugin;
    private static ReflectionManager reflectionManager;

    private static List<Integer> runningAnimations = Collections.synchronizedList(new ArrayList<Integer>());

    public static FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public static void saveConfig() {
        plugin.saveConfig();
    }

    public static Main getPlugin() {
        return plugin;
    }

    public static void load(Main plugin) {
        TitleManager.plugin = plugin;
        try {
            reflectionManager = new ReflectionManager();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            plugin.getLogger().log(Level.SEVERE, "Failed to load NMS classes, please update to the latest version of Spigot! Disabling plugin...");
            plugin.getPluginLoader().disablePlugin(plugin);
        }
    }

    public static ReflectionManager getReflectionManager() {
        return reflectionManager;
    }

    public static TitleObject generateTitleObjectFromArgs(int offset, String[] args) {
        int fadeIn = -1;
        int stay = -1;
        int fadeOut = -1;

        StringBuilder sb = new StringBuilder();
        boolean isReadingTimes = true;
        for (int i = offset; args.length > i; i++) {
            if (isReadingTimes) {
                String lower = args[i].toLowerCase();
                int amount = -1;
                try {
                    amount = Integer.parseInt(lower.replaceAll("\\D", ""));
                } catch (NumberFormatException ignored) {
                }
                if (lower.startsWith("-fadein=")) {
                    if (amount != -1)
                        fadeIn = amount;
                    continue;
                } else if (lower.startsWith("-stay=")) {
                    if (amount != -1)
                        stay = amount;
                    continue;
                } else if (lower.startsWith("-fadeout=")) {
                    if (amount != -1)
                        fadeOut = amount;
                    continue;
                } else {
                    isReadingTimes = false;
                    sb.append(args[i]);
                    continue;
                }
            }
            sb.append(" " + args[i]);
        }

        String title = ChatColor.translateAlternateColorCodes('&', sb.toString());
        String subtitle = null;
        if (title.contains("<nl>")) {
            String[] titles = title.split("<nl>", 2);
            title = titles[0];
            subtitle = titles[1];
        }
        TitleObject object;
        if (subtitle == null)
            object = new TitleObject(title, TitleObject.TitleType.TITLE);
        else object = new TitleObject(title, subtitle);

        if (fadeIn != -1)
            object.setFadeIn(fadeIn);
        else object.setFadeIn(Config.getConfig().getInt("welcome_message.fadeIn"));
        if (stay != -1)
            object.setStay(stay);
        else object.setFadeIn(Config.getConfig().getInt("welcome_message.stay"));
        if (fadeOut != -1)
            object.setFadeOut(fadeOut);
        else object.setFadeIn(Config.getConfig().getInt("welcome_message.fadeOut"));
        return object;
    }

    public static Player getPlayer(String name) {
        Player correctPlayer = null;
        for (Player player : Bukkit.getOnlinePlayers())
            if (StringUtils.containsIgnoreCase(player.getName(), name)) {
                correctPlayer = player;
                break;
            }
        return correctPlayer;
    }

    public static String combineArray(int offset, String[] array) {
        StringBuilder sb = new StringBuilder(array[offset]);
        for (int i = offset + 1; array.length > i; i++)
            sb.append(" " + array[i]);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static void addRunningAnimationId(int id) {
        runningAnimations.add(id);
    }

    public static void removeRunningAnimationId(int id) {
        runningAnimations.remove(id);
    }

    public static List<Integer> getRunningAnimations() {
        return runningAnimations;
    }
}
