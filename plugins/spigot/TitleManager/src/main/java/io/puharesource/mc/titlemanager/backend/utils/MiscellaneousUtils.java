package io.puharesource.mc.titlemanager.backend.utils;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.animations.*;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.ITabObject;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.api.iface.Script;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.regex.Pattern;

public final class MiscellaneousUtils {
    private static final Pattern SPLIT_PATTERN = Pattern.compile("(([<{%]((?i)nl)[>}%])|\\n)");
    private static final Pattern ANIMATION_PATTERN = Pattern.compile("^((i?)animation:).*$");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("^((i?)script:).*(:).*$");

    public static String format(final String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    public static Set<Player> getWithinRadius(final Location location, final double radius) {
        final Set<Player> players = new HashSet<>();
        final World world = location.getWorld();

        for(final Player player : Bukkit.getOnlinePlayers()) {
            if(player.getWorld().equals(world)) {
                final double dist = location.distance(player.getLocation());

                if(dist <= radius) {
                    players.add(player);
                }
            }
        }
        return players;
    }

    public static FrameSequence isValidAnimationString(final String text) {
        if (text == null) return null;

        return ANIMATION_PATTERN.matcher(text).matches() ? Config.getAnimation(text.split(":", 2)[1]) : null;
    }

    public static FrameSequence isValidScriptString(final String text) {
        if (text == null) return null;

        if (SCRIPT_PATTERN.matcher(text).matches()) {
            final String[] parts = text.split(":", 3);
            final Script script = TitleManager.getInstance().getConfigManager().getScript(parts[1]);

            return script != null ? new FrameSequence(script, parts[2]) : null;
        }

        return null;
    }

    public static String combineArray(final int offset, final String[] array) {
        StringBuilder sb = new StringBuilder(array[offset]);
        for (int i = offset + 1; array.length > i; i++) sb.append(" ").append(array[i]);
        return format(sb.toString());
    }

    public static String formatNumber(final double number) {
        return formatNumber(new BigDecimal(number));
    }

    public static String formatNumber(final BigDecimal number) {
        ConfigMain config = TitleManager.getInstance().getConfigManager().getConfig();

        if (config.numberFormatEnabled) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            return new DecimalFormat(config.numberFormat, symbols).format(number);
        }

        return String.valueOf(number.doubleValue());
    }

    @SuppressWarnings("deprecation")
    public static Player getPlayer(final String name) {
        if (!Pattern.matches("^[a-z0-9A-Z_]+", name) && name.length() <= 16) return null;
        Player correctPlayer = Bukkit.getPlayerExact(name);
        if (correctPlayer == null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (StringUtils.containsIgnoreCase(player.getName(), name)) {
                    return player;
                }
            }
        }

        return correctPlayer;
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
        Object headerObject = isValidAnimationString(header);
        Object footerObject = isValidAnimationString(footer);

        headerObject = headerObject == null ? isValidScriptString(header) : headerObject;
        footerObject = footerObject == null ? isValidScriptString(footer) : footerObject;

        headerObject = headerObject == null ? MiscellaneousUtils.format(header).replace("\\n", "\n") : headerObject;
        footerObject = footerObject == null ? MiscellaneousUtils.format(footer).replace("\\n", "\n") : footerObject;

        headerObject = headerObject instanceof String ? new FrameSequence(Collections.singletonList(new AnimationFrame((String) headerObject, 0, 5, 0))) : headerObject;
        footerObject = footerObject instanceof String ? new FrameSequence(Collections.singletonList(new AnimationFrame((String) footerObject, 0, 5, 0))) : footerObject;

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
