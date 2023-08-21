package io.github.thebusybiscuit.slimefun4.implementation.tasks;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.bakedlibs.dough.blocks.BlockPosition;
import io.github.thebusybiscuit.slimefun4.api.ErrorReport;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.thread.TickerThreadFactory;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.tasks.ticker.TickerCollection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * The {@link TickerTask} is responsible for ticking every {@link BlockTicker},
 * synchronous or not.
 *
 * @author TheBusyBiscuit
 * @see BlockTicker
 */
public class TickerTask {
    /**
     * This is the maximum time we wait a sync task to finished.
     * If overtime, the task will be re-executed in next tick.
     */
    private static final long MAX_WAIT_TIME = 1_250_000L;

    /**
     * This is the maximum time we poll out a task.
     */
    private static final long MAX_POLL_TIME = 250_000L;

    /**
     * This represents an empty runnable task, for marking async task is completed.
     */
    private static final Runnable EMPTY_ELEMENT = () -> {
    };

    /**
     * This collection holds all currently actively ticking locations.
     */
    private final TickerCollection tickerCollection = new TickerCollection();

    /**
     * This Map tracks how many bugs have occurred in a given Location .
     * If too many bugs happen, we delete that Location.
     */
    private final Map<BlockPosition, Integer> bugs = new ConcurrentHashMap<>();
    private final BlockingQueue<Runnable> syncTasks = new ArrayBlockingQueue<>(48);
    private final TickerThreadFactory tickerThreadFactory = new TickerThreadFactory();
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), tickerThreadFactory);

    private Slimefun plugin;
    private int tickRate;
    private final AtomicInteger activeAsyncTaskCount = new AtomicInteger(0);

    private boolean halted = false;
    private volatile boolean paused = false;

    /**
     * This method starts the {@link TickerTask} on an asynchronous schedule.
     *
     * @param plugin
     *            The instance of our {@link Slimefun}
     */
    public void start(@Nonnull Slimefun plugin) {
        this.tickRate = Slimefun.getCfg().getInt("URID.custom-ticker-delay");
        this.plugin = plugin;

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        scheduler.runTaskLater(plugin, this::tick, 100L);
    }

    public void tick() {
        // Ticker is paused, let wait until it started.
        if (paused) {
            return;
        }

        if (activeAsyncTaskCount.get() > 0) {
            processSyncTasks();
            return;
        }

        if (!Bukkit.isPrimaryThread()) {
            return;
        }

        processAsyncTasks();
        processSyncTasks();
    }

    public void nextTick() {
        activeAsyncTaskCount.getAndAdd(0);

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::tick, tickRate);
    }

    private void processAsyncTasks() {
        try {
            Slimefun.getProfiler().start();

            Set<BlockTicker> tickers = new CopyOnWriteArraySet<>();

            if (!halted) {
                var tickerBlocks = tickerCollection.getBlocksLocation();
                activeAsyncTaskCount.getAndSet(tickerBlocks.size());
                tickLocations(tickers, tickerBlocks);
            }
        } catch (Exception | LinkageError x) {
            Slimefun.logger().log(Level.SEVERE, x, () -> "An Exception was caught while ticking the Block Tickers Task for Slimefun v" + Slimefun.getVersion());
        }
    }

    private void processSyncTasks() {
        if (!Bukkit.isPrimaryThread()) {
            return;
        }

        long deadlineTime = System.nanoTime() + MAX_WAIT_TIME;

        try {
            while (!plugin.isEnabled() || deadlineTime > System.nanoTime()) {
                var task = syncTasks.poll(MAX_POLL_TIME, TimeUnit.NANOSECONDS);

                if (task == null) {
                    if (!plugin.isEnabled()) {
                        break;
                    }
                } else if (task == EMPTY_ELEMENT) {
                    // Async task is finished, let move to next tick.
                    nextTick();
                    return;
                } else {
                    task.run();
                }
            }
        } catch (InterruptedException x) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!isHalted()) {
            Bukkit.getScheduler().runTaskLater(plugin, this::processSyncTasks, 4L);
        }
    }

    private void tickLocations(@Nonnull Set<BlockTicker> tickers, @Nonnull Set<Location> locations) {
        for (Location l : locations) {
            var blockData = StorageCacheUtils.getBlock(l);

            if (blockData == null || !blockData.isDataLoaded() || blockData.isPendingRemove()) {
                continue;
            }

            try {
                SlimefunItem item = SlimefunItem.getById(blockData.getSfId());

                if (item != null && item.getBlockTicker() != null) {
                    try {
                        var ticker = item.getBlockTicker();
                        if (!ticker.isSynchronized()) {
                            tickers.add(item.getBlockTicker());
                        }
                        runTask(tickers, l, blockData, item);
                    } catch (Exception x) {
                        reportErrors(l, item, x);
                    }
                }
            } catch (Exception | LinkageError x) {
                Slimefun.logger().log(Level.SEVERE, x, () -> "An Exception was caught while ticking the Block Tickers Task for Slimefun v" + Slimefun.getVersion());
            }
        }
    }

    private void finishTickLocation(@Nonnull Set<BlockTicker> tickers) {
        if (activeAsyncTaskCount.decrementAndGet() == 0) {
            tickers.forEach(BlockTicker::startNewTick);

            Slimefun.getProfiler().stop();
            try {
                // Notify the sync task processor that the end of the ticker list has been reached.
                syncTasks.put(EMPTY_ELEMENT);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @ParametersAreNonnullByDefault
    private void runTask(@Nonnull Set<BlockTicker> tickers, Location l, SlimefunBlockData data, SlimefunItem item) {
        BlockTicker ticker = item.getBlockTicker();

        // Check on which Thread to run this.
        if (ticker.isSynchronized()) {
            // Inform our profiler that we will be scheduling a task.
            Slimefun.getProfiler().scheduleEntries(1);

            // Start a new iteration for our block ticker
            ticker.update();

            /*
             * We are inserting a new timestamp because synchronized
             * actions are always ran with a 50ms delay (1 game tick).
             */
            try {
                syncTasks.put(() -> {
                    Block b = l.getBlock();
                    tickBlock(l, b, item, data, System.nanoTime());
                    ticker.startNewTick();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Manually ignore sync ticker
            activeAsyncTaskCount.decrementAndGet();
        } else {
            asyncExecutor.submit(() -> {
                long timestamp = Slimefun.getProfiler().newEntry();
                ticker.update();
                Block b = l.getBlock();
                tickBlock(l, b, item, data, timestamp);
                finishTickLocation(tickers);
            });
        }
    }

    @ParametersAreNonnullByDefault
    private void tickBlock(Location l, Block b, SlimefunItem item, SlimefunBlockData data, long timestamp) {
        try {
            item.getBlockTicker().tick(b, item, data);
        } catch (Exception | LinkageError x) {
            reportErrors(l, item, x);
        } finally {
            Slimefun.getProfiler().closeEntry(l, item, timestamp);
        }
    }

    @ParametersAreNonnullByDefault
    private void reportErrors(Location l, SlimefunItem item, Throwable x) {
        BlockPosition position = new BlockPosition(l);
        int errors = bugs.getOrDefault(position, 0) + 1;

        if (errors == 1) {
            // Generate a new Error-Report
            new ErrorReport<>(x, l, item);
            bugs.put(position, errors);
        } else if (errors == 4) {
            Slimefun.logger().log(Level.SEVERE, "X: {0} Y: {1} Z: {2} ({3})", new Object[]{l.getBlockX(), l.getBlockY(), l.getBlockZ(), item.getId()});
            Slimefun.logger().log(Level.SEVERE, "在过去的 4 个 Tick 中发生多次错误，该方块对应的机器已被停用。");
            Slimefun.logger().log(Level.SEVERE, "请在 /plugins/Slimefun/error-reports/ 文件夹中查看错误详情。");
            Slimefun.logger().log(Level.SEVERE, " ");
            bugs.remove(position);

            disableTicker(l);
        } else {
            bugs.put(position, errors);
        }
    }

    public boolean isHalted() {
        return halted;
    }

    public void halt() {
        Slimefun.logger().info("正在处理未完成工作的机器, 这可能需要一点时间...");
        halted = true;
        tick();

        activeAsyncTaskCount.getAndSet(0);

        asyncExecutor.shutdown();

        try {
            if (!asyncExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                Slimefun.logger().log(Level.SEVERE, "处理机器工作超时!");
            }
        } catch (InterruptedException ex) {
            Slimefun.logger().log(Level.SEVERE, ex, () -> "关闭异步执行器时出现问题");
            /*
             * When the exception is fired, the Thread is un-marked as interrupted, so we re-mark it.
             */
            Thread.currentThread().interrupt();
        }

        while (!syncTasks.isEmpty()) {
            syncTasks.poll().run();
        }

        Slimefun.logger().info("成功完成剩余任务.");
    }

    /**
     * This returns the delay between ticks
     * 
     * @return The tick delay
     */
    public int getTickRate() {
        return tickRate;
    }

    /**
     * This method returns a <strong>read-only</strong> {@link Set}
     * of all ticking {@link Location Locations} in a given {@link Chunk}.
     * The {@link Chunk} does not have to be loaded.
     * If no {@link Location} is present, the returned {@link Set} will be empty.
     * 
     * @param chunk
     *            The {@link Chunk}
     * 
     * @return A {@link Set} of all ticking {@link Location Locations}
     */
    @Nonnull
    public Set<Location> getLocations(@Nonnull Chunk chunk) {
        Validate.notNull(chunk, "The Chunk cannot be null!");

        Set<Location> locations = tickerCollection.getBlocksLocation();
        return Collections.unmodifiableSet(locations);
    }

    /**
     * This enables the ticker at the given {@link Location} and adds it to our "queue".
     * 
     * @param l
     *            The {@link Location} to activate
     */
    public void enableTicker(@Nonnull Location l) {
        Validate.notNull(l, "Location cannot be null!");

        synchronized (tickerCollection) {
            tickerCollection.tickBlock(l);
        }
    }

    /**
     * This method disables the ticker at the given {@link Location} and removes it from our internal
     * "queue".
     * 
     * @param l
     *            The {@link Location} to remove
     */
    public void disableTicker(@Nonnull Location l) {
        Validate.notNull(l, "Location cannot be null!");

        synchronized (tickerCollection) {
            tickerCollection.removeBlock(l);
        }
    }

    public void setPaused(boolean isPaused) {
        paused = isPaused;
    }

}
