package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.ReflectionManager;
import io.puharesource.mc.titlemanager.TitleManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TitleAnimation extends BukkitRunnable {

    private int speed;
    private Object title;
    private Object subtitle;

    private Player player;

    private ReflectionManager manager;

    public TitleAnimation(Player player, int speed, StringAnimation title, StringAnimation subtitle) {
        this(player, speed, title, (Object) subtitle);
    }

    public TitleAnimation(Player player, int speed, StringAnimation title, String subtitle) {
        this(player, speed, title, (Object) subtitle);
    }

    public TitleAnimation(Player player, int speed, String title, StringAnimation subtitle) {
        this(player, speed, title, (Object) subtitle);
    }

    TitleAnimation(Player player, int speed, Object title, Object subtitle) {
        manager = TitleManager.getReflectionManager();
        this.player = player;
        this.speed = speed;
        this.title = (title instanceof String) ? manager.getIChatBaseComponent((String) title) : title;
        this.subtitle = (subtitle instanceof String) ? manager.getIChatBaseComponent((String) subtitle) : subtitle;
    }

    @Override
    public void run() {
        if (player == null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                manager.sendPacket(manager.constructTitleTimingsPacket(0, speed, 0), player);
                manager.sendPacket(manager.constructTitlePacket(false, (title instanceof StringAnimation) ? ((StringAnimation) title).nextAndGet() : title), player);
                manager.sendPacket(manager.constructTitlePacket(true, (subtitle instanceof StringAnimation) ? ((StringAnimation) subtitle).nextAndGet() : subtitle), player);
            }
        } else {
            manager.sendPacket(manager.constructTitleTimingsPacket(0, speed, 0), player);
            manager.sendPacket(manager.constructTitlePacket(false, (title instanceof StringAnimation) ? ((StringAnimation) title).nextAndGet() : title), player);
            manager.sendPacket(manager.constructTitlePacket(true, (subtitle instanceof StringAnimation) ? ((StringAnimation) subtitle).nextAndGet() : subtitle), player);
        }
    }
}
