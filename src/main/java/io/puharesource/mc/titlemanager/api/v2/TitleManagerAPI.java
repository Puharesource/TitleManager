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
     * @return The {@link Animation} generated from the frames.
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
     * @param name The name of the JavaScript animation.
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

    void sendTitle(Player player, String title);
    void sendTitle(Player player, String title, int fadeIn, int stay, int fadeOut);
    void sendTitleWithPlaceholders(Player player, String title);
    void sendTitleWithPlaceholders(Player player, String title, int fadeIn, int stay, int fadeOut);

    void sendSubtitle(Player player, String subtitle);
    void sendSubtitle(Player player, String subtitle, int fadeIn, int stay, int fadeOut);
    void sendSubtitleWithPlaceholders(Player player, String subtitle);
    void sendSubtitleWithPlaceholders(Player player, String subtitle, int fadeIn, int stay, int fadeOut);

    void sendTitles(Player player, String title, String subtitle);
    void sendTitles(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);
    void sendTitlesWithPlaceholders(Player player, String title, String subtitle);
    void sendTitlesWithPlaceholders(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    void sendTimings(Player player, int fadeIn, int stay, int fadeOut);

    void clearTitle(Player player);
    void clearSubtitle(Player player);
    void clearTitles(Player player);

    // Actionbar

    void sendActionbar(Player player, String text);
    void sendActionbarWithPlaceholders(Player player, String text);

    void clearActionbar(Player player);

    // Player list

    void setHeader(Player player, String header);
    void setHeaderWithPlaceholders(Player player, String header);
    String getHeader(Player player);

    void setFooter(Player player, String footer);
    void setFooterWithPlaceholders(Player player, String footer);
    String getFooter(Player player);

    void setHeaderAndFooter(Player player, String header, String footer);
    void setHeaderAndFooterWithPlaceholders(Player player, String header, String footer);
}
