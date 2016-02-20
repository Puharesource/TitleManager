package io.puharesource.mc.sponge.titlemanager.guicemodules;

import com.google.inject.AbstractModule;
import io.puharesource.mc.sponge.titlemanager.api.Sendables;
import io.puharesource.mc.sponge.titlemanager.utils.MiscellaneousUtils;

public final class StaticModule extends AbstractModule {
    @Override
    protected void configure() {
        requestStaticInjection(MiscellaneousUtils.class);
        requestStaticInjection(Sendables.class);
    }
}