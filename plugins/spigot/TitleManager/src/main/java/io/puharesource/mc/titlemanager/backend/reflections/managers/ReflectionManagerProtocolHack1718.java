package io.puharesource.mc.titlemanager.backend.reflections.managers;

import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is for the ProtocolHack version, to the massive difference in how Titles are handled in the ProtocolHack version
 */
public final class ReflectionManagerProtocolHack1718 extends ReflectionManager {

    private @Getter final Map<String, ReflectionClass> classes;

    public ReflectionManagerProtocolHack1718() {
        classes = new LinkedHashMap<>();

        classes.put("ChatComponentText", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"));
        classes.put("IChatBaseComponent", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"));
        classes.put("CraftPlayer", ReflectionType.ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"));
        classes.put("EntityPlayer", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"));
        classes.put("PlayerConnection", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"));
        classes.put("Packet", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("Packet"));
        classes.put("PacketPlayOutChat", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"));
        classes.put("PacketPlayOutSetSlot", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutSetSlot"));
        classes.put("ItemStack", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("ItemStack"));
        classes.put("CraftItemStack", ReflectionType.ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("inventory.CraftItemStack"));

        ReflectionClass protocolInjector = ReflectionType.ORG_SPIGOTMC.getReflectionClass("ProtocolInjector");
        ReflectionClass packetTitle = protocolInjector.getInnerReflectionClass("PacketTitle");
        classes.put("ProtocolInjector", protocolInjector);
        classes.put("PacketTitle", packetTitle);
        classes.put("Action", packetTitle.getInnerReflectionClass("Action"));
        classes.put("PacketTabHeader", protocolInjector.getInnerReflectionClass("PacketTabHeader"));
    }

    @SneakyThrows
    @Override
    public Object getIChatBaseComponent(final String text) {
        return text == null ? null : classes.get("ChatComponentText").getConstructor(String.class).newInstance(text);
    }
}
