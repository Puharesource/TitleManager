package io.puharesource.mc.titlemanager.commands;

import org.bukkit.ChatColor;

import io.puharesource.mc.titlemanager.backend.language.Messages;

public final class TMCommandException extends RuntimeException {
    public TMCommandException(final Messages message, final Object... args) {
        super(ChatColor.RED + String.format(message.getMessage().replace("%s", ChatColor.RESET + "%s" + ChatColor.RED), args));
    }
}
