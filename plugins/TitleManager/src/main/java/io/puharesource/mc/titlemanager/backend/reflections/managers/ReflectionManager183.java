package io.puharesource.mc.titlemanager.backend.reflections.managers;

import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ReflectionManager183 extends ReflectionManager {

    private final Map<String, ReflectionClass> classes;

    public ReflectionManager183() {
        classes = new LinkedHashMap<>();

        try {
            classes.put("ChatComponentText", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"));
            classes.put("IChatBaseComponent", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"));
            classes.put("CraftPlayer", ReflectionType.ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"));
            classes.put("EntityPlayer", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"));
            classes.put("PlayerConnection", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"));
            classes.put("Packet", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("Packet"));
            classes.put("PacketPlayOutTitle", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle"));
            classes.put("PacketPlayOutChat", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"));
            classes.put("PacketPlayOutPlayerListHeaderFooter", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutPlayerListHeaderFooter"));
            classes.put("EnumTitleAction", ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle").getInnerReflectionClass("EnumTitleAction"));
        } catch (Exception e) {
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
