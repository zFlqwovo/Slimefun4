package city.norain.slimefun4.timings;

import city.norain.slimefun4.timings.entry.TimingEntry;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.Getter;
import org.bukkit.command.CommandSender;

public class SQLProfiler {
    private final ThreadFactory threadFactory = r -> new Thread(r, "Slimefun SQL Profiler");

    private final ExecutorService executor = Executors.newFixedThreadPool(2, threadFactory);

    @Getter
    private volatile boolean isProfiling = false;

    private final Map<TimingEntry, Long> samplingEntries = new HashMap<>();

    private final Map<TimingEntry, Long> entries = new HashMap<>();

    private final Set<CommandSender> subscribers = new HashSet<>();

    public void start() {
        if (isProfiling) return;

        samplingEntries.clear();
        entries.clear();
        subscribers.clear();

        isProfiling = true;
    }

    public void subscribe(@Nonnull CommandSender sender) {
        subscribers.add(sender);
    }

    public void recordEntry(TimingEntry timingEntry) {
        if (!isProfiling) return;

        long now = System.nanoTime();
        samplingEntries.put(timingEntry, now);
    }

    public void finishEntry(TimingEntry timingEntry) {
        if (!isProfiling) return;

        Long startTime = samplingEntries.remove(timingEntry);
        if (startTime == null) {
            return;
        }
        entries.put(timingEntry, System.nanoTime() - startTime);
    }

    public void stop() {
        if (!isProfiling) return;

        samplingEntries.clear();
        isProfiling = false;

        executor.execute(this::generateReport);
    }

    public void generateReport() {
        if (isProfiling || entries.isEmpty()) return;

        Map<String, List<Map.Entry<TimingEntry, Long>>> groupByEntries = entries.entrySet().stream()
                .collect(Collectors.groupingBy(entry -> entry.getKey().getIdentifier()));

        var reportPath = generateReportFile(groupByEntries);

        if (reportPath == null) {
            subscribers.forEach(sub -> Slimefun.getLocalization().sendMessage(sub, "sf-cn.timings.warning", true));
        } else {
            subscribers.forEach(sub -> Slimefun.getLocalization()
                    .sendMessage(sub, "sf-cn.timings.stopped", true, msg -> msg.replace("{0}", reportPath)));
        }

        subscribers.clear();
        entries.clear();
    }

    private String generateReportFile(Map<String, List<Map.Entry<TimingEntry, Long>>> entries) {
        if (entries.isEmpty()) {
            return null;
        }

        File reportFile =
                new File("plugins/Slimefun/sql-timings/", "sql-timing-report-" + System.currentTimeMillis() + ".txt");

        try {
            reportFile.createNewFile();
        } catch (Exception e) {
            Slimefun.logger().log(Level.WARNING, "Unable to create sql timing report!");
        }

        int entryCount = 0;
        Duration totalTime = Duration.ZERO;

        try (var writer = Files.newBufferedWriter(reportFile.toPath(), StandardCharsets.UTF_8)) {
            writer.append("Slimefun SQL Timing 报告");
            writer.newLine();
            writer.newLine();

            for (Map.Entry<String, List<Map.Entry<TimingEntry, Long>>> entry : entries.entrySet()) {
                String columnType = entry.getKey();
                List<Map.Entry<TimingEntry, Long>> value = entry.getValue().stream()
                        .sorted(Comparator.comparingLong(Map.Entry::getValue))
                        .toList();

                try {
                    writer.append("-- ").append(columnType);
                    writer.newLine();
                    for (Map.Entry<TimingEntry, Long> timingEntry : value) {
                        entryCount++;

                        var duration = Duration.ofNanos(timingEntry.getValue());
                        totalTime = totalTime.plus(duration);

                        var formattedTime = String.format(
                                "%ds%dms%dns",
                                duration.toSecondsPart(), duration.toMillisPart(), duration.toNanosPart());

                        writer.append(timingEntry.getKey().normalize())
                                .append(" -- ")
                                .append(formattedTime);
                        writer.newLine();
                    }
                    writer.newLine();
                    writer.newLine();
                } catch (IOException e) {
                    Slimefun.logger().log(Level.WARNING, "Unable to create sql timing report!", e);
                }
            }

            writer.append("总耗时: ")
                    .append(String.format(
                            "%dh%dm%dns", totalTime.toHours(), totalTime.toMinutesPart(), totalTime.toSecondsPart()));
            writer.newLine();
            writer.append("平均耗时: ")
                    .append(String.valueOf(totalTime.dividedBy(entryCount).toSeconds()))
                    .append(" 秒");
        } catch (IOException e) {
            Slimefun.logger().log(Level.WARNING, "Unable to create sql timing report!", e);
        }

        return reportFile.getAbsolutePath();
    }
}
