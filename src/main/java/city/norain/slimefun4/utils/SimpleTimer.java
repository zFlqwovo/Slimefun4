package city.norain.slimefun4.utils;

import java.time.Duration;
import java.time.LocalDateTime;

public class SimpleTimer {
    private final LocalDateTime startTime;
    private Duration snapshot = null;

    public SimpleTimer() {
        startTime = LocalDateTime.now();
    }

    public Duration duration() {
        var duration = Duration.between(startTime, LocalDateTime.now());
        snapshot = duration;
        return duration;
    }

    public String durationStr() {
        var duration = duration();
        return String.format("%d:%02d:%02d", duration.toHours(), duration.toMinutesPart(),
            duration.toSecondsPart());
    }

    public boolean isTimeout(Duration duration) {
        if (snapshot == null) {
            return false;
        }

        return snapshot.compareTo(duration) > 0;
    }
}
