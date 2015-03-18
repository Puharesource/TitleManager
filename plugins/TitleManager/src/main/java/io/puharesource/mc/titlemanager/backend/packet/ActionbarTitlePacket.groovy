package io.puharesource.mc.titlemanager.backend.packet

import io.puharesource.mc.titlemanager.TitleManager

final class ActionbarTitlePacket extends Packet {
    private Object handle

    ActionbarTitlePacket (String text) {
        def manager = TitleManager.reflectionManager

        handle = manager.classes["PacketPlayOutChat"].createInstance(manager.getIChatBaseComponent(text), (byte) 2)
    }

    @Override
    Object getHandle() {
        return handle
    }
}
