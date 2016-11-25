package io.puharesource.mc.titlemanager.api.animations;

import io.puharesource.mc.titlemanager.api.TabTitleObject;
import io.puharesource.mc.titlemanager.api.iface.IAnimation;
import io.puharesource.mc.titlemanager.api.iface.ITabObject;
import io.puharesource.mc.titlemanager.scheduling.AsyncScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * This is the actionbar title animation.
 * It can send a sequence of actionbar messages to the player, making it look like an animation.
 */
@Deprecated
public class TabTitleAnimation implements IAnimation, ITabObject {
    private Object header;
    private Object footer;

    @Deprecated
    public TabTitleAnimation(FrameSequence header, FrameSequence footer) {
        this((Object) header, (Object) footer);
    }

    @Deprecated
    public TabTitleAnimation(FrameSequence header, String footer) {
        this((Object) header, (Object) footer);
    }

    @Deprecated
    public TabTitleAnimation(String header, FrameSequence footer) {
        this((Object) header, (Object) footer);
    }

    @Deprecated
    public TabTitleAnimation(Object header, Object footer) {
        if (header != null && !(header instanceof FrameSequence) && !(header instanceof String)) throw new IllegalArgumentException("The header must be a String or a FrameSequence!");
        if (footer != null && !(footer instanceof FrameSequence) && !(footer instanceof String)) throw new IllegalArgumentException("The footer must be a String or a FrameSequence!");
        this.header = header;
        this.footer = footer;
    }

    @Override
    @Deprecated
    public void broadcast() {
        send(null);
    }

    @Override
    @Deprecated
    public void send(Player player) {
        final AsyncScheduler engine = AsyncScheduler.INSTANCE;

        int times = 0;
        if (header instanceof FrameSequence && footer instanceof FrameSequence) {
            FrameSequence headerSequence = (FrameSequence) header;
            FrameSequence footerSequence = (FrameSequence) footer;
            MultiTask task = new MultiTask(headerSequence, footerSequence, player);
            int id = engine.schedule(task, 0, 1);
            task.setId(id);
        } else if (header instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) header).getFrames()) {
                Task task = new Task(frame.getText(), (String) footer, player);
                int id = engine.schedule(task, times, ((FrameSequence) header).getTotalTime());
                task.setId(id);
                times += frame.getTotalTime();
            }
        } else if (footer instanceof FrameSequence) {
            for (AnimationFrame frame : ((FrameSequence) footer).getFrames()) {
                Task task = new Task((String) header, frame.getText(), player);
                int id = engine.schedule(task, times, ((FrameSequence) footer).getTotalTime());
                task.setId(id);
                times += frame.getTotalTime();
            }
        }
    }

    @Deprecated
    private class Task implements Runnable {
        private String header;
        private String footer;

        private UUID uuid;
        private int id;

        @Deprecated
        public Task(String header, String footer, UUID uuid) {
            this.header = header;
            this.footer = footer;
            this.uuid = uuid;
        }

        @Deprecated
        public Task(String header, String footer, Player player) {
            this(header, footer, player == null ? null : player.getUniqueId());
        }

        @Deprecated
        public Task(String title, TabTitleObject.Position position, UUID uuid) {
            if (position == TabTitleObject.Position.HEADER)
                header = title;
            else footer = title;
            this.uuid = uuid;
        }

        @Deprecated
        public Task(String title, TabTitleObject.Position position, Player player) {
            this(title, position, player == null ? null : player.getUniqueId());
        }

        @Deprecated
        public void setId(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            if (uuid == null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (header != null && footer == null)
                        new TabTitleObject(header, TabTitleObject.Position.HEADER).send(player);
                    else if (header == null && footer != null)
                        new TabTitleObject(footer, TabTitleObject.Position.FOOTER).send(player);
                    else new TabTitleObject(header, footer).send(player);
                }
            } else {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    if (header != null && footer == null)
                        new TabTitleObject(header, TabTitleObject.Position.HEADER).send(player);
                    else if (header == null && footer != null)
                        new TabTitleObject(footer, TabTitleObject.Position.FOOTER).send(player);
                    else new TabTitleObject(header, footer).send(player);
                } else {
                    AsyncScheduler.INSTANCE.cancel(id);
                }
            }
        }
    }

    @Deprecated
    private class MultiTask implements Runnable {
        private int i;
        private int j;

        private int id;

        private int headerTimeLeft;
        private int footerTimeLeft;

        private AnimationFrame currentHeaderFrame;
        private AnimationFrame currentFooterFrame;

        private FrameSequence header;
        private FrameSequence footer;
        private UUID uuid;

        @Deprecated
        public MultiTask(FrameSequence header, FrameSequence footer, UUID uuid) {
            this.header = header;
            this.footer = footer;
            this.uuid = uuid;
        }

        @Deprecated
        public MultiTask(FrameSequence header, FrameSequence footer, Player player) {
            this(header, footer, player == null ? null : player.getUniqueId());
        }

        @Deprecated
        public void setId(int id) {
            this.id = id;
        }

        @Override
        @Deprecated
        public void run() {
            boolean headerChanged = false;
            boolean footerChanged = false;

            if (headerTimeLeft == 0 || footerTimeLeft == 0) {
                if (headerTimeLeft == 0) {
                    currentHeaderFrame = header.getFrames().get(i);
                    headerTimeLeft = header.getFrames().get(i).getTotalTime();
                    if (header.getFrames().size() - 1 == i)
                        i = 0;
                    else i++;
                    headerChanged = true;
                }

                if (footerTimeLeft == 0) {
                    currentFooterFrame = footer.getFrames().get(j);
                    footerTimeLeft = footer.getFrames().get(j).getTotalTime();
                    if (footer.getFrames().size() - 1 == j)
                        j = 0;
                    else j++;
                    footerChanged = true;
                }

                if (uuid == null) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        new TabTitleObject(currentHeaderFrame.getText(), currentFooterFrame.getText()).send(player);
                    }
                } else {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        new TabTitleObject(currentHeaderFrame.getText(), currentFooterFrame.getText()).send(player);
                    } else {
                        AsyncScheduler.INSTANCE.cancel(id);;
                    }
                }
            }

            if (!headerChanged)
                headerTimeLeft--;
            if (!footerChanged)
                footerTimeLeft--;
        }
    }
}