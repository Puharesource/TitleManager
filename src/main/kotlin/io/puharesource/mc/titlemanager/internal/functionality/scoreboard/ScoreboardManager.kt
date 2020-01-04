package io.puharesource.mc.titlemanager.internal.functionality.scoreboard

import io.puharesource.mc.titlemanager.internal.APIProvider
import io.puharesource.mc.titlemanager.internal.extensions.modify
import io.puharesource.mc.titlemanager.internal.reflections.NMSClassProvider
import io.puharesource.mc.titlemanager.internal.reflections.NMSManager
import io.puharesource.mc.titlemanager.internal.reflections.PacketPlayOutScoreboardDisplayObjective
import io.puharesource.mc.titlemanager.internal.reflections.PacketPlayOutScoreboardObjective
import io.puharesource.mc.titlemanager.internal.reflections.PacketPlayOutScoreboardScore
import io.puharesource.mc.titlemanager.internal.reflections.sendNMSPacket
import io.puharesource.mc.titlemanager.internal.scheduling.AsyncScheduler
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

object ScoreboardManager {
    private val classPacketPlayOutScoreboardObjective = PacketPlayOutScoreboardObjective()
    private val classPacketPlayOutScoreboardDisplayObjective = PacketPlayOutScoreboardDisplayObjective()
    private val classPacketPlayOutScoreboardScore = PacketPlayOutScoreboardScore()

    private val provider: NMSClassProvider
        get() = NMSManager.getClassProvider()

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
                    setScoreboardValueWithName(player, index + (15 - scoreboard.size) + 1, text, newScoreboardName)
                }

                scoreboard.isUpdatePending.set(false)

                displayScoreboardWithName(player, newScoreboardName)
                removeScoreboardWithName(player, currentScoreboardName)
            }, 1, 1)
        }
    }

    fun stopUpdateTask(player: Player) {
        playerScoreboardUpdateTasks.remove(player)?.let {
            AsyncScheduler.cancel(it)
        }
    }

    fun createScoreboardWithName(player: Player, scoreboardName: String) {
        val packet = classPacketPlayOutScoreboardObjective.createInstance()

        classPacketPlayOutScoreboardObjective.nameField.modify { set(packet, scoreboardName) }
        classPacketPlayOutScoreboardObjective.modeField.modify { setInt(packet, 0) }

        if (NMSManager.versionIndex > 6) {
            classPacketPlayOutScoreboardObjective.valueField.modify { set(packet, NMSManager.getClassProvider().getIChatComponent("")) }
        } else {
            classPacketPlayOutScoreboardObjective.valueField.modify { set(packet, "") }
        }

        if (NMSManager.versionIndex > 0) {
            classPacketPlayOutScoreboardObjective.typeField.modify { set(packet, provider["EnumScoreboardHealthDisplay"].handle.enumConstants[0]) }
        }

        player.sendNMSPacket(packet)
    }

    fun displayScoreboardWithName(player: Player, scoreboardName: String) {
        val packet = classPacketPlayOutScoreboardDisplayObjective.createInstance()

        classPacketPlayOutScoreboardDisplayObjective.positionField.modify { setInt(packet, 1) }
        classPacketPlayOutScoreboardDisplayObjective.nameField.modify { set(packet, scoreboardName) }

        player.sendNMSPacket(packet)
    }

    fun removeScoreboardWithName(player: Player, scoreboardName: String) {
        val packet = classPacketPlayOutScoreboardObjective.createInstance()

        classPacketPlayOutScoreboardObjective.nameField.modify { set(packet, scoreboardName) }
        classPacketPlayOutScoreboardObjective.modeField.modify { set(packet, 1) }

        player.sendNMSPacket(packet)
    }

    fun removeScoreboard(player: Player) {
        playerScoreboards.remove(player)?.let { scoreboard ->
            stopUpdateTask(player)

            APIProvider.removeRunningScoreboardTitleAnimation(player)
            (1..15).forEach { APIProvider.removeRunningScoreboardValueAnimation(player, it) }

            removeScoreboardWithName(player, scoreboard.name)
        }
    }

    fun setScoreboardTitleWithName(player: Player, title: String, scoreboardName: String) {
        val packet = classPacketPlayOutScoreboardObjective.createInstance()

        classPacketPlayOutScoreboardObjective.nameField.modify { set(packet, scoreboardName) }
        classPacketPlayOutScoreboardObjective.modeField.modify { setInt(packet, 2) }

        if (NMSManager.versionIndex > 6) {
            classPacketPlayOutScoreboardObjective.valueField.modify { set(packet, NMSManager.getClassProvider().getIChatComponent(title)) }
        } else {
            val modifiedTitle = if (title.length > 32) {
                title.substring(0, 32)
            } else {
                title
            }

            classPacketPlayOutScoreboardObjective.valueField.modify { set(packet, modifiedTitle) }
        }

        if (NMSManager.versionIndex > 0) {
            classPacketPlayOutScoreboardObjective.typeField.modify { set(packet, provider["EnumScoreboardHealthDisplay"].handle.enumConstants[0]) }
        }

        player.sendNMSPacket(packet)
    }

    fun setScoreboardValueWithName(player: Player, index: Int, value: String, scoreboardName: String) {
        val packet = classPacketPlayOutScoreboardScore.createInstance()

        classPacketPlayOutScoreboardScore.scoreNameField.modify { set(packet, value) }

        if (NMSManager.versionIndex > 0) {
            classPacketPlayOutScoreboardScore.actionField.modify { set(packet, provider["EnumScoreboardAction"].handle.enumConstants[0]) }
        } else {
            classPacketPlayOutScoreboardScore.actionField.modify { set(packet, 0) }
        }

        classPacketPlayOutScoreboardScore.objectiveNameField.modify { set(packet, scoreboardName) }
        classPacketPlayOutScoreboardScore.valueField.modify { setInt(packet, 15 - index) }

        player.sendNMSPacket(packet)
    }
}