package io.puharesource.mc.sponge.titlemanager.api;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.TitlePosition;
import io.puharesource.mc.sponge.titlemanager.api.iface.ITitleObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.world.World;

/**
 * The title object is an object used whenever a hovering message is sent to the player, whether it's an animation or not.
 */
public class TitleObject implements ITitleObject {
    @Inject private TitleManager plugin;

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
    public TitleObject(final String title, final TitlePosition type) {
        if (type == TitlePosition.TITLE)
            setTitle(title);
        else if (type == TitlePosition.SUBTITLE)
            setSubtitle(title);
        else
            setTitle(title);
        updateTimes();
    }

    /**
     * Creates a title object with both the title/subtitle present.
     * @param title The text of the title.
     * @param subtitle The text of the subtitle.
     */
    public TitleObject(String title, String subtitle) {
        setTitle(title);
        setSubtitle(subtitle);
        updateTimes();
    }
    
    private void updateTimes() {
        ConfigMain config = plugin.getConfigHandler().getConfig();
        if (config.usingConfig) return;

        fadeIn = config.welcomeMessageFadeIn;
        stay = config.welcomeMessageStay;
        fadeOut = config.welcomeMessageFadeOut;
    }

    @Override
    public void broadcast() {
        Sponge.getServer().getOnlinePlayers().forEach(this::send);
    }

    @Override
    public void broadcast(final World world) {
        Sponge.getServer().getOnlinePlayers()
                .stream()
                .filter(p -> p.getWorld().equals(world))
                .forEach(this::send);
    }

    @Override
    public void send(final Player player) {
        final Title.Builder builder = Title.builder()
                .fadeIn(fadeIn)
                .stay(stay)
                .fadeOut(fadeOut);

        if (title != null) builder.title(Text.of(plugin.replacePlaceholders(player, title)));
        if (subtitle != null) builder.subtitle(Text.of(plugin.replacePlaceholders(player, subtitle)));

        player.sendTitle(builder.build());
    }

    /**
     * Gets the raw text of the title.
     * @return The title text.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the text of the title.
     * @param title The text shown as the title.
     * @return The root object.
     */
    public TitleObject setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Gets the raw text of the subtitle.
     * @return The subtitle text.
     */
    public String getSubtitle() {
        return subtitle;
    }

    /**
     * Sets the text of the subtitle.
     * @param subtitle The text shown as the subtitle.
     * @return The root object.
     */
    public TitleObject setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    /**
     * Gets the amount of ticks it takes for the object to fade into view.
     * @return The amount of ticks.
     */
    public int getFadeIn() {
        return fadeIn;
    }

    /**
     * Sets the amount of ticks it takes for the object to fade into view.
     * @param ticks The amount of ticks.
     * @return The root object.
     */
    public TitleObject setFadeIn(int ticks) {
        fadeIn = ticks;
        return this;
    }

    /**
     * Gets the amount of ticks it takes for the object to stay on screen without fading in or out of view.
     * @return The amount of ticks.
     */
    public int getStay() {
        return stay;
    }

    /**
     * Sets the amount of ticks it takes for the object to stay on screen without fading in or out of view.
     * @param ticks The amount of ticks.
     * @return The root object.
     */
    public TitleObject setStay(int ticks) {
        stay = ticks;
        return this;
    }

    /**
     * Gets the amount of ticks it takes for the object to fade out of view.
     * @return The amount of ticks.
     */
    public int getFadeOut() {
        return fadeOut;
    }

    /**
     * Sets the amount of ticks it takes for the object to fade out of view.
     * @param ticks The amount of ticks.
     * @return The root object.
     */
    public TitleObject setFadeOut(int ticks) {
        fadeOut = ticks;
        return this;
    }
}
