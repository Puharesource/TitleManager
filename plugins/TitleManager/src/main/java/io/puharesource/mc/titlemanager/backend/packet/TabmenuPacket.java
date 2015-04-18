package io.puharesource.mc.titlemanager.backend.packet;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import io.puharesource.mc.titlemanager.backend.reflections.managers.ReflectionManagerProtocolHack1718;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public final class TabmenuPacket extends Packet {
    public TabmenuPacket(String header, String footer) {
        ReflectionManager manager = TitleManager.getInstance().getReflectionManager();
        Map<String, ReflectionClass> classes = manager.getClasses();

        try {
            if (manager instanceof ReflectionManagerProtocolHack1718) {
                handle = classes.get("PacketTabHeader").
                        getConstructor(classes.get("IChatBaseComponent").getHandle(), classes.get("IChatBaseComponent").getHandle()).
                        newInstance(manager.getIChatBaseComponent(header), manager.getIChatBaseComponent(footer));
            } else {
                handle = classes.get("PacketPlayOutPlayerListHeaderFooter").getHandle().newInstance();

                if (header != null) {
                    Field field = classes.get("PacketPlayOutPlayerListHeaderFooter").getHandle().getDeclaredField("a");
                    field.setAccessible(true);
                    field.set(handle, manager.getIChatBaseComponent(header));
                    field.setAccessible(false);
                }


                if (footer != null) {
                    Field field = classes.get("PacketPlayOutPlayerListHeaderFooter").getHandle().getDeclaredField("b");
                    field.setAccessible(true);
                    field.set(handle, manager.getIChatBaseComponent(footer));
                    field.setAccessible(false);
                }
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getHandle() {
        return handle;
    }

    private Object handle;
}
