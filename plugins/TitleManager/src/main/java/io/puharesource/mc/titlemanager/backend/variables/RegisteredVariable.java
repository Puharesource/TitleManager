package io.puharesource.mc.titlemanager.backend.variables;

import io.puharesource.mc.titlemanager.api.variables.Variable;
import io.puharesource.mc.titlemanager.api.variables.VariableReplacer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RegisteredVariable {

    private final Method method;
    private final Variable variable;
    private final int replacer;

    public RegisteredVariable(final Method method, final Variable variable, final int replacer) {
        this.method = method;
        this.variable = variable;
        this.replacer = replacer;
    }

    public String invoke(final VariableReplacer replacer, final Player player) throws InvocationTargetException, IllegalAccessException {
        return (String) method.invoke(replacer, player);
    }

    public Variable getVariable() {
        return variable;
    }

    public int getReplacer() {
        return replacer;
    }
}
