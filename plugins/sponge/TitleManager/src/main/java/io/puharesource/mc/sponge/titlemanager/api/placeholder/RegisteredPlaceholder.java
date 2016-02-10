package io.puharesource.mc.sponge.titlemanager.api.placeholder;

import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RegisteredPlaceholder {
    private final Method method;
    private @Getter final Placeholder variable;
    private @Getter final int replacer;

    public RegisteredPlaceholder(final Method method, final Placeholder variable, final int replacer) {
        this.method = method;
        this.variable = variable;
        this.replacer = replacer;
    }

    public String invoke(final PlaceholderReplacer replacer, final Player player) throws InvocationTargetException, IllegalAccessException {
        return (String) method.invoke(replacer, player);
    }
}
