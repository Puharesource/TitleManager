package io.puharesource.mc.sponge.titlemanager.api.placeholder;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.placeholder.hook.PluginHook;
import org.spongepowered.api.entity.living.player.Player;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaceholderManager {
    @Inject private TitleManager plugin;

    private final Map<Integer, PlaceholderReplacer> replacers = new HashMap<>();
    private final Set<RegisteredPlaceholder> variables = Sets.newSetFromMap(new MapMaker().concurrencyLevel(4).makeMap());

    private Map<String, PluginHook> hooks = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, PlaceholderRule> rules = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private void registerMethod(final Method method, int replacer, final Placeholder variable) {
        variables.add(new RegisteredPlaceholder(method, variable, replacer));
    }

    public void registerVariableReplacer(final PlaceholderReplacer replacer) {
        int rReplacer = replacers.size();
        replacers.put(rReplacer, replacer);

        for (Method method : replacer.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Placeholder.class)) continue;
            Placeholder variable = null;
            for (Annotation annotation : method.getDeclaredAnnotations()) {
                if (annotation instanceof Placeholder) {
                    variable = (Placeholder) annotation;
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
        hooks.put(name, hook);
    }

    public void registerRule(final String name, final PlaceholderRule rule) {
        rules.put(name, rule);
    }

    public PluginHook getHook(final String name) {
        return hooks.get(name);
    }

    public PlaceholderRule getRule(final String name) {
        return rules.get(name);
    }

    private Pattern getVariablePattern(final String var) {
        return Pattern.compile("[{](?i)" + var + "[:]\\d+[,]?(\\d+)?[}]");
    }

    public String replaceText(final Player player, String text) {
        for (RegisteredPlaceholder variable : variables) {
            String hookString = variable.getVariable().hook();
            if (!hookString.isEmpty()) {
                PluginHook hook = hooks.get(hookString);
                if (hook != null && !hook.getPlugin().isPresent()) continue;
            }

            String ruleString = variable.getVariable().rule();
            if (!ruleString.isEmpty()) {
                PlaceholderRule rule = rules.get(ruleString);
                if (rule != null && !rule.rule(player)) continue;
            }

            for (String var : variable.getVariable().vars()) {
                if (plugin.getConfigHandler().getConfig().disabledVariables.contains(var)) continue;
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
