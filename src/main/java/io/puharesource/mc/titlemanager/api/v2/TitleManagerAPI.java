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
    String replaceText(Player player, String text);
    boolean containsPlaceholders(String text);
    boolean containsPlaceholder(String text, String placeholder);

    boolean containsAnimations(String text);
    boolean containsAnimation(String text, String animation);

    Map<String, Animation> getRegisteredAnimations();
    Set<String> getRegisteredScripts();
    void addAnimation(String id, Animation animation);
    void removeAnimation(String id);

    SendableAnimation toTitleAnimation(Animation animation, Player player, boolean withPlaceholders);
    SendableAnimation toSubtitleAnimation(Animation animation, Player player, boolean withPlaceholders);
    SendableAnimation toActionbarAnimation(Animation animation, Player player, boolean withPlaceholders);
    SendableAnimation toHeaderAnimation(Animation animation, Player player, boolean withPlaceholders);
    SendableAnimation toFooterAnimation(Animation animation, Player player, boolean withPlaceholders);

    SendableAnimation toTitleAnimation(List<AnimationPart> parts, Player player, boolean withPlaceholders);
    SendableAnimation toSubtitleAnimation(List<AnimationPart> parts, Player player, boolean withPlaceholders);
    SendableAnimation toActionbarAnimation(List<AnimationPart> parts, Player player, boolean withPlaceholders);
    SendableAnimation toHeaderAnimation(List<AnimationPart> parts, Player player, boolean withPlaceholders);
    SendableAnimation toFooterAnimation(List<AnimationPart> parts, Player player, boolean withPlaceholders);

    AnimationPart<String> toAnimationPart(String text);
    AnimationPart<Animation> toAnimationPart(Animation animation);

    List<AnimationPart> toAnimationParts(String text);

    AnimationFrame createAnimationFrame(String text, int fadeIn, int stay, int fadeOut);

    Animation fromText(String... frames);
    Animation fromTextFile(File file);
    Animation fromJavaScript(String name, String input);

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

    void clearTitle(Player player, int fadeIn, int stay, int fadeOut);
    void clearSubtitle(Player player, int fadeIn, int stay, int fadeOut);
    void clearTitles(Player player, int fadeIn, int stay, int fadeOut);

    void sendActionbar(Player player, String text);
    void sendActionbarWithPlaceholders(Player player, String text);

    void clearActionbar(Player player);

    void setHeader(Player player, String header);
    void setHeaderWithPlaceholders(Player player, String header);
    String getHeader(Player player);

    void setFooter(Player player, String footer);
    void setFooterWithPlaceholders(Player player, String footer);
    String getFooter(Player player);

    void setHeaderAndFooter(Player player, String header, String footer);
    void setHeaderAndFooterWithPlaceholders(Player player, String header, String footer);
}
