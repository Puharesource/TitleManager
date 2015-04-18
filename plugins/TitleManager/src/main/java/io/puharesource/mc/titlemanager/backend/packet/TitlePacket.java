package io.puharesource.mc.titlemanager.backend.packet;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public final class TitlePacket extends Packet {
    private Object handle;
    public TitlePacket(TitleObject.TitleType action, String text) {
        this(action, text, -1, -1, -1);
    }

    public TitlePacket(int fadeIn, int stay, int fadeOut) {
        this(TitleObject.TitleType.TIMES, null, fadeIn, stay, fadeOut);
    }

    public TitlePacket(TitleObject.TitleType action, String text, int fadeIn, int stay, int fadeOut) {
        ReflectionManager manager = TitleManager.getInstance().getReflectionManager();

        try {
            Map<String, ReflectionClass> classes = manager.getClasses();

            this.handle = classes.get("PacketPlayOutTitle").
                    getConstructor(
                            classes.get("EnumTitleAction").getHandle(),
                            classes.get("IChatBaseComponent").getHandle(),
                            Integer.TYPE, Integer.TYPE, Integer.TYPE)
                    .newInstance(
                            action.getHandle(),
                            manager.getIChatBaseComponent(text),
                            fadeIn, stay, fadeOut);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getHandle() {
        return handle;
    }
}
