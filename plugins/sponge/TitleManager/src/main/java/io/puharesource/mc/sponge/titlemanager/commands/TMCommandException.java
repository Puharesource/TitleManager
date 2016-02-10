package io.puharesource.mc.sponge.titlemanager.commands;


import io.puharesource.mc.sponge.titlemanager.Messages;
import org.spongepowered.api.text.format.TextColors;

public final class TMCommandException extends Exception {
    public TMCommandException(final Messages message, final Object... args) {
        super(TextColors.RED + String.format(message.getMessage().replace("%s", TextColors.RESET + "%s" + TextColors.RED), args));
    }
}
