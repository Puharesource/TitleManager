package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.events.TitleEvent;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import io.puharesource.mc.titlemanager.backend.config.ConfigMain;
import io.puharesource.mc.titlemanager.backend.packet.TitlePacket;
import io.puharesource.mc.titlemanager.backend.player.TMPlayer;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import io.puharesource.mc.titlemanager.backend.reflections.managers.ReflectionManagerProtocolHack1718;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * The title object is an object used whenever a hovering message is sent to the player, whether it's an animation or not.
 */
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
    public TitleObject(String title, String subtitle) {
        setTitle(title);
        setSubtitle(subtitle);
        updateTimes();
    }
    
    private void updateTimes() {
        ConfigMain config = TitleManager.getInstance().getConfigManager().getConfig();
        if (config.usingConfig) return;

        fadeIn = config.welcomeMessageFadeIn;
        stay = config.welcomeMessageStay;
        fadeOut = config.welcomeMessageFadeOut;
    }
    
    @Override
    public void broadcast() {
        for (Player player : Bukkit.getOnlinePlayers())
            send(player);
    }

    @Override
    public void send(Player player) {
        final TitleEvent event = new TitleEvent(player, this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        TMPlayer tmPlayer = new TMPlayer(player);

        tmPlayer.sendPacket(new TitlePacket(fadeIn, stay, fadeOut));
        if (title != null)
            tmPlayer.sendPacket(new TitlePacket(TitleType.TITLE, TextConverter.setVariables(player, title)));
        if (subtitle != null)
            tmPlayer.sendPacket(new TitlePacket(TitleType.SUBTITLE, TextConverter.setVariables(player, subtitle)));
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

    public enum TitleType {
        TITLE(0),
        SUBTITLE(1),
        TIMES(2),
        CLEAR(3),
        RESET(4);

        private final int i;

        TitleType(final int i) {
            this.i = i;
        }

        public Object getHandle() {
            ReflectionManager manager = TitleManager.getInstance().getReflectionManager();
            return manager instanceof ReflectionManagerProtocolHack1718 ?
                    manager.getClasses().get("Action").getHandle().getEnumConstants()[i] :
                    manager.getClasses().get("EnumTitleAction").getHandle().getEnumConstants()[i];
        }
    }
}
