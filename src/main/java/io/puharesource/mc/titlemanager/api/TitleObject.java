package io.puharesource.mc.titlemanager.api;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.api.events.TitleEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TitleObject {

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

    public void send(Player player) {
        final TitleEvent event = new TitleEvent(player, this);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;
        /*boolean vars = false;
        for(TitleVariable tv : TitleVariable.values())
        {
            if(rawTitle.toLowerCase().contains(tv.getTextRaw().toLowerCase()))
            {
                setTitle(TextConverter.setVariables(player, rawTitle));
                vars = true;
            }
            if(rawSubtitle.toLowerCase().contains(tv.getTextRaw().toLowerCase()))
            {
                setSubtitle(TextConverter.setVariables(player, rawSubtitle));
                vars = true;
            }
            if(vars) break;
        }*/

        TitleManager.getReflectionManager().sendPacket(TitleManager.getReflectionManager().constructTitleTimingsPacket(fadeIn, stay, fadeOut), player);
        if (title != null && rawTitle != null)
            TitleManager.getReflectionManager().sendPacket(TitleManager.getReflectionManager().constructTitlePacket(false, title), player);
        if (subtitle != null && rawSubtitle != null)
            TitleManager.getReflectionManager().sendPacket(TitleManager.getReflectionManager().constructTitlePacket(true, subtitle), player);
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
