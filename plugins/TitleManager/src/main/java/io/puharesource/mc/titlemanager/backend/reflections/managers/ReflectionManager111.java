package io.puharesource.mc.titlemanager.backend.reflections.managers;

import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager.ReflectionType.NET_MINECRAFT_SERVER;
import static io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager.ReflectionType.ORG_BUKKIT_CRAFTBUKKIT;

public final class ReflectionManager111 extends ReflectionManager {

    @Getter private final Map<String, ReflectionClass> classes;

    public ReflectionManager111() {
        classes = new LinkedHashMap<>();

        classes.put("ChatComponentText", NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"));
        classes.put("IChatBaseComponent", NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"));
        classes.put("CraftPlayer", ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"));
        classes.put("EntityPlayer", NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"));
        classes.put("PlayerConnection", NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"));
        classes.put("Packet", NET_MINECRAFT_SERVER.getReflectionClass("Packet"));
        classes.put("PacketPlayOutTitle", NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle"));
        classes.put("PacketPlayOutChat", NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"));
        classes.put("PacketPlayOutPlayerListHeaderFooter", NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutPlayerListHeaderFooter"));
        classes.put("EnumTitleAction", NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle").getInnerReflectionClass("EnumTitleAction"));
    }

    @SneakyThrows
    @Override
    public Object getIChatBaseComponent(final String text) {
        return text == null ? null : classes.get("ChatComponentText")
                .getConstructor(String.class)
                .newInstance(text);
    }
}