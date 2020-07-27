package io.puharesource.mc.titlemanager.internal.reflections

import io.puharesource.mc.titlemanager.internal.extensions.modify
import io.puharesource.mc.titlemanager.internal.extensions.read
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import java.lang.reflect.Constructor
import java.lang.reflect.Method

abstract class NMSClass(className: String? = null) {
    val provider = NMSManager.getClassProvider()
    val clazz = NMSManager.getClassProvider()[className ?: javaClass.simpleName]

    fun createInstance(vararg objects: Any) = clazz.createInstance(*objects)
}

class CraftPlayer : NMSClass() {
    private val methodGetHandle: Method = clazz.getMethod("getHandle")

    fun getHandle(player: Any): Any? = methodGetHandle.invoke(player)
}

class EntityPlayer : NMSClass() {
    val playerConnection = clazz.getField("playerConnection")
    val ping = clazz.getField("ping")
}

class PlayerConnection : NMSClass() {
    private val classPacket: Class<*> = provider["Packet"].handle
    private val methodSendPacket: Method by lazy { clazz.getMethod("sendPacket", classPacket) }

    val networkManager = clazz.getField("networkManager")

    fun sendPacket(instance: Any, packet: Any): Any? = methodSendPacket.invoke(instance, packet) // NMSVersionIndex <= 2
}

class NetworkManager : NMSClass() {
    private val classPacket: Class<*> = provider["Packet"].handle
    private val methodSendPacket: Method by lazy { clazz.getMethod("sendPacket", classPacket) }
    private val methodGetVersion: Method by lazy { clazz.getMethod("getVersion") }

    fun sendPacket(instance: Any, packet: Any): Any? = methodSendPacket.invoke(instance, packet) // NMSVersionIndex > 2

    fun getVersion(instance: Any) = methodGetVersion.invoke(instance) as Int
}

class PacketPlayOutScoreboardObjective : NMSClass() {
    val nameField = clazz.getField("a") // Objective Name | String | (String | A unique name for the objective)
    val modeField = clazz.getField(if (NMSManager.versionIndex > 0) "d" else "c") // Mode | Byte | (int | 0 to create the scoreboard. 1 to remove the scoreboard. 2 to update the display text.)
    val valueField = clazz.getField("b") // Objective Value | Optional String/IChatComponent | (String | Only if mode is 0 or 2. The text to be displayed for the score)
    val typeField = clazz.getField("c") // Type | Optional String | (EnumScoreboardHealthDisplay | Only if mode is 0 or 2. “integer” or “hearts”)
}

class PacketPlayOutScoreboardDisplayObjective : NMSClass() {
    val positionField = clazz.getField("a") // Position | Byte | (int | The position of the scoreboard. 0: list, 1: sidebar, 2: below name.)
    val nameField = clazz.getField("b") // Score Name | String | (String | The unique name for the scoreboard to be displayed.)
}

class PacketPlayOutScoreboardScore : NMSClass() {
    val scoreNameField = clazz.getField("a") // Score Name | String | (String | The name of the score to be updated or removed)
    val actionField = clazz.getField("d") // Action | Byte | (EnumScoreboardAction | 0 to create/update an item. 1 to remove an item.)
    val objectiveNameField = clazz.getField("b") // Objective Name | String | (String  | The name of the objective the score belongs to)
    val valueField = clazz.getField("c") // Value | Optional VarInt | (int | The score to be displayed next to the entry. Only sent when Action does not equal 1.)
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

class PacketPlayOutScoreboardTeam<T> : NMSClass() {
    companion object {
        const val MODE_TEAM_CREATE = 0
        const val MODE_TEAM_REMOVED = 1
        const val MODE_TEAM_UPDATED = 2
        const val MODE_PLAYERS_ADDED = 3
        const val MODE_PLAYERS_REMOVED = 4
    }

    private val nameField by lazy { clazz.getField("a") }
    private val prefixField by lazy { clazz.getField("c") }
    private val suffixField by lazy { clazz.getField("d") }
    private val playersField by lazy { if (NMSManager.versionIndex > 2) clazz.getField("h") else clazz.getField("g") }
    private val modeField by lazy { if (NMSManager.versionIndex > 2) clazz.getField("i") else clazz.getField("h") }

    val handle: Any = clazz.getConstructor().newInstance()

    var name: String
        get() = nameField.read { nameField.get(handle) as String }
        set(value) = nameField.modify { nameField.set(handle, value) }

    var prefix: T
        get() = prefixField.read { prefixField.get(handle) as T }
        set(value) = prefixField.modify { prefixField.set(handle, value) }

    var suffix: T
        get() = suffixField.read { suffixField.get(handle) as T }
        set(value) = suffixField.modify { suffixField.set(handle, value) }

    var text: T
        get() {
            if (prefix is String) {
                return (prefix as String + suffix as String) as T
            }

            return prefix
        }
        set(value) {
            if (value is String && value.length > 16) {
                prefix = value.substring(0, 16) as T
                suffix = value.substring(16, value.length.coerceAtMost(32)) as T
            } else {
                prefix = value
            }
        }

    var players: List<String>
        get() = playersField.read { playersField.read { playersField.get(handle) as List<String> } }
        set(value) = playersField.modify { playersField.modify { playersField.set(handle, value) } }

    var mode: Int
        get() = modeField.read { modeField.getInt(handle) }
        set(value) = modeField.modify { modeField.setInt(handle, value) }
}

class ChatComponentText : NMSClass() {
    val constructor = clazz.getConstructor(String::class.java)
}

object ChatSerializer : NMSClass() {
    private val deserializeMethod by lazy { clazz.getMethod("a", String::class.java) }

    fun deserializeLegacyText(text: String) = deserialize(*TextComponent.fromLegacyText(text))

    fun deserialize(vararg components: BaseComponent) = deserialize(ComponentSerializer.toString(*components))

    fun deserialize(json: String): Any {
        return deserializeMethod.invoke(null, json)
    }
}
