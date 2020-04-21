package io.puharesource.mc.titlemanager.api.v2

import io.puharesource.mc.titlemanager.api.v2.animation.Animation
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationFrame
import io.puharesource.mc.titlemanager.api.v2.animation.AnimationPart
import io.puharesource.mc.titlemanager.api.v2.animation.SendableAnimation
import org.bukkit.entity.Player
import java.io.File

/**
 * The API for TitleManager<p/>
 *
 * Here you'll find all methods available in the API.
 *
 * @since 2.0.0
 */
interface TitleManagerAPI {
    /**
     * Replaces the text with any placeholders that follow the pattern `%{my-placeholder}`
     * as well as any placeholders following the pattern `%{my-placeholder:my-parameter}`.
     *
     *
     * If [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) is installed and enabled,
     * any placeholders available within PlaceholderAPI will be replaced before TitleManager replaces its own.
     *
     * @param player The player that any player specific placeholders will be matched to.
     * @param text The text that will be replaced.
     *
     * @return The input text with all available placeholders replaced.
     * If no placeholders were found the input text will be returned instead.
     *
     * @see .containsPlaceholders
     * @see .containsPlaceholder
     * @since 2.0.0
     */
    fun replaceText(player: Player, text: String): String

    /**
     * Checks if the input text contains any registered placeholders following the pattern `%{my-placeholder}`
     * as well as any placeholders following the pattern `%{my-placeholder:my-parameter}`.
     * Though the placeholder does need to be registered with the plugin.
     *
     * @param text The text that will be matched to the patterns.
     *
     * @return `true` if the text returned with a match and `false` if it doesn't.
     *
     * @see .replaceText
     * @see .containsPlaceholder
     * @since 2.0.0
     */
    fun containsPlaceholders(text: String): Boolean

    /**
     * Checks if the input text contains the pattern `%{my-placeholder}` or the pattern
     * `%{my-placeholder:parameter}` where `my-placeholder` is the placeholder parameter.
     *
     * @param text The text that will be matched to the pattern.
     * @param placeholder The placeholder that will be matched for.
     *
     * @return `true` if the text returned with a match and `false` if it doesn't.
     *
     * @see .replaceText
     * @see .containsPlaceholders
     * @since 2.0.0
     */
    fun containsPlaceholder(text: String, placeholder: String): Boolean

    /**
     * Checks if the input text contains any registered animations following the pattern `${my-animation}`
     * as well as any animations or scripts following the pattern `${my-animation:my-parameter}`.
     * Though the animation or script does need to be registered with the plugin.
     *
     * @param text The text that will be matched to the patterns.
     *
     * @return `true` if the text returned with a match and `false` if it doesn't.
     *
     * @see .containsAnimation
     * @see .addAnimation
     * @see .removeAnimation
     * @see .toAnimationPart
     * @see .toAnimationParts
     * @see .getRegisteredAnimations
     * @see .getRegisteredScripts
     * @since 2.0.0
     */
    fun containsAnimations(text: String): Boolean

    /**
     * Checks if the input text contains the pattern `${my-animation}` or the pattern
     * `${my-animation:parameter}` where `my-animation` is the animation parameter.
     *
     * @param text The text that will be matched.
     * @param animation The animation or script that will be matched for.
     *
     * @return `true` if the text returned with a match and `false` if it doesn't.
     *
     * @see .containsAnimations
     * @see .addAnimation
     * @see .removeAnimation
     * @see .toAnimationPart
     * @see .toAnimationParts
     * @see .getRegisteredAnimations
     * @see .getRegisteredScripts
     * @since 2.0.0
     */
    fun containsAnimation(text: String, animation: String): Boolean

    /**
     * Gets an immutable [Map] where the key is a [String] and value is a [Animation].
     *
     * @return Gets the immutable [Map] results.
     *
     * @see .containsAnimation
     * @see .containsAnimations
     * @see .addAnimation
     * @see .removeAnimation
     * @see .toAnimationPart
     * @see .toAnimationParts
     * @see .getRegisteredScripts
     * @since 2.0.0
     */
    fun getRegisteredAnimations(): Map<String, Animation>

    /**
     * Gets an immutable [Set] of all of the Script names registered.
     *
     * @return Gets the immutable $[Set] results.
     *
     * @see .containsAnimation
     * @see .containsAnimations
     * @see .addAnimation
     * @see .removeAnimation
     * @see .toAnimationPart
     * @see .toAnimationParts
     * @see .getRegisteredAnimations
     * @since 2.0.0
     */
    fun getRegisteredScripts(): Set<String>

    /**
     * Adds an animation to the plugin.
     *
     * @param id The id of the animation (case-insensitive).
     * @param animation The animation to be added.
     *
     * @see .containsAnimations
     * @see .removeAnimation
     * @see .toAnimationPart
     * @see .toAnimationParts
     * @see .getRegisteredAnimations
     * @see .getRegisteredScripts
     * @since 2.0.0
     */
    fun addAnimation(id: String, animation: Animation)

    /**
     * Removes an animation from the plugin.
     *
     * @param id The id of the animation that should be removed (case-insensitive).
     *
     * @see .containsAnimations
     * @see .addAnimation
     * @see .toAnimationPart
     * @see .toAnimationParts
     * @see .getRegisteredAnimations
     * @see .getRegisteredScripts
     * @since 2.0.0
     */
    fun removeAnimation(id: String)

    /**
     * Creates a [SendableAnimation] that sends Titles to the player.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param animation The animation to be used.
     * @param player The player the [SendableAnimation] is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @return The [SendableAnimation] instance associated with the animation.
     *
     * @since 2.0.0
     */
    fun toTitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sends Subtitles to the player.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param animation The animation to be used.
     * @param player The player the [SendableAnimation] is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @see .toTitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @return The [SendableAnimation] instance associated with the animation.
     *
     * @since 2.0.0
     */
    fun toSubtitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sends Actionbar messages to the player.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param animation The animation to be used.
     * @param player The player the [SendableAnimation] is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @return The [SendableAnimation] instance associated with the animation.
     *
     * @since 2.0.0
     */
    fun toActionbarAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sets the Tab List header.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param animation The animation to be used.
     * @param player The player the [SendableAnimation] is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toFooterAnimation
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @return The [SendableAnimation] instance associated with the animation.
     *
     * @since 2.0.0
     */
    fun toHeaderAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sets the Tab List footer.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param animation The animation to be used.
     * @param player The player the [SendableAnimation] is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @return The [SendableAnimation] instance associated with the animation.
     *
     * @since 2.0.0
     */
    fun toFooterAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sets the title for the player's scoreboard.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param animation The animation to be used.
     * @param player The player the [SendableAnimation] is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @see .toScoreboardTitleAnimation
     * @see .toScoreboardValueAnimation
     * @see .toScoreboardValueAnimation
     * @return The [SendableAnimation] instance associated with the given player.
     *
     * @since 2.0.0
     */
    fun toScoreboardTitleAnimation(animation: Animation, player: Player, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sets the text value for the player's scoreboard at the given index.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param animation The animation to be used.
     * @param player The player the [SendableAnimation] is associated with.
     * @param index The index the text value should be set to.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @return The [SendableAnimation] instance associated with the given player.
     *
     * @see .toScoreboardTitleAnimation
     * @see .toScoreboardTitleAnimation
     * @see .toScoreboardValueAnimation
     * @since 2.0.0
     */
    fun toScoreboardValueAnimation(animation: Animation, player: Player, index: Int, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sends Titles to the player.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param parts The parts of the animation the [SendableAnimation] will be made off of.
     * @param player The player the [SendableAnimation] is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @return The [SendableAnimation] instance associated with the animation.
     *
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @since 2.0.0
     */
    fun toTitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sends Subtitles to the player.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param parts The parts of the animation the [SendableAnimation] will be made off of.
     * @param player The player the [SendableAnimation] is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @return The [SendableAnimation] instance associated with the animation.
     *
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @see .toTitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @since 2.0.0
     */
    fun toSubtitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sends Actionbar messages to the player.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param parts The parts of the animation the [SendableAnimation] will be made off of.
     * @param player The player the [SendableAnimation] is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @return The [SendableAnimation] instance associated with the animation.
     *
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @since 2.0.0
     */
    fun toActionbarAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sets the Tab List header.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param parts The parts of the animation the [SendableAnimation] will be made off of.
     * @param player The player the [SendableAnimation] is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @return The [SendableAnimation] instance associated with the animation.
     *
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toFooterAnimation
     * @since 2.0.0
     */
    fun toHeaderAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sets the Tab List footer.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param parts The parts of the animation the [SendableAnimation] will be made off of.
     * @param player The player the [SendableAnimation] is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @return The [SendableAnimation] instance associated with the animation.
     *
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @see .toFooterAnimation
     * @see .toTitleAnimation
     * @see .toSubtitleAnimation
     * @see .toActionbarAnimation
     * @see .toHeaderAnimation
     * @since 2.0.0
     */
    fun toFooterAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sets the title for the player's scoreboard.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param parts The animation parts to be used.
     * @param player The player the [List] of [AnimationPart]s is associated with.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @see .toScoreboardTitleAnimation
     * @see .toScoreboardValueAnimation
     * @see .toScoreboardValueAnimation
     * @return The [List] of [AnimationPart]s associated with the given player.
     *
     * @since 2.0.0
     */
    fun toScoreboardTitleAnimation(parts: List<AnimationPart<*>>, player: Player, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates a [SendableAnimation] that sets the text value for the player's scoreboard at the given index.
     *
     *
     * If `withPlaceholders` is true, placeholders will be replaced.
     *
     * @param parts The animation to be used.
     * @param player The player the [SendableAnimation] is associated with.
     * @param index The index the text value should be set to.
     * @param withPlaceholders Whether or not placeholders should be replaced.
     *
     * @return The [SendableAnimation] instance associated with the given player.
     *
     * @see .toScoreboardTitleAnimation
     * @see .toScoreboardTitleAnimation
     * @see .toScoreboardValueAnimation
     * @since 2.0.0
     */
    fun toScoreboardValueAnimation(parts: List<AnimationPart<*>>, player: Player, index: Int, withPlaceholders: Boolean): SendableAnimation

    /**
     * Creates an [AnimationPart] from a [String]
     *
     * @param text The [String] that will be the value of the [AnimationPart].
     *
     * @return The [AnimationPart] instance associated with the input text.
     *
     * @see .toAnimationPart
     * @see .toAnimationParts
     * @since 2.0.0
     */
    fun toAnimationPart(text: String): AnimationPart<String>

    /**
     * Creates an [AnimationPart] from an [Animation]
     *
     * @param animation The [Animation] that will be the value of the [AnimationPart].
     *
     * @return The [AnimationPart] instance associated with the input text.
     *
     * @see .toAnimationPart
     * @see .toAnimationParts
     * @since 2.0.0
     */
    fun toAnimationPart(animation: Animation): AnimationPart<Animation>

    /**
     * Converts the text found to an immutable list of [AnimationPart]s with values of [String] or [Animation]
     *
     *
     * All plain text that follows the animation or script patterns will be converted to their respected Animations.
     *
     * @param text The text to be converted.
     *
     * @return The instance of the immutable list with all of the Animation parts, that were converted from the text.
     *
     * @see .toAnimationPart
     * @see .toAnimationPart
     * @since 2.0.0
     */
    fun toAnimationParts(text: String): List<AnimationPart<*>>

    /**
     * Creates an [Animation] from the input text and timings values.
     *
     *
     * All timings are measured in ticks.
     *
     * @param text The input text to be displayed.
     * @param fadeIn If the frame is being used in a title,
     * this is the time it takes for the title to fade onto the screen.
     * If the frame is not being used in a title, it will be added onto the stay time.
     * @param stay The time it takes for the frame to stay on the screen.
     * (The only exception is for Actionbar messages)
     * @param fadeOut If the frame is being used in a title,
     * this is the time it takes for the title to fade off of the screen.
     * If this frame is not being used in a title, it will be added onto the stay time.
     *
     * @return An instance of [AnimationFrame] that has the given values assigned.
     *
     * @since 2.0.0
     */
    fun createAnimationFrame(text: String, fadeIn: Int, stay: Int, fadeOut: Int): AnimationFrame

    /**
     * Creates an animation from an Array of [String].
     *
     *
     * Each line must follow the pattern `[fade-in;stay;fade-out]text`
     *
     *
     * fade-in = The time it takes for a title to fade onto the screen.
     * If the frame is not used in a title, it will be added onto the stay time instead.
     *
     *
     * stay = The time it takes for the frame to stay on the screen.
     *
     *
     * fade-out = The time it takes for a title to fade off of the screen.
     * If the frame is not used in a title, it will be added onto the stay time instead.
     *
     * @param frames The frames that will be converted to [String]
     *
     * @return The [Animation] generated from the frames.
     *
     * @see .fromTextFile
     * @see .fromJavaScript
     * @since 2.0.0
     */
    fun fromText(vararg frames: String): Animation

    /**
     * Creates an animation from a File. Each line of the file will be converted to an Array
     * and fed into the [fromText]
     *
     *
     * Each line must follow the pattern `[fade-in;stay;fade-out]text`
     *
     *
     * fade-in = The time it takes for a title to fade onto the screen.
     * If the frame is not used in a title, it will be added onto the stay time instead.
     *
     *
     * stay = The time it takes for the frame to stay on the screen.
     *
     *
     * fade-out = The time it takes for a title to fade off of the screen.
     * If the frame is not used in a title, it will be added onto the stay time instead.
     *
     * @param file The file that will be read and converted to an [Animation].
     *
     * @return The [Animation] generated from the frames.
     *
     * @see .fromText
     * @see .fromJavaScript
     * @since 2.0.0
     */
    fun fromTextFile(file: File): Animation

    /**
     * Creates an [Animation] from the name of a loaded JavaScript animation,
     * with the given input text.
     *
     * @param name The name of the JavaScript animation.
     * @param input The input text to be converted to an [Animation]
     *
     * @return The [Animation] generated from the JavaScript.
     *
     * @see .fromText
     * @see .fromTextFile
     * @since 2.0.0
     */
    fun fromJavaScript(name: String, input: String): Animation

    /**
     * Sends a title message to the given [Player] with the given title.
     * This will also use the last sent timings.
     *
     * @param player The player the title should be sent to.
     * @param title The title to be displayed.
     *
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @see .sendTitlesWithPlaceholders
     * @since 2.0.0
     */
    fun sendTitle(player: Player, title: String)

    /**
     * Sends a title message to the given [Player] with the given timings.
     *
     * @param player The player the title should be sent to.
     * @param title The title to be sent to the [Player].
     * @param fadeIn The time it takes for the title to fade onto the screen.
     * @param stay The time it takes for the title to stay on the screen.
     * @param fadeOut The time it takes for the title to fade off of the screen.
     *
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @see .sendTitlesWithPlaceholders
     * @since 2.0.0
     */
    fun sendTitle(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int)

    /**
     * Sends a title message to the given [Player] where all placeholders have been replaced.
     * This will also use the last sent timings.
     *
     * @param player The player the title should be sent to.
     * @param title The title to be sent to the [Player].
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @see .sendTitlesWithPlaceholders
     * @since 2.0.0
     */
    fun sendTitleWithPlaceholders(player: Player, title: String)

    /**
     * Sends a title message to the given [Player] with the given timings where all placeholders have been replaced
     * and with the given timings.
     *
     * @param player The player the title should be sent to.
     * @param title The title to be sent to the [Player].
     * @param fadeIn The time it takes for the title to fade onto the screen.
     * @param stay The time it takes for the title to stay on the screen.
     * @param fadeOut The time it takes for the title to fade off of the screen.
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @see .sendTitlesWithPlaceholders
     * @since 2.0.0
     */
    fun sendTitleWithPlaceholders(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int)

    /**
     * Sends a title message to the given [Player] with the given timings where all placeholders have been replaced
     * and with the given timings.
     * If animations or scripts are available, they will be shown.
     *
     * @param player The player the title should be sent to.
     * @param title The title to be sent to the [Player].
     * @param fadeIn The time it takes for the title to fade onto the screen.
     * @param stay The time it takes for the title to stay on the screen.
     * @param fadeOut The time it takes for the title to fade off of the screen.
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @see .sendTitlesWithPlaceholders
     * @since 2.2.0
     */
    fun sendProcessedTitle(player: Player, title: String, fadeIn: Int, stay: Int, fadeOut: Int)

    /**
     * Sends a subtitle message to the given [Player].
     * This will also use the last sent timings.
     *
     * @param player The player the subtitle should be sent to.
     * @param subtitle The subtitle to be displayed.
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @see .sendTitlesWithPlaceholders
     * @since 2.0.0
     */
    fun sendSubtitle(player: Player, subtitle: String)

    /**
     * Sends a subtitle message to the given [Player] with the given timings as well as the given subtitles.
     *
     * @param player The player the subtitle should be sent to.
     * @param subtitle The subtitle to be displayed.
     * @param fadeIn The time it takes for the subtitle to fade onto the screen.
     * @param stay The time it takes for the subtitle to stay on the screen.
     * @param fadeOut The time it takes for the subtitle to fade off of the screen.
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @see .sendTitlesWithPlaceholders
     * @since 2.0.0
     */
    fun sendSubtitle(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int)

    /**
     * Sends a subtitle message to the given [Player] where all placeholders have been replaced.
     * This will also use the last sent timings.
     *
     * @param player The player the subtitle should be sent to.
     * @param subtitle The subtitle to be displayed.
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @see .sendTitlesWithPlaceholders
     * @since 2.0.0
     */
    fun sendSubtitleWithPlaceholders(player: Player, subtitle: String)

    /**
     * Sends a subtitle message to the given [Player] where all placeholders have been replaced
     * and with the given timings.
     *
     * @param player The player the subtitle should be sent to.
     * @param subtitle The subtitle to be displayed.
     * @param fadeIn The time it takes for the subtitle to fade onto the screen.
     * @param stay The time it takes for the subtitle to stay on the screen.
     * @param fadeOut The time it takes for the subtitle to fade off of the screen.
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @see .sendTitlesWithPlaceholders
     * @since 2.0.0
     */
    fun sendSubtitleWithPlaceholders(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int)

    /**
     * Sends a subtitle message to the given [Player] where all placeholders have been replaced
     * and with the given timings.
     * If animations or scripts are available, they will be shown.
     *
     * @param player The player the subtitle should be sent to.
     * @param subtitle The subtitle to be displayed.
     * @param fadeIn The time it takes for the subtitle to fade onto the screen.
     * @param stay The time it takes for the subtitle to stay on the screen.
     * @param fadeOut The time it takes for the subtitle to fade off of the screen.
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @see .sendTitlesWithPlaceholders
     * @since 2.2.0
     */
    fun sendProcessedSubtitle(player: Player, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int)

    /**
     * Sends a title and a subtitle message to the given [Player].
     *
     * @param player The player the titles should be sent to.
     * @param title The title to be displayed.
     * @param subtitle The subtitle to be displayed.
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @see .sendTitlesWithPlaceholders
     * @since 2.0.0
     */
    fun sendTitles(player: Player, title: String, subtitle: String)

    /**
     * Sends a title and a subtitle message to the given [Player] with the given timings.
     *
     * @param player The player the titles should be sent to.
     * @param title The title to be displayed.
     * @param subtitle The subtitle to be displayed.
     * @param fadeIn The time it takes for the titles to fade onto the screen.
     * @param stay The time it takes for the titles to stay on the screen.
     * @param fadeOut The time it takes for the titles to fade off of the screen.
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @see .sendTitlesWithPlaceholders
     * @since 2.0.0
     */
    fun sendTitles(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int)

    /**
     * Sends a title and a subtitle message to the given [Player] where all placeholders are replaced.
     *
     * @param player The player the titles should be sent to.
     * @param title The title to be displayed.
     * @param subtitle The subtitle to be displayed.
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @since 2.0.0
     */
    fun sendTitlesWithPlaceholders(player: Player, title: String, subtitle: String)

    /**
     * Sends a title and a subtitle message to the given [Player] where all placeholders are replaced
     * and with the given timings.
     *
     * @param player The player the titles should be sent to.
     * @param title The title to be displayed.
     * @param subtitle The subtitle to be displayed.
     * @param fadeIn The time it takes for the titles to fade onto the screen.
     * @param stay The time it takes for the titles to stay on the screen.
     * @param fadeOut The time it takes for the titles to fade off of the screen.
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @since 2.0.0
     */
    fun sendTitlesWithPlaceholders(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int)

    /**
     * Sends a title and a subtitle message to the given [Player] where all placeholders are replaced
     * and with the given timings.
     * If animations or scripts are available, they will be shown.
     *
     * @param player The player the titles should be sent to.
     * @param title The title to be displayed.
     * @param subtitle The subtitle to be displayed.
     * @param fadeIn The time it takes for the titles to fade onto the screen.
     * @param stay The time it takes for the titles to stay on the screen.
     * @param fadeOut The time it takes for the titles to fade off of the screen.
     *
     * @see .sendTitle
     * @see .sendTitle
     * @see .sendTitleWithPlaceholders
     * @see .sendTitleWithPlaceholders
     * @see .sendSubtitle
     * @see .sendSubtitle
     * @see .sendSubtitleWithPlaceholders
     * @see .sendSubtitleWithPlaceholders
     * @see .sendTitles
     * @see .sendTitles
     * @see .sendTitlesWithPlaceholders
     * @since 2.2.0
     */
    fun sendProcessedTitles(player: Player, title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int)

    /**
     * Sets the timings for all of the next titles and subtitles sent to the [Player].
     *
     * @param player The player the timings should be set for.
     * @param fadeIn The time it takes for all titles sent afterwards to fade onto the screen.
     * @param stay The time it takes for all titles sent afterwards to stay on the screen.
     * @param fadeOut The time it takes for all titles sent afterwards to to fade off of the screen.
     *
     * @since 2.0.0
     */
    fun sendTimings(player: Player, fadeIn: Int, stay: Int, fadeOut: Int)

    /**
     * Removes a title that might be on the player's screen.
     *
     * @param player The player to clear.
     *
     * @see .clearSubtitle
     * @see .clearTitles
     * @since 2.0.0
     */
    fun clearTitle(player: Player)

    /**
     * Removes a subtitle that might be on the player's screen.
     *
     * @param player The player to clear.
     *
     * @see .clearTitle
     * @see .clearTitles
     * @since 2.0.0
     */
    fun clearSubtitle(player: Player)

    /**
     * Removes a title and a subtitle that might be on the player's screen.
     *
     * @param player The player to clear.
     *
     * @see .clearTitle
     * @see .clearSubtitle
     * @since 2.0.0
     */
    fun clearTitles(player: Player)

    /**
     * Sends an actionbar message to the [Player].
     *
     * @param player The player to send the actionbar message.
     * @param text The text to be sent.
     *
     * @see .sendActionbarWithPlaceholders
     * @since 2.0.0
     */
    fun sendActionbar(player: Player, text: String)

    /**
     * Sends an actionbar message to the [Player] where all placeholders are replaced.
     *
     * @param player The player to send the actionbar message.
     * @param text The text to be sent.
     *
     * @see .sendActionbar
     * @since 2.0.0
     */
    fun sendActionbarWithPlaceholders(player: Player, text: String)

    /**
     * Sends an actionbar message to the [Player] where all placeholders are replaced.
     * If animations or scripts are available, they will be shown.
     *
     * @param player The player to send the actionbar message.
     * @param text The text to be sent.
     *
     * @see .sendActionbar
     * @since 2.2.0
     */
    fun sendProcessedActionbar(player: Player, text: String)

    /**
     * Removes an actionbar message that might be on the player's screen.
     *
     * @param player The player to clear.
     *
     * @since 2.0.0
     */
    fun clearActionbar(player: Player)

    /**
     * Set the player list header for the given [Player].
     *
     * @param player The player to set the header for.
     * @param header The header to be displayed.
     *
     * @see .getHeader
     * @see .setHeaderWithPlaceholders
     * @see .getFooter
     * @see .setFooter
     * @see .setFooterWithPlaceholders
     * @see .setHeaderAndFooter
     * @see .setHeaderAndFooterWithPlaceholders
     * @since 2.0.0
     */
    fun setHeader(player: Player, header: String)

    /**
     * Sets the player list header for the given [Player] where all placeholders are replaced.
     *
     * @param player The player to set the header for.
     * @param header The header to be displayed.
     *
     * @see .getHeader
     * @see .setHeader
     * @see .getFooter
     * @see .setFooter
     * @see .setFooterWithPlaceholders
     * @see .setHeaderAndFooter
     * @see .setHeaderAndFooterWithPlaceholders
     * @since 2.0.0
     */
    fun setHeaderWithPlaceholders(player: Player, header: String)

    /**
     * Sets the player list header for the given [Player] where all placeholders are replaced.
     * If animations or scripts are available, they will be shown.
     *
     * @param player The player to set the header for.
     * @param header The header to be displayed.
     *
     * @see .getHeader
     * @see .setHeader
     * @see .getFooter
     * @see .setFooter
     * @see .setFooterWithPlaceholders
     * @see .setHeaderAndFooter
     * @see .setHeaderAndFooterWithPlaceholders
     * @since 2.2.0
     */
    fun setProcessedHeader(player: Player, header: String)

    /**
     * Gets the player list header that is currently being displayed for the given [Player].
     *
     * @param player The player to get the header from.
     *
     * @return The header that is being displayed.
     *
     * @see .setHeader
     * @see .setHeaderWithPlaceholders
     * @see .getFooter
     * @see .setFooter
     * @see .setFooterWithPlaceholders
     * @see .setHeaderAndFooter
     * @see .setHeaderAndFooterWithPlaceholders
     * @since 2.0.0
     */
    fun getHeader(player: Player): String

    /**
     * Sets the player list footer for the given [Player].
     *
     * @param player The player to set the footer for.
     * @param footer The footer to be displayed.
     *
     * @see .getHeader
     * @see .setHeader
     * @see .setHeaderWithPlaceholders
     * @see .getFooter
     * @see .setFooterWithPlaceholders
     * @see .setHeaderAndFooter
     * @see .setHeaderAndFooterWithPlaceholders
     * @since 2.0.0
     */
    fun setFooter(player: Player, footer: String)

    /**
     * Sets the player list footer for the given [Player] where all placeholders are replaced.
     *
     * @param player The player to set the footer for.
     * @param footer The footer to be displayed.
     *
     * @see .getHeader
     * @see .setHeader
     * @see .setHeaderWithPlaceholders
     * @see .getFooter
     * @see .setFooter
     * @see .setHeaderAndFooter
     * @see .setHeaderAndFooterWithPlaceholders
     * @since 2.0.0
     */
    fun setFooterWithPlaceholders(player: Player, footer: String)

    /**
     * Sets the player list footer for the given [Player] where all placeholders are replaced.
     * If animations or scripts are available, they will be shown.
     *
     * @param player The player to set the footer for.
     * @param footer The footer to be displayed.
     *
     * @see .getHeader
     * @see .setHeader
     * @see .setHeaderWithPlaceholders
     * @see .getFooter
     * @see .setFooter
     * @see .setHeaderAndFooter
     * @see .setHeaderAndFooterWithPlaceholders
     * @since 2.2.0
     */
    fun setProcessedFooter(player: Player, footer: String)

    /**
     * Gets the player list footer that is currently being displayed for the given [Player].
     *
     * @param player The player to get the header from.
     *
     * @return The footer that is being displayed.
     *
     * @see .getHeader
     * @see .setHeader
     * @see .setHeaderWithPlaceholders
     * @see .setFooter
     * @see .setFooterWithPlaceholders
     * @see .setHeaderAndFooter
     * @see .setHeaderAndFooterWithPlaceholders
     * @since 2.0.0
     */
    fun getFooter(player: Player): String

    /**
     * Sets the player list header and footer for the given [Player].
     *
     * @param player The player to get and set the footer for.
     * @param header The header to be displayed.
     * @param footer The footer to be displayed.
     *
     * @see .getHeader
     * @see .setHeader
     * @see .setHeaderWithPlaceholders
     * @see .getFooter
     * @see .setFooter
     * @see .setFooterWithPlaceholders
     * @see .setHeaderAndFooterWithPlaceholders
     * @since 2.0.0
     */
    fun setHeaderAndFooter(player: Player, header: String, footer: String)

    /**
     * Sets the player list header and footer for the given [Player] where all placeholders are replaced.
     *
     * @param player The player to get and set the footer for.
     * @param header The header to be displayed.
     * @param footer The footer to be displayed.
     *
     * @see .getHeader
     * @see .setHeader
     * @see .setHeaderWithPlaceholders
     * @see .getFooter
     * @see .setFooter
     * @see .setFooterWithPlaceholders
     * @see .setHeaderAndFooter
     * @since 2.0.0
     */
    fun setHeaderAndFooterWithPlaceholders(player: Player, header: String, footer: String)

    /**
     * Sets the player list header and footer for the given [Player] where all placeholders are replaced.
     * If animations or scripts are available, they will be shown.
     *
     * @param player The player to get and set the footer for.
     * @param header The header to be displayed.
     * @param footer The footer to be displayed.
     *
     * @see .getHeader
     * @see .setHeader
     * @see .setHeaderWithPlaceholders
     * @see .getFooter
     * @see .setFooter
     * @see .setFooterWithPlaceholders
     * @see .setHeaderAndFooter
     * @since 2.2.0
     */
    fun setProcessedHeaderAndFooter(player: Player, header: String, footer: String)

    /**
     * Gives the [Player] a scoreboard as a sidebar.
     *
     *
     * The scoreboard won't be displayed until at least one value has been added.
     *
     * @param player The player the scoreboard should be given to.
     *
     * @see .removeScoreboard
     * @see .hasScoreboard
     * @see .setScoreboardValue
     * @see .setScoreboardValueWithPlaceholders
     * @since 2.0.0
     */
    fun giveScoreboard(player: Player)

    /**
     * Gives the [Player] a scoreboard as a sidebar.
     *
     *
     * Gives you the TitleManager's own Scoreboard found in the config file.
     *
     * @param player The player the scoreboard should be given to.
     *
     * @see .removeScoreboard
     * @see .hasScoreboard
     * @see .setScoreboardValue
     * @see .setScoreboardValueWithPlaceholders
     * @see .giveScoreboard
     * @since 2.2.0
     */
    fun giveDefaultScoreboard(player: Player)

    /**
     * Removes the scoreboard sidebar from the given player.
     *
     * @param player The player the scoreboard should be removed from.
     *
     * @see .giveScoreboard
     * @see .hasScoreboard
     * @since 2.0.0
     */
    fun removeScoreboard(player: Player)

    /**
     * Checks whether or not the given [Player] has a scoreboard assigned.
     *
     * @param player The player to check for.
     *
     * @return Whether or not the player has a scoreboard assigned.
     *
     * @see .giveScoreboard
     * @see .removeScoreboard
     * @since 2.0.0.
     */
    fun hasScoreboard(player: Player): Boolean

    /**
     * Sets the title of the given [Player]'s scoreboard, if they have one assigned.
     *
     * @param player The player to set the title for.
     * @param title The title to be set. (Will be trimmed to a maximum of 32 characters)
     *
     * @see .giveScoreboard
     * @see .hasScoreboard
     * @see .setScoreboardTitleWithPlaceholders
     * @see .getScoreboardTitle
     * @since 2.0.0
     */
    fun setScoreboardTitle(player: Player, title: String)

    /**
     * Sets the title of the given [Player]'s scoreboard, if they have one assigned.
     *
     *
     * If placeholders are available, the title will try to replace them.
     *
     * @param player The player to set the title for.
     * @param title The title to be set. (Will be trimmed to a maximum of 32 characters)
     *
     * @see .giveScoreboard
     * @see .hasScoreboard
     * @see .setScoreboardTitle
     * @see .getScoreboardTitle
     * @since 2.0.0
     */
    fun setScoreboardTitleWithPlaceholders(player: Player, title: String)

    /**
     * Sets the title of the given [Player]'s scoreboard, if they have one assigned.
     *
     *
     * If placeholders are available, the title will try to replace them.
     * If animations or scripts are available, they will be shown.
     *
     * @param player The player to set the title for.
     * @param title The title to be set. (Will be trimmed to a maximum of 32 characters)
     *
     * @see .giveScoreboard
     * @see .hasScoreboard
     * @see .setScoreboardTitle
     * @see .setScoreboardTitleWithPlaceholders
     * @see .getScoreboardTitle
     * @since 2.2.0
     */
    fun setProcessedScoreboardTitle(player: Player, title: String)

    /**
     * Gets the title of the given [Player]'s scoreboard.
     *
     * @param player The player to get the title from.
     *
     * @return The title of the player's scoreboard, if no scoreboard is assigned this will return null.
     *
     * @see .giveScoreboard
     * @see .hasScoreboard
     * @see .setScoreboardTitle
     * @see .setScoreboardTitleWithPlaceholders
     * @since 2.0.0
     */
    fun getScoreboardTitle(player: Player): String?

    /**
     * Sets the text value of the given [Player]'s scoreboard at the given index.
     *
     * @param player The player to set the text value for.
     * @param index The index at which the text should be set. 1-15 (1 and 15 inclusive)
     * @param value The text value that should be set. (will be trimmed to a maximum of 40 characters)
     *
     * @see .giveScoreboard
     * @see .hasScoreboard
     * @see .setScoreboardValueWithPlaceholders
     * @see .getScoreboardValue
     * @see .removeScoreboard
     * @since 2.0.0
     */
    fun setScoreboardValue(player: Player, index: Int, value: String)

    /**
     * Sets the text value of the given [Player]'s scoreboard at the given index.
     *
     *
     * If placeholders are available, the value will try to replace them.
     *
     * @param player The player to set the text value for.
     * @param index The index at which the text should be set. 1-15 (1 and 15 inclusive)
     * @param value The text value that should be set.
     *
     * @see .giveScoreboard
     * @see .hasScoreboard
     * @see .setScoreboardValue
     * @see .getScoreboardValue
     * @see .removeScoreboardValue
     * @since 2.0.0
     */
    fun setScoreboardValueWithPlaceholders(player: Player, index: Int, value: String)

    /**
     * Sets the text value of the given [Player]'s scoreboard at the given index.
     *
     *
     * If placeholders are available, the value will try to replace them.
     * If animations or scripts are available, they will be shown.
     *
     * @param player The player to set the text value for.
     * @param index The index at which the text should be set. 1-15 (1 and 15 inclusive)
     * @param value The text value that should be set.
     *
     * @see .giveScoreboard
     * @see .hasScoreboard
     * @see .setScoreboardValue
     * @see .setScoreboardValueWithPlaceholders
     * @see .getScoreboardValue
     * @see .removeScoreboardValue
     * @since 2.2.0
     */
    fun setProcessedScoreboardValue(player: Player, index: Int, value: String)

    /**
     * Gets the text value of the given [Player]'s scoreboard at the given index.
     *
     * @param player The player to get the text value from.
     * @param index The index at which the text value is present.
     *
     * @return The text value of the given index. If no text value is present, this will return null.
     *
     * @see .giveScoreboard
     * @see .hasScoreboard
     * @see .setScoreboardValue
     * @see .setScoreboardValueWithPlaceholders
     * @see .removeScoreboardValue
     * @since 2.0.0
     */
    fun getScoreboardValue(player: Player, index: Int): String?

    /**
     * Removes the text value of the given [Player]'s scoreboardat the given index.
     *
     * @param player The player to remove the text value from.
     * @param index The index at which the text value should be removed.
     *
     * @see .giveScoreboard
     * @see .hasScoreboard
     * @see .setScoreboardValue
     * @see .setScoreboardValueWithPlaceholders
     * @see .getScoreboardValue
     * @since 2.0.0
     */
    fun removeScoreboardValue(player: Player, index: Int)
}
