package io.puharesource.mc.titlemanager.backend.reflections.managers
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager

class SecondReflectionManager extends ReflectionManager {

    protected Map<String, ReflectionClass> classes

    SecondReflectionManager() {
        classes = [
                //Text
                ChatComponentText : ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"),
                IChatBaseComponent : ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"),

                //Player
                CraftPlayer : ReflectionType.ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"),
                EntityPlayer : ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"),
                PlayerConnection : ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"),

                //Packet
                Packet : ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("Packet"),
                PacketPlayOutTitle : ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle"),
                PacketPlayOutChat : ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"),
                PacketPlayOutPlayerListHeaderFooter : ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutPlayerListHeaderFooter"),
                EnumTitleAction : ReflectionType.NET_MINECRAFT_SERVER.getReflectionClass("EnumTitleAction")
        ]
    }

    Object getIChatBaseComponent(String text) {
        return classes["ChatComponentText"].getConstructor(String.class).newInstance(text)
    }

    @Override
    Map<String, ReflectionClass> getClasses() { classes }
}
