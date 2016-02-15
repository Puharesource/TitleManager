package io.puharesource.mc.sponge.titlemanager.api;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.iface.TitleSendable;
import io.puharesource.mc.sponge.titlemanager.config.configs.ConfigMain;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * The title object is an object used whenever a hovering message is sent to the player, whether it's an animation or not.
 */
public class TitleObject implements TitleSendable {
    @Inject private TitleManager plugin;

    private Optional<Text> title;
    private Optional<Text> subtitle;

    private int fadeIn = -1;
    private int stay = -1;
    private int fadeOut = -1;

    /**
     * Creates a title object with only one type of title title/subtitle
     * @param title The text of the title/subtitle.
     * @param type The type of the title/subtitle.
     */
    public TitleObject(final Text title, final TitlePosition type) {
        if (type == TitlePosition.TITLE)
            setTitle(title);
        else if (type == TitlePosition.SUBTITLE)
            setSubtitle(title);

        updateTimes();
    }

    /**
     * Creates a title object with both the title/subtitle present.
     * @param title The text of the title.
     * @param subtitle The text of the subtitle.
     */
    public TitleObject(final Text title, final Text subtitle) {
        setTitle(title);
        setSubtitle(subtitle);
        updateTimes();
    }
    
    private void updateTimes() {
        final ConfigMain config = plugin.getConfigHandler().getMainConfig().getConfig();
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

        title.ifPresent(text -> builder.title(plugin.replacePlaceholders(player, text)));
        subtitle.ifPresent(text -> builder.title(plugin.replacePlaceholders(player, text)));

        player.sendTitle(builder.build());
    }

    /**
     * Gets the raw text of the title.
     * @return The title text.
     */
    public Optional<Text> getTitle() {
        return title;
    }

    /**
     * Sets the text of the title.
     * @param title The text shown as the title.
     * @return The root object.
     */
    public TitleObject setTitle(final Text title) {
        this.title = Optional.ofNullable(title);
        return this;
    }

    /**
     * Gets the raw text of the subtitle.
     * @return The subtitle text.
     */
    public Optional<Text> getSubtitle() {
        return title;
    }

    /**
     * Sets the text of the subtitle.
     * @param subtitle The text shown as the subtitle.
     * @return The root object.
     */
    public TitleObject setSubtitle(final Text subtitle) {
        this.subtitle = Optional.of(subtitle);
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
    public TitleObject setFadeIn(final int ticks) {
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
    public TitleObject setStay(final int ticks) {
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
    public TitleObject setFadeOut(final int ticks) {
        fadeOut = ticks;
        return this;
    }
}
