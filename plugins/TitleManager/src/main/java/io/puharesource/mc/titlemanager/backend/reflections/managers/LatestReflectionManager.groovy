package io.puharesource.mc.titlemanager.backend.reflections.managers

final class LatestReflectionManager extends SecondReflectionManager {
    public LatestReflectionManager() {
        super()
        classes["EnumTitleAction"] = classes["PacketPlayOutTitle"].getInnerReflectionClass("EnumTitleAction")
    }
}
