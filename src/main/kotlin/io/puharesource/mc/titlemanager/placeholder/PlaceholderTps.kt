package io.puharesource.mc.titlemanager.placeholder

import io.puharesource.mc.titlemanager.script.createScriptCommandSender
import org.bukkit.Bukkit

object PlaceholderTps {
    private val regex = """([§][a-f0-9].+), ([§][a-f0-9].+), ([§][a-f0-9].+)""".toRegex()

    private fun getOutput() : String {
        val sender = createScriptCommandSender()
        Bukkit.dispatchCommand(sender, "tps")

        return sender.receivedMessages.first()
    }

    fun getTps(index: Int? = null) : String {
        val output = getOutput()
        val result = regex.matchEntire(output.substring(29))!!

        if (index == 1) {
            return result.groups[1]!!.value
        }

        if (index == 5) {
            return result.groups[2]!!.value
        }

        if (index == 15) {
            return result.groups[3]!!.value
        }

        return output.substring(29)
    }

    fun getTps(text: String) : String {
        val output = getOutput()

        if (text.equals("short", ignoreCase = true)) {
            return output.substring(29)
        }

        return output
    }
}