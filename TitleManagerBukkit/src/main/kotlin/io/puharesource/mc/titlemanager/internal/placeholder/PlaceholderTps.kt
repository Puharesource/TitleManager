package io.puharesource.mc.titlemanager.internal.placeholder

import io.puharesource.mc.titlemanager.internal.model.script.ScriptCommandSender
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import kotlin.math.roundToInt

object PlaceholderTps {
    private val regex =
        """([§][a-f0-9].+), ([§][a-f0-9].+), ([§][a-f0-9].+)""".toRegex()

    private val isUsingPaper by lazy {
        try {
            Class.forName("com.destroystokyo.paper.Title")

            return@lazy true
        } catch (ignored: ClassNotFoundException) {
            return@lazy false
        }
    }

    private fun getCommandOutput(): String {
        val sender = ScriptCommandSender()
        Bukkit.dispatchCommand(sender, "tps")

        return sender.receivedMessages.first()
    }

    private fun getPaperCommandReplica(): String {
        return "${ChatColor.GOLD} TPS from last 1m, 5m, 15m: " + getTpsFromPaper()
    }

    private fun getTpsFromCommand(index: Int? = null): String {
        val output = getCommandOutput()
        val result = regex.matchEntire(output.substring(29)) ?: return "N/A"

        return when (index) {
            1 -> result.groups[1]!!.value
            5 -> result.groups[2]!!.value
            15 -> result.groups[3]!!.value
            else -> output.substring(29)
        }
    }

    private fun getTpsFromPaper(index: Int? = null): String {
        val tps = Bukkit.getTPS()

        return when (index) {
            1 -> formatTpsNumber(tps[0])
            5 -> formatTpsNumber(tps[1])
            15 -> formatTpsNumber(tps[2])
            else -> "${formatTpsNumber(tps[0], true)}, ${formatTpsNumber(tps[1], true)}, ${formatTpsNumber(tps[2], true)}"
        }
    }

    private fun formatTpsNumber(tps: Double, withStar: Boolean = false): String {
        val color = when {
            tps > 18.0 -> ChatColor.GREEN
            tps > 16.0 -> ChatColor.YELLOW
            else -> ChatColor.RED
        }

        val star = if (withStar && tps > 20.0) "*" else ""
        val roundedTps = ((tps * 100.0).roundToInt() / 100.0).coerceAtMost(20.0)

        return "$color$star$roundedTps"
    }

    fun getTps(index: Int? = null): String {
        return (if (isUsingPaper) getTpsFromPaper(index) else getTpsFromCommand(index)).trim()
    }

    fun getTps(text: String): String {
        val output = if (isUsingPaper) getPaperCommandReplica() else getCommandOutput()

        if (text.equals("short", ignoreCase = true)) {
            return output.substring(29).trim()
        }

        return output.trim()
    }
}
