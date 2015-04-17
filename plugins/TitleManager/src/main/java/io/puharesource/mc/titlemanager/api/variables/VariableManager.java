package io.puharesource.mc.titlemanager.api.variables;

import io.puharesource.mc.titlemanager.backend.hooks.PluginHook;
import io.puharesource.mc.titlemanager.backend.variables.RegisteredVariable;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class VariableManager {

    private final Map<Integer, VariableReplacer> replacers = new HashMap<>();
    private final List<RegisteredVariable> variables = Collections.synchronizedList(new ArrayList<RegisteredVariable>());

    private Map<String, PluginHook> hooks = new HashMap<>();
    private Map<String, VariableRule> rules = new HashMap<>();

    private void registerMethod(final Method method, int replacer, final Variable variable) {
        variables.add(new RegisteredVariable(method, variable, replacer));
    }

    public void registerVariableReplacer(final VariableReplacer replacer) {
        int rReplacer = replacers.size();
        replacers.put(rReplacer, replacer);

        for (Method method : replacer.getClass().getMethods()) {
            Annotation[] annotations = method.getAnnotations();
            if (annotations.length <= 0) continue;

            Variable variable = null;
            for (Annotation annotation : annotations) {
                if (annotation instanceof Variable) {
                    variable = (Variable) annotation;
                    break;
                }
            }

            if (variable == null) continue;

            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 || params[0].equals(Player.class)) {
                registerMethod(method, rReplacer, variable);
            }
        }
    }

    public void registerHook(final String name, final PluginHook hook) {
        hooks.put(name.toUpperCase().trim(), hook);
    }

    public void registerRule(final String name, final VariableRule rule) {
        rules.put(name.toUpperCase().trim(), rule);
    }

    public String replaceText(final Player player, String text) {
        for (RegisteredVariable variable : variables) {
            PluginHook hook = hooks.get(variable.getVariable().hook().toUpperCase().trim());
            if (!hook.isEnabled()) continue;
            VariableRule rule = rules.get(variable.getVariable().rule().toUpperCase().trim());
            if (!rule.rule(player)) continue;

            for (String var : variable.getVariable().vars()) {
                String invoked;
                try {
                    invoked = variable.invoke(replacers.get(variable.getReplacer()), player);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    invoked = "UNSUPPORTED";
                }
                text = var.replaceAll("(?i)\\{" + var + "\\}", invoked);
            }
        }

        return text;
    }
}
