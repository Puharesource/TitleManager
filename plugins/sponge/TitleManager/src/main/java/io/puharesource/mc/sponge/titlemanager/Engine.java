package io.puharesource.mc.sponge.titlemanager;

import org.apache.commons.lang3.Validate;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class Engine {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<Integer, ScheduledFuture<?>> scheduled = new ConcurrentHashMap<>();
    private final AtomicInteger ids = new AtomicInteger(0);

    public int schedule(final Runnable runnable, final int delay) {
        Validate.notNull(runnable);

        final int id = ids.incrementAndGet();

        scheduled.put(id, scheduler.schedule(() -> {
            runnable.run();
            scheduled.remove(id);
        }, delay * 50, TimeUnit.MILLISECONDS));

        return id;
    }

    public int schedule(final Runnable runnable, final int delay, final int period) {
        Validate.notNull(runnable);

        final int id = ids.incrementAndGet();

        scheduled.put(id, scheduler.scheduleAtFixedRate(runnable, delay * 50, period * 50, TimeUnit.MILLISECONDS));

        return id;
    }

    public void cancel(final int taskId) {
        if (scheduled.containsKey(taskId)) {
            scheduled.get(taskId).cancel(false);
            scheduled.remove(taskId);
        }
    }

    public void cancelAll() {
        scheduled.values().forEach(task -> task.cancel(false));
        scheduled.clear();
    }
}
