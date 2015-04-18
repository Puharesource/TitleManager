package io.puharesource.mc.titlemanager.backend.reflections.managers;

import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is for the ProtocolHack version, to the massive difference in how Titles are handled in the ProtocolHack version
 */
public final class ReflectionManagerProtocolHack1718 extends ReflectionManager {

    private final Map<String, ReflectionClass> classes;

    public ReflectionManagerProtocolHack1718() {
        classes = new LinkedHashMap<>();

        classes.put("ChatComponentText", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"));
        classes.put("IChatBaseComponent", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"));
        classes.put("CraftPlayer", ReflectionType.ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"));
        classes.put("EntityPlayer", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"));
        classes.put("PlayerConnection", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"));
        classes.put("Packet", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("Packet"));
        classes.put("PacketPlayOutChat", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"));
        try {
            ReflectionClass protocolInjector = ReflectionType.ORG_SPIGOTMC.getReflectionClass("ProtocolInjector");
            ReflectionClass packetTitle = protocolInjector.getInnerReflectionClass("PacketTitle");
            classes.put("ProtocolInjector", protocolInjector);
            classes.put("PacketTitle", packetTitle);
            classes.put("Action", packetTitle.getInnerReflectionClass("Action"));
            classes.put("PacketTabHeader", protocolInjector.getInnerReflectionClass("PacketTabHeader"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, ReflectionClass> getClasses() {
        return classes;
    }

    @Override
    public Object getIChatBaseComponent(String text) {
        try {
            return text == null ? null : classes.get("ChatComponentText").getConstructor(String.class).newInstance(text);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
