package io.puharesource.mc.titlemanager.backend.packet;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;

import java.lang.reflect.Field;

public final class TabmenuPacket extends Packet {
    public TabmenuPacket(String header, String footer) {
        ReflectionManager manager = TitleManager.getInstance().getReflectionManager();

        try {
            handle = manager.getClasses().get("PacketPlayOutPlayerListHeaderFooter").getHandle().newInstance();

            if (header != null) {
                Field field = manager.getClasses().get("PacketPlayOutPlayerListHeaderFooter").getHandle().getDeclaredField("a");
                field.setAccessible(true);
                field.set(handle, TitleManager.getInstance().getReflectionManager().getIChatBaseComponent(header));
                field.setAccessible(false);
            }


            if (footer != null) {
                Field field = manager.getClasses().get("PacketPlayOutPlayerListHeaderFooter").getHandle().getDeclaredField("b");
                field.setAccessible(true);
                field.set(handle, TitleManager.getInstance().getReflectionManager().getIChatBaseComponent(footer));
                field.setAccessible(false);
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getHandle() {
        return handle;
    }

    private Object handle;
}
