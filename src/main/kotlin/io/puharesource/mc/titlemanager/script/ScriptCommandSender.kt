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

class ScriptCommandSender : ConsoleCommandSender {
    private val consoleSender : ConsoleCommandSender by lazy { Bukkit.getConsoleSender() }

    val receivedMessages : MutableList<String> = mutableListOf()
    val receivedRawMessages : MutableList<String> = mutableListOf()

    override fun sendRawMessage(message: String) {
        receivedRawMessages.add(message)
    }

    override fun sendMessage(message: String) {
        receivedMessages.add(message)
    }

    override fun sendMessage(messages: Array<out String>) {
        messages.forEach { receivedMessages.add(it) }
    }

    override fun getName() : String = consoleSender.name

    override fun isOp(): Boolean = consoleSender.isOp

    override fun setOp(value: Boolean) {
        consoleSender.isOp = value
    }

    override fun acceptConversationInput(input: String?) = consoleSender.acceptConversationInput(input)

    override fun recalculatePermissions() = consoleSender.recalculatePermissions()

    override fun hasPermission(name: String?): Boolean = consoleSender.hasPermission(name)

    override fun hasPermission(perm: Permission?): Boolean = consoleSender.hasPermission(perm)

    override fun isConversing(): Boolean = consoleSender.isConversing

    override fun beginConversation(conversation: Conversation?): Boolean = consoleSender.beginConversation(conversation)

    override fun abandonConversation(conversation: Conversation?) = consoleSender.abandonConversation(conversation)

    override fun abandonConversation(conversation: Conversation?, details: ConversationAbandonedEvent?) = consoleSender.abandonConversation(conversation, details)

    override fun isPermissionSet(name: String?): Boolean = consoleSender.isPermissionSet(name)

    override fun isPermissionSet(perm: Permission?): Boolean = consoleSender.isPermissionSet(perm)

    override fun addAttachment(plugin: Plugin?, name: String?, value: Boolean): PermissionAttachment = consoleSender.addAttachment(plugin, name, value)

    override fun addAttachment(plugin: Plugin?): PermissionAttachment = consoleSender.addAttachment(plugin)

    override fun addAttachment(plugin: Plugin?, name: String?, value: Boolean, ticks: Int): PermissionAttachment = consoleSender.addAttachment(plugin, name, value, ticks)

    override fun addAttachment(plugin: Plugin?, ticks: Int): PermissionAttachment = consoleSender.addAttachment(plugin, ticks)

    override fun removeAttachment(attachment: PermissionAttachment?) = consoleSender.removeAttachment(attachment)

    override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> = consoleSender.effectivePermissions

    override fun getServer(): Server = consoleSender.server
}