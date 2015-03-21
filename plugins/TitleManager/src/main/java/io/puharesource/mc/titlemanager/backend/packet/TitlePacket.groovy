package io.puharesource.mc.titlemanager.backend.packet
import io.puharesource.mc.titlemanager.TitleManager
import io.puharesource.mc.titlemanager.api.TitleObject

final class TitlePacket extends Packet {

    private TitleObject.TitleType action
    private Object iChatBaseComponent
    private Object handle
    private int fadeIn
    private int stay
    private int fadeOut

    TitlePacket(TitleObject.TitleType action, String text) {
        this(action, text, -1, -1, -1)
    }

    TitlePacket(int fadeIn, int stay, int fadeOut) {
        this(TitleObject.TitleType.TIMES, null, fadeIn, stay, fadeOut)
    }

    TitlePacket(TitleObject.TitleType action, String text, int fadeIn, int stay, int fadeOut) {
        def manager = TitleManager.getInstance().getReflectionManager();

        this.action = action
        this.iChatBaseComponent = iChatBaseComponent
        this.fadeIn = fadeIn
        this.stay = stay
        this.fadeOut = fadeOut

        this.handle = manager.classes["PacketPlayOutTitle"]
                .getConstructor(action.getHandle().class, manager.classes["IChatBaseComponent"].class, Integer.TYPE, Integer.TYPE, Integer.TYPE)
                .newInstance(action.getHandle(), text ?: manager.getIChatBaseComponent(text), fadeIn, stay, fadeOut)
    }

    @Override
    Object getHandle() {
        return handle
    }
}
