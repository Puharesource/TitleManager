package io.puharesource.mc.titlemanager.backend.packet;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;

import java.lang.reflect.InvocationTargetException;

public final class TitlePacket extends Packet {
    private TitleObject.TitleType action;
    private Object iChatBaseComponent;
    private Object handle;
    private int fadeIn;
    private int stay;
    private int fadeOut;

    public TitlePacket(TitleObject.TitleType action, String text) {
        this(action, text, -1, -1, -1);
    }

    public TitlePacket(int fadeIn, int stay, int fadeOut) {
        this(TitleObject.TitleType.TIMES, null, fadeIn, stay, fadeOut);
    }

    public TitlePacket(TitleObject.TitleType action, String text, int fadeIn, int stay, int fadeOut) {
        ReflectionManager manager = TitleManager.getInstance().getReflectionManager();

        this.action = action;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;

        try {
            this.handle = manager.getClasses().get("PacketPlayOutTitle").createInstance(
                        action.getHandle(),
                        text == null ? manager.getIChatBaseComponent("") : manager.getIChatBaseComponent(text),
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
