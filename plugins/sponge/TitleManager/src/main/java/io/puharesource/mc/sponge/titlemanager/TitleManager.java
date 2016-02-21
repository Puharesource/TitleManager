package io.puharesource.mc.sponge.titlemanager;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.puharesource.mc.sponge.titlemanager.api.Sendables;
import io.puharesource.mc.sponge.titlemanager.api.animations.AnimationFrame;
import io.puharesource.mc.sponge.titlemanager.api.animations.FrameSequence;
import io.puharesource.mc.sponge.titlemanager.api.placeholder.PlaceholderManager;
import io.puharesource.mc.sponge.titlemanager.commands.TMCommand;
import io.puharesource.mc.sponge.titlemanager.guicemodules.StaticModule;
import io.puharesource.mc.sponge.titlemanager.listeners.ListenerConnection;
import io.puharesource.mc.sponge.titlemanager.listeners.ListenerWorldChange;
import io.puharesource.mc.sponge.titlemanager.placeholders.StandardPlaceholders;
import io.puharesource.mc.sponge.titlemanager.utils.MiscellaneousUtils;
import lombok.Getter;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.InputStream;
import java.net.URL;
import java.util.Set;

@Plugin(id = "TitleManager", name = "TitleManager", version = "1.0.0-Sponge")
public final class TitleManager {
    @Getter @Inject private Logger logger;
    @Getter @Inject private Injector injector;

    @Getter private Engine engine;
    @Getter private ConfigHandler configHandler;
    @Getter private PlaceholderManager placeholderManager;

    private TMCommand mainCommand;

    private final Set<Integer> runningAnimations = Sets.newSetFromMap(new MapMaker().concurrencyLevel(4).makeMap());

    @Listener
    public void onStart(final GameStartedServerEvent event) {
        logger.info("Starting TitleManager!");

        injector.getInstance(Sendables.class);
        final Injector staticInjector = injector.createChildInjector(new StaticModule());
        staticInjector.getInstance(MiscellaneousUtils.class);


        logger.info("Starting engine.");
        engine = new Engine();

        logger.info("Adding config serializers");
        addSerializer(AnimationFrame.class, new AnimationFrame.Serializer());
        addSerializer(FrameSequence.class, new FrameSequence.Serializer());

        logger.info("Starting config handler.");
        configHandler = new ConfigHandler();
        injector.injectMembers(configHandler);
        configHandler.load();

        logger.info("Starting placeholder manager.");
        placeholderManager = new PlaceholderManager();
        injector.injectMembers(placeholderManager);

        placeholderManager.registerVariableReplacer(new StandardPlaceholders());

        logger.info("Registering commands.");
        mainCommand = new TMCommand();
        injector.injectMembers(mainCommand);
        mainCommand.load();

        logger.info("Registering listeners.");
        final ListenerConnection listenerConnection = new ListenerConnection();
        final ListenerWorldChange listenerWorldChange = new ListenerWorldChange();

        injector.injectMembers(listenerConnection);
        injector.injectMembers(listenerWorldChange);

        Sponge.getEventManager().registerListeners(this, listenerConnection);
        Sponge.getEventManager().registerListeners(this, listenerWorldChange);
        logger.info("TitleManager has successfully been started!");
    }

    private <T> void addSerializer(final Class<T> clazz, final TypeSerializer<? super T> typeSerializer) {
        injector.injectMembers(typeSerializer);
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(clazz), typeSerializer);
    }

    public Text replacePlaceholders(final Player player, final Text text) {
        if (!containsVariable(text)) return text;

        return placeholderManager.replaceText(player, text);
    }

    public boolean containsVariable(final Text... texts) {
        for (final Text text : texts) {
            final String str = text.toPlain();

            if ((str.contains("{") && str.contains("}")) || str.contains("%")) return true;
        }

        return false;
    }

    public Set<Integer> getRunningAnimations() {
        return ImmutableSet.copyOf(runningAnimations);
    }

    public void addRunningAnimationId(final int id) {
        runningAnimations.add(id);
    }

    public void removeRunningAnimationId(final int id) {
        runningAnimations.remove(id);
    }

    public InputStream getResourceStream(final String fileName) {
        return this.getClass().getResourceAsStream("/" + fileName);
    }

    public URL getResourceURL(final String fileName) {
        return this.getClass().getResource("/" + fileName);
    }
}
