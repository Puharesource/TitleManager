package io.puharesource.mc.titlemanager.internal.model.script

import org.bukkit.Bukkit
import org.bukkit.command.ConsoleCommandSender

class ScriptCommandSender : ConsoleCommandSender by Bukkit.getConsoleSender() {
    private val receivedRawMessages: MutableList<String> = mutableListOf()

    val receivedMessages: MutableList<String> = mutableListOf()

    override fun sendRawMessage(message: String) {
        receivedRawMessages.add(message)
    }

    override fun sendMessage(message: String) {
        receivedMessages.add(message)
    }

    override fun sendMessage(messages: Array<out String>) {
        messages.forEach { receivedMessages.add(it) }
    }
}
