package io.puharesource.mc.sponge.titlemanager.api.placeholder;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.utils.MiscellaneousUtils;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.placeholder.hook.PluginHook;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
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
        plugin.getInjector().injectMembers(replacer);

        int rReplacer = replacers.size();
        replacers.put(rReplacer, replacer);

        for (Method method : replacer.getClass().getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Placeholder.class)) continue;

            final Optional<Placeholder> placeholder = Arrays.stream(method.getDeclaredAnnotations())
                    .filter(annotation -> annotation instanceof Placeholder)
                    .limit(1)
                    .map(annotation -> (Placeholder) annotation)
                    .findAny();

            if (!placeholder.isPresent()) continue;

            final Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 || params[0].equals(Player.class)) {
                registerMethod(method, rReplacer, placeholder.get());
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
        return Pattern.compile("[$][{](?i)" + var + "[}]");
    }

    public Text replaceText(final Player player, Text text) {
        for (final RegisteredPlaceholder variable : variables) {
            final String hookString = variable.getVariable().hook();
            if (!hookString.isEmpty()) {
                final PluginHook hook = hooks.get(hookString);
                if (hook != null && !hook.getPlugin().isPresent()) continue;
            }

            final String ruleString = variable.getVariable().rule();
            if (!ruleString.isEmpty()) {
                final PlaceholderRule rule = rules.get(ruleString);
                if (rule != null && !rule.rule(player)) continue;
            }

            for (final String var : variable.getVariable().placeholders()) {
                if (plugin.getConfigHandler().getMainConfig().getConfig().disabledVariables.contains(var)) continue;

                if (!text.toPlain().toLowerCase().contains("${" + var.toLowerCase() + "}")) continue;

                String invoked;
                try {
                    invoked = variable.invoke(replacers.get(variable.getReplacer()), player);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    invoked = "UNSUPPORTED";
                }

                final String invokedString = invoked;

                text = MiscellaneousUtils.transformText(text,
                        str -> str.replaceAll(getVariablePattern(var).pattern(), invokedString));
            }
        }

        return text;
    }
}
