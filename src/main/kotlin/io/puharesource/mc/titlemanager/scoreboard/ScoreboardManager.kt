package io.puharesource.mc.titlemanager.scoreboard

import io.puharesource.mc.titlemanager.extensions.modify
import io.puharesource.mc.titlemanager.reflections.NMSManager
import io.puharesource.mc.titlemanager.reflections.sendNMSPacket
import io.puharesource.mc.titlemanager.scheduling.AsyncScheduler
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

object ScoreboardManager {
    internal val playerScoreboards: MutableMap<Player, ScoreboardRepresentation> = ConcurrentHashMap()
    internal val playerScoreboardUpdateTasks: MutableMap<Player, Int> = ConcurrentHashMap()

    fun startUpdateTask(player: Player) {
        if (playerScoreboards.containsKey(player) && !playerScoreboardUpdateTasks.containsKey(player)) {
            playerScoreboardUpdateTasks[player] = AsyncScheduler.schedule({
                val scoreboard = playerScoreboards[player]

                if (scoreboard == null) {
                    stopUpdateTask(player)
                    return@schedule
                }

                if (!scoreboard.isUpdatePending.get()) {
                    return@schedule
                }

                val currentScoreboardName = scoreboard.name

                scoreboard.generateNewScoreboardName()
                val newScoreboardName = scoreboard.name

                createScoreboardWithName(player, newScoreboardName)
                setScoreboardTitleWithName(player, scoreboard.title, newScoreboardName)

                (1..15).mapNotNull { scoreboard.get(it) }.forEachIndexed { index, text ->
                    setScoreboardValueWithName(player, index, text, newScoreboardName)
                }

                scoreboard.isUpdatePending.set(false)

                displayScoreboardWithName(player, newScoreboardName)

                scoreboard.isUsingPrimaryBoard.set(!scoreboard.isUsingPrimaryBoard.get())

                removeScoreboardWithName(player, currentScoreboardName)
            }, 1, 1)
        }
    }

    fun stopUpdateTask(player: Player) {
        playerScoreboardUpdateTasks[player]?.let {
            AsyncScheduler.cancel(it)
            playerScoreboardUpdateTasks.remove(player)
        }
    }

    fun createScoreboardWithName(player: Player, scoreboardName: String) {
        val provider = NMSManager.getClassProvider()

        val packet = provider.get("PacketPlayOutScoreboardObjective").handle.newInstance()

        val createNameField = packet.javaClass.getDeclaredField("a")  // Objective Name   | String            | (String                       | A unique name for the objective)
        val createModeField = packet.javaClass.getDeclaredField("d")  // Mode             | Byte              | (int                          | 0 to create the scoreboard. 1 to remove the scoreboard. 2 to update the display text.)
        val createValueField = packet.javaClass.getDeclaredField("b") // Objective Value  | Optional String   | (String                       | Only if mode is 0 or 2. The text to be displayed for the score)
        val createTypeField = packet.javaClass.getDeclaredField("c")  // Type             | Optional String   | (EnumScoreboardHealthDisplay  | Only if mode is 0 or 2. “integer” or “hearts”)

        createNameField.modify { set(packet, scoreboardName) }
        createModeField.modify { setInt(packet, 0) }
        createValueField.modify { set(packet, "") }
        createTypeField.modify { set(packet, provider.get("EnumScoreboardHealthDisplay").handle.enumConstants[0]) }

        player.sendNMSPacket(packet)
    }

    fun displayScoreboardWithName(player: Player, scoreboardName: String) {
        val provider = NMSManager.getClassProvider()

        val packet = provider.get("PacketPlayOutScoreboardDisplayObjective").handle.newInstance()

        val displayPositionField = packet.javaClass.getDeclaredField("a")    // Position     | Byte      | (int      | The position of the scoreboard. 0: list, 1: sidebar, 2: below name.)
        val displayNameField = packet.javaClass.getDeclaredField("b")        // Score Name   | String    | (String   | 	The unique name for the scoreboard to be displayed.)

        displayPositionField.modify { setInt(packet, 1) }
        displayNameField.modify { set(packet, scoreboardName) }

        player.sendNMSPacket(packet)
    }

    fun removeScoreboardWithName(player: Player, scoreboardName: String) {
        val provider = NMSManager.getClassProvider()
        val packet = provider.get("PacketPlayOutScoreboardObjective").handle.newInstance()

        val nameField = packet.javaClass.getDeclaredField("a")  // Objective Name   | String    | (String   | A unique name for the objective)
        val modeField = packet.javaClass.getDeclaredField("d")  // Mode             | Byte      | (int      | 0 to create the scoreboard. 1 to remove the scoreboard. 2 to update the display text.)

        nameField.modify { nameField.set(packet, scoreboardName) }
        modeField.modify { modeField.set(packet, 1) }

        player.sendNMSPacket(packet)
    }

    fun setScoreboardTitleWithName(player: Player, title: String, scoreboardName: String) {
        val provider = NMSManager.getClassProvider()
        val packet = provider.get("PacketPlayOutScoreboardObjective").handle.newInstance()

        val nameField = packet.javaClass.getDeclaredField("a")  // Objective Name   | String            | (String                       | A unique name for the objective)
        val modeField = packet.javaClass.getDeclaredField("d")  // Mode             | Byte              | (int                          | 0 to create the scoreboard. 1 to remove the scoreboard. 2 to update the display text.)
        val valueField = packet.javaClass.getDeclaredField("b") // Objective Value  | Optional String   | (String                       | Only if mode is 0 or 2. The text to be displayed for the score)
        val typeField = packet.javaClass.getDeclaredField("c")  // Type             | Optional String   | (EnumScoreboardHealthDisplay  | Only if mode is 0 or 2. “integer” or “hearts”)

        nameField.modify { set(packet, scoreboardName) }
        modeField.modify { setInt(packet, 2) }

        if (title.length > 32) {
            valueField.modify { set(packet, title.substring(0, 32)) }
        } else {
            valueField.modify { set(packet, title) }
        }

        typeField.modify { set(packet, provider.get("EnumScoreboardHealthDisplay").handle.enumConstants[0]) }

        player.sendNMSPacket(packet)
    }

    fun setScoreboardValueWithName(player: Player, index: Int, value: String, scoreboardName: String) {
        val provider = NMSManager.getClassProvider()
        val packet = provider.get("PacketPlayOutScoreboardScore").handle.newInstance()

        val scoreNameField = packet.javaClass.getDeclaredField("a")     // Score Name       | String            | (String               | The name of the score to be updated or removed)
        val actionField = packet.javaClass.getDeclaredField("d")        // Action           | Byte              | (EnumScoreboardAction | 0 to create/update an item. 1 to remove an item.)
        val objectiveNameField = packet.javaClass.getDeclaredField("b") // Objective Name   | String            | (String               | The name of the objective the score belongs to)
        val valueField = packet.javaClass.getDeclaredField("c")         // Value            | Optional VarInt   | (int                  | The score to be displayed next to the entry. Only sent when Action does not equal 1.)

        if (value.length > 40) {
            scoreNameField.modify { set(packet, value.substring(0, 40)) }
        } else {
            scoreNameField.modify { set(packet, value) }
        }

        actionField.modify { set(packet, provider.get("EnumScoreboardAction").handle.enumConstants[0]) }
        objectiveNameField.modify { set(packet, scoreboardName) }
        valueField.modify { setInt(packet, 15 - index) }

        player.sendNMSPacket(packet)
    }
}