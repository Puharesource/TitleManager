package io.puharesource.mc.titlemanager.script

import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.conversations.Conversation
import org.bukkit.conversations.ConversationAbandonedEvent
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.plugin.Plugin

fun createScriptCommandSender() = ScriptCommandSender()

class ScriptCommandSender : ConsoleCommandSender by Bukkit.getConsoleSender() {
    private val receivedMessages : MutableList<String> = mutableListOf()
    private val receivedRawMessages : MutableList<String> = mutableListOf()

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