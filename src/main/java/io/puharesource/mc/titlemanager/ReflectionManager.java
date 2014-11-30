package io.puharesource.mc.titlemanager;

import io.puharesource.mc.titlemanager.api.TextConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class ReflectionManager {
    final static Pattern BRAND = Pattern.compile("(v|)[0-9][_.][0-9][_.][R0-9]*");

    final static String CRAFTBUKKIT_PATH = "org.bukkit.craftbukkit.";
    final static String NMS_PATH = "net.minecraft.server.";

    final Class<?> CLASS_ChatSerializer;
    final Class<?> CLASS_IChatBaseComponent;
    final Class<?> CLASS_CraftPlayer;
    final Class<?> CLASS_EntityPlayer;
    final Class<?> CLASS_PacketPlayOutTitle;
    final Class<?> CLASS_PacketPlayOutChat;
    final Class<?> CLASS_PacketPlayOutPlayerListHeaderFooter;
    final Class<?> CLASS_EnumTitleAction;
    final Class<?> CLASS_Packet;

    public ReflectionManager() throws ClassNotFoundException {
        CLASS_ChatSerializer = getNMSClass("ChatSerializer");
        CLASS_IChatBaseComponent = getNMSClass("IChatBaseComponent");
        CLASS_CraftPlayer = getCraftbukkitClass("entity.CraftPlayer");
        CLASS_EntityPlayer = getNMSClass("EntityPlayer");
        CLASS_PacketPlayOutTitle = getNMSClass("PacketPlayOutTitle");
        CLASS_PacketPlayOutChat = getNMSClass("PacketPlayOutChat");
        CLASS_PacketPlayOutPlayerListHeaderFooter = getNMSClass("PacketPlayOutPlayerListHeaderFooter");
        CLASS_EnumTitleAction = getNMSClass("EnumTitleAction");
        CLASS_Packet = getNMSClass("Packet");
    }

    public static Class<?> getCraftbukkitClass(String path) throws ClassNotFoundException {
        return Class.forName(CRAFTBUKKIT_PATH + getServerVersion() + path);
    }

    public static Class<?> getNMSClass(String path) throws ClassNotFoundException {
        return Class.forName(NMS_PATH + getServerVersion() + path);
    }

    public static String getServerVersion() {
        String version;
        String pkg = Bukkit.getServer().getClass().getPackage().getName();
        String version0 = pkg.substring(pkg.lastIndexOf('.') + 1);
        if (!BRAND.matcher(version0).matches())
            version0 = "";
        version = version0;
        return !version.isEmpty() ? version + "." : "";
    }

    public static Method getMethod(String methodName, Class<?> clazz, Class<?>... params) throws NoSuchMethodException {
        main:
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterTypes().length != params.length) continue;
            for (int i = 0; method.getParameterTypes().length > i; i++)
                if (method.getParameterTypes()[i] != params[i]) continue main;
            return method;
        }
        throw new NoSuchMethodException("Couldn't find method \"" + methodName + "\" for " + clazz.getName() + ".");
    }

    public static Method getDeclaredMethod(String methodName, Class<?> clazz, Class<?>... params) throws NoSuchMethodException {
        main:
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterTypes().length != params.length) continue;
            for (int i = 0; method.getParameterTypes().length > i; i++)
                if (method.getParameterTypes()[i] != params[i]) continue main;
            method.setAccessible(true);
            return method;
        }
        throw new NoSuchMethodException("Couldn't find declared method \"" + methodName + "\" for " + clazz.getName() + ".");
    }

    public static Constructor getConstructor(Class<?> clazz, Class<?>... params) throws NoSuchMethodException {
        main:
        for (Constructor constructor : clazz.getConstructors()) {
            if (constructor.getParameterTypes().length != params.length) continue;
            for (int i = 0; constructor.getParameterTypes().length > i; i++)
                if (constructor.getParameterTypes()[i] != params[i]) continue main;
            return constructor;
        }
        throw new NoSuchMethodException("Couldn't find constructor for " + clazz.getName() + ".");
    }

    public Object getIChatBaseComponent(String text) {
        try {
            return getMethod("a", CLASS_ChatSerializer, String.class).invoke(null, TextConverter.convert(text));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getEntityPlayer(Player player) {
        Object craftPlayer = CLASS_CraftPlayer.cast(player);
        try {
            return getMethod("getHandle", CLASS_CraftPlayer).invoke(craftPlayer);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getPlayerConnection(Player player) {
        Object handle = getEntityPlayer(player);
        try {
            return handle.getClass().getField("playerConnection").get(handle);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendPacket(Object packet, Player player) {
        Object connection = getPlayerConnection(player);
        try {
            getMethod("sendPacket", connection.getClass(), CLASS_Packet).invoke(
                    connection,
                    packet);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public Object constructTitleTimingsPacket(int fadeIn, int stay, int fadeOut) {
        try {
            return CLASS_PacketPlayOutTitle.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE)
                    .newInstance(fadeIn, stay, fadeOut);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object constructTitlePacket(boolean isSubtitle, Object iChatBaseComponent) {
        try {
            return getConstructor(CLASS_PacketPlayOutTitle, CLASS_EnumTitleAction, CLASS_IChatBaseComponent).newInstance(CLASS_EnumTitleAction.getEnumConstants()[isSubtitle ? 1 : 0], iChatBaseComponent);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object constructActionbarTitlePacket(Object iChatBaseComponent) {
        try {
            return CLASS_PacketPlayOutChat.getConstructor(CLASS_IChatBaseComponent, Byte.TYPE).newInstance(iChatBaseComponent, (byte) 2);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object constructHeaderAndFooterPacket(Object header, Object footer) {
        try {
            Object packet = CLASS_PacketPlayOutPlayerListHeaderFooter.newInstance();
            if (header != null) {
                Field field = CLASS_PacketPlayOutPlayerListHeaderFooter.getDeclaredField("a");
                field.setAccessible(true);
                field.set(packet, header);
                field.setAccessible(false);
            }
            if (footer != null) {
                Field field = CLASS_PacketPlayOutPlayerListHeaderFooter.getDeclaredField("b");
                field.setAccessible(true);
                field.set(packet, footer);
                field.setAccessible(false);
            }
            return packet;
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
}
