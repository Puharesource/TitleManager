package io.puharesource.mc.titlemanager.reflections

import io.puharesource.mc.titlemanager.reflections.NMSType.*

/**
 * Provides classes for Minecraft 1.7 - 1.8 Protocol Hack
 */
object ProviderProtocolHack : NMSClassProvider() {
    init {
        put("ChatComponentText",        NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"))
        put("IChatBaseComponent",       NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"))
        put("CraftPlayer",              ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"))
        put("EntityPlayer",             NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"))
        put("PlayerConnection",         NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"))
        put("Packet",                   NET_MINECRAFT_SERVER.getReflectionClass("Packet"))
        put("PacketPlayOutChat",        NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"))
        put("PacketPlayOutSetSlot",     NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutSetSlot"))
        put("ItemStack",                NET_MINECRAFT_SERVER.getReflectionClass("ItemStack"))
        put("CraftItemStack",           ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("inventory.CraftItemStack"))

        val protocolInjector = ORG_SPIGOTMC.getReflectionClass("ProtocolInjector")
        val packetTitle = protocolInjector.getInnerReflectionClass("PacketTitle")

        put("ProtocolInjector",         protocolInjector)
        put("PacketTitle",              packetTitle)
        put("Action",                   packetTitle.getInnerReflectionClass("Action"))
        put("PacketTabHeader",          protocolInjector.getInnerReflectionClass("PacketTabHeader"))
    }
}

/**
 * Provides classes for Minecraft 1.8 - 1.8.2
 */
object Provider18 : NMSClassProvider() {
    init {
        classes.put("ChatComponentText",                    NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"))
        classes.put("IChatBaseComponent",                   NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"))
        classes.put("CraftPlayer",                          ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"))
        classes.put("EntityPlayer",                         NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"))
        classes.put("PlayerConnection",                     NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"))
        classes.put("Packet",                               NET_MINECRAFT_SERVER.getReflectionClass("Packet"))
        classes.put("PacketPlayOutTitle",                   NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle"))
        classes.put("PacketPlayOutChat",                    NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"))
        classes.put("PacketPlayOutPlayerListHeaderFooter",  NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutPlayerListHeaderFooter"))
        classes.put("EnumTitleAction",                      NET_MINECRAFT_SERVER.getReflectionClass("EnumTitleAction"))
    }
}

/**
 * Provides classes for Minecraft 1.8.3 - 1.9.X
 */
object Provider183 : NMSClassProvider() {
    init {
        classes.put("ChatComponentText",                    NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"))
        classes.put("IChatBaseComponent",                   NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"))
        classes.put("CraftPlayer",                          ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"))
        classes.put("EntityPlayer",                         NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"))
        classes.put("PlayerConnection",                     NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"))
        classes.put("Packet",                               NET_MINECRAFT_SERVER.getReflectionClass("Packet"))
        classes.put("PacketPlayOutTitle",                   NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle"))
        classes.put("PacketPlayOutChat",                    NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"))
        classes.put("PacketPlayOutPlayerListHeaderFooter",  NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutPlayerListHeaderFooter"))
        classes.put("EnumTitleAction",                      NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle").getInnerReflectionClass("EnumTitleAction"))
    }
}

/**
 * Provides classes for Minecraft 1.10 - 1.11
 */
object Provider110 : NMSClassProvider() {
    init {
        classes.put("ChatComponentText",                    NET_MINECRAFT_SERVER.getReflectionClass("ChatComponentText"))
        classes.put("IChatBaseComponent",                   NET_MINECRAFT_SERVER.getReflectionClass("IChatBaseComponent"))
        classes.put("CraftPlayer",                          ORG_BUKKIT_CRAFTBUKKIT.getReflectionClass("entity.CraftPlayer"))
        classes.put("EntityPlayer",                         NET_MINECRAFT_SERVER.getReflectionClass("EntityPlayer"))
        classes.put("PlayerConnection",                     NET_MINECRAFT_SERVER.getReflectionClass("PlayerConnection"))
        classes.put("Packet",                               NET_MINECRAFT_SERVER.getReflectionClass("Packet"))
        classes.put("PacketPlayOutTitle",                   NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle"))
        classes.put("PacketPlayOutChat",                    NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutChat"))
        classes.put("PacketPlayOutPlayerListHeaderFooter",  NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutPlayerListHeaderFooter"))
        classes.put("EnumTitleAction",                      NET_MINECRAFT_SERVER.getReflectionClass("PacketPlayOutTitle").getInnerReflectionClass("EnumTitleAction"))
    }
}