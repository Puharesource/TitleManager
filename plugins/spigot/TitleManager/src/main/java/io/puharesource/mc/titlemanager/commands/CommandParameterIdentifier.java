package io.puharesource.mc.titlemanager.commands;

import org.apache.commons.lang.Validate;

import java.util.Optional;

public enum CommandParameterIdentifier {
    SILENT,
    BUNGEE,
    WORLD,
    RADIUS,
    FADE_IN,
    STAY,
    FADE_OUT;

    public String getName() {
        return this.toString().replace("_", "");
    }

    public static Optional<CommandParameterIdentifier> getByName(final String name) {
        Validate.notNull(name, "Name cannot be null!");

        for (final CommandParameterIdentifier id : values()) {
            if (id.getName().equalsIgnoreCase(name)) {
                return Optional.of(id);
            }
        }

        return Optional.empty();
    }
}
