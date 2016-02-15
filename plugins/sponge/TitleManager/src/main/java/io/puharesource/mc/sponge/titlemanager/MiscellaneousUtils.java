package io.puharesource.mc.sponge.titlemanager;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.api.Sendables;
import io.puharesource.mc.sponge.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.sponge.titlemanager.api.animations.AnimationToken;
import io.puharesource.mc.sponge.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.sponge.titlemanager.api.iface.ActionbarSendable;
import io.puharesource.mc.sponge.titlemanager.api.iface.Script;
import io.puharesource.mc.sponge.titlemanager.api.iface.TabListSendable;
import io.puharesource.mc.sponge.titlemanager.api.iface.TitleSendable;
import io.puharesource.mc.sponge.titlemanager.config.configs.ConfigMain;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
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

    public static Text format(final String text) {
        return TextSerializers.formattingCode('&').deserialize(text);
    }
    
    public static Set<Player> getWithinRadius(final Location<World> location, final double radius) {
        return Sponge.getServer().getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(location.getExtent()))
                .filter(p -> p.getLocation().getPosition().distance(location.getPosition()) <= radius)
                .collect(Collectors.toSet());
    }

    public static Optional<FrameSequence> loadAnimationFromString(final Text text) {
        if (text == null) return Optional.empty();

        return ANIMATION_PATTERN.matcher(text.toPlain()).matches() ? plugin.getConfigHandler().getAnimation(text.toPlain().split(":", 2)[1]) : Optional.empty();
    }

    public static Optional<FrameSequence> loadScriptFromString(final Text text) {
        if (text == null) return Optional.empty();

        if (SCRIPT_PATTERN.matcher(text.toPlain()).matches()) {
            final String[] parts = text.toPlain().split(":", 3);
            final Optional<Script> script = plugin.getConfigHandler().getScript(parts[1]);

            return script.isPresent() ? Optional.of(new FrameSequence(script.get(), Text.of(parts[2]))) : Optional.empty();
        }

        return Optional.empty();
    }

    public static String formatNumber(final double number) {
        return formatNumber(new BigDecimal(number));
    }

    public static String formatNumber(final BigDecimal number) {
        ConfigMain config = plugin.getConfigHandler().getMainConfig().getConfig();

        if (config.numberFormatEnabled) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            return new DecimalFormat(config.numberFormat, symbols).format(number);
        }

        return String.valueOf(number.doubleValue());
    }

    public static TabListSendable generateTabObject(final Text header, final Text footer) {
        final FrameSequence headerObject = loadAnimationFromString(header)
                .orElseGet(() -> loadScriptFromString(header)
                        .orElse(new FrameSequence(Collections.singletonList(new AnimationFrame(header, 0, 5, 0)))));

        final FrameSequence footerObject = loadAnimationFromString(footer)
                .orElseGet(() -> loadScriptFromString(footer)
                        .orElse(new FrameSequence(Collections.singletonList(new AnimationFrame(footer, 0, 5, 0)))));

        return Sendables.tabList(AnimationToken.of(headerObject), AnimationToken.of(footerObject));
    }

    public static TitleSendable generateTitleObject(final Text title, final Text subtitle, final int fadeIn, final int stay, final int fadeOut) {
        final Optional<FrameSequence> oTitle = Optional.ofNullable(loadAnimationFromString(title)
                .orElseGet(() -> loadScriptFromString(title).get()));
        final Optional<FrameSequence> oSubtitle = Optional.ofNullable(loadAnimationFromString(subtitle)
                .orElseGet(() -> loadScriptFromString(subtitle).get()));

        if (oTitle.isPresent() || oSubtitle.isPresent()) {

            return Sendables.title(
                    oTitle.isPresent() ? AnimationToken.of(oTitle.get()) : AnimationToken.of(title),
                    oSubtitle.isPresent() ? AnimationToken.of(oSubtitle.get()) : AnimationToken.of(subtitle));
        }

        return Sendables.title(
                title,
                subtitle)
                .setFadeIn(fadeIn)
                .setStay(stay)
                .setFadeOut(fadeOut);
    }

    public static ActionbarSendable generateActionbarObject(final Text message) {
        final Optional<FrameSequence> oMessage = Optional.ofNullable(loadAnimationFromString(message)
                .orElseGet(() -> loadScriptFromString(message).get()));

        return oMessage.isPresent() ? Sendables.actionbar(oMessage.get()) : Sendables.actionbar(message);
    }

    public static String[] splitString(String str) {
        str = str.replaceFirst("\\n", "\n");

        if (str.matches("^.*" + SPLIT_PATTERN.pattern() + ".*$")) {
            return str.split(SPLIT_PATTERN.pattern(), 2);
        }
        return new String[]{str, ""};
    }
}
