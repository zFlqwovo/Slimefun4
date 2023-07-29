package city.norain.slimefun4.utils;

import java.time.Duration;

public class Timer {
    private final long startTimestamp;

    public Timer() {
        startTimestamp = System.nanoTime();
    }

    public long measureNanos() {
        return System.nanoTime() - startTimestamp;
    }

    public Duration measureDuration() {
        return Duration.ofNanos(measureNanos());
    }

    public static Timer createTimer() {
        return new Timer();
    }
}
