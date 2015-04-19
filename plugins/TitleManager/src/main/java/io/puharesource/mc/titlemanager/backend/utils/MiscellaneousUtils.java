package io.puharesource.mc.titlemanager.backend.utils;

import io.puharesource.mc.titlemanager.Config;
import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.ActionbarTitleObject;
import io.puharesource.mc.titlemanager.api.TitleObject;
import io.puharesource.mc.titlemanager.api.animations.*;
import io.puharesource.mc.titlemanager.api.iface.IActionbarObject;
import io.puharesource.mc.titlemanager.api.iface.ITabObject;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

public final class MiscellaneousUtils {
    public static String format(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static FrameSequence isValidAnimationString(String text) {
        if (text == null) return null;
        text = text.toUpperCase().trim();

        return text.startsWith("ANIMATION:") ?
                Config.getAnimation(text.substring(10)) : null;
    }

    public static TitleObject generateTitleObjectFromArgs(int offset, String[] args) {
        int fadeIn = -1;
        int stay = -1;
        int fadeOut = -1;

        StringBuilder sb = new StringBuilder();
        boolean isReadingTimes = true;
        for (int i = offset; args.length > i; i++) {
            if (isReadingTimes) {
                String lower = args[i].toLowerCase();
                int amount = -1;
                try {
                    amount = Integer.parseInt(lower.replaceAll("\\D", ""));
                } catch (NumberFormatException ignored) {
                }

                if (lower.startsWith("-fadein=")) {
                    if (amount != -1) fadeIn = amount;
                    continue;
                } else if (lower.startsWith("-stay=")) {
                    if (amount != -1) stay = amount;
                    continue;
                } else if (lower.startsWith("-fadeout=")) {
                    if (amount != -1) fadeOut = amount;
                    continue;
                } else {
                    isReadingTimes = false;
                    sb.append(args[i]);
                    continue;
                }

            }

            sb.append(" ").append(args[i]);
        }


        String title = format(sb.toString());
        String subtitle = null;

        if (title.contains("{nl}")) title = title.replace("{nl}", "<nl>");
        if (title.contains("<nl>")) {
            String[] titles = title.split("<nl>", 2);
            title = titles[0];
            subtitle = titles[1];
        }

        TitleObject object = subtitle == null ? new TitleObject(title, TitleObject.TitleType.TITLE) : new TitleObject(title, subtitle);

        ConfigMain config = TitleManager.getInstance().getConfigManager().getConfig();

        object.setFadeIn(fadeIn != -1 ? fadeIn : config.welcomeMessageFadeIn);
        object.setStay(stay != -1 ? stay : config.welcomeMessageStay);
        object.setFadeOut(fadeOut != -1 ? fadeOut : config.welcomeMessageFadeOut);

        return object;
    }

    public static String combineArray(int offset, String[] array) {
        StringBuilder sb = new StringBuilder(array[offset]);
        for (int i = offset + 1; array.length > i; i++) sb.append(" ").append(array[i]);
        return format(sb.toString());
    }

    public static String formatNumber(double number) {
        return formatNumber(new BigDecimal(number));
    }

    public static String formatNumber(BigDecimal number) {
        ConfigMain config = TitleManager.getInstance().getConfigManager().getConfig();

        if (config.numberFormatEnabled) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            return new DecimalFormat(config.numberFormat, symbols).format(number);
        }

        return String.valueOf(number.doubleValue());
    }

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

        headerObject = headerObject == null ? MiscellaneousUtils.format(header).replace("\\n", "\n") : headerObject;
        footerObject = footerObject == null ? MiscellaneousUtils.format(footer).replace("\\n", "\n") : footerObject;

        headerObject = headerObject instanceof String ? new FrameSequence(Arrays.asList(new AnimationFrame((String) headerObject, 0, 5, 0))) : headerObject;
        footerObject = footerObject instanceof String ? new FrameSequence(Arrays.asList(new AnimationFrame((String) footerObject, 0, 5, 0))) : footerObject;

        return new TabTitleAnimation(headerObject, footerObject);
    }

    public static ITitleObject generateTitleObject(final String title, final String subtitle, final int fadeIn, final int stay, final int fadeOut) {
        Object titleObject = isValidAnimationString(title);
        Object subtitleObject = isValidAnimationString(subtitle);

        titleObject = titleObject == null ? MiscellaneousUtils.format(title).replace("\\n", "\n") : titleObject;
        subtitleObject = subtitleObject == null ? MiscellaneousUtils.format(subtitle).replace("\\n", "\n") : subtitleObject;

        if (titleObject instanceof FrameSequence || subtitleObject instanceof FrameSequence)
            return new TitleAnimation(title, subtitle);
        return new TitleObject((String) titleObject, (String) subtitleObject).setFadeIn(fadeIn).setStay(stay).setFadeOut(fadeOut);
    }

    public static IActionbarObject generateActionbarObject(final String message) {
        Object messageObject = isValidAnimationString(message);

        messageObject = messageObject == null ? MiscellaneousUtils.format(message).replace("\\n", "\n") : messageObject;

        return messageObject instanceof String ? new ActionbarTitleObject((String) messageObject) : new ActionbarTitleAnimation((FrameSequence) messageObject);
    }
}
