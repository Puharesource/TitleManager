package io.puharesource.mc.sponge.titlemanager.api;

import com.google.inject.Inject;
import io.puharesource.mc.sponge.titlemanager.TitleManager;
import io.puharesource.mc.sponge.titlemanager.api.iface.ActionbarSendable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.world.World;

/**
 * This is the standard actionbar message object.
 * It is used whenever both in actionbar animations and simply for displaying a message above the actionbar.
 */
public class ActionbarTitleObject implements ActionbarSendable {
    @Inject private TitleManager plugin;

    private Text title;

    public ActionbarTitleObject(final Text title) {
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
        player.sendMessage(ChatTypes.ACTION_BAR, plugin.replacePlaceholders(player, title));
    }

    public Text getTitle() {
        return title;
    }

    public void setTitle(final Text title) {
        this.title = title;
    }
}
