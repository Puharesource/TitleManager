package io.puharesource.mc.titlemanager.backend.packet;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import io.puharesource.mc.titlemanager.backend.reflections.managers.ReflectionManagerProtocolHack1718;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public final class ActionbarTitlePacket extends Packet {
    private Object handle;

    public ActionbarTitlePacket(String text) {
        ReflectionManager manager = TitleManager.getInstance().getReflectionManager();

        try {
            Map<String, ReflectionClass> classes = manager.getClasses();

            if (manager instanceof ReflectionManagerProtocolHack1718) {
                try {
                    handle = classes.get("PacketPlayOutChat").
                            getConstructor(classes.get("IChatBaseComponent").getHandle(), Integer.TYPE)
                            .newInstance(manager.getIChatBaseComponent(text), 2);
                } catch (NoSuchMethodException e) {
                    System.out.println("(If you're using Spigot #1649) Your version of Spigot #1649 doesn't support actionbar messages. Please find that spigot version from another source!");
                }
            } else {
                handle = classes.get("PacketPlayOutChat").
                        getConstructor(classes.get("IChatBaseComponent").getHandle(), Byte.TYPE)
                        .newInstance(manager.getIChatBaseComponent(text), (byte) 2);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getHandle() {
        return handle;
    }
}
