package io.puharesource.mc.titlemanager.internal.model.scoreboard

import io.puharesource.mc.titlemanager.internal.pluginConfig
import io.puharesource.mc.titlemanager.internal.reflections.ChatSerializer
import org.bukkit.ChatColor
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.math.BigInteger
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class ScoreboardHandler(val scoreboard: Scoreboard, title: String = "TitleManager", private val lines: MutableMap<Int, String> = ConcurrentHashMap()) {
    companion object {
        private val random = Random()

        private fun generateRandomString(): String = BigInteger(80, random).toString(32)
    }

    private var objective: Objective
    private val isUpdatePending = AtomicBoolean(false)

    var name: String = generateRandomString()

    var title: String = ""
        set(value) {
            if (field == value) {
                return
            }

            field = value

            setTitleWithoutLimit(value)

            isUpdatePending.set(true)
        }

    init {
        this.objective = getOrCreateObjective(title.take(128))
        this.title = title

        this.objective.displaySlot = DisplaySlot.SIDEBAR
    }

    val size: Int
        get() = lines.size

    fun get(index: Int) = lines[index]

    fun set(index: Int, text: String) {
        val isNew = lines[index] != text

        lines[index] = text
        getOrCreateTeam(index).prefix = text
        objective.getScore(getTeamName(index)).score = 15 - index

        if (isNew || !pluginConfig.bandwidth.preventDuplicatePackets) {
            isUpdatePending.set(true)
        }
    }

    fun remove(index: Int) {
        val existed = lines.remove(index) != null

        if (existed || !pluginConfig.bandwidth.preventDuplicatePackets) {
            isUpdatePending.set(true)
        }
    }

    fun update() {
        if (!isUpdatePending.getAndSet(false)) {
            return
        }
    }

    private fun getTeamName(index: Int): String = ChatColor.values()[index].toString()

    private fun getOrCreateObjective(title: String = this.title): Objective {
        val name = generateRandomString()

        return scoreboard.getObjective(name) ?: scoreboard.registerNewObjective(name, "dummy", title)
    }

    private fun getOrCreateTeam(index: Int): Team {
        val name = getTeamName(index)
        var team = scoreboard.getTeam(name)

        if (team != null) {
            return team
        }

        team = scoreboard.registerNewTeam(name)
        team.addEntry(name)

        return team
    }

    private fun setTitleWithoutLimit(title: String) {
        val craftObjectiveField = objective::class.java.getDeclaredField("objective")
        craftObjectiveField.isAccessible = true

        val craftObjective = craftObjectiveField.get(objective)
        craftObjective::class.java.declaredMethods.first { it.name == "setDisplayName" }.invoke(craftObjective, ChatSerializer.deserializeLegacyText(title))
    }
}
