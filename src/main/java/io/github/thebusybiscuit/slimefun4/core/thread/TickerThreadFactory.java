package io.github.thebusybiscuit.slimefun4.core.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

public record TickerThreadFactory() implements ThreadFactory {

    /**
     * This creates a new {@link Thread} for the {@link io.github.thebusybiscuit.slimefun4.implementation.tasks.TickerTask}.
     */
    @Override
    public Thread newThread(@Nonnull Runnable runnable) {
        return new Thread(runnable, "SF Ticker Thread");
    }
}
