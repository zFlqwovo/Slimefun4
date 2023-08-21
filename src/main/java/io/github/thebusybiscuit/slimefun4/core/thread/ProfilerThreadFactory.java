package io.github.thebusybiscuit.slimefun4.core.thread;

import io.github.thebusybiscuit.slimefun4.core.services.profiler.SlimefunProfiler;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nonnull;

/**
 * This is our {@link ThreadFactory} for the {@link SlimefunProfiler}.
 * It holds the amount of {@link Thread Threads} we dedicate towards our {@link SlimefunProfiler}
 * and provides a naming convention for our {@link Thread Threads}.
 *
 * @author TheBusyBiscuit
 * @see SlimefunProfiler
 */
public record ProfilerThreadFactory(int threadCount) implements ThreadFactory {
    /**
     * This returns the amount of {@link Thread Threads} we dedicate towards
     * the {@link SlimefunProfiler}.
     *
     * @return The {@link Thread} count
     */
    @Override
    public int threadCount() {
        return threadCount;
    }

    /**
     * This creates a new {@link Thread} for the {@link SlimefunProfiler}.
     */
    @Override
    public Thread newThread(@Nonnull Runnable runnable) {
        return new Thread(runnable, "Slimefun Profiler");
    }

}