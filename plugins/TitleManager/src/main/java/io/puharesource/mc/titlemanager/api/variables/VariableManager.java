package io.puharesource.mc.titlemanager.api.variables;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.hooks.PluginHook;
import io.puharesource.mc.titlemanager.backend.variables.RegisteredVariable;
import org.bukkit.entity.Player;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VariableManager {

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

        for (Method method : replacer.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Variable.class)) continue;
            Variable variable = null;
            for (Annotation annotation : method.getDeclaredAnnotations()) {
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

    public PluginHook getHook(final String name) {
        return hooks.get(name.toUpperCase().trim());
    }

    public VariableRule getRule(final String name) {
        return rules.get(name.toUpperCase().trim());
    }

    private Pattern getVariablePattern(final String var) {
        return Pattern.compile("[{](?i)" + var + "[:]\\d+[,]?(\\d+)?[}]");
    }

    public String replaceText(final Player player, String text) {
        for (RegisteredVariable variable : variables) {
            String hookString = variable.getVariable().hook();
            if (!hookString.isEmpty()) {
                PluginHook hook = hooks.get(hookString.toUpperCase().trim());
                if (hook != null && !hook.isEnabled()) continue;
            }

            String ruleString = variable.getVariable().rule();
            if (!ruleString.isEmpty()) {
                VariableRule rule = rules.get(ruleString.toUpperCase().trim());
                if (rule != null && !rule.rule(player)) continue;
            }

            for (String var : variable.getVariable().vars()) {
                if (TitleManager.getInstance().getConfigManager().getConfig().disabledVariables.contains(var)) continue;
                Matcher matcher = getVariablePattern(var).matcher(text);

                int[] dimensions = new int[]{-1, -1};
                boolean found = false;

                if(matcher.find()) {
                    found = true;
                    String varText = text.substring(matcher.start(), matcher.end());
                    String[] parts = varText.split(":");

                    if(parts[1].contains(",")) {
                        String[] strDims = parts[1].split(",", 2);

                        try {
                            dimensions[0] = Integer.parseInt(strDims[0]);
                        } catch (NumberFormatException ignored) {}

                        try {
                            dimensions[1] = Integer.parseInt(strDims[1].substring(0, strDims[1].length() - 1));
                        } catch (NumberFormatException ignored) {}
                    } else {
                        try {
                            dimensions[1] = Integer.parseInt(parts[1].substring(0, parts[1].length() - 1));
                        } catch (NumberFormatException ignored) {}
                    }
                } else if (!text.toLowerCase().contains("{" + var.toLowerCase() + "}")) continue;

                String invoked;
                try {
                    invoked = variable.invoke(replacers.get(variable.getReplacer()), player);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    invoked = "UNSUPPORTED";
                }
                if(!invoked.equals("UNSUPPORTED")) {
                    if (dimensions[0] <= -1) {
                        dimensions[0] = 0;
                    }

                    if (dimensions[1] > invoked.length() || dimensions[1] <= -1) {
                        dimensions[1] = invoked.length();
                    }

                    if (dimensions[0] != 0 || dimensions[1] != invoked.length()) {
                        invoked = invoked.substring(dimensions[0], dimensions[1]);
                    }
                }

                text = text.replaceAll(found ? matcher.pattern().pattern() : "(?i)\\{" + var + "\\}", invoked);
            }
        }

        return text;
    }
}
