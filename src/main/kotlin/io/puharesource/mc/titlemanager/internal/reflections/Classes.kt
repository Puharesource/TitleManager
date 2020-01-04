package io.puharesource.mc.titlemanager.internal.reflections

import java.lang.reflect.Constructor
import java.lang.reflect.Method

abstract class NMSClass(className: String? = null) {
    val provider = NMSManager.getClassProvider()
    val clazz = NMSManager.getClassProvider()[className ?: javaClass.simpleName]

    fun createInstance(vararg objects: Any) = clazz.createInstance(*objects)
}

class CraftPlayer : NMSClass() {
    private val methodGetHandle: Method = clazz.getMethod("getHandle")

    fun getHandle(player: Any) : Any = methodGetHandle.invoke(player)
}

class EntityPlayer : NMSClass() {
    val playerConnection = clazz.getField("playerConnection")
    val ping = clazz.getField("ping")
}

class PlayerConnection : NMSClass() {
    private val classPacket: Class<*> = provider["Packet"].handle
    private val methodSendPacket: Method by lazy { clazz.getMethod("sendPacket", classPacket) }

    val networkManager = clazz.getField("networkManager")

    fun sendPacket(instance: Any, packet: Any) : Any = methodSendPacket.invoke(instance, packet) // NMSVersionIndex <= 2
}

class NetworkManager : NMSClass() {
    private val classPacket: Class<*> = provider["Packet"].handle
    private val methodSendPacket: Method by lazy { clazz.getMethod("sendPacket", classPacket) }
    private val methodGetVersion: Method by lazy { clazz.getMethod("getVersion") }

    fun sendPacket(instance: Any, packet: Any) : Any = methodSendPacket.invoke(instance, packet) // NMSVersionIndex > 2

    fun getVersion(instance: Any) = methodGetVersion.invoke(instance) as Int
}

class PacketPlayOutScoreboardObjective : NMSClass() {
    val nameField = clazz.getField("a")                                 // Objective Name   | String            | (String                       | A unique name for the objective)
    val modeField = clazz.getField(if (NMSManager.versionIndex > 0) "d" else "c") // Mode             | Byte              | (int                          | 0 to create the scoreboard. 1 to remove the scoreboard. 2 to update the display text.)
    val valueField = clazz.getField("b")                                // Objective Value  | Optional String/IChatComponent   | (String                       | Only if mode is 0 or 2. The text to be displayed for the score)
    val typeField = clazz.getField("c")                                 // Type             | Optional String   | (EnumScoreboardHealthDisplay  | Only if mode is 0 or 2. “integer” or “hearts”)
}

class PacketPlayOutScoreboardDisplayObjective : NMSClass() {
    val positionField = clazz.getField("a")    // Position     | Byte      | (int      | The position of the scoreboard. 0: list, 1: sidebar, 2: below name.)
    val nameField = clazz.getField("b")        // Score Name   | String    | (String   | 	The unique name for the scoreboard to be displayed.)
}

class PacketPlayOutScoreboardScore : NMSClass() {
    val scoreNameField = clazz.getField("a")     // Score Name       | String            | (String               | The name of the score to be updated or removed)
    val actionField = clazz.getField("d")        // Action           | Byte              | (EnumScoreboardAction | 0 to create/update an item. 1 to remove an item.)
    val objectiveNameField = clazz.getField("b") // Objective Name   | String            | (String               | The name of the objective the score belongs to)
    val valueField = clazz.getField("c")         // Value            | Optional VarInt   | (int                  | The score to be displayed next to the entry. Only sent when Action does not equal 1.)
}

class PacketTabHeader : NMSClass(if (NMSManager.versionIndex == 0) "PacketTabHeader" else "PacketPlayOutPlayerListHeaderFooter") {
    val legacyConstructor: Constructor<*> by lazy { clazz.getConstructor(provider["IChatBaseComponent"].handle, provider["IChatBaseComponent"].handle) }

    val headerField = clazz.getField(if (NMSManager.versionIndex < 8) "a" else "header")
    val footerField = clazz.getField(if (NMSManager.versionIndex < 8) "b" else "footer")
}

class PacketTitle : NMSClass(if (NMSManager.versionIndex == 0) "PacketTitle" else "PacketPlayOutTitle") {
    val constructor: Constructor<*> = if (NMSManager.versionIndex == 0) {
        clazz.getConstructor(
                provider["Action"].handle,
                provider["IChatBaseComponent"].handle,
                Integer.TYPE, Integer.TYPE, Integer.TYPE)
    } else {
        clazz.getConstructor(
                provider["EnumTitleAction"].handle,
                provider["IChatBaseComponent"].handle,
                Integer.TYPE, Integer.TYPE, Integer.TYPE)
    }

    val timingsConstructor: Constructor<*> = if (NMSManager.versionIndex == 0) {
        clazz.getConstructor(
                provider["Action"].handle,
                Integer.TYPE, Integer.TYPE, Integer.TYPE)
    } else {
        this.constructor
    }
}

class PacketPlayOutChat : NMSClass() {
    val constructor = if (NMSManager.versionIndex == 0) {
        clazz.getConstructor(provider["IChatBaseComponent"].handle, Integer.TYPE)
    } else {
        clazz.getConstructor(provider["IChatBaseComponent"].handle, Byte::class.java)
    }
}

class ChatComponentText : NMSClass() {
    val constructor = clazz.getConstructor(String::class.java)
}