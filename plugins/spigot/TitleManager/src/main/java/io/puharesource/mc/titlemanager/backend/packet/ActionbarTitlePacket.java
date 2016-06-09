package io.puharesource.mc.titlemanager.backend.packet;

import java.util.Map;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import io.puharesource.mc.titlemanager.backend.reflections.managers.ReflectionManagerProtocolHack1718;
import lombok.Getter;
import lombok.SneakyThrows;

public final class ActionbarTitlePacket extends Packet {
    @Getter private Object handle;
    @Getter private String text;

    @SneakyThrows
    public ActionbarTitlePacket(final String text) {
        this.text = text;
        final ReflectionManager manager = TitleManager.getInstance().getReflectionManager();
        final Map<String, ReflectionClass> classes = manager.getClasses();

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
    }
}
