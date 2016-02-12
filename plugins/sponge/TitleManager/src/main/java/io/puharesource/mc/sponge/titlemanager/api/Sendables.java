package io.puharesource.mc.sponge.titlemanager.api;

import io.puharesource.mc.sponge.titlemanager.api.animations.*;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationIterable;
import org.spongepowered.api.text.Text;

public final class Sendables {
    private Sendables() {}

    // Titles

    public static TitleObject title(final Text title, final Text subtitle) {
        return new TitleObject(title, subtitle);
    }

    public static TitleObject title(final Text title, final TitlePosition position) {
        return new TitleObject(title, position);
    }

    public static TitleAnimation title(final AnimationToken title, final AnimationToken subtitle) {
        return new TitleAnimation(title, subtitle);
    }

    // Actionbar titles

    public static ActionbarTitleObject actionbar(final Text title) {
        return new ActionbarTitleObject(title);
    }

    public static ActionbarAnimationSendable actionbar(final AnimationIterable title) {
        return new ActionbarAnimationSendable(title);
    }

    // Tab list titles

    public static TabTitleObject tabList(final Text header, final Text footer) {
        return new TabTitleObject(header, footer);
    }

    public static TabTitleObject tabList(final Text title, final TabListPosition position) {
        return new TabTitleObject(title, position);
    }

    public static TabTitleAnimation tabList(final AnimationToken header, final AnimationToken footer) {
        return new TabTitleAnimation(header, footer);
    }
}
