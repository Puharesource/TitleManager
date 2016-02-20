package io.puharesource.mc.sponge.titlemanager.utils;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.Sendables;
import io.puharesource.mc.sponge.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.sponge.titlemanager.api.animations.AnimationToken;
import io.puharesource.mc.sponge.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.sponge.titlemanager.api.animations.MultiFrameSequence;
import io.puharesource.mc.sponge.titlemanager.api.iface.*;
import io.puharesource.mc.sponge.titlemanager.config.configs.ConfigMain;
import org.apache.commons.lang3.Validate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class MiscellaneousUtils {
    @Inject private static TitleManager plugin;

    private static final Pattern SPLIT_PATTERN = Pattern.compile("(([<{%]((?i)nl)[>}%])|\\n)");
    private static final Pattern ANIMATION_PATTERN = Pattern.compile("^((i?)animation:).*$");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("^((i?)script:).*$");

    MiscellaneousUtils() {}

    /**
     * Loads a standard Animation from the config with the use of text.
     * @param text The text that will be analyzed for animations.
     * @return An optional of the animation found in the text.
     */
    public static Optional<AnimationIterable> loadAnimationFromText(final Text text) {
        if (text == null) return Optional.empty();

        return ANIMATION_PATTERN.matcher(text.toPlain()).matches() ? plugin.getConfigHandler().getAnimation(text.toPlain().split(":", 2)[1]) : Optional.empty();
    }

    /**
     * Loads a script Animation.
     * @param text The text that will be analyzed for animations.
     * @return An optional of the animation found the text.
     */
    public static Optional<AnimationIterable> loadScriptFromText(final Text text) {
        if (text == null) return Optional.empty();

        if (SCRIPT_PATTERN.matcher(text.toPlain()).matches()) {
            final String[] parts = text.toPlain().split(":", 3);
            final Optional<Script> script = plugin.getConfigHandler().getScript(parts[1]);

            return script.isPresent() ? Optional.of(new FrameSequence(script.get(), Text.of(parts[2]))) : Optional.empty();
        }

        return Optional.empty();
    }

    /**
     * Loads an animation.
     * @param text
     * @return
     */
    public static Optional<AnimationIterable> loadAnimationIterable(final Text text) {
        if (SCRIPT_PATTERN.matcher(text.toPlain()).matches() || ANIMATION_PATTERN.matcher(text.toPlain()).matches())
            return createFrameSequenceFromChild(text);

        final List<AnimationToken> tokens = createAnimationTokens(text);
        if (!containsAnimations(tokens)) return Optional.empty();

        final MultiFrameSequence multiFrameSequence = new MultiFrameSequence(tokens);
        plugin.getInjector().injectMembers(multiFrameSequence);

        return Optional.of(multiFrameSequence);
    }

    public static List<AnimationToken> createAnimationTokens(final Text text) {
        Validate.notNull(text);

        return textBuilderStream(text)
                .map(builder -> {
                    final Text t = builder.build();
                    final Optional<AnimationIterable> sequence = createFrameSequenceFromChild(t);

                    return sequence.isPresent() ? AnimationToken.of(sequence.get()) : AnimationToken.of(t);
                }).collect(Collectors.toList());
    }

    public static boolean containsAnimations(final Collection<AnimationToken> tokens) {
        for (final AnimationToken token : tokens) {
            if (token.isIterable()) return true;
        }

        return false;
    }

    public static Optional<AnimationIterable> createFrameSequenceFromChild(final Text text) {
        Validate.notNull(text);

        final Optional<AnimationIterable> animation = loadAnimationFromText(text);
        if (animation.isPresent()) return animation;

        final Optional<AnimationIterable> script = loadScriptFromText(text);
        if (script.isPresent()) return script;

        return Optional.empty();
    }

    public static TabListSendable createTabListSendable(final Text header, final Text footer) {
        Validate.notNull(header);
        Validate.notNull(footer);

        final AnimationIterable headerObject = loadAnimationIterable(header)
                .orElseGet(() -> new FrameSequence(Collections.singletonList(new AnimationFrame(header, 0, 5, 0))));
        final AnimationIterable footerObject = loadAnimationIterable(footer)
                .orElseGet(() -> new FrameSequence(Collections.singletonList(new AnimationFrame(footer, 0, 5, 0))));

        return Sendables.tabList(AnimationToken.of(headerObject), AnimationToken.of(footerObject));
    }

    public static TitleSendable createTitleSendable(final Text title, final Text subtitle, final int fadeIn, final int stay, final int fadeOut) {
        Validate.notNull(title);
        Validate.notNull(subtitle);

        final Optional<AnimationIterable> oTitle = loadAnimationIterable(title);
        final Optional<AnimationIterable> oSubtitle = loadAnimationIterable(subtitle);

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

    public static ActionbarSendable createActionbarSendable(final Text message) {
        Validate.notNull(message);

        final Optional<AnimationIterable> oMessage = loadAnimationIterable(message);

        return oMessage.isPresent() ? Sendables.actionbar(oMessage.get()) : Sendables.actionbar(message);
    }

    // Number related
    // =================>

    public static String formatNumber(final double number) {
        return formatNumber(new BigDecimal(number));
    }

    public static String formatNumber(final BigDecimal number) {
        Validate.notNull(number);

        final ConfigMain config = plugin.getConfigHandler().getMainConfig().getConfig();

        if (config.numberFormatEnabled) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            return new DecimalFormat(config.numberFormat, symbols).format(number);
        }

        return String.valueOf(number.doubleValue());
    }

    // Text / String related
    // ========================>

    public static String[] splitString(String str) {
        Validate.notNull(str);

        str = str.replaceFirst("\\n", "\n");

        if (str.matches("^.*" + SPLIT_PATTERN.pattern() + ".*$")) {
            return str.split(SPLIT_PATTERN.pattern(), 2);
        }
        return new String[]{str, ""};
    }

    public static Text format(final String text) {
        Validate.notNull(text);

        return TextSerializers.formattingCode('&').deserialize(text);
    }

    public static Text transformText(final Text text, final Function<String, String> function) {
        Validate.notNull(text);
        Validate.notNull(function);

        final Text.Builder newTextBuilder = Text.builder();

        textBuilderStream(text)
                .map(builder -> builder.content(function.apply(builder.getContent())).build())
                .forEach(newTextBuilder::append);

        return newTextBuilder.toText();
    }

    public static Stream<LiteralText.Builder> textBuilderStream(final Text text) {
        Validate.notNull(text);

        return StreamSupport.stream(text.withChildren().spliterator(), false)
                .filter(t -> t instanceof LiteralText)
                .map(t -> (LiteralText.Builder) t.toBuilder())
                .map(builder -> {
                    final LiteralText.Builder literalBuilder = (LiteralText.Builder) LiteralText.builder();
                    literalBuilder.content(builder.getContent());
                    literalBuilder.format(builder.getFormat());

                    return literalBuilder;
                });
    }

    // Other
    // ========>

    public static Set<Player> getWithinRadius(final Location<World> location, final double radius) {
        Validate.notNull(location);

        return Sponge.getServer().getOnlinePlayers().stream()
                .filter(p -> p.getWorld().equals(location.getExtent()))
                .filter(p -> p.getLocation().getPosition().distance(location.getPosition()) <= radius)
                .collect(Collectors.toSet());
    }
}
