package io.puharesource.mc.titlemanager.backend.packet

import io.puharesource.mc.titlemanager.TitleManager

import java.lang.reflect.Field

final class TabmenuPacket extends Packet {

    private Object handle

    public TabmenuPacket(String header, String footer) {
        def manager = TitleManager.reflectionManager

        handle = manager.classes["PacketPlayOutPlayerListHeaderFooter"].handle.newInstance()
        if (header != null) {
            Field field = manager.classes["PacketPlayOutPlayerListHeaderFooter"].handle.getDeclaredField("a")
            field.setAccessible(true)
            field.set(handle, header)
            field.setAccessible(false)
        }

        if (footer != null) {
            Field field = manager.classes["PacketPlayOutPlayerListHeaderFooter"].handle.getDeclaredField("b")
            field.setAccessible(true)
            field.set(handle, footer)
            field.setAccessible(false)
        }
    }

    @Override
    Object getHandle() {
        return handle
    }
}
