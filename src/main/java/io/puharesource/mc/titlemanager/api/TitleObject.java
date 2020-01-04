package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.internal.InternalsKt;
import io.puharesource.mc.titlemanager.TitleManagerPlugin;
import io.puharesource.mc.titlemanager.api.events.TitleEvent;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.internal.reflections.TitleTypeMapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * The title object is an object used whenever a hovering message is sent to the player, whether it's an animation or not.
 *
 * @deprecated In favor of the methods seen under the "see also" section.
 *
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#sendTitle(Player, String)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#sendSubtitle(Player, String)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#sendTitles(Player, String, String)
 *
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#sendTitle(Player, String, int, int, int)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#sendSubtitle(Player, String, int, int, int)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#sendTitles(Player, String, String, int, int, int)
 *
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#sendTitleWithPlaceholders(Player, String)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#sendSubtitleWithPlaceholders(Player, String)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#sendTitlesWithPlaceholders(Player, String, String)
 *
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#sendTitleWithPlaceholders(Player, String, int, int, int)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#sendSubtitleWithPlaceholders(Player, String, int, int, int)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#sendTitlesWithPlaceholders(Player, String, String, int, int, int)
 *
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#clearTitle(Player)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#clearSubtitle(Player)
 * @see io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI#clearTitles(Player)
 *
 * @since 1.0.1
 */
@Deprecated
public class TitleObject implements ITitleObject {

    private String title;
    private String subtitle;

    private int fadeIn = -1;
    private int stay = -1;
    private int fadeOut = -1;

    /**
     * Creates a title object with only one type of title title/subtitle
     * @param title The text of the title/subtitle.
     * @param type The type of the title/subtitle.
     */
    @Deprecated
    public TitleObject(String title, TitleType type) {
        if (type == TitleType.TITLE)
            setTitle(title);
        else if (type == TitleType.SUBTITLE)
            setSubtitle(title);
        updateTimes();
    }

    /**
     * Creates a title object with both the title/subtitle present.
     * @param title The text of the title.
     * @param subtitle The text of the subtitle.
     */
    @Deprecated
    public TitleObject(String title, String subtitle) {
        setTitle(title);
        setSubtitle(subtitle);
        updateTimes();
    }

    @Deprecated
    private void updateTimes() {}

    @Override
    @Deprecated
    public void broadcast() {
        Bukkit.getOnlinePlayers().forEach(this::send);
    }

    @Override
    @Deprecated
    public void send(Player player) {
        final TitleEvent event = new TitleEvent(player, this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        final TitleManagerPlugin plugin = InternalsKt.getPluginInstance();

        if (title != null) {
            plugin.sendTitleWithPlaceholders(player, title, fadeIn, stay, fadeOut);
        }

        if (subtitle != null) {
            plugin.sendSubtitleWithPlaceholders(player, subtitle, fadeIn, stay, fadeOut);
        }
    }

    /**
     * Gets the raw text of the title.
     * @return The title text.
     */
    @Deprecated
    public String getTitle() {
        return title;
    }

    /**
     * Sets the text of the title.
     * @param title The text shown as the title.
     * @return The root object.
     */
    @Deprecated
    public TitleObject setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Gets the raw text of the subtitle.
     * @return The subtitle text.
     */
    @Deprecated
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * Sets the text of the subtitle.
     * @param subtitle The text shown as the subtitle.
     * @return The root object.
     */
    @Deprecated
    public TitleObject setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    /**
     * Gets the amount of ticks it takes for the object to fade into view.
     * @return The amount of ticks.
     */
    @Deprecated
    public int getFadeIn() {
        return fadeIn;
    }

    /**
     * Sets the amount of ticks it takes for the object to fade into view.
     * @param ticks The amount of ticks.
     * @return The root object.
     */
    @Deprecated
    public TitleObject setFadeIn(int ticks) {
        fadeIn = ticks;
        return this;
    }

    /**
     * Gets the amount of ticks it takes for the object to stay on screen without fading in or out of view.
     * @return The amount of ticks.
     */
    @Deprecated
    public int getStay() {
        return stay;
    }

    /**
     * Sets the amount of ticks it takes for the object to stay on screen without fading in or out of view.
     * @param ticks The amount of ticks.
     * @return The root object.
     */
    @Deprecated
    public TitleObject setStay(int ticks) {
        stay = ticks;
        return this;
    }

    /**
     * Gets the amount of ticks it takes for the object to fade out of view.
     * @return The amount of ticks.
     */
    @Deprecated
    public int getFadeOut() {
        return fadeOut;
    }

    /**
     * Sets the amount of ticks it takes for the object to fade out of view.
     * @param ticks The amount of ticks.
     * @return The root object.
     */
    @Deprecated
    public TitleObject setFadeOut(int ticks) {
        fadeOut = ticks;
        return this;
    }

    @Deprecated
    public enum TitleType {
        TITLE,
        SUBTITLE,
        ACTIONBAR,
        TIMES,
        CLEAR,
        RESET;

        @Deprecated
        public Object getHandle() {
            return TitleTypeMapper.valueOf(this.name()).getHandle();
        }
    }
}
