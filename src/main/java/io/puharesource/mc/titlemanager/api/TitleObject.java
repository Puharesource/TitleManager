package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.events.TitleEvent;
import io.puharesource.mc.titlemanager.api.iface.ITitleObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TitleObject implements ITitleObject {

    private String rawTitle;
    private String rawSubtitle;

    private Object title;
    private Object subtitle;

    private int fadeIn = -1;
    private int stay = -1;
    private int fadeOut = -1;

    public TitleObject(String title, TitleType type) {
        if (type == TitleType.TITLE)
            setTitle(title);
        else if (type == TitleType.SUBTITLE)
            setSubtitle(title);
    }

    public TitleObject(String title, String subtitle) {
        rawTitle = title;
        rawSubtitle = subtitle;
        this.title = TitleManager.getReflectionManager().getIChatBaseComponent(title);
        this.subtitle = TitleManager.getReflectionManager().getIChatBaseComponent(subtitle);
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

        TitleManager.getReflectionManager().sendPacket(TitleManager.getReflectionManager().constructTitleTimingsPacket(fadeIn, stay, fadeOut), player);
        if (rawTitle != null && title != null)
            TitleManager.getReflectionManager().sendPacket(TitleManager.getReflectionManager().constructTitlePacket(false, (rawTitle.contains("{") && rawTitle.contains("}")) ? TitleManager.getReflectionManager().getIChatBaseComponent(TextConverter.setVariables(player, rawTitle)) : title), player);
        if (rawSubtitle != null && title != null)
            TitleManager.getReflectionManager().sendPacket(TitleManager.getReflectionManager().constructTitlePacket(true, (rawSubtitle.contains("{") && rawSubtitle.contains("}")) ? TitleManager.getReflectionManager().getIChatBaseComponent(TextConverter.setVariables(player, rawSubtitle)) : subtitle), player);
    }

    public String getTitle() {
        return rawTitle;
    }

    public TitleObject setTitle(String title) {
        rawTitle = title;
        this.title = TitleManager.getReflectionManager().getIChatBaseComponent(title);
        return this;
    }

    public String getSubtitle() {
        return rawSubtitle;
    }

    public TitleObject setSubtitle(String subtitle) {
        rawSubtitle = subtitle;
        this.subtitle = TitleManager.getReflectionManager().getIChatBaseComponent(subtitle);
        return this;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public TitleObject setFadeIn(int i) {
        fadeIn = i;
        return this;
    }

    public int getStay() {
        return stay;
    }

    public TitleObject setStay(int i) {
        stay = i;
        return this;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    public TitleObject setFadeOut(int i) {
        fadeOut = i;
        return this;
    }

    public enum TitleType {TITLE, SUBTITLE}
}
