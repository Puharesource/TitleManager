package io.puharesource.mc.titlemanager.api.v2;

import io.puharesource.mc.titlemanager.api.v2.animation.Animation;
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame;
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart;
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The API for TitleManager<p>
 *
 * Here you'll find all methods available in the API.
 *
 * @since 2.0.0
 */
public interface TitleManagerAPI {

    // Placeholder

    /**
     * Replaces the text with any placeholders that follow the pattern <code>%{my-placeholder}</code>
     * as well as any placeholders following the pattern <code>%{my-placeholder:my-parameter}</code>.
     * <p>
     * If <a href="https://www.spigotmc.org/resources/placeholderapi.6245/">PlaceholderAPI</a> is installed and enabled,
     * any placeholders available within PlaceholderAPI will be replaced before TitleManager replaces its own.
     *
     * @param player The player that any player specific placeholders will be matched to.
     * @param text   The text that will be replaced.
     *
     * @return       The input text with all available placeholders replaced.
     *               If no placeholders were found the input text will be returned instead.
     *
     * @see <a href="https://www.spigotmc.org/resources/placeholderapi.6245/">PlaceholderAPI</a>
     * @see #containsPlaceholders(String)
     * @see #containsPlaceholder(String, String)
     *
     * @since 2.0.0
     */
    String replaceText(Player player, String text);

    /**
     * Checks if the input text contains any registered placeholders following the pattern <code>%{my-placeholder}</code>
     * as well as any placeholders following the pattern <code>%{my-placeholder:my-parameter}</code>.
     * Though the placeholder does need to be registered with the plugin.
     *
     * @param text The text that will be matched to the patterns.
     *
     * @return     <code>true</code> if the text returned with a match and <code>false</code> if it doesn't.
     *
     * @see #replaceText(Player, String)
     * @see #containsPlaceholder(String, String)
     *
     * @since 2.0.0
     */
    boolean containsPlaceholders(String text);

    /**
     * Checks if the input text contains the pattern <code>%{my-placeholder}</code> or the pattern
     * <code>%{my-placeholder:parameter}</code> where <code>my-placeholder</code> is the placeholder parameter.
     *
     * @param text        The text that will be matched to the pattern.
     * @param placeholder The placeholder that will be matched for.
     *
     * @return            <code>true</code> if the text returned with a match and <code>false</code> if it doesn't.
     *
     * @see #replaceText(Player, String)
     * @see #containsPlaceholders(String)
     *
     * @since 2.0.0
     */
    boolean containsPlaceholder(String text, String placeholder);

    // Animation

    /**
     * Checks if the input text contains any registered animations following the pattern <code>${my-animation}</code>
     * as well as any animations or scripts following the pattern <code>${my-animation:my-parameter}</code>.
     * Though the animation or script does need to be registered with the plugin.
     *
     * @param text The text that will be matched to the patterns.
     *
     * @return     <code>true</code> if the text returned with a match and <code>false</code> if it doesn't.
     *
     * @see #containsAnimation(String, String)
     * @see #addAnimation(String, Animation)
     * @see #removeAnimation(String)
     * @see #toAnimationPart(String)
     * @see #toAnimationParts(String)
     * @see #getRegisteredAnimations()
     * @see #getRegisteredScripts()
     *
     * @since 2.0.0
     */
    boolean containsAnimations(String text);

    /**
     * Checks if the input text contains the pattern <code>${my-animation}</code> or the pattern
     * <code>${my-animation:parameter}</code> where <code>my-animation</code> is the animation parameter.
     *
     * @param text      The text that will be matched.
     * @param animation The animation or script that will be matched for.
     *
     * @return          <code>true</code> if the text returned with a match and <code>false</code> if it doesn't.
     *
     * @see #containsAnimations(String)
     * @see #addAnimation(String, Animation)
     * @see #removeAnimation(String)
     * @see #toAnimationPart(String)
     * @see #toAnimationParts(String)
     * @see #getRegisteredAnimations()
     * @see #getRegisteredScripts()
     *
     * @since 2.0.0
     */
    boolean containsAnimation(String text, String animation);

    /**
     * Gets an immutable {@link Map} where the key is a {@link String} and value is a {@link Animation}.
     *
     * @return Gets the immutable {@link Map} results.
     *
     * @see #containsAnimation(String, String)
     * @see #containsAnimations(String)
     * @see #addAnimation(String, Animation)
     * @see #removeAnimation(String)
     * @see #toAnimationPart(String)
     * @see #toAnimationParts(String)
     * @see #getRegisteredScripts()
     *
     * @since 2.0.0
     */
    Map<String, Animation> getRegisteredAnimations();

    /**
     * Gets an immutable {@link Set} of all of the Script names registered.
     *
     * @return Gets the immutable ${@link Set} results.
     *
     * @see #containsAnimation(String, String)
     * @see #containsAnimations(String)
     * @see #addAnimation(String, Animation)
     * @see #removeAnimation(String)
     * @see #toAnimationPart(String)
     * @see #toAnimationParts(String)
     * @see #getRegisteredAnimations()
     *
     * @since 2.0.0
     */
    Set<String> getRegisteredScripts();

    /**
     * Adds an animation to the plugin.
     *
     * @param id        The id of the animation (case-insensitive).
     * @param animation The animation to be added.
     *
     * @see #containsAnimations(String)
     * @see #removeAnimation(String)
     * @see #toAnimationPart(String)
     * @see #toAnimationParts(String)
     * @see #getRegisteredAnimations()
     * @see #getRegisteredScripts()
     *
     * @since 2.0.0
     */
    void addAnimation(String id, Animation animation);

    /**
     * Removes an animation from the plugin.
     *
     * @param id The id of the animation that should be removed (case-insensitive).
     *
     * @see #containsAnimations(String)
     * @see #addAnimation(String, Animation)
     * @see #toAnimationPart(String)
     * @see #toAnimationParts(String)
     * @see #getRegisteredAnimations()
     * @see #getRegisteredScripts()
     *
     * @since 2.0.0
     */
    void removeAnimation(String id);

    /**
     * Creates a {@link SendableAnimation} that sends Titles to the player.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     *
     * @param animation        The animation to be used.
     * @param player           The player the {@link SendableAnimation} is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @see #toSubtitleAnimation(Animation, Player, boolean)
     * @see #toActionbarAnimation(Animation, Player, boolean)
     * @see #toHeaderAnimation(Animation, Player, boolean)
     * @see #toFooterAnimation(Animation, Player, boolean)
     *
     * @see #toTitleAnimation(List, Player, boolean)
     * @see #toSubtitleAnimation(List, Player, boolean)
     * @see #toActionbarAnimation(List, Player, boolean)
     * @see #toHeaderAnimation(List, Player, boolean)
     * @see #toFooterAnimation(List, Player, boolean)
     *
     * @return The {@link SendableAnimation} instance associated with the animation.
     *
     * @since 2.0.0
     */
    SendableAnimation toTitleAnimation(Animation animation, Player player, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sends Subtitles to the player.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     *
     * @param animation        The animation to be used.
     * @param player           The player the {@link SendableAnimation} is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @see #toTitleAnimation(Animation, Player, boolean)
     * @see #toActionbarAnimation(Animation, Player, boolean)
     * @see #toHeaderAnimation(Animation, Player, boolean)
     * @see #toFooterAnimation(Animation, Player, boolean)
     *
     * @see #toTitleAnimation(List, Player, boolean)
     * @see #toSubtitleAnimation(List, Player, boolean)
     * @see #toActionbarAnimation(List, Player, boolean)
     * @see #toHeaderAnimation(List, Player, boolean)
     * @see #toFooterAnimation(List, Player, boolean)
     *
     * @return The {@link SendableAnimation} instance associated with the animation.
     *
     * @since 2.0.0
     */
    SendableAnimation toSubtitleAnimation(Animation animation, Player player, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sends Actionbar messages to the player.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     *
     * @param animation        The animation to be used.
     * @param player           The player the {@link SendableAnimation} is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @see #toTitleAnimation(Animation, Player, boolean)
     * @see #toSubtitleAnimation(Animation, Player, boolean)
     * @see #toHeaderAnimation(Animation, Player, boolean)
     * @see #toFooterAnimation(Animation, Player, boolean)
     *
     * @see #toTitleAnimation(List, Player, boolean)
     * @see #toSubtitleAnimation(List, Player, boolean)
     * @see #toActionbarAnimation(List, Player, boolean)
     * @see #toHeaderAnimation(List, Player, boolean)
     * @see #toFooterAnimation(List, Player, boolean)
     *
     * @return The {@link SendableAnimation} instance associated with the animation.
     *
     * @since 2.0.0
     */
    SendableAnimation toActionbarAnimation(Animation animation, Player player, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sets the Tab List header.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     *
     * @param animation        The animation to be used.
     * @param player           The player the {@link SendableAnimation} is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @see #toTitleAnimation(Animation, Player, boolean)
     * @see #toSubtitleAnimation(Animation, Player, boolean)
     * @see #toActionbarAnimation(Animation, Player, boolean)
     * @see #toFooterAnimation(Animation, Player, boolean)
     *
     * @see #toTitleAnimation(List, Player, boolean)
     * @see #toSubtitleAnimation(List, Player, boolean)
     * @see #toActionbarAnimation(List, Player, boolean)
     * @see #toHeaderAnimation(List, Player, boolean)
     * @see #toFooterAnimation(List, Player, boolean)
     *
     * @return The {@link SendableAnimation} instance associated with the animation.
     *
     * @since 2.0.0
     */
    SendableAnimation toHeaderAnimation(Animation animation, Player player, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sets the Tab List footer.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     *
     * @param animation        The animation to be used.
     * @param player           The player the {@link SendableAnimation} is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @see #toTitleAnimation(Animation, Player, boolean)
     * @see #toSubtitleAnimation(Animation, Player, boolean)
     * @see #toActionbarAnimation(Animation, Player, boolean)
     * @see #toHeaderAnimation(Animation, Player, boolean)
     *
     * @see #toTitleAnimation(List, Player, boolean)
     * @see #toSubtitleAnimation(List, Player, boolean)
     * @see #toActionbarAnimation(List, Player, boolean)
     * @see #toHeaderAnimation(List, Player, boolean)
     * @see #toFooterAnimation(List, Player, boolean)
     *
     * @return The {@link SendableAnimation} instance associated with the animation.
     *
     * @since 2.0.0
     */
    SendableAnimation toFooterAnimation(Animation animation, Player player, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sets the title for the player's scoreboard.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     * </p>
     * @param animation         The animation to be used.
     * @param player            The player the {@link SendableAnimation} is associated with.
     * @param withPlaceholders  Whether or not placeholders should be replaced.
     *
     * @see #toScoreboardTitleAnimation(List, Player, boolean)
     * @see #toScoreboardValueAnimation(Animation, Player, int, boolean)
     * @see #toScoreboardValueAnimation(List, Player, int, boolean)
     *
     * @return The {@link SendableAnimation} instance associated with the given player.
     *
     * @since 2.0.0
     */
    SendableAnimation toScoreboardTitleAnimation(Animation animation, Player player, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sets the text value for the player's scoreboard at the given index.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     * </p>
     * @param animation         The animation to be used.
     * @param player            The player the {@link SendableAnimation} is associated with.
     * @param index             The index the text value should be set to.
     * @param withPlaceholders  Whether or not placeholders should be replaced.
     *
     * @return The {@link SendableAnimation} instance associated with the given player.
     *
     * @see #toScoreboardTitleAnimation(Animation, Player, boolean)
     * @see #toScoreboardTitleAnimation(List, Player, boolean)
     * @see #toScoreboardValueAnimation(List, Player, int, boolean)
     *
     * @since 2.0.0
     */
    SendableAnimation toScoreboardValueAnimation(Animation animation, Player player, int index, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sends Titles to the player.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     *
     * @param parts            The parts of the animation the {@link SendableAnimation} will be made off of.
     * @param player           The player the {@link SendableAnimation} is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @return The {@link SendableAnimation} instance associated with the animation.
     *
     * @see #toTitleAnimation(Animation, Player, boolean)
     * @see #toSubtitleAnimation(Animation, Player, boolean)
     * @see #toActionbarAnimation(Animation, Player, boolean)
     * @see #toHeaderAnimation(Animation, Player, boolean)
     * @see #toFooterAnimation(Animation, Player, boolean)
     *
     * @see #toSubtitleAnimation(List, Player, boolean)
     * @see #toActionbarAnimation(List, Player, boolean)
     * @see #toHeaderAnimation(List, Player, boolean)
     * @see #toFooterAnimation(List, Player, boolean)
     *
     * @since 2.0.0
     */
    SendableAnimation toTitleAnimation(List<AnimationPart> parts, Player player, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sends Subtitles to the player.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     *
     * @param parts            The parts of the animation the {@link SendableAnimation} will be made off of.
     * @param player           The player the {@link SendableAnimation} is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @return The {@link SendableAnimation} instance associated with the animation.
     *
     * @see #toTitleAnimation(Animation, Player, boolean)
     * @see #toSubtitleAnimation(Animation, Player, boolean)
     * @see #toActionbarAnimation(Animation, Player, boolean)
     * @see #toHeaderAnimation(Animation, Player, boolean)
     * @see #toFooterAnimation(Animation, Player, boolean)
     *
     * @see #toTitleAnimation(List, Player, boolean)
     * @see #toActionbarAnimation(List, Player, boolean)
     * @see #toHeaderAnimation(List, Player, boolean)
     * @see #toFooterAnimation(List, Player, boolean)
     *
     * @since 2.0.0
     */
    SendableAnimation toSubtitleAnimation(List<AnimationPart> parts, Player player, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sends Actionbar messages to the player.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     *
     * @param parts            The parts of the animation the {@link SendableAnimation} will be made off of.
     * @param player           The player the {@link SendableAnimation} is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @return The {@link SendableAnimation} instance associated with the animation.
     *
     * @see #toTitleAnimation(Animation, Player, boolean)
     * @see #toSubtitleAnimation(Animation, Player, boolean)
     * @see #toActionbarAnimation(Animation, Player, boolean)
     * @see #toHeaderAnimation(Animation, Player, boolean)
     * @see #toFooterAnimation(Animation, Player, boolean)
     *
     * @see #toTitleAnimation(List, Player, boolean)
     * @see #toSubtitleAnimation(List, Player, boolean)
     * @see #toHeaderAnimation(List, Player, boolean)
     * @see #toFooterAnimation(List, Player, boolean)
     *
     * @since 2.0.0
     */
    SendableAnimation toActionbarAnimation(List<AnimationPart> parts, Player player, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sets the Tab List header.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     *
     * @param parts            The parts of the animation the {@link SendableAnimation} will be made off of.
     * @param player           The player the {@link SendableAnimation} is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @return The {@link SendableAnimation} instance associated with the animation.
     *
     * @see #toTitleAnimation(Animation, Player, boolean)
     * @see #toSubtitleAnimation(Animation, Player, boolean)
     * @see #toActionbarAnimation(Animation, Player, boolean)
     * @see #toHeaderAnimation(Animation, Player, boolean)
     * @see #toFooterAnimation(Animation, Player, boolean)
     *
     * @see #toTitleAnimation(List, Player, boolean)
     * @see #toSubtitleAnimation(List, Player, boolean)
     * @see #toActionbarAnimation(List, Player, boolean)
     * @see #toFooterAnimation(List, Player, boolean)
     *
     * @since 2.0.0
     */
    SendableAnimation toHeaderAnimation(List<AnimationPart> parts, Player player, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sets the Tab List footer.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     *
     * @param parts            The parts of the animation the {@link SendableAnimation} will be made off of.
     * @param player           The player the {@link SendableAnimation} is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @return The {@link SendableAnimation} instance associated with the animation.
     *
     * @see #toTitleAnimation(Animation, Player, boolean)
     * @see #toSubtitleAnimation(Animation, Player, boolean)
     * @see #toActionbarAnimation(Animation, Player, boolean)
     * @see #toHeaderAnimation(Animation, Player, boolean)
     * @see #toFooterAnimation(Animation, Player, boolean)
     *
     * @see #toTitleAnimation(List, Player, boolean)
     * @see #toSubtitleAnimation(List, Player, boolean)
     * @see #toActionbarAnimation(List, Player, boolean)
     * @see #toHeaderAnimation(List, Player, boolean)
     *
     * @since 2.0.0
     */
    SendableAnimation toFooterAnimation(List<AnimationPart> parts, Player player, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sets the title for the player's scoreboard.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     * </p>
     * @param parts             The animation parts to be used.
     * @param player            The player the {@link List} of {@link AnimationPart}s is associated with.
     * @param withPlaceholders  Whether or not placeholders should be replaced.
     *
     * @see #toScoreboardTitleAnimation(Animation, Player, boolean)
     * @see #toScoreboardValueAnimation(Animation, Player, int, boolean)
     * @see #toScoreboardValueAnimation(List, Player, int, boolean)
     *
     * @return The {@link List} of {@link AnimationPart}s associated with the given player.
     *
     * @since 2.0.0
     */
    SendableAnimation toScoreboardTitleAnimation(List<AnimationPart> parts, Player player, boolean withPlaceholders);

    /**
     * Creates a {@link SendableAnimation} that sets the text value for the player's scoreboard at the given index.
     * <p>
     * If <code>withPlaceholders</code> is true, placeholders will be replaced.
     * </p>
     * @param parts         The animation to be used.
     * @param player            The player the {@link SendableAnimation} is associated with.
     * @param index             The index the text value should be set to.
     * @param withPlaceholders  Whether or not placeholders should be replaced.
     *
     * @return The {@link SendableAnimation} instance associated with the given player.
     *
     * @see #toScoreboardTitleAnimation(Animation, Player, boolean)
     * @see #toScoreboardTitleAnimation(List, Player, boolean)
     * @see #toScoreboardValueAnimation(Animation, Player, int, boolean)
     *
     * @since 2.0.0
     */
    SendableAnimation toScoreboardValueAnimation(List<AnimationPart> parts, Player player, int index, boolean withPlaceholders);

    /**
     * Creates an {@link AnimationPart} from a {@link String}
     *
     * @param text The {@link String} that will be the value of the {@link AnimationPart}.
     *
     * @return The {@link AnimationPart} instance associated with the input text.
     *
     * @see #toAnimationPart(Animation)
     * @see #toAnimationParts(String)
     *
     * @since 2.0.0
     */
    AnimationPart<String> toAnimationPart(String text);

    /**
     * Creates an {@link AnimationPart} from an {@link Animation}
     *
     * @param animation The {@link Animation} that will be the value of the {@link AnimationPart}.
     *
     * @return The {@link AnimationPart} instance associated with the input text.
     *
     * @see #toAnimationPart(String)
     * @see #toAnimationParts(String)
     *
     * @since 2.0.0
     */
    AnimationPart<Animation> toAnimationPart(Animation animation);

    /**
     * Converts the text found to an immutable list of {@link AnimationPart}s with values of {@link String} or {@link Animation}
     * <p>
     * All plain text that follows the animation or script patterns will be converted to their respected Animations.
     *
     * @param text The text to be converted.
     *
     * @return     The instance of the immutable list with all of the Animation parts, that were converted from the text.
     *
     * @see #toAnimationPart(Animation)
     * @see #toAnimationPart(String)
     *
     * @since 2.0.0
     */
    List<AnimationPart> toAnimationParts(String text);

    /**
     * Creates an {@link Animation} from the input text and timings values.
     * <p>
     * All timings are measured in ticks.
     *
     * @param text    The input text to be displayed.
     * @param fadeIn  If the frame is being used in a title,
     *                this is the time it takes for the title to fade onto the screen.
     *                If the frame is not being used in a title, it will be added onto the stay time.
     * @param stay    The time it takes for the frame to stay on the screen.
     *                (The only exception is for Actionbar messages)
     * @param fadeOut If the frame is being used in a title,
     *                this is the time it takes for the title to fade off of the screen.
     *                If this frame is not being used in a title, it will be added onto the stay time.
     *
     * @return        An instance of {@link AnimationFrame} that has the given values assigned.
     *
     * @since 2.0.0
     */
    AnimationFrame createAnimationFrame(String text, int fadeIn, int stay, int fadeOut);

    /**
     * Creates an animation from an Array of {@link String}.
     * <p>
     * Each line must follow the pattern <code>[fade-in;stay;fade-out]text</code>
     * <p>
     * fade-in = The time it takes for a title to fade onto the screen.
     *           If the frame is not used in a title, it will be added onto the stay time instead.
     * <p>
     * stay = The time it takes for the frame to stay on the screen.
     * <p>
     * fade-out = The time it takes for a title to fade off of the screen.
     *            If the frame is not used in a title, it will be added onto the stay time instead.
     *
     * @param frames The frames that will be converted to {@link String}
     *
     * @return The {@link Animation} generated from the frames.
     *
     * @see #fromTextFile(File)
     * @see #fromJavaScript(String, String)
     *
     * @since 2.0.0
     */
    Animation fromText(String... frames);

    /**
     * Creates an animation from a File. Each line of the file will be converted to an Array
     * and fed into the {@link #fromText(String...)}
     * <p>
     * Each line must follow the pattern <code>[fade-in;stay;fade-out]text</code>
     * <p>
     * fade-in = The time it takes for a title to fade onto the screen.
     *           If the frame is not used in a title, it will be added onto the stay time instead.
     * <p>
     * stay = The time it takes for the frame to stay on the screen.
     * <p>
     * fade-out = The time it takes for a title to fade off of the screen.
     *            If the frame is not used in a title, it will be added onto the stay time instead.
     *
     * @param file The file that will be read and converted to an {@link Animation}.
     *
     * @return     The {@link Animation} generated from the frames.
     *
     * @see #fromText(String...)
     * @see #fromJavaScript(String, String)
     *
     * @since 2.0.0
     */
    Animation fromTextFile(File file);

    /**
     * Creates an {@link Animation} from the name of a loaded JavaScript animation,
     * with the given input text.
     *
     * @param name  The name of the JavaScript animation.
     * @param input The input text to be converted to an {@link Animation}
     *
     * @return The {@link Animation} generated from the JavaScript.
     *
     * @see #fromText(String...)
     * @see #fromTextFile(File)
     *
     * @since 2.0.0
     */
    Animation fromJavaScript(String name, String input);

    // Title

    /**
     * Sends a title message to the given {@link Player} with the given title.
     * This will also use the last sent timings.
     *
     * @param player The player the title should be sent to.
     * @param title  The title to be displayed.
     *
     * @see #sendTitle(Player, String, int, int, int)
     * @see #sendTitleWithPlaceholders(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendSubtitle(Player, String)
     * @see #sendSubtitle(Player, String, int, int, int)
     * @see #sendSubtitleWithPlaceholders(Player, String)
     * @see #sendSubtitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendTitles(Player, String, String)
     * @see #sendTitles(Player, String, String, int, int, int)
     * @see #sendTitlesWithPlaceholders(Player, String, String)
     * @see #sendTitlesWithPlaceholders(Player, String, String, int, int, int)
     *
     * @since 2.0.0
     */
    void sendTitle(Player player, String title);

    /**
     * Sends a title message to the given {@link Player} with the given timings.
     *
     * @param player  The player the title should be sent to.
     * @param title   The title to be sent to the {@link Player}.
     * @param fadeIn  The time it takes for the title to fade onto the screen.
     * @param stay    The time it takes for the title to stay on the screen.
     * @param fadeOut The time it takes for the title to fade off of the screen.
     *
     * @see #sendTitle(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendSubtitle(Player, String)
     * @see #sendSubtitle(Player, String, int, int, int)
     * @see #sendSubtitleWithPlaceholders(Player, String)
     * @see #sendSubtitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendTitles(Player, String, String)
     * @see #sendTitles(Player, String, String, int, int, int)
     * @see #sendTitlesWithPlaceholders(Player, String, String)
     * @see #sendTitlesWithPlaceholders(Player, String, String, int, int, int)
     *
     * @since 2.0.0
     */
    void sendTitle(Player player, String title, int fadeIn, int stay, int fadeOut);

    /**
     * Sends a title message to the given {@link Player} where all placeholders have been replaced.
     * This will also use the last sent timings.
     *
     * @param player The player the title should be sent to.
     * @param title  The title to be sent to the {@link Player}.
     *
     * @see #sendTitle(Player, String)
     * @see #sendTitle(Player, String, int, int, int)
     * @see #sendTitleWithPlaceholders(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendSubtitle(Player, String)
     * @see #sendSubtitle(Player, String, int, int, int)
     * @see #sendSubtitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendTitles(Player, String, String)
     * @see #sendTitles(Player, String, String, int, int, int)
     * @see #sendTitlesWithPlaceholders(Player, String, String)
     * @see #sendTitlesWithPlaceholders(Player, String, String, int, int, int)
     *
     * @since 2.0.0
     */
    void sendTitleWithPlaceholders(Player player, String title);

    /**
     * Sends a title message to the given {@link Player} with the given timings where all placeholders have been replaced
     * and with the given timings.
     *
     * @param player  The player the title should be sent to.
     * @param title   The title to be sent to the {@link Player}.
     * @param fadeIn  The time it takes for the title to fade onto the screen.
     * @param stay    The time it takes for the title to stay on the screen.
     * @param fadeOut The time it takes for the title to fade off of the screen.
     *
     * @see #sendTitle(Player, String)
     * @see #sendTitle(Player, String, int, int, int)
     * @see #sendTitleWithPlaceholders(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendSubtitle(Player, String)
     * @see #sendSubtitle(Player, String, int, int, int)
     * @see #sendSubtitleWithPlaceholders(Player, String)
     *
     * @see #sendTitles(Player, String, String)
     * @see #sendTitles(Player, String, String, int, int, int)
     * @see #sendTitlesWithPlaceholders(Player, String, String)
     * @see #sendTitlesWithPlaceholders(Player, String, String, int, int, int)
     *
     * @since 2.0.0
     */
    void sendTitleWithPlaceholders(Player player, String title, int fadeIn, int stay, int fadeOut);

    /**
     * Sends a subtitle message to the given {@link Player}.
     * This will also use the last sent timings.
     *
     * @param player    The player the subtitle should be sent to.
     * @param subtitle  The subtitle to be displayed.
     *
     * @see #sendTitle(Player, String)
     * @see #sendTitle(Player, String, int, int, int)
     * @see #sendTitleWithPlaceholders(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendSubtitle(Player, String, int, int, int)
     * @see #sendSubtitleWithPlaceholders(Player, String)
     * @see #sendSubtitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendTitles(Player, String, String)
     * @see #sendTitles(Player, String, String, int, int, int)
     * @see #sendTitlesWithPlaceholders(Player, String, String)
     * @see #sendTitlesWithPlaceholders(Player, String, String, int, int, int)
     *
     * @since 2.0.0
     */
    void sendSubtitle(Player player, String subtitle);

    /**
     * Sends a subtitle message to the given {@link Player} with the given timings as well as the given subtitles.
     *
     * @param player    The player the subtitle should be sent to.
     * @param subtitle  The subtitle to be displayed.
     * @param fadeIn    The time it takes for the subtitle to fade onto the screen.
     * @param stay      The time it takes for the subtitle to stay on the screen.
     * @param fadeOut   The time it takes for the subtitle to fade off of the screen.
     *
     * @see #sendTitle(Player, String)
     * @see #sendTitle(Player, String, int, int, int)
     * @see #sendTitleWithPlaceholders(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendSubtitle(Player, String)
     * @see #sendSubtitleWithPlaceholders(Player, String)
     * @see #sendSubtitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendTitles(Player, String, String)
     * @see #sendTitles(Player, String, String, int, int, int)
     * @see #sendTitlesWithPlaceholders(Player, String, String)
     * @see #sendTitlesWithPlaceholders(Player, String, String, int, int, int)
     *
     * @since 2.0.0
     */
    void sendSubtitle(Player player, String subtitle, int fadeIn, int stay, int fadeOut);

    /**
     * Sends a subtitle message to the given {@link Player} where all placeholders have been replaced.
     * This will also use the last sent timings.
     *
     * @param player    The player the subtitle should be sent to.
     * @param subtitle  The subtitle to be displayed.
     *
     * @see #sendTitle(Player, String)
     * @see #sendTitle(Player, String, int, int, int)
     * @see #sendTitleWithPlaceholders(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendSubtitle(Player, String)
     * @see #sendSubtitle(Player, String, int, int, int)
     * @see #sendSubtitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendTitles(Player, String, String)
     * @see #sendTitles(Player, String, String, int, int, int)
     * @see #sendTitlesWithPlaceholders(Player, String, String)
     * @see #sendTitlesWithPlaceholders(Player, String, String, int, int, int)
     *
     * @since 2.0.0
     */
    void sendSubtitleWithPlaceholders(Player player, String subtitle);

    /**
     * Sends a subtitle message to the given {@link Player} where all placeholders have been replaced
     * and with the given timings.
     *
     * @param player    The player the subtitle should be sent to.
     * @param subtitle  The subtitle to be displayed.
     * @param fadeIn    The time it takes for the subtitle to fade onto the screen.
     * @param stay      The time it takes for the subtitle to stay on the screen.
     * @param fadeOut   The time it takes for the subtitle to fade off of the screen.
     *
     * @see #sendTitle(Player, String)
     * @see #sendTitle(Player, String, int, int, int)
     * @see #sendTitleWithPlaceholders(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendSubtitle(Player, String)
     * @see #sendSubtitleWithPlaceholders(Player, String)
     * @see #sendSubtitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendTitles(Player, String, String)
     * @see #sendTitles(Player, String, String, int, int, int)
     * @see #sendTitlesWithPlaceholders(Player, String, String)
     * @see #sendTitlesWithPlaceholders(Player, String, String, int, int, int)
     *
     * @since 2.0.0
     */
    void sendSubtitleWithPlaceholders(Player player, String subtitle, int fadeIn, int stay, int fadeOut);

    /**
     * Sends a title and a subtitle message to the given {@link Player}.
     *
     * @param player    The player the titles should be sent to.
     * @param title     The title to be displayed.
     * @param subtitle  The subtitle to be displayed.
     *
     * @see #sendTitle(Player, String)
     * @see #sendTitle(Player, String, int, int, int)
     * @see #sendTitleWithPlaceholders(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendSubtitle(Player, String)
     * @see #sendSubtitle(Player, String, int, int, int)
     * @see #sendSubtitleWithPlaceholders(Player, String)
     * @see #sendSubtitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendTitles(Player, String, String, int, int, int)
     * @see #sendTitlesWithPlaceholders(Player, String, String)
     * @see #sendTitlesWithPlaceholders(Player, String, String, int, int, int)
     *
     * @since 2.0.0
     */
    void sendTitles(Player player, String title, String subtitle);

    /**
     * Sends a title and a subtitle message to the given {@link Player} with the given timings.
     *
     * @param player    The player the titles should be sent to.
     * @param title     The title to be displayed.
     * @param subtitle  The subtitle to be displayed.
     * @param fadeIn    The time it takes for the titles to fade onto the screen.
     * @param stay      The time it takes for the titles to stay on the screen.
     * @param fadeOut   The time it takes for the titles to fade off of the screen.
     *
     * @see #sendTitle(Player, String)
     * @see #sendTitle(Player, String, int, int, int)
     * @see #sendTitleWithPlaceholders(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendSubtitle(Player, String)
     * @see #sendSubtitle(Player, String, int, int, int)
     * @see #sendSubtitleWithPlaceholders(Player, String)
     * @see #sendSubtitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendTitles(Player, String, String)
     * @see #sendTitlesWithPlaceholders(Player, String, String)
     * @see #sendTitlesWithPlaceholders(Player, String, String, int, int, int)
     *
     * @since 2.0.0
     */
    void sendTitles(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    /**
     * Sends a title and a subtitle message to the given {@link Player} where all placeholders are replaced.
     *
     * @param player    The player the titles should be sent to.
     * @param title     The title to be displayed.
     * @param subtitle  The subtitle to be displayed.
     *
     * @see #sendTitle(Player, String)
     * @see #sendTitle(Player, String, int, int, int)
     * @see #sendTitleWithPlaceholders(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendSubtitle(Player, String)
     * @see #sendSubtitle(Player, String, int, int, int)
     * @see #sendSubtitleWithPlaceholders(Player, String)
     * @see #sendSubtitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendTitles(Player, String, String)
     * @see #sendTitles(Player, String, String, int, int, int)
     * @see #sendTitlesWithPlaceholders(Player, String, String, int, int, int)
     *
     * @since 2.0.0
     */
    void sendTitlesWithPlaceholders(Player player, String title, String subtitle);

    /**
     * Sends a title and a subtitle message to the given {@link Player} where all placeholders are replaced
     * and with the given timings.
     *
     * @param player    The player the titles should be sent to.
     * @param title     The title to be displayed.
     * @param subtitle  The subtitle to be displayed.
     * @param fadeIn    The time it takes for the titles to fade onto the screen.
     * @param stay      The time it takes for the titles to stay on the screen.
     * @param fadeOut   The time it takes for the titles to fade off of the screen.
     *
     * @see #sendTitle(Player, String)
     * @see #sendTitle(Player, String, int, int, int)
     * @see #sendTitleWithPlaceholders(Player, String)
     * @see #sendTitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendSubtitle(Player, String)
     * @see #sendSubtitle(Player, String, int, int, int)
     * @see #sendSubtitleWithPlaceholders(Player, String)
     * @see #sendSubtitleWithPlaceholders(Player, String, int, int, int)
     *
     * @see #sendTitles(Player, String, String)
     * @see #sendTitles(Player, String, String, int, int, int)
     * @see #sendTitlesWithPlaceholders(Player, String, String)
     *
     * @since 2.0.0
     */
    void sendTitlesWithPlaceholders(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    /**
     * Sets the timings for all of the next titles and subtitles sent to the {@link Player}.
     *
     * @param player The player the timings should be set for.
     * @param fadeIn  The time it takes for all titles sent afterwards to fade onto the screen.
     * @param stay    The time it takes for all titles sent afterwards to stay on the screen.
     * @param fadeOut The time it takes for all titles sent afterwards to to fade off of the screen.
     *
     * @since 2.0.0
     */
    void sendTimings(Player player, int fadeIn, int stay, int fadeOut);

    /**
     * Removes a title that might be on the player's screen.
     *
     * @param player The player to clear.
     *
     * @see #clearSubtitle(Player)
     * @see #clearTitles(Player)
     *
     * @since 2.0.0
     */
    void clearTitle(Player player);

    /**
     * Removes a subtitle that might be on the player's screen.
     *
     * @param player The player to clear.
     *
     * @see #clearTitle(Player)
     * @see #clearTitles(Player)
     *
     * @since 2.0.0
     */
    void clearSubtitle(Player player);

    /**
     * Removes a title and a subtitle that might be on the player's screen.
     *
     * @param player The player to clear.
     *
     * @see #clearTitle(Player)
     * @see #clearSubtitle(Player)
     *
     * @since 2.0.0
     */
    void clearTitles(Player player);

    // Actionbar

    /**
     * Sends an actionbar message to the {@link Player}.
     *
     * @param player The player to send the actionbar message.
     * @param text   The text to be sent.
     *
     * @see #sendActionbarWithPlaceholders(Player, String)
     *
     * @since 2.0.0
     */
    void sendActionbar(Player player, String text);

    /**
     * Sends an actionbar message to the {@link Player} where all placeholders are replaced.
     *
     * @param player The player to send the actionbar message.
     * @param text   The text to be sent.
     *
     * @see #sendActionbar(Player, String)
     *
     * @since 2.0.0
     */
    void sendActionbarWithPlaceholders(Player player, String text);

    /**
     * Removes an actionbar message that might be on the player's screen.
     *
     * @param player The player to clear.
     *
     * @since 2.0.0
     */
    void clearActionbar(Player player);

    // Player list

    /**
     * Set the player list header for the given {@link Player}.
     *
     * @param player The player to set the header for.
     * @param header The header to be displayed.
     *
     * @see #getHeader(Player)
     * @see #setHeaderWithPlaceholders(Player, String)
     *
     * @see #getFooter(Player)
     * @see #setFooter(Player, String)
     * @see #setFooterWithPlaceholders(Player, String)
     *
     * @see #setHeaderAndFooter(Player, String, String)
     * @see #setHeaderAndFooterWithPlaceholders(Player, String, String)
     *
     * @since 2.0.0
     */
    void setHeader(Player player, String header);

    /**
     * Sets the player list header for the given {@link Player} where all placeholders are replaced.
     *
     * @param player The player to set the header for.
     * @param header The header to be displayed.
     *
     * @see #getHeader(Player)
     * @see #setHeader(Player, String)
     *
     * @see #getFooter(Player)
     * @see #setFooter(Player, String)
     * @see #setFooterWithPlaceholders(Player, String)
     *
     * @see #setHeaderAndFooter(Player, String, String)
     * @see #setHeaderAndFooterWithPlaceholders(Player, String, String)
     *
     * @since 2.0.0
     */
    void setHeaderWithPlaceholders(Player player, String header);

    /**
     * Gets the player list header that is currently being displayed for the given {@link Player}.
     *
     * @param player The player to get the header from.
     *
     * @return       The header that is being displayed.
     *
     * @see #setHeader(Player, String)
     * @see #setHeaderWithPlaceholders(Player, String)
     *
     * @see #getFooter(Player)
     * @see #setFooter(Player, String)
     * @see #setFooterWithPlaceholders(Player, String)
     *
     * @see #setHeaderAndFooter(Player, String, String)
     * @see #setHeaderAndFooterWithPlaceholders(Player, String, String)
     *
     * @since 2.0.0
     */
    String getHeader(Player player);

    /**
     * Sets the player list footer for the given {@link Player}.
     *
     * @param player The player to set the footer for.
     * @param footer The footer to be displayed.
     *
     * @see #getHeader(Player)
     * @see #setHeader(Player, String)
     * @see #setHeaderWithPlaceholders(Player, String)
     *
     * @see #getFooter(Player)
     * @see #setFooterWithPlaceholders(Player, String)
     *
     * @see #setHeaderAndFooter(Player, String, String)
     * @see #setHeaderAndFooterWithPlaceholders(Player, String, String)
     *
     * @since 2.0.0
     */
    void setFooter(Player player, String footer);

    /**
     * Sets the player list footer for the given {@link Player} where all placeholders are replaced.
     *
     * @param player The player to set the footer for.
     * @param footer The footer to be displayed.
     *
     * @see #getHeader(Player)
     * @see #setHeader(Player, String)
     * @see #setHeaderWithPlaceholders(Player, String)
     *
     * @see #getFooter(Player)
     * @see #setFooter(Player, String)
     *
     * @see #setHeaderAndFooter(Player, String, String)
     * @see #setHeaderAndFooterWithPlaceholders(Player, String, String)
     *
     * @since 2.0.0
     */
    void setFooterWithPlaceholders(Player player, String footer);

    /**
     * Gets the player list footer that is currently being displayed for the given {@link Player}.
     *
     * @param player The player to get the header from.
     *
     * @return       The footer that is being displayed.
     *
     * @see #getHeader(Player)
     * @see #setHeader(Player, String)
     * @see #setHeaderWithPlaceholders(Player, String)
     *
     * @see #setFooter(Player, String)
     * @see #setFooterWithPlaceholders(Player, String)
     *
     * @see #setHeaderAndFooter(Player, String, String)
     * @see #setHeaderAndFooterWithPlaceholders(Player, String, String)
     *
     * @since 2.0.0
     */
    String getFooter(Player player);

    /**
     * Sets the player list header and footer for the given {@link Player}.
     *
     * @param player The player to get and set the footer for.
     * @param header The header to be displayed.
     * @param footer The footer to be displayed.
     *
     * @see #getHeader(Player)
     * @see #setHeader(Player, String)
     * @see #setHeaderWithPlaceholders(Player, String)
     *
     * @see #getFooter(Player)
     * @see #setFooter(Player, String)
     * @see #setFooterWithPlaceholders(Player, String)
     *
     * @see #setHeaderAndFooterWithPlaceholders(Player, String, String)
     *
     * @since 2.0.0
     */
    void setHeaderAndFooter(Player player, String header, String footer);

    /**
     * Sets the player list header and footer for the given {@link Player} where all placeholders are replaced.
     *
     * @param player The player to get and set the footer for.
     * @param header The header to be displayed.
     * @param footer The footer to be displayed.
     *
     * @see #getHeader(Player)
     * @see #setHeader(Player, String)
     * @see #setHeaderWithPlaceholders(Player, String)
     *
     * @see #getFooter(Player)
     * @see #setFooter(Player, String)
     * @see #setFooterWithPlaceholders(Player, String)
     *
     * @see #setHeaderAndFooter(Player, String, String)
     *
     * @since 2.0.0
     */
    void setHeaderAndFooterWithPlaceholders(Player player, String header, String footer);

    // Sidebar

    /**
     * Gives the {@link Player} a scoreboard as a sidebar.
     * <p>
     * The scoreboard won't be displayed until at least one value has been added.
     *
     * @param player The player the scoreboard should be given to.
     *
     * @see #removeScoreboard(Player)
     * @see #hasScoreboard(Player)
     * @see #setScoreboardValue(Player, int, String)
     * @see #setScoreboardValueWithPlaceholders(Player, int, String)
     *
     * @since 2.0.0
     */
    void giveScoreboard(Player player);

    /**
     * Removes the scoreboard sidebar from the given player.
     *
     * @param player The player the scoreboard should be removed from.
     *
     * @see #giveScoreboard(Player)
     * @see #hasScoreboard(Player)
     *
     * @since 2.0.0
     */
    void removeScoreboard(Player player);

    /**
     * Checks whether or not the given {@link Player} has a scoreboard assigned.
     *
     * @param player The player to check for.
     *
     * @return Whether or not the player has a scoreboard assigned.
     *
     * @see #giveScoreboard(Player)
     * @see #removeScoreboard(Player)
     *
     * @since 2.0.0.
     */
    boolean hasScoreboard(Player player);

    /**
     * Sets the title of the given {@link Player}'s scoreboard, if they have one assigned.
     *
     * @param player The player to set the title for.
     * @param title The title to be set. (Will be trimmed to a maximum of 32 characters)
     *
     * @see #giveScoreboard(Player)
     * @see #hasScoreboard(Player)
     *
     * @see #setScoreboardTitleWithPlaceholders(Player, String)
     * @see #getScoreboardTitle(Player)
     *
     * @since 2.0.0
     */
    void setScoreboardTitle(Player player, String title);

    /**
     * Sets the title of the given {@link Player}'s scoreboard, if they have one assigned.
     * <p>
     * If placeholders are available, the title will try to replace them.
     *
     * @param player The player to set the title for.
     * @param title The title to be set. (Will be trimmed to a maximum of 32 characters)
     *
     * @see #giveScoreboard(Player)
     * @see #hasScoreboard(Player)
     *
     * @see #setScoreboardTitle(Player, String)
     * @see #getScoreboardTitle(Player)
     *
     * @since 2.0.0
     */
    void setScoreboardTitleWithPlaceholders(Player player, String title);

    /**
     * Gets the title of the given {@link Player}'s scoreboard.
     *
     * @param player The player to get the title from.
     *
     * @return The title of the player's scoreboard, if no scoreboard is assigned this will return null.
     *
     * @see #giveScoreboard(Player)
     * @see #hasScoreboard(Player)
     *
     * @see #setScoreboardTitle(Player, String)
     * @see #setScoreboardTitleWithPlaceholders(Player, String)
     *
     * @since 2.0.0
     */
    String getScoreboardTitle(Player player);

    /**
     * Sets the text value of the given {@link Player}'s scoreboard at the given index.
     *
     * @param player The player to set the text value for.
     * @param index The index at which the text should be set. 1-15 (1 and 15 inclusive)
     * @param value The text value that should be set. (will be trimmed to a maximum of 40 characters)
     *
     * @see #giveScoreboard(Player)
     * @see #hasScoreboard(Player)
     *
     * @see #setScoreboardValueWithPlaceholders(Player, int, String)
     * @see #getScoreboardValue(Player, int)
     * @see #removeScoreboard(Player)
     *
     * @since 2.0.0
     */
    void setScoreboardValue(Player player, int index, String value);

    /**
     * Sets the text value of the given {@link Player}'s scoreboard at the given index.
     * <p>
     * If placeholders are available, the value will try to replace them.
     *
     * @param player The player to set the text value for.
     * @param index The index at which the text should be set. 1-15 (1 and 15 inclusive)
     * @param value The text value that should be set.
     *
     * @see #giveScoreboard(Player)
     * @see #hasScoreboard(Player)
     *
     * @see #setScoreboardValue(Player, int, String)
     * @see #getScoreboardValue(Player, int)
     * @see #removeScoreboardValue(Player, int)
     *
     * @since 2.0.0
     */
    void setScoreboardValueWithPlaceholders(Player player, int index, String value);

    /**
     * Gets the text value of the given {@link Player}'s scoreboard at the given index.
     *
     * @param player The player to get the text value from.
     * @param index The index at which the text value is present.
     *
     * @return The text value of the given index. If no text value is present, this will return null.
     *
     * @see #giveScoreboard(Player)
     * @see #hasScoreboard(Player)
     *
     * @see #setScoreboardValue(Player, int, String)
     * @see #setScoreboardValueWithPlaceholders(Player, int, String)
     * @see #removeScoreboardValue(Player, int)
     *
     * @since 2.0.0
     */
    String getScoreboardValue(Player player, int index);

    /**
     * Removes the text value of the given {@link Player}'s scoreboardat the given index.
     *
     * @param player The player to remove the text value from.
     * @param index  The index at which the text value should be removed.
     *
     * @see #giveScoreboard(Player)
     * @see #hasScoreboard(Player)
     *
     * @see #setScoreboardValue(Player, int, String)
     * @see #setScoreboardValueWithPlaceholders(Player, int, String)
     * @see #getScoreboardValue(Player, int)
     *
     * @since 2.0.0
     */
    void removeScoreboardValue(Player player, int index);
}
