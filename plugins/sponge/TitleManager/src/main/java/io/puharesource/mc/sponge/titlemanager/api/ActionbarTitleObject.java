package io.puharesource.mc.sponge.titlemanager.api;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.iface.IActionbarObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.world.World;

/**
 * This is the standard actionbar message object.
 * It is used whenever both in actionbar animations and simply for displaying a message above the actionbar.
 */
public class ActionbarTitleObject implements IActionbarObject {
    @Inject private TitleManager plugin;

    private String title;

    public ActionbarTitleObject(String title) {
        setTitle(title);
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
        player.sendMessage(ChatTypes.ACTION_BAR, Text.of(plugin.setVariables(player, title)));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
