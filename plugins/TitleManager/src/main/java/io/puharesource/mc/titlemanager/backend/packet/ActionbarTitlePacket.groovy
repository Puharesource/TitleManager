package io.puharesource.mc.titlemanager.backend.packet

import io.puharesource.mc.titlemanager.TitleManager

final class ActionbarTitlePacket extends Packet {
    private Object handle

    ActionbarTitlePacket (String text) {
        def manager = TitleManager.getInstance().getReflectionManager()

        handle = manager.classes["PacketPlayOutChat"].createInstance(manager.getIChatBaseComponent(text), (byte) 2)
    }

    @Override
    Object getHandle() {
        return handle
    }
}
