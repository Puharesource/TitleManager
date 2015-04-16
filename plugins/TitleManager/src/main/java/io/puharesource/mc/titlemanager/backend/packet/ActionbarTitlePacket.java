package io.puharesource.mc.titlemanager.backend.packet;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;

import java.lang.reflect.InvocationTargetException;

public final class ActionbarTitlePacket extends Packet {
    public ActionbarTitlePacket(String text) {
        ReflectionManager manager = TitleManager.getInstance().getReflectionManager();

        try {
            handle = manager.getClasses().get("PacketPlayOutChat").createInstance(manager.getIChatBaseComponent(text), (byte) 2);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getHandle() {
        return handle;
    }

    private Object handle;
}
