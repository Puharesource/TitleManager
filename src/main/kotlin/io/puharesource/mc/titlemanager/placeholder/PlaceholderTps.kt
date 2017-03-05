package io.puharesource.mc.titlemanager.placeholder

import io.puharesource.mc.titlemanager.script.createScriptCommandSender
import org.bukkit.Bukkit

object PlaceholderTps {
    val regex = """([§][a-f0-9].+), ([§][a-f0-9].+), ([§][a-f0-9].+)""".toRegex()

    private fun getOutput() : String {
        val sender = createScriptCommandSender()
        Bukkit.dispatchCommand(sender, "tps")

        return sender.receivedMessages[0]
    }

    fun getTps(index: Int? = null) : String {
        val output = getOutput()
        val matcher = regex.toPattern().matcher(output.substring(29))

        if (index === 1) {
            return matcher.group(1)
        }

        if (index === 5) {
            return matcher.group(2)
        }

        if (index === 15) {
            return matcher.group(3)
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