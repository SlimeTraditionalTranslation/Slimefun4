package com.xzavier0722.mc.plugin.slimefun4.storage.task;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

public class DatabaseThreadFactory implements ThreadFactory {
    private final AtomicInteger threadCount = new AtomicInteger(0);

    @Override
    public Thread newThread(@Nonnull Runnable r) {
        return new Thread(r, "SF-Database-Thread #" + threadCount.getAndIncrement());
    }
}
