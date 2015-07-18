package io.puharesource.mc.titlemanager.backend.packet.v1_7;

import io.puharesource.mc.titlemanager.TitleManager;
import io.puharesource.mc.titlemanager.backend.packet.Packet;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionClass;
import io.puharesource.mc.titlemanager.backend.reflections.ReflectionManager;
import io.puharesource.mc.titlemanager.backend.reflections.managers.ReflectionManagerProtocolHack1718;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public final class ItemMessagePacket extends Packet {
    private @Getter Object handle;

    @SneakyThrows
    public ItemMessagePacket(final String text, final Player player, final int slot) {
        ReflectionManager manager = TitleManager.getInstance().getReflectionManager();

        if (manager instanceof ReflectionManagerProtocolHack1718) {
            val item = player.getInventory().getItem(slot);
            if (item == null) return;

            val normalItem = item.clone();
            if (text != null) {
                ItemMeta meta = normalItem.getItemMeta();
                meta.setDisplayName(text);
                normalItem.setItemMeta(meta);
            }

            Map<String, ReflectionClass> classes = manager.getClasses();

            Object nmsItem = classes.get("CraftItemStack").getMethod("asNMSCopy", ItemStack.class).invoke(null, normalItem);
            handle = classes.get("PacketPlayOutSetSlot").getHandle()
                    .getConstructor(Integer.TYPE, Integer.TYPE, classes.get("ItemStack").getHandle())
                    .newInstance(0, slot + 36, nmsItem);
        }
    }
}
