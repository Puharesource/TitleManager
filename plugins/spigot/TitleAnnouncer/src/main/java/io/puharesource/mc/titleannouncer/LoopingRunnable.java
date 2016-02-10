package io.puharesource.mc.titleannouncer;

import io.puharesource.mc.titlemanager.api.iface.ISendable;

import java.util.Iterator;
import java.util.List;

public final class LoopingRunnable<T extends ISendable> implements Runnable {
    private final List<T> list;
    private Iterator<T> it;

    public LoopingRunnable(final List<T> list) {
        this.list = list;
        it = list.iterator();
    }

    @Override
    public void run() {
        if (list.isEmpty()) return;

        if (it.hasNext()) {
            it.next().broadcast();
        } else {
            it = list.iterator();
            it.next().broadcast();
        }
    }
}
