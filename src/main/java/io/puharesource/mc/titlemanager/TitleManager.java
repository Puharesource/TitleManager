package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.TabTitleChangeEvent;
import io.puharesource.mc.titlemanager.api.TitleEvent;
import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.IChatBaseComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.spigotmc.ProtocolInjector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class TitleManager {

    private static Main plugin;

    private static boolean usingConfig;
    private static String header;
    private static String footer;

    private static String title;
    private static String substring;

    private static Map<UUID, TitleObject> playerTitles = new HashMap<>();

    public static void loadConfig() {
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdir();
        File config = new File(plugin.getDataFolder(), "config.yml");

        try {
            if (!config.exists()) Files.copy(plugin.getResource("config.yml"), config.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        plugin.getConfig();
        plugin.saveConfig();
        reloadConfig();
    }

    public static void reloadConfig() {
        plugin.reloadConfig();
        usingConfig = getConfig().getBoolean("usingConfig");
        header = serializeJson(ChatColor.translateAlternateColorCodes('&', getConfig().getString("header")));
        footer = serializeJson(ChatColor.translateAlternateColorCodes('&', getConfig().getString("footer")));

        title = serializeJson(ChatColor.translateAlternateColorCodes('&', getConfig().getString("title")));
        substring = serializeJson(ChatColor.translateAlternateColorCodes('&', getConfig().getString("subtitle")));
    }

    public static FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public static void saveConfig() {
        plugin.saveConfig();
    }

    public static Main getPlugin() {
        return plugin;
    }

    public static void setPlugin(Main plugin) {
        TitleManager.plugin = plugin;
    }

    public static boolean isUsingConfig() {
        return usingConfig;
    }

    public static String getHeader() {
        return header;
    }

    public static String getFooter() {
        return footer;
    }

    public static void setHeader(Player player, String header, TabTitleChangeEvent.ChangeReason reason) {
        executeTabEvent(player, new TitleObject(header, null), reason, Position.HEADER);
    }

    public static void setFooter(Player player, String footer, TabTitleChangeEvent.ChangeReason reason) {
        executeTabEvent(player, new TitleObject(null, footer), reason, Position.FOOTER);
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String title) {
        TitleManager.title = title;
    }

    public static String getSubstring() {
        return substring;
    }

    public static void setSubstring(String substring) {
        TitleManager.substring = substring;
    }

    public static void setHeaderAndFooter(Player player, String header, String footer, TabTitleChangeEvent.ChangeReason reason) {
        executeTabEvent(player, new TitleObject(header, footer), reason, Position.BOTH);
    }

    public static void sendTitle(Player player, String title, String subtitle) {
        final TitleEvent event = new TitleEvent(player, title, subtitle);

        if(!event.isCancelled()) {
            CraftPlayer craftPlayer = (CraftPlayer) event.getPlayer();
            if(craftPlayer.getHandle().playerConnection.networkManager.getVersion() == 47) {
                ProtocolInjector.PacketTitle packet = new ProtocolInjector.PacketTitle(ProtocolInjector.PacketTitle.Action.TITLE, ChatSerializer.a(title));
                ProtocolInjector.PacketTitle packet1 = new ProtocolInjector.PacketTitle(ProtocolInjector.PacketTitle.Action.SUBTITLE, ChatSerializer.a(subtitle));

                craftPlayer.getHandle().playerConnection.sendPacket(packet);
                craftPlayer.getHandle().playerConnection.sendPacket(packet1);
            }
        }
    }

    private static void executeTabEvent(Player player, TitleObject titleObject, TabTitleChangeEvent.ChangeReason reason, Position position) {
        if (position == Position.HEADER) {
            TitleObject titleObject1 = playerTitles.get(player.getUniqueId());
            if (titleObject1 != null)
                titleObject.setFooter(titleObject1.getFooter());
        } else if (position == Position.FOOTER) {
            TitleObject titleObject1 = playerTitles.get(player.getUniqueId());
            if (titleObject1 != null)
                titleObject.setHeader(titleObject1.getHeader());
        }

        final TabTitleChangeEvent event = new TabTitleChangeEvent(player, titleObject, reason);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            if(craftPlayer.getHandle().playerConnection.networkManager.getVersion() == 47) {
                playerTitles.put(event.getPlayer().getUniqueId(), event.getTitleObject());
                IChatBaseComponent headerComponent = ChatSerializer.a(event.getTitleObject().getHeader());
                IChatBaseComponent footerComponent = ChatSerializer.a(event.getTitleObject().getFooter());

                ProtocolInjector.PacketTabHeader packet = new ProtocolInjector.PacketTabHeader(headerComponent, footerComponent);
                craftPlayer.getHandle().playerConnection.sendPacket(packet);
            }
        }
    }

    static String serializeJson(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char         c;
        int          i;
        int          len = string.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String       t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            c = string.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    enum Position {HEADER, FOOTER, BOTH}
}
