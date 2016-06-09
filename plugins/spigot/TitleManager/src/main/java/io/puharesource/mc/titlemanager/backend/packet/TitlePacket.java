package io.puharesource.mc.titlemanager.backend.packet;

import java.util.Map;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import io.puharesource.mc.titlemanager.backend.reflections.managers.ReflectionManagerProtocolHack1718;
import lombok.Getter;
import lombok.SneakyThrows;

public final class TitlePacket extends Packet {
    @Getter private Object handle;
    public TitlePacket(TitleObject.TitleType action, String text) {
        this(action, text, -1, -1, -1);
    }

    public TitlePacket(int fadeIn, int stay, int fadeOut) {
        this(TitleObject.TitleType.TIMES, null, fadeIn, stay, fadeOut);
    }

    @SneakyThrows
    public TitlePacket(TitleObject.TitleType action, String text, int fadeIn, int stay, int fadeOut) {
        final ReflectionManager manager = TitleManager.getInstance().getReflectionManager();
        final Map<String, ReflectionClass> classes = manager.getClasses();

        if (manager instanceof ReflectionManagerProtocolHack1718) {
            ReflectionClass packetTitle = classes.get("PacketTitle");

            if (text == null) {
                handle = packetTitle.getConstructor(
                        classes.get("Action").getHandle(),
                        Integer.TYPE, Integer.TYPE, Integer.TYPE).
                        newInstance(action.getHandle(), fadeIn, stay, fadeOut);
            } else {
                handle = packetTitle.getConstructor(
                        classes.get("Action").getHandle(),
                        classes.get("IChatBaseComponent").getHandle()).
                        newInstance(action.getHandle(), manager.getIChatBaseComponent(text));
            }
        } else {
            handle = classes.get("PacketPlayOutTitle").
                    getConstructor(
                            classes.get("EnumTitleAction").getHandle(),
                            classes.get("IChatBaseComponent").getHandle(),
                            Integer.TYPE, Integer.TYPE, Integer.TYPE)
                    .newInstance(
                            action.getHandle(),
                            manager.getIChatBaseComponent(text),
                            fadeIn, stay, fadeOut);
        }
    }
}
