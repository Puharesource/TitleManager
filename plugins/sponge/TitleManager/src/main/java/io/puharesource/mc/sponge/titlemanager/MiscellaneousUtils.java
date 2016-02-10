package io.puharesource.mc.sponge.titlemanager;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.sponge.titlemanager.api.TitleObject;
import io.puharesource.mc.sponge.titlemanager.api.animations.*;
import io.puharesource.mc.sponge.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.sponge.titlemanager.api.iface.ITabObject;
import io.puharesource.mc.sponge.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.sponge.titlemanager.api.iface.Script;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MiscellaneousUtils {
    @Inject private static TitleManager plugin;

    private static final Pattern SPLIT_PATTERN = Pattern.compile("(([<{%]((?i)nl)[>}%])|\\n)");
    private static final Pattern ANIMATION_PATTERN = Pattern.compile("^((i?)animation:).*$");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("^((i?)script:).*(:).*$");

    public static String format(final String text) {
        return text.replaceAll("&(?=[\\dA-Fa-f])", "§");
    }
    
    public static Set<Player> getWithinRadius(final Location<World> location, final double radius) {
        return Sponge.getServer().getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(location.getExtent()))
                .filter(p -> p.getLocation().getPosition().distance(location.getPosition()) <= radius)
                .collect(Collectors.toSet());
    }

    public static Optional<FrameSequence> isValidAnimationString(final String text) {
        if (text == null) return Optional.empty();

        return ANIMATION_PATTERN.matcher(text).matches() ? plugin.getConfigHandler().getAnimation(text.split(":", 2)[1]) : Optional.empty();
    }

    public static Optional<FrameSequence> isValidScriptString(final String text) {
        if (text == null) return Optional.empty();

        if (SCRIPT_PATTERN.matcher(text).matches()) {
            final String[] parts = text.split(":", 3);
            final Optional<Script> script = plugin.getConfigHandler().getScript(parts[1]);

            return script.isPresent() ? Optional.of(new FrameSequence(script.get(), parts[2])) : Optional.empty();
        }

        return Optional.empty();
    }

    public static String combineArray(final int offset, final String[] array) {
        final StringBuilder sb = new StringBuilder(array[offset]);
        for (int i = offset + 1; array.length > i; i++) sb.append(" ").append(array[i]);
        return format(sb.toString());
    }

    public static String formatNumber(final double number) {
        return formatNumber(new BigDecimal(number));
    }

    public static String formatNumber(final BigDecimal number) {
        ConfigMain config = plugin.getConfigHandler().getConfig();

        if (config.numberFormatEnabled) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            return new DecimalFormat(config.numberFormat, symbols).format(number);
        }

        return String.valueOf(number.doubleValue());
    }

    public static AnimationFrame getFrameFromString(String frame) {
        int fadeIn = -1;
        int stay = -1;
        int fadeOut = -1;

        frame = MiscellaneousUtils.format(frame);
        if (frame.startsWith("[") && frame.length() > 1) {
            char[] chars = frame.toCharArray();
            String timesString = "";
            for (int i = 1; frame.length() > i; i++) {
                char c = chars[i];
                if (c == ']') {
                    frame = frame.substring(i + 1);
                    break;
                }

                timesString += chars[i];
            }

            try {
                String[] times = timesString.split(";", 3);
                fadeIn = Integer.valueOf(times[0]);
                stay = Integer.valueOf(times[1]);
                fadeOut = Integer.parseInt(times[2]);
            } catch (NumberFormatException ignored) {
            }
        }

        return new AnimationFrame(frame, fadeIn, stay, fadeOut);
    }

    public static ITabObject generateTabObject(final String header, final String footer) {
        final FrameSequence headerObject = isValidAnimationString(header)
                .orElseGet(() -> isValidScriptString(header)
                        .orElse(new FrameSequence(Collections.singletonList(new AnimationFrame(MiscellaneousUtils.format(header).replace("\\n", "\n"), 0, 5, 0)))));

        final FrameSequence footerObject = isValidAnimationString(footer)
                .orElseGet(() -> isValidScriptString(footer)
                        .orElse(new FrameSequence(Collections.singletonList(new AnimationFrame(MiscellaneousUtils.format(footer).replace("\\n", "\n"), 0, 5, 0)))));

        return new TabTitleAnimation(headerObject, footerObject);
    }

    public static ITitleObject generateTitleObject(final String title, final String subtitle, final int fadeIn, final int stay, final int fadeOut) {
        Object titleObject = isValidAnimationString(title);
        Object subtitleObject = isValidAnimationString(subtitle);

        titleObject = titleObject == null ? isValidScriptString(title) : titleObject;
        subtitleObject = subtitleObject == null ? isValidScriptString(subtitle) : subtitleObject;

        titleObject = titleObject == null ? MiscellaneousUtils.format(title) : titleObject;
        subtitleObject = subtitleObject == null ? MiscellaneousUtils.format(subtitle) : subtitleObject;

        if (titleObject instanceof FrameSequence || subtitleObject instanceof FrameSequence)
            return new TitleAnimation(titleObject, subtitleObject);
        return new TitleObject((String) titleObject, (String) subtitleObject).setFadeIn(fadeIn).setStay(stay).setFadeOut(fadeOut);
    }

    public static IActionbarObject generateActionbarObject(final String message) {
        Object messageObject = isValidAnimationString(message);

        messageObject = messageObject == null ? isValidScriptString(message) : messageObject;
        messageObject = messageObject == null ? MiscellaneousUtils.format(message).replace("\\n", "\n") : messageObject;

        return messageObject instanceof String ? new ActionbarTitleObject((String) messageObject) : new ActionbarTitleAnimation((FrameSequence) messageObject);
    }

    public static String[] splitString(String str) {
        str = str.replaceFirst("\\n", "\n");

        if (str.matches("^.*" + SPLIT_PATTERN.pattern() + ".*$")) {
            return str.split(SPLIT_PATTERN.pattern(), 2);
        }
        return new String[]{str, ""};
    }
}
