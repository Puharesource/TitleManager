package io.puharesource.mc.sponge.titlemanager.api;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.animations.*;
import io.puharesource.mc.sponge.titlemanager.api.iface.AnimationIterable;
import org.spongepowered.api.text.Text;

public final class Sendables {
    @Inject private static TitleManager plugin;

    Sendables() {}

    private static void inject(final Object object) {
        plugin.getInjector().injectMembers(object);
    }

    // Titles

    public static TitleObject title(final Text title, final Text subtitle) {
        final TitleObject object = new TitleObject(title, subtitle);
        inject(object);

        return object;
    }

    public static TitleObject title(final Text title, final TitlePosition position) {
        final TitleObject object = new TitleObject(title, position);
        inject(object);

        return object;
    }

    public static TitleAnimation title(final AnimationToken title, final AnimationToken subtitle) {
        final TitleAnimation object = new TitleAnimation(title, subtitle);
        inject(object);

        return object;
    }

    // Actionbar titles

    public static ActionbarTitleObject actionbar(final Text title) {
        final ActionbarTitleObject object = new ActionbarTitleObject(title);
        inject(object);

        return object;
    }

    public static ActionbarAnimationSendable actionbar(final AnimationIterable title) {
        final ActionbarAnimationSendable object = new ActionbarAnimationSendable(title);
        inject(object);

        return object;
    }

    // Tab list titles

    public static TabTitleObject tabList(final Text header, final Text footer) {
        final TabTitleObject object = new TabTitleObject(header, footer);
        inject(object);

        return object;
    }

    public static TabTitleObject tabList(final Text title, final TabListPosition position) {
        final TabTitleObject object = new TabTitleObject(title, position);
        inject(object);

        return object;
    }

    public static TabTitleAnimation tabList(final AnimationToken header, final AnimationToken footer) {
        final TabTitleAnimation object = new TabTitleAnimation(header, footer);
        inject(object);

        return object;
    }
}
